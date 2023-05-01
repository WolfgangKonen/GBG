package games.ZweiTausendAchtundVierzig;

import controllers.MC.MCAgentN;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.PlayAgent;
import games.Arena;
import games.EvalResult;
import tools.Types;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class collects 2048 evaluation results in one object. Such objects may be combined in
 * {@code ArrayList<EResult>} from several runs, for later inspection and printout
 */
public class EResult2048 extends EvalResult {
    public PlayAgent m_PlayAgent;   // reference to the evaluated agent
    public int nPly;                // nPly
    public int numEval;             // number of evaluation games (episodes) for this nPly
    public double lowScore;         // lowest score
    public double higScore;         // highest score
    public double avgScore;         // average score
    public double medScore;         // median score
    public double stdDevScore;      // standard deviation of the scores
    public long avgGameDur;         // average game duration in ms
    public double avgMovPerGame;    // average number of moves per game
    public double movesPerSec;      // average number of moves second
    public double t16384_Perc;      // percentage of evaluation runs where tile 16384 is highest tile
    public TreeMap<Integer, Integer> tiles;
    String sep = ", ";

    public EResult2048(PlayAgent m_PlayAgent, int nPly, int numEval, double lowScore, double higScore, double avgScore, double medScore,
                       double stdDevScore, long avgGameDur, double avgMovPerGame, double movesPerSec, TreeMap<Integer, Integer> tiles) {
        this.m_PlayAgent = m_PlayAgent;
        this.nPly = nPly;
        this.numEval = numEval;
        this.lowScore = lowScore;
        this.higScore = higScore;
        this.avgScore = avgScore;
        this.medScore = medScore;
        this.stdDevScore = stdDevScore;
        this.avgGameDur = avgGameDur;
        this.avgMovPerGame = avgMovPerGame;
        this.movesPerSec = movesPerSec;
        this.tiles = tiles;
        this.t16384_Perc = 0.0;

        for (Map.Entry<Integer, Integer> tile : tiles.entrySet()) {
            if (tile.getKey() == 16384) {
                t16384_Perc = tile.getValue().doubleValue() / ConfigEvaluator.NUMBEREVALUATIONS;
            }
        }
    }

    public EResult2048(PlayAgent m_PlayAgent, int nPly, int numEval, double lowScore, double higScore, double avgScore, double medScore,
                       double stdDevScore, long avgGameDur, double avgMovPerGame, double movesPerSec, TreeMap<Integer, Integer> tiles,
                       double result, boolean success, String msg, int mode, double thresh) {
        // initialize superclass:
        this.result = result;
        this.success = success;
        this.msg = msg;
        this.mode = mode;
        this.thresh = thresh;

        this.m_PlayAgent = m_PlayAgent;
        this.nPly = nPly;
        this.numEval = numEval;
        this.lowScore = lowScore;
        this.higScore = higScore;
        this.avgScore = avgScore;
        this.medScore = medScore;
        this.stdDevScore = stdDevScore;
        this.avgGameDur = avgGameDur;
        this.avgMovPerGame = avgMovPerGame;
        this.movesPerSec = movesPerSec;
        this.tiles = tiles;
        this.t16384_Perc = 0.0;

        for (Map.Entry<Integer, Integer> tile : tiles.entrySet()) {
            if (tile.getKey() == 16384) {
                t16384_Perc = tile.getValue().doubleValue() / ConfigEvaluator.NUMBEREVALUATIONS;
            }
        }
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public String getReport() {
        StringBuilder tilesString = new StringBuilder();
        DecimalFormat frm = new DecimalFormat("00000");
        DecimalFormat frm1 = new DecimalFormat("000");
        DecimalFormat frm2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);
        frm2.applyPattern("#0.0%");

        for (Map.Entry<Integer, Integer> tile : tiles.entrySet()) {
            tilesString.append("\n").append(frm.format(tile.getKey().longValue()))
                    .append(", ").append(frm1.format(tile.getValue().longValue()))
                    .append("  (").append(frm2.format(tile.getValue().doubleValue() / ConfigEvaluator.NUMBEREVALUATIONS)).append(")");
        }

        String agentSettings = "";

        if(m_PlayAgent.getName().equals("MC-N")) {
            MCAgentN mcAgent = (MCAgentN)m_PlayAgent;
            agentSettings = "\nROLLOUTDEPTH: " + mcAgent.getParMC().getRolloutDepth() +
                    "\nITERATIONS: " + mcAgent.getParMC().getNumIter() +
                    "\nNUMBERAGENTS: " + mcAgent.getParMC().getNumAgents();
        } else if(m_PlayAgent.getName().equals("MCTS Expectimax")) {
            MCTSExpectimaxAgt mctsExpectimaxAgt = (MCTSExpectimaxAgt) m_PlayAgent;
            agentSettings = "\nROLLOUTDEPTH: " + mctsExpectimaxAgt.params.getRolloutDepth() +
                    "\nITERATIONS: " + mctsExpectimaxAgt.params.getNumIter() +
                    "\nMAXNODES:" + mctsExpectimaxAgt.params.getMaxNodes();
        }

        return "\n\nSettings:" +
                "\nAgent Name: " + m_PlayAgent.getName() +
                agentSettings +
                "\nNumber of games: " + ConfigEvaluator.NUMBEREVALUATIONS +
                "\n" +
                "\nResults:" +
                "\nLowest score is: " + lowScore +
                "\nAverage score is: " + Math.round(avgScore) + " +- " + Math.round(stdDevScore) +
                "\nMedian score is: " + Math.round(medScore) +
                "\nHighest score is: " + higScore +
                "\nAverage game duration: " +  avgGameDur + "ms" +
                "\nDuration of evaluation: " + Math.round(avgGameDur*numEval) + "s" +
                "\nAverage moves per game: " +  avgMovPerGame +
                "\nMoves per second: " + movesPerSec +
                "\n" +
                "\nHighest tiles: " +
                tilesString +
                "\n\n";
    }

    public void print(PrintWriter mtWriter) {
        DecimalFormat frm0 = new DecimalFormat("#0000");
        DecimalFormat df, df2;
        df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);
        df.applyPattern("#0000.0");  // now numbers formatted by df  appear with a decimal *point*
        df2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);
        df2.applyPattern("#0.000");  // now numbers formatted by df2 appear with a decimal *point*

        mtWriter.print(nPly + sep + numEval + sep);
        mtWriter.println(frm0.format(lowScore) + sep + frm0.format(avgScore) + sep + frm0.format(stdDevScore)
                + sep + frm0.format(medScore) + sep + frm0.format(higScore) + sep + avgGameDur
                + sep + df.format(avgMovPerGame) + sep + frm0.format(movesPerSec) + sep + df2.format(t16384_Perc));
    }

    public void printEResultList(String csvName, ArrayList<EResult2048> erList, PlayAgent pa, Arena ar,
                                 String userTitle1, String userTitle2) {
        PrintWriter erWriter = null;
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
                erWriter = new PrintWriter(new FileWriter(strDir + "/" + csvName, false));
                retry = false;
            } catch (IOException e) {
                try {
                    // We may get here if file csvName is open in another application (e.g. Excel).
                    // Here we give the user the chance to close the file in the other application:
                    System.out.print("*** Warning *** Could not open " + strDir + "/" + csvName + ". Retry? (y/n): ");
                    String s = bufIn.readLine();
                    retry = (s.contains("y"));
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }

        if (erWriter != null) {
            erWriter.println(pa.stringDescr());
            erWriter.println(pa.stringDescr2());

            erWriter.println("nPly" + sep + "numEval" + sep + "lowScore" + sep + "avgScore" + sep + "stdDevScore" + sep
                    + "medScore" + sep + "higScore" + sep
                    + "avgGameDur" + sep + "avgMovPerGame" + sep + "movesPerSec" + sep + "tile16kPerc");
            for (EResult2048 result : erList) {
                result.print(erWriter);
            }

            erWriter.close();
        } else {
            System.out.print("*** Warning *** Could not write " + strDir + "/" + csvName + ".");
        }
    }
}
