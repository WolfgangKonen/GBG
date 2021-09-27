package controllers.MCTSExpectimax;

import controllers.ExpectimaxNAgent;
import controllers.PlayAgent;
import games.KuhnPoker.GameBoardKuhnPoker;
import games.KuhnPoker.StateObserverKuhnPoker;
import params.ParMCTSE;
import starters.GBGBatch;
import tools.Types;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

public class MCTSExpectimaxTest extends GBGBatch {

    /**
     * This test checks for KuhnPoker a state with the Queen for the starting player:
     * Is the agent MCTSExpectimax able to find the optimal next action or not?
     * <p>
     * It turns out that only the <b>one</b> combination <ul>
     *      <li> MCTSETreeNode.WITH_PARTIAL = true                   and
     *      <li> StateObserverKuhnPoker.PLAY_ONE_ROUND_ONLY = true
     * </ul>of the tested four combinations gets the desired result (always action 1 = CHECK)
     */
    @Test
    public void mctseKuhnPartialTest1st() {
        PlayAgent pa;
//        String selectedGame = "KuhnPoker";
//        String[] scaPar = GBGBatch.setDefaultScaPars(selectedGame);
//        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
//        GameBoardKuhnPoker gb = new GameBoardKuhnPoker(t_Game); //,3,2);		// needed for chooseStartState()
        StateObserverKuhnPoker startSO;
        boolean[] withPartialArr = {false,true};
        boolean[] oneRoundArr = {false,true};

        pa = new MCTSExpectimaxAgt("mctse",new ParMCTSE());

        for (boolean b : withPartialArr) {
            MCTSETreeNode.WITH_PARTIAL = b;
            System.out.println("\n*** Test WITH_PARTIAL=" + MCTSETreeNode.WITH_PARTIAL + " starts ***");

            for (boolean value : oneRoundArr) {
                StateObserverKuhnPoker.PLAY_ONE_ROUND_ONLY = value;
                System.out.println("   *** Part PLAY_ONE_ROUND_ONLY=" + StateObserverKuhnPoker.PLAY_ONE_ROUND_ONLY + " starts ***");

                // select a state with a Queen holecard for player 0:
                startSO = new StateObserverKuhnPoker();
                while (startSO.getHoleCards(0)[0].getRank() != 10)
                    startSO = new StateObserverKuhnPoker();
                //System.out.println(startSO.stringDescr());

                int[] actHist = new int[4];
                for (int m = 0; m < 20; m++) {
                    Types.ACTIONS_VT act = pa.getNextAction2(startSO.partialState(), false, true);
                    actHist[act.toInt()]++;

                    //printDetailedResults(act);
                }
                System.out.print("  found:");
                for (int i : actHist) System.out.print(i + " ");
                System.out.println("\n" + "optimal:0 20 0 0");    // the desired action is 1 = CHECK

            } // for (value)
        } // for (b)

//        for (int k=0; k<30; k++) {
//            System.out.print(ThreadLocalRandom.current().nextInt(5));
//        }
//        System.out.println();
        System.out.println("[mctseKuhnPartialTest1st] finished");
    }

    /**
     * This test checks for KuhnPoker a state with Queen for the second player and the starting player has played CHECK:
     * Is the agent MCTSExpectimax able to find the optimal next action (it is a CHECK) or not?
     * <p>
     * It turns out that only the <b>one</b> combination <ul>
     *      <li> MCTSETreeNode.WITH_PARTIAL = true                   and
     *      <li> StateObserverKuhnPoker.PLAY_ONE_ROUND_ONLY = true
     * </ul>of the tested four combinations gets the desired result (always action 1 = CHECK)
   */
    @Test
    public void mctseKuhnPartialTest2nd() {
        PlayAgent pa;
        boolean silent = true;

        String selectedGame = "KuhnPoker";
        StateObserverKuhnPoker startSO;
        boolean[] withPartialArr = {false,true};
        boolean[] oneRoundArr = {false,true};

        pa = new MCTSExpectimaxAgt("mctse",new ParMCTSE());
        //pa = new ExpectimaxNAgent("expN",5);

        for (boolean p : withPartialArr) {
            MCTSETreeNode.WITH_PARTIAL = p;
            System.out.println("\n*** Test with WITH_PARTIAL="+MCTSETreeNode.WITH_PARTIAL+ " starts ***");

            for (boolean b : oneRoundArr) {
                StateObserverKuhnPoker.PLAY_ONE_ROUND_ONLY = b;
                System.out.println("   *** Part PLAY_ONE_ROUND_ONLY=" + StateObserverKuhnPoker.PLAY_ONE_ROUND_ONLY + " starts ***");

                // select a state with a Queen holecard for player 1:
                startSO = new StateObserverKuhnPoker();
                while (startSO.getHoleCards(1)[0].getRank() != 11)
                    startSO = new StateObserverKuhnPoker();
                //System.out.println(startSO.stringDescr());

                // advance player-0 with 1 (CHECK) or 2 (BET):
                startSO.advance(new Types.ACTIONS(1));

                // now test the reaction of player 1 (should be 1=CHECK on player-1-CHECK and 3=CALL on player-1-BET):
                int[] actHist = new int[4];
                for (int m = 0; m < 20; m++) {
                    Types.ACTIONS_VT act = pa.getNextAction2(startSO.partialState(), false, true);
                    actHist[act.toInt()]++;

                    //printDetailedResults(act);
                }
                System.out.print("  found:");
                for (int k = 0; k < actHist.length; k++) System.out.print(actHist[k] + " ");
                System.out.println("\n" + "optimal:0 20 0 0");    // the desired action is 1 = CHECK

            } // for (b)
        } // for (p)
        System.out.println("[mctseKuhnPartialTest2nd] finished");
    }

    private void printDetailedResults(Types.ACTIONS_VT act) {
        double[] vtab = act.getVTable();
        for (int k=0;k<vtab.length;k++) System.out.print(vtab[k]+" ");
        System.out.println("--> best action = "+act.toInt());
//                ArrayList<Types.ACTIONS> alist = startSO.getAvailableActions();
//                System.out.print("avail actions: ");
//                for (int k=0;k< alist.size();k++) System.out.print(alist.get(k).toInt()+" ");
//                System.out.println();
    }

}
