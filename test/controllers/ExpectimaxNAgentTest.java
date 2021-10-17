package controllers;

import games.EWN.GameBoardEWN;
import games.EWN.StateObserverEWN;
import org.junit.Test;
import starters.GBGBatch;
import tools.Types;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ExpectimaxNAgentTest extends GBGBatch {

    /**
     * This test just executes nEpi EWN[3x3 2-player] episodes where ExpectimaxNAgent plays against itself.
     * No assertions, just printout on console and the user can check if everything is OK.
     * <p>
     * For nDepth &ge; 15, a tree traversal should never stop prematurely (i.e. countMaxDepth=0 for all moves should hold).
     */
    @Test
    public void playGameTest() {
        PlayAgent pa;
        int nEpi=2;
        int nDepth=15;
        Types.ACTIONS_VT act_pa;

        String selectedGame = "EWN";
        String[] scaPar = GBGBatch.setDefaultScaPars(selectedGame);  // for EWN currently: 3x3 2-player
        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoardEWN gb = new GameBoardEWN(t_Game); //,3,2);		// needed for chooseStartState()
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

    /**
     * This test checks for 2-player 3x3 EWN whether state {@code so} has an Expectimax best value that is the opposite
     * of the value that we get when we weight-average the afterstate values for all possible next non-deterministic
     * dice values.
     * <p>
     * [The reason for this relation is the recursive nature of the Expectimax tree. Why 'opposite'? - Because EWN is a
     * 2-player zero-sum game.]
     * <p>
     * Why "Tree3"? - The state {@code so} loops through the 3 states "start state + one of the  3 possible dice values".
     * <p>
     * The test will fail, if {@code nDepth} is too small (less than 8).
     */
    @Test
    public void expectimaxTree3Test() {
        PlayAgent pa;
        int nDepth=9;
        boolean silent = false;

        String selectedGame = "EWN";
        String[] scaPar = GBGBatch.setDefaultScaPars(selectedGame);  // for EWN currently: 3x3 2-player
        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoardEWN gb = new GameBoardEWN(t_Game); //,3,2);		// needed for chooseStartState()
        StateObserverEWN startSO, so;

        pa = new ExpectimaxNAgent("ExpectimaxN",nDepth);

        // select a state:
        gb.clearBoard(true,true);
        startSO = (StateObserverEWN) gb.getStateObs();
        ArrayList<Types.ACTIONS> startRandoms = startSO.getAvailableRandoms();

        for (Types.ACTIONS startR : startRandoms) {
            so = (StateObserverEWN) startSO.copy();
            so.advanceNondeterministic(startR);

            //if (startR.toInt()==0) continue;

            System.out.println("\n*** Episode with dice value "+startR.toInt()+ " starts ***");
            System.out.print(so);


            innerETreeTest(pa,so,silent,"dice value="+startR.toInt());

        }
        System.out.println("[expectimaxTree3Test] finished");
    }

    /**
     * This test checks for 2-player 3x3 EWN whether state {@code so} has an Expectimax best value that is the opposite
     * of the value that we get when we take the afterstate resulting from the best action and weight-average the
     * afterstate values for all possible next non-deterministic dice values.
     * <p>
     * [The reason for this relation is the recursive nature of the Expectimax tree. Why 'opposite'? - Because EWN is a
     * 2-player zero-sum game.]
     * <p>
     * Why "Tree9"? - The state {@code so} loops through the 9 states that we get when we advance from "start state +
     * one of the 3 possible dice values" with one of the 3 available actions.
     * <p>
     * The test will fail, if {@code nDepth} is too small (less than 8).
     */
    @Test
    public void expectimaxTree9Test() {
        PlayAgent pa;
        int nDepth=13;
        boolean silent = false;  // passed on to getNextAction2

        String selectedGame = "EWN";
        String[] scaPar = GBGBatch.setDefaultScaPars(selectedGame);  // for EWN currently: 3x3 2-player
        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        GameBoardEWN gb = new GameBoardEWN(t_Game); //,3,2);		// needed for chooseStartState()
        StateObserverEWN startSO, so;
        DecimalFormat frmAct = new DecimalFormat("0000");

        pa = new ExpectimaxNAgent("ExpectimaxN",nDepth);

        // select a state:
        gb.clearBoard(true,true);
        startSO = (StateObserverEWN) gb.getStateObs();
        ArrayList<Types.ACTIONS> startRandoms = startSO.getAvailableRandoms();

        for (Types.ACTIONS startR : startRandoms) {
            startSO.advanceNondeterministic(startR);
            ArrayList<Types.ACTIONS> nextActions = startSO.getAvailableActions();

            System.out.print(startSO);

            for (Types.ACTIONS a : nextActions) {
                System.out.println("\n*** Episode with action "+frmAct.format(a.toInt())+ " starts ***");

                so = (StateObserverEWN) startSO.copy();
                so.advance(a);

                innerETreeTest(pa,so,silent,"action="+frmAct.format(a.toInt()));

            }
        }
        System.out.println("[expectimaxTree9Test] finished");
    }

    private void innerETreeTest(PlayAgent pa, StateObserverEWN so, boolean silent, String text) {
        StateObserverEWN afterstate, newSO;
        Types.ACTIONS_VT act_pa = pa.getNextAction2(so.partialState(),false,silent);
        double vA = act_pa.getVBest();
        afterstate = (StateObserverEWN) so.copy();
        afterstate.advanceDeterministic(act_pa);

        ArrayList<Types.ACTIONS> nextRandoms = afterstate.getAvailableRandoms();
        double vNew=0.0;
        double prob;
        for (Types.ACTIONS r : nextRandoms) {
            newSO = (StateObserverEWN) afterstate.copy();
            newSO.advanceNondeterministic(r);
            act_pa = pa.getNextAction2(newSO.partialState(),false,silent);
            prob = afterstate.getProbability(r);
            vNew += act_pa.getVBest()*prob;
        }
        System.out.println(text+":  vA = "+vA+",  vNew = "+vNew);
        assert Math.abs(vA+vNew)<1e-8 : "Assertion vA+vNew=0 failed!";
    }

}