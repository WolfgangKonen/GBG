package games.KuhnPoker;

import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import games.*;
import tools.ScoreTuple;

/**
 * Evaluator for KuhnPoker
 */
public class EvaluatorKuhnPoker extends Evaluator {

	private final RandomAgent randomAgent = new RandomAgent("Random");
	private final KuhnPokerAgent kuhnAgent = new KuhnPokerAgent("Kuhn");


	protected double[] m_thresh={
			10, // random
			5 // kuhn
	}; // threshold for each value of m_mode

//	protected static ArrayList<StateObserverKuhnPoker> diffStartList = null;


	public EvaluatorKuhnPoker(PlayAgent e_PlayAgent, GameBoard gb, int mode, int verbose) {
		super(e_PlayAgent, gb, mode, verbose);
		//initEvaluator(gb);
	}
	
//	private void initEvaluator(GameBoard gb) {
//		//gb.getArena();
//	}

	@Override
	public EvalResult evalAgent(PlayAgent playAgent) {
		m_PlayAgent = playAgent;
		switch(m_mode) {
			case -1:
				m_msg = "no evaluation done ";
				lastResult = Double.NaN;
				return new EvalResult(lastResult, true, m_msg, m_mode, Double.NaN);
			case 0:  return evalAgent1(m_PlayAgent,randomAgent,m_gb,m_thresh[0]);
			case 1:  return evalAgent1(m_PlayAgent,kuhnAgent,m_gb,m_thresh[0]);
			default: throw new RuntimeException("Invalid m_mode = "+m_mode);
		}
	}

	// --- never used ---
//	public void evalAgentSpecificState(PlayAgent playAgent, StateObserverKuhnPoker so) {
//		m_PlayAgent = playAgent;
//		PlayAgent[] pavec = new PlayAgent[] {playAgent,randomAgent};
//		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(pavec), so, 10, 2, null);
//		lastResult = sc.scTup[0];
//		m_msg = playAgent.getName()+": "+getPrintString() + lastResult;
//		System.out.println(m_msg);
//	}

	public EvalResult evalAgent1(PlayAgent playAgent, PlayAgent opponent, GameBoard gb, double thresh) {
		StateObservation so = gb.getDefaultStartState();
		PlayAgent[] pavec = new PlayAgent[] {playAgent,opponent};
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pavec), so, 1000, 0);
		lastResult = sc.scTup[0];
		m_msg = playAgent.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
	}

//	public double specificState(){
//		// Setup SO
//		// GETAction
//		// Check if Action is stupid
//
//		return 0;
//	}

 	@Override
 	public int[] getAvailableModes() {
		int[] modes;
		modes = new int[]{-1,0,1};
		return modes;
 	}
 	
 	@Override
 	public int getDefaultEvalMode() {
		return 0;
	}
	public int getQuickEvalMode()
	{
		return 0;
	}
	public int getTrainEvalMode()
	{
		return 0;
	}

	@Override
	public String getPrintString() {
		return switch (m_mode) {
			case -1 -> "no evaluation done ";
			case 0 -> "success rate (against random agent): ";
			case 1 -> "success rate (against KuhnPokerAgent): ";
			default -> null;
		};
	}

	@Override
	public String getTooltipString() {
		return "<html>-1: none<br>"
				+ "</html>";
	}

	@Override
	public String getPlotTitle() {
		return switch (m_mode) {
			case 0 -> "average chips vs Random";
			case 1 -> "average chips vs Kuhn";
			default -> null;
		};
	}

}
