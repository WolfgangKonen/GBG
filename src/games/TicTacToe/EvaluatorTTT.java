package games.TicTacToe;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import controllers.MinimaxAgent;
import controllers.PlayAgent;
import controllers.RandomAgent;
import games.ArenaTrain;
import games.Evaluator;
import games.GameBoard;
import games.XArenaFuncs;
import tools.Types;

/**
 * Same as {@link Evaluator9}, but instead of {@link Evaluator9#evalAgent1(PlayAgent)} use <p>
 * if mode=1: {@link EvaluatorTTT#evaluateAgent1(PlayAgent,GameBoard)} (competition with MinimaxPlayer and RandomPlayer) or <p>
 * if mode=2: {@link EvaluatorTTT#evaluateAgent2(PlayAgent,GameBoard)} (competition with MinimaxPlayer from different start positions). 
 * <p>  
 * The value of mode is set in the constructor. Class Evaluator2 works also for featmode==3.
 */
public class EvaluatorTTT extends Evaluator {
 	private static final int[] AVAILABLE_MODES = {0,1,2,9};
	private String sRandom = Types.GUI_AGENT_LIST[2];
	private String sMinimax = Types.GUI_AGENT_LIST[1];
	private RandomAgent random_agent = new RandomAgent(sRandom);
	private MinimaxAgent minimax_agent = new MinimaxAgent(sMinimax);
	private Evaluator9 m_evaluator9 = null; 
	private int m_mode;
	private double m_om=-1;			// avg. success against RandomPlayer, best is 0.9 (m_mode=0)
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
			throw new RuntimeException("Value mode = "+mode+" is not allowed for EvaluatorTTT!");
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
	 * @return true if evaluateAgentX is above m_thresh=-0.15 (best: 0.0, worst: -1.0).
	 * The choice for X=1 or 2 is made with 3rd parameter mode in 
	 * {@link #EvaluatorTTT(PlayAgent, GameBoard, int, int)} [default mode=1].
	 */
	@Override
	public boolean eval_Agent() {
		switch(m_mode) {
		case 0: return evaluateAgent0(m_PlayAgent,m_gb)>m_thresh[0];
		case 1: return evaluateAgent1(m_PlayAgent,m_gb)>m_thresh[1];
		case 2: return evaluateAgent2(m_PlayAgent,m_gb)>m_thresh[2];
		case 9: return m_evaluator9.eval_Agent();
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
		m_om = XArenaFuncs.competeBoth(pa, random_agent, 100, gb);
		m_msg = "Success against random = " + m_om;
		if (this.verbose>0) System.out.println(m_msg);
		return m_om;
	}

 	/**	
	 * @param gb		needed to get a default start state (competeBoth)
 	 * @return
	 * Known callers of evaluateAgent1 (outside this class): 
	 * 		{@link XArenaFuncs#multiTrain(String,TicGameButtons)}
	 */
 	private double evaluateAgent1(PlayAgent pa, GameBoard gb) {
		m_om = XArenaFuncs.competeBoth(pa, minimax_agent, 1, gb);
		m_msg = "Success against minimax = " + m_om;
		if (this.verbose>0) System.out.println(m_msg);
		return m_om;
	}
 	
 	/**
 	 * 
 	 * @param pa
	 * @param gb		needed to get a default start state (competeBoth)
 	 * @return
 	 */
 	private double evaluateAgent2(PlayAgent pa, GameBoard gb) {
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
		m_om=0;
		for (int k=0; k<state.length; ++k) {
			startPlayer = Evaluator9.string2table(state[k],startTable);
			StateObserverTTT startSO = new StateObserverTTT(startTable,startPlayer);
			res = XArenaFuncs.compete(pa, minimax_agent, startSO, competeNum, verbose);
			resX  = res[0] - res[2];		// X-win minus O-win percentage, \in [-1,1]
											// resp. \in [-1,0], if opponent never looses.
											// +1 is best for pa, -1 worst for pa.
			res = XArenaFuncs.compete(minimax_agent, pa, startSO, competeNum, verbose);
			resO  = res[2] - res[0];		// O-win minus X-win percentage, \in [-1,1]
											// resp. \in [-1,0], if opponent never looses.
											// +1 is best for pa, -1 worst for pa.
			m_om += (resX+resO)/2.0;
		}
		m_om=m_om/state.length;
		
		m_msg = "Success against minimax (different starts, best is 0.0) = " + m_om;
		if (this.verbose>0) System.out.println(m_msg);
		
		return m_om;
	}
 	
 	/**
 	 * @return mean success rate against {@link MinimaxAgent}, best is 0.0. Either when starting from
 	 * empty board ({@code mode==1}) or from different start positions ({@code mode==2}), 
 	 * depending on {@code mode} as set in constructor.
 	 */
 	public double getOm() { return m_om; }
 	
 	@Override
 	public double getLastResult() { 
 		return (m_mode==9) ? m_evaluator9.getLastResult() : m_om; 
 	}
 	@Override
 	public String getMsg() { 
 		return (m_mode==9) ? m_evaluator9.getMsg() : m_msg; } 
 	
 	public static int[] getAvailableModes() {
 		return AVAILABLE_MODES;
 	}
 	
 	public static boolean isAvailableMode(int mode) {
 		for (int i : AVAILABLE_MODES) {
 			if (mode==i) return true;
 		}
 		return false;
 	}
}
