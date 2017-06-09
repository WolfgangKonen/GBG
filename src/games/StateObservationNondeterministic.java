package games;

import javafx.util.Pair;
import tools.Types;

import java.util.ArrayList;

/**
 * This interface is used to implement nondeterministic games like 2048.
 * It extends the normal StateObservation interface
 *
 * Created by Johannes on 17.05.2017.
 */
public interface StateObservationNondeterministic extends StateObservation {
    /**
     * Advance the current state to a new state using a deterministic Action
     *
     * @param action the action
     */
    void advanceDeterministic(Types.ACTIONS action);

    /**
     * Advance the current state to a new state using a nondeterministic Action
     */
    void advanceNondeterministic();

    /**
     * Get the next nondeterministic action
     *
     * @return the action, null if the next action is deterministic
     */
    Types.ACTIONS getNextNondeterministicAction();

    StateObservationNondeterministic copy();
}
