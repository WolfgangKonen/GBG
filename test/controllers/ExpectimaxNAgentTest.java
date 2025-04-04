package controllers;

import games.EWN.GameBoardEWN;
import games.EWN.StateObserverEWN;
import org.junit.Test;
import starters.GBGBatch;
import starters.SetupGBG;
import tools.Types;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

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

        String[] scaPar = SetupGBG.setDefaultScaPars("EWN");  // for EWN currently: 3x3 2-player
        arenaTrain = SetupGBG.setupSelectedGame("EWN",scaPar,"",false,true);
        GameBoardEWN gb = new GameBoardEWN(arenaTrain); //,3,2);		// needed for chooseStartState()
        StateObserverEWN so;

        pa = new ExpectimaxNAgent("ExpectimaxN",nDepth);

        for (int i=0; i<nEpi; i++) {
            System.out.println("\n*** Episode "+i+ " starts ***");
            gb.clearBoard(true,true, null);
            so = (StateObserverEWN) gb.getStateObs();
            while(!so.isGameOver()) {
                act_pa = pa.getNextAction2(so.partialState(),false, false, false);
                         // due to silent=false, each call to getNextAction2 has state-info-printout on console
                so.advance(act_pa, null);
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

        String[] scaPar = SetupGBG.setDefaultScaPars("EWN");                // for EWN: 3x3
        //scaPar = new String[]{"5x5 2-Player","[0,1][2,3][3,4]","False"};    // for EWN: 5x5
            // ExpectimaxN for 5x5 EWN has exponentially rising time and memory demands:
            // For nDepth=4,5,6 the hash map size is 70.000, 600.000, 4.500.000, resp., so we cannot reach from
            // the start state the final states --> pretty useless
        arenaTrain = SetupGBG.setupSelectedGame("EWN",scaPar,"",false,true);
        GameBoardEWN gb = new GameBoardEWN(arenaTrain); //,3,2);		// needed for chooseStartState()
        StateObserverEWN startSO, so;

        pa = new ExpectimaxNAgent("ExpectimaxN",nDepth);

        // --- just as test, will not fulfill assertions exactly ---
//        String agtFile = "tdnt4-10000.agt.zip";
//        pa = arenaTrain.loadAgent(agtFile);

        // select a state:
        gb.clearBoard(true,true, null);
        startSO = (StateObserverEWN) gb.getStateObs();
        ArrayList<Types.ACTIONS> startRandoms = startSO.getAvailableRandoms();

        for (Types.ACTIONS startR : startRandoms) {
            so = (StateObserverEWN) startSO.copy();
            so.advanceNondetSpecific(startR);

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
        int nDepth=9;
        boolean silent = false;  // passed on to getNextAction2

        String[] scaPar = SetupGBG.setDefaultScaPars("EWN");        // for EWN currently: 3x3 2-player
        arenaTrain = SetupGBG.setupSelectedGame("EWN",scaPar,"",false,true);
        GameBoardEWN gb = new GameBoardEWN(arenaTrain); //,3,2);		// needed for chooseStartState()
        StateObserverEWN startSO, so;
        DecimalFormat frmAct = new DecimalFormat("0000");

        pa = new ExpectimaxNAgent("ExpectimaxN",nDepth);

        // select a state:
        gb.clearBoard(true,true, null);
        startSO = (StateObserverEWN) gb.getStateObs();
        ArrayList<Types.ACTIONS> startRandoms = startSO.getAvailableRandoms();

        for (Types.ACTIONS startR : startRandoms) {
            System.out.println("\n*** Branch with diceVal "+startR.toInt()+ " starts ***");
            startSO.advanceNondetSpecific(startR);
            ArrayList<Types.ACTIONS> nextActions = startSO.getAvailableActions();

            System.out.print(startSO);

            for (Types.ACTIONS a : nextActions) {
                System.out.println("\n  *** Episode with action "+frmAct.format(a.toInt())+ " starts ***");

                so = (StateObserverEWN) startSO.copy();
                so.advance(a, null);

                innerETreeTest(pa,so,silent,"action="+frmAct.format(a.toInt()));
            }
        }
        System.out.println("[expectimaxTree9Test] finished");
    }

    /**
     * Helper method for {@link #expectimaxTree3Test()} and {@link #expectimaxTree9Test()}:
     * For {@code pa} which should have a nondeterministic next action (otherwise exception):
     * Check that the ExpectimaxN value of {@code so} plus the probability-weight-average of all
     * child state values resulting from each of the possible dice values sums up to 0.
     * @param pa
     * @param so
     * @param silent
     * @param text
     */
    private void innerETreeTest(PlayAgent pa, StateObserverEWN so, boolean silent, String text) {
        StateObserverEWN afterstate, newSO;
        Types.ACTIONS_VT act_pa = pa.getNextAction2(so.partialState(),false, false, silent);
        double vA = act_pa.getVBest();
        afterstate = (StateObserverEWN) so.copy();
        afterstate.advanceDeterministic(act_pa);

        ArrayList<Types.ACTIONS> nextRandoms = afterstate.getAvailableRandoms();
        double vNew=0.0;
        double prob;
        for (Types.ACTIONS r : nextRandoms) {
            newSO = (StateObserverEWN) afterstate.copy();
            newSO.advanceNondetSpecific(r);
            act_pa = pa.getNextAction2(newSO.partialState(),false, false, silent);
            prob = afterstate.getProbability(r);
            vNew += act_pa.getVBest()*prob;
        }
        System.out.println(text+":  vA = "+vA+",  vNew = "+vNew);
        assert Math.abs(vA+vNew)<1e-8 : "Assertion vA+vNew=0 failed!";
    }

    /**
     * This test checks for 2-player 3x3 EWN a certain state
     * <pre>
     *     .. X2 ..
     *     O2 .. O1   with dice 1 and O to move
     *     .. X1 ..
     * </pre>
     * the win probabilities that {@link ExpectimaxNAgent} returns: What is the probability for player O
     * to win, if he takes one of its available moves (504, 502, 501)?
     * <p>
     * The theoretical expectation is (can be analyzed by following all paths and multiplying their win rate with
     * their probability: P(O wins) = {1/3, 1/9, 0}.
     * <p>
     * The test will fail, if the discrepancy between theoretical and calculated result is larger than 1e-8.
     */
    @Test
    public void certainEWNTest() {
        PlayAgent pa;
        int nDepth=9;
        boolean silent = false;  // passed on to getNextAction2
        Types.ACTIONS_VT act_pa;

        String[] scaPar = SetupGBG.setDefaultScaPars("EWN");        // for EWN currently: 3x3 2-player
        arenaTrain = SetupGBG.setupSelectedGame("EWN",scaPar,"",false,true);
        GameBoardEWN gb = new GameBoardEWN(arenaTrain); //,3,2);		// needed for chooseStartState()
        StateObserverEWN startSO, so;
        //DecimalFormat frmAct = new DecimalFormat("0000");

        pa = new ExpectimaxNAgent("ExpectimaxN",nDepth);

        // build the specific state:
        gb.clearBoard(true,true, null);
        startSO = (StateObserverEWN) gb.getStateObs();
        int[] actList = new int[]{4,804,304,703,407};
        for (int a : actList) {
            startSO.advanceDeterministic(new Types.ACTIONS(a));
        }
        startSO.advanceNondetSpecific(new Types.ACTIONS(1));
        System.out.println(startSO);

        for (int run=0; run<1; run++) {
            ArrayList<Types.ACTIONS> availActs = startSO.getAvailableActions();
            int[] iActs = new int[availActs.size()];
            for (int i=0; i<iActs.length; i++) iActs[i] = availActs.get(i).toInt();

            act_pa = pa.getNextAction2(startSO.partialState(),false, false, silent);

            System.out.println("For available actions  "+Arrays.toString(iActs));
            double[] expnWinProb = act_pa.getWinProb();
            System.out.println("ExpectimaxN win probs: "+Arrays.toString(expnWinProb));
            double[] theoWinProb = new double[]{1/3., 1/9., 0};
            System.out.println("theoretical win probs: "+Arrays.toString(theoWinProb));

            for (int i=0; i<theoWinProb.length; i++) {
                assert Math.abs(theoWinProb[i]-expnWinProb[i]) < 1e-8
                        : "theoWinProb[i]="+theoWinProb[i]+" != expnWinProb[i]"+ expnWinProb[i];
            }

        }
        System.out.println("[certainEWNTest] successfully passed");
    }

}
