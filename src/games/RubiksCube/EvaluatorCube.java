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
import games.ArenaTrain;
import games.Evaluator;
import games.TicTacToe.Evaluator9;
import games.GameBoard;
import games.PStats;
import games.TStats;
import games.TStats.TAggreg;
import games.XArenaFuncs;
import games.RubiksCube.CSArrayList.TupleInt;
import tools.MessageBox;
import tools.Types;

/**
 * Same as {@link Evaluator9}, but instead of {@link Evaluator9#evalAgent1(PlayAgent, boolean)} use
 * <ul>
 * <li> if mode=1: {@link EvaluatorCube#evaluateAgent1(PlayAgent,GameBoard)} (competition with MinimaxPlayer and RandomPlayer) or 
 * <li> if mode=2: {@link EvaluatorCube#evaluateAgent2(PlayAgent,GameBoard)} (competition with MinimaxPlayer from different start positions). 
 * </ul>  
 * The value of mode is set in the constructor. Class Evaluator2 works also for featmode==3.
 */
public class EvaluatorCube extends Evaluator {
 	private static final int[] AVAILABLE_MODES = {0};
	private String sRandom = Types.GUI_AGENT_LIST[2];
	private String sMinimax = Types.GUI_AGENT_LIST[1];
	private long seed = 999;
	private Random rand = new Random(seed);
//	private AgentLoader agtLoader = null;
	private int m_mode;
	private double m_res=-1;		// avg. success against RandomPlayer, best is 0.9 (m_mode=0)
									// or against MinimaxPlayer, best is 0.0 (m_mode=1,2)
	private String m_msg;
	protected double[] m_thresh={0.8}; // threshold for each value of m_mode
	private GameBoardCube m_gb;
	private CSArrayList[] D;		// the array of distance sets
		// Nmax: how many states to pick randomly from each distance set D[p]
	private int[] Nmax = {0,10,50,300,2000,2000,2000,2000,2000,2000,2000,2000};
	
	public EvaluatorCube(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
		super(e_PlayAgent, stopEval);
		initEvaluator(gb,1);
	}

	public EvaluatorCube(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode) {
		super(e_PlayAgent, stopEval);
		initEvaluator(gb,mode);
	}

	public EvaluatorCube(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, stopEval, verbose);
		initEvaluator(gb,mode);
	}
	
	private void initEvaluator(GameBoard gb, int mode) {
		if (!isAvailableMode(mode)) 
			throw new RuntimeException("EvaluatorTTT: Value mode = "+mode+" is not allowed!");
		m_mode = mode;
		m_gb = (GameBoardCube)gb;	
		D = m_gb.getD();
	}
	
	/**
	 * 
	 * @return true if evaluateAgentX is above m_thresh.<br>
	 * The choice for X=0 is made with 4th parameter mode in 
	 * {@link #EvaluatorCube(PlayAgent, GameBoard, int, int)} [default mode=0].<p>
	 * 
	 * If mode==0, then m_thresh=0.8 (best: 0.9, worst: 0.0) <br>
	 * If mode==1 or 2, then m_thresh=-0.15 (best: 0.0, worst: -1.0)
	 */
	@Override
	public boolean eval_Agent(PlayAgent playAgent) {
		m_PlayAgent = playAgent;
		switch(m_mode) {
		case 0:  return evaluateAgent0(m_PlayAgent)>m_thresh[0];
		default: return false;
		}
	}
	
	/**	
	 * @param pa 
 	 * @return
	 */
 	private double evaluateAgent0(PlayAgent pa) {
 		int moveNum;
		ArrayList tsList = new ArrayList<TStats>();
		ArrayList taggList = new ArrayList<TAggreg>();
		TStats tstats;
		TAggreg tagg;
		for (int p=1; p<=m_gb.getPMax(); p++) {
			int epiLength = 2*p; //(2*p>10) ? 2*p : 10;
			if (D[p]!=null) {
	 			for (int n=0; n<Nmax[p]; n++) {
 	 				int index = rand.nextInt(D[p].size());
 	 				CubeState cS = (CubeState)D[p].get(index);
 	 				StateObserverCube so = new StateObserverCube(cS);
 	 				
 	                moveNum=0;
 	                while (!so.isGameOver() && moveNum<epiLength) {
 	                    so.advance(m_PlayAgent.getNextAction2(so, false, true));
 	                    moveNum++;
 	                }
 	                tstats = new TStats(n,p,moveNum,epiLength);
 	    			tsList.add(tstats);

 	                if(verbose == 0) {
 	                    System.out.print("Finished game " + n + " with moveNum " + moveNum + " twists.\n");
 	                }
 				} // for (n)
 			} // if
 			tagg = new TAggreg(tsList,p);
 			taggList.add(tagg);
 		} // for (p)
		m_res = TStats.avgResTAggregList(taggList);
		m_msg = pa.getName()+": "+getPrintString() + m_res;
		if (this.verbose>0) TStats.printTAggregList(taggList);
		return m_res;
	}

 	@Override
 	public double getLastResult() { 
 		return m_res; 
 	}
 	@Override
 	public String getMsg() { 
 		return m_msg; 
 	} 
 	
	@Override
 	public boolean isAvailableMode(int mode) {
		for (int i : AVAILABLE_MODES) {
			if (mode==i) return true;
		}
		return false;
 	}
 	
 	@Override
 	public int[] getAvailableModes() {
 		return AVAILABLE_MODES;
 	}
 	
 	//@Override
 	public static int getDefaultEvalMode() {
		return AVAILABLE_MODES[0];		
	}
 	
	public int getQuickEvalMode() 
	{
		return 0;
	}
	public int getTrainEvalMode() 
	{
		return 0;
	}
	public int getMultiTrainEvalMode() 
	{
		return 0;
	}

	@Override
	public String getPrintString() {
		switch (m_mode) {
		case 0:  return "success rate (randomAgent, best is 0.9): ";
		default: return null;
		}
	}

	@Override
	public String getPlotTitle() {
		switch (m_mode) {
		case 0:  return "success against Random";
		default: return null;
		}
	}


}
