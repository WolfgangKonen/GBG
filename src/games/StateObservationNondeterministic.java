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
     * Advance the current state with a deterministic action to a new state
     *
     * @param action the action
     */
    void advanceDeterministic(Types.ACTIONS action);

    /**
     * Get the available deterministic actions
     *
     * @return the actions
     */
    ArrayList<Types.ACTIONS> getAvailableActionsDeterministic();

    /**
     * Update the available deterministic actions
     */
    void setAvailableActionsDeterministic();

    /**
     * @return the number of available deterministic actions
     */
    int getNumAvailableActionsDeterministic();

    /**
     * Advance the current state with a nondeterministic action to a new state
     *
     * @param action the action
     */
    void advanceNondeterministic(Types.ACTIONS action);

    /**
     * Get the available nondeterministic actions and the weight associated with each action
     *
     * @return the actions
     */
    ArrayList<Pair<Types.ACTIONS, Double>> getAvailableActionsNondeterministic();

    /**
     * Update the available nondeterministic actions
     */
    void setAvailableActionsNondeterministic();

    /**
     * @return the number of available nondeterministic actions
     */
    int getNumAvailableActionsNondeterministic();

    /**
     * Is the next action deterministic or nondeterministic?
     *
     * @return true if the next action is deterministic, false if it is nondeterministic
     */
    boolean isNextActionDeterminisitc();
}
