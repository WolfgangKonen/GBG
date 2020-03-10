package games.Othello;

import java.util.ArrayList;
import java.util.List;

import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import controllers.MCTS.MCTSAgentT;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import games.XArenaFuncs;
import games.Hex.StateObserverHex;
import games.Othello.BenchmarkPlayer.BenchMarkPlayer;
import params.ParMCTS;
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
 * The value of mode is set in the constructor. 
 */
public class EvaluatorOthello extends Evaluator{

    private RandomAgent randomAgent;
	private MaxNAgent maxNAgent;
	private BenchMarkPlayer heurPlayer;
	private BenchMarkPlayer benchPlayer;
	private MCTSAgentT mctsAgent;
	
	/**
	 * A list of all Othello states which are NPLY_DS plies away from the default start state.
	 * For NPLY_DS=(1, 2, 3, 4) these are (4, 12, 56, 244) states. <br>
	 * According to [ReeWiering03], in case NPLY_DS==4,  only 236 of them are truly different.
	 * <p>
	 * This list is a static member, so that it needs to be constructed only once. <br>
	 * This list is used in {@code mode} 19, 20, 21.
	 */
	protected static ArrayList<StateObserverOthello> diffStartList = null;
	protected static int NPLY_DS = 4;

	
    public EvaluatorOthello(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
		super(e_PlayAgent, gb, 1, stopEval);		// default mode: 1
		initEvaluator(gb);
	}
    
    public EvaluatorOthello(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode) {
		super(e_PlayAgent, gb, mode, stopEval);
		initEvaluator(gb);
	}
    
    
	public EvaluatorOthello(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, gb, mode, stopEval, verbose);
		initEvaluator(gb);

	}
	
	public void initEvaluator(GameBoard gb){
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
	
	public boolean evalAgent(PlayAgent playAgent){
		int competeNum = (m_mode>11) ?  1 : 10;			// number of episodes in evaluation competition
		m_PlayAgent = playAgent;
		boolean diffStarts = true;
        
        // when evalAgent is called for the first time, construct diffStartList once for all 
        // EvaluatorOthello objects (will not change during runtime)
        if (diffStartList==null) {
        	StateObserverOthello so = (StateObserverOthello) m_gb.getDefaultStartState();
        	diffStartList = new  ArrayList<StateObserverOthello>(); 
        	diffStartList = addAllNPlyStates(diffStartList,so,0);        	
        }

		switch(m_mode) {
		case -1:
			m_msg = "no evaluation done ";
			lastResult = 0.0;
			return false;
		case 0: return evaluateAgainstOpponent(m_PlayAgent, randomAgent, false, competeNum) > 0.0;	
		case 1:	return evaluateAgainstOpponent(m_PlayAgent,   maxNAgent, false, competeNum) > 0.0;	
		case 2:	return evaluateAgainstOpponent(m_PlayAgent,   mctsAgent, false, competeNum) > 0.0;	
		case 9:	return evaluateAgainstOpponent(m_PlayAgent, benchPlayer, false, competeNum) > 0.0;	
		case 10:return evaluateAgainstOpponent(m_PlayAgent,  heurPlayer, false, competeNum) > 0.0;	
		case 11: 
			//	Evaluator.getTDReferee throws RuntimeException, if TDReferee.agt.zip is not found:
			return evaluateAgainstOpponent(m_PlayAgent, this.getTDReferee(), false, competeNum) > 0.0;	
		case 19:return evaluateAgainstOpponent(m_PlayAgent, benchPlayer, diffStarts, competeNum) > 0.0;	
		case 20:return evaluateAgainstOpponent(m_PlayAgent,  heurPlayer, diffStarts, competeNum) > 0.0;	
		case 21: 
			//	Evaluator.getTDReferee throws RuntimeException, if TDReferee.agt.zip is not found:
			return evaluateAgainstOpponent(m_PlayAgent, this.getTDReferee(), diffStarts, competeNum) > 0.0;	
		default: return false;
		}		
	}
	
	/**
	 * Evaluate {@link PlayAgent} {@code playAgent} vs. {@code opponent}, both playing in <b>both roles</b>.
	 * Perform {@code competeNum} such competitions and average results. 
	 * 
	 * @param playAgent
	 * @param opponent
	 * @param diffStarts	If true, select in each of the {@code competeNum} competitions <b>every</b> 
	 * 						element from {@link #diffStartList} and play two episodes: 
	 * 						{@code playAgent} vs. {@code opponent} and {@code opponent} vs. {@code playAgent}.
	 * 						If false, start always from default start state and play in both roles.  
	 * @param competeNum
	 * @return a {@link ScoreTuple} with two elements, holding the average score for {@code playAgent} and {@code opponent}. 
	 */
	private double evaluateAgainstOpponent(PlayAgent playAgent, PlayAgent opponent, boolean diffStarts, int competeNum) {
		StateObservation so = m_gb.getDefaultStartState();
		int N = so.getNumPlayers();
		ScoreTuple scMean = new ScoreTuple(N);
		if (diffStarts) 	// start from all start states in diffStartList
		{
			double sWeight = 1 / (double) (competeNum * diffStartList.size());
			int count=0;
			ScoreTuple sc = null;
			for (int c=0; c<competeNum; c++) {
				for (StateObservation sd : diffStartList) {
					sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, opponent), sd, competeNum, 0);			
					scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
					//count++;
				}
			}	
			//System.out.println("count = "+ count);
		} else 		// start always from default start state
		{
			scMean = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, opponent), so, competeNum, 0);						
		}
		lastResult = scMean.scTup[0];
		m_msg = playAgent.getName()+": "+getPrintString() + lastResult; 
	    return lastResult;
    }
	 
	/**
	 * This method is deprecated since it has much more fluctuation than 
	 * {@link #evaluateAgainstOpponent(PlayAgent, PlayAgent, boolean, int) evaluateAgainstOpponent}.
	 * <p>
	 * Evaluate {@link PlayAgent} {@code playAgent} vs. {@code opponent}, both playing in both roles.
	 * Perform {@code competeNum} such competitions and average results. 
	 * 
	 * @param playAgent
	 * @param opponent
	 * @param diffStarts	If false, start from default start state. If true, select in each of the {@code competeNum} 
	 * 						competitions a random start state according to {@link GameBoardOthello#chooseStartState()}.
	 * @param competeNum
	 * @return a {@link ScoreTuple} with two elements, holding the average score for {@code playAgent} and {@code opponent}. 
	 */
	@Deprecated
	private double evaluateAgainstOpponent_OLD(PlayAgent playAgent, PlayAgent opponent, boolean diffStarts, int competeNum) {
		StateObservation so = m_gb.getDefaultStartState();
		int N = so.getNumPlayers();
		ScoreTuple sc = null;
		ScoreTuple scMean = new ScoreTuple(N);
		double sWeight = 1 / (double) competeNum;
		for (int c=0; c<competeNum; c++) {
			if (diffStarts) so = m_gb.chooseStartState();	// choose a different start state in each pass
			sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, opponent), so, competeNum, 0);			
			scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
		}
		lastResult = scMean.scTup[0];
		m_msg = playAgent.getName()+": "+getPrintString() + lastResult; 
	    return lastResult;
    }
	 
	  
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
		switch (m_mode) {
		case -1: return "no evaluation done ";
		case 0:  return "success against Random (best is 1.0): ";
		case 1:  return "success against Max-N (best is 1.0): ";
		case 2:  return "success against MCTS (best is 1.0): ";
		case 9:  return "success against BenchPlayer (best is 1.0): ";
		case 10: return "success against HeurPlayer (best is 1.0): ";
		case 11: return "success against TDReferee.agt.zip (best is 1.0): ";
		case 19: return "success against BenchPlayer, diff starts (best is 1.0): ";
		case 20: return "success against HeurPlayer, diff starts (best is 1.0): ";
		case 21: return "success against TDReferee.agt.zip, diff starts (expected 0.0): ";
		default: return null;
		}
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
		switch (m_mode) {
          case 0:  return "success against Random";
          case 1:  return "success against MaxN";
          case 2:  return "success against MCTS";
          case 9:  return "success against BenchPlayer";
          case 10: return "success against HeurPlayer";
          case 11: return "success against TDReferee"; 
          case 19: return "success vs BenchPlayer, diff";
          case 20: return "success vs HeurPlayer, diff";
          case 21: return "success vs TDReferee, diff"; 
          default: return null;
		 }
	}
}