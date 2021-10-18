package controllers.MCTSExpWrapper;

import controllers.ExpectimaxNAgent;
import controllers.MC.MCAgentN;
import controllers.MCTSExpWrapper.stateApproximation2.PlayAgentApproximator2;
import controllers.MaxN2Wrapper;
import controllers.PlayAgent;
import games.EWN.GameBoardEWN;
import games.EWN.StateObserverEWN;
import games.KuhnPoker.GameBoardKuhnPoker;
import games.KuhnPoker.StateObserverKuhnPoker;
import games.StateObservation;
import games.ZweiTausendAchtundVierzig.GameBoard2048;
import org.junit.Test;
import params.ParMC;
import params.ParOther;
import starters.GBGBatch;
import tools.Types;
import tools.Types.ACTIONS;

import java.util.HashMap;
import java.util.Map;

public class MctseWrapperTest extends GBGBatch {
    String selectedGame;
    String[] scaPar;
    String[] agtFiles;

    /**
     * Test the MctseWrapperAgent on EWN: We build a state
     * <pre>
     *  [  ] [X2] [  ]   (diceVal:1, availActions: 0501,0502,0504 )
     *  [  ] [X0] [O1]
     *  [  ] [O2] [  ]       </pre>
     * and test whether TDNT4, MCTSE-Wrapper[TDNT4] or ExpectimaxN suggest the right move 0504.
     * <p>
     * Next, run checkTree on MCTSE-Wrapper[TDNT4]: check for consistent visit counts and report number of nodes.
     * Finally, run numChilds on MCTSE-Wrapper[TDNT4]: if a MctseExpecNode has enough visit counts (empirically
     * 4*maxNumChildren), is then every child formed, i.e. visited at least once?:
     */
    @Test
    public void stateEWNTest() {
        scaPar=new String[]{"", "", ""};
        agtFiles = new String[]{"tdnt4-10000.agt.zip"};
        int[] iterMctseWrapArr = {1000}; //{0,100,200,500,1000}; //,100,200,300,500,600,800,1000};
        int nTrial = 1;
        String csvFile = "dummy.csv";

        long startTime;
        double elapsedTime=0,deltaTime;

        selectedGame = "EWN";
        PlayAgent pa,qa,ra;
        double c_puct=500.0;
        int maxDepth = -1;
        Types.ACTIONS_VT act_pa, act_qa, act_ra;


        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoardEWN gb = new GameBoardEWN(t_Game);		// needed for chooseStartState()

        for (int run=0; run<nTrial; run++) {
            for (String agtFile : agtFiles) {
                setupPaths(agtFile, csvFile);     // builds filePath
                boolean res = t_Game.loadAgent(0, filePath);
                assert res : "\n[MctseWrapperTest] Aborted: agtFile = " + agtFile + " not found!";
                System.out.println("*** Running stateEWNTest for " + agtFile + " (scaPar={" + scaPar[0] + "," + scaPar[1] + "," + scaPar[2] + "}) ***");

                String sAgent = t_Game.m_xab.getSelectedAgent(0);
                pa = t_Game.m_xfun.fetchAgent(0, sAgent, t_Game.m_xab);
                ParOther oPar = new ParOther();
                oPar.setWrapperNPly(0);     // or >0 together with iterMCTSWrapArr={0}, if testing MaxNWrapper
                pa.setWrapperParams(oPar);
                ra = new ExpectimaxNAgent("");

                for (int iterMctseWrap : iterMctseWrapArr) {
                    System.out.println("*** iterMctseWrap="+iterMctseWrap+ " ***");
                    if (iterMctseWrap == 0) qa = pa;
                    else qa = new MctseWrapperAgent(iterMctseWrap, c_puct,
                            new PlayAgentApproximator2(pa),
                            "Mctse-Wrapped " + pa.getName(),
                            maxDepth);

                    startTime = System.currentTimeMillis();

                    // build the state to examine
                    StateObserverEWN so = (StateObserverEWN) gb.getDefaultStartState();
                    so.setNextActionDeterministic(ACTIONS.fromInt(1));  // bestAction X: 0307
//                    so.advanceDeterministic(ACTIONS.fromInt(304));  // X: from 3 to 4
//                    so.advanceNondeterministic(ACTIONS.fromInt(0));     // bestAction O: 0804
//                    so.advanceDeterministic(ACTIONS.fromInt(804));  // O: from 8 to 4
//                    so.advanceNondeterministic(ACTIONS.fromInt(0));     // bestAction X: 0004
//                    so.advanceDeterministic(ACTIONS.fromInt(4));    // X: from 0 to 4
//                    so.advanceNondeterministic(ACTIONS.fromInt(1));     // bestAction O: 0504
/* Now we have the state
        [  ] [X2] [  ]     (diceVal:1,   availActions: 0501,0502,0504 )
        [  ] [X0] [O1]
        [  ] [O2] [  ]
   The optimal action is 0504
 */

                    for (int i=0; i<2; i++) {
                        act_pa = pa.getNextAction2(so,false,true);
                        act_qa = qa.getNextAction2(so,false,true);
                        act_ra = ra.getNextAction2(so,false,false);
                        if (iterMctseWrap==1)
                            assert act_pa.getVBest()==act_qa.getVBest() : "vBest differs for pa and qa";
                        System.out.println("i="+i+": "+so.stringDescr()+
                                ", vBest="+act_pa.getVBest() + " " + act_qa.getVBest()+
                                ", act_TD="+act_pa.toInt()+", act_Wrap="+act_qa.toInt()+", act_Expec="+act_ra.toInt());
                        //so.advance(act_pa);

                        int numNodes = ((MctseWrapperAgent) qa).getRootNode().checkTree(iterMctseWrap-1);
                        // why iterMctseWrap-1? - The first call expands, does not increment any visit count
                        System.out.println("[checkTree] numNodes="+numNodes);

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

            } // for (agtFiles)
        } // for (run)

        System.out.println("[stateEWNTest] "+elapsedTime+" sec.");
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

        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoardKuhnPoker gb = new GameBoardKuhnPoker(t_Game);		// needed for chooseStartState()

        for (int run=0; run<nTrial; run++) {
            ParMC mcPar = new ParMC();
            pa = new MCAgentN(mcPar);
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

                startTime = System.currentTimeMillis();

                // build the state to examine
                StateObserverKuhnPoker so = (StateObserverKuhnPoker) gb.getDefaultStartState();

                for (int i=0; i<10; i++) {
                    act_pa = pa.getNextAction2(so,false,true);
                    act_qa = qa.getNextAction2(so,false,true);
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
        PlayAgent pa;
        PlayAgent qa;
        double c_puct=1.0;
        int maxDepth = -1;
        Types.ACTIONS_VT act_pa, act_qa;


        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoard2048 gb = new GameBoard2048(t_Game);		// needed for chooseStartState()

        for (int run=0; run<nTrial; run++) {
            for (String agtFile : agtFiles) {
                setupPaths(agtFile, csvFile);     // builds filePath
                boolean res = t_Game.loadAgent(0, filePath);
                assert res : "\n[MctseWrapperTest] Aborted: agtFile = " + agtFile + " not found!";
                System.out.println("*** Running oneExpandTest for " + agtFile + " (scaPar={" + scaPar[0] + "," + scaPar[1] + "," + scaPar[2] + "}) ***");

                String sAgent = t_Game.m_xab.getSelectedAgent(0);
                pa = t_Game.m_xfun.fetchAgent(0, sAgent, t_Game.m_xab);
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


}
