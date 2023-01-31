package starters;

import controllers.PlayAgent;
import games.Arena;
import games.GameBoard;
import games.XArenaButtons;
import games.XArenaFuncs;
import tools.Types;

import java.io.*;
import java.util.ArrayList;

/**
 * Helper class for {@link GBGBatch} and test {@code SymmTrainTest} to collect training results and store them in CSV file.
 *
 * @see MTrain
 */
public class MCube {
    public int i;                // number of training run during multiTrain
    public String agtFile;
    public int gameNum;            // number of training games (episodes) during a run
    public int nSym;
    public int pMinEval;
    public int pMaxEval;
    public double evalQ;        // quick eval score
    public double evalT;        // train eval score
    public double totalTrainSec;
    public double userValue1;
    public double userValue2;
    String sep = "; ";

    public MCube(int i, String agtFile, int gameNum, int nSym, int pMin, int pMax, double evalQ, double evalT, double totalTrainSec,
                 double userValue1, double userValue2) {
        this.i = i;
        this.agtFile = agtFile;
        this.gameNum = gameNum;
        this.nSym = nSym;
        this.pMinEval = pMin;
        this.pMaxEval = pMax;
        this.evalQ = evalQ;
        this.evalT = evalT;
        this.totalTrainSec = totalTrainSec;
        this.userValue1 = userValue1;
        this.userValue2 = userValue2;
    }

    public void print(PrintWriter mtWriter) {
        mtWriter.print(i + sep + agtFile + sep + gameNum + sep + nSym + sep + pMinEval + sep + pMaxEval + sep);
        mtWriter.println(evalQ + sep + evalT + sep + totalTrainSec
                + sep + userValue1 + sep + userValue2);
    }

    /**
     * Print the results from {@link XArenaFuncs#multiTrain(int, String, XArenaButtons, GameBoard, String)} XArenaFuncs.multiTrain} to
     * file <br>
     *
     * <pre>  {@link Types#GUI_DEFAULT_DIR_AGENT}{@code /<gameName>[/subDir]/csv/<csvName>} </pre>
     * <p>
     * where the optional {@code subdir} is for games with different flavors (like Hex: board size).
     * The directory is created, if it does not exist.
     *
     * @param csvName    where to write results, e.g. "multiTrain.csv"
     * @param mcList     the results from {@code multiTrain}
     * @param pa         the agent used in {@code multiTrain}
     * @param ar         needed for game name and {@code subdir}
     * @param userTitle1 title of 1st user column
     * @param userTitle2 title of 2nd user column
     * @return the filename
     */
    public String printMCubeList(String csvName, ArrayList<MCube> mcList, PlayAgent pa, String agtFile, Arena ar,
                                 String userTitle1, String userTitle2) {
        PrintWriter mcWriter = null;
        String strDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + ar.getGameName();
        String subDir = ar.getGameBoard().getSubDir();
        if (subDir != null) {
            strDir += "/" + subDir;
        }
        strDir += "/csv";
        tools.Utils.checkAndCreateFolder(strDir);

        boolean retry = true;
        BufferedReader bufIn = new BufferedReader(new InputStreamReader(System.in));
        while (retry) {
            try {
                mcWriter = new PrintWriter(new FileWriter(strDir + "/" + csvName, false));
                retry = false;
            } catch (IOException e) {
                try {
                    // We may get here if csvName is open in another application (e.g. Excel).
                    // Here we give the user the chance to close the file in the other application:
                    System.out.print("*** Warning *** Could not open " + strDir + "/" + csvName + ". Retry? (y/n): ");
                    String s = bufIn.readLine();
                    retry = s.contains("y");
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }

        if (mcWriter != null) {
            mcWriter.println(pa.stringDescr());
            mcWriter.println(pa.stringDescr2());
            mcWriter.println("from: " + agtFile);

            mcWriter.println("run" + sep + "agtFile" + sep + "gameNum" + sep + "nSym" + sep + "pMinEval" + sep + "pMaxEval" + sep + "evalQ" + sep + "evalT" + sep
                    + "totalSec" + sep + userTitle1 + sep + userTitle2);
            for (MCube mCube : mcList) {
                mCube.print(mcWriter);
            }

            mcWriter.close();
            return strDir + "/" + csvName;
        } else {
            System.err.print("*** Warning *** Could not write " + strDir + "/" + csvName + ".");
            return null;
        }
    }

}
