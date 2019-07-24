package games;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ListIterator;

import controllers.PlayAgent;
import games.XArenaFuncs;
import tools.Types;

/**
 *  Class MTrain holds the results from one iteration (one episode) during multi-training.
 *  When starting multiTrain, an object {@code ArrayList<MTrain> mtList} is created and 
 *  finally written with {@link MTrain#printMultiTrainList(ArrayList, PlayAgent)}
 *  to file {@code multiTrain.csv}.
 *  
 *  @see XArenaFuncs#multiTrain(String, XArenaButtons, GameBoard)
 *  @see games.PStats
 */
class MTrain {
	public int i;				// number of training runs during multiTrain
	public int gameNum;			// number of training games (episodes) during a run
	public double evalQ;		// quick eval score
	public double evalT;		// train eval score
	public long actionNum;		// number of learning actions (excluding random moves)
	public long trnMoveNum;		// number of train moves (including random moves)
	public double elapsedTime=0.0;
	public double movesSecond=0.0;
	public double userValue1=0.0;
	public double userValue2=0.0;
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
	
	MTrain(int i, int gameNum, double evalQ, double evalT, /*double evalM,*/ 
			long actionNum, long trnMoveNum, double elapsedTime, double movesSecond,
			double userValue1, double userValue2) {
		this.i=i;
		this.gameNum=gameNum;
		this.evalQ=evalQ;
		this.evalT=evalT;
		this.actionNum=actionNum;
		this.trnMoveNum=trnMoveNum;
		this.elapsedTime=elapsedTime;
		this.movesSecond=movesSecond;
		this.userValue1=userValue1;
		this.userValue2=userValue2;
	}
	
	public void print(PrintWriter mtWriter)  {
		mtWriter.print(i + sep + gameNum + sep);
		mtWriter.println(evalQ + sep + evalT /*+ sep + evalM */
				+ sep + actionNum + sep + trnMoveNum + sep + elapsedTime + sep + movesSecond
				+ sep + userValue1 + sep + userValue2);
	}
	
	/**
	 * Print the results from {@link XArenaFuncs#multiTrain(int, String, XArenaButtons, GameBoard)} to
	 * file <br>
	 *    {@link Types#GUI_DEFAULT_DIR_AGENT}{@code /<gameName>[/subDir]/csv/multiTrain.csv}. <br>
	 * where the optional {@code subdir} is for games with different flavors (like Hex: board size). 
	 * The directory is created, if it does not exist.   
	 * 
	 * @param mtList	the results from {@code multiTrain}
	 * @param pa		the agent used in {@code multiTrain} 
	 * @param ar		needed for game name and {@code subdir}
	 * @param userTitle1	title of 1st user column
	 * @param userTitle2	title of 2nd user column
	 */
	public static void printMultiTrainList(ArrayList<MTrain> mtList, PlayAgent pa, Arena ar,
			String userTitle1, String userTitle2){
		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+ar.getGameName();
		String subDir = ar.getGameBoard().getSubDir();
		if (subDir != null){
			strDir += "/"+subDir;
		}
		strDir += "/csv";
		tools.Utils.checkAndCreateFolder(strDir);

		boolean retry=true;
		PrintWriter mtWriter = null;
		BufferedReader bufIn=new BufferedReader(new InputStreamReader(System.in));
		while (retry) {
			try {
				mtWriter = new PrintWriter(new FileWriter(strDir+"/"+"multiTrain.csv",false));
				retry = false;
			} catch (IOException e) {
				try {
					// We may get here if multiTrain.csv is open in another application (e.g. Excel).
					// Here we give the user the chance to close the file in the other application:
				    System.out.print("*** Warning *** Could not open "+strDir+"/"+"multiTrain.csv. Retry? (y/n): ");
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
					+"trnMoves"+sep+"elapsedTime"+sep+"movesSecond"+sep+userTitle1+sep+userTitle2);
			ListIterator<MTrain> iter = mtList.listIterator();		
			while(iter.hasNext()) {
				(iter.next()).print(mtWriter);
			}

		    mtWriter.close();
		} else {
			System.out.print("*** Warning *** Could not write "+strDir+"/"+"multiTrain.csv.");
		}
	}

}

