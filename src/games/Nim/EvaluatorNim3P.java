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
 * Evaluator for the game Nim3P (3 players). Depending on the value of parameter {@code mode} in constructor:
 * <ul>
 * <li> -1: no evaluation
 * <li>  0: compete against two Random agents, best is 0.9
 * <li>  1: compete against two MaxNAgents, best is 0.0
 * <li>  2: compete against two MaxNAgents, different starts, best is 0.0
 * <li>  3: compete against two MCTS agents, best is 0.0
 * <li>  4: compete against two MCTS agents, different start states, best is 0.0
 * <li> 11: compete against TDReferee.agt.zip, different start states
 * </ul>  
 * All competitions are COMPETE_BOTH. 
 */
public class EvaluatorNim3P extends Evaluator {
 	private static final int[] AVAILABLE_MODES = {-1,0,1,2,3,4,11};
	private RandomAgent randomAgent = new RandomAgent("Random");
	private RandomAgent randomAgent2 = new RandomAgent("Random2");
	private MCTSAgentT mctsAgent = null;
	private MCTSAgentT mctsAgent2 = null;
	private MaxNAgent maxNAgent = new MaxNAgent("MaxN",15,true);
	private MaxNAgent maxNAgent2 = new MaxNAgent("MaxN",15,true);
	private PlayAgent secondAgent, thirdAgent;
	private PlayAgtVector paVector;
	protected double[] m_thresh={0.8,-0.15,-0.15}; // threshold for each value of m_mode
	
//	private int m_mode;						// now in Evaluator
//	private AgentLoader agtLoader = null;	// now in Evaluator
//	private GameBoard m_gb;					// now in Evaluator
	
	public EvaluatorNim3P(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
		super(e_PlayAgent, gb, 1, stopEval);
		initEvaluator(gb);
	}

	public EvaluatorNim3P(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode) {
		super(e_PlayAgent, gb, mode, stopEval);
		initEvaluator(gb);
	}

	public EvaluatorNim3P(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, gb, mode, stopEval, verbose);
		initEvaluator(gb);
	}
	
	private void initEvaluator(GameBoard gb) {
		m_gb = gb;		
        ParMCTS params = new ParMCTS();
        params.setNumIter(10000);
        mctsAgent = new MCTSAgentT("MCTS", new StateObserverNim3P(), params);
        mctsAgent2 = new MCTSAgentT("MCTS2", new StateObserverNim3P(), params);
	}
	
	/**
	 * 
	 * @return true if evaluateAgentX is above m_thresh.<br>
	 * The choice for X=0, 1 or 2 is made with 4th parameter mode in 
	 * {@link #EvaluatorNim3P(PlayAgent, GameBoard, int, int)} [default mode=1].<p>
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
		
		int competeNum=10;
		switch(m_mode) {
		case 0:  
			competeNum=100; break;
		case 1:  
		case 2:  
			secondAgent=maxNAgent; thirdAgent=maxNAgent2; break;
		case 3:  
		case 4:  
			secondAgent=mctsAgent; thirdAgent=mctsAgent2; break;
		case 11: 
			//	Evaluator.getTDReferee throws RuntimeException, if TDReferee.agt.zip is not found:
			secondAgent=getTDReferee(); thirdAgent=getTDReferee(); break;
		}		
		paVector = new PlayAgtVector(m_PlayAgent,secondAgent,thirdAgent);
		if (m_mode==4) competeNum=3;

		int numPlayers = paVector.getNumPlayers();
		String[] pa_string = new String[numPlayers];
		for (int i = 0; i < numPlayers; i++)
			pa_string[i] = paVector.pavec[i].stringDescr();
		String sMsg = "QuickEval, " + competeNum + " episodes: \n";
		for (int n = 0; n < numPlayers; n++) {
			sMsg = sMsg + "    P" + n + ": " + pa_string[n];
			if (n < numPlayers - 1)
				sMsg = sMsg + ", \n";
		}
		System.out.println(sMsg);

		switch(m_mode) {
		case 0:  return evaluateAgent0(m_PlayAgent,m_gb,competeNum)>m_thresh[0];
		case 1:  return evaluateAgent1(paVector,m_gb,competeNum)>m_thresh[1];
		case 2:  return evaluateAgent2(paVector,m_gb,competeNum)>m_thresh[2];
		case 3:  return evaluateAgent1(paVector,m_gb,competeNum)>m_thresh[1];
		case 4:  return evaluateAgent2(paVector,m_gb,competeNum)>m_thresh[2];
		case 11: return evaluateAgent2(paVector,m_gb,competeNum)>m_thresh[2];
		default: return false;
		}
	}
	
	/**	
	 * competeBoth against random
 	 * @param pa
	 * @param gb		needed to get a default start state (competeBoth)
 	 * @return
	 */
 	private double evaluateAgent0(PlayAgent pa, GameBoard gb, int competeNum) {
 		StateObservation so = gb.getDefaultStartState();
//		lastResult = XArenaFuncs.competeBoth(pa, random_agent, so, 100, 0, gb);
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa,randomAgent,randomAgent2), so, competeNum, 0);
		lastResult = sc.scTup[0];
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return lastResult;
	}

 	/**
 	 * competeBoth against opponent, from default start state	
 	 * @param paVector
	 * @param gb		needed to get default start state (competeBoth)
 	 * @return
	 */
 	private double evaluateAgent1(PlayAgtVector paVector, GameBoard gb, int competeNum) {
 		StateObservation so = gb.getDefaultStartState();
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(paVector, so, competeNum, 0);
		lastResult = sc.scTup[0];
		m_msg = paVector.pavec[0].getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return lastResult;
	}
 	
 	/**
 	 * competeBoth against opponent, from different start states	
 	 * @param paVector
	 * @param gb		needed to get the start states
 	 * @return
 	 */
 	private double evaluateAgent2(PlayAgtVector paVector, GameBoard gb, int competeNum) {
		double[] res;
//		double resX, resO;
        double success = 0;
        double averageSuccess = 0; 
		
		lastResult=0;
 		
		int numK=10;
		for (int k=0; k<numK; ++k) {
			// Choose randomly one of the possible 0-1-ply start states. Repeat 
			// this numK times to sample a representative subset of possible configurations.
			StateObservation startSO = gb.chooseStartState();
			ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(paVector, startSO, competeNum, 0);
    		success = sc.scTup[0];
    		averageSuccess += success;
		}
        averageSuccess /= numK;
		lastResult=averageSuccess;
		
		m_msg = paVector.pavec[0].getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);		// this.verbose is def'd in Evaluator
		
		return lastResult;
	}
 	
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
		case 0:  return "0.9"; // random agent, competeAllRoles
		case 1:  return "0.333"; // MaxN, competeAllRoles
		case 2:  return "0.333"; // MaxN, diff. starts, competeAllRoles
		case 3:  return "0.333"; // MCTS, competeAllRoles
		case 4:  return "0.333"; // MCTS, diff. starts, competeAllRoles
		case 11: return "0.333"; // TDReferee (if perfect), different starts, competeAllRoles 
		default: return null;
		}
	}
		
	@Override
	public String getPrintString() {
		String strBest = getBestResultString(m_mode);
		switch (m_mode) {
		case 0:  return "success rate (randomAgent, best is "+strBest+"): ";
		case 1:  return "success rate (Max-N, expec is "+strBest+"): ";
		case 2:  return "success rate (Max-N, different starts, expec is "+strBest+"): ";
		case 3:  return "success rate (MCTS, expec is "+strBest+"): ";	
		case 4:  return "success rate (MCTS, different starts, expec is "+strBest+"): ";	
		case 11: return "success rate (TDReferee, different starts, expec is "+strBest+"): ";
		default: return null;
		}
	}
	
	@Override
	public String getTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>-1: none<br>"
				+ "0: against Random, best is "+getBestResultString(0)+"<br>"
				+ "1: against Max-N, expec is "+getBestResultString(1)+"<br>"
				+ "2: against Max-N, different starts, expec is "+getBestResultString(2)+"<br>"
				+ "3: against MCTS, expec is "+getBestResultString(3)+"<br>"
				+ "4: against MCTS, different starts, expec is "+getBestResultString(4)+"<br>"
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
