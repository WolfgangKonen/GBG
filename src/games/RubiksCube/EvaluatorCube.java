package games.RubiksCube;

import java.util.ArrayList;
import controllers.PlayAgent;
import games.EvalResult;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import tools.TStats;
import tools.TStats.TAggreg;

/**
 * Evaluator for RubiksCube. For p = {pMin, ..., pMax}: Test with {@code ecp.EvalNmax}
 * cube states randomly picked via  {@link GameBoardCube#chooseStartState(int) GameBoardCube#chooseStartState(p)}
 * <ul>
 * <li> If mode=0: how many percent of the states are solved within &le; p twists? 
 * <li> If mode=1: how many percent of the states are solved within {@code epiLength} twists? 
 * </ul>  
 * The value of mode is set in the constructor. <br>
 * The value of {@code epiLength} is set from the agent's {@code getParOther().getStopEval()}.<br>
 * The value of {@link CubeConfig#pMax} is set from {@link params.OtherParams} element {@code pMax}.
 */
public class EvaluatorCube extends Evaluator {
 	private static final int[] AVAILABLE_MODES = new int[]{-1,0,1};
//	private int m_mode;			// now in Evaluator
	private	int countStates=0;
	private EvalCubeParams ecp;

	/**
	 * threshold for each value of m_mode
	 */
	protected double[] m_thresh={0.0,0.85,0.9}; 		// thresholds for m_mode=-1,0,1

	public EvaluatorCube(PlayAgent pa, GameBoard gb, int mode, int verbose) {
		super(pa, gb, mode, verbose);
		initEvaluator(gb);			// might change CubeConfig.pMin and .pMax
		ecp = new EvalCubeParams(pa);		// construct with actual CubeConfig.pMax and with epiLength = pa.getParOther().getStopEval()
	}

	public EvaluatorCube(PlayAgent pa, GameBoard gb, int mode, int verbose,
						 EvalCubeParams evalCubePar) {
		super(pa, gb, mode, verbose);
		initEvaluator(gb);			// might change CubeConfig.pMin and .pMax
		this.ecp = new EvalCubeParams(evalCubePar);
	}

	private void initEvaluator(GameBoard gb) {
		if (gb != null) {
			gb.updateParams();							// update 5 params in CubeConfig from agent settings
			//assert (gb instanceof GameBoardCube);
			//((GameBoardCube)gb).getPMax();			// actualize CubeConfig.pMin and .pMax, if GUI present
		}
	}
	
	/**
	 * @return true if evaluateAgentX is above {@link #m_thresh}.
	 * The choice for {@link #m_thresh} is made with 4th parameter mode in 
	 * {@link #EvaluatorCube(PlayAgent, GameBoard, int, int)} [default: mode=0].
	 */
	@Override
	public EvalResult evalAgent(PlayAgent playAgent) {
		assert (m_thresh.length >= AVAILABLE_MODES.length);
		m_PlayAgent = playAgent;
		switch(m_mode) {
		case -1: 
			m_msg = "no evaluation done ";
			lastResult = Double.NaN;
			return new EvalResult(lastResult, true, m_msg, m_mode, Double.NaN);
		case 0:  return evaluateAgent0(m_PlayAgent, m_thresh[1]);
		case 1:  return evaluateAgent0(m_PlayAgent, m_thresh[2]);
		default: throw new RuntimeException("Invalid m_mode = "+m_mode);
		}
	}
	
	/**	
	 * For each p = {@code ecp.pMin} ... {@code ecp.pMax}: Generate {@code ecp.evalNmax} scrambled cubes
	 * via {@link GameBoardCube#chooseStartState(int) GameBoardCube#chooseStartState(p)}. Measure the success rate with
	 * which the agent can solve them: 
	 * <ul>
	 * <li> {@link Evaluator#m_mode m_mode}{@code =0}:  percent solved within &le; p twists
	 * <li> {@link Evaluator#m_mode m_mode}{@code =1}:  percent solved within {@code epiLength} twists
	 * </ul> 
	 * @param pa the agent to evaluate. Use {@code pa}'s {@link PlayAgent#getParOther()#getStopEval()} to infer {@code epiLength}
	 *           ('EpiLength Eval' in OtherParams)
 	 * @return the weighted average success on different sets of scrambled cubes. Currently, constant weights are
	 * 			 hard-wired in source code.
	 */
 	private EvalResult evaluateAgent0(PlayAgent pa, double thresh) {
		ArrayList<TStats> tsList = new ArrayList<>();
		ArrayList<TAggreg> taggList = new ArrayList<>();
		TStats tstats;
		TAggreg tagg;
		StateObservation so;
		double[] constWght = new double[ecp.pMax];  	// weights for each p-level, see weightedAvgResTAggregList
		for (int p=0; p<ecp.pMax; p++) { constWght[p]=1.0; }
		if (ecp.epiLength<=ecp.pMax) {
			System.err.println("WARNING: epiLength="+ecp.epiLength+" has to be larger than pMax="+ecp.pMax+"!");
			System.err.println("         Setting epiLength to "+(ecp.pMax+1));
			ecp.epiLength=ecp.pMax+1;
			// if epiLength were not larger than ecp.pMax, the calculation in TAggreg would go wrong
			// (such that percentages would sum to something >1)
		}

 		countStates=0;
		for (int p=ecp.pMin; p<=ecp.pMax; p++) {
 			for (int n=0; n<ecp.evalNmax; n++) {
				so = ((GameBoardCube) m_gb).chooseStartState(p);	// uses selectByTwist1(p)
 				so.resetMoveCounter();

				pa.resetAgent();			// needed if pa is MCTSWrapperAgent

				while (!so.isGameOver() && so.getMoveCounter()<ecp.epiLength) {
 	                 so.advance(pa.getNextAction2(so.partialState(), false, true), null);
                }
                int moveNum = so.getMoveCounter();
                tstats = new TStats(n,p,moveNum,ecp.epiLength);	// both p and epiLength are later used in TAggreg(tsList,p) to form counters
    			tsList.add(tstats);

                if(verbose > 1) {
                    System.out.print("Finished game " +p+","+ n + " with moveNum " + moveNum + " twists.\n");
                }
			} // for (n)
			countStates += ecp.evalNmax;
 			tagg = new TAggreg(tsList,p);
 			taggList.add(tagg);
 		} // for (p)

		//the distinction between mode==0 and mode==1 happens in TStats.weightedAvgResTAggregList (!):
		lastResult = TStats.weightedAvgResTAggregList(taggList, constWght, m_mode);
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>=0) {
			TStats.printTAggregList(taggList);
			//System.out.println((CubeConfig.boardVecType==BoardVecType.CUBESTATE) ? "CUBESTATE" : "CUBEPLUSACTION");
		}
		return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
	}

 	@Override
 	public int[] getAvailableModes() {
 		return AVAILABLE_MODES;
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
			case 0 -> countStates + " cubes: % solved with minimal twists (best is 1.0): ";
			case 1 -> countStates + " cubes: % solved within epiLength=" + ecp.epiLength + " (best is 1.0): ";
			default -> null;
		};
	}

	@Override
	public String getTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>-1: none<br>"
				+ "0: % solved with min. twists, best is 1.0<br>"
				+ "1: % solved within EpiLength Eval, best is 1.0"
				+ "</html>";
	}

	@Override
	public String getPlotTitle() {
		return switch (m_mode) {
			case 0 -> "% solved with minimal twists";
			case 1 -> "% solved below epiLength";
			default -> null;
		};
	}


}
