package controllers;

import games.GBGBatch;
import games.RubiksCube.GameBoardCube;
import games.StateObservation;
import org.junit.Test;
import tools.Types;

public class MaxN2WrapperTest extends GBGBatch {
    String selectedGame = "RubiksCube";
    String[] agtFile = {"davi2-p11-2000k.agt.zip","davi3-p11-2000k-120-7t-BASE.agt.zip","TCL3-p13-3000k-120-7t.agt.zip"};
    String[] agtType = {"DAVI2", "DAVI3","TDNT3"};
    String csvFile = "test.csv";
    String[] scaPar = GBGBatch.setDefaultScaPars(selectedGame);

    /**
     * Calling MaxN2Wrapper with nPly=0 should result in the same best values as calling the wrapped agent directly.
     * Check this on nStates random start states for RubiksCube and for all agents in {@code agtFile} (currently certain
     * DAVI2 and DAVI3 agents).
     */
    @Test
    public void nPlyEqualsZeroTest() {
        PlayAgent pa;
        PlayAgent qa;
        StateObservation so;
        int nply=0;
        int nStates=100;
        Types.ACTIONS_VT act_pa, act_qa;

        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoardCube gb = new GameBoardCube(t_Game);		// needed for chooseStartState()

        for (int k=0; k< agtType.length; k++) {
            setupPaths(agtFile[k],csvFile);     // builds filePath

            boolean res = t_Game.loadAgent(0, filePath);
            assert res : "\n[MaxN2WrapperTest] Aborted: agtFile = "+agtFile[k] + " not found!";

            String sAgent = t_Game.m_xab.getSelectedAgent(0);
            pa = t_Game.m_xfun.fetchAgent(0,sAgent, t_Game.m_xab);
            qa = new MaxN2Wrapper(pa, nply, pa.getParOther());


            for (int i=0; i<nStates; i++) {
                so = gb.chooseStartState(11);
                act_pa = pa.getNextAction2(so,false,true);
                act_qa = qa.getNextAction2(so,false,true);
                assert act_pa.getVBest() == act_qa.getVBest() : "The actions have different vBest!";
                if (act_pa.toInt() != act_qa.toInt()) {
                    System.out.println(i+": The actions differ, but vBest = "+act_pa.getVBest()+" is the same");
                }
            }
            System.out.println("[nPlyEqualsZeroTest,"+agtType[k]+"] all states checked with getNextAction2 result in the same vBest");
        } // for (k)
    }



}