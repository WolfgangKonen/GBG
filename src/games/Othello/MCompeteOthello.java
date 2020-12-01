package games.Othello;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import controllers.MCTSWrapper.ConfigWrapper;
import controllers.MCTSWrapper.MCTSWrapperAgent;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import games.Arena;
import games.GameBoard;
import games.Othello.Edax.Edax2;
import games.StateObservation;
import games.XArenaFuncs;
import params.ParEdax;
import tools.ScoreTuple;
import tools.Types;

/**
 *  This class holds the results from episodes during Othello multi-competition.
 *  When starting multiCompeteSweep, an object {@code ArrayList<MCompete> mcList} is created and
 *  finally written with {@link MCompeteOthello#printMultiCompeteList(String, ArrayList, PlayAgent, Arena, String, String)}
 *  to file <b>{@code agents/<gameDir>/csv/<csvName>}</b> (usually {@code multiCompete.csv}).
 *
 *  The columns in {@code multiCompete.csv} are: <br>
 *  <ul>
 *  <li> {@code run}: the number of the competition run
 *  <li> {@code competeNum}: competition episodes
 *  <li> {@code dEdax}: depth parameter Edax2
 *  <li> {@code iterMCTS}: iterations in MCTSWrapperAgent
 *  <li> {@code EPS}: parameter ConfigWrapper.EPS
 *  <li> {@code p_MCTS}: whether MCTSWrapperAgent is player 0 or player 1
 *  <li> {@code c_puct}: parameter c_puct in MCTSWrapperAgent
 *  <li> {@code winrate}: win rate MCTSWrapperAgent
 *  <li> {@code userValue1}: <em>not used</em>
 *  <li> {@code userValue2}: <em>not used</em>
 *  </ul>
 *
 *  @see games.MTrain
 */
public class MCompeteOthello {
    public int i;				// number of training run during multiTrain
    public int competeNum;		// number of competition games (episodes) during a run
    public int dEdax;		// quick eval score
    public int iterMCTS;	// how many iterations in MCTSWrapperAgent
    public int p_MCTS;		// which player is MCTSWrapperAgent
    public double EPS;
    public double c_puct;
    public double winrate;
    public double userValue1;
    public double userValue2;
    static String sep = ", ";

    // --- never used ---
//    MCompete(int i, int competeNum, int dEdax, int iterMCTS, double EPS,
//             int p_MCTS, double c_puct, double winrate) {
//        this.i=i;
//        this.competeNum =competeNum;
//        this.dEdax = dEdax;
//        this.iterMCTS = iterMCTS;
//        this.EPS = EPS;
//        this.p_MCTS=p_MCTS;
//        this.c_puct=c_puct;
//        this.winrate=winrate;
//    }

    public MCompeteOthello(int i, int competeNum, int dEdax, int iterMCTS, double EPS,
                           int p_MCTS, double c_puct, double winrate,
                           double userValue1, double userValue2) {
        this.i=i;
        this.competeNum =competeNum;
        this.dEdax = dEdax;
        this.iterMCTS = iterMCTS;
        this.EPS = EPS;
        this.p_MCTS=p_MCTS;
        this.c_puct=c_puct;
        this.winrate=winrate;
        this.userValue1=userValue1;
        this.userValue2=userValue2;
    }

    public void print(PrintWriter mtWriter)  {
        mtWriter.print(i + sep + competeNum + sep);
        mtWriter.println(dEdax + sep + iterMCTS + sep + EPS
                + sep + p_MCTS + sep + c_puct + sep + winrate
                + sep + userValue1 + sep + userValue2);
    }

    /**
     * Print the results from {@link #multiCompeteSweep(PlayAgent, int, Arena, GameBoard, String) multiCompeteSweep} to
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
    public static void printMultiCompeteList(String csvName, ArrayList<MCompeteOthello> mtList, PlayAgent pa, Arena ar,
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

            mtWriter.println("run"+sep+"competeNum"+sep+"dEdax"+sep+"iterMCTS"+sep+"EPS"+sep
                    +"p_MCTS"+sep+"c_puct"+sep+"winrate"+sep+userTitle1+sep+userTitle2);
            for (MCompeteOthello mCompete : mtList) {
                mCompete.print(mtWriter);
            }

            mtWriter.close();
        } else {
            System.out.print("*** Warning *** Could not write "+strDir+"/"+csvName+".");
        }
    }

    /**
     * Perform multi-competition with MCTSWrapperAgent wrapped around agent {@code pa} against Edax2 with
     * different depth levels. The Edax depth values to sweep are coded in
     * array {@code depthArr} in this method. <br>
     * Write results to file {@code csvName}.
     *
     * @param pa		index of agent to train (usually n=0)
     * @param iterMCTS	alpha final values to sweep over
     * @param gb		the game board, needed for evaluators and start state selection
     * @param csvName	results are written to this filename
     * @return the (last) trained agent
     * <p>
     * Side effect: writes results of multi-training to <b>{@code agents/<gameDir>/csv/<csvName>}</b>.
     * This file has the columns: <br>
     * {@code run, competeNum, dEdax, iterMCTS, EPS, p_MCTS, c_puct, winrate, userValue1, userValue2}. <br>
     * The contents may be visualized with one of the R-scripts found in {@code resources\R_plotTools}.
     */
    public static PlayAgent multiCompeteSweep(PlayAgent pa, int iterMCTS, Arena t_Game,
                                       GameBoard gb, String csvName) {
        int[] depthArr = {1,2,3,4,5,6,7,8,9};
        double[] epsArr = {1e-8, 0.0, -1.0};
        String userTitle1 = "user1", userTitle2 = "user2";
        double userValue1=0.0, userValue2=0.0;
        double c_puct = 1.0;
        double winrate;
        int numEpisodes;

        PlayAgent qa = null;
        ParEdax parEdax = new ParEdax();

        MCompeteOthello mCompete;
        ArrayList<MCompeteOthello> mcList = new ArrayList<>();

        for (int d : depthArr) {
            parEdax.setDepth(d);
            Edax2 edaxAgent = new Edax2("Edax",parEdax);
            System.out.println("*** Starting multiCompete with Edax depth = "+edaxAgent.getParEdax().getDepth()+" ***");

            for (double EPS : epsArr) {
                ConfigWrapper.EPS = EPS;
                numEpisodes = (EPS<0) ? 10 : 1;
                if (iterMCTS == 0) qa = pa;
                else qa = new MCTSWrapperAgent(iterMCTS, c_puct,
                        new PlayAgentApproximator(pa),
                        "MCTS-Wrapped " + pa.getName(),
                        -1);

                StateObservation so = gb.getDefaultStartState();
                ScoreTuple sc;
                PlayAgtVector paVector = new PlayAgtVector(qa, edaxAgent);
                for (int p_MCTS : new int[]{0, 1})
                {     // p_MCTS: whether MCTSWrapper is player 0 or player 1
                    sc = XArenaFuncs.competeNPlayer(paVector.shift(p_MCTS), so, numEpisodes, 0, null);
                    winrate = (sc.scTup[p_MCTS] + 1) / 2;
                    mCompete = new MCompeteOthello(0, numEpisodes, d, iterMCTS,
                            EPS, p_MCTS, c_puct, winrate,
                            userValue1, userValue2);
                    mcList.add(mCompete);
                } // for (p_MCTS)
            } // for (EPS)

            // print the full list mcList after finishing each pair (p_MCTS,EPS)
            // (overwrites the file written from previous (p_MCTS,EPS))
            MCompeteOthello.printMultiCompeteList(csvName,mcList, pa, t_Game, userTitle1, userTitle2);
        } // for (d)

        return qa;
    } // multiCompeteSweep

}

