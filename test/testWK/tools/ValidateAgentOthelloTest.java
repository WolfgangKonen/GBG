package testWK.tools;

import controllers.PlayAgent;
import games.Arena;
import games.Othello.ArenaTrainOthello;
import org.junit.jupiter.api.Test;
import testWK.tools.ValidateAgentTest;
import tools.Types;

import java.io.IOException;

/**
 *  Run tests to validate an Othello agent.
 *  Perform the tests from {@link ValidateAgentTest} that are generally admitted for any agent in any game.
 *  
 * @see ValidateAgentTest
 */
public class ValidateAgentOthelloTest {
    private ValidateAgentTest vat = new ValidateAgentTest();
    private final static String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/Othello/";
    private final static String gbgAgentPath = strDir + "TCL3-100_7_250k-lam05_P4_nPly2-FAm.agt.zip";
    private static final Arena ar = new ArenaTrainOthello("", false);

    //
    // choose an agent to validate - select one of the options in buildAgent for constructing pa:
    //
    private static final PlayAgent pa = buildAgent();       // static final so that it is built only once

    private static PlayAgent buildAgent() {
        PlayAgent pa = null;
        try {            // try-catch is for loadGBGAgent which may throw exceptions
//			pa = new BenchMarkPlayer("bp",1);   // only for Othello
//			pa = constructTDNTuple3Agt(ar);
            pa = ar.tdAgentIO.loadGBGAgent(gbgAgentPath);    // check if a TDNT3-agent reloaded from disk passes all test
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pa;
    }

    private static PlayAgent constructTDNTuple3Agt(Arena ar) {
        PlayAgent p;
        try {
            p = ar.m_xfun.constructAgent(0, "TD-Ntuple-3", ar.m_xab);
        } catch (IOException e1) {
            e1.printStackTrace();
            p = null;
        }
        return p;
    }

    @Test
    public void testScoreTuple() {
        vat.runTestScoreTuple(pa, ar);
    }

    @Test
    public void testFinalScoreTuple() {
        vat.runTestFinalScoreTuple(pa, ar);
    }

    @Test
    public void testTrainingEpisode() {
        vat.testTrainingEpisode(pa, ar);
    }

    @Test
    public void testBoardVectorSymmetries() {
        vat.testBoardVectorSymmetries(ar);
    }
}
