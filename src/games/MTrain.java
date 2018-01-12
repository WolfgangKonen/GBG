package games;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
//	public double evalM;		// multi train eval score
	public long actionNum;		// number of learning actions (excluding random moves)
	public long trnMoveNum;		// number of train moves (including random moves)
	
	MTrain(int i, int gameNum, double evalQ, double evalT, /*double evalM,*/ 
			long actionNum, long trnMoveNum) {
		this.i=i;
		this.gameNum=gameNum;
		this.evalQ=evalQ;
		this.evalT=evalT;
//		this.evalM=evalM;
		this.actionNum=actionNum;
		this.trnMoveNum=trnMoveNum;
	}
	
	public void print(PrintWriter mtWriter)  {
		String sep = ", ";
		mtWriter.print(i + sep + gameNum + sep);
		mtWriter.println(evalQ + sep + evalT /*+ sep + evalM */
				+ sep + actionNum + sep + trnMoveNum);
	}
	
	/**
	 * Print the results from {@link XArenaFuncs#multiTrain(int n, String, XArenaButtons, GameBoard)} to
	 * file <br>
	 *    {@link Types#GUI_DEFAULT_DIR_AGENT}{@code /<gameName>[/subDir]/csv/multiTrain.csv}. <br>
	 * where the optional {@code subdir} is for games with different flavors (like Hex: board size). 
	 * The directory is created, if it does not exist.   
	 * 
	 * @param mtList	the results from {@code multiTrain}
	 * @param pa		the agent used in {@code multiTrain} 
	 */
	public static void printMultiTrainList(ArrayList<MTrain> mtList, PlayAgent pa, Arena ar){
		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+ar.getGameName();
		String subDir = ar.getGameBoard().getSubDir();
		if (subDir != null){
			strDir += "/"+subDir;
		}
		strDir += "/csv";
		tools.Utils.checkAndCreateFolder(strDir);

		PrintWriter mtWriter = null;
		try {
			mtWriter = new PrintWriter(new FileWriter(strDir+"/"+"multiTrain.csv",false));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		mtWriter.println(pa.stringDescr());		
		mtWriter.println(pa.stringDescr2());
		
//		mtWriter.println("run, gameNum, evalQ, evalT, evalM, actionNum, trnMoves");
		mtWriter.println("run, gameNum, evalQ, evalT, actionNum, trnMoves");
		ListIterator<MTrain> iter = mtList.listIterator();		
		while(iter.hasNext()) {
			(iter.next()).print(mtWriter);
		}

	    mtWriter.close();
	}

}

