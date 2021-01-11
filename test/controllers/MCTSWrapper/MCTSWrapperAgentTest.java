package controllers.MCTSWrapper;

import controllers.MCTS.MCTSAgentT;
import controllers.MCTSWrapper.passStates.GameStateIncludingPass;
import controllers.MCTSWrapper.stateApproximation.Approximator;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.MaxN2Wrapper;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import games.*;
import games.CFour.AlphaBetaAgent;
import games.CFour.GameBoardC4;
import games.CFour.openingBook.BookSum;
import games.Othello.GameBoardOthello;
import games.RubiksCube.CubeConfig;
import games.RubiksCube.EvalCubeParams;
import games.RubiksCube.EvaluatorCube;
import games.RubiksCube.GameBoardCube;
import starters.GBGBatch;
import starters.MCompeteMWrap;
import org.junit.Test;
import params.ParMCTS;
import params.ParOther;
import tools.ScoreTuple;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class tests the performance of MCTSWrapper in various games. There are certain evaluation expectations coded
 * in the test methods and a well-performing GBG software framework should pass these tests. The evaluation thresholds
 * are not strict, there is a low probability in situations with random elements (from training or from game play) that
 * a test might fail.
 */
public class MCTSWrapperAgentTest extends GBGBatch {
    String selectedGame;
    String[] scaPar;
    String[] agtFiles;

    /**
     * Test the performance of MCTSWrapperAgent on RubiksCube: We run 200 evaluations for each p=7,8,9
     * and for MCTSWrapper wrapped around 3x3x3_STICKER2_AT/TCL4-p9-2000k-120-7t.agt.zip with nPly=0.
     * The %-solved rates are reported like in QuickEval.
     * Results are written to console and may be copied to .txt for later inspection.
     * Results (%-solved rates) are tested against certain expectations for EPS=1e-08, c_puct=1.0, see HashMap hm.
     *
     * Note that pMin=7 and pMax=9. This is still far away from God's number = 20 for the 3x3x3 cube. Thus, the
     * evaluation results are not yet very satisfactorily (we get not much better than a 65% solved-rate).
     *
     * Computation time depends on iterMCTSWrap and the number of for-loop-passes in rubiksCubeTest. For a single pass
     * with iterMCTSWrap=1000, EPS=1e-08, maxDepth=50 the time is about 550 sec. But it can last also much longer (up to
     * 1500 sec, if the perc-solved-rate goes down (unsuccessful searches take longer)). So be aware that the total
     * test may take considerable time, depending on the settings.
     */
    @Test
    public void rubiksCube3x3Test() {
        scaPar=new String[]{"3x3x3", "STICKER2", "ALL"};
        agtFiles = new String[]{"TCL4-p9-2000k-120-7t.agt.zip"};
        int[] iterMCTSWrapArr = {0,100,200,500,1000}; //,100,200,300,500,600,800,1000};
        //int[] iterMCTSWrapArr={0};  // only in conjunction with oPar's nPly > 0 (see below)
        HashMap<Integer, Double> hm = new HashMap<>();  // lower bounds of %-solved-rates to expect as a fct of iterMCTSWrap
        hm.put(   0,0.23);                              // Other values: EPS=1e-8, maxDepth=50, c_puct=1.0
        hm.put( 100,0.23);
        hm.put( 200,0.44);
        hm.put( 500,0.65);
        hm.put(1000,0.65);
        int nTrial = 1;
        String csvFile = "mRubiks3x3.csv";

        innerRubiksTest(scaPar,agtFiles,iterMCTSWrapArr,hm,7,9, nTrial, csvFile);
    }

    /**
     * Same as {@link #rubiksCube3x3Test()}, but for 2x2x2 cube.
     * Note that pMin=11, pMax=13.
     * (11 is already God's number for 2x2x2 cube and 'ALL_TWISTS'. So a successful test shows that this cube can be
     * basically solved with MCTSWrapper.)
     */
    @Test
    public void rubiksCube2x2Test() {
        scaPar=new String[]{"2x2x2", "STICKER2", "ALL"};
        agtFiles = new String[]{"TCL4-p13-3000k-60-7t.agt.zip"};
        int[] iterMCTSWrapArr={0,50,100,200,300}; // {0,100,200,500,1000}; //,100,200,300,500,600,800,1000};
        //int[] iterMCTSWrapArr={0};  // only in conjunction with oPar's nPly > 0 (see below)
        HashMap<Integer, Double> hm = new HashMap<>();  // lower bounds of %-solved-rates to expect as a fct of iterMCTSWrap
        hm.put(   0,0.70);                              // Other values: EPS=1e-8, maxDepth=50, c_puct=1.0
        hm.put(  50,0.80);
        hm.put( 100,0.98);
        hm.put( 200,0.99);
        hm.put( 300,0.99);
        hm.put( 500,0.99);
        hm.put(1000,0.99);
        int nTrial = 4;
        String csvFile = "mRubiks2x2.csv";

        innerRubiksTest(scaPar,agtFiles,iterMCTSWrapArr,hm,11,13, nTrial, csvFile);
    }

    /**
     * @param scaPar    scalable parameters
     * @param agtFiles  the agent(s) to wrap
     * @param hm        hash map with expected lower bounds on %-solved-rate as a fct of iterMCTSWrap
     * @param pMin      min number of scrambling twists
     * @param pMax      max number of scrambling twists
     */
    public void innerRubiksTest(String[] scaPar,
                                String[] agtFiles,
                                int[] iterMCTSWrapArr,
                                HashMap<Integer,Double> hm,
                                int pMin, int pMax, int nTrial,
                                String csvFile) {
        long startTime;
        double elapsedTime=0,deltaTime;

        selectedGame = "RubiksCube";
        PlayAgent pa;
        PlayAgent qa;
        double[] epsArr = {1e-8, 0.0, -1e-8}; // {1e-8, 0.0};    // {1e-8};
        double c_puct=1.0;
        String userTitle1 = "user1", userTitle2 = "user2";
        double userValue1=0.0, userValue2=0.0;
        int maxDepth = 50;  // 25, 50, -1
        double percSolved;

        MCompeteMWrap mCompete;
        ArrayList<MCompeteMWrap> mcList = new ArrayList<>();

        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoardCube gb = new GameBoardCube(t_Game);		// needed for chooseStartState()

        for (int run=0; run<nTrial; run++) {
            for (String agtFile : agtFiles) {
                setupPaths(agtFile, csvFile);     // builds filePath
                boolean res = t_Game.loadAgent(0, filePath);
                assert res : "\n[MCTSWrapperAgentTest] Aborted: agtFile = " + agtFile + " not found!";
                System.out.println("*** Running rubiksCubeTest for " + agtFile + " (scaPar={" + scaPar[0] + "," + scaPar[1] + "," + scaPar[2] + "}) ***");

                String sAgent = t_Game.m_xab.getSelectedAgent(0);
                pa = t_Game.m_xfun.fetchAgent(0, sAgent, t_Game.m_xab);
                ParOther oPar = new ParOther();
                oPar.setWrapperNPly(0);     // or >0 together with iterMCTSWrapArr={0}, if testing MaxNWrapper
                pa.setWrapperParams(oPar);

                for (double EPS : epsArr) {
                    ConfigWrapper.EPS = EPS;
                    for (int iterMCTSWrap : iterMCTSWrapArr) {
                        if (iterMCTSWrap == 0) qa = pa;
                        else qa = new MCTSWrapperAgent(iterMCTSWrap, c_puct,
                                new PlayAgentApproximator(pa),
                                "MCTS-Wrapped " + pa.getName(),
                                maxDepth);
                        if (oPar.getWrapperNPly() > 0) {
                            System.out.println("oPar nPly = " + oPar.getWrapperNPly());
                            qa = new MaxN2Wrapper(pa, oPar.getWrapperNPly(), oPar);
                        }

                        startTime = System.currentTimeMillis();

                        EvalCubeParams ecp = new EvalCubeParams(pMin, pMax, 20, CubeConfig.EvalNmax);
                        EvaluatorCube m_eval = new EvaluatorCube(qa, gb, 50, 1, 0, ecp);
                        m_eval.evalAgent(qa);
                        percSolved = m_eval.getLastResult();
                        System.out.println(m_eval.getMsg());
                        if (EPS == 1e-8 && c_puct == 1.0)       // test thresholds currently only available for this setting
                            assert percSolved > hm.get(iterMCTSWrap) :
                                    "Test failed for iterMCTSWrap = " + iterMCTSWrap + ": % solved=" + percSolved;
                        mCompete = new MCompeteMWrap(run, 1, 0, iterMCTSWrap,
                                EPS, 0, c_puct, percSolved,
                                userValue1, userValue2);
                        mcList.add(mCompete);

                        deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
                        elapsedTime += deltaTime;
                        System.out.println("... for EPS=" + EPS + ", iter=" + iterMCTSWrap + ", " + deltaTime + " sec");
                    }

                    // print the full list mcList after finishing each  (iterMCTSWrap)
                    // (overwrites the file written from previous (iterMCTSWrap))
                    MCompeteMWrap.printMultiCompeteList(csvFile, mcList, pa, t_Game, userTitle1, userTitle2);
                } // for (EPS)
            } // for (agtFiles)
        } // for (run)

        System.out.println("[innerRubiksTest] "+elapsedTime+" sec.");
    }

    /**
     * Test the performance of MCTSWrapperAgent on ConnectFour: We run multi-compete episodes
     *     MCTS(iter=10000,treeDepth=40)    vs.    MCTSWrapper[agtFile](iter=iterMWrap, EPS=1e-08)
     *     MCTS(iter=10000,treeDepth=40)    vs.    MCTSWrapper[agtFile](iter=iterMWrap, EPS=0.0)
     * where MCTSWrapper plays 2nd. [If it plays 1st, it will always win, this is uninteresting.]
     * The win rates for agtFile="TCL-EXP-NT3-al37-lam000-6000k-epsfin0.agt.zip" are tested against these expectations:
     *     1) If iterMCTSWrap==0, then winrate[MCTSWrapper] &le; 0.4 (not strict, sometimes only &le; 0.6), for both EPS
     *     2) If iterMCTSWrap &ge; 300, then winrate[MCTSWrapper] &ge; 0.95, for both EPS
     *  Results are written to mCompeteMCTS-vs-MWrap.csv for inspection.
     *  Computation time &lt; 400 sec for 8 iterMWrap values, 2 EPS values and 1 trial with 10 episodes for each
     *  parameter setting.
     */
    @Test
    public void C4_vs_MCTS_Test() {
        long startTime;
        double elapsedTime;

        int numEpisodes=10;
        int nTrial=1;
        int[] iterMCTSWrapArr={0,50,100,200,300,500,600,800,1000};
        double[] epsArr = { 1e-8, 0.0,-1e-8}; // {1e-8, 0.0};    // {1e-8, 0.0, -1e-8};
        String csvFile = "mCompeteMCTS-vs-MWrap.csv";
        startTime = System.currentTimeMillis();

        innerC4Test(numEpisodes,nTrial,iterMCTSWrapArr,epsArr,"MCTS",csvFile);

        elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[C4_vs_MCTS_Test] "+elapsedTime+" sec.");
    }

    /**
     * Test the performance of MCTSWrapperAgent on ConnectFour: We run multi-compete episodes
     * <ul>
     *   <li>  MCTSWrapper[agtFile](iter=iterMWrap, EPS=1e-08)    vs.    AlphaBeta-DL (distant losses)
     *   <li>  MCTSWrapper[agtFile](iter=iterMWrap, EPS=0.0  )    vs.    AlphaBeta-DL (distant losses)
     * </ul>
     * where MCTSWrapper plays 1st. [If it plays 2nd, it will always lose, this is uninteresting.] <br>
     * The win rates for agtFile="TCL-EXP-NT3-al37-lam000-6000k-epsfin0.agt.zip" are tested against these expectations:
     * <ol>
     *  <li>   If iterMCTSWrap==0, then winrate[MCTSWrapper] &le; 0.955 (not strict, sometimes only &le; 0.96), for both EPS
     *  <li>   If iterMCTSWrap &ge; 500, then winrate[MCTSWrapper] &ge; 0.95, for both EPS
     * </ol>
     * Results are written to mCompeteMWrap-vs-ABDL.csv for inspection.
     * Computation time is about 600 sec for 8 iterMWrap values, 2 EPS values and 1 trial with 50 episodes for each
     * parameter setting (we need 50 and not 10 episodes to get statistically sound results).
     */
    @Test
    public void C4_vs_ABDL_Test() {
        long startTime;
        double elapsedTime;
        String csvFile = "mCompeteMWrap-vs-ABDL.csv";

        int numEpisodes=50;
        int nTrial=4;
        int[] iterMCTSWrapArr={0,50,100,200,300,500,600,800,1000};
        double[] epsArr = { 1e-8,0.0,-1e-8}; // {1e-8, 0.0};    // {1e-8, 0.0, -1e-8};
        startTime = System.currentTimeMillis();

        innerC4Test(numEpisodes,nTrial,iterMCTSWrapArr,epsArr,"AlphaBetaDL",csvFile);

        elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[C4_vs_ABDL_Test] "+elapsedTime+" sec.");
    }

    public void innerC4Test(int numEpisodes, int nTrial,
                            int[] iterMCTSWrapArr,
                            double[] epsArr,
                            String opponentName,
                            String csvFile)
    {
        selectedGame = "ConnectFour";
        scaPar = GBGBatch.setDefaultScaPars(selectedGame);
        String[] agtFiles = {"TCL-EXP-NT3-al37-lam000-6000k-epsfin0.agt.zip"};
        PlayAgent pa;
        PlayAgent qa;
        PlayAgent opponent;
        String userTitle1 = "user1", userTitle2 = "user2";
        double userValue1=0.0, userValue2=0.0;
        double winrate;     // the win rate of MCTSWrapperAgent
        //double[] cpuctArr = {1.0}; //{0.2, 0.4, 0.6, 0.8, 1.0, 1.4, 2.0, 4.0, 10.0}; // {1.0};
        double c_puct = 1.0;
        int playerMWrap;

        MCompeteMWrap mCompete;
        ArrayList<MCompeteMWrap> mcList = new ArrayList<>();

        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoardC4 gb = new GameBoardC4(t_Game);		// needed for chooseStartState()
        StateObservation so = gb.getDefaultStartState();

        switch(opponentName) {
            case "MCTS" -> {
                playerMWrap = 1;
                ParMCTS parMCTS = new ParMCTS();
                parMCTS.setNumIter(10000);
                parMCTS.setTreeDepth(40);
                ParOther oPar = new ParOther();
                opponent = new MCTSAgentT("MCTS",so, parMCTS, oPar);
            }
            case "AlphaBetaDL" -> {
                playerMWrap = 0;
                opponent = new AlphaBetaAgent(new BookSum(),1000);	// search for distant losses
                opponent.instantiateAfterLoading();

            }
            default -> throw new RuntimeException("Wrong value for opponentName");
        }

        for (int run=0; run<nTrial; run++) {
            for (String agtFile : agtFiles) {
                setupPaths(agtFile, csvFile);     // builds filePath
                boolean res = t_Game.loadAgent(0, filePath);
                assert res : "\n[MCTSWrapperAgentTest] Aborted: agtFile = " + agtFile + " not found!";

                String sAgent = t_Game.m_xab.getSelectedAgent(0);
                pa = t_Game.m_xfun.fetchAgent(0, sAgent, t_Game.m_xab);

                for (double EPS : epsArr) {
                    ConfigWrapper.EPS = EPS;
                    //numEpisodes = (EPS<0) ? 10 : 1;
                    for (int iterMCTSWrap : iterMCTSWrapArr) {
                        if (iterMCTSWrap == 0) qa = pa;
                        else qa = new MCTSWrapperAgent(iterMCTSWrap, c_puct,
                                new PlayAgentApproximator(pa),
                                "MCTS-Wrapped " + pa.getName(),
                                -1);

                        // *** only a sub-test: How good would AB-DL be in place of MCTSWrap(TCL-EXP)? ***
                        //qa = new AlphaBetaAgent(new BookSum(),1000);	// search for distant losses
                        //qa.instantiateAfterLoading();

                        ScoreTuple sc;
                        PlayAgtVector paVector = new PlayAgtVector(qa, opponent);
                        for (int p_MWrap : new int[]{playerMWrap})       // {0,1}
                        {     // p_MWrap: whether MCTSWrapper is player 0 or player 1
                            sc = XArenaFuncs.competeNPlayer(paVector.shift(p_MWrap), so, numEpisodes, 0, null);
                            winrate = (sc.scTup[p_MWrap] + 1) / 2;
                            System.out.println("(iterMW,EPS,p_MWrap) = (" + iterMCTSWrap + "," + EPS + "," + p_MWrap + "): " +
                                    "  winrate = " + winrate);
                            mCompete = new MCompeteMWrap(run, numEpisodes, 0, iterMCTSWrap,
                                    EPS, p_MWrap, c_puct, winrate,
                                    userValue1, userValue2);
                            mcList.add(mCompete);
                            if (opponent instanceof MCTSAgentT) {
                                if (p_MWrap == 1 && iterMCTSWrap == 0)
                                    assert (winrate <= 1.96) : "winrate is not <=0.6 for iter==0!";
                                if (p_MWrap == 1 && iterMCTSWrap >= 500)
                                    assert (winrate >= 0.05) : "winrate is not >=0.95 for iter>=500!";
                            } else if (opponent instanceof AlphaBetaAgent) {
                                if (p_MWrap == 0 && iterMCTSWrap == 0)
                                    assert (winrate <= 0.999) : "winrate is not <=0.95 for iter==0!";
                                if (p_MWrap == 0 && iterMCTSWrap >= 500)
                                    assert (winrate >= 0.05) : "winrate is not >=0.95 for iter>=500!";
                            }
                        } // for (p_MWrap)

                        // print the full list mcList after finishing each  (p_MCTS)
                        // (overwrites the file written from previous (p_MCTS))
                        MCompeteMWrap.printMultiCompeteList(csvFile, mcList, pa, t_Game, userTitle1, userTitle2);
                    } // for (iterMCTSWrap)
                } // for (EPS)
            } // for (agtFile)
        } // for (run)
    }

    /**
     * Measure whether getVTable2For is faster than getVTableFor.
     * Result: Is 5x faster, but has only negligible impact, because seldom called.
     */
    @Test
    public void getVTable_Test() {
        selectedGame = "Othello";
        scaPar = GBGBatch.setDefaultScaPars(selectedGame);
        t_Game = GBGBatch.setupSelectedGame(selectedGame, scaPar);   // t_Game is ArenaTrain object
        GameBoard gb = new GameBoardOthello(t_Game);        // needed for chooseStartState()
        StateObservation sob = gb.getDefaultStartState();
        MCTSNode mctsNode = new MCTSNode(new GameStateIncludingPass(sob));
        Approximator approximator = new PlayAgentApproximator(new RandomAgent("rand"));
        MCTS mcts = new MCTS(approximator, 1.0, 50);
        MCTSWrapperAgent mwa = new MCTSWrapperAgent(100,1.0,approximator,"",50);
        int ncalls = 100000;
        long startTime;
        double elapsedTime;

        for (int i = 0; i < 100; i++) {
            mcts.search(mctsNode,0);
        }

        startTime = System.currentTimeMillis();
        for (int i = 0; i < ncalls; i++) {
            mwa.getVTable2For(mctsNode);
        }
        elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[getVTable2For] "+elapsedTime+" sec.");

        startTime = System.currentTimeMillis();
        for (int i = 0; i < ncalls; i++) {
            mwa.getVTableFor(mctsNode);
        }
        elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[getVTableFor] "+elapsedTime+" sec.");

    }

}