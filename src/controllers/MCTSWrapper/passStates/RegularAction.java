package controllers.MCTSWrapper.passStates;

import controllers.MCTSWrapper.utils.StateObservationExtensions;
import controllers.MCTSWrapper.utils.Tuple;
import games.StateObsNondeterministic;
import games.StateObservation;
import tools.Types;

/**
 * Represents a real action that is applicable to a game state.
 * For this use case an action of the type Types.ACTIONS is wrapped.
 */
public final class RegularAction implements ApplicableAction {
    private final Types.ACTIONS action;

    public RegularAction(final Types.ACTIONS action) {
        this.action = action;
    }

    /**
     * Applies a regular action to a passed StateObservation.
     * <p>
     * Since the GBG framework skips game states in which a
     * player has to pass, this method reverses the skip and
     * returns the state in which the player has to pass.
     *
     * @param so StateObservation instance to which the action should be applied.
     * @return A new StateObservation instance which results from applying
     * the action to the given StateObservation.
     * So the given StateObservation instance remains unchanged.
     */
    @Override
    public StateObservation applyTo(final StateObservation so) {
        final var stateCopy = so.copy();
        stateCopy.advance(action, null);

        if (so.getNumPlayers()==1)  return stateCopy;
        // WK: fix for all 1-player games. If we don't return here, the app will run into swapCurrentPlayer and then
        // stop with a RuntimeException because the 1-player game does not override setPlayer(int) [why should it?]

        return stateCopy.getPlayer() == so.getPlayer() // If it is still the same player's turn after the action has been performed,
                                                       // a passing situation has occurred that has been skipped.
            ? StateObservationExtensions.passToNextPlayer(stateCopy)
            : stateCopy;
    }

    /**
     * This is for MCTSExpWrapper (without pass possibility)
     *
     * @return return a copy of so which is deterministically advanced by this.action
     */
    @Override
    public StateObservation advanceDet(final StateObservation so) {
        final var stateCopy = so.copy();
        stateCopy.advanceDeterministic(action);
        return stateCopy;
    }

    /**
     * This is for MCTSExpWrapper (without pass possibility)
     *
     * @return return a copy of so which is non-deterministically advanced by this.action
     */
    @Override
    public Tuple<Types.ACTIONS,StateObsNondeterministic> advanceNonDet(final StateObsNondeterministic so) {
        final var stateCopy = so.copy();
        final var r = stateCopy.advanceNondetSpecific(action);
        return new Tuple( r, stateCopy);
    }

    /**
     * An action is identified by the board position it addresses.
     */
    @Override
    public int getId() {
        return action.toInt();
    }

    @Override
    public Types.ACTIONS getAction() { return action; }
}
