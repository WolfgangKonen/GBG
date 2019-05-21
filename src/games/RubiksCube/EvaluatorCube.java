package games.RubiksCube;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import agentIO.AgentLoader;
import controllers.PlayAgent;
import controllers.RandomAgent;
import controllers.TD.ntuple2.TDNTuple2Agt;
import games.ArenaTrain;
import games.Evaluator;
//import games.TicTacToe.Evaluator9;
import games.GameBoard;
import games.PStats;
import games.TStats;
import games.TStats.TAggreg;
import games.XArenaFuncs;
import games.RubiksCube.CSArrayList.TupleInt;
import games.RubiksCube.CubeConfig.StateType;
import tools.MessageBox;
import tools.Types;

/**
 * Evaluator for RubiksCube. For p in {1, ..., {@link CubeConfig#pMax}}: Test with Nmax[p] cube states 
 * randomly picked from test distance set {@link GameBoardCube#getT()}:
 * <ul>
 * <li> If mode=0: how many percent of the states are solved with the minimal amount of twists? 
 * <li> If mode=1: how many percent of the states are solved in an episode not longer than 2*p? 
 * </ul>  
 * The value of mode is set in the constructor. 
 */
public class EvaluatorCube extends Evaluator {
 	private static final int[] AVAILABLE_MODES = new int[]{-1,0,1};
	private Random rand;
//	private int m_mode;			// now in Evaluator
	private CSArrayList[] T;		// the array of distance sets
	private	int countStates=0;

	/**
	 * threshold for each value of m_mode
	 */
	protected double[] m_thresh={0.0,0.85,0.9}; // 
	
	public EvaluatorCube(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
		super(e_PlayAgent, 0, stopEval);
		initEvaluator(gb,0);
	}

	public EvaluatorCube(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode) {
		super(e_PlayAgent, mode, stopEval);
		initEvaluator(gb,mode);
	}

	public EvaluatorCube(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, mode, stopEval, verbose);
		initEvaluator(gb,mode);
	}
	
	private void initEvaluator(GameBoard gb, int mode) {
//		long seed = 999;
//		rand 		= new Random(seed);
	    rand 		= new Random(System.currentTimeMillis());	
		// --- this is now in Evaluator ---
//		if (!isAvailableMode(mode)) 
//			throw new RuntimeException("EvaluatorCube: Value mode = "+mode+" is not allowed!");
//		m_mode = mode;
		if (gb != null) {
			assert (gb instanceof GameBoardCube);	
			T = ((GameBoardCube)gb).getT();			
			//T = ((GameBoardCube)gb).generateDistanceSets(rand);		
			//--- this takes too much time (each time an Evaluator is constructed), so we do it 
			//--- once in GameBoardCube
		}
	}
	
	/**
	 * @return true if evaluateAgentX is above {@link #m_thresh}.
	 * The choice for {@link #m_thresh} is made with 4th parameter mode in 
	 * {@link #EvaluatorCube(PlayAgent, GameBoard, int, int)} [default: mode=0].
	 */
	@Override
	public boolean evalAgent(PlayAgent playAgent) {
		assert (m_thresh.length >= AVAILABLE_MODES.length);
		m_PlayAgent = playAgent;
		switch(m_mode) {
		case -1: 
			m_msg = "no evaluation done ";
			lastResult = Double.NaN;
			return false;
		case 0:  return evaluateAgent0(m_PlayAgent)>m_thresh[0];
		case 1:  return evaluateAgent0(m_PlayAgent)>m_thresh[1];
		default: return false;
		}
	}
	
	/**	
	 * @param pa 
 	 * @return average success on array D of distance sets
	 */
 	private double evaluateAgent0(PlayAgent pa) {
		ArrayList tsList = new ArrayList<TStats>();
		ArrayList taggList = new ArrayList<TAggreg>();
		TStats tstats;
		TAggreg tagg;
 		countStates=0;
		for (int p=1; p<=CubeConfig.pMax; p++) {
			int epiLength = CubeConfig.EVAL_EPILENGTH; //50, 2*p; //(2*p>10) ? 2*p : 10;
			if (T[p]!=null) {
	 			for (int n=0; n<CubeConfig.EvalNmax[p]; n++) {
 	 				int index = rand.nextInt(T[p].size());
 	 				CubeState cS = (CubeState)T[p].get(index);
 	 				cS.clearLast();
 	 				StateObserverCube so = new StateObserverCube(cS);
 	 				so.resetMoveCounter();
 	 				
 	                while (!so.isGameOver() && so.getMoveCounter()<epiLength) {
 	                	// --- the if-branch is just for speedup (definitely no symmetries during eval) ---
 	                	// --- but it is no longer really needed since we abandon symmetries also in training ---
 	                	if (pa instanceof TDNTuple2Agt) {
 	 	                    so.advance(((TDNTuple2Agt) pa).getNextAction2SYM(so, false, true, false)); 	                		
 	                	} else {
 	 	                    so.advance(pa.getNextAction2(so, false, true));
 	                	}
 	                }
 	                int moveNum = so.getMoveCounter();
 	                tstats = new TStats(n,p,moveNum,epiLength);
 	    			tsList.add(tstats);

 	                if(verbose > 1) {
 	                    System.out.print("Finished game " + n + " with moveNum " + moveNum + " twists.\n");
 	                }
 				} // for (n)
				countStates += CubeConfig.EvalNmax[p];
 			} // if
 			tagg = new TAggreg(tsList,p);
 			taggList.add(tagg);
 		} // for (p)
		lastResult = TStats.weightedAvgResTAggregList(taggList, CubeConfig.theoCov, m_mode);
		m_msg = pa.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>=0) {
			TStats.printTAggregList(taggList);
			//System.out.println((CubeConfig.stateCube==StateType.CUBESTATE) ? "CUBESTATE" : "CUBEPLUSACTION");
		}
		return lastResult;
	}

 	// --- implemented by Evaluator ---
// 	@Override
// 	public double getLastResult() { 
// 		return lastResult; 
// 	}
 	
 	// --- implemented by Evaluator ---
// 	@Override
// 	public String getMsg() { 
// 		return m_msg; 
// 	} 
 	
 	// --- implemented by Evaluator ---
//	@Override
// 	public boolean isAvailableMode(int mode) {
//		for (int i : AVAILABLE_MODES) {
//			if (mode==i) return true;
//		}
//		return false;
// 	}
 	
 	@Override
 	public int[] getAvailableModes() {
 		return AVAILABLE_MODES;
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
//	public int getMultiTrainEvalMode() 
//	{
//		return 0;
//	}

	@Override
	public String getPrintString() {
		switch (m_mode) {
		case 0:  return countStates+" cubes: % solved with minimal twists (best is 1.0): ";
		case 1:  return countStates+" cubes: % solved below epiLength="+ CubeConfig.EVAL_EPILENGTH +" (best is 1.0): ";
		default: return null;
		}
	}

	@Override
	public String getTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>-1: none<br>"
				+ "0: % solved with min. twists, best is 1.0<br>"
				+ "1: % solved below epiLength, best is 1.0"
				+ "</html>";
	}

	@Override
	public String getPlotTitle() {
		switch (m_mode) {
		case 0:  return "% solved with minimal twists";
		case 1:  return "% solved below epiLength";
		default: return null;
		}
	}


}
