package games.Othello;

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
 * </ul>  
 * The value of mode is set in the constructor. 
 */
public class EvaluatorOthello extends Evaluator{

    private RandomAgent randomAgent;
	private MaxNAgent maxNAgent;
	private BenchMarkPlayer heurPlayer;
	private BenchMarkPlayer benchPlayer;
	private MCTSAgentT mctsAgent;
	

	
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


	public boolean evalAgent(PlayAgent playAgent){
		int competeNum = 10;			// number of games in evaluation competition
		m_PlayAgent = playAgent;
		switch(m_mode) {
		case -1:
			m_msg = "no evaluation done ";
			lastResult = Double.NaN;
			return false;
		case 0: return evaluateAgainstOpponent(m_PlayAgent, randomAgent, competeNum, m_gb) > 0.0;	
		case 1:	return evaluateAgainstOpponent(m_PlayAgent,   maxNAgent, competeNum, m_gb) > 0.0;	
		case 2:	return evaluateAgainstOpponent(m_PlayAgent,   mctsAgent, competeNum, m_gb) > 0.0;	
		case 9:	return evaluateAgainstOpponent(m_PlayAgent, benchPlayer, competeNum, m_gb) > 0.0;	
		case 10:return evaluateAgainstOpponent(m_PlayAgent,  heurPlayer, competeNum, m_gb) > 0.0;	
		case 11: 
			//	Evaluator.getTDReferee throws RuntimeException, if TDReferee.agt.zip is not found:
			return evaluateAgainstOpponent(m_PlayAgent, this.getTDReferee(), competeNum, m_gb) > 0.0;	
		default: return false;
		}		
	}
	
	private double evaluateAgainstOpponent(PlayAgent playAgent, PlayAgent opponent, int competeNum, GameBoard gameBoard) {
		StateObservation so = gameBoard.getDefaultStartState();
//		lastResult = XArenaFuncs.competeBoth(playAgent, opponent, so, competeNum, 0, gameBoard);
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, opponent), so, competeNum, 0);
		lastResult = sc.scTup[0];
		m_msg = playAgent.getName()+": "+getPrintString() + lastResult; 
	    return lastResult;
	    }
	 
	  
	@Override
	public int[] getAvailableModes() {
		// TODO Auto-generated method stub
		return new int[] {-1,0,1,2,9,10,11};
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
		default: return null;
		}
	}

	@Override
	public String getTooltipString() {
		return "<html>-1: none<br>"
				+ "0: against Random, best is 1.0<br>"
				+ "1: against MaxN, best is 1.0<br>"
				+ "2: against MCTS, best is 1.0<br>"
				+ "9: against BenchPlayer, best is 0.0<br>"
				+ "10: against HeurPlayer, best is 1.0<br>"
				+ "11: against TDReferee.agt.zip"
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
          default: return null;
		 }
	}
}