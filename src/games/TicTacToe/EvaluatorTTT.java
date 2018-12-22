package games.TicTacToe;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

import agentIO.AgentLoader;
import controllers.MinimaxAgent;
import controllers.PlayAgent;
import controllers.RandomAgent;
import games.ArenaTrain;
import games.Evaluator;
import games.TicTacToe.Evaluator9;
import games.GameBoard;
import games.StateObservation;
import games.XArenaFuncs;
import tools.MessageBox;
import tools.Types;

/**
 * Same as {@link Evaluator9}, but instead of {@link Evaluator9#evalAgent1(PlayAgent, boolean)} use
 * <ul>
 * <li> if mode=1: {@link EvaluatorTTT#evaluateAgent1(PlayAgent,GameBoard)} (competition with MinimaxPlayer and RandomPlayer) or 
 * <li> if mode=2: {@link EvaluatorTTT#evaluateAgent2(PlayAgent,PlayAgent,GameBoard)} (competition with MinimaxPlayer from different start positions). 
 * </ul>  
 * The value of mode is set in the constructor. Class Evaluator2 works also for featmode==3.
 */
public class EvaluatorTTT extends Evaluator {
 	private static final int[] AVAILABLE_MODES = {-1,0,1,2,9,11};
	private RandomAgent random_agent = new RandomAgent("Random");
	private MinimaxAgent minimax_agent = new MinimaxAgent("Minimax");
	private AgentLoader agtLoader = null;
	private Evaluator9 m_evaluator9 = null; 
	private int m_mode;
	private double m_res=-1;		// avg. success against RandomPlayer, best is 0.9 (m_mode=0)
									// or against MinimaxPlayer, best is 0.0 (m_mode=1,2)
	private String m_msg;
	protected double[] m_thresh={0.8,-0.15,-0.15}; // threshold for each value of m_mode
	private GameBoard m_gb;
	
	public EvaluatorTTT(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
		super(e_PlayAgent, stopEval);
		m_evaluator9 = new Evaluator9(e_PlayAgent,stopEval);
		initEvaluator(gb,1);
	}

	public EvaluatorTTT(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode) {
		super(e_PlayAgent, stopEval);
		m_evaluator9 = new Evaluator9(e_PlayAgent,stopEval);
		initEvaluator(gb,mode);
	}

	public EvaluatorTTT(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, stopEval, verbose);
		m_evaluator9 = new Evaluator9(e_PlayAgent,stopEval);
		initEvaluator(gb,mode);
	}
	
	private void initEvaluator(GameBoard gb, int mode) {
		if (!isAvailableMode(mode)) 
			throw new RuntimeException("EvaluatorTTT: Value mode = "+mode+" is not allowed!");
		m_mode = mode;
		m_gb = gb;				
	}
	
//	/**	
//	 * Known callers of eval (outside this class): 
//	 * 		{@link ArenaTrain#run()}, case TRAIN_X, TRAIN_O, 
//	 * 		{@link XArenaFuncs#train(String, XArenaButtons, GameBoard)},
//	 */
//	public boolean eval() {
//		return setState(eval_Agent());
//	}
	
	/**
	 * 
	 * @return true if evaluateAgentX is above m_thresh.<br>
	 * The choice for X=1 or 2 is made with 3rd parameter mode in 
	 * {@link #EvaluatorTTT(PlayAgent, GameBoard, int, int)} [default mode=1].<p>
	 * 
	 * If mode==0, then m_thresh=0.8 (best: 0.9, worst: 0.0) <br>
	 * If mode==1 or 2, then m_thresh=-0.15 (best: 0.0, worst: -1.0)
	 */
	@Override
	public boolean eval_Agent(PlayAgent playAgent) {
		m_PlayAgent = playAgent;
		switch(m_mode) {
		case 0:  return evaluateAgent0(m_PlayAgent,m_gb)>m_thresh[0];
		case 1:  return evaluateAgent1(m_PlayAgent,m_gb)>m_thresh[1];
		case 2:  return evaluateAgent2(m_PlayAgent,minimax_agent,m_gb)>m_thresh[2];
		case 11: 
			if (agtLoader==null) agtLoader = new AgentLoader(m_gb.getArena(),"TDReferee.agt.zip");
			return evaluateAgent2(m_PlayAgent,agtLoader.getAgent(),m_gb)>m_thresh[2];
		case 9:  return m_evaluator9.eval_Agent(playAgent);
		default: return false;
		}
	}
	
	/**	
	 * @param gb		needed to get a default start state (competeBoth)
 	 * @return
	 * Known callers of evaluateAgent1 (outside this class): 
	 * 		{@link XArenaFuncs#multiTrain(String,TicGameButtons)}
	 */
 	private double evaluateAgent0(PlayAgent pa, GameBoard gb) {
 		StateObservation so = gb.getDefaultStartState();
		m_res = XArenaFuncs.competeBoth(pa, random_agent, so, 100, 0, gb);
		m_msg = pa.getName()+": "+getPrintString() + m_res;
		if (this.verbose>0) System.out.println(m_msg);
		return m_res;
	}

 	/**	
	 * @param gb		needed to get a default start state (competeBoth)
 	 * @return
	 * Known callers of evaluateAgent1 (outside this class): 
	 * 		{@link XArenaFuncs#multiTrain(String,TicGameButtons)}
	 */
 	private double evaluateAgent1(PlayAgent pa, GameBoard gb) {
 		StateObservation so = gb.getDefaultStartState();
		m_res = XArenaFuncs.competeBoth(pa, minimax_agent, so, 1, 0, gb);
		m_msg = pa.getName()+": "+getPrintString() + m_res;
		if (this.verbose>0) System.out.println(m_msg);
		return m_res;
	}
 	
 	/**
 	 * 
 	 * @param pa
	 * @param gb		needed to get a default start state (competeBoth)
 	 * @return
 	 */
 	private double evaluateAgent2(PlayAgent pa, PlayAgent opponent, GameBoard gb) {
		int verbose=0;
		int competeNum=1;
		int startPlayer=+1;
		int[][] startTable=new int[3][3];
		double[] res;
		double resX, resO;
		// all these start states have the result "Tie" for perfect playing agents:
		String state[] = {"---------","--------X","-------X-"
						, "------X--","-----X---","----X----"
						, "---X-----","--X------","-X-------"
						, "X--------"};
		m_res=0;
		
		if (opponent == null) {
			String tdstr = agtLoader.getLoadMsg() + " (no opponent)";
			MessageBox.show(gb.getArena(),"ERROR: " + tdstr,
					"Load Error", JOptionPane.ERROR_MESSAGE);
			m_res = Double.NaN;
			return m_res;
		} 

		for (int k=0; k<state.length; ++k) {
			startPlayer = Evaluator9.string2table(state[k],startTable);
			StateObserverTTT startSO = new StateObserverTTT(startTable,startPlayer);
			res = XArenaFuncs.compete(pa, opponent, startSO, competeNum, verbose);
			resX  = res[0] - res[2];		// X-win minus O-win percentage, \in [-1,1]
											// resp. \in [-1,0], if opponent never looses.
											// +1 is best for pa, -1 worst for pa.
			res = XArenaFuncs.compete(opponent, pa, startSO, competeNum, verbose);
			resO  = res[2] - res[0];		// O-win minus X-win percentage, \in [-1,1]
											// resp. \in [-1,0], if opponent never looses.
											// +1 is best for pa, -1 worst for pa.
			m_res += (resX+resO)/2.0;
		}
		m_res=m_res/state.length;
		
		m_msg = pa.getName()+": "+getPrintString() + m_res;
		if (this.verbose>0) System.out.println(m_msg);
		
		return m_res;
	}
 	
 	/**
 	 * @return mean success rate against {@link MinimaxAgent}, best is 0.0. Either when starting from
 	 * empty board ({@code mode==1}) or from different start positions ({@code mode==2}), 
 	 * depending on {@code mode} as set in constructor.
 	 */
 	public double getOm() { return m_res; }
 	
 	@Override
 	public double getLastResult() { 
 		return (m_mode==9) ? m_evaluator9.getLastResult() : m_res; 
 	}
 	@Override
 	public String getMsg() { 
 		return (m_mode==9) ? m_evaluator9.getMsg() : m_msg; } 
 	
	@Override
 	public boolean isAvailableMode(int mode) {
		for (int i : AVAILABLE_MODES) {
			if (mode==i) return true;
		}
		return false;
 	}
 	
 	@Override
 	public int[] getAvailableModes() {
 		return AVAILABLE_MODES;
 	}
 	
 	//@Override
 	public static int getDefaultEvalMode() {
		return AVAILABLE_MODES[2];		// mode 2
	}
 	
	public int getQuickEvalMode() 
	{
		return 2;
	}
	public int getTrainEvalMode() 
	{
		return 9;
	}
	public int getMultiTrainEvalMode() 
	{
		return 0;
	}

	@Override
	public String getPrintString() {
		switch (m_mode) {
		case 0:  return "success rate (randomAgent, best is 0.9): ";
		case 1:  return "success rate (minimax, best is 0.0): ";
		case 2:  return "success rate (minimax, different starts, best is 0.0): ";
		case 9:  return "success rate (Evaluator9, best is ?): ";	
		case 11: return "success rate (TDReferee, different starts, best is 0.0): ";
		default: return null;
		}
	}
	
	@Override
	public String getTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>-1: none<br>"
				+ "0: against Random, best is 0.9<br>"
				+ "1: against Minimax, best is 0.0<br>"
				+ "2: against Minimax, different starts, best is 0.0<br>"
				+ "9: evaluate set of states, best is ?<br>"
				+ "11: against TDReferee.agt.zip, different starts"
				+ "</html>";
	}

	@Override
	public String getPlotTitle() {
		switch (m_mode) {
		case 0:  return "success against Random";
		case 1:  return "success against Minimax";
		case 2:  return "success against Minimax, dStart";
		case 9:  return "success Evaluator9";		
		case 11: return "success TDReferee";		
		default: return null;
		}
	}

}
