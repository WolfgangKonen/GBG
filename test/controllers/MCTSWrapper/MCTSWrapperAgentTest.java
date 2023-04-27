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
import params.ParWrapper;
import starters.GBGBatch;
import starters.MCompeteMWrap;
import org.junit.Test;
import params.ParMCTS;
import params.ParOther;
import starters.SetupGBG;
import tools.ScoreTuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

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
     * --- Deprecated, better use {@link GBGBatch#batch10(int, Properties, String[], String, String, XArenaButtons, GameBoard, String) batch10}
     *     from {@link GBGBatch} ---
     * <p>
     * Test the performance of MCTSWrapperAgent on RubiksCube: We run 200 evaluations for each p=1,2,...,9
     * and for MCTSWrapper wrapped around 3x3x3_STICKER2_AT/TCL4-p9-2000k-120-7t.agt.zip with nPly=0.
     * The %-solved rates are reported like in QuickEval.
     * Results are written to console and to {@code csvFile}.
     * Results (%-solved rates) for EPS=1e-08, c_puct=1.0 are tested against certain expectations, see {@code HashMap hm}.
     * <p>
     * Note that pMax=9 (HTM) or 13 (QTM). This is still far away from God's number = 20 (HTM) or 26 (QTM) for the 3x3x3 cube.
     * The evaluation results are not yet very satisfactorily (we get not much better than a 65% solved-rate).
     * <p>
     * Computation time depends on iterMCTSWrap and the number of for-loop-passes in {@code innerRubiksCubeTest}. For a
     * single pass with iterMCTSWrap=1000, EPS=1e-08, maxDepth=50 the time is about 550 sec. But it can last also much
     * longer (up to 1500 sec, if the perc-solved-rate goes down (unsuccessful searches take longer)). So be aware that
     * the total test may take considerable time, depending on the settings.
     */
    @Test
    @Deprecated
    public void rubiksCube3x3Test() {
        int pMin=1, pMax;
        int nruns = 4;
        String csvFile = "mRubiks3x3.csv";
        scaPar=new String[]{"3x3x3", "STICKER2", "QTM"};    // select here between "HTM" and "QTM"
        switch (scaPar[2]) {
            case "HTM" -> {
                //agtFiles = new String[]{"TCL4-p20-5000k-120-7t.agt.zip"};
                //agtFiles = new String[]{"TCL4-p9-2000k-120-7t.agt.zip"};                    // older setting Jan-2022
                agtFiles = new String[]{"multiTrain/TCL4-p9-ET13-3000k-120-7t_00.agt.zip"};   // newer setting Aug-2022
                pMax = 9;
            }
            case "QTM" -> {
                //agtFiles = new String[]{"TCL4-p13-3000k-120-7t.agt.zip"};                   // older setting Jan-2022
                //agtFiles = new String[]{"multiTrain/TCL4-p13-ET16-3000k-120-7t_00.agt.zip"};  // newer setting Aug-2022
                agtFiles = new String[]{"multiEval/TCL4-p13-ET16-3000k-120-7t_02.agt.zip"};  // Aug-2022, from lwivs48
                pMax = 13;
            }
            default -> throw new RuntimeException("Unallowed value " + scaPar[2] + " for scaPar[2]");
        }
        int[] iterMCTSWrapArr = {0,100,800};//{0,100,200,500,1000}; //{20,50}; //{200,500}; //,100,200,300,500,600,800,1000};
        //int[] iterMCTSWrapArr={10000};  // only in conjunction with oPar's nPly > 0 (see below)
        int fact=0;   // 1 or 0: whether to invoke lower bounds (1) or not (0)
        HashMap<Integer, Double> hm = new HashMap<>();  // lower bounds of %-solved-rates to expect as a fct of iterMCTSWrap
        hm.put(   0,fact*0.23);                         // Other values: EPS=1e-8, maxDepth=50, c_puct=1.0
        hm.put( 100,fact*0.23);
        hm.put( 200,fact*0.44);
        hm.put( 500,fact*0.65);
        hm.put( 800,fact*0.65);
        hm.put(1000,fact*0.65);

        selectedGame = "RubiksCube";
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);

//      innerRubiksTest(scaPar,agtFiles,iterMCTSWrapArr,hm,1,9, nruns, csvFile); // for Rubiks-both-cubes-ptwist.pdf
        innerRubiksTest(scaPar,agtFiles,iterMCTSWrapArr,hm,pMin,pMax, nruns, csvFile); // for Rubiks-both-cubes-iter.pdf
    }

    /**
     * --- Deprecated, better use {@link GBGBatch#batch10(int, Properties, String[], String, String, XArenaButtons, GameBoard, String) batch10}
     *     from {@link GBGBatch} ---
     * <p>
     * Same as {@link #rubiksCube3x3Test()}, but for 2x2x2 cube. <br>
     * Note that pMax=13 for HTM and pMax=16 for QTM. <br>
     * (Since for the 2x2x2 cube God's number is 11 in the HTM- and 14 in the QTM-case, a successful test shows that MCTSWrapper
     * solves the 2x2x2 cube.)
     * <p>
     * Computation time depends on iterMCTSWrap and the number of for-loop-passes in {@code innerRubiksCubeTest}.
     * Example for iterMCTSWrapArr={0,50,100,200,300}, nruns=4, epsArr = {1e-8, 0.0, -1e-8}, one agtFile:
     * 60 for-loop-passes, in total 2700 sec.
     */
    @Test
    @Deprecated
    public void rubiksCube2x2Test() {
        int pMin=1, pMax;
        int nruns = 4;
        String csvFile = "mRubiks2x2.csv";
        scaPar=new String[]{"2x2x2", "STICKER2", "QTM"};    // select here between "HTM" and "QTM"
        switch (scaPar[2]) {
            case "HTM" -> {
                //agtFiles = new String[]{"TCL4-p13-3000k-60-7t.agt.zip"};                    // older setting Jan-2022
                agtFiles = new String[]{"multiTrain/TCL4-p13-ET16-3000k-60-7t_00.agt.zip"};   // newer setting Aug-2022
                pMax = 13;
            }
            case "QTM" -> {
                //agtFiles = new String[]{"TCL4-p16-3000k-60-7t.agt.zip"};
                //agtFiles = new String[]{"TCL4-p16-3000k-60-7t-lam05.agt.zip"};              // older setting Jan-2022
                agtFiles = new String[]{"multiTrain/TCL4-p16-ET20-3000k-60-7t_00.agt.zip"};   // newer setting Aug-2022
                pMax = 16;
            }
            default -> throw new RuntimeException("Illegal value " + scaPar[2] + " for scaPar[2]");
        }
        int[] iterMCTSWrapArr={0,100,800}; //{0,50,100,200,500,1000}; //{0,50,100,200,300}; // ,100,200,300,500,600,800,1000};
        //int[] iterMCTSWrapArr={0};  // only in conjunction with oPar's nPly > 0 (see below)
        int fact=0;   // 1 or 0: whether to invoke lower bounds (1) or not (0)
        HashMap<Integer, Double> hm = new HashMap<>();  // lower bounds of %-solved-rates to expect as a fct of iterMCTSWrap
        hm.put(   0,fact*0.70);                         // Other values: EPS=1e-8, maxDepth=50, c_puct=1.0
        hm.put(  50,fact*0.80);
        hm.put( 100,fact*0.98);
        hm.put( 200,fact*0.99);
        hm.put( 300,fact*0.99);
        hm.put( 500,fact*0.99);
        hm.put( 800,fact*0.99);
        hm.put(1000,fact*0.99);

        selectedGame = "RubiksCube";
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);

        innerRubiksTest(scaPar,agtFiles,iterMCTSWrapArr,hm,pMin,pMax, nruns, csvFile);
    }

    /**
     * @param scaPar    scalable parameters
     * @param agtFiles  the agent(s) to wrap
     * @param iterMCTSWrapArr the iterations for MCTS wrapper
     * @param hm        hash map with lower bounds on %-solved-rate as a fct of iterMCTSWrap. Throws an assertion if
     *                  solved-rate does not surpass lower bound. Currently only invoked for
     *                  <pre>{@code EPS = 1e-8 && c_puct = 1.0}</pre>
     * @param pMin      min number of scrambling twists
     * @param pMax      max number of scrambling twists
     * @param nruns    number of runs
     * @param csvFile   result file
     */
    public void innerRubiksTest(String[] scaPar,
                                String[] agtFiles,
                                int[] iterMCTSWrapArr,
                                HashMap<Integer,Double> hm,
                                int pMin, int pMax, int nruns,
                                String csvFile) {
        long startTime;
        double elapsedTime=0,deltaTime;

        PlayAgent pa;
        PlayAgent qa;
        double[] epsArr = {1e-8}; // {1e-8, 0.0, -1e-8}; // {1e-8, 0.0};    //
        double c_puct=10.0; //1.0; //10.0;      // Sep'2022: c_puct=1.0  was found to be in most cases better than 10.0
        String userTitle1 = "pTwist", userTitle2 = "EE";
        int maxDepth = 50;  // 25, 50, -1
        int ee = 50;       // 20 or 50: eval-epiLength
        double percSolved;

        MCompeteMWrap mCompete;
        ArrayList<MCompeteMWrap> mcList = new ArrayList<>();

        GameBoardCube gb = new GameBoardCube(arenaTrain);		// needed for chooseStartState()

        for (int run=0; run<nruns; run++) {
            for (String agtFile : agtFiles) {
                System.out.println("*** Starting run "+run+"/"+(nruns-1) +
                        " of innerRubiksCubeTest for " + agtFile + " (scaPar={" + scaPar[0] + "," + scaPar[1] + "," + scaPar[2] +
                        "}) ***");
                pa = arenaTrain.loadAgent(agtFile);

                ParOther oPar = new ParOther();
                ParWrapper wrPar = new ParWrapper();
                wrPar.setWrapperNPly(0);     // or >0 together with iterMCTSWrapArr={0}, if testing MaxNWrapper
                pa.setWrapperParamsOfromWr(wrPar);

                for (double eps : epsArr) {
                    ConfigWrapper.EPS = eps;
                    for (int iterMCTSWrap : iterMCTSWrapArr) {
                        if (iterMCTSWrap == 0) qa = pa;
                        else qa = new MCTSWrapperAgent(iterMCTSWrap, c_puct,
                                new PlayAgentApproximator(pa),
                                "MCTS-wrapped", // +" " + pa.getName(),
                                maxDepth, oPar);
                        if (wrPar.getWrapperNPly() > 0) {
                            System.out.println("wrPar nPly = " + wrPar.getWrapperNPly());
                            qa = new MaxN2Wrapper(pa, wrPar.getWrapperNPly(), oPar);
                        }

                        startTime = System.currentTimeMillis();

                        double avgPercSolved=0.0;
                        for (int p=pMin; p<=pMax; p++) {
                            EvalCubeParams ecp = new EvalCubeParams(p, p, ee, CubeConfig.EvalNmax);
                            EvaluatorCube m_eval = new EvaluatorCube(qa, gb, 0, 1, 0, ecp);
                            EvalResult eRes = m_eval.evalAgent(qa);
                            percSolved = eRes.getResult();
                            System.out.println(eRes.getMsg());
                            mCompete = new MCompeteMWrap(run, agtFile, 1, 0, iterMCTSWrap,
                                    eps, 0, c_puct, percSolved,
                                    p, ee);
                            mcList.add(mCompete);
                            avgPercSolved += percSolved;
                        }
                        avgPercSolved /= (pMax-pMin+1);
                        if (eps == 1e-8 && c_puct == 1.0) {     // test thresholds currently only available for this setting
                            if (hm.get(iterMCTSWrap)==null) {
                                System.err.println("[innerRubiksTest] Warning: Assertion skipped because hm has no threshold entry for iter="+iterMCTSWrap);
                            } else
                                assert avgPercSolved > hm.get(iterMCTSWrap) :
                                    "Test failed for iterMCTSWrap = " + iterMCTSWrap +
                                    ": % solved=" + avgPercSolved + ", but threshold is "+hm.get(iterMCTSWrap);

                        }

                        deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
                        elapsedTime += deltaTime;
                        System.out.println("... for EPS=" + eps + ", iter=" + iterMCTSWrap + ", " + deltaTime + " sec");
                    }

                    // print the full list mcList after finishing each  (iterMCTSWrap)
                    // (overwrites the file written from previous (iterMCTSWrap))
                    MCompeteMWrap.printMultiCompeteList(csvFile, mcList, pa, arenaTrain, userTitle1, userTitle2);
                } // for (EPS)
            } // for (agtFiles)
        } // for (run)

        System.out.println("[innerRubiksTest] "+elapsedTime+" sec.");
    }

    /**
     * Test the performance of MCTSWrapperAgent on ConnectFour: We run multi-compete episodes for EPS=1e-08
     * <ul>
     *     <li> MCTS(iter=10000,treeDepth=40)    vs.    <br>
     *          MCTSWrapper[agtFile](iter=iterMWrap)
     * </ul>
     * where agtFile="TCL-EXP-NT3-al37-lam000-6000k-epsfin0.agt.zip"  and MCTSWrapper plays 2nd.
     * [If it plays 1st, it will always win, this is uninteresting.]
     * <p>
     * The win rates are tested against these expectations:
     * <ol>
     *     <li> If iterMCTSWrap==0, then winrate[MCTSWrapper] &le; 0.4 (not strict, sometimes only &le; 0.6)
     *     <li> If iterMCTSWrap &ge; 300, then winrate[MCTSWrapper] &ge; 0.95
     *  </ol>
     *  Results are written to {@code csvFile} for inspection.
     *  <p>
     * Computation time depends on iterMCTSWrap and the number of for-loop-passes in {@code innerC4Test}.
     * Example for numEpisodes=25, iterMCTSWrapArr={0,50,100,200,300,500,750,1000}, nTrial=5, one EPS, one agtFile, one player role for MCTSWrapper:
     * 8*5=40 for-loop-passes, in total 2800 sec.
     */
    @Test
    public void C4_vs_MCTS_Test() {
        long startTime;
        double elapsedTime;

        int numEpisodes=25;
        int nTrial=5;
        int[] iterMCTSWrapArr={0,50,100,200,300,500,750,1000};  // ={0,1000}; //
        double[] epsArr = { 1e-8}; // {1e-8, 0.0};    // {1e-8, 0.0, -1e-8};
        String csvFile = "mCompeteMCTS-vs-MWrap-25runs-normF.csv";
        boolean doAssert = false;
        startTime = System.currentTimeMillis();

        innerC4Test(numEpisodes,nTrial,iterMCTSWrapArr,epsArr,"MCTS",csvFile,doAssert);

        elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[C4_vs_MCTS_Test] "+elapsedTime+" sec.");
    }

    // this is just for time measurements
    @Test
    public void C4_vs_Rand_Test() {
        long startTime;
        double elapsedTime;

        int numEpisodes=25;
        int nTrial=5;
        int[] iterMCTSWrapArr={0,50,100,200,300,500,750,1000};  // ={0,1000}; //
        double[] epsArr = { 1e-8}; // {1e-8, 0.0};    // {1e-8, 0.0, -1e-8};
        String csvFile = "mCompeteRand-vs-MWrap-25runs-normF.csv";
        boolean doAssert = false;
        startTime = System.currentTimeMillis();

        innerC4Test(numEpisodes,nTrial,iterMCTSWrapArr,epsArr,"Random",csvFile,doAssert);

        elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[C4_vs_Rand_Test] "+elapsedTime+" sec.");
    }

    /**
     * Test the performance of MCTSWrapperAgent on ConnectFour: We run multi-compete episodes
     * <ul>
     *   <li>  MCTSWrapper[agtFile](iter=iterMWrap, EPS=1e-08)    vs.    AlphaBeta-DL
     *   <li>  MCTSWrapper[agtFile](iter=iterMWrap, EPS=0.0  )    vs.    AlphaBeta-DL
     * </ul>
     * where AlphaBeta-DL="AlphaBeta with distant losses" and MCTSWrapper plays 1st.
     * [If it plays 2nd, it will always lose, this is uninteresting.]
     * <p>
     * The win rates for agtFile="TCL-EXP-NT3-al37-lam000-6000k-epsfin0.agt.zip" are compared with these expectations:
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
        int[] iterMCTSWrapArr={0}; //,50,100,200,300,500,600,800,1000};
        double[] epsArr = { 1e-8,0.0,-1e-8}; // {1e-8, 0.0};    // {1e-8, 0.0, -1e-8};
        boolean doAssert = false;
        startTime = System.currentTimeMillis();

        innerC4Test(numEpisodes,nTrial,iterMCTSWrapArr,epsArr,"AlphaBetaDL",csvFile,doAssert);

        elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[C4_vs_ABDL_Test] "+elapsedTime+" sec.");
    }

    public void innerC4Test(int numEpisodes, int nTrial,
                            int[] iterMCTSWrapArr,
                            double[] epsArr,
                            String opponentName,
                            String csvFile,
                            boolean doAssert)
    {
        long startTime;
        double elapsedTime;
        selectedGame = "ConnectFour";
        scaPar = GBGBatch.setDefaultScaPars(selectedGame);
        String[] agtFiles = {"2-TCL-EXP-NT3-al37-lam000-6000k-epsfin0.agt.zip"};
        PlayAgent pa,qa,opponent;
        String userTitle1 = "time", userTitle2 = "user2";
        double userValue1=0.0, userValue2=0.0;
        double winrate;     // the win rate of MCTSWrapperAgent
        //double[] cpuctArr = {1.0}; //{0.2, 0.4, 0.6, 0.8, 1.0, 1.4, 2.0, 4.0, 10.0}; // {1.0};
        double c_puct = 1.0;
        int playerMWrap;        // playerMWrap: whether MCTSWrapper is player 0 or player 1

        MCompeteMWrap mCompete;
        ArrayList<MCompeteMWrap> mcList = new ArrayList<>();

        arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);
        GameBoardC4 gb = new GameBoardC4(arenaTrain);
        StateObservation so = gb.getDefaultStartState();

        switch(opponentName) {
            case "MCTS" -> {
                playerMWrap = 1;
                boolean CONSTRUCT_MCTS_ANEW = true;
                if (CONSTRUCT_MCTS_ANEW) {
                    ParMCTS parMCTS = new ParMCTS();
                    parMCTS.setNumIter(10000);
                    parMCTS.setTreeDepth(40);
                    parMCTS.setNormalize(false);        // was missing prior to 2021-08-11, makes MCTS stronger when playing 1st
                    ParOther oPar = new ParOther();
                    opponent = new MCTSAgentT("MCTS",so, parMCTS, oPar);
                } else {        // load MCTS from file, just for debugging
                    String mctsFile = "3-MCTS10000-40.agt.zip";
                    opponent = arenaTrain.loadAgent(mctsFile);
                }
            }
            case "AlphaBetaDL" -> {
                playerMWrap = 0;
                opponent = new AlphaBetaAgent(new BookSum(),1000);	// search for distant losses
                opponent.instantiateAfterLoading();

            }
            case "Random" -> {
                playerMWrap = 0;
                opponent = new RandomAgent("Rand");
                opponent.instantiateAfterLoading();

            }
            default -> throw new RuntimeException("Wrong value for opponentName");
        }

        for (int run=0; run<nTrial; run++) {
            for (String agtFile : agtFiles) {
                //setupPaths(agtFile, csvFile);     // builds filePath
                pa = arenaTrain.loadAgent(agtFile);

                for (double EPS : epsArr) {
                    ConfigWrapper.EPS = EPS;
                    //numEpisodes = (EPS<0) ? 10 : 1;
                    for (int iterMCTSWrap : iterMCTSWrapArr) {
                        if (iterMCTSWrap == 0) qa = pa;
                        else qa = new MCTSWrapperAgent(iterMCTSWrap, c_puct,
                                new PlayAgentApproximator(pa),
                                "MCTS-wrapped " + pa.getName(),
                                100, new ParOther()); // -1);

                        // *** only a sub-test: How good would AB-DL be in place of MCTSWrap(TCL-EXP)? ***
                        //qa = new AlphaBetaAgent(new BookSum(),1000);	// search for distant losses
                        //qa.instantiateAfterLoading();

                        // *** another sub-test: How good would MaxNWrapper be in place of MCTSWrapperAgent
                        int nPly=0;                 // if nPly>0, test this together with iterMCTSWrapArr={0}
                        if (nPly > 0)
                        {
                            pa.getParOther().setWrapperNPly(nPly);
                            pa.getParWrapper().setWrapperNPly(nPly);
                            System.out.println("oPar nPly = " + nPly);
                            qa = new MaxN2Wrapper(pa, nPly, pa.getParOther());
                        }

                        ScoreTuple sc;
                        PlayAgtVector paVector = new PlayAgtVector(qa, opponent);
                        for (int p_MWrap : new int[]{playerMWrap})       // {0,1}
                        {     // p_MWrap: whether MCTSWrapper is player 0 or player 1
                            startTime = System.currentTimeMillis();
                            sc = XArenaFuncs.competeNPlayer(paVector.shift(p_MWrap), so, numEpisodes, 0, null);
                            winrate = (sc.scTup[p_MWrap] + 1) / 2;
                            elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
                            System.out.println("(iterMW,EPS,p_MWrap) = (" + iterMCTSWrap + "," + EPS + "," + p_MWrap + "): " +
                                    "  winrate = " + winrate+"     time = "+elapsedTime);
                            mCompete = new MCompeteMWrap(run, agtFile, numEpisodes, 0, iterMCTSWrap,
                                    EPS, p_MWrap, c_puct, winrate,
                                    elapsedTime, userValue2);
                            mcList.add(mCompete);
                            if (opponent instanceof MCTSAgentT && doAssert) {
                                if (p_MWrap == 1 && iterMCTSWrap == 0)
                                    assert (winrate <= 0.6) : "winrate is not <=0.6 for iter==0!";
                                if (p_MWrap == 1 && iterMCTSWrap >= 500)
                                    assert (winrate >= 0.95) : "winrate is not >=0.95 for iter>=500!";
                            } else if (opponent instanceof AlphaBetaAgent && doAssert) {
                                if (p_MWrap == 0 && iterMCTSWrap == 0)
                                    assert (winrate <= 0.95) : "winrate is not <=0.95 for iter==0!";
                                if (p_MWrap == 0 && iterMCTSWrap >= 500)
                                    assert (winrate >= 0.95) : "winrate is not >=0.95 for iter>=500!";
                            }
                        } // for (p_MWrap)

                        // print the full list mcList after finishing each  (p_MCTS)
                        // (overwrites the file written from previous (p_MCTS))
                        MCompeteMWrap.printMultiCompeteList(csvFile, mcList, pa, arenaTrain, userTitle1, userTitle2);
                    } // for (iterMCTSWrap)
                } // for (EPS)
            } // for (agtFile)
        } // for (run)
    }

    // this is just for time measurements
    @Test
    public void Othello_vs_Rand_Test() {
        long startTime;
        double elapsedTime;

        int numEpisodes=1;
        int nTrial=5;
        int[] iterMCTSWrapArr={0,100,500,10000};  // ={0,1000}; //
        double[] epsArr = { 1e-8}; // {1e-8, 0.0};    // {1e-8, 0.0, -1e-8};
        String csvFile = "multiCompeteOthello-time.csv";
        boolean doAssert = false;
        startTime = System.currentTimeMillis();

        innerOthelloTest(numEpisodes,nTrial,iterMCTSWrapArr,epsArr,"Random",csvFile,doAssert);

        elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[C4_vs_Rand_Test] "+elapsedTime+" sec.");
    }

    public void innerOthelloTest(int numEpisodes, int nTrial,
                            int[] iterMCTSWrapArr,
                            double[] epsArr,
                            String opponentName,
                            String csvFile,
                            boolean doAssert)
    {
        long startTime;
        double elapsedTime;
        selectedGame = "Othello";
        scaPar = GBGBatch.setDefaultScaPars(selectedGame);
        String[] agtFiles = {"TCL4-100_7_250k-lam05_P4_nPly2-FAm_C.agt.zip"};
        PlayAgent pa,qa,opponent;
        String userTitle1 = "time", userTitle2 = "user2";
        double userValue1=0.0, userValue2=0.0;
        double winrate;     // the win rate of MCTSWrapperAgent
        //double[] cpuctArr = {1.0}; //{0.2, 0.4, 0.6, 0.8, 1.0, 1.4, 2.0, 4.0, 10.0}; // {1.0};
        double c_puct = 1.0;
        int playerMWrap;        // playerMWrap: whether MCTSWrapper is player 0 or player 1

        MCompeteMWrap mCompete;
        ArrayList<MCompeteMWrap> mcList = new ArrayList<>();

        arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);
        GameBoardOthello gb = new GameBoardOthello(arenaTrain);
        StateObservation so = gb.getDefaultStartState();

        switch(opponentName) {
            case "MCTS" -> {
                playerMWrap = 1;
                boolean CONSTRUCT_MCTS_ANEW = true;
                if (CONSTRUCT_MCTS_ANEW) {
                    ParMCTS parMCTS = new ParMCTS();
                    parMCTS.setNumIter(10000);
                    parMCTS.setTreeDepth(40);
                    parMCTS.setNormalize(false);        // was missing prior to 2021-08-11, makes MCTS stronger when playing 1st
                    ParOther oPar = new ParOther();
                    opponent = new MCTSAgentT("MCTS",so, parMCTS, oPar);
                } else {        // load MCTS from file, just for debugging
                    String mctsFile = "3-MCTS10000-40.agt.zip";
                    opponent = arenaTrain.loadAgent(mctsFile);
                }
            }
            case "Random" -> {
                playerMWrap = 0;
                opponent = new RandomAgent("Rand");
                opponent.instantiateAfterLoading();

            }
            default -> throw new RuntimeException("Wrong value for opponentName");
        }

        for (int run=0; run<nTrial; run++) {
            for (String agtFile : agtFiles) {
                //setupPaths(agtFile, csvFile);     // builds filePath
                pa = arenaTrain.loadAgent(agtFile);

                for (double EPS : epsArr) {
                    ConfigWrapper.EPS = EPS;
                    //numEpisodes = (EPS<0) ? 10 : 1;
                    for (int iterMCTSWrap : iterMCTSWrapArr) {
                        if (iterMCTSWrap == 0) qa = pa;
                        else qa = new MCTSWrapperAgent(iterMCTSWrap, c_puct,
                                new PlayAgentApproximator(pa),
                                "MCTS-wrapped " + pa.getName(),
                                100, new ParOther()); // -1);

                        // *** only a sub-test: How good would AB-DL be in place of MCTSWrap(TCL-EXP)? ***
                        //qa = new AlphaBetaAgent(new BookSum(),1000);	// search for distant losses
                        //qa.instantiateAfterLoading();

                        // *** another sub-test: How good would MaxNWrapper be in place of MCTSWrapperAgent
                        int nPly=0;                 // if nPly>0, test this together with iterMCTSWrapArr={0}
                        if (nPly > 0)
                        {
                            pa.getParOther().setWrapperNPly(nPly);
                            pa.getParWrapper().setWrapperNPly(nPly);
                            System.out.println("oPar nPly = " + nPly);
                            qa = new MaxN2Wrapper(pa, nPly, pa.getParOther());
                        }

                        ScoreTuple sc;
                        PlayAgtVector paVector = new PlayAgtVector(qa, opponent);
                        for (int p_MWrap : new int[]{playerMWrap})       // {0,1}
                        {     // p_MWrap: whether MCTSWrapper is player 0 or player 1
                            startTime = System.currentTimeMillis();
                            sc = XArenaFuncs.competeNPlayer(paVector.shift(p_MWrap), so, numEpisodes, 0, null);
                            winrate = (sc.scTup[p_MWrap] + 1) / 2;
                            elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
                            System.out.println("(iterMW,EPS,p_MWrap) = (" + iterMCTSWrap + "," + EPS + "," + p_MWrap + "): " +
                                    "  winrate = " + winrate+"     time = "+elapsedTime);
                            mCompete = new MCompeteMWrap(run, agtFile, numEpisodes, 0, iterMCTSWrap,
                                    EPS, p_MWrap, c_puct, winrate,
                                    elapsedTime, userValue2);
                            mcList.add(mCompete);
                            if (opponent instanceof MCTSAgentT && doAssert) {
                                if (p_MWrap == 1 && iterMCTSWrap == 0)
                                    assert (winrate <= 0.6) : "winrate is not <=0.6 for iter==0!";
                                if (p_MWrap == 1 && iterMCTSWrap >= 500)
                                    assert (winrate >= 0.95) : "winrate is not >=0.95 for iter>=500!";
                            }
                        } // for (p_MWrap)

                        // print the full list mcList after finishing each  (p_MCTS)
                        // (overwrites the file written from previous (p_MCTS))
                        MCompeteMWrap.printMultiCompeteList(csvFile, mcList, pa, arenaTrain, userTitle1, userTitle2);
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
        scaPar = SetupGBG.setDefaultScaPars(selectedGame);
        arenaTrain = SetupGBG.setupSelectedGame(selectedGame, scaPar,"",false,true);
        GameBoard gb = new GameBoardOthello(arenaTrain);        // needed for chooseStartState()
        StateObservation sob = gb.getDefaultStartState();
        MCTSNode mctsNode = new MCTSNode(new GameStateIncludingPass(sob));
        Approximator approximator = new PlayAgentApproximator(new RandomAgent("rand"));
        MCTS mcts = new MCTS(approximator, 1.0, 50);
        MCTSWrapperAgent mwa = new MCTSWrapperAgent(100,1.0,approximator,"",50, new ParOther());
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