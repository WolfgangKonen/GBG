package games.Sim;

import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import controllers.MCTS.MCTSAgentT;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import games.XArenaFuncs;
import tools.ScoreTuple;

public class EvaluatorSim extends Evaluator {
	
	private static final int[] AVAILABLE_MODES = {-1,0,1,2};		//9,
	private RandomAgent randomAgent = new RandomAgent("Random");
	private RandomAgent randomAgent2 = new RandomAgent("Random2");
	private MCTSAgentT mctsAgent = new MCTSAgentT();
	private MCTSAgentT mctsAgent2 = new MCTSAgentT();
	private MaxNAgent maxNAgent = new MaxNAgent("MaxN",15,true);
	private MaxNAgent maxNAgent2 = new MaxNAgent("MaxN",15,true);
	private PlayAgent secondAgent, thirdAgent;
	private PlayAgtVector paVector;
	protected int[] competeNum={100,5,20};
	protected double[] thresh={0.8,-0.15,-0.15, -0.15}; // threshold for each value of m_mode
	
	private GameBoard m_gb;

//	public EvaluatorSim(PlayAgent e_PlayAgent, int mode, int stopEval) {
//		super(e_PlayAgent, mode, stopEval);
//	}
	
	public EvaluatorSim(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, gb, mode, stopEval, verbose);
		this.m_gb = gb;
	}

	@Override
	protected boolean evalAgent(PlayAgent playAgent) {
		m_PlayAgent = playAgent;
		switch(m_mode) {
		case -1: 
			m_msg = "no evaluation done ";
			lastResult = Double.NaN;
			return false;
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
			return false;
		}
		
		switch (ConfigSim.NUM_PLAYERS) {
		case 2: 
			paVector = new PlayAgtVector(m_PlayAgent,secondAgent);
			break;
		case 3:
			paVector = new PlayAgtVector(m_PlayAgent,secondAgent,thirdAgent);
			break;
		default:
			paVector = null;
		}
		
		return evaluateAgainstOpponents(paVector, competeNum[m_mode], m_gb)>thresh[m_mode];
	}
	
	private double evaluateAgainstOpponents(PlayAgtVector paVector, int competeNum, GameBoard gb) {
 		StateObservation so = gb.getDefaultStartState();
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(paVector, so, competeNum, 0);
		lastResult = sc.scTup[0];
		m_msg = paVector.pavec[0].getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return lastResult;
	}
	

	// all the following six functions are obsolete: we have evaluateAgainstOpponents()
	// and a new unifying logic in evalAgent.
	//
//	private double evaluateAgentRandom(PlayAgent pa, GameBoard gb) {
// 		StateObservation so = gb.getDefaultStartState();
////		lastResult = XArenaFuncs.competeBoth(pa, random_agent, so, 100, 0, gb);
//		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa, randomAgent), so, 100, 0);
//		lastResult = sc.scTup[0];
//		m_msg = pa.getName()+": "+getPrintString() + lastResult;
//		if (this.verbose>0) System.out.println(m_msg);
//		return lastResult;
//	}
//	
//	private double evaluateMaxN(PlayAgent pa, GameBoard gb) {
// 		StateObservation so = gb.getDefaultStartState();
////		lastResult = XArenaFuncs.competeBoth(pa, maxNAgent, so, 5, 0, gb);
//		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa, maxNAgent), so, 5, 0);
//		lastResult = sc.scTup[0];
//		m_msg = pa.getName()+": "+getPrintString() + lastResult;
//		if (this.verbose>0) System.out.println(m_msg);
//		return lastResult;
//	}
//	
//	private double evaluateMCTS(PlayAgent pa, GameBoard gb) {
// 		StateObservation so = gb.getDefaultStartState();
////		lastResult = XArenaFuncs.competeBoth(pa, mctsAgent, so, 5, 0, gb);
//		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa, mctsAgent), so, 5, 0);
//		lastResult = sc.scTup[0];
//		m_msg = pa.getName()+": "+getPrintString() + lastResult;
//		if (this.verbose>0) System.out.println(m_msg);
//		return lastResult;
//	}
//	
//	private double evaluateAgentRandom3Player(PlayAgent pa, GameBoard gb)
//	{
//		StateObservation so = gb.getDefaultStartState();
////		lastResult = XArenaFuncs.compete3(pa, random_agent, random_agent2, so, 100, 0, gb);
//		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa, randomAgent, randomAgent2), so, 100, 0);
//		lastResult = sc.scTup[0];
//		m_msg = pa.getName()+": "+getPrintString() + lastResult;
//		if (this.verbose>0) System.out.println(m_msg);
//		return lastResult;
//	}
//
//	private double evaluateMaxN3Player(PlayAgent pa, GameBoard gb) {
// 		StateObservation so = gb.getDefaultStartState();
////		lastResult = XArenaFuncs.compete3(pa, maxNAgent,maxNAgent2, so, 5, 0, gb);
//		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa, maxNAgent,maxNAgent2), so, 5, 0);
//		lastResult = sc.scTup[0];
//		m_msg = pa.getName()+": "+getPrintString() + lastResult;
//		if (this.verbose>0) System.out.println(m_msg);
//		return lastResult;
//	}
//	
//	private double evaluateMCTS3Player(PlayAgent pa, GameBoard gb) {
// 		StateObservation so = gb.getDefaultStartState();
////		lastResult = XArenaFuncs.compete3(pa, mctsAgent,mctsAgent2, so, 5, 0, gb);
//		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pa, mctsAgent,mctsAgent2), so, 5, 0);
//		lastResult = sc.scTup[0];
//		m_msg = pa.getName()+": "+getPrintString() + lastResult;
//		if (this.verbose>0) System.out.println(m_msg);
//		return lastResult;
//	}
	
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
		switch (m_mode) 
		{
		case -1: return "no evaluation done ";
		case 0:  return "success rate (against randomAgent, best is 0.9): ";
		case 1:  return "success rate (against MaxN, best is 0.0): ";
		case 2:  return "success rate (against MCTS, best is 0.0): ";
		default: return null;
		}
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
		switch (m_mode) {
		case 0:  return "success against Random";		
		case 1:  return "success against MaxN";		
		case 2:  return "success against MCTS";		
		default: return null;
		}
	}

}
