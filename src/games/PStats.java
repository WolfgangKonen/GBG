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
import games.Arena;
import tools.Types;

/**
 *  Class PStats holds the results from one move during game play.
 *  When playing one or multiple games, an object {@code ArrayList<PStats> psList} is 
 *  created and finally written with
 *  {@link PStats#printPlayStats(ArrayList, PlayAgent, Arena)} <br>
 *  to file {@code playStats.csv}. <p>
 *  
 *  This class is currently in part specific to game 2048 (it records for example the number of 
 *  empty tiles). This is the reason why it is needed in addition to the information stored
 *  by {@link games.LogManager}. If called in other games, PStats receives just 0 for the 
 *  number of empty tiles.<p>
 *   
 *  Class PStats is just for diagnostic purposes, it has no influence on the game strategy. 
 *  
 *  @see Arena#PlayGame()
 *  @see games.ZweiTausendAchtundVierzig.Evaluator2048#eval_Agent(PlayAgent)
 *  @see games.MTrain
 */
public class PStats {
	public int i;				// episode number (if needed)
	public int moveNum;			// number of moves during a play game episode
	public double gScore;		// game score (cumulative rewards)
	public double nEmptyTile; 	// actual number of empty tiles
	public double cumEmptyTl; 	// cumulative number of empty tiles
//	public double evalM;
//	public long actionNum;		// number of learning actions
//	public long trnMoveNum;		// number of train moves (including random moves)
	
	public PStats(int i, int moveNum, double gScore, double nEmptyTile, double cumEmptyTiles) {
		this.i=i;
		this.moveNum=moveNum;
		this.gScore=gScore;
		this.nEmptyTile=nEmptyTile;
		this.cumEmptyTl=cumEmptyTiles;
//		this.evalM=evalM;
//		this.actionNum=actionNum;
//		this.trnMoveNum=trnMoveNum;
	}
	
	public void print(PrintWriter mtWriter)  {
		String sep = ", ";
		mtWriter.print(i + sep + moveNum + sep);
		mtWriter.println((int)(gScore+0.5) + sep + (int)(nEmptyTile+0.5) + sep + (int)(cumEmptyTl+0.5));
	}
	
	/**
	 * Print the results from playing one or multiple games to
	 * file <br>
	 *    {@link Types#GUI_DEFAULT_DIR_AGENT}{@code /<gameName>[/subDir]/csv/playStats.csv} <br>
	 * where the optional {@code subdir} is for games with different flavors (like Hex: board size). 
	 * The directory of the file is created, if it does not exist.   
	 * 
	 * @param psList	the results from playing the game(s)
	 * @param pa		the agent(s) used when playing a game 
	 * @param ar		needed for accessing {@code gameName} and the (optional) {@code subDir}
	 */
	public static void printPlayStats(ArrayList<PStats> psList, PlayAgent pa, Arena ar){
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
				mtWriter = new PrintWriter(new FileWriter(strDir+"/"+"playStats.csv",false));
				retry = false;
			} catch (IOException e) {
				try {
					// We may get here if multiTrain.csv is open in another application (e.g. Excel).
					// Here we give the user the chance to close the file in the other application:
				    System.out.print("*** Warning *** Could not open "+strDir+"/"+"playStats.csv. Retry? (y/n): ");
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
			
			mtWriter.println("run, moveNum, gameScore, nEmptyTile, cumEmptyTl");
			ListIterator<PStats> iter = psList.listIterator();		
			while(iter.hasNext()) {
				(iter.next()).print(mtWriter);
			}

		    mtWriter.close();			
		} else {
			System.out.print("*** Warning *** Could not write "+strDir+"/"+"playStats.csv.");
		}
	}

}

