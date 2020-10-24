package src.games.Poker;

import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;

import java.util.ArrayList;

/**
 * Evaluator for Poker
 */
public class EvaluatorPoker extends Evaluator {

	protected double[] m_thresh={0.8,-0.15,-0.15}; // threshold for each value of m_mode

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
 		return null;
 	}
 	
 	@Override
 	public int getDefaultEvalMode() {
		return 2;
	}
	public int getQuickEvalMode()
	{
		return 2;
	}
	public int getTrainEvalMode()
	{
		return 9;
	}

	@Override
	public String getPrintString() {
		return switch (m_mode) {
			case -1 -> "no evaluation done ";
			case 0 -> "success rate (against randomAgent, best is 0.9): ";
			case 1 -> "success rate (against Max-N, best is 0.0): ";
			case 2 -> "success rate (against Max-N, different starts, best is 0.0): ";
			case 11 -> "success rate (against TDReferee, different starts, best is ?): ";
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
			case 1 -> "success vs Max-N";
			case 2 -> "success vs Max-N, diff.Start";
			case 11 -> "success vs TDReferee";
			default -> null;
		};
	}

}
