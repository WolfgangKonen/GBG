package games.TicTacToe;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
import agentIO.AgentLoader;
import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import games.ArenaTrain;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import games.XArenaFuncs;
import tools.MessageBox;
import tools.ScoreTuple;
import tools.Types;

/**
 * Evaluator for TicTacToe:
 * <ul>
 * <li> if mode=0: {@link EvaluatorTTT#evaluateAgent1(PlayAgent,GameBoard)} (competition against RandomAgent) or 
 * <li> if mode=1: {@link EvaluatorTTT#evaluateAgent1(PlayAgent,GameBoard)} (competition against MaxNAgent) or 
 * <li> if mode=2: {@link EvaluatorTTT#evaluateAgent2(PlayAgent,PlayAgent,GameBoard)} (competition against MaxNAgent from different start positions). 
 * </ul>  
 * The value of mode is set in the constructor. Class Evaluator2 works also for featmode==3.
 */
public class EvaluatorTTT extends Evaluator {
 	private static final int[] AVAILABLE_MODES = {-1,0,1,2,11};		//9,
	private RandomAgent randomAgent = new RandomAgent("Random");
	private MaxNAgent maxNAgent = new MaxNAgent("Max-N");
	private AgentLoader agtLoader = null;
//	private int m_mode;
	protected double[] m_thresh={0.8,-0.15,-0.15}; // threshold for each value of m_mode
	private GameBoard m_gb;
	
	public EvaluatorTTT(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
		super(e_PlayAgent, 1, stopEval);
		initEvaluator(gb);
	}

	public EvaluatorTTT(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode) {
		super(e_PlayAgent, mode, stopEval);
		initEvaluator(gb);
	}

	public EvaluatorTTT(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, mode, stopEval, verbose);
		initEvaluator(gb);
	}
	
	private void initEvaluator(GameBoard gb) {
		m_gb = gb;				
	}
	
	/**
	 * @return true if evaluateAgentX is above m_thresh.<br>
	 * The choice for X=1 or 2 is made with 3rd parameter mode in 
	 * {@link #EvaluatorTTT(PlayAgent, GameBoard, int, int)} [default mode=1].<p>
	 * 
	 * If mode==0, then m_thresh=0.8 (best: 0.9, worst: 0.0) <br>
	 * If mode==1 or 2, then m_thresh=-0.15 (best: 0.0, worst: -1.0)
	 */
	@Override
	public boolean evalAgent(PlayAgent playAgent) {
		m_PlayAgent = playAgent;
		switch(m_mode) {
		case -1: 
			m_msg = "no evaluation done ";
			lastResult = Double.NaN;
			return false;
		case 0:  return evaluateAgent1(m_PlayAgent,randomAgent,m_gb)>m_thresh[0];
		case 1:  return evaluateAgent1(m_PlayAgent,maxNAgent,m_gb)>m_thresh[1];
		case 2:  return evaluateAgent2(m_PlayAgent,maxNAgent,m_gb)>m_thresh[2];
		case 11: 
			if (agtLoader==null) agtLoader = new AgentLoader(m_gb.getArena(),"TDReferee.agt.zip");
			return evaluateAgent2(m_PlayAgent,agtLoader.getAgent(),m_gb)>m_thresh[2];
		default: return false;
		}
	}
	
//	/**	
//	 * @param gb		needed to get a default start state (competeBoth)
// 	 * @return the result from competeBoth
//	 */
// 	private double evaluateAgent0(PlayAgent pa, GameBoard gb) {
// 		StateObservation so = gb.getDefaultStartState();
//		lastResult = XArenaFuncs.competeBoth(pa, random_agent, so, 100, 0, gb);
//		m_msg = pa.getName()+": "+getPrintString() + lastResult;
//		if (this.verbose>0) System.out.println(m_msg);
//		return lastResult;
//	}

 	/**	
	 * @param gb		needed to get a default start state (competeBoth)
 	 * @return the result from competeBoth
	 */
 	private double evaluateAgent1(PlayAgent pa, PlayAgent opponent, GameBoard gb) {
 		StateObservation so = gb.getDefaultStartState();
//		lastResult = XArenaFuncs.competeBoth(pa, opponent, so, 1, 0, gb);
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa,opponent), so, 1, 0);
		lastResult = sc.scTup[0];
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return lastResult;
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
		lastResult=0;
		
		if (opponent == null) {
			String tdstr = agtLoader.getLoadMsg() + " (no opponent)";
			gb.getArena().showMessage("ERROR: " + tdstr,"Load Error", JOptionPane.ERROR_MESSAGE);
			lastResult = Double.NaN;
			m_msg = "EvaluatorTTT: opponent is null!";
			return lastResult;
		} 

		for (int k=0; k<state.length; ++k) {
			startPlayer = EvaluatorTTT.string2table(state[k],startTable);
			StateObserverTTT startSO = new StateObserverTTT(startTable,startPlayer);
//			res = XArenaFuncs.compete(pa, opponent, startSO, competeNum, verbose, null);
//			resX  = res[0] - res[2];		// X-win minus O-win percentage, \in [-1,1]
//											// resp. \in [-1,0], if opponent never looses.
//											// +1 is best for pa, -1 worst for pa.
//			res = XArenaFuncs.compete(opponent, pa, startSO, competeNum, verbose, null);
//			resO  = res[2] - res[0];		// O-win minus X-win percentage, \in [-1,1]
//											// resp. \in [-1,0], if opponent never looses.
//											// +1 is best for pa, -1 worst for pa.
//			lastResult += (resX+resO)/2.0;
			ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa,opponent), startSO, competeNum, verbose);
			lastResult = sc.scTup[0];
		}
		lastResult=lastResult/state.length;
		
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		
		return lastResult;
	}
 	
 	/**
 	 * @return mean success rate against {@link MaxNAgent}, best is 0.0. Either when starting from
 	 * empty board ({@code mode==1}) or from different start positions ({@code mode==2}), 
 	 * depending on {@code mode} as set in constructor.
 	 */
 	public double getOm() { return lastResult; }
 	
 	// --- implemented by Evaluator ---
// 	@Override
// 	public double getLastResult() { 
// 		return lastResult;
// 	}
 	
 	// --- implemented by Evaluator ---
// 	@Override
// 	public String getMsg() { 
// 		return m_msg;
// 	} 
 	
 	// --- implemented by Evaluator ---
//	@Override
// 	public boolean isAvailableMode(int mode) {
//		for (int i : AVAILABLE_MODES) {
//			if (mode==i) return true;
//		}
//		return false;
// 	}
 	
 	@Override
 	public int[] getAvailableModes() {
 		return AVAILABLE_MODES;
 	}
 	
 	//@Override
 	public int getDefaultEvalMode() {
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
//	public int getMultiTrainEvalMode() 
//	{
//		return 0;
//	}

	@Override
	public String getPrintString() {
		switch (m_mode) {
		case -1: return "no evaluation done ";
		case 0:  return "success rate (against randomAgent, best is 0.9): ";
		case 1:  return "success rate (against Max-N, best is 0.0): ";
		case 2:  return "success rate (against Max-N, different starts, best is 0.0): ";
		case 11: return "success rate (against TDReferee, different starts, best is ?): ";
		default: return null;
		}
	}
	
	/**
	 * Derived classes return a tooltip string with this method. Use 
	 * <pre>
	 *    "&lt;html&gt; ... &lt;br&gt; ... &lt;/html&gt;"  </pre>
	 * to get multi-line tooltip text 
	 * @return the tooltip string for evaluator boxes in "Other pars"
	 */
	// Use "<html> ... <br> ... </html>" to get multi-line tooltip text
	@Override
	public String getTooltipString() {
		return "<html>-1: none<br>"
				+ "0: against Random, best is 0.9<br>"
				+ "1: against Max-N, best is 0.0<br>"
				+ "2: against Max-N, different starts, best is 0.0<br>"
				+ "11: against TDReferee.agt.zip, different starts"
				+ "</html>";
	}

	@Override
	public String getPlotTitle() {
		switch (m_mode) {
		case 0:  return "success against Random";
		case 1:  return "success against Max-N";
		case 2:  return "success against Max-N, dStart";
		case 11: return "success TDReferee";		
		default: return null;
		}
	}
	
	/** Transform a string like "Xo-X---o-" into its board position and the player 
	 *  who makes the next move 
	 * 
	 * @param state_k	a string like "Xo-X---o-" coding a board position
	 * @param table		on input an allocated memory area, on output it contains the board position
	 * @return			+1 or -1 if either X or O makes the next move
	 */
	public static int string2table(String state_k, int table[][]) {
		int Xcount=0, Ocount=0;
		for (int i=0;i<3;++i)
			for (int j=0;j<3;++j) {
				if (state_k.charAt(3*i+j)=='X') {
					table[i][j]=+1;
					Xcount++;
				}
				else if (state_k.charAt(3*i+j)=='o') {
					table[i][j]=-1;
					Ocount++;
				}
				else {
					table[i][j]=0;					
				}
			}
		int player = (Ocount-Xcount)*2+1;	// the player who makes the next move
		if (player!=-1 && player!=1)
			throw new RuntimeException("invalid state!!");
		return player;
	}
	


}
