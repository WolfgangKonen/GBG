package tools;

import controllers.PlayAgent;
import games.Arena;
import games.StateObservation;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

/**
 * Provide tests to validate a StateObservation object. These tests are generally admitted for any StateObservation in any game.
 * <p>
 * The class does not contain test cases itself, it is used by test cases from other classes.
 *
 * @see ValidateStateObsOthelloTest
 * @see ValidateStateObsSimTest
 */
public class ValidateStateObsTest {
    StateObservation sob;
    PlayAgent pa;

    public boolean runTestScoreTuple(StateObservation sob, PlayAgent pa, Arena ar) {
        Types.ACTIONS a;
        ScoreTuple sc;
        StateObservation newSob = sob.copy();
        boolean verbose = true;
        boolean random = true;
        boolean silent = true;

        //
        // Check if sob.getGameScoreTuple runs correctly through and returns valid numbers.
        //
        int n = 0;
        while (!newSob.isGameOver()) {
            sc = newSob.getGameScoreTuple();
            if (n++ > 4) verbose = false;
            checkScoreTuple(sc, newSob, verbose);
            a = pa.getNextAction2(newSob.partialState(), random, silent);
            newSob.advance(a, null);
        }
        System.out.println("getGameScoreTuple check ... OK");

        //
        // Check for 2-player games whether the final score tuple (which usually contains non-zero rewards)
        //  has a sum of zero, i.e. 2 opposite entries.
        //
        sc = newSob.getGameScoreTuple();
        checkScoreTuple(sc, newSob, true);
        if (sob.getNumPlayers() == 2) {
            assert (sc.scTup[1] == -sc.scTup[0]) : "elements of final ScoreTuple do not sum to zero";
        }
        System.out.println("final getScoreTuple check ... OK");
        return true;
    }

    public boolean runTestSerializable(StateObservation sob, boolean verbose) {
        //
        // Check whether sob is serializable (needed for logging)
        //
        try {
            String tempPath = "logs\\temp";
            tools.Utils.checkAndCreateFolder(tempPath);
            FileOutputStream fos = new FileOutputStream(tempPath + "\\temp_.temp");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(sob);
            fos.close();
            oos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Serialization failed for "+sob.getClass().getSimpleName());
            return false;
        }
    }

    private boolean checkScoreTuple(ScoreTuple sc, StateObservation sob, boolean verbose) {
        double scMin = sob.getMinGameScore();
        double scMax = sob.getMaxGameScore();
        if (verbose) {
            System.out.print(sc);
            System.out.println("     "+scMin+"-->"+ scMax);
        }
        for (int i=0; i<sc.scTup.length; i++) {
            assert !Double.isNaN(sc.scTup[i]);
            assert Double.isFinite(sc.scTup[i]);
            assert (scMin <= sc.scTup[i]) : "ScoreTuple < getMinScore() : "+sc.scTup[i]+" < "+scMin;
            assert (sc.scTup[i] <= scMax) : "ScoreTuple > getMaxScore() : "+sc.scTup[i]+" > "+scMax;
        }
        return true;
    }

}