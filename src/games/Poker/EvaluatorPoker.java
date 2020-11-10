package src.games.Poker;

import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;

import java.util.ArrayList;

/**
 * Evaluator for Poker
 */
public class EvaluatorPoker extends Evaluator {

	protected double[] m_thresh={0}; // threshold for each value of m_mode

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
		return false;
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
			case 1 -> "palceholder";
			default -> null;
		};
	}

}
