package controllers;

import games.EWN.GameBoardEWN;
import games.EWN.StateObserverEWN;
import games.RubiksCube.GameBoardCube;
import games.StateObservation;
import org.junit.Test;
import starters.GBGBatch;
import tools.Types;

public class ExpectimaxNAgentTest extends GBGBatch {

    /**
     * This test just executes nEpi EWN[3x3 2-player] episodes where ExpectimaxNAgent plays against itself.
     * No assertions, just printout on console and the user can check if everything is OK.
     * For nDepth>=15, no tree traversal should never stop prematurely (i.e. countMaxDepth=0 for all moves).
     */
    @Test
    public void playGameTest() {
        PlayAgent pa;
        int nEpi=5;
        int nDepth=15;
        Types.ACTIONS_VT act_pa;

        String selectedGame = "EWN";
        String[] scaPar = GBGBatch.setDefaultScaPars(selectedGame);  // for EWN currently: 3x3 2-player
        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoardEWN gb = new GameBoardEWN(t_Game,3,2);		// needed for chooseStartState()
        StateObserverEWN so;

        pa = new ExpectimaxNAgent("ExpectimaxN",nDepth);

        for (int i=0; i<nEpi; i++) {
            System.out.println("\n*** Episode "+i+ " starts ***");
            gb.clearBoard(true,true);
            so = (StateObserverEWN) gb.getStateObs();
            while(!so.isGameOver()) {
                act_pa = pa.getNextAction2(so.partialState(),false,false);
                         // due to silent=false, each call to getNextAction2 has state-info-printout on console
                so.advance(act_pa);
            }
            int winner = so.getPlayerWin();
            System.out.println("Episode "+i+" finished, player "+winner+" wins");
        }
        System.out.println("[playGameTest] finished");
    }

}
