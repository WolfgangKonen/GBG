package games.Nim;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JOptionPane;

import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import controllers.MCTS.MCTSAgentT;
import games.ArenaTrain;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import games.XArenaFuncs;
import games.Hex.StateObserverHex;
import gui.MessageBox;
import params.ParMCTS;
import params.ParMaxN;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types;

/**
 * Evaluator for the game Nim. Depending on the value of parameter {@code mode} in constructor:
 * <ul>
 * <li> -1: no evaluation
 * <li>  0: compete against Random, best is 0.9
 * <li>  1: compete against MaxNAgent, best is 0.0
 * <li>  2: compete against MaxNAgent, different starts, best is 0.0
 * <li>  3: compete against MCTS, best is 0.0
 * <li>  4: compete against MCTS, different start states, best is 0.0
 * <li> 11: compete against TDReferee.agt.zip, different start states
 * </ul>  
 * All competitions are COMPETE_BOTH. 
 */
public class EvaluatorNim extends Evaluator {
 	private static final int[] AVAILABLE_MODES = {-1,0,1,2,3,4,11};
	private RandomAgent random_agent = new RandomAgent("Random");
	private MaxNAgent maxNAgent = null;
    private MCTSAgentT mctsAgent = null;
	protected double[] m_thresh={0.8,-0.15,-0.15}; // threshold for each value of m_mode
	
//	private int m_mode;						// now in Evaluator
//	private AgentLoader agtLoader = null;	// now in Evaluator
//	private GameBoard m_gb;					// now in Evaluator
	
	public EvaluatorNim(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
		super(e_PlayAgent, gb, 1, stopEval);
		initEvaluator(gb);
	}

	public EvaluatorNim(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode) {
		super(e_PlayAgent, gb, mode, stopEval);
		initEvaluator(gb);
	}

	public EvaluatorNim(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, gb, mode, stopEval, verbose);
		initEvaluator(gb);
	}
	
	private void initEvaluator(GameBoard gb) {
		// --- this is now in Evaluator ---
//		if (!isAvailableMode(mode)) 
//			throw new RuntimeException("EvaluatorNim: Value mode = "+mode+" is not allowed!");
		m_gb = gb;		
        ParMCTS params = new ParMCTS();
        params.setNumIter(1000);
        mctsAgent = new MCTSAgentT("MCTS", new StateObserverHex(), params);
        
    	ParMaxN parM = new ParMaxN();
    	parM.setMaxNDepth(15);
    	parM.setMaxNUseHashmap(true);
    	maxNAgent = new MaxNAgent("Max-N", parM, new ParOther());
	}
	
	/**
	 * 
	 * @return true if evaluateAgentX is above m_thresh.<br>
	 * The choice for X=0, 1 or 2 is made with 4th parameter mode in 
	 * {@link #EvaluatorNim(PlayAgent, GameBoard, int, int)} [default mode=1].<p>
	 * 
	 * If mode==0, then m_thresh=0.8 (best: 0.9, worst: 0.0) <br>
	 * If mode==1 or 2, then m_thresh=-0.15 (best: 0.0, worst: -1.0)
	 */
	@Override
	public boolean evalAgent(PlayAgent playAgent) {
		m_PlayAgent = playAgent;
		if (m_mode==1 || m_mode==2) {
			StateObserverNim so = (StateObserverNim) m_gb.getDefaultStartState();
			int heapsum = so.getHeapSum();
			int depth = maxNAgent.getDepth();
			if (depth<heapsum)
				System.out.println("Warning: Max-N depth = "+depth+" is smaller than required: heap sum ="+heapsum+" !");
		}
		switch(m_mode) {
		case 0:  return evaluateAgent0(m_PlayAgent,m_gb)>m_thresh[0];
		case 1:  return evaluateAgent1(m_PlayAgent,maxNAgent,m_gb)>m_thresh[1];
		case 2:  return evaluateAgent2(m_PlayAgent,maxNAgent,m_gb)>m_thresh[2];
		case 3:  return evaluateAgent1(m_PlayAgent,mctsAgent,m_gb)>m_thresh[1];
		case 4:  return evaluateAgent2(m_PlayAgent,mctsAgent,m_gb)>m_thresh[2];
		case 11: 
			//	Evaluator.getTDReferee throws RuntimeException, if TDReferee.agt.zip is not found:
			return evaluateAgent2(m_PlayAgent,getTDReferee(),m_gb)>m_thresh[2];
		default: return false;
		}
	}
	
	/**	
	 * competeBoth against random
 	 * @param pa
	 * @param gb		needed to get a default start state (competeBoth)
 	 * @return
	 */
 	private double evaluateAgent0(PlayAgent pa, GameBoard gb) {
 		StateObservation so = gb.getDefaultStartState();
//		lastResult = XArenaFuncs.competeBoth(pa, random_agent, so, 100, 0, gb);
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa,random_agent), so, 100, 0);
		lastResult = sc.scTup[0];
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return lastResult;
	}

 	/**
 	 * competeBoth against opponent, from default start state	
 	 * @param pa
 	 * @param opponent
	 * @param gb		needed to get default start state (competeBoth)
 	 * @return
	 */
 	private double evaluateAgent1(PlayAgent pa, PlayAgent opponent, GameBoard gb) {
 		StateObservation so = gb.getDefaultStartState();
		if (opponent == null) {
			gb.getArena().showMessage("ERROR: no opponent","Load Error", JOptionPane.ERROR_MESSAGE);
			lastResult = Double.NaN;
			return lastResult;
		}
//		lastResult = XArenaFuncs.competeBoth(pa, opponent, so, 1, 0, gb);
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa,opponent), so, 10, 0);
		lastResult = sc.scTup[0];
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return lastResult;
	}
 	
 	/**
 	 * competeBoth against opponent, from different start states	
 	 * @param pa
 	 * @param opponent
	 * @param gb		needed to get the start states 
 	 * @return
 	 */
 	private double evaluateAgent2(PlayAgent pa, PlayAgent opponent, GameBoard gb) {
		int competeNum=1;
		double[] res;
//		double resX, resO;
        double success = 0;
        double averageSuccess = 0; 
		
		if (opponent == null) {
			gb.getArena().showMessage("ERROR: no opponent","Load Error", JOptionPane.ERROR_MESSAGE);
			lastResult = Double.NaN;
			m_msg = "EvaluatorTTT: opponent is null!";
			return lastResult;
		} 

		lastResult=0;
 		
		int numK=10;
		for (int k=0; k<numK; ++k) {
			// Choose randomly one of the possible 0-1-ply start states. Repeat 
			// this numK times to sample a representative subset of possible configurations.
			StateObservation startSO = gb.chooseStartState();
//			res = XArenaFuncs.compete(pa, opponent, startSO, competeNum, this.verbose-1, null);
//			resX  = res[0] - res[2];		// X-win minus O-win percentage, \in [-1,1]
//											// resp. \in [-1,0], if opponent never looses.
//											// +1 is best for pa, -1 worst for pa.
//			res = XArenaFuncs.compete(opponent, pa, startSO, competeNum, this.verbose-1, null);
//			resO  = res[2] - res[0];		// O-win minus X-win percentage, \in [-1,1]
//											// resp. \in [-1,0], if opponent never looses.
//											// +1 is best for pa, -1 worst for pa.
//			lastResult += (resX+resO)/2.0;
			ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa,opponent), startSO, competeNum, 0);
    		success = sc.scTup[0];
    		averageSuccess += success;
		}
        averageSuccess /= numK;
		lastResult=averageSuccess;
		
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);		// this.verbose is def'd in Evaluator
		
		return lastResult;
	}
 	
 	// --- implemented by Evaluator ---
// 	@Override
// 	public double getLastResult() { 
// 		return lastResult; 
// 	}
 	
 	// --- implemented by Evaluator ---
// 	@Override
// 	public String getMsg() { 
// 		return m_msg; } 
 	
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
		return AVAILABLE_MODES[3];		// mode 2
	}
 	
	public int getQuickEvalMode() 
	{
		return 1;
	}
	public int getTrainEvalMode() 
	{
		return 3;
	}
	private String getBestResultString(int mode) {
		DecimalFormat df = new DecimalFormat();				
		df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);		
		df.applyPattern("0.00");  
		switch (mode) {
		case 0:  				// random agent, competeBoth
			// a rough estimate of the probability that RandomAgent wins: in one half of the 
			// competeBoth games, RandomAgent will always loose, in the other half it may 
			// win if it chooses by accident always a correct move: If there are exactly MAX_MINUS+1
			// items left on 1 heap and the other agent moves, the probability for RandomAgent
			// to win is pLastMoveWin=(1/mx)*(1/mx+...+1/1), averaging over 1,2,..,mx=MAX_MINUS items  
			// taken by the other agent. nMoves is the number
			// of moves that RandomAgent has to make to reach MAX_MINUS+1. The probability
			// to guess the correct move is in each case (1/MAX_MINUS). So the total probability that 
			// the *other* agent wins is
			//		1 - (1/MAX_MINUS)^nMoves * pLastMoveWin / 2.0
			int nItems = NimConfig.NUMBER_HEAPS*NimConfig.HEAP_SIZE;		// # of items on all heaps
			int mx = NimConfig.MAX_MINUS;
			int nMoves = (int) Math.floor((nItems-1e-6)/(mx+1));	// approx. # of moves of one agent
			double pLastMoveWin = 0.0;
			for (int i=1; i<=mx; i++) pLastMoveWin += (1.0/i);
			pLastMoveWin /= mx;
			double sr = 1.0 - Math.pow(1.0/mx, nMoves)*pLastMoveWin/2.0;
			return df.format(sr);
		case 1:  return "0.0"; // MaxN, competeBoth
		case 2:  return "0.0"; // MaxN, diff. starts, competeBoth
		case 3:  return "0.0"; // MCTS, competeBoth
		case 4:  return "0.0"; // MCTS, diff. starts, competeBoth
		case 11: return "0.0"; // TDReferee (if perfect), different starts, competeBoth 
		default: return null;
		}
	}
		
	@Override
	public String getPrintString() {
		String strBest = getBestResultString(m_mode);
		switch (m_mode) {
		case 0:  return "success rate (randomAgent, best is "+strBest+"): ";
		case 1:  return "success rate (Max-N, best is "+strBest+"): ";
		case 2:  return "success rate (Max-N, different starts, best is "+strBest+"): ";
		case 3:  return "success rate (MCTS, best is "+strBest+"): ";	
		case 4:  return "success rate (MCTS, different starts, best is "+strBest+"): ";	
		case 11: return "success rate (TDReferee, different starts, best is "+strBest+"): ";
		default: return null;
		}
	}
	
	@Override
	public String getTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>-1: none<br>"
				+ "0: against Random, best is "+getBestResultString(0)+"<br>"
				+ "1: against Max-N, best is "+getBestResultString(1)+"<br>"
				+ "2: against Max-N, different starts, best is "+getBestResultString(2)+"<br>"
				+ "3: against MCTS, best is "+getBestResultString(3)+"<br>"
				+ "4: against MCTS, different starts, best is "+getBestResultString(4)+"<br>"
				+ "11: against TDReferee.agt.zip, different starts"
				+ "</html>";
	}

	@Override
	public String getPlotTitle() {
		switch (m_mode) {
		case 0:  return "success against Random";
		case 1:  return "success against Max-N";
		case 2:  return "success against Max-N, dStart";
		case 3:  return "success against MCTS";
		case 4:  return "success against MCTS, dStart";
		case 11: return "success TDReferee";		
		default: return null;
		}
	}

}
