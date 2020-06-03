package games.TicTacToe;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import games.ArenaTrain;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import games.XArenaFuncs;
import games.Hex.StateObserverHex;
import gui.MessageBox;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;

/**
 * Evaluator for TicTacToe:
 * <ul>
 * <li> if mode=0: {@link EvaluatorTTT#evaluateAgent1(PlayAgent,PlayAgent,GameBoard)} (competition against RandomAgent) or 
 * <li> if mode=1: {@link EvaluatorTTT#evaluateAgent1(PlayAgent,PlayAgent,GameBoard)} (competition against MaxNAgent) or 
 * <li> if mode=2: {@link EvaluatorTTT#evaluateAgent2(PlayAgent,PlayAgent,GameBoard)} (competition against MaxNAgent from different start positions). 
 * </ul>  
 * The value of mode is set in the constructor. Class Evaluator2 works also for featmode==3.
 */
public class EvaluatorTTT extends Evaluator {
	private RandomAgent randomAgent = new RandomAgent("Random");
	private MaxNAgent maxNAgent = new MaxNAgent("Max-N");
	protected double[] m_thresh={0.8,-0.15,-0.15}; // threshold for each value of m_mode
	/**
	 * A list of all TTT states which are 0 or 1 ply away from the default start state.
	 * These are 1+9 states in the case of TicTacToe. <br>
	 * <p>
	 * This list is a static member, so that it needs to be constructed only once. <br>
	 */
	protected static ArrayList<StateObserverTTT> diffStartList = null;
	
	public EvaluatorTTT(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
		super(e_PlayAgent, gb, 1, stopEval);
		initEvaluator(gb);
	}

	public EvaluatorTTT(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode) {
		super(e_PlayAgent, gb, mode, stopEval);
		initEvaluator(gb);
	}

	public EvaluatorTTT(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, gb, mode, stopEval, verbose);
		initEvaluator(gb);
	}
	
	private void initEvaluator(GameBoard gb) { 	}
	
    /**
     * Add all 1-ply start states plus the default start state to diffStartList
     * @param diffStartList
     * @return diffStartList, filled with states
     */
	private ArrayList<StateObserverTTT> 
	addAll1PlyStates(ArrayList<StateObserverTTT> diffStartList) {
    	StateObserverTTT s = (StateObserverTTT) m_gb.getDefaultStartState();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {         	
				Types.ACTIONS a = new ACTIONS(i * 3 + j);
	        	StateObserverTTT s_copy = s.copy();
	        	s_copy.advance(a);
	        	diffStartList.add(s_copy);
			}
		}	
		diffStartList.add(s);		// add the default start state as well
		return diffStartList;
	}
	
	/**
	 * Evaluate {@code playAgent}. Side effects: write results to {@link Evaluator#lastResult} and 
	 * {@link Evaluator#m_msg}.
	 * 
	 * @return true if evaluateAgentX is above m_thresh.<br>
	 * The choice for X=1 or 2 is made with 3rd parameter mode in 
	 * {@link #EvaluatorTTT(PlayAgent, GameBoard, int, int)} [default mode=1].<p>
	 * 
	 * If mode==0, then m_thresh=0.8 (best: 0.9, worst: 0.0) <br>
	 * If mode==1 or 2, then m_thresh=-0.15 (best: 0.0, worst: -1.0)
	 */
	@Override
	public boolean evalAgent(PlayAgent playAgent) {

		// when evalAgent is called for the first time, construct diffStartList once for all 
        // EvaluatorHex objects (will not change during runtime)
        if (diffStartList==null) {
        	diffStartList = new  ArrayList<StateObserverTTT>(); 
        	diffStartList = addAll1PlyStates(diffStartList);        	
        }
		
		m_PlayAgent = playAgent;
		switch(m_mode) {
		case -1: 
			m_msg = "no evaluation done ";
			lastResult = Double.NaN;
			return false;
		case 0:  return evaluateAgent1(m_PlayAgent,randomAgent,m_gb)>m_thresh[0];
		case 1:  return evaluateAgent1(m_PlayAgent,  maxNAgent,m_gb)>m_thresh[1];
		case 2:  return evaluateAgent2(m_PlayAgent,  maxNAgent,m_gb)>m_thresh[2];
		case 11: 
			//	Evaluator.getTDReferee throws RuntimeException, if TDReferee.agt.zip is not found:
			return evaluateAgent2(m_PlayAgent,this.getTDReferee(),m_gb)>m_thresh[2];
		default: return false;
		}
	}
	
 	/**	
 	 * Evaluate pa against opponent, playing in both roles and starting from default
 	 * start state. If opponent plays perfect, best outcome is 0.0.
 	 * @param pa
 	 * @param opponent
	 * @param gb		needed to get a default start state (competeBoth)
 	 * @return average success rate in [-1,1]
	 */
 	private double evaluateAgent1(PlayAgent pa, PlayAgent opponent, GameBoard gb) {
 		StateObservation so = gb.getDefaultStartState();
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa,opponent), so, 1, 0);
		lastResult = sc.scTup[0];
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return lastResult;
	}
 	
 	/**
 	 * Evaluate pa against opponent, playing in both roles and starting from all 0-ply and 1-ply
 	 * start states. If opponent plays perfect, best outcome is 0.0.
 	 * @param pa
 	 * @param opponent
	 * @param gb		needed to get a default start state (competeBoth)
 	 * @return average success rate in [-1,1]
 	 */
 	private double evaluateAgent2(PlayAgent pa, PlayAgent opponent, GameBoard gb) {
		int verbose=0;
		int competeNum=1;
		int startPlayer=+1;
		int[][] startTable=new int[3][3];
		double[] res;
		double resX, resO;
        double success = 0;
        double averageSuccess = 0; 

//      // all these start states have the result "Tie" for perfect playing agents:
//		String state[] = {"---------","--------X","-------X-"
//						, "------X--","-----X---","----X----"
//						, "---X-----","--X------","-X-------"
//						, "X--------"};
//		lastResult=0;
		
		if (opponent == null) {
			gb.getArena().showMessage("ERROR: no opponent","Load Error", JOptionPane.ERROR_MESSAGE);
			lastResult = Double.NaN;
			m_msg = "EvaluatorTTT: opponent is null!";
			return lastResult;
		} 

        int numStartStates = diffStartList.size();
        // evaluate each start state in turn and return average success rate: 
        int i=0;
        for (StateObserverTTT so : diffStartList) {
            long gameStartTime = System.currentTimeMillis();
    		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa, opponent), so, competeNum, 0);
    		success = sc.scTup[0];
    		averageSuccess += success;
        	long duration = System.currentTimeMillis() - gameStartTime;
//          System.out.println("Finished evaluation " + i + " after " + duration + "ms, success="+success);
            i++;
        }
        averageSuccess /= numStartStates;
        double winrate = (averageSuccess+1)/2;

//		for (int k=0; k<state.length; ++k) {
//			startPlayer = EvaluatorTTT.string2table(state[k],startTable);
//			StateObserverTTT startSO = new StateObserverTTT(startTable,startPlayer);
//			ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa,opponent), startSO, competeNum, verbose);
//			lastResult = sc.scTup[0];
//		}
//		lastResult=lastResult/state.length;

        lastResult = averageSuccess;			
		m_msg = pa.getName()+": "+getPrintString() + lastResult + " (winrate="+winrate+")";
		if (this.verbose>0) System.out.println(m_msg);
		return lastResult;
	}
 	
// 	/**
// 	 * @return mean success rate against {@link MaxNAgent}, best is 0.0. Either when starting from
// 	 * empty board ({@code mode==1}) or from different start positions ({@code mode==2}), 
// 	 * depending on {@code mode} as set in constructor.
// 	 */
// 	public double getOm() { return lastResult; }
 	
 	@Override
 	public int[] getAvailableModes() {
 		int[] availModes = {-1,0,1,2,11};
 		return availModes;
 	}
 	
 	//@Override
 	public int getDefaultEvalMode() {
		return 2;
	}
 	
	public int getQuickEvalMode() 
	{
		return 2;
	}
	public int getTrainEvalMode() 
	{
		return 9;
	}

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
		case 0:  return "success vs Random";
		case 1:  return "success vs Max-N";
		case 2:  return "success vs Max-N, diff.Start";
		case 11: return "success vs TDReferee";		
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
	private static int string2table(String state_k, int table[][]) {
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
