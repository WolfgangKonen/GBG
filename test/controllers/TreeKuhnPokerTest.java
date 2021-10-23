package controllers;

import controllers.MCTSExpectimax.MCTSETreeNode;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import games.KuhnPoker.StateObserverKuhnPoker;
import games.Poker.StateObserverPoker;
import org.junit.Test;
import params.ParMCTSE;
import starters.GBGBatch;
import tools.Types;

import java.util.HashMap;

/**
 * This class has various tests for tree-based agents ond the game KuhnPoker (as an example for an
 * imperfect-information game)
 */
public class TreeKuhnPokerTest extends GBGBatch {

    /**
     * <ul>
     *     <li> If {@code false}, loop over the four combinations of the program switches
     *          {@link MCTSETreeNode#WITH_PARTIAL WITH_PARTIAL} = {true,false} and
     *          {@link StateObserverKuhnPoker#PLAY_ONE_ROUND_ONLY PLAY_ONE_ROUND_ONLY} = {true,false}
     *     <li> If {@code true}, test only the <b>recommended</b> combination
     *          {@link MCTSETreeNode#WITH_PARTIAL WITH_PARTIAL} = true and
     *          {@link StateObserverKuhnPoker#PLAY_ONE_ROUND_ONLY PLAY_ONE_ROUND_ONLY} = true
     * </ul>
     */
    public boolean TEST_ONE_SWITCH_ONLY=true;

    //
    // -------------- first player tests ------------------------------------------------------------------
    //

    /**
     * Checks for KuhnPoker and the starting player 0: Far a given state with a certain card {@code numCard}
     * and a certain action {@code pact} that has been played before (either 0 (NONE) or 1 (CHECK-BET)):
     * Is the agent MCTSExpectimax able to find the optimal next action for player 0 or not?
     * <p>
     * It turns out that -- if {@code numCard}=10 (Q) and {@code pact}=0 (NONE) is selected -- only <b>one</b> of the
     * four tested swtich combinations  (see {@link #TEST_ONE_SWITCH_ONLY}), namely<ul>
     *      <li> {@link MCTSETreeNode#WITH_PARTIAL WITH_PARTIAL} = true                   and
     *      <li> {@link StateObserverKuhnPoker#PLAY_ONE_ROUND_ONLY PLAY_ONE_ROUND_ONLY} = true,
     * </ul>gets the desired result (20x action 1 = CHECK)
     */
    @Test
    public void mctseKuhnPartialTest1st() {
        PlayAgent pa = new MCTSExpectimaxAgt("mctse", new ParMCTSE());

        // select a state with a Jack (9), Queen (10) or King (11) holecard for player 0:
        int numCard = 10;   // 9 10 11
        // do nothing if pact==0 or advance by CHECK-BET if pact==1:
        int pact = 0;

        agentKuhnPartialTest1st(pa, numCard, pact);
    }

    /**
     * Same as {@link #mctseKuhnPartialTest1st()}, but for all cards and all possible prior actions
     */
    @Test
    public void mctseKuhnAllTest1st() {
        PlayAgent pa = new MCTSExpectimaxAgt("mctse", new ParMCTSE());

        int[] cards = {9,10,11};    // holecard for player 0: Jack (9), Queen (10) or King (11)
        int[] pacts = {0,1};        // advance by CHECK-BET if pact==1 or do nothing if pact==0
        for (int numCard : cards) {
            for (int pact : pacts)
                agentKuhnPartialTest1st(pa, numCard, pact);
        }
    }

    /**
     * Same as {@link #mctseKuhnPartialTest1st()}, but for agent ExpectimaxN
     */
    @Test
    public void expecKuhnPartialTest1st() {
        PlayAgent pa = new ExpectimaxNAgent("expecN", 5);

        // select a state with a Jack (9), Queen (10) or King (11) holecard for player 0:
        int numCard = 11;   // 9 10 11
        // advance by CHECK-BET if pact==1 or do nothing if pact==0:
        int pact = 0;

        agentKuhnPartialTest1st(pa, numCard, pact);
    }

    /**
     * Same as {@link #expecKuhnPartialTest1st()}, but for all cards and all possible prior actions
     */
    @Test
    public void expecKuhnAllTest1st() {
        PlayAgent pa = new ExpectimaxNAgent("expecN", 5);

        int[] cards = {9,10,11};    // holecard for player 0: Jack (9), Queen (10) or King (11)
        int[] pacts = {0,1};        // advance by CHECK-BET if pact==1 or do nothing if pact==0
        for (int numCard : cards) {
            for (int pact : pacts)
                agentKuhnPartialTest1st(pa, numCard, pact);
        }
    }

    /**
     * Helper method for the four tests above
     */
    private void agentKuhnPartialTest1st(PlayAgent pa, int numCard, int pact) {
        // The desired action with J is either 1=CHECK or 2=BET if pact=0 (NONE). And it is 0=FOLD if pact=1 (CHECK-BET).
        // The desired action with Q is 1=CHECK if pact=0 (NONE). And it is either 0=FOLD or 3=CALL if pact=1 (CHECK-BET).
        // The desired action with K is either 1=CHECK or 2=BET if pact=0 (NONE). And it is 3=CALL if pact=1 (CHECK-BET).
        String[] desiredJ = {"optimal:0 *  * 0","optimal:20 0 0 0"};
        String[] desiredQ = {"optimal:0 20 0 0","optimal:* 0 0 *"};
        String[] desiredK = {"optimal:0 *  * 0","optimal:0 0 0 20"};
        int[][] desiredZerosJ = {{0,3}, {1,2,3}};
        int[][] desiredZerosQ = {{0,2,3}, {1,2}};
        int[][] desiredZerosK = {{0,3}, {0,1,2}};
        HashMap<Integer, String[]> hashmap = new HashMap<>();
        hashmap.put( 9,desiredJ);
        hashmap.put(10,desiredQ);
        hashmap.put(11,desiredK);
        HashMap<Integer, int[][]> hashzero = new HashMap<>();
        hashzero.put( 9,desiredZerosJ);
        hashzero.put(10,desiredZerosQ);
        hashzero.put(11,desiredZerosK);

        String[] cardname = {"JACK","QUEEN","KING"};
        System.out.println("\nCard="+cardname[numCard-9]+", pact="+pact);
        StateObserverKuhnPoker startSO;
        boolean[] withPartialArr = (TEST_ONE_SWITCH_ONLY) ? new boolean[]{true} : new boolean[]{false,true};
        boolean[] oneRoundArr = (TEST_ONE_SWITCH_ONLY) ? new boolean[]{true} : new boolean[]{false,true};

        for (boolean b : withPartialArr) {
            MCTSETreeNode.WITH_PARTIAL = b;
            System.out.println("*** Test WITH_PARTIAL=" + MCTSETreeNode.WITH_PARTIAL + " starts ***");

            for (boolean value : oneRoundArr) {
                StateObserverKuhnPoker.PLAY_ONE_ROUND_ONLY = value;
                System.out.println("   *** Part PLAY_ONE_ROUND_ONLY=" + StateObserverKuhnPoker.PLAY_ONE_ROUND_ONLY + " starts ***");

                // select a state with holecard = numCard for player 0:
                startSO = new StateObserverKuhnPoker();
                while (startSO.getHoleCards(0)[0].getRank() != numCard)
                    startSO = new StateObserverKuhnPoker();
                //System.out.println(startSO.stringDescr());

                // advance by CHECK-BET if pact==1 (do nothing if pact==0)
                if (pact==1) {
                    startSO.advance(new Types.ACTIONS(1));
                    startSO.advance(new Types.ACTIONS(2));
                }

                // now measure the reaction of player 0 (one out of 0 (FOLD), 1 (CHECK), 2 (BET), 3 (CALL)):
                int[] actHist = new int[4];
                for (int m = 0; m < 20; m++) {
                    Types.ACTIONS_VT act = pa.getNextAction2(startSO.partialState(), false, true);
                    actHist[act.toInt()]++;

                    //printDetailedResults(act);
                }
                // How often was it found that the agent chooses 0 (FOLD), 1 (CHECK), 2 (BET), 3 (CALL)?
                System.out.print("  found:");
                for (int i : actHist) System.out.print(i + " ");
                System.out.println("\n" + hashmap.get(numCard)[pact]);
                for (int zero : hashzero.get(numCard)[pact]) assert actHist[zero] == 0 : "actHist["+zero+"] is not 0 !";

            } // for (value)
        } // for (b)

//        for (int k=0; k<30; k++) {
//            System.out.print(ThreadLocalRandom.current().nextInt(5));
//        }
//        System.out.println();
        System.out.println("[agentKuhnPartialTest1st] with pa="+pa.getClass().getSimpleName()+" finished");
    }

    //
    // ------------ second player tests ------------------------------------------------------------------
    //

    /**
     * Checks for KuhnPoker and the second player 1: For a given state with a certain card {@code numCard}
     * and a certain action {@code p0act} that player 0 has played:
     * Is the agent MCTSExpectimax able to find the optimal next action or not?
     * <p>
     * It turns out that -- if {@code numCard}=10 (Q) and {@code p0act}=1 (CHECK) is selected -- only <b>two</b> of the
     * four tested switch combinations (see {@link #TEST_ONE_SWITCH_ONLY}), namely <ul>
     *      <li> {@link MCTSETreeNode#WITH_PARTIAL WITH_PARTIAL} = true                   and
     *      <li> {@link StateObserverKuhnPoker#PLAY_ONE_ROUND_ONLY PLAY_ONE_ROUND_ONLY} = arbitrary,
     * </ul>gets the desired result (20x action 1 = CHECK)
     */
    @Test
    public void mctseKuhnPartialTest2nd() {
        PlayAgent pa = new MCTSExpectimaxAgt("mctse",new ParMCTSE());

        // select a state with a Jack (9), Queen (10) or King (11) holecard for player 1:
        int numCard = 10;   // 9 10 11
        // advance player 0 with 1 (CHECK) or 2 (BET):
        int p0act = 1;  // 1  2

        agentKuhnPartialTest2nd(pa, numCard, p0act);
    }

    /**
     * Same as {@link #mctseKuhnPartialTest2nd()}, but loop over all {@code numCard} and {@code p0act}
     */
    @Test
    public void mctseKuhnAllTest2nd() {
        PlayAgent pa = new MCTSExpectimaxAgt("mctse",new ParMCTSE());

        int[] cards = {9,10,11};    // holecard for player 1: Jack (9), Queen (10) or King (11)
        int[] p0acts = {1,2};       // advance player 0 with 1 (CHECK) or 2 (BET)
        for (int numCard : cards) {
            for (int p0act : p0acts)
                agentKuhnPartialTest2nd(pa, numCard, p0act);
        }
    }

    /**
     * Same as {@link #mctseKuhnPartialTest2nd()}, but for agent ExpectimaxN
     */
    @Test
    public void expecKuhnPartialTest2nd() {
        PlayAgent pa = new ExpectimaxNAgent("expecN", 5);

        // select a state with a Jack (9), Queen (10) or King (11) holecard for player 1:
        int numCard = 10;   // 9 10 11
        // advance player 0 with 1 (CHECK) or 2 (BET):
        int p0act = 1;  // 1  2

        agentKuhnPartialTest2nd(pa, numCard, p0act);
    }

    /**
     * Same as {@link #expecKuhnPartialTest2nd()}, but loop over all {@code numCard} and {@code p0act}
     */
    @Test
    public void expecKuhnAllTest2nd() {
        PlayAgent pa = new ExpectimaxNAgent("expecN", 5);

        int[] cards = {9,10,11};    // holecard for player 1: Jack (9), Queen (10) or King (11)
        int[] p0acts = {1,2};        // advance player 0 with 1 (CHECK) or 2 (BET)
        for (int numCard : cards) {
            for (int p0act : p0acts)
                agentKuhnPartialTest2nd(pa, numCard, p0act);
        }
    }

    /**
     * Helper method for the four tests above
     */
    private void agentKuhnPartialTest2nd(PlayAgent pa, int numCard, int p0act) {
        // The desired action with J is either 1=CHECK or 2=BET if p0act=1 (CHECK). And it is 0=FOLD if p0act=2 (BET).
        // The desired action with Q is 1=CHECK if p0act=1 (CHECK). And it is either 0=FOLD or 3=CALL if p0act=2 (BET).
        // The desired action with K is 2 = BET if p0act=1 (CHECK). And it is 3=CALL if p0act=2 (BET).
        String[] desiredJ = {"-","optimal:0 *  * 0","optimal:20 0 0 0","-"};
        String[] desiredQ = {"-","optimal:0 20 0 0","optimal:* 0 0 *","-"};
        String[] desiredK = {"-","optimal:0 0 20 0","optimal:0 0 0 20","-"};
        int[][] desiredZerosJ = {{-1}, {0,3}, {1,2,3}};
        int[][] desiredZerosQ = {{-1}, {0,2,3}, {1,2}};
        int[][] desiredZerosK = {{-1}, {0,1,3}, {0,1,2}};
        HashMap<Integer, String[]> hashmap = new HashMap<>();
        hashmap.put( 9,desiredJ);
        hashmap.put(10,desiredQ);
        hashmap.put(11,desiredK);
        HashMap<Integer, int[][]> hashzero = new HashMap<>();
        hashzero.put( 9,desiredZerosJ);
        hashzero.put(10,desiredZerosQ);
        hashzero.put(11,desiredZerosK);

        String[] cardname = {"JACK","QUEEN","KING"};
        System.out.println("\n Card="+cardname[numCard-9]+", p0act="+p0act);
        StateObserverKuhnPoker startSO;
        boolean[] withPartialArr = (TEST_ONE_SWITCH_ONLY) ? new boolean[]{true} : new boolean[]{false,true};
        boolean[] oneRoundArr = (TEST_ONE_SWITCH_ONLY) ? new boolean[]{true} : new boolean[]{false,true};

        for (boolean p : withPartialArr) {
            MCTSETreeNode.WITH_PARTIAL = p;
            System.out.println("*** Test with WITH_PARTIAL="+MCTSETreeNode.WITH_PARTIAL+ " starts ***");

            for (boolean b : oneRoundArr) {
                StateObserverKuhnPoker.PLAY_ONE_ROUND_ONLY = b;
                System.out.println("   *** Part PLAY_ONE_ROUND_ONLY=" + StateObserverKuhnPoker.PLAY_ONE_ROUND_ONLY + " starts ***");

                // select a state with holecard = numCard for player 1:
                startSO = new StateObserverKuhnPoker();
                while (startSO.getHoleCards(1)[0].getRank() != numCard)
                    startSO = new StateObserverKuhnPoker();
                //System.out.println(startSO.stringDescr());

                // advance player 0 with 1 (CHECK) or 2 (BET):
                startSO.advance(new Types.ACTIONS(p0act));

                // now measure the reaction of player 1 (one out of 0 (FOLD), 1 (CHECK), 2 (BET), 3 (CALL)):
                int[] actHist = new int[4];
                for (int m = 0; m < 20; m++) {
                    Types.ACTIONS_VT act = pa.getNextAction2(startSO.partialState(), false, true);
                    actHist[act.toInt()]++;

                    //printDetailedResults(act);
                }

                // How often is it found that the agent chooses 0 (FOLD), 1 (CHECK), 2 (BET), 3 (CALL)?
                System.out.print("  found:");
                for (int count : actHist) System.out.print(count + " ");
                System.out.println("\n" + hashmap.get(numCard)[p0act]);
                for (int zero : hashzero.get(numCard)[p0act]) assert actHist[zero] == 0 : "actHist["+zero+"] is not 0 !";

            } // for (b)
        } // for (p)
        System.out.println("[agentKuhnPartialTest2nd] with pa="+pa.getClass().getSimpleName()+" finished");
    }

    private void printDetailedResults(Types.ACTIONS_VT act) {
        double[] vtab = act.getVTable();
        for (double v : vtab) System.out.print(v + " ");
        System.out.println("--> best action = "+act.toInt());
//                ArrayList<Types.ACTIONS> alist = startSO.getAvailableActions();
//                System.out.print("avail actions: ");
//                for (int k=0;k< alist.size();k++) System.out.print(alist.get(k).toInt()+" ");
//                System.out.println();
    }

    //
    // ------------ Poker -----------------------------------------------------------------------------
    //

    @Test
    public void testPoker() {
        StateObserverPoker startSO = new StateObserverPoker();

        System.out.println(startSO.stringDescr());
    }

}
