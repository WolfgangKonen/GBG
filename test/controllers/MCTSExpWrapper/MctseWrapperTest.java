package controllers.MCTSExpWrapper;

import controllers.*;
import controllers.MC.MCAgentN;
import controllers.MCTSExpWrapper.stateApproximation2.PlayAgentApproximator2;
import controllers.MCTSWrapper.ConfigWrapper;
import games.Arena;
import games.EWN.EvaluatorEWN;
import games.EWN.GameBoardEWN;
import games.EWN.StateObserverEWN;
import games.EvalResult;
import games.KuhnPoker.GameBoardKuhnPoker;
import games.KuhnPoker.StateObserverKuhnPoker;
import games.StateObservation;
import games.ZweiTausendAchtundVierzig.GameBoard2048;
import org.junit.Test;
import params.ParMC;
import params.ParOther;
import params.ParWrapper;
import starters.GBGBatch;
import starters.MCompeteMWrap;
import starters.SetupGBG;
import tools.Types;
import tools.Types.ACTIONS;

import java.text.DecimalFormat;
import java.util.*;

public class MctseWrapperTest extends GBGBatch {
    String selectedGame;
    String[] scaPar;
    String[] agtFiles;

    /**
     * Different episodes for {@link #stateEWNTest()}
     */
    enum EpisodeType {CASE_A,CASE_B,CASE_C,CASE_D}

    /**
     * number of states in episode
     */
    int[] kmax = {4,5,4,5};

    static HashMap<String,StateObserverEWN> stateMap = null;

    /**
     * Test the MctseWrapperAgent on 3x3 EWN: We run through a sequence {@code CASE_A}, ... of {@link #kmax} states, e.g.
     * <pre>
     *  [  ] [X2] [  ]   (diceVal:1, availActions: 0501,0502,0504 )
     *  [  ] [X0] [O1]
     *  [  ] [O2] [  ]       </pre>
     * and test whether TDNT4, MCTSE-Wrapper[TDNT4] or ExpectimaxN suggest the right move 0504. We assert that
     * ExpectimaxN, the perfect agent for 3x3 EWN, returns the stored best moves for this sequence.
     * <p>
     * Next, run {@link MctseChanceNode#checkTree(int) checkTree} on MCTSE-Wrapper[TDNT4]: check for consistent visit
     * counts and report number of nodes.
     * <p>
     * Next, run {@link MctseChanceNode#numChilds(Map) numChilds} on MCTSE-Wrapper[TDNT4]: if a MctseExpecNode has
     * enough visit counts (empirically: at least 4*maxNumChildren), is then every child formed, i.e. visited at
     * least once? This test has no guarantee to succeed, it may fail with low probability.
     * <p>
     * Finally, we report the number of correct decisions that the wrapped agent made.
     */
    @Test
    public void stateEWNTest() {
        String agtFile = "tdnt4-10000.agt.zip";
        int[] iterMctseWrapArr = {1000}; //{100,500,1000}; //{0,100,200,500,1000}; //,100,200,300,500,600,800,1000};
        int nTrial = 1;
        EpisodeType m_epi = EpisodeType.CASE_A;     // CASE_A   CASE_B    CASE_C   CASE_D

        long startTime;
        double elapsedTime=0,deltaTime;

        PlayAgent pa,qa,ra;
        double c_puct=0.5;
        int maxDepth = -1;
        Types.ACTIONS_VT act_pa, act_qa, act_ra;
        StateObserverEWN so;

        scaPar = SetupGBG.setDefaultScaPars("EWN");                // for EWN: 3x3
        arenaTrain = SetupGBG.setupSelectedGame("EWN",scaPar,"",false,true);

        pa = arenaTrain.loadAgent(agtFile);
        ra = new ExpectimaxNAgent("",15);
        int countWrapCorrect = 0;
        System.out.println("*** Running stateEWNTest for " + agtFile + " (scaPar={" + scaPar[0] + "," + scaPar[1] + "," + scaPar[2] + "}) ***");

//        ParOther oPar = new ParOther();
//        oPar.setWrapperNPly(0);     // or >0 together with iterMCTSWrapArr={0}, if testing MaxNWrapper
//        pa.setWrapperParams(oPar);

        for (int run=0; run<nTrial; run++) {
            for (int k=0; k<kmax[m_epi.ordinal()]; k++) {   // steps of the sequence
                System.out.println("+++ k="+k+": +++");

                for (int iterMctseWrap : iterMctseWrapArr) {
                    System.out.println("*** iterMctseWrap="+iterMctseWrap+ " ***");
                    if (iterMctseWrap == 0) qa = pa;
                    else qa = new MctseWrapperAgent(iterMctseWrap, c_puct,
                            new PlayAgentApproximator2(pa),
                            "Mctse-wrapped " + pa.getName(),
                            maxDepth,new ParOther());

                    startTime = System.currentTimeMillis();

                    // build the state to examine
                    so = switch (m_epi) {
                        case CASE_A -> buildStateA(arenaTrain, k);
                        case CASE_B -> buildStateB(arenaTrain, k);
                        case CASE_C -> buildStateC(arenaTrain, k);
                        case CASE_D -> buildStateD(arenaTrain, k);
                    };

                    for (int i=0; i<1; i++) {
                        // ---- best move test ----
                        act_pa = pa.getNextAction2(so,false, false, true);
                        act_qa = qa.getNextAction2(so,false, false, true);
                        act_ra = ra.getNextAction2(so,false, false, false);
                        if (iterMctseWrap==1)
                            assert act_pa.getVBest()==act_qa.getVBest() : "vBest differs for pa and qa";
                        DecimalFormat form = new DecimalFormat("0.000");
                        double[] vtab = act_ra.getVTable();
                        System.out.print("i="+i+": "+so.stringDescr()+"[vTab Expectimax] ");
                        for (double v : vtab) System.out.print(form.format(v)+" ");
                        System.out.println(
                                "\n[3 agts] vBest=("+form.format(act_pa.getVBest()) + " " + form.format(act_qa.getVBest()) + " " + form.format(act_ra.getVBest())+")"+
                                ", act_TD="+act_pa.toInt()+", act_Wrap="+act_qa.toInt()+", act_Expec="+act_ra.toInt());
                        int bestAct = so.getStoredActBest().toInt();
                        assert act_ra.toInt() == bestAct : "Mismatch: act_Expec="+act_ra.toInt()+" != bestAct="+ bestAct;
                        if (act_ra.toInt()==act_qa.toInt()) countWrapCorrect++;

                        // ---- checkTree test ----
                        int numNodes = ((MctseWrapperAgent) qa).getRootNode().checkTree(iterMctseWrap-1);
                        // why iterMctseWrap-1? - The first call expands, does not increment any visit count
                        System.out.println("[checkTree] numNodes="+numNodes);
                        if (!ConfigExpWrapper.DO_EXPECTIMAX_EXPAND)
                            System.out.println("[checkTree] Assertion on numVisits succeeded for every branch");

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
                            countsHisto += sum(vals);
                            double[] vals2 = vals.clone();
                            vals2[vals.length-1]=0;  // we delete the highest bin count, then all entries should be 0.0
                            if (numVisits > 4*vals.length) {
                                assert sum(vals2)==0 :
                                        "[EWNTest] Unexpected histo-entry: numVisits="+numVisits+", histo="+printString(vals);
                            }
                        }
                        assert numExpec == countsHisto :
                                "[EWNTest] numExpec="+numExpec+" and countsHisto="+countsHisto+" differ!";
                        System.out.println("[numChilds] numExpecNodes="+numExpec+","+countsHisto);
                        System.out.println("[numChilds] Number of EXPECTIMAX nodes equal to sum of counts in histogram");

                    } // for (i)

                    deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
                    elapsedTime += deltaTime;
                    System.out.println("  ");
                }

            } // for (k)
        } // for (run)

        System.out.println("[stateEWNTest] Wrapped agent had "+countWrapCorrect+" from "+kmax[m_epi.ordinal()]+" decisions correct");
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
        StateObserverEWN so = (StateObserverEWN) gb.getDefaultStartState(null);
        so.setNextActionDeterministic(ACTIONS.fromInt(1));      // bestAction X: 0307, V=+0.827
        int[] bestAction = new int []{307,804,4,504};

        if (k>0) {
            so.advanceDeterministic(ACTIONS.fromInt(304));  // X: from 3 to 4
            so.advanceNondetSpecific(ACTIONS.fromInt(0));     // bestAction O: 0804, V=+5/9
        }
        if (k>1) {
            so.advanceDeterministic(ACTIONS.fromInt(804));  // O: from 8 to 4
            so.advanceNondetSpecific(ACTIONS.fromInt(0));     // bestAction X: 0004, V=-5/9
        }
        if (k>2) {
            so.advanceDeterministic(ACTIONS.fromInt(4));    // X: from 0 to 4
            so.advanceNondetSpecific(ACTIONS.fromInt(1));     // bestAction O: 0504, V=+1/3
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
        StateObserverEWN so = (StateObserverEWN) gb.getDefaultStartState(null);
        so.setNextActionDeterministic(ACTIONS.fromInt(0));      // bestAction X: 0004, V=-0.235
        int[] bestAction = new int []{4,804,105,704,307};

        if (k>0) {
            so.advanceDeterministic(ACTIONS.fromInt(1));    // X: from 0 to 1
            so.advanceNondetSpecific(ACTIONS.fromInt(0));     // bestAction O: 0804, V=-1/9
        }
        if (k>1) {
            so.advanceDeterministic(ACTIONS.fromInt(805));   // O: from 8 to 5
            so.advanceNondetSpecific(ACTIONS.fromInt(0));     // bestAction X: 0105, V=+1
        }
        if (k>2) {
            so.advanceDeterministic(ACTIONS.fromInt(104));   // X: from 1 to 4
            so.advanceNondetSpecific(ACTIONS.fromInt(2));     // bestAction O: 0704, V=+1/3
            /* Now we have the state (O moves)
                [  ] [  ] [  ]     (diceVal:2,   availActions: 0706,0704,0703 )
                [X1] [X0] [O0]
                [  ] [O2] [  ]     The optimal action is 0704 (V=1.0)
             */
        }
        if (k>3) {
            so.advanceDeterministic(ACTIONS.fromInt(704));   // O: from 7 to 4
            so.advanceNondetSpecific(ACTIONS.fromInt(0));     // bestAction X: 0307
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

    /**
     * Same as {@link #buildStateA(Arena, int)}, but for another episode C.
     */
    private StateObserverEWN buildStateC(Arena arenaEWN, int k) {
        GameBoardEWN gb = new GameBoardEWN(arenaEWN);
        StateObserverEWN so = (StateObserverEWN) gb.getDefaultStartState(null);
        so.setNextActionDeterministic(ACTIONS.fromInt(1));      // bestAction X: 0307, V=+0.827
        int[] bestAction = new int []{307,504,408,805,4};

        if (k>0) {
            so.advanceDeterministic(ACTIONS.fromInt(304));    // X: from 3 to 4
            so.advanceNondetSpecific(ACTIONS.fromInt(1));     // bestAction O: 0504, V=+0.58
        }
        if (k>1) {
            so.advanceDeterministic(ACTIONS.fromInt(501));   // O: from 5 to 1
            so.advanceNondetSpecific(ACTIONS.fromInt(1));     // bestAction X: 0408, V=+1
        }
        if (k>2) {
            so.advanceDeterministic(ACTIONS.fromInt(405));   // X: from 4 to 5
            so.advanceNondetSpecific(ACTIONS.fromInt(0));     // bestAction O: 0805, V=+1
            /* Now we have the state (O moves)
                [X0] [O1] [  ]     (diceVal:2,   availActions: 0706,0704,0703 )
                [  ] [  ] [X1]
                [  ] [O2] [O0]     The optimal action is 0805 (V=1.0)
             */
        }
        if (k>3) {
            so.advanceDeterministic(ACTIONS.fromInt(804));   // O: from 8 to 4
            so.advanceNondetSpecific(ACTIONS.fromInt(0));     // bestAction X: 0004
            /* Now we have the state (X moves)
                [X0] [O1] [  ]     (diceVal:0,   availActions: 1,3,4 )
                [  ] [O0] [X1]
                [  ] [O2] [  ]     The optimal action is 4 (V=0.3)
             */
        }
//          so.advanceDeterministic(ACTIONS.fromInt(307));   // X: from 3 to 7
//          so.advanceNondeterministic(ACTIONS.fromInt(0));     // bestAction O: none, O looses
        so.storeBestActionInfo(new Types.ACTIONS_VT(bestAction[k]));
        return so;
    }

    /**
     * Same as {@link #buildStateA(Arena, int)}, but for another episode D.
     */
    private StateObserverEWN buildStateD(Arena arenaEWN, int k) {
        GameBoardEWN gb = new GameBoardEWN(arenaEWN);
        StateObserverEWN so = (StateObserverEWN) gb.getDefaultStartState(null);
        so.setNextActionDeterministic(ACTIONS.fromInt(2));      // bestAction X: 0105, V=+0.48
        int[] bestAction = new int []{105,703,205,805,304};

        if (k>0) {
            so.advanceDeterministic(ACTIONS.fromInt(102));    // X: from 1 to 2, suboptimal, V=-0.09
            so.advanceNondetSpecific(ACTIONS.fromInt(2));     // bestAction O: 0703, V=+0.39
        }
        if (k>1) {
            so.advanceDeterministic(ACTIONS.fromInt(704));   // O: from 7 to 4, suboptimal
            so.advanceNondetSpecific(ACTIONS.fromInt(2));     // bestAction X: 0205, V=+1
        }
        if (k>2) {
            so.advanceDeterministic(ACTIONS.fromInt(205));   // X: from 2 to 5
            so.advanceNondetSpecific(ACTIONS.fromInt(0));     // bestAction O: 0805, V=+1
        }
        if (k>3) {
            so.advanceDeterministic(ACTIONS.fromInt(805));   // O: from 8 to 5
            so.advanceNondetSpecific(ACTIONS.fromInt(2));     // bestAction X: 304
            /* Now we have the state (X moves)
                [X0] [  ] [  ]     (diceVal:2,   availActions: 304,306,307 )
                [X1] [O2] [O0]
                [  ] [  ] [  ]     The optimal action is 304 (V=1.0)
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

        arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);   // t_Game is ArenaTrain object
        GameBoardKuhnPoker gb = new GameBoardKuhnPoker(arenaTrain);		// needed for chooseStartState()

        for (int run=0; run<nTrial; run++) {
            ParMC mcPar = new ParMC();
            pa = new MCAgentN(mcPar);
            //pa = new RandomAgent("");
            pa = new ExpectimaxNAgent("ExpectimaxN",6);

            for (int iterMctseWrap : iterMctseWrapArr) {
                System.out.println("*** iterMctseWrap="+iterMctseWrap+ " ***");
                if (iterMctseWrap == 0) qa = pa;
                else qa = new MctseWrapperAgent(iterMctseWrap, c_puct,
                        new PlayAgentApproximator2(pa),
                        "Mctse-wrapped " + pa.getName(),
                        maxDepth,new ParOther());

                startTime = System.currentTimeMillis();

                for (int i=0; i<10; i++) {
                    // The actions are 0=FOLD, 1=CHECK, 2=BET, 3=CALL
                    // build the state to examine
                    StateObserverKuhnPoker so = (StateObserverKuhnPoker) gb.getDefaultStartState(null);
                    // best action for p0 is {1,2} if p0=J, {2} if p0=Q, {1,2} if p0=K

                    so.advance(new ACTIONS(1), null);   // p0 plays CHECK
                    // best action for p1 is {1,2} if p1=J, {2} if p1=Q, {2} if p1=K

                    //so.advance(new ACTIONS(1));   // p0 plays BET
                    // best action for p1 is {0} if p1=J, {0,3} if p1=Q, {3} if p1=K

                    act_pa = pa.getNextAction2(so.partialState(),false, false, true);
                    act_qa = qa.getNextAction2(so.partialState(),false, false, true);
                    if (iterMctseWrap==1)
                        assert act_pa.getVBest()==act_qa.getVBest() : "vBest differs for pa and qa";
                    System.out.println("i="+i+": "+so.stringDescr()+
                            ", vBest_(pa,qa)=("+act_pa.getVBest() + ", " + act_qa.getVBest()+
                            "), act_pa="+act_pa.toInt()+", act_qa="+act_qa.toInt());
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

        long startTime;
        double elapsedTime=0,deltaTime;

        selectedGame = "2048";
        PlayAgent pa, qa;
        double c_puct=1.0;
        int maxDepth = -1;
        Types.ACTIONS_VT act_pa, act_qa;


        arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);   // t_Game is ArenaTrain object
        GameBoard2048 gb = new GameBoard2048(arenaTrain);		// needed for chooseStartState()

        for (int run=0; run<nTrial; run++) {
            for (String agtFile : agtFiles) {
                System.out.println("*** Running oneExpandTest for " + agtFile + " (scaPar={" + scaPar[0] + "," + scaPar[1] + "," + scaPar[2] + "}) ***");

                pa = arenaTrain.loadAgent(agtFile);
                ParOther oPar = new ParOther();
                ParWrapper wrPar = new ParWrapper();
                wrPar.setWrapperNPly(0);     // or >0 together with iterMCTSWrapArr={0}, if testing MaxNWrapper
                pa.setWrapperParamsOfromWr(wrPar);

                for (int iterMctseWrap : iterMctseWrapArr) {
                    System.out.println("*** iterMctseWrap="+iterMctseWrap+ " ***");
                    if (iterMctseWrap == 0) qa = pa;
                    else qa = new MctseWrapperAgent(iterMctseWrap, c_puct,
                            new PlayAgentApproximator2(pa),
                            "Mctse-wrapped " + pa.getName(),
                            maxDepth,oPar);
                    if (wrPar.getWrapperNPly() > 0) {
                        System.out.println("wrPar nPly = " + wrPar.getWrapperNPly());
                        qa = new ExpectimaxN2Wrapper(pa, wrPar.getWrapperNPly());
                    }

                    startTime = System.currentTimeMillis();

                    StateObservation so = gb.getDefaultStartState(null);
                    for (int i=0; i<10; i++) {
                        act_pa = pa.getNextAction2(so,false, false, true);
                        act_qa = qa.getNextAction2(so,false, false, true);
                        if (iterMctseWrap==1)
                            assert act_pa.getVBest()==act_qa.getVBest() : "vBest differs for pa and qa";
                        System.out.println("i="+i+": "+so.stringDescr()+
                                ", vBest="+act_pa.getVBest() + " " + act_qa.getVBest()+
                                ", act="+act_pa.toInt()+"="+act_qa.toInt());
                        so.advance(act_pa, null);
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
     * Test the performance of MCTSEWrapperAgent on 3x3 EWN: We perform {@code nTrial} evaluation runs with different
     * MCTSE iterations {@code iterMCTSWrapArr} for an MCTSEWrapper wrapped around agent {@code tdnt4-10000.agt.zip}.
     * <p>
     * The wrapped agent is either evaluated with {@link EvaluatorEWN} (if {@code mode=0,1,2}) or with
     * {@link #precision3x3EWNStates(PlayAgent, PlayAgent, GameBoardEWN)}  precision3x3EWNStates} (if {@code mode=10}).
     * <p>
     * Results are written to console and to {@code csvFile}.
     * <p>
     * Computation time depends on iterMCTSWrap, mode and nTrial. For {@code mode=10},
     * {@code nTrial=1} and {@code iterMCTSWrapArr = {0,100,200,500,1000,2000,5000}} we need 25 sec.
     */
    @Test
    public void wholeEWN3x3Test() {
        scaPar = GBGBatch.setDefaultScaPars("EWN");                // for EWN: 3x3
        agtFiles = new String[]{"tdnt4-10000.agt.zip"};
        int[] iterMCTSWrapArr = {0,100,200,500,1000,2000,5000}; //{0,100,200,500,1000}; //,100,200,300,500,600,800,1000};
        //int[] iterMCTSWrapArr={0};  // only in conjunction with oPar's nPly > 0 (see below)
        //int[] iterMCTSWrapArr = {500}; //{0,100,200,500,1000}; //,100,200,300,500,600,800,1000};
        int fact=1;   // 1 or 0: whether to invoke lower bounds (1) or not (0)
        HashMap<Integer, Double> hm = new HashMap<>();  // lower bounds of precision to expect as a fct of iterMCTSWrap
        for (int iter : iterMCTSWrapArr) {              // Other values: mode=10, EPS=1e-8, c_puct=1.0
            hm.put(iter,fact*0.90);
        }
        int nTrial = 1;
        int mode = 10;
        String csvFile = "mEWN3x3.csv";

        innerEWNTest(scaPar,agtFiles,iterMCTSWrapArr,hm,nTrial,mode, csvFile);
    }

    /**
     * Similar to {@link #wholeEWN3x3Test()}, but for 5x5 EWN
     */
    @Test
    public void wholeEWN5x5Test() {
        scaPar = new String[]{"5x5 2-Player","[0,1],[2,3],[4,5]","False"};
        agtFiles = new String[]{"tcl4-100000.agt.zip"};
        int[] iterMCTSWrapArr = {0,100,200,500,1000,2000,5000}; //{0,100,200,500,1000}; //,100,200,300,500,600,800,1000};
        //int[] iterMCTSWrapArr={0};  // only in conjunction with oPar's nPly > 0 (see below)
        int fact=0;   // 1 or 0: whether to invoke lower bounds (1) or not (0)
        HashMap<Integer, Double> hm = new HashMap<>();  // lower bounds of precision to expect as a fct of iterMCTSWrap
        for (int iter : iterMCTSWrapArr) {
            hm.put(iter,fact*0.90);                     // Other values: mode=10, EPS=1e-8, c_puct=1.0
        }
        int nTrial = 1;
        int mode = 2;
        String csvFile = "mEWN5x5.csv";

        innerEWNTest(scaPar,agtFiles,iterMCTSWrapArr,hm,nTrial,mode, csvFile);
    }

    /**
     * @param scaPar    scalable parameters
     * @param agtFiles  the agent(s) to wrap
     * @param iterMCTSWrapArr   array of MCTSE iterations
     * @param hm        hash map with expected lower bounds on %-solved-rate as a fct of iterMCTSWrap
     * @param nTrial    number of runs
     * @param csvFile   where to write evaluation results
     */
    public void innerEWNTest(String[] scaPar,
                                String[] agtFiles,
                                int[] iterMCTSWrapArr,
                                HashMap<Integer,Double> hm,
                                int nTrial, int mode,
                                String csvFile) {
        long startTime;
        double elapsedTime=0,deltaTime;

        PlayAgent pa,qa;
        double[] epsArr = {1e-8}; //{1e-8, 0.0, -1e-8}; // {1e-8, 0.0};    //
        double c_puct=5.0; //1.0; //5.0; //
        String userTitle1 = "time", userTitle2 = "DO_EEXPAND";
        DecimalFormat frm = new DecimalFormat("0.0000");
        double userValue2=(ConfigExpWrapper.DO_EXPECTIMAX_EXPAND ? 1 : 0);
        int maxDepth = -1;  // 25, 50, -1
        boolean doAssert = true;
        double winrate;

        MCompeteMWrap mCompete;
        ArrayList<MCompeteMWrap> mcList = new ArrayList<>();

        arenaTrain = SetupGBG.setupSelectedGame("EWN",scaPar,"",false,true);
        GameBoardEWN gb = new GameBoardEWN(arenaTrain);		// needed for start state

        for (int run=0; run<nTrial; run++) {
            for (String agtFile : agtFiles) {
                System.out.println("*** Running innerEWNTest for " + agtFile
                        + " (scaPar={" + scaPar[0] + "," + scaPar[1] + "," + scaPar[2] + "}), run "+run+" ***");
                pa = arenaTrain.loadAgent(agtFile);
                //pa = new RandomAgent("");  // just a debug check
                
//                ParMCTSE params = new ParMCTSE();   // another debug check
//                params.setNumIter(500);
//                pa = new MCTSExpectimaxAgt("",params);

                ParOther oPar = new ParOther();
                ParWrapper wrPar = new ParWrapper();
                wrPar.setWrapperNPly(0);     // or >0 together with iterMCTSWrapArr={0}, if testing MaxNWrapper
                pa.setWrapperParamsOfromWr(wrPar);

                for (double EPS : epsArr) {
                    ConfigWrapper.EPS = EPS;
                    for (int iterMCTSWrap : iterMCTSWrapArr) {
                        if (iterMCTSWrap == 0) qa = pa;
                        else qa = new MctseWrapperAgent(iterMCTSWrap, c_puct,
                                new PlayAgentApproximator2(pa),
                                "Mctse-wrapped " + pa.getName(),
                                maxDepth,oPar);
                        if (wrPar.getWrapperNPly() > 0) {
                            System.out.println("wrPar nPly = " + wrPar.getWrapperNPly());
                            qa = new ExpectimaxN2Wrapper(pa, wrPar.getWrapperNPly());
                        }

                        // just a check (only for 3x3 EWN): ExpectimaxN with nply=15 is a perfect agent for 3x3 EWN.
                        // When evaluated against Random (mode=0), we see win rates in [0.86,0.93]
                        // When evaluated against MCTSE-1000, 100epi (mode=1), we see win rates in [0.50,0.59] (takes time! 15 sec)
                        // When evaluated against MCTSE-1000, 10epi (mode=2), we see win rates in [0.35,0.65]
                        // Thus, MCTSE 10epi (numEpisodes=10) is highly unstable and MCTSE 100epi needs some time
                        //qa = new ExpectimaxNAgent("",15);

                        startTime = System.currentTimeMillis();

                        if (mode<10) {
                            EvaluatorEWN m_eval = new EvaluatorEWN(qa, gb, mode, 0);
                            EvalResult eRes = m_eval.eval(qa);
                            winrate = (1+eRes.getResult())/2;
                            System.out.print(m_eval.getPrintString() + ",  winrate="+frm.format(winrate));
                        } else {
                            assert scaPar[0].equals("3x3 2-Player") : "[innerEWNTest] Error: mode>=10 only viable for 3x3 EWN";
                            winrate = precision3x3EWNStates(pa,qa,gb);
                            System.out.print("Percent of states with correct decision: "+frm.format(winrate));
                        }
                        if (doAssert && EPS == 1e-8 && c_puct == 1.0)
                            assert winrate >= hm.get(iterMCTSWrap) :
                                    "Test failed for iterMCTSWrap = " + iterMCTSWrap +
                                            ": winrate=" + frm.format(winrate) + ", but threshold = "+hm.get(iterMCTSWrap);
                        deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
                        mCompete = new MCompeteMWrap(run,agtFile, 1, 0, iterMCTSWrap,
                                EPS, 0, c_puct, winrate,
                                deltaTime, userValue2);
                        mcList.add(mCompete);

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

    /**
     * Helper method for {@link #wholeEWN3x3Test()}:
     * Check whether agents {@code pa} and {@code qa} return correct decisions on a number of {@code numStates=1000} 3x3 EWN states.
     * <p>
     * This check works only for 3x3 EWN because only there we have a perfect benchmark agent ExpectimaxN. (For 5x5 EWN
     * or larger, an attempt to run Expectimax leads to exploding mem-size and run time.)
     * @param pa    the (unwrapped) agent
     * @param qa    the (wrapped) agent
     * @param gb    needed for default start state
     * @return      the precision, i.e. the rate of correct decisions of agent {@code qa}
     */
    private double precision3x3EWNStates(PlayAgent pa, PlayAgent qa, GameBoardEWN gb) {
        PlayAgent ea = new ExpectimaxNAgent("",15);
        int s=0, cp=0, cq=0, fpq=0, numStates=1000;
        int iterMctseWrap = qa.getParWrapper().getWrapperMCTS_iterations();
        Types.ACTIONS_VT act_pa,act_qa,act_ea;
        //Random rand = new Random(System.currentTimeMillis());
        Random rand = new Random(42);
        DecimalFormat frm = new DecimalFormat("0.0000");

        if (stateMap==null) stateMap = createStateMap(numStates, rand, gb);

        for (StateObserverEWN so : stateMap.values()) {
            ArrayList<ACTIONS> actlist = so.getAvailableActions();
            act_ea=ea.getNextAction2(so,false, false, true);
            act_pa=pa.getNextAction2(so,false, false, true);
            act_qa=qa.getNextAction2(so,false, false, true);
            int ind_pa = where(actlist,act_pa);
            int ind_qa = where(actlist,act_qa);
            double[] vtab = act_ea.getVTable();
            final var vmax = Arrays.stream(vtab).max().orElse(Double.NaN);
            boolean pcorrect = (vtab[ind_pa]==vmax);
            boolean qcorrect = (vtab[ind_qa]==vmax);
            if (pcorrect) cp++;
            if (qcorrect) cq++;
            if (!pcorrect && !qcorrect) {
//                System.out.println(so+"act_pa="+act_pa.toInt()+", act_qa="+act_qa.toInt()+", act_ea="+act_ea.toInt());
//                System.out.print("vtab_ea: "); for (double v : vtab) System.out.print(frm.format(v)+" "); System.out.println();
//
//                int numNodes = ((MctseWrapperAgent) qa).getRootNode().checkTree(iterMctseWrap-1);
//                // why iterMctseWrap-1? - The first call expands, does not increment any visit count
//                System.out.println("[checkTree] numNodes="+numNodes);
                fpq++;
            }
//            else {
//                System.out.println(so.stringDescr()+"act_pa:"+act_pa.toInt()+", act_qa:"+act_qa.toInt()+", act_ea:"+act_ea.toInt());
//                int dummy=1;
//            }
            s++;
        }

        // report fp = the number of pa's false decisions
        //        fq = the number of pa's false decisions
        //        fpq= the number of states where both, pa and qa deliver a false decision
        int fp = s-cp;
        int fq = s-cq;
        System.out.println("fp,fq,fpq = "+fp+", "+fq+", "+fpq);

        double precision = ((double)cq)/s;
        return precision;
    }


    /**
     * Helper method for {@link #precision3x3EWNStates(PlayAgent, PlayAgent, GameBoardEWN) precision3x3EWNStates}:
     * Create a {@link HashMap} with {@code numStates} different EWN states.
     * <p>
     * We use a {@link HashMap} here and not a {@link List}, because we want to ensure <i>different</i> states.
     * Two states are considered different if their {@link StateObserverEWN#stringDescr()} differs.
     * @param numStates desired size
     * @param rand      RNG
     * @param gb        needed for default start state
     * @return
     */
    private HashMap<String, StateObserverEWN> createStateMap(int numStates, Random rand, GameBoardEWN gb) {
        int diceVal = 0;
        stateMap = new HashMap<>();
        StateObserverEWN so = (StateObserverEWN) gb.getDefaultStartState(null);
        so.advanceNondetSpecific(new ACTIONS(diceVal));
        while (stateMap.size()<numStates) {
            while (!so.isGameOver()) {
                StateObserverEWN ewnState = new StateObserverEWN(so); // make a copy
                stateMap.putIfAbsent(ewnState.stringDescr(), ewnState);
                ArrayList<ACTIONS> actlist = so.getAvailableActions();
                if (stateMap.size() >= numStates)
                    return stateMap;

                // perform a random deterministic action + a random nondeterministic action
                ACTIONS act = actlist.get(rand.nextInt(actlist.size()));
                so.advanceDeterministic(act);
                ACTIONS ranact = new ACTIONS(rand.nextInt(3));
                so.advanceNondetSpecific(ranact);
            }
            // start a new episode
            diceVal++;
            diceVal = diceVal % 3;
            so = (StateObserverEWN) gb.getDefaultStartState(null);
            so.advanceNondetSpecific(new ACTIONS(diceVal));
        }
        System.err.println("we should not get here (the normal return is above)");
        return stateMap;
    }

    // --- not needed, now done via Arrays.stream(vtab).max() ---
//    private double maximum(double[] vtab) {
//        double vmax = -Double.MAX_VALUE;
//        for (double v : vtab) if (v>vmax) vmax=v;
//        return vmax;
//    }

    private int where(ArrayList<ACTIONS> actlist, ACTIONS act_qa) {
        int ind_qa=-1;
        for (int i=0; i<actlist.size(); i++)
            if (act_qa.toInt()==actlist.get(i).toInt()) { ind_qa=i; break; }
        return ind_qa;
    }
}
