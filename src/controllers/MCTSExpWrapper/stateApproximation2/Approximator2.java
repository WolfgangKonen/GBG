package controllers.MCTSExpWrapper.stateApproximation2;

import controllers.MCTSWrapper.utils.Tuple;
import games.StateObservation;
import tools.ScoreTuple;
import tools.Types;

/**
 * A component used to approximate the value v and
 * the vector of move probabilities p of a given StateObservation.
 */
public interface Approximator2 {
    /**
     * Predicts the value v and the move probabilities p of a given StateObservation.
     *
     * @return A tuple containing the value v and an array for the vector p.
     */
    Tuple<ScoreTuple, double[]> predict(final StateObservation stateObservation);

    Types.ACTIONS_VT getNextAction(StateObservation stateObservation);

    /**
     * @return The approximator's estimate of the final score for that game state.
     */
    double getScore(final StateObservation sob);

    String getName();
}
