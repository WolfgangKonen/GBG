package controllers.MCTSWrapper.passStates;

import controllers.MCTSWrapper.stateApproximation.Approximator;
import controllers.MCTSWrapper.utils.Lazy;
import controllers.MCTSWrapper.utils.StateObservationExtensions;
import controllers.MCTSWrapper.utils.Tuple;
import games.ObserverBase;
import games.StateObservation;

import java.util.ArrayList;

/**
 * Represents a game state where pass states are not skipped.
 * For this purpose a StateObservation instance is wrapped.
 */
public final class GameStateIncludingPass {
    /**
     * Wrapped StateObservation on which the main operations are performed under the hood.
     */
    private final StateObservation state;

    /**
     * Lazy initialized property that indicates whether
     * the player whose turn it is in this state must pass.
     */
    public final Lazy<Boolean> lazyMustPass;
    /**
     * Lazy initialized collection of applicable actions possible from this game state.
     * If the player has to pass, the collection only contains a pass action.
     */
    private final Lazy<ApplicableAction[]> lazyAvailableActions;

    /**
     * Initializes the GameStateWithPasses instance based on a given StateObservation.
     */
    public GameStateIncludingPass(
        final StateObservation stateObservation
    ) {
        state = stateObservation;
        lazyMustPass = new Lazy<>(
            () -> stateObservation.getNumAvailableActions() == 0
        );
        lazyAvailableActions = new Lazy<>(this::availableActionsIncludingPassActions);
    }

    /**
     * Calculates the actions available from the current game state including pass actions.
     *
     * @return A collection of applicable actions possible from this game state.
     * If no actions are possible the collection contains a pass action.
     */
    private ApplicableAction[] availableActionsIncludingPassActions() {
        return lazyMustPass.value()
            ? new ApplicableAction[]{new PassAction()}
            : state.getAvailableActions().stream().map(RegularAction::new).toArray(ApplicableAction[]::new);
    }

    /**
     * Applies an applicable action to the current game state.
     * This behavior is polymorphic, since a given action can be both a regular action and a pass action.
     *
     * @param action Action to be applied.
     * @return A new GameStateWithPasses instance.
     */
    public GameStateIncludingPass advance(final ApplicableAction action) {
        return new GameStateIncludingPass(
            action.applyTo(state)
        );
    }

    /**
     * @return Actions available from the current game state including pass actions.
     */
    public ApplicableAction[] getAvailableActionsIncludingPassActions() {
        return lazyAvailableActions.value();
    }

    /**
     * @return The information if the game state is a terminating state.
     */
    public boolean isFinalGameState() {
        return state.isGameOver();
    }

    /**
     * @return The final game result of a terminating game state.
     */
    public double getFinalGameScore() {
        return state.getGameScore(state);
    }

    public int getNumPlayers() { return state.getNumPlayers(); }

    public int getMoveCounter() { return state.getMoveCounter(); }
    public String stringDescr() { return state.stringDescr(); }
    public ArrayList<Integer> getLastMoves() { return ((ObserverBase) state).getLastMoves(); }

    /**
     * Delegates the approximation of the values v and p necessary for a Monte Carlo Tree Search to a given approximator.
     * <p>
     * Attention:
     * If the game state is a pass situation, which means that there are no real available actions that can be evaluated,
     * in this case the negated evaluation from the opposing player's point of view is used for the value v.
     * The vector of probabilities of possible actions p will then provide a 100% probability of a pass action,
     * since this is the only possible action.
     *
     * @param approximator A component that is able to approximate the necessary values v and p for a Monte Carlo Tree Search.
     * @return A tuple containing the necessary values v and p for a Monte Carlo Tree Search.
     */
    public Tuple<Double, double[]> getApproximatedValueAndMoveProbabilities(final Approximator approximator) {
        return lazyMustPass.value()
            ? approximateValueAndMoveProbabilitiesForPassingState(approximator)
            : approximator.predict(state);
    }

    /**
     * For a passing state the negated evaluation from the opposing player's point of view is used for the value v.
     * The vector of probabilities of possible actions p will then provide a 100% probability of a pass action,
     * since this is the only possible action.
     *
     * @param approximator A component that is able to approximate the necessary values v and p for a Monte Carlo Tree Search.
     * @return A tuple containing the necessary values v and p for a Monte Carlo Tree Search.
     */
    private Tuple<Double, double[]> approximateValueAndMoveProbabilitiesForPassingState(final Approximator approximator) {
        final var swappedState = new GameStateIncludingPass(
            StateObservationExtensions.passToNextPlayer(state)
        );

        final var valueAndPolicy = approximator.predict(swappedState.state);

        assert(state.getNumPlayers()==2) : "Error in GameStateIncludingPass: Tuple creation is only valid for 2-player games!";

        return new Tuple<>(
            -valueAndPolicy.element1, // negated evaluation from the opposing player's point of view
            new double[]{1.0} // 100% probability of a pass action, since a pass action is the single remaining possible action
        );
    }
}
