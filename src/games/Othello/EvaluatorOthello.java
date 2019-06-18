package games.Othello;

import agentIO.AgentLoader;
import controllers.MaxNAgent;
import controllers.PlayAgent;
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

public class EvaluatorOthello extends Evaluator{

    private GameBoard m_gb;
    private RandomAgent randomAgent = new RandomAgent("Random");
	private MaxNAgent maxNAgent;
	private BenchMarkPlayer heurPlayer;
	private BenchMarkPlayer benchPlayer;
	private MCTSAgentT mctsAgent;
	
    private AgentLoader agtLoader = null;

	
    public EvaluatorOthello(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
		super(e_PlayAgent, 1, stopEval);
		initEvaluator(gb);
	}
    
    public EvaluatorOthello(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode) {
		super(e_PlayAgent, mode, stopEval);
		initEvaluator(gb);
	}
    
    
	public EvaluatorOthello(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, mode, stopEval, verbose);
		initEvaluator(gb);

	}
	
	public void initEvaluator(GameBoard gb){
		m_gb = gb;
		ParMaxN params = new ParMaxN();
        int maxNDepth =  4; // set to 4 otherwise it will take too long
        params.setMaxNDepth(maxNDepth);
        maxNAgent = new MaxNAgent("Max-N", params, new ParOther());
        mctsAgent = new MCTSAgentT();
        heurPlayer = new BenchMarkPlayer("HeurPlayer", 0);
        benchPlayer = new BenchMarkPlayer("BenchPlayer", 1);
	}


	public boolean evalAgent(PlayAgent playAgent){
		m_PlayAgent = playAgent;
		switch(m_mode) {
		case -1:
			m_msg = "no evaluation done ";
			lastResult = Double.NaN;
			return false;
		case 0:
			return evaluateAgent0(m_PlayAgent, m_gb) >= 0.0;
		case 1:
			return evaluateAgent1(m_PlayAgent, m_gb)>= 0.0;
		case 2:
			return evaluateAgent2(m_PlayAgent, m_gb) >= 0.0;
		case 9:
			return evaluateAgent9(m_PlayAgent, m_gb) >= 0.0;
		case 10:
			return evaluateAgent10(m_PlayAgent, m_gb) >= 0.0;
		case 11: 
			if (agtLoader == null) agtLoader = new AgentLoader(m_gb.getArena(), "TDReferee.agt.zip");
			//return evaluateAgent10(m_PlayAgent, agtLoader.getAgent(), m_gb) > 0.0;	
		default: return false;
		}
		
		
	}
	
	private double evaluateAgent0(PlayAgent playAgent, GameBoard gameBoard) {
		StateObservation so = gameBoard.getDefaultStartState();
		lastResult = XArenaFuncs.competeBoth(playAgent, randomAgent, so, 10, 0, gameBoard);
		m_msg = playAgent.getName()+": "+getPrintString() + lastResult; 
	    return lastResult;
	    }
	 
	  private double evaluateAgent1(PlayAgent playAgent, GameBoard gameBoard) {
		  StateObservation so = gameBoard.getDefaultStartState(); 
		  lastResult = XArenaFuncs.competeBoth(playAgent, maxNAgent, so, 10, 0, gameBoard);
	      m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult;
	      return lastResult;
	    }
	
	  private double evaluateAgent2(PlayAgent playAgent, GameBoard gameBoard) {
		  StateObservation so = gameBoard.getDefaultStartState(); 
		  lastResult = XArenaFuncs.competeBoth(playAgent, mctsAgent, so, 10, 0, gameBoard);
	      m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult;
	      return lastResult;
	    }
	  private double evaluateAgent9(PlayAgent playAgent, GameBoard gameBoard) {
		  StateObservation so = gameBoard.getDefaultStartState(); 
		  lastResult = XArenaFuncs.competeBoth(playAgent, benchPlayer, so, 10, 0, gameBoard);
		  m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult;
		  return lastResult;
	    }
	
	  private double evaluateAgent10(PlayAgent playAgent, GameBoard gameBoard) {
		  StateObservation so = gameBoard.getDefaultStartState(); 
		  lastResult = XArenaFuncs.competeBoth(playAgent, heurPlayer, so, 100, 0, gameBoard);
		  m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult;
		  return lastResult;
	    }
	  
	  
	@Override
	public int[] getAvailableModes() {
		// TODO Auto-generated method stub
		return new int[] {-1,0,1,2,9,10};
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
				+ "11: against TDReferee.agt.zip, different starts"
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
          case 11: return "success against TDReferee"; // not ready yet
          default: return null;
		 }
	}
}