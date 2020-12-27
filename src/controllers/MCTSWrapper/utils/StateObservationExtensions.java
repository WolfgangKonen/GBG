package controllers.MCTSWrapper.utils;

import games.Othello.BaseOthello;
import games.Othello.StateObserverOthello;
import games.StateObservation;

/**
 * Contains utility functions to extend the functionality of StateObservation instances.
 */
public final class StateObservationExtensions {
    /**
     * Swaps a passed game state's current player.
     * <p>
     * Attention: Currently only working for Othello.
     *
     * @param so Affected game state.
     * @return A new game state where it is the next player's turn.
     */
    public static StateObservation passToNextPlayer(final StateObservation so) {
        final var stateCopy = so.copy();
        stateCopy.passToNextPlayer();
//        ((StateObserverOthello) stateCopy).setPlayer(BaseOthello.getOpponent(stateCopy.getPlayer()));
        stateCopy.setAvailableActions();
        return stateCopy;
    }
}
