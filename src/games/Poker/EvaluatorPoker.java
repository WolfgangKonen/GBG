package src.games.Poker;

import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import games.XArenaFuncs;
import tools.ScoreTuple;

import java.util.ArrayList;

/**
 * Evaluator for Poker
 */
public class EvaluatorPoker extends Evaluator {

	private final RandomAgent randomAgent = new RandomAgent("Random");
	private final RandomAgent randomAgent2 = new RandomAgent("Random");
	private final RandomAgent randomAgent3 = new RandomAgent("Random");

	protected double[] m_thresh={0.8}; // threshold for each value of m_mode

	protected static ArrayList<StateObserverPoker> diffStartList = null;


	public EvaluatorPoker(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, gb, mode, stopEval, verbose);
		initEvaluator(gb);
	}
	
	private void initEvaluator(GameBoard gb) {
		gb.getArena();
	}

	@Override
	public boolean evalAgent(PlayAgent playAgent) {
		m_PlayAgent = playAgent;
		switch(m_mode) {
			case -1:
				m_msg = "no evaluation done ";
				lastResult = Double.NaN;
				return false;
			case 0:  return evalAgent1(m_PlayAgent,randomAgent,randomAgent2,randomAgent3,m_gb)>m_thresh[0];
			default: return false;
		}
	}

	public double evalAgent1(PlayAgent playAgent, PlayAgent opponent,PlayAgent opponent2,PlayAgent opponent3, GameBoard gb) {
		StateObservation so = gb.getDefaultStartState();
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent,opponent,opponent2,opponent3), so, 1, 0);
		lastResult = sc.scTup[0];
		m_msg = playAgent.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return lastResult;
	}
	

 	@Override
 	public int[] getAvailableModes() {
		int[] modes;
		modes = new int[]{0};
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
			case 0 -> "success rate (against randomAgent, best is 0.9): ";
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
			case 0 -> "success vs Random";
			case 1 -> "placeholder";
			default -> null;
		};
	}

}
