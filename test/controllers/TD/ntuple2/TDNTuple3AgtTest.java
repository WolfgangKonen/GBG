package controllers.TD.ntuple2;

import controllers.MaxN2Wrapper;
import controllers.PlayAgent;
import games.GBGBatch;
import games.RubiksCube.GameBoardCube;
import games.StateObservation;
import org.junit.Test;
import tools.Types;

import static org.junit.Assert.*;

public class TDNTuple3AgtTest extends GBGBatch {
    String selectedGame = "RubiksCube";
    String[] agtFile = {"TCL3-p11-2000k-120-7t.agt.zip","davi3-p11-2000k-120-7t-BASE.agt.zip"};
    String[] agtType = {"TDNT3", "DAVI3"};
    double[][] evalThresh = {{0.75, 0.85}, {0.74, 0.84}};
    String csvFile = "test.csv";
    String[] scaPar = GBGBatch.setDefaultScaPars(selectedGame);

    /**
     * Calling MaxN2Wrapper with nPly=0 should result in the same best values as calling the wrapped agent directly.
     * Check this on nStates random start states for RubiksCube and for all agents in {@code agtFile} (currently certain
     * DAVI2 and DAVI3 agents).
     */
    @Test
    public void quickEvalTest() {
        PlayAgent pa;
        PlayAgent qa;
        StateObservation so;
        int nplyMax=1;
        int nStates=2;
        double evalQ;
        int stopEval=50;
        Types.ACTIONS_VT act_pa, act_qa;

        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoardCube gb = new GameBoardCube(t_Game);		// needed for chooseStartState()

        for (int k=0; k< agtType.length; k++) {
            setupPaths(agtFile[k],csvFile);     // builds filePath

            boolean res = t_Game.loadAgent(0, filePath);
            assert res : "\n[TDNTuple3AgtTest] Aborted: agtFile = "+agtFile[k] + " not found!";

            String sAgent = t_Game.m_xab.getSelectedAgent(0);
            pa = t_Game.m_xfun.fetchAgent(0,sAgent, t_Game.m_xab);
            for (int nply=0; nply<=nplyMax; nply++) {
                qa = new MaxN2Wrapper(pa, nply, pa.getParOther());

                int qem = t_Game.m_xab.oPar[0].getQuickEvalMode();
                m_evaluatorQ = t_Game.m_xab.m_arena.makeEvaluator(pa,gb,stopEval,qem,-1);

                for (int i=0; i<nStates; i++) {
                    m_evaluatorQ.eval(qa);
                    evalQ = m_evaluatorQ.getLastResult();
                    System.out.println("nply="+nply+", "+i+ ": evalQ="+evalQ+"    "+evalThresh[k][nply]);
                    assert evalQ > evalThresh[k][nply] : "k="+k+", nply="+nply+": did not pass evalThresh test";

                }

            }
            System.out.println("[quickEvalTest,"+agtType[k]+"] all eval runs above evalThresh");
        } // for (k)
    }

}