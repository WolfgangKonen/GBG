package controllers.MCTSWrapper.passStates;

import controllers.MCTSWrapper.utils.StateObservationExtensions;
import games.StateObservation;

/**
 * Represents a pseudo action that must be performed
 * by a player when no real moves are possible.
 */
public final class PassAction implements ApplicableAction {
    /**
     * Applies the pass action to a passed StateObservation.
     * <p>
     * A pass action does not really modify the game state,
     * since the current player has no available moves.
     * Therefore, a pass action only changes the information about
     * which player's turn it is and updates the available actions.
     *
     * @param so StateObservation instance to which the action should be applied.
     * @return A new StateObservation instance which results from applying
     * the action to the given StateObservation.
     * So the given StateObservation instance remains unchanged.
     */
    @Override
    public StateObservation applyTo(final StateObservation so) {
        return StateObservationExtensions.passToNextPlayer(so);
    }

    @Override
    public int getId() {
        // Since a pass action does not address a specific board position,
        // the pseudo id MIN_VALUE is used here, since it is very unlikely
        // that this value is used for real actions.
        return Integer.MIN_VALUE;
    }
}
