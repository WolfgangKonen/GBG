package controllers.MCTSExpWrapper;

import controllers.MCTSExpWrapper.stateApproximation2.PlayAgentApproximator2;
import controllers.MaxN2Wrapper;
import controllers.PlayAgent;
import games.EWN.GameBoardEWN;
import games.EWN.StateObserverEWN;
import games.StateObservation;
import games.ZweiTausendAchtundVierzig.GameBoard2048;
import org.junit.Test;
import params.ParOther;
import starters.GBGBatch;
import tools.Types;
import tools.Types.ACTIONS;

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
     * and test whether a TDNT4 or a MCTSE-Wrapper[TDNT4] suggest the right move 0504.
     * Result: Only MCTSE-Wrapper[TDNT4] suggest the right move. TDNT4 randomly picks a move because
     * all values for this state are exactly the same.
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
        PlayAgent pa;
        PlayAgent qa;
        double c_puct=500.0;
        int maxDepth = -1;
        Types.ACTIONS_VT act_pa, act_qa;


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
                    so.setNextActionDeterministic(ACTIONS.fromInt(1));
                    so.advanceDeterministic(ACTIONS.fromInt(304));
                    so.advanceNondeterministic(ACTIONS.fromInt(0));
                    so.advanceDeterministic(ACTIONS.fromInt(804));
                    so.advanceNondeterministic(ACTIONS.fromInt(0));
                    so.advanceDeterministic(ACTIONS.fromInt(4));
                    so.advanceNondeterministic(ACTIONS.fromInt(1));
/* Now we have the state
        [  ] [X2] [  ]     (diceVal:1,   availActions: 0501,0502,0504 )
        [  ] [X0] [O1]
        [  ] [O2] [  ]
   The optimal action is 0504
 */

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
                    }

                    deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
                    elapsedTime += deltaTime;
                    System.out.println("  ");
                }

            } // for (agtFiles)
        } // for (run)

        System.out.println("[stateEWNTest] "+elapsedTime+" sec.");
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
