package tools;

import controllers.PlayAgent;
import games.Arena;
import games.Sim.ArenaSim;
import games.StateObservation;
import org.junit.Test;

/**
 *  Run tests to validate a Sim StateObservation. Perform the tests from {@link ValidateStateObsTest}
 *  that are generally admitted for any StateObservation in any game.
 *
 * @see ValidateStateObsTest
 */
public class ValidateStateObsSimTest {
    private ValidateStateObsTest vat = new ValidateStateObsTest();
    private final static String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/Sim/K6_Player2/";
    private final static String gbgAgentPath = strDir + "TDNT3-15mover-fixed4-lam05-NEW.agt.zip";
    private static final Arena ar =  new ArenaSim("",false);  // default Sim: 6 nodes, 2 players

    private static final StateObservation sob = ar.getGameBoard().getDefaultStartState(null);

    //
    // choose an agent to validate - select one of the options in buildAgent for constructing pa:
    //
    private static final PlayAgent pa = buildAgent();       // static final so that it is built only once

    private static PlayAgent buildAgent() {
        PlayAgent pa = null;
        try {            // try-catch is for loadGBGAgent which may throw exceptions
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
        } catch (Exception e1) {
            e1.printStackTrace();
            p = null;
        }
        return p;
    }

    @Test
    public void testScoreTuple() {
        vat.runTestScoreTuple(sob, pa, ar);
    }

    @Test
    public void testSerializable() {
        assert vat.runTestSerializable(sob,true);
    }
}
