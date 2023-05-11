package games.Sim;

import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import controllers.MCTS.MCTSAgentT;
import games.*;
import tools.ScoreTuple;

public class EvaluatorSim extends Evaluator {
	
	private static final int[] AVAILABLE_MODES = {-1,0,1,2};		//9,
	private final RandomAgent randomAgent = new RandomAgent("Random");
	private final RandomAgent randomAgent2 = new RandomAgent("Random2");
	private final MCTSAgentT mctsAgent = new MCTSAgentT();
	private final MCTSAgentT mctsAgent2 = new MCTSAgentT();
	private final MaxNAgent maxNAgent = new MaxNAgent("MaxN",15,true);
	private final MaxNAgent maxNAgent2 = new MaxNAgent("MaxN",15,true);
	protected int[] competeNum={100,5,20};
	protected double[] thresh={0.8,-0.15,-0.15, -0.15}; // threshold for each value of m_mode
	
	private final GameBoard m_gb;

//	public EvaluatorSim(PlayAgent e_PlayAgent, int mode, int stopEval) {
//		super(e_PlayAgent, mode, stopEval);
//	}
	
	public EvaluatorSim(PlayAgent e_PlayAgent, GameBoard gb, int mode, int verbose) {
		super(e_PlayAgent, gb, mode, verbose);
		this.m_gb = gb;
	}

	@Override
	protected EvalResult evalAgent(PlayAgent playAgent) {
		PlayAgent secondAgent, thirdAgent;
		PlayAgtVector paVector;

		m_PlayAgent = playAgent;
		switch(m_mode) {
		case -1: 
			m_msg = "no evaluation done ";
			lastResult = Double.NaN;
			return new EvalResult(lastResult, true, m_msg, m_mode, Double.NaN);
		case 0:
			secondAgent = randomAgent;
			thirdAgent = randomAgent2;
			break;
//			if(ConfigSim.NUM_PLAYERS > 2)
//				return evaluateAgentRandom3Player(m_PlayAgent,gb)>thresh[0];
//			else
//				return evaluateAgentRandom(m_PlayAgent,gb)>thresh[0];
		case 1:
			secondAgent = maxNAgent;
			thirdAgent = maxNAgent2;
			break;
//			if(ConfigSim.NUM_PLAYERS > 2)
//				return evaluateMaxN3Player(m_PlayAgent,gb)>thresh[2];
//			else
//				return evaluateMaxN(m_PlayAgent,gb)>thresh[2];
		case 2:
			secondAgent = mctsAgent;
			thirdAgent = mctsAgent2;
			break;
//			if(ConfigSim.NUM_PLAYERS > 2)
//				return evaluateMCTS3Player(m_PlayAgent,gb)>thresh[1];
//			else
//				return evaluateMCTS(m_PlayAgent,gb)>thresh[1];		
		default:
			throw new RuntimeException("Invalid m_mode = "+m_mode);
		}

		paVector = switch (ConfigSim.NUM_PLAYERS) {
			case 2 -> new PlayAgtVector(m_PlayAgent, secondAgent);
			case 3 -> new PlayAgtVector(m_PlayAgent, secondAgent, thirdAgent);
			default -> null;
		};
		
		return evaluateAgainstOpponents(paVector, competeNum[m_mode], m_gb,thresh[m_mode]);
	}
	
	private EvalResult evaluateAgainstOpponents(PlayAgtVector paVector, int competeNum, GameBoard gb, double thresh) {
 		StateObservation so = gb.getDefaultStartState();
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(paVector, so, competeNum, 0, null);
		lastResult = sc.scTup[0];
		m_msg = paVector.pavec[0].getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
	}
	

	@Override
	public int[] getAvailableModes() {
		return AVAILABLE_MODES;
	}

	@Override
	public int getQuickEvalMode() {
		return 0;
	}

	@Override
	public int getTrainEvalMode() {
		return 0;
	}

	@Override
	public String getPrintString() 
	{
		return switch (m_mode) {
			case -1 -> "no evaluation done ";
			case 0 -> "success rate (against randomAgent, best is 0.9): ";
			case 1 -> "success rate (against MaxN, best is 0.0): ";
			case 2 -> "success rate (against MCTS, best is 0.0): ";
			default -> null;
		};
	}

	@Override
	public String getTooltipString() {
		return "<html>-1: none<br>"
				+ "0: against Random, best is 0.9<br>"
				+ "1: against MaxN, best is 0.0<br>"
				+ "2: against MCTS, best is 0.0<br>"
				+ "</html>";
	}

	@Override
	public String getPlotTitle() {
		return switch (m_mode) {
			case 0 -> "success against Random";
			case 1 -> "success against MaxN";
			case 2 -> "success against MCTS";
			default -> null;
		};
	}

}
