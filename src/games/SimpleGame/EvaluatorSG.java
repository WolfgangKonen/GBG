package games.SimpleGame;

import controllers.PlayAgent;
import games.EvalResult;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import tools.Types;

/**
 * Evaluator for SimpleGame:
 * <ul>
 * <li> if mode=0: average payoff from NUMEPISODES episodes (optimum: 5.87654) or
 * <li> if mode=1: percentage of optimal decisions from NUMEPISODES episodes (optimum: 1.0)
 * </ul>
 * The value of mode is set in the constructor.
 */
public class EvaluatorSG extends Evaluator {
	private final int NUMEPISODES=10000;
	protected double[] m_thresh={5.0, 0.9}; // threshold for each value of m_mode

	public EvaluatorSG(PlayAgent e_PlayAgent, GameBoard gb, int mode, int verbose) {
		super(e_PlayAgent, gb, mode, verbose);
		//initEvaluator(gb);
	}
	
//	private void initEvaluator(GameBoard gb) { 	}
	

	/**
	 * Evaluate {@code playAgent}. Side effects: write results to {@link Evaluator#lastResult} and 
	 * {@link Evaluator#m_msg}.
	 * 
	 * @return true if evaluateAgentX is above m_thresh.<br>
	 * The choice for X=1 or 2 is made with 3rd parameter mode in 
	 * {@link #EvaluatorSG(PlayAgent, GameBoard, int, int)} [default mode=1].<p>
	 * 
	 * If mode==0, then m_thresh=0.8 (best: 0.9, worst: 0.0) <br>
	 * If mode==1 or 2, then m_thresh=-0.15 (best: 0.0, worst: -1.0)
	 */
	@Override
	public EvalResult evalAgent(PlayAgent playAgent) {

		m_PlayAgent = playAgent;
		switch(m_mode) {
		case -1: 
			m_msg = "no evaluation done ";
			lastResult = Double.NaN;
			return new EvalResult(lastResult, true, m_msg, m_mode, Double.NaN);
		case 0:  return evaluateAgent0(m_PlayAgent,m_gb,m_thresh[0]);
		case 1:  return evaluateAgent1(m_PlayAgent,m_gb,m_thresh[1]);
		default: throw new RuntimeException("Invalid m_mode = "+m_mode);
		}
	}
	
 	/**	
 	 * Calculate average reward from {@link #NUMEPISODES} episodes. Optimum is 5.87654.
 	 * @param pa		agent to evaluate
	 * @param gb		needed to get a default start state (competeBoth)
 	 * @return average reward
	 */
 	private EvalResult evaluateAgent0(PlayAgent pa, GameBoard gb, double thresh) {
 		double avgReward=0.0;
 		for (int i=0; i<NUMEPISODES; i++) {
			StateObservation so = gb.getDefaultStartState(null);
			Types.ACTIONS_VT actBest = pa.getNextAction2(so.partialState(), false, true);
			so.advance(actBest, null);
			avgReward += so.getGameScore(so.getPlayer());
		}
		lastResult = avgReward/NUMEPISODES;
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
	}

	/**
	 * Calculate percent correct decisions from {@link #NUMEPISODES} episodes. Optimum is 1.
	 * @param pa		agent to evaluate
	 * @param gb		needed to get a default start state (competeBoth)
	 * @return percent correct decisions
	 */
	private EvalResult evaluateAgent1(PlayAgent pa, GameBoard gb, double thresh) {
		double percCorrect=0.0;
		for (int i=0; i<NUMEPISODES; i++) {
			StateObserverSG so = (StateObserverSG) gb.getDefaultStartState(null);
			Types.ACTIONS_VT actBest = pa.getNextAction2(so.partialState(), false, true);
			if (so.get_sum() < 4)
				if (actBest.toInt() == 0) {
					percCorrect += 1;        // correct HIT
				}
			if (so.get_sum()>=4)
				if (actBest.toInt()==1) {
					percCorrect += 1;	// correct STAND
				}
		}
		lastResult = percCorrect/NUMEPISODES;
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
	}


 	@Override
 	public int[] getAvailableModes() {
 		return new int[]{-1,0,1};
 	}
 	
 	//@Override
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
			case 0 -> "average reward, best is 5.87654: ";
			case 1 -> "perc correct decisions, best is 1.0: ";
			default -> null;
		};
	}
	
	/**
	 * Derived classes return a tooltip string with this method. Use 
	 * <pre>
	 *    "&lt;html&gt; ... &lt;br&gt; ... &lt;/html&gt;"  </pre>
	 * to get multi-line tooltip text 
	 * @return the tooltip string for evaluator boxes in "Other pars"
	 */
	// Use "<html> ... <br> ... </html>" to get multi-line tooltip text
	@Override
	public String getTooltipString() {
		return "<html>-1: none<br>"
				+ "0: average reward, best is 5.87654<br>"
				+ "1: perc correct decisions, best is 1.0<br>"
				+ "</html>";
	}

	@Override
	public String getPlotTitle() {
		return switch (m_mode) {
			case 0 -> "avg reward";
			case 1 -> "perc correct";
			default -> null;
		};
	}
	

}
