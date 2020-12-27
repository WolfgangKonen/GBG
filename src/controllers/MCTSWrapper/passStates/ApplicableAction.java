package controllers.MCTSWrapper.passStates;

import controllers.MCTSWrapper.utils.Identifiable;
import games.StateObservation;

/**
 * Represents an identifiable action that can be applied to a game state.
 */
public interface ApplicableAction extends Identifiable {
    /**
     * Applies the action to a passed StateObservation.
     *
     * @param so StateObservation instance to which the action should be applied.
     * @return A new StateObservation instance which results from applying
     * the action to the given StateObservation.
     * So the given StateObservation instance remains unchanged.
     */
    StateObservation applyTo(final StateObservation so);
}
