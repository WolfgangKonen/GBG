package games;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ListIterator;

import controllers.PlayAgent;
import games.XArenaFuncs;
import tools.Types;

/**
 *  This class holds the results from {@code numEval} episodes during multi-training.
 *  When starting multiTrain, an object {@code ArrayList<MTrain> mtList} is created and 
 *  finally written with {@link MTrain#printMultiTrainList(ArrayList, PlayAgent)}
 *  to file <b>{@code agents/<gameDir>/csv/<csvName>}</b> (usually {@code multiTrain.csv}).
 *  
 *  The columns in {@code multiTrain.csv} are: <br>
 *  <ul>
 *  <li> {@code run}: the number of the training run
 *  <li> {@code gameNum}: episodes trained so far
 *  <li> {@code evalQ}: evaluation result from Quick Evaluator
 *  <li> {@code evalT}: evaluation result from Train Evaluator
 *  <li> {@code actionNum}: cumulated number of learning actions (excluding random moves) in this run
 *  <li> {@code trnMoves}: cumulated number of training moves (including random moves) in this run
 *  <li> {@code totalTrainSec}: time [sec] spent in trainAgent since start of this training run (only self-play, excluding evaluations)
 *  <li> {@code movesSecond}: average number of moves per second since start of this training run (counting only the time spent in trainAgent)
 *  <li> {@code userValue1}: <em>not used</em>
 *  <li> {@code userValue2}: <em>not used</em>
 *  </ul>
 *  Example:
 *  <pre>
 *  run, gameNum, evalQ, evalT, actionNum, trnMoves, totalTrainSec, movesSecond, , 
 *  0, 100, 0.80,1.00,  900,  900, 0.667, 1349.3253373313344, 0.0, 0.0
 *  0, 200, 0.84,0.95, 1800, 1800, 1.294, 1391.035548686244, 0.0, 0.0
 *  0, 300, 0.92,1.00, 2696, 2696, 1.916, 1407.098121085595, 0.0, 0.0
 *  0, 400, 0.99,0.98, 3592, 3592, 2.536, 1416.4037854889589, 0.0, 0.0
 *  </pre>
 *  That is, after 100 episodes we have 900 training moves in 0.667 sec, accounting for 1349 moves/s.<br>
 *  After 200 episodes we have 1800 training moves conducted in 1.294 sec, leading to 1391 moves/s.<br>
 *  After 300 episodes we have 2696 training moves conducted in 1.916 sec, leading to 1407 moves/s.
 *  <p>
 *  If we want the average moves/s during episodes 200 to 300, we can calculate it from 
 *  <pre>
 *      (trnMoves[300] - trnMoves[200]) / (totalTrainSec[300] - totalTrainSec[200]) 
 *   =  (   2696       -    1800      ) / (    1.916          -       1.294       )    =   1440.5 
 *  </pre>
 *    
 *  @see XArenaFuncs#multiTrain(String, XArenaButtons, GameBoard)
 *  @see games.PStats
 */
public class MTrain {
	public int i;				// number of training runs during multiTrain
	public int gameNum;			// number of training games (episodes) during a run
	public double evalQ;		// quick eval score
	public double evalT;		// train eval score
	public long actionNum;		// number of learning actions (excluding random moves)
	public long trnMoveNum;		// number of train moves (including random moves)
	public double totalTrainSec=0.0;
	public double movesSecond=0.0;
	public double userValue1=0.0;
	public double userValue2=0.0;
	//DecimalFormat frm1 = new DecimalFormat("#0.0000");
	static String sep = ", ";
	
	MTrain(int i, int gameNum, double evalQ, double evalT, /*double evalM,*/ 
			long actionNum, long trnMoveNum) {
		this.i=i;
		this.gameNum=gameNum;
		this.evalQ=evalQ;
		this.evalT=evalT;
		this.actionNum=actionNum;
		this.trnMoveNum=trnMoveNum;
	}
	
	public MTrain(int i, int gameNum, double evalQ, double evalT, /*double evalM,*/ 
			long actionNum, long trnMoveNum, double totalTrainSec, double movesSecond,
			double userValue1, double userValue2) {
		this.i=i;
		this.gameNum=gameNum;
		this.evalQ=evalQ;
		this.evalT=evalT;
		this.actionNum=actionNum;
		this.trnMoveNum=trnMoveNum;
		this.totalTrainSec=totalTrainSec;
		this.movesSecond=movesSecond;
		this.userValue1=userValue1;
		this.userValue2=userValue2;
	}
	
	public void print(PrintWriter mtWriter)  {
		mtWriter.print(i + sep + gameNum + sep);
		mtWriter.println(evalQ + sep + evalT /*+ sep + evalM */
				+ sep + actionNum + sep + trnMoveNum + sep + totalTrainSec + sep + movesSecond
				+ sep + userValue1 + sep + userValue2);
	}
	
	/**
	 * Print the results from {@link XArenaFuncs#multiTrain(int, String, XArenaButtons, GameBoard) XArenaFuncs.multiTrain} to
	 * file <br>
	 * 
	 * <pre>  {@link Types#GUI_DEFAULT_DIR_AGENT}{@code /<gameName>[/subDir]/csv/<csvName>} </pre> 
	 * 
	 * where the optional {@code subdir} is for games with different flavors (like Hex: board size). 
	 * The directory is created, if it does not exist.   
	 * 
	 * @param csvName	where to write results, e.g. "multiTrain.csv"
	 * @param mtList	the results from {@code multiTrain}
	 * @param pa		the agent used in {@code multiTrain} 
	 * @param ar		needed for game name and {@code subdir}
	 * @param userTitle1	title of 1st user column
	 * @param userTitle2	title of 2nd user column
	 */
	public static void printMultiTrainList(String csvName, ArrayList<MTrain> mtList, PlayAgent pa, Arena ar,
			String userTitle1, String userTitle2){
		PrintWriter mtWriter = null;
		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+ar.getGameName();
		String subDir = ar.getGameBoard().getSubDir();
		if (subDir != null){
			strDir += "/"+subDir;
		}
		strDir += "/csv";
		tools.Utils.checkAndCreateFolder(strDir);

		boolean retry=true;
		BufferedReader bufIn=new BufferedReader(new InputStreamReader(System.in));
		while (retry) {
			try {
				mtWriter = new PrintWriter(new FileWriter(strDir+"/"+csvName,false));
				retry = false;
			} catch (IOException e) {
				try {
					// We may get here if multiTrain.csv is open in another application (e.g. Excel).
					// Here we give the user the chance to close the file in the other application:
				    System.out.print("*** Warning *** Could not open "+strDir+"/"+csvName+". Retry? (y/n): ");
				    String s = bufIn.readLine();
				    retry = (s.contains("y")) ? true : false;
				} catch (IOException e2) {
					e2.printStackTrace();					
				}
			}			
		}
		
		if (mtWriter!=null) {
			mtWriter.println(pa.stringDescr());		
			mtWriter.println(pa.stringDescr2());
			
			mtWriter.println("run"+sep+"gameNum"+sep+"evalQ"+sep+"evalT"+sep+"actionNum"+sep
					+"trnMoves"+sep+"totalTrainSec"+sep+"movesSecond"+sep+userTitle1+sep+userTitle2);
			ListIterator<MTrain> iter = mtList.listIterator();		
			while(iter.hasNext()) {
				(iter.next()).print(mtWriter);
			}

		    mtWriter.close();
		} else {
			System.out.print("*** Warning *** Could not write "+strDir+"/"+csvName+".");
		}
	}

}

