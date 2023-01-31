package starters;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.GameBoard;
import tools.Types;

import java.io.*;
import java.util.ArrayList;

/**
 *  This class holds the results from multi-evaluations of MCTSWrapper and writes them to CSV file.
 *  Used from {@link MCubeIterSweep}.
 *  <p>
 *  The columns in the CSV file are: <br>
 *  <ul>
 *  <li> {@code agtFile}: filename of the wrapped file
 *  <li> {@code run}: the number of the competition run
 *  <li> {@code maxDepth}: maximum tree depth MCTS
 *  <li> {@code scaPar0}: '2x2x2' or '3x3x3'
 *  <li> {@code iterMCTS}: iterations in MCTSWrapperAgent
 *  <li> {@code EPS}: parameter ConfigWrapper.EPS
 *  <li> {@code p_MWrap}: whether MCTSWrapperAgent is player 0 or player 1 (not relevant here)
 *  <li> {@code c_puct}: parameter c_puct in MCTSWrapperAgent
 *  <li> {@code winrate}: win rate MCTSWrapperAgent
 *  <li> {@code userValue1}: <em>not used</em>
 *  <li> {@code userValue2}: <em>not used</em>
 *  </ul>
 *
 * @see MCompeteMWrap
 * @see MTrain
 * @see GBGBatch
 */
public class MRubiksMWrap {
    public int i;				// number of trial (if nTrial>1)
    public int maxDepth;
    public String scaPar0;
    public int iterMWrap;	    // how many iterations in MCTSWrapperAgent
    public int p_MWrap;		    // which player is MCTSWrapperAgent
    public double EPS;
    public double c_puct;
    public double winrate;
    public double userValue1;
    public double userValue2;
    public String agtFile;
    static String sep = "; ";

    protected Evaluator m_evaluatorQ = null;
    protected Evaluator m_evaluatorT = null;

    public MRubiksMWrap(int i, String agtFile, int maxDepth, String scaPar0, int iterMWrap, double EPS,
                        int p_MWrap, double c_puct, double winrate,
                        double userValue1, double userValue2) {
        this.i=i;
        this.agtFile=agtFile;
        this.maxDepth =maxDepth;
        this.scaPar0 = scaPar0;
        this.iterMWrap = iterMWrap;
        this.p_MWrap = p_MWrap;
        this.EPS = EPS;
        this.c_puct=c_puct;
        this.winrate=winrate;
        this.userValue1=userValue1;
        this.userValue2=userValue2;
    }

    private void print(PrintWriter mtWriter)  {
        mtWriter.print(agtFile + sep + i + sep + maxDepth + sep);
        mtWriter.println(scaPar0 + sep + iterMWrap + sep + EPS
                + sep + p_MWrap + sep + c_puct + sep + winrate
                + sep + userValue1 + sep + userValue2);
    }

    /**
     * Print the results from {@link MCompeteSweep#multiCompeteOthello(PlayAgent, int, int, Arena, GameBoard, String)}
     * multiCompeteOthello} to file <br>
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
    public static void printMultiEvalList(String csvName, ArrayList<MRubiksMWrap> mtList, PlayAgent pa, Arena ar,
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
                    retry = s.contains("y");
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }

        if (mtWriter!=null) {
            mtWriter.println(pa.stringDescr());
            mtWriter.println(pa.stringDescr2());

            mtWriter.println("agtFile"+sep+"run"+sep+"maxDepth"+sep+"cubeSz"+sep+"iterMWrap"+sep+"EPS"+sep
                    +"p_MWrap"+sep+"c_puct"+sep+"winrate"+sep+userTitle1+sep+userTitle2);
            for (MRubiksMWrap mCompete : mtList) {
                mCompete.print(mtWriter);
            }

            mtWriter.close();
        } else {
            System.out.print("*** Warning *** Could not write "+strDir+"/"+csvName+".");
        }
    }

}

