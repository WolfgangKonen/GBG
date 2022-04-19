package controllers.MCTSExpWrapper.stateApproximation2;

import controllers.MCTSWrapper.utils.Tuple;
import controllers.PlayAgent;
import games.StateObservation;
import tools.ScoreTuple;
import tools.Types;

/**
 * A component that approximates the {@link ScoreTuple} and the
 * vector of move probabilities <b>p</b> of a state as predicted by a wrapped {@link PlayAgent}.
 * <p>
 * Difference to {@link controllers.MCTSWrapper.stateApproximation.Approximator Approximator}:
 * The method {@link #predict(StateObservation)} returns a {@link ScoreTuple} as first Tuple element.
 */
public interface Approximator2 {
    /**
     * Predicts the {@link ScoreTuple} and the move probabilities p of a given state.
     *
     * @return A tuple containing the {@link ScoreTuple} for the best action and an array for the vector <b>p</b>.
     */
    Tuple<ScoreTuple, double[]> predict(final StateObservation stateObservation);

    Types.ACTIONS_VT getNextAction(StateObservation stateObservation);

    /**
     * @return The approximator's estimate of the final score for that game state.
     */
    double getScore(final StateObservation sob);

    String getName();

    PlayAgent getWrappedPlayAgent();
}
