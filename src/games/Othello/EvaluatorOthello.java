package games.Othello;

import java.util.ArrayList;
import java.util.List;

import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import controllers.MCTS.MCTSAgentT;
import games.*;
import games.Othello.BenchmarkPlayer.BenchMarkPlayer;
import params.ParMaxN;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types.ACTIONS;

/**
 * Evaluator for the game Othello. Depending on the value of parameter {@code mode} in constructor:
 * <ul>
 * <li> -1: no evaluation
 * <li>  0: compete against Random
 * <li>  1: compete against Max-N (limited tree depth: 4)
 * <li>  2: compete against MCTS
 * <li>  9: compete against BenchPlayer
 * <li> 10: compete against HeurPlayer
 * <li> 11: compete against TDReferee.agt.zip
 * <li> 19: compete against BenchPlayer, different starts
 * <li> 20: compete against HeurPlayer, different starts
 * <li> 21: compete against TDReferee.agt.zip, different starts
 * </ul>  
 * The value of {@code mode} is set in the constructor. <p>
 * Competition is done in all roles for {@code numEpisodes=10} episodes. <br>
 * For 'different starts' see {@link #diffStartList}. In that case we use {@code numEpisodes=1}, because we have already
 * many start states.
 */
public class EvaluatorOthello extends Evaluator {

    private RandomAgent randomAgent;
	private MaxNAgent maxNAgent;
	private BenchMarkPlayer heurPlayer;
	private BenchMarkPlayer benchPlayer;
	private MCTSAgentT mctsAgent;
	
	/**
	 * A list of all Othello states which are {@link #NPLY_DS} plies away from the default start state.
	 * For {@link #NPLY_DS}=(1, 2, 3, 4) these are (4, 12, 56, 244) states. <br>
	 * According to [ReeWiering03], in case {@link #NPLY_DS}==4,  only 236 of them are truly different.
	 * <p>
	 * This list is a static member, so that it needs to be constructed only once. <br>
	 * This list is used in evaluation mode 19, 20 and 21.
	 */
	protected static ArrayList<StateObserverOthello> diffStartList = null;
	/**
	 * Number of plies for {@link #diffStartList}
	 */
	protected static int NPLY_DS = 4;

	// --- never used ---
//    public EvaluatorOthello(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
//		super(e_PlayAgent, gb, 1, stopEval);		// default mode: 1
//		initEvaluator();
//	}
//
//    public EvaluatorOthello(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode) {
//		super(e_PlayAgent, gb, mode, stopEval);
//		initEvaluator();
//	}
    
    
	public EvaluatorOthello(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, gb, mode, stopEval, verbose);
		initEvaluator();
	}
	
	public void initEvaluator(){
		ParMaxN params = new ParMaxN();
        int maxNDepth =  4; // set to 4 otherwise it will take too long
        params.setMaxNDepth(maxNDepth);
        randomAgent = new RandomAgent("Random");
        maxNAgent = new MaxNAgent("Max-N", params, new ParOther());
        mctsAgent = new MCTSAgentT();
        heurPlayer = new BenchMarkPlayer("HeurPlayer", 0);
        benchPlayer = new BenchMarkPlayer("BenchPlayer", 1);
	}

	private static ArrayList<StateObserverOthello> addAllNPlyStates(ArrayList<StateObserverOthello> diffStartList
			, StateObserverOthello s, int k) {
		if (k<NPLY_DS) {
	        List<ACTIONS> actions = s.getAvailableActions();
	        for (ACTIONS a : actions) {
	        	StateObserverOthello s_copy = s.copy();
	        	s_copy.advance(a);
	        	if (k==NPLY_DS-1) diffStartList.add(s_copy);
	        	addAllNPlyStates(diffStartList,s_copy,k+1);
	        }
			
		}
		return diffStartList;
	}
	
	public EvalResult evalAgent(PlayAgent playAgent){
		boolean diffStarts = true;
		int numEpisodes = (m_mode>11) ?  1 : 10;			// number of episodes in evaluation competition.
				// We take in the diffStarts-modes 19,20,21 only numEpisodes=1, since we have there 244 different
				// start states in diffStartList (at least for NPLY_DS=4).
        
        // when evalAgent is called for the first time, construct diffStartList once for all 
        // EvaluatorOthello objects (will not change during runtime)
        if (diffStartList==null) {
        	StateObserverOthello so = (StateObserverOthello) m_gb.getDefaultStartState();
        	diffStartList = new  ArrayList<>();
        	diffStartList = addAllNPlyStates(diffStartList,so,0);
        }

		m_PlayAgent = playAgent;
		switch(m_mode) {
		case -1:
			m_msg = "no evaluation done ";
			lastResult = 0.0;
			return new EvalResult(lastResult, true, m_msg, m_mode, Double.NaN);
		case 0: return evaluateAgainstOpponent(m_PlayAgent, randomAgent, false, numEpisodes, 0.0);
		case 1:	return evaluateAgainstOpponent(m_PlayAgent,   maxNAgent, false, numEpisodes, 0.0);
		case 2:	return evaluateAgainstOpponent(m_PlayAgent,   mctsAgent, false, numEpisodes, 0.0);
		case 9:	return evaluateAgainstOpponent(m_PlayAgent, benchPlayer, false, numEpisodes, 0.0);
		case 10:return evaluateAgainstOpponent(m_PlayAgent,  heurPlayer, false, numEpisodes, 0.0);
		case 11: 
			//	Evaluator.getTDReferee throws RuntimeException, if TDReferee.agt.zip is not found:
			return evaluateAgainstOpponent(m_PlayAgent, this.getTDReferee(), false, numEpisodes, 0.0);
		case 19:return evaluateAgainstOpponent(m_PlayAgent, benchPlayer, diffStarts, numEpisodes, 0.0);
		case 20:return evaluateAgainstOpponent(m_PlayAgent,  heurPlayer, diffStarts, numEpisodes, 0.0);
		case 21: 
			//	Evaluator.getTDReferee throws RuntimeException, if TDReferee.agt.zip is not found:
			return evaluateAgainstOpponent(m_PlayAgent, this.getTDReferee(), diffStarts, numEpisodes, 0.0);
		default: throw new RuntimeException("Invalid m_mode = "+m_mode);
		}		
	}
	
	/**
	 * Evaluate {@link PlayAgent} {@code playAgent} vs. {@code opponent}, both playing in <b>both roles</b>.
	 * Perform {@code numEpisodes} such competitions and average results.
	 * 
	 * @param playAgent agent to be evaluated
	 * @param opponent agent against which {@code playAgent} plays.
	 * @param diffStarts	If true, select in each of the {@code numEpisodes} competitions <b>every</b>
	 * 						element from {@link #diffStartList} and play two episodes: 
	 * 						{@code playAgent} vs. {@code opponent} and {@code opponent} vs. {@code playAgent}.
	 * 						If false, always start from default start state and play in both roles.
	 * @param numEpisodes number of episodes played during evaluation
	 * @return a {@link ScoreTuple} with two elements, holding the average score for {@code playAgent} and {@code opponent}. 
	 */
	private EvalResult evaluateAgainstOpponent(PlayAgent playAgent, PlayAgent opponent, boolean diffStarts,
										   int numEpisodes, double thresh) {
		StateObservation so = m_gb.getDefaultStartState();
		int N = so.getNumPlayers();
		ScoreTuple scMean = new ScoreTuple(N);
		if (diffStarts) 	// start from all start states in diffStartList
		{
			double sWeight = 1 / (double) (numEpisodes * diffStartList.size());
			int count=0;
			ScoreTuple sc;
			for (int c=0; c<numEpisodes; c++) {
				for (StateObservation sd : diffStartList) {
					sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, opponent), sd, 1, 0);
					scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
					count++;
				}
			}	
			System.out.println("count = "+ count);
		} 
		else 		// always start from default start state
		{
			scMean = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, opponent), so, numEpisodes, 0);
		}
		lastResult = scMean.scTup[0];
		m_msg = playAgent.getName()+": "+getPrintString() + lastResult; 
	    return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
    }
	 
//	/**
//	 * This method is deprecated since it has much more fluctuation than
//	 * {@link #evaluateAgainstOpponent(PlayAgent, PlayAgent, boolean, int, double) evaluateAgainstOpponent}.
//	 * <p>
//	 * Evaluate {@link PlayAgent} {@code playAgent} vs. {@code opponent}, both playing in both roles.
//	 * Perform {@code numEpisodes} such competitions and average results.
//	 *
//	 * @param playAgent agent to be evaluated
//	 * @param opponent agent against which {@code playAgent} plays.
//	 * @param diffStarts	If false, start from default start state. If true, select in each of the {@code numEpisodes}
//	 * 						competitions a random start state according to {@link GameBoardOthello#chooseStartState()}.
//	 * @param numEpisodes number of episodes played during evaluation
//	 * @return a {@link ScoreTuple} with two elements, holding the average score for {@code playAgent} and {@code opponent}.
//	 */
//	@Deprecated
//	private double evaluateAgainstOpponent_OLD(PlayAgent playAgent, PlayAgent opponent, boolean diffStarts, int numEpisodes) {
//		StateObservation so = m_gb.getDefaultStartState();
//		int N = so.getNumPlayers();
//		ScoreTuple sc;
//		ScoreTuple scMean = new ScoreTuple(N);
//		double sWeight = 1 / (double) numEpisodes;
//		for (int c=0; c<numEpisodes; c++) {
//			if (diffStarts) so = m_gb.chooseStartState();	// choose a different start state in each pass
//			sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, opponent), so, 1, 0);
//			scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
//		}
//		lastResult = scMean.scTup[0];
//		m_msg = playAgent.getName()+": "+getPrintString() + lastResult;
//	    return lastResult;
//    }
	 
	  
	@Override
	public int[] getAvailableModes() {
		return new int[] {-1,0,1,2,9,10,11,19,20,21};
	}

	@Override
	public int getQuickEvalMode() {
		return 10;
	}

	@Override
	public int getTrainEvalMode() {
		return 10;
	}

	@Override
	public String getPrintString() {
		return switch (m_mode) {
			case -1 -> "no evaluation done ";
			case 0 -> "success against Random (best is 1.0): ";
			case 1 -> "success against Max-N (best is 1.0): ";
			case 2 -> "success against MCTS (best is 1.0): ";
			case 9 -> "success against BenchPlayer (best is 1.0): ";
			case 10 -> "success against HeurPlayer (best is 1.0): ";
			case 11 -> "success against TDReferee.agt.zip (expected 0.0): ";
			case 19 -> "success against BenchPlayer, diff starts (best is 1.0): ";
			case 20 -> "success against HeurPlayer, diff starts (best is 1.0): ";
			case 21 -> "success against TDReferee.agt.zip, diff starts (expected 0.0): ";
			default -> null;
		};
	}

	@Override
	public String getTooltipString() {
		return "<html>-1: none<br>"
				+ " 0: vs. Random, best is 1.0<br>"
				+ " 1: vs. MaxN, best is 1.0<br>"
				+ " 2: vs. MCTS, best is 1.0<br>"
				+ " 9: vs. BenchPlayer, best is 0.0<br>"
				+ "10: vs. HeurPlayer, best is 1.0<br>"
				+ "11: vs. TDReferee.agt.zip<br>"
				+ "19: vs. BenchPlayer, diff,best is 0.0<br>"
				+ "20: vs. HeurPlayer, diff, best is 1.0 <br>"
				+ "21: vs. TDReferee.agt.zip, diff"
				+ "</html>";
	}

	@Override
	public String getPlotTitle() {
		return switch (m_mode) {
			case 0 -> "success against Random";
			case 1 -> "success against MaxN";
			case 2 -> "success against MCTS";
			case 9 -> "success against BenchPlayer";
			case 10 -> "success against HeurPlayer";
			case 11 -> "success against TDReferee";
			case 19 -> "success vs BenchPlayer, diff";
			case 20 -> "success vs HeurPlayer, diff";
			case 21 -> "success vs TDReferee, diff";
			default -> null;
		};
	}
}