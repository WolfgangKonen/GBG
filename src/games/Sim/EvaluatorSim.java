package games.Sim;

import agentIO.AgentLoader;
import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.RandomAgent;
import controllers.MCTS.MCTSAgentT;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import games.XArenaFuncs;

public class EvaluatorSim extends Evaluator {
	
	private static final int[] AVAILABLE_MODES = {-1,0,1};		//9,
	private RandomAgent random_agent = new RandomAgent("Random");
	private RandomAgent random_agent2 = new RandomAgent("Random2");
	private MCTSAgentT mctsAgent = new MCTSAgentT();
	private MCTSAgentT mctsAgent2 = new MCTSAgentT();
	protected double[] thresh={0.8,-0.15,-0.15}; // threshold for each value of m_mode
	private GameBoard gb;

	public EvaluatorSim(PlayAgent e_PlayAgent, int mode, int stopEval) {
		super(e_PlayAgent, mode, stopEval);
	}
	
	public EvaluatorSim(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, mode, stopEval, verbose);
		this.gb = gb;
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
			if(ConfigSim.NUM_PLAYERS > 2)
				return evaluateAgentRandom3Player(m_PlayAgent,gb)>thresh[0];
			else
				return evaluateAgentRandom(m_PlayAgent,gb)>thresh[0];
		case 1:
			if(ConfigSim.NUM_PLAYERS > 2)
				return evaluateMCTS3Player(m_PlayAgent,gb)>thresh[0];
			else
				return evaluateMCTS(m_PlayAgent,gb)>thresh[0];
		
		default: return false;
		}
	}
	
	private double evaluateAgentRandom3Player(PlayAgent pa, GameBoard gb)
	{
		StateObservation so = gb.getDefaultStartState();
		lastResult = XArenaFuncs.compete3(pa, random_agent, random_agent2, so, 100, 0, gb);
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return lastResult;
	}

	private double evaluateMCTS(PlayAgent pa, GameBoard gb) {
 		StateObservation so = gb.getDefaultStartState();
		lastResult = XArenaFuncs.competeBoth(pa, mctsAgent, so, 1, 0, gb);
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return lastResult;
	}
	
	private double evaluateAgentRandom(PlayAgent pa, GameBoard gb) {
 		StateObservation so = gb.getDefaultStartState();
		lastResult = XArenaFuncs.competeBoth(pa, random_agent, so, 100, 0, gb);
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return lastResult;
	}
	
	private double evaluateMCTS3Player(PlayAgent pa, GameBoard gb) {
 		StateObservation so = gb.getDefaultStartState();
		lastResult = XArenaFuncs.compete3(pa, mctsAgent,mctsAgent2, so, 1, 0, gb);
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return lastResult;
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
		switch (m_mode) 
		{
		case -1: return "no evaluation done ";
		case 0:  return "success rate (against randomAgent, best is 0.9): ";
		default: return null;
		}
	}

	@Override
	public String getTooltipString() {
		return "<html>-1: none<br>"
				+ "0: against Random, best is 0.9<br>"
				+ "</html>";
	}

	@Override
	public String getPlotTitle() {
		switch (m_mode) {
		case 0:  return "success against Random";		
		default: return null;
		}
	}

}
