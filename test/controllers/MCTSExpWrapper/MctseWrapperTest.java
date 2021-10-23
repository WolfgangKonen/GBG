package controllers.MCTSExpWrapper;

import controllers.ExpectimaxNAgent;
import controllers.MC.MCAgentN;
import controllers.MCTSExpWrapper.stateApproximation2.PlayAgentApproximator2;
import controllers.MCTSWrapper.ConfigWrapper;
import controllers.MCTSWrapper.MCTSWrapperAgent;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.MaxN2Wrapper;
import controllers.PlayAgent;
import games.Arena;
import games.EWN.EvaluatorEWN;
import games.EWN.GameBoardEWN;
import games.EWN.StateObserverEWN;
import games.KuhnPoker.GameBoardKuhnPoker;
import games.KuhnPoker.StateObserverKuhnPoker;
import games.RubiksCube.CubeConfig;
import games.RubiksCube.EvalCubeParams;
import games.RubiksCube.EvaluatorCube;
import games.RubiksCube.GameBoardCube;
import games.StateObservation;
import games.ZweiTausendAchtundVierzig.GameBoard2048;
import org.junit.Test;
import params.ParMC;
import params.ParOther;
import starters.GBGBatch;
import starters.MCompeteMWrap;
import tools.Types;
import tools.Types.ACTIONS;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MctseWrapperTest extends GBGBatch {
    String selectedGame;
    String[] scaPar;
    String[] agtFiles;
    enum EpisodeType {CASE_A,CASE_B}
    int[] kmax = {4,5};

    /**
     * Test the MctseWrapperAgent on 3x3 EWN: We build a state, e.g.
     * <pre>
     *  [  ] [X2] [  ]   (diceVal:1, availActions: 0501,0502,0504 )
     *  [  ] [X0] [O1]
     *  [  ] [O2] [  ]       </pre>
     * and test whether TDNT4, MCTSE-Wrapper[TDNT4] or ExpectimaxN suggest the right move 0504.
     * <p>
     * Next, run {@link MctseChanceNode#checkTree(int) checkTree} on MCTSE-Wrapper[TDNT4]: check for consistent visit
     * counts and report number of nodes.
     * <p>
     * Finally, run {@link MctseChanceNode#numChilds(Map) numChilds} on MCTSE-Wrapper[TDNT4]: if a MctseExpecNode has
     * enough visit counts (empirically: at least 4*maxNumChildren), is then every child formed, i.e. visited at
     * least once? This test has no guarantee to succeed, it may fail with low probability.
     */
    @Test
    public void stateEWNTest() {
        String agtFile = "tdnt4-10000.agt.zip";
        int[] iterMctseWrapArr = {1000}; //{0,100,200,500,1000}; //,100,200,300,500,600,800,1000};
        int nTrial = 1;
        EpisodeType m_epi = EpisodeType.CASE_A;     // CASE_A   CASE_B

        long startTime;
        double elapsedTime=0,deltaTime;

        PlayAgent pa,qa,ra;
        double c_puct=500.0;
        int maxDepth = -1;
        Types.ACTIONS_VT act_pa, act_qa, act_ra;
        StateObserverEWN so;

        scaPar = GBGBatch.setDefaultScaPars("EWN");                // for EWN: 3x3
        arenaTrain = GBGBatch.setupSelectedGame("EWN",scaPar);

        pa = arenaTrain.loadAgent(agtFile);
        System.out.println("*** Running stateEWNTest for " + agtFile + " (scaPar={" + scaPar[0] + "," + scaPar[1] + "," + scaPar[2] + "}) ***");

//        ParOther oPar = new ParOther();
//        oPar.setWrapperNPly(0);     // or >0 together with iterMCTSWrapArr={0}, if testing MaxNWrapper
//        pa.setWrapperParams(oPar);

        for (int run=0; run<nTrial; run++) {
            for (int k=0; k<kmax[m_epi.ordinal()]; k++) {
                System.out.println("+++ k="+k+": +++");
                ra = new ExpectimaxNAgent("",15);

                for (int iterMctseWrap : iterMctseWrapArr) {
                    System.out.println("*** iterMctseWrap="+iterMctseWrap+ " ***");
                    if (iterMctseWrap == 0) qa = pa;
                    else qa = new MctseWrapperAgent(iterMctseWrap, c_puct,
                            new PlayAgentApproximator2(pa),
                            "Mctse-Wrapped " + pa.getName(),
                            maxDepth);

                    startTime = System.currentTimeMillis();

                    // build the state to examine
                    so = switch (m_epi) {
                        case CASE_A -> buildStateA(arenaTrain, k);
                        case CASE_B -> buildStateB(arenaTrain, k);
                    };

                    for (int i=0; i<1; i++) {
                        // ---- best move test ----
                        act_pa = pa.getNextAction2(so,false,true);
                        act_qa = qa.getNextAction2(so,false,true);
                        act_ra = ra.getNextAction2(so,false,false);
                        if (iterMctseWrap==1)
                            assert act_pa.getVBest()==act_qa.getVBest() : "vBest differs for pa and qa";
                        DecimalFormat form = new DecimalFormat("0.000");
                        double[] vtab = act_ra.getVTable();
                        System.out.println("i="+i+": "+so.stringDescr()+
                                "[vTab Expectimax] "+ form.format(vtab[0]) + " " + form.format(vtab[1]) + " " +form.format(vtab[2]) + " " +
                                "\n[3 agts] vBest=("+form.format(act_pa.getVBest()) + " " + form.format(act_qa.getVBest()) + " " + form.format(act_ra.getVBest())+")"+
                                ", act_TD="+act_pa.toInt()+", act_Wrap="+act_qa.toInt()+", act_Expec="+act_ra.toInt());
                        int bestAct = so.getStoredActBest().toInt();
                        assert act_ra.toInt() == bestAct : "Mismatch: act_Expec="+act_ra.toInt()+" != bestAct="+ bestAct;

                        // ---- checkTree test ----
                        int numNodes = ((MctseWrapperAgent) qa).getRootNode().checkTree(iterMctseWrap-1);
                        // why iterMctseWrap-1? - The first call expands, does not increment any visit count
                        System.out.println("[checkTree] numNodes="+numNodes);

                        // ---- numChild test ----
                        // Make a histogram of childNodes.size() of all MctseExpecNodes (Expectimax nodes).
                        // This histogram is stored in HashMap histo with numVisits (numuber of visits to this node) as key.
                        // a) Is the number of MctseExpecNodes equal to the counts in the histogram?
                        // b) If numVisits is large enough (empirically: > 4*maxNumChildren), then with high probability
                        //    each possible child should be visited at least once
                        //    -->  all histo counts should be in the largest bin.
                        Map<Integer, double[]> histo = new HashMap<>();
                        int numExpec = ((MctseWrapperAgent) qa).getRootNode().numChilds(histo);
                        int countsHisto = 0;
                        for (Map.Entry<Integer, double[]> entry : histo.entrySet()) {
                            int numVisits = entry.getKey();
                            double[] vals = entry.getValue();
                            double[] vals2 = vals.clone();
                            countsHisto += sum(vals);
                            vals2[vals.length-1]=0;  // we delete the highest bin count, than all entries should be 0.0
                            if (numVisits > 4*vals.length) {
                                assert sum(vals2)==0 :
                                        "[EWNTest] Unexpected histo-entry: numVisits="+numVisits+", histo="+printString(vals);
                            }
                        }
                        assert numExpec == countsHisto :
                                "[EWNTest] numExpec="+numExpec+" and countsHisto="+countsHisto+" differ!";
                        System.out.println("[numChilds] numExpecNodes="+numExpec+","+countsHisto);

                    } // for (i)

                    deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
                    elapsedTime += deltaTime;
                    System.out.println("  ");
                }

            } // for (k)
        } // for (run)

        System.out.println("[stateEWNTest] "+elapsedTime+" sec.");
    }

    /**
     * Helper method for {@link #stateEWNTest()}: return the {@code k}'th move from an episode A.
     * @param arenaEWN  to build the start state
     * @param k         ply number
     * @return  an EWN state with the known best action set in {@code storedActBest}
     */
    private StateObserverEWN buildStateA(Arena arenaEWN, int k) {
        GameBoardEWN gb = new GameBoardEWN(arenaEWN);
        StateObserverEWN so = (StateObserverEWN) gb.getDefaultStartState();
        so.setNextActionDeterministic(ACTIONS.fromInt(1));      // bestAction X: 0307, V=+0.827
        int[] bestAction = new int []{307,804,4,504};

        if (k>0) {
            so.advanceDeterministic(ACTIONS.fromInt(304));  // X: from 3 to 4
            so.advanceNondeterministic(ACTIONS.fromInt(0));     // bestAction O: 0804, V=+5/9
        }
        if (k>1) {
            so.advanceDeterministic(ACTIONS.fromInt(804));  // O: from 8 to 4
            so.advanceNondeterministic(ACTIONS.fromInt(0));     // bestAction X: 0004, V=-5/9
        }
        if (k>2) {
            so.advanceDeterministic(ACTIONS.fromInt(4));    // X: from 0 to 4
            so.advanceNondeterministic(ACTIONS.fromInt(1));     // bestAction O: 0504, V=+1/3
            /* Now we have the state (O moves)
                [  ] [X2] [  ]     (diceVal:1,   availActions: 0501,0502,0504 )
                [  ] [X0] [O1]
                [  ] [O2] [  ]     The optimal action is 0504
             */
        }
        so.storeBestActionInfo(new Types.ACTIONS_VT(bestAction[k]));
        return so;
    }

    /**
     * Same as {@link #buildStateA(Arena, int)}, but for another episode B.
     */
    private StateObserverEWN buildStateB(Arena arenaEWN, int k) {
        GameBoardEWN gb = new GameBoardEWN(arenaEWN);
        StateObserverEWN so = (StateObserverEWN) gb.getDefaultStartState();
        so.setNextActionDeterministic(ACTIONS.fromInt(0));      // bestAction X: 0004, V=-0.235
        int[] bestAction = new int []{4,804,105,704,307};

        if (k>0) {
            so.advanceDeterministic(ACTIONS.fromInt(1));    // X: from 0 to 1
            so.advanceNondeterministic(ACTIONS.fromInt(0));     // bestAction O: 0804, V=-1/9
        }
        if (k>1) {
            so.advanceDeterministic(ACTIONS.fromInt(805));   // O: from 8 to 5
            so.advanceNondeterministic(ACTIONS.fromInt(0));     // bestAction X: 0105, V=+1
        }
        if (k>2) {
            so.advanceDeterministic(ACTIONS.fromInt(104));   // X: from 1 to 4
            so.advanceNondeterministic(ACTIONS.fromInt(2));     // bestAction O: 0704, V=+1/3
            /* Now we have the state (O moves)
                [  ] [  ] [  ]     (diceVal:2,   availActions: 0706,0704,0703 )
                [X1] [X0] [O0]
                [  ] [O2] [  ]     The optimal action is 0704 (V=1.0)
             */
        }
        if (k>3) {
            so.advanceDeterministic(ACTIONS.fromInt(704));   // O: from 7 to 4
            so.advanceNondeterministic(ACTIONS.fromInt(0));     // bestAction X: 0307
            /* Now we have the state (X moves)
                [  ] [  ] [  ]     (diceVal:0,   availActions: 0706,0704,0703 )
                [X1] [O2] [O0]
                [  ] [  ] [  ]     The optimal action is 0307 (V=-1.0)
             */
        }
//          so.advanceDeterministic(ACTIONS.fromInt(307));   // X: from 3 to 7
//          so.advanceNondeterministic(ACTIONS.fromInt(0));     // bestAction O: none, O looses
        so.storeBestActionInfo(new Types.ACTIONS_VT(bestAction[k]));
        return so;
    }


    private static double sum(double[] values) {
        double s=0.0;
        for (double value : values) s += value;
        return s;
    }
    private static String printString(double[] values) {
        StringBuilder s= new StringBuilder("[");
        for (int i=0; i<values.length-1; i++) s.append((int) values[i]).append(", ");
        s.append((int) values[values.length - 1]).append("]");
        return s.toString();

    }

    /**
     * Test the MctseWrapperAgent on KuhnPoker: We build a (random) start state
     * and test whether MC-N or MCTSE-Wrapper[MC-N] suggest the right move.
     */
    @Test
    public void gameKuhnTest() {
        scaPar=new String[]{"", "", ""};
        int[] iterMctseWrapArr = {1000}; //{0,100,200,500,1000}; //,100,200,300,500,600,800,1000};
        int nTrial = 1;

        long startTime;
        double elapsedTime=0,deltaTime;

        selectedGame = "KuhnPoker";
        PlayAgent pa,qa;
        double c_puct=1.0;
        int maxDepth = -1;
        Types.ACTIONS_VT act_pa, act_qa;

        arenaTrain = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoardKuhnPoker gb = new GameBoardKuhnPoker(arenaTrain);		// needed for chooseStartState()

        for (int run=0; run<nTrial; run++) {
            ParMC mcPar = new ParMC();
            pa = new MCAgentN(mcPar);
            pa = new ExpectimaxNAgent("ExpectimaxN",6);

//            ParOther oPar = new ParOther();
//            oPar.setWrapperNPly(0);     // or >0 together with iterMCTSWrapArr={0}, if testing MaxNWrapper
//            pa.setWrapperParams(oPar);

            for (int iterMctseWrap : iterMctseWrapArr) {
                System.out.println("*** iterMctseWrap="+iterMctseWrap+ " ***");
                if (iterMctseWrap == 0) qa = pa;
                else qa = new MctseWrapperAgent(iterMctseWrap, c_puct,
                        new PlayAgentApproximator2(pa),
                        "Mctse-Wrapped " + pa.getName(),
                        maxDepth);

                startTime = System.currentTimeMillis();

                for (int i=0; i<10; i++) {
                    // The actions are 0=FOLD, 1=CHECK, 2=BET, 3=CALL
                    // build the state to examine
                    StateObserverKuhnPoker so = (StateObserverKuhnPoker) gb.getDefaultStartState();
                    // best action for p0 is {1,2} if p0=J, {2} if p0=Q, {1,2} if p0=K

                    //so.advance(new ACTIONS(1));   // p0 plays CHECK
                    // best action for p1 is {1,2} if p1=J, {2} if p1=Q, {2} if p1=K

                    //so.advance(new ACTIONS(1));   // p0 plays BET
                    // best action for p1 is {0} if p1=J, {0,3} if p1=Q, {3} if p1=K

                    act_pa = pa.getNextAction2(so.partialState(),false,true);
                    act_qa = qa.getNextAction2(so.partialState(),false,true);
                    if (iterMctseWrap==1)
                        assert act_pa.getVBest()==act_qa.getVBest() : "vBest differs for pa and qa";
                    System.out.println("i="+i+": "+so.stringDescr()+
                            ", vBest="+act_pa.getVBest() + " " + act_qa.getVBest()+
                            ", act_pa="+act_pa.toInt()+", act_qa="+act_qa.toInt());
                    //so.advance(act_pa);

                    int numNodes = ((MctseWrapperAgent) qa).getRootNode().checkTree(iterMctseWrap-1);
                    // why iterMctseWrap-1? - The first call expands, does not increment any visit count
                    System.out.println("[checkTree] numNodes="+numNodes);

                } // for (i)

                deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
                elapsedTime += deltaTime;
                System.out.println("  ");
            } // for (iterMctseWrap)

        } // for (run)

        System.out.println("[gameKuhnTest] "+elapsedTime+" sec.");
    }

    /**
     * Test the MctseWrapperAgent on 2048: If we run for exactly one iteration, the wrapped agent
     * should have the same result as the unwrapped agent, because for visitCounts.size()==0 we just
     * call mctsNode.gameState.getNextAction(this.approximator).
     *
     * For larger iterations, we just print out the vBest of selected states, no check here.
     */
    @Test
    public void oneExpand2048Test() {
        scaPar=new String[]{"", "", ""};
        agtFiles = new String[]{"TC-NT3_fixed_4_6-Tupels_200k-FA.agt.zip"};
        int[] iterMctseWrapArr = {1,1000}; //{0,100,200,500,1000}; //,100,200,300,500,600,800,1000};
        int nTrial = 1;
        String csvFile = "dummy.csv";

        long startTime;
        double elapsedTime=0,deltaTime;

        selectedGame = "2048";
        PlayAgent pa, qa;
        double c_puct=1.0;
        int maxDepth = -1;
        Types.ACTIONS_VT act_pa, act_qa;


        arenaTrain = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoard2048 gb = new GameBoard2048(arenaTrain);		// needed for chooseStartState()

        for (int run=0; run<nTrial; run++) {
            for (String agtFile : agtFiles) {
                System.out.println("*** Running oneExpandTest for " + agtFile + " (scaPar={" + scaPar[0] + "," + scaPar[1] + "," + scaPar[2] + "}) ***");

                pa = arenaTrain.loadAgent(agtFile);
                ParOther oPar = new ParOther();
                oPar.setWrapperNPly(0);     // or >0 together with iterMCTSWrapArr={0}, if testing MaxNWrapper
                pa.setWrapperParams(oPar);

                for (int iterMctseWrap : iterMctseWrapArr) {
                    System.out.println("*** iterMctseWrap="+iterMctseWrap+ " ***");
                    if (iterMctseWrap == 0) qa = pa;
                    else qa = new MctseWrapperAgent(iterMctseWrap, c_puct,
                            new PlayAgentApproximator2(pa),
                            "Mctse-Wrapped " + pa.getName(),
                            maxDepth);
                    if (oPar.getWrapperNPly() > 0) {
                        System.out.println("oPar nPly = " + oPar.getWrapperNPly());
                        qa = new MaxN2Wrapper(pa, oPar.getWrapperNPly(), oPar);
                    }

                    startTime = System.currentTimeMillis();

                    StateObservation so = gb.getDefaultStartState();
                    for (int i=0; i<10; i++) {
                        act_pa = pa.getNextAction2(so,false,true);
                        act_qa = qa.getNextAction2(so,false,true);
                        if (iterMctseWrap==1)
                            assert act_pa.getVBest()==act_qa.getVBest() : "vBest differs for pa and qa";
                        System.out.println("i="+i+": "+so.stringDescr()+
                                ", vBest="+act_pa.getVBest() + " " + act_qa.getVBest()+
                                ", act="+act_pa.toInt()+"="+act_qa.toInt());
                        so.advance(act_pa);
                    }

                    deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
                    elapsedTime += deltaTime;
                    System.out.println("  ");
                }

            } // for (agtFiles)
        } // for (run)

        System.out.println("[oneExpandTest] "+elapsedTime+" sec.");
    }

    /**
     * Test the performance of MCTSWrapperAgent on RubiksCube: We run 200 evaluations for each p=7,8,9
     * and for MCTSWrapper wrapped around 3x3x3_STICKER2_AT/TCL4-p9-2000k-120-7t.agt.zip with nPly=0.
     * The %-solved rates are reported like in QuickEval.
     * Results are written to console and may be copied to .txt for later inspection.
     * Results (%-solved rates) are tested against certain expectations for EPS=1e-08, c_puct=1.0, see HashMap hm.
     * <p>
     * Note that pMin=7 and pMax=9. This is still far away from God's number = 20 for the 3x3x3 cube. Thus, the
     * evaluation results are not yet very satisfactorily (we get not much better than a 65% solved-rate).
     * <p>
     * Computation time depends on iterMCTSWrap and the number of for-loop-passes in rubiksCubeTest. For a single pass
     * with iterMCTSWrap=1000, EPS=1e-08, maxDepth=50 the time is about 550 sec. But it can last also much longer (up to
     * 1500 sec, if the perc-solved-rate goes down (unsuccessful searches take longer)). So be aware that the total
     * test may take considerable time, depending on the settings.
     */
    @Test
    public void wholeEWN3x3Test() {
        scaPar = GBGBatch.setDefaultScaPars("EWN");                // for EWN: 3x3
        agtFiles = new String[]{"tdnt4-10000.agt.zip"};
        int[] iterMCTSWrapArr = {0,100,200,500,1000,2000,5000,10000}; //{0,100,200,500,1000}; //,100,200,300,500,600,800,1000};
        //int[] iterMCTSWrapArr={0};  // only in conjunction with oPar's nPly > 0 (see below)
        int fact=0;   // 1 or 0: whether to invoke lower bounds (1) or not (0)
        HashMap<Integer, Double> hm = new HashMap<>();  // lower bounds of %-solved-rates to expect as a fct of iterMCTSWrap
        hm.put(   0,fact*0.23);                         // Other values: EPS=1e-8, maxDepth=50, c_puct=1.0
        hm.put( 100,fact*0.23);
        hm.put( 200,fact*0.44);
        hm.put( 500,fact*0.65);
        hm.put(1000,fact*0.65);
        int nTrial = 1;
        String csvFile = "mEWN3x3.csv";

        innerEWNTest(scaPar,agtFiles,iterMCTSWrapArr,hm,7,9, nTrial, csvFile);
    }

    /**
     * @param scaPar    scalable parameters
     * @param agtFiles  the agent(s) to wrap
     * @param hm        hash map with expected lower bounds on %-solved-rate as a fct of iterMCTSWrap
     * @param pMin      min number of scrambling twists
     * @param pMax      max number of scrambling twists
     */
    public void innerEWNTest(String[] scaPar,
                                String[] agtFiles,
                                int[] iterMCTSWrapArr,
                                HashMap<Integer,Double> hm,
                                int pMin, int pMax, int nTrial,
                                String csvFile) {
        long startTime;
        double elapsedTime=0,deltaTime;

        selectedGame = "EWN";
        PlayAgent pa;
        PlayAgent qa;
        double[] epsArr = {1e-8}; //{1e-8, 0.0, -1e-8}; // {1e-8, 0.0};    //
        double c_puct=1.0;
        String userTitle1 = "user1", userTitle2 = "user2";
        double userValue1=0.0, userValue2=0.0;
        int maxDepth = -1;  // 25, 50, -1
        boolean doAssert = false;
        double winrate;

        MCompeteMWrap mCompete;
        ArrayList<MCompeteMWrap> mcList = new ArrayList<>();

        arenaTrain = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoardEWN gb = new GameBoardEWN(arenaTrain);		// needed for start state

        for (int run=0; run<nTrial; run++) {
            for (String agtFile : agtFiles) {
                System.out.println("*** Running innerEWNTest for " + agtFile + " (scaPar={" + scaPar[0] + "," + scaPar[1] + "," + scaPar[2] + "}) ***");
                pa = arenaTrain.loadAgent(agtFile);

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

                        // just a check: ExpectimaxN is a perfect agent for 3x3 EWN.
                        // When evaluated against Random (mode=0), we see win rates in [0.86,0.93]
                        // When evaluated against MCTSE-100 (mode=1), we see win rates in [0.45,0.58] (takes time! 55 sec)
                        // When evaluated against MCTSE-10 (mode=2), we see win rates in [0.25,0.65]
                        // Thus, MCTSE-10 (numEpisodes=10) is highly unstable and MCTSE-100 needs some time
                        //qa = new ExpectimaxNAgent("");

                        startTime = System.currentTimeMillis();

                        int mode = 2;
                        EvaluatorEWN m_eval = new EvaluatorEWN(qa, gb, 0, mode, 0);
                        m_eval.eval(qa);
                        winrate = (1+m_eval.getLastResult())/2;
                        System.out.print(m_eval.getPrintString() + ",  winrate="+winrate);
                        if (doAssert && EPS == 1e-8 && c_puct == 1.0)
                            assert winrate > hm.get(iterMCTSWrap) :
                                    "Test failed for iterMCTSWrap = " + iterMCTSWrap +
                                            ": winrate=" + winrate + ", but threshold = "+hm.get(iterMCTSWrap);
                        mCompete = new MCompeteMWrap(run, 1, 0, iterMCTSWrap,
                                EPS, 0, c_puct, winrate,
                                userValue1, userValue2);
                        mcList.add(mCompete);

                        deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
                        elapsedTime += deltaTime;
                        System.out.println("    for EPS=" + EPS + ", iter=" + iterMCTSWrap + ", " + deltaTime + " sec");
                    }

                    // print the full list mcList after finishing each  (iterMCTSWrap)
                    // (overwrites the file written from previous (iterMCTSWrap))
                    MCompeteMWrap.printMultiCompeteList(csvFile, mcList, pa, arenaTrain, userTitle1, userTitle2);
                } // for (EPS)
            } // for (agtFiles)
        } // for (run)

        System.out.println("[innerEWNTest] "+elapsedTime+" sec.");
    }


}
