package games.CFour;

import controllers.PlayAgent;
import games.Arena;
import games.StateObservation;
import org.junit.Test;
import tools.Types;
import tools.ValidateStateObsTest;

import java.io.IOException;

public class StateObserverC4Test {
    // this is a full board which has in the highest row a 'oooo', i.e. it is a win for player 1 ('o')
    String sb1 = "|ooXoXo|XoXoXo|oXXXoo|XXoXoo|oXoXXX|XoXoXo|XoXoXo|";
    int[][] board1 = {           // |ooXoXo|XoXoXo|oXXXoo|XXoXoo|oXoXXX|XoXoXo|XoXoXo|
            {2,2,1,2,1,2},
            {1,2,1,2,1,2},
            {2,1,1,1,2,2},
            {1,1,2,1,2,2},
            {2,1,2,1,1,1},
            {1,2,1,2,1,2},
            {1,2,1,2,1,2}
    };
    // bprev1 is the previous board which advances with action 1 (placing 'o' in column 1) to 'board'
    String sp1 = "|ooXoXo|XoXoX-|oXXXoo|XXoXoo|oXoXXX|XoXoXo|XoXoXo|";
    int[][] bprev1 = {           // |ooXoXo|XoXoX-|oXXXoo|XXoXoo|oXoXXX|XoXoXo|XoXoXo|
            {2,2,1,2,1,2},
            {1,2,1,2,1,0},
            {2,1,1,1,2,2},
            {1,1,2,1,2,2},
            {2,1,2,1,1,1},
            {1,2,1,2,1,2},
            {1,2,1,2,1,2}
    };
    // this is a non-full board which has a diagonal 'oooo', i.e. it is a win for player 1 ('o')
    String sb2 = "|oXXoXo|oXXoXo|XoXo--|XXoXoX|XoXXoo|oXXoXo|ooXoXo|";
    int[][] board2 = {           // |oXXoXo|oXXoXo|XoXo--|XXoXoX|XoXXoo|oXXoXo|ooXoXo|
            {2,1,1,2,1,2},
            {2,1,1,2,1,2},
            {1,2,1,2,0,0},
            {1,1,2,1,2,1},
            {1,2,1,1,2,2},
            {2,1,1,2,1,2},
            {2,2,1,2,1,2}
    };
    // bprev2 is the previous board which advances with action 2 (placing 'o' in column 2) to 'board'
    String sp2 = "|oXXoXo|oXXoXo|XoX---|XXoXoX|XoXXoo|oXXoXo|ooXoXo|";
    int[][] bprev2 = {           // |oXXoXo|oXXoXo|XoX---|XXoXoX|XoXXoo|oXXoXo|ooXoXo|
            {2,1,1,2,1,2},
            {2,1,1,2,1,2},
            {1,2,1,0,0,0},
            {1,1,2,1,2,1},
            {1,2,1,1,2,2},
            {2,1,1,2,1,2},
            {2,2,1,2,1,2}
    };

    @Test
    public void testWin() {
        // test for specific states, if they are detected as win or not
        runTestWin("1 ",board1,bprev1,sb1,sp1);
        runTestWin("2 ",board2,bprev2,sb2,sp2);
    }
    public void runTestWin(String prefix, int[][] board, int[][] bprev, String sb, String sp) {
        StateObserverC4 sobC4 = new StateObserverC4(board);
        StateObserverC4 sprev = new StateObserverC4(bprev);
        String[] players = {"P1","P2"};

        assert sobC4.isLegalState();
        assert sobC4.stringDescr().equals(sb);
        assert sprev.isLegalState();
        assert sprev.stringDescr().equals(sp);

        // we cannot detect that sobC4 is a win:
        System.out.println(prefix+"sobC4: "+ sobC4.stringDescr());
        System.out.println(prefix+"sobC4 isWin: "+ sobC4.win());

        // but we can detect that sprev advanced by ACTIONS(1) - leading to sobC4 - is a win.
        // This is because advance with method C4Base.canWin detects whether the piece to set
        // closes a 4-in-a-row. It stores this in boolean member isWin.
        sprev.advance(sprev.getAvailableActions().get(0));
        System.out.println(prefix+"sprev: "+sprev.stringDescr());
        System.out.println(prefix+"sprev isWin: "+sprev.win());
        System.out.println(prefix+"sprev sc: "+sprev.getGameScoreTuple());
        assert sprev.win();
        assert sprev.getGameScoreTuple().toString().equals("(-1.0, 1.0)");
        System.out.println(prefix+ ar.gameOverString(sprev,players,null));
    }

    private final ValidateStateObsTest vat = new ValidateStateObsTest();
    private final static String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/ConnectFour/";
    private final static String gbgAgentPath = strDir + "TCL-EXP-NT3-al37-lam000-6000k-epsfin0.agt.zip";
    private static final Arena ar = new ArenaTrainC4("", false);
    private static final StateObservation sob = ar.getGameBoard().getDefaultStartState();

    //
    // choose an agent to validate - select one of the options in buildAgent for constructing pa:
    //
    private static final PlayAgent pa = buildAgent();       // static final so that it is built only once

    private static PlayAgent buildAgent() {
        PlayAgent pa = null;
        try {            // try-catch is for loadGBGAgent which may throw exceptions
			pa = constructTDNTuple3Agt(ar);
//          pa = ar.tdAgentIO.loadGBGAgent(gbgAgentPath);    // check if a TDNT3-agent reloaded from disk passes all test
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
        vat.runTestScoreTuple(sob, pa, ar);
    }

    @Test
    public void testSerializable() {
        StateObserverC4 sobC4 = new StateObserverC4(board1);
        try{
            vat.runTestSerializable(sobC4,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}