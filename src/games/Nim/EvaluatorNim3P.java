package games.Nim;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import controllers.MCTS.MCTSAgentT;
import games.*;
import params.ParMCTS;
import tools.ScoreTuple;

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
	private final RandomAgent randomAgent = new RandomAgent("Random");
	private final RandomAgent randomAgent2 = new RandomAgent("Random2");
	private final MaxNAgent maxNAgent = new MaxNAgent("MaxN",15,true);
	private final MaxNAgent maxNAgent2 = new MaxNAgent("MaxN",15,true);
	private MCTSAgentT mctsAgent = null;
	private MCTSAgentT mctsAgent2 = null;
	private PlayAgent secondAgent, thirdAgent;
	protected double[] m_thresh={0.8,-0.15,-0.15}; // threshold for each value of m_mode
	
//	private int m_mode;						// now in Evaluator
//	private AgentLoader agtLoader = null;	// now in Evaluator
//	private GameBoard m_gb;					// now in Evaluator

	public EvaluatorNim3P(PlayAgent e_PlayAgent, GameBoard gb, int mode, int verbose) {
		super(e_PlayAgent, gb, mode, verbose);
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
	public EvalResult evalAgent(PlayAgent playAgent) {
		PlayAgtVector paVector;
		m_PlayAgent = playAgent;
		if (m_mode==1 || m_mode==2) {
			StateObserverNim so = (StateObserverNim) m_gb.getDefaultStartState();
			int heapsum = so.getHeapSum();
			int depth = maxNAgent.getDepth();
			if (depth<heapsum)
				System.out.println("Warning: Max-N depth = "+depth+" is smaller than required: heap sum ="+heapsum+" !");
		}
		
		int competeNum=10;
		switch (m_mode) {
			case 0 -> competeNum = 100;
			case 1, 2 -> {
				secondAgent = maxNAgent;
				thirdAgent = maxNAgent2;
			}
			case 3, 4 -> {
				secondAgent = mctsAgent;
				thirdAgent = mctsAgent2;
			}
			case 11 -> {
				//	Evaluator.getTDReferee throws RuntimeException, if TDReferee.agt.zip is not found:
				secondAgent = getTDReferee();
				thirdAgent = getTDReferee();
			}
		}
		paVector = new PlayAgtVector(m_PlayAgent,secondAgent,thirdAgent);
		if (m_mode==4) competeNum=3;

		int numPlayers = paVector.getNumPlayers();
		String[] pa_string = new String[numPlayers];
		for (int i = 0; i < numPlayers; i++)
			pa_string[i] = paVector.pavec[i].stringDescr();
		StringBuilder sMsg = new StringBuilder("QuickEval, " + competeNum + " episodes: \n");
		for (int n = 0; n < numPlayers; n++) {
			sMsg.append("    P").append(n).append(": ").append(pa_string[n]);
			if (n < numPlayers - 1)
				sMsg.append(", \n");
		}
		System.out.println(sMsg);

		return switch (m_mode) {
			case 0 -> evaluateAgent0(m_PlayAgent, m_gb, competeNum, m_thresh[0]);
			case 1, 3 -> evaluateAgent1(paVector, m_gb, competeNum, m_thresh[1]);
			case 2, 4, 11 -> evaluateAgent2(paVector, m_gb, competeNum, m_thresh[2]);
			default -> throw new RuntimeException("Invalid m_mode = " + m_mode);
		};
	}
	
	/**	
	 * competeBoth against random
	 * @param pa		the play agent
	 * @param gb		needed to get a default start state (competeBoth)
	 * @return			the evaluation result
	 */
 	private EvalResult evaluateAgent0(PlayAgent pa, GameBoard gb, int competeNum, double thresh) {
 		StateObservation so = gb.getDefaultStartState();
//		lastResult = XArenaFuncs.competeBoth(pa, random_agent, so, 100, 0, gb);
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa,randomAgent,randomAgent2), so, competeNum, 0, null);
		lastResult = sc.scTup[0];
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
	}

 	/**
 	 * competeBoth against opponent, from default start state	
 	 * @param paVector	the vector of play agents
	 * @param gb		needed to get default start state (competeBoth)
	 * @return			the evaluation result
	 */
 	private EvalResult evaluateAgent1(PlayAgtVector paVector, GameBoard gb, int competeNum, double thresh) {
 		StateObservation so = gb.getDefaultStartState();
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(paVector, so, competeNum, 0, null);
		lastResult = sc.scTup[0];
		m_msg = paVector.pavec[0].getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
	}
 	
 	/**
 	 * competeBoth against opponent, from different start states	
	 * @param paVector	the vector of play agents
	 * @param gb		needed to get the start states
	 * @return			the evaluation result
 	 */
 	private EvalResult evaluateAgent2(PlayAgtVector paVector, GameBoard gb, int competeNum, double thresh) {
//		double[] res;
//		double resX, resO;
        double success;
        double averageSuccess = 0; 
		
		lastResult=0;
 		
		int numK=10;
		for (int k=0; k<numK; ++k) {
			// Choose randomly one of the possible 0-1-ply start states. Repeat 
			// this numK times to sample a representative subset of possible configurations.
			StateObservation startSO = gb.chooseStartState();
			ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(paVector, startSO, competeNum, 0, null);
    		success = sc.scTup[0];
    		averageSuccess += success;
		}
        averageSuccess /= numK;
		lastResult=averageSuccess;
		
		m_msg = paVector.pavec[0].getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);		// this.verbose is def'd in Evaluator
		
		return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
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
		DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);
		df.applyPattern("0.00");
		return switch (mode) {
			case 0 -> "0.9"; // random agent, competeAllRoles
			case 1 -> "0.333"; // MaxN, competeAllRoles
			case 2 -> "0.333"; // MaxN, diff. starts, competeAllRoles
			case 3 -> "0.333"; // MCTS, competeAllRoles
			case 4 -> "0.333"; // MCTS, diff. starts, competeAllRoles
			case 11 -> "0.333"; // TDReferee (if perfect), different starts, competeAllRoles
			default -> null;
		};
	}
		
	@Override
	public String getPrintString() {
		String strBest = getBestResultString(m_mode);
		return switch (m_mode) {
			case 0 -> "success rate (randomAgent, best is " + strBest + "): ";
			case 1 -> "success rate (Max-N, expec is " + strBest + "): ";
			case 2 -> "success rate (Max-N, different starts, expec is " + strBest + "): ";
			case 3 -> "success rate (MCTS, expec is " + strBest + "): ";
			case 4 -> "success rate (MCTS, different starts, expec is " + strBest + "): ";
			case 11 -> "success rate (TDReferee, different starts, expec is " + strBest + "): ";
			default -> null;
		};
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
		return switch (m_mode) {
			case 0 -> "success against Random";
			case 1 -> "success against Max-N";
			case 2 -> "success against Max-N, dStart";
			case 3 -> "success against MCTS";
			case 4 -> "success against MCTS, dStart";
			case 11 -> "success TDReferee";
			default -> null;
		};
	}

}
