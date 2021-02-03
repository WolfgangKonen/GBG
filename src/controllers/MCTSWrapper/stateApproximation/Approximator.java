package controllers.MCTSWrapper.stateApproximation;

import controllers.MCTSWrapper.utils.Tuple;
import games.StateObservation;

/**
 * A component used to approximate the value v and
 * the vector of move probabilities p of a given StateObservation.
 */
public interface Approximator {
    /**
     * Predicts the value v and the move probabilities p of a given StateObservation.
     *
     * @return A tuple containing the value v and an array for the vector p.
     */
    Tuple<Double, double[]> predict(final StateObservation stateObservation);

    /**
     * @return The approximator's estimate of the final score for that game state.
     */
    double getScore(final StateObservation sob);

    String getName();
}
