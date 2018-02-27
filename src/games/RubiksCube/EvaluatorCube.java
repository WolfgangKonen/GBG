package games.RubiksCube;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.JOptionPane;

import agentIO.AgentLoader;
import controllers.MinimaxAgent;
import controllers.PlayAgent;
import controllers.RandomAgent;
import games.ArenaTrain;
import games.Evaluator;
import games.TicTacToe.Evaluator9;
import games.ZweiTausendAchtundVierzig.ConfigEvaluator;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import games.GameBoard;
import games.PStats;
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
	private RandomAgent random_agent = new RandomAgent(sRandom);
	private AgentLoader agtLoader = null;
	private int m_mode;
	private double m_res=-1;		// avg. success against RandomPlayer, best is 0.9 (m_mode=0)
									// or against MinimaxPlayer, best is 0.0 (m_mode=1,2)
	private String m_msg;
	protected double[] m_thresh={0.8,-0.15,-0.15}; // threshold for each value of m_mode
	private GameBoardCube m_gb;
	private int[] Nmax = {0,10,50,300,2000,2000,2000,2000,2000,2000,2000,2000};
	private CSArrayList[] D;		// the array of distance sets
	
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
		long seed = 999;
		Random rand = new Random(seed);
		ArrayList csList = new ArrayList<CStats>();
		ArrayList caggList = new ArrayList<CAggreg>();
		CStats cstats;
		CAggreg cagg;
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
 	                cstats = new CStats(n,p,moveNum,epiLength);
 	    			csList.add(cstats);

 	                if(verbose == 0) {
 	                    System.out.print("Finished game " + n + " with moveNum " + moveNum + " twists.\n");
 	                }
 				} // for (n)
 			} // if
 			cagg = new CAggreg(csList);
 			caggList.add(cagg);
 		} // for (p)
		m_res = avgResCAggregList(caggList);
		m_msg = pa.getName()+": "+getPrintString() + m_res;
		if (this.verbose>0) printCAggregList(caggList);
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

	/**
	 *  TupleInt is just a class to store a tuple of int's with diagnostic information
	 *  about {@code this} (called by constructor CSArrayList(GenerateNext*,...)) <ul>
	 *  <li> <b>n</b> 			the for-loop counter
	 *  <li> <b>setSize</b> 	the size of the set just added
	 *  <li> <b>dSize</b>		the current size of {@code this}
	 *  <li> <b>prevCounter</b>	how many generated states belong to Dprev
	 *  <li> <b>currCounter</b>	how many generated states belong to D
	 *  <li> <b>twinCounter</b>	how many generated states have a twin in Dnext
	 *  </ul>
	 */
	public static class CStats {
		int n;
		int p;
		int moveNum;
		int epiLength;
		
		public CStats(int n, int p, int moveNum, int epiLength) {
			this.n=n;
			this.p=p;
			this.moveNum=moveNum;
			this.epiLength=epiLength;
		}		
	}
	
	public static void printCStatsList(ArrayList<CStats> csList) {
		DecimalFormat form = new DecimalFormat("000");
		Iterator it = csList.iterator();
	    while (it.hasNext()) {
		    CStats tint = (CStats)it.next();
		    System.out.println(form.format(tint.n) + ", " + form.format(tint.p) + ", "+ tint.moveNum 
		    		+ ", epiLength="+tint.epiLength);
        } 		
	}

	public static void printLastCStats(ArrayList<CStats> csList) {
		DecimalFormat form = new DecimalFormat("000");
		CStats tint = csList.get(csList.size()-1);
	    System.out.println(form.format(tint.n) + ", " + form.format(tint.p) + ", "+ tint.moveNum 
	    		+ ", epiLength="+tint.epiLength);
	}

	public static class CAggreg {
		int size;
		int p;
		double percSolved;
		double percLonger;
		double percNotSol;
		
		public CAggreg(ArrayList<CStats> csList) {
			Iterator it = csList.iterator();
			int nSolved=0;
			int nLonger=0;
			int nNot=0;
		    while (it.hasNext()) {
			    CStats cs = (CStats)it.next();
			    this.p = cs.p;
			    if (cs.moveNum==cs.p) nSolved++;
			    if (cs.p<cs.moveNum && cs.moveNum<cs.epiLength) nLonger++;
			    if (cs.moveNum==cs.epiLength) nNot++;
	        } 
		    this.size = csList.size();
			this.percSolved = ((double)nSolved)/size;
			this.percLonger = ((double)nLonger)/size;
			this.percNotSol = ((double)nNot)/size;
		}		
	}
	
	public static void printCAggregList(ArrayList<CAggreg> csList) {
		DecimalFormat form = new DecimalFormat("000");
		DecimalFormat fper = new DecimalFormat("0.0%"); 
		Iterator it = csList.iterator();
	    while (it.hasNext()) {
			CAggreg tint = (CAggreg)it.next();
		    System.out.println(form.format(tint.p) + ", " + form.format(tint.size) + ": "
		    		+ fper.format(tint.percSolved) + ", "
		    		+ fper.format(tint.percLonger) + ", "
		    		+ fper.format(tint.percNotSol) );
        } 		
	}
	public static double avgResCAggregList(ArrayList<CAggreg> csList) {
		Iterator it = csList.iterator();
		double res=0;
	    while (it.hasNext()) {
			CAggreg tint = (CAggreg)it.next();
			res += tint.percSolved;
        } 		
		return res/csList.size();
	}


}
