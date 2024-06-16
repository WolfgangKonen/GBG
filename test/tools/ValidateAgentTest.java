package tools;

import controllers.PlayAgent;
import games.*;

import java.util.ArrayList;


/**
 * Provide tests to validate an agent. These tests are generally admitted for any agent in any game.
 * <p>
 * The class does not contain test cases itself, it is used by test cases from other classes.
 *
 * @see ValidateAgentOthelloTest
 */
public class ValidateAgentTest {

    public boolean runTestScoreTuple(PlayAgent pa, Arena ar)
    {
        //
        // Check if pa.getScoreTuple and pa.estimateGameValueTuple run correctly through and return valid
        // numbers. Check this with different StateObservation objects so that there is at least one
        // state for every player. (Of course this test is not exhaustive.)
        //
        StateObservation sob = ar.getGameBoard().getDefaultStartState(null);
        boolean verbose = true;
        ScoreTuple sc;
        sc = pa.getScoreTuple(sob, null);
        checkScoreTuple(sc, sob, verbose);
        sc = pa.estimateGameValueTuple(sob, null);
        checkScoreTuple(sc, sob, verbose);
        for (int i = 1; i < sob.getNumPlayers(); i++) {
            Types.ACTIONS a = sob.getAvailableActions().get(0);
            sob.advance(a, null);
            sc = pa.getScoreTuple(sob, null);
            checkScoreTuple(sc, sob, verbose);
            sc = pa.estimateGameValueTuple(sob, null);
            checkScoreTuple(sc, sob, verbose);
        }
        return true;
    }

    /**
     * Check that {@code sc}'s values are valid, finite numbers, lie between min and max score
     * and that they sum to 0.0.
     *
     * @param sc
     * @param sob
     * @param verbose
     * @return
     */
    private boolean checkScoreTuple(ScoreTuple sc, StateObservation sob, boolean verbose) {
        double scMin = sob.getMinGameScore();
        double scMax = sob.getMaxGameScore();
        double scSum = 0.0;
        if (verbose) {
            System.out.print(sc);
            System.out.println("     "+scMin+"-->"+ scMax);
        }
        System.out.println(sob + " : gameOver=" + sob.isGameOver());
        for (int i=0; i<sc.scTup.length; i++) {
            scSum += sc.scTup[i];
            assert !Double.isNaN(sc.scTup[i]);
            assert Double.isFinite(sc.scTup[i]);
            if (!sob.isGameOver()) {	// there are cases (TDNT3+Othello) where a game-over state produces sc with
                                        // values outside interval [scMin,scMax] --> we exclude such cases from the test
                assert (scMin <= sc.scTup[i]) : "ScoreTuple < getMinScore() : "+sc.scTup[i]+" < "+scMin +" for StateObservation "+sob;
                assert (sc.scTup[i] <= scMax) : "ScoreTuple > getMaxScore() : "+sc.scTup[i]+" > "+scMax +" for StateObservation "+sob;
            }
        }
        assert (Math.abs(scSum) < 1e-20) : "ScoreTuple does not sum to 0: "+scSum;
        return true;
    }

    public boolean runTestFinalScoreTuple(PlayAgent pa, Arena ar) {
        StateObservation sob = ar.getGameBoard().getDefaultStartState(null);
        boolean verbose = true;
        ScoreTuple sc;
        //
        // Check if the final score tuple (which usually contains non-zero rewards) has a sum of zero
        // for 2-player games, i.e. 2 opposite entries:
        //
        while (!sob.isGameOver()) {
            ArrayList<Types.ACTIONS> arr = sob.getAvailableActions();
            Types.ACTIONS a = pa.getNextAction2(sob.partialState(), false, false, true);
            sob.advance(a, null);
        }
        sc = pa.getScoreTuple(sob, null);
        checkScoreTuple(sc,sob,verbose);
        sc = sob.getGameScoreTuple();
        checkScoreTuple(sc,sob,verbose);
        System.out.println("final getScoreTuple check ... OK");
        return true;
    }

    public void testTrainingEpisode(PlayAgent pa, Arena ar) {
        //
        // check if a training episode runs through successfully
        //
        System.out.print("train pa for one episode ... ");
        int num = pa.getGameNum();
        StateObservation sob = ar.getGameBoard().getDefaultStartState(null);
        pa.trainAgent(sob);
        assert (pa.getGameNum()-num == 1) : "Game counter not correctly incremented!";
        System.out.println("OK");
    }

    public void testBoardVectorSymmetries(Arena ar) {
        //
        // construct a board vector bv where each element is different and check that each board
        // vector returned by  xnf.symmetryVectors(bv) is
        //    a) a permutation of bv and
        //    b) different from all the others.
        //
        StateObservation sob = ar.getGameBoard().getDefaultStartState(null);
        XNTupleFuncs xnf = ar.makeXNTupleFuncs();
        BoardVector bv = xnf.makeBoardVectorEachCellDifferent();
        StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(sob,bv);
        BoardVector[] sym = xnf.symmetryVectors(curSOWB,0);
        boolean testPassed=true;
        for (int i=0; i<sym.length; i++) {
            if (!assertEachCellDifferent(sym[i])) {
                System.out.println("Error: some cell numbers in state sym["+i+"] appear not exactly once");
                testPassed=false;
            }
            for (int j=i+1; j<sym.length; j++) {
                if (!assertBvDifferent(sym[i], sym[j])) {
                    System.out.println("Error: symmetry states identical: "+i+", "+j);
                    testPassed=false;
                }
            }
        }
        assert (testPassed);

        System.out.println("BoardVector symmetries test ... OK");
    }

    // assert that bv1 is a permutation of [0,1,...,N-1] with N=bv1.bvec.length
    private boolean assertEachCellDifferent(BoardVector bv1) {
        // bcount[i] = how often appears cell value i in bv1 ?
        BoardVector bcount = new BoardVector(new int[bv1.bvec.length]);
        for (int i=0; i<bv1.bvec.length; i++) bcount.bvec[bv1.bvec[i]]++;
        for (int i=0; i<bcount.bvec.length; i++)
            if (bcount.bvec[i] != 1) return false;

        return true;
    }

    private boolean assertBvDifferent(BoardVector bv1, BoardVector bv2) {
        for (int i=0; i<bv1.bvec.length; i++)
            if (bv1.bvec[i]!=bv2.bvec[i]) return true;

        return false;
    }
}