package controllers.TD.ntuple2;

import controllers.ExpectimaxNWrapper;
import controllers.MaxN2Wrapper;
import controllers.PlayAgent;
import games.*;
import org.junit.Test;
import starters.GBGBatch;
import starters.MTrainSweep;
import starters.SetupGBG;

/**
 * The JUnit tests in this class are not exact, because most agent performances fluctuate a bit due to random variations
 * in both training and evaluation. But the tests really go for subtle details, since they test if the performance of
 * numerous agents for numerous games remains if not the same but at least similar after software changes.
 * The lower thresholds given below are with some safety margin towards the average performance, so the JUnit tests
 * should be passed with high probability (although not with certainty).
 *
 * Running all JUnit test contained in this file will require a decent amount of computation time. So, for quick checks
 * you will usually only comment out some of the lines tagged with time comments.
 */
public class TDNTuple3AgtTest extends GBGBatch {
    String csvFile = "test.csv";

    // settings for RubiksCube with default scalable parameters {"2x2x2","CSTATE","ALL"}, see GBGBatch::setDefaultScaPars
    String[] agtFileA = {"TCL3-p06-1000k-60-7t-TEST.agt.zip","davi3-p06-500k-60-7t.agt.zip"};
                    // lower thresholds for quickEvalTest and ...
    double[][] evalThreshA = {{0.97, 0.99},      // ... TCL3
                              {0.98, 0.99}};     // ... DAVI3
                    // nPly =   0     1
    String[] agtFileB = {"TCL3-p13-3000k-120-7t.agt.zip","davi3-p11-2000k-120-7t-BASE.agt.zip"};  //
                    // lower thresholds for quickEvalTest and ...
    double[][] evalThreshB = {{0.87, 0.88, 0.985},      // ... TCL3
                              {0.85, 0.91, 0.985}};     // ... DAVI3
                    // nPly =   0     1     2

    // settings for Othello
    String[] agtFileC = {"TCL3-fixed6_250k-lam05_P4_nPly2-FAm.agt.zip","TCL3-100_7_250k-lam05_P4_nPly2-FAm.agt.zip"};
                    // lower thresholds for quickEvalTest and ...
    double[][] evalThreshC = {{0.87, 0.92, 0.96},      // ... TCL3 (nPly=2 had 0.98 before)
                              {0.89, 0.94, 0.99}};     // ... TCL3
                    // nPly =   0     1     2

    // settings for 6x6 Hex
    String[] agtFileD = {"TDNT3-TCLid-25_6-300k-eps02.agt.zip","TDNT3-TCLid-25_6-300k-eps02-eval10.agt.zip"};
                    // lower thresholds for quickEvalTest and ...
    double[][] evalThreshD = {{1.00, 1.00, 1.00},      // ... TCL3
                              {0.80, 0.80, 0.95}};     // ... TCL3
                    // nPly =   0     1     2

    // settings for TicTacToe
    String[] agtFileE = {"tdntuple3.agt.zip"};
                    // lower thresholds for quickEvalTest and ...
    double[][] evalThreshE = {{0.00, 0.00, 0.00}};     // ... TCL3
                    // nPly =   0     1     2

    // settings for 2048
    String[] agtFileF = {"TC-NT3_fixed_4_6-Tupels_200k-FA.agt.zip"};
                    // lower thresholds for quickEvalTest and ...
    double[][] evalThreshF = {{130000, 130000, 180000}};      // ... TCL3
                    // nPly =   0        1        2

    /**
     * Test that the agents loaded from agtFile*[k] achieve a certain performance when quick-evaluated with nPly =
     * 0,1,...,nplyMax. The performance is measured by comparing the QuickEval result with evalThresh*[k][nPly].
     * Repeat each eval nRuns times. (* = A, B, C or D)
     */
    @Test
    public void quickEvalTest() {
        long startTime = System.currentTimeMillis();
        String selectedGame = "RubiksCube";
        quickEval(selectedGame, agtFileA, evalThreshA,1);   // 5 sec
        //quickEval(selectedGame, agtFileB, evalThreshB,1);   // 30 sec
        //quickEval(selectedGame, agtFileB, evalThreshB,2);   // 3 min
        selectedGame = "Othello";
        //quickEval(selectedGame, agtFileC, evalThreshC,1);   // 1 min
        //quickEval(selectedGame, agtFileC, evalThreshC,2);   // 8 min
        selectedGame = "Hex";
        //quickEval(selectedGame, agtFileD, evalThreshD,2);   // 5 min
        selectedGame = "2048";
        //quickEval(selectedGame, agtFileF, evalThreshF,2);   // 2.5 min

        double elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("quickEvalTest finished in "+elapsedTime+" sec.");
    }

    /**
     * Test that the agents loaded from agtFile*[k] and trained anew achieve a certain performance when quick-evaluated
     * with a) nPly=0 and b) nPly=1. The performance is measured by comparing the QuickEval result with evalThresh*[k][nPly].
     * Repeat each eval nRuns times. (* = A, B, C, D)
     */
    @Test
    public void retrainAndEvalTest() {
        String selectedGame = "RubiksCube";
        retrainAndEval(selectedGame, agtFileA, evalThreshA, 500000);          // 2 min runtime
        //retrainAndEval(selectedGame, agtFileB, evalThreshB,-1);        // 23 min runtime
        selectedGame = "Hex";
        //retrainAndEval(selectedGame, agtFileD, evalThreshD,-1);        // 14 min runtime
        selectedGame = "TicTacToe";
        //retrainAndEval(selectedGame, agtFileE, evalThreshE,-1);        // 4 sec runtime
    }

    /**
     *
     * @param selectedGame  name of the game
     * @param agtFile       vector of agent files
     * @param evalThresh    eval threshold for each agent file and nPly
     * @param nplyMax       tests for 0,1,...,nplyMax
     */
    public void quickEval(String selectedGame, String[] agtFile, double[][] evalThresh, int nplyMax) {
        PlayAgent pa;

        String[] scaPar = SetupGBG.setDefaultScaPars(selectedGame);
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);
        GameBoard gb = arenaTrain.makeGameBoard();		// needed for chooseStartState()

        assert evalThresh[0].length>nplyMax : "evalThresh[0] too small for nplyMax = "+nplyMax;

        for (int k=0; k< agtFile.length; k++) {
            setupPaths(agtFile[k],csvFile);     // builds filePath

            boolean res = arenaTrain.loadAgent(0, filePath);
            assert res : "\n[TDNTuple3AgtTest] Aborted: agtFile = "+ agtFile[k] + " not found!";

            String sAgent = arenaTrain.m_xab.getSelectedAgent(0);
            pa = arenaTrain.m_xfun.fetchAgent(0,sAgent, arenaTrain.m_xab);

            innerQuickEval(pa, k, nplyMax, agtFile, evalThresh, gb, 0);

            System.out.println("[quickEvalTest,"+agtFile[k]+"] all eval runs above evalThresh");
        } // for (k)
    }

    /**
     *
     * @param selectedGame  name of the game
     * @param agtFile       vector of agent files
     * @param evalThresh    eval threshold for each agent file and nPly
     * @param maxGameNum    number of episodes to train (if -1, infer the this number from m_xab.getGameNumber())
     */
    public void retrainAndEval(String selectedGame, String[] agtFile, double[][] evalThresh, int maxGameNum) {
        PlayAgent pa;
        int nplyMax=1;

        String[] scaPar = SetupGBG.setDefaultScaPars(selectedGame);
        if (selectedGame.equals("RubiksCube")) scaPar = new String[]{"2x2x2","CSTATE","ALL"};
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);
        GameBoard gb = arenaTrain.makeGameBoard();		// needed for chooseStartState()

        MTrainSweep mTrainSweep = new MTrainSweep();

        for (int k=0; k< agtFile.length; k++) {
            setupPaths(agtFile[k],csvFile);     // builds filePath

            boolean res = arenaTrain.loadAgent(0, filePath);
            assert res : "\n[TDNTuple3AgtTest] Aborted: agtFile = "+agtFile[k] + " not found!";

            if (maxGameNum==-1) maxGameNum= arenaTrain.m_xab.getGameNumber();
            arenaTrain.m_xab.oPar[0].setNumEval(maxGameNum/2);
            String sAgent = arenaTrain.m_xab.getSelectedAgent(0);
            pa = arenaTrain.m_xfun.fetchAgent(0,sAgent, arenaTrain.m_xab);

            pa = mTrainSweep.doSingleTraining(0,0,pa, arenaTrain, arenaTrain.m_xab,gb,maxGameNum,0.0,0.0);

            innerQuickEval(pa,k,nplyMax, agtFile, evalThresh, gb, -1);

            System.out.println("[retrainAndEvalTest] "+agtFile[k]+": all eval runs above evalThresh");
        } // for (k)
    }

    private void innerQuickEval(PlayAgent pa,int k, int nplyMax, String[] agtFile, double[][] evalThresh,
                                GameBoard gb, int verbose) {
        PlayAgent qa;
        Evaluator m_evaluatorQ;
        double evalQ;
        int nRuns=2;
        int stopEval=50;
        StateObservation so = gb.getDefaultStartState();

        for (int nply=0; nply<=nplyMax; nply++) {
            if (nply==0) qa = pa; // this was necessary before bug fix in MaxN2Wrapper.getNextAction2 (.clearedCopy()
            else                  // commented out). clearedCopy leads to inferior MaxN2Wrapper[nply=0] results (!). But
                                  // now we found that it is still needed for the RubiksCube(2x2x2,CSTATE) test on agent
                                  // "TCL3-p13-3000k-120-7t.agt.zip", see agtFileB
                    qa = so.isDeterministicGame() ?
                            new MaxN2Wrapper(pa, nply, pa.getParOther()) :
                            new ExpectimaxNWrapper(pa, nply);

            int qem = arenaTrain.m_xab.oPar[0].getQuickEvalMode();
            m_evaluatorQ = arenaTrain.m_xab.m_arena.makeEvaluator(pa,gb,stopEval,qem,-1);

            for (int i=0; i<nRuns; i++) {
                m_evaluatorQ.eval(qa);
                evalQ = m_evaluatorQ.getLastResult();
                System.out.println("nply="+nply+", "+i+ ": evalQ="+evalQ+"    "+evalThresh[k][nply]);
                assert evalQ >= evalThresh[k][nply] : "["+agtFile[k]+"] nply="+nply+": did not pass evalThresh test";
            }
        }
    }

}