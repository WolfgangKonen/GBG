package controllers.MCTSWrapper.stateApproximation;

import controllers.MCTSWrapper.utils.Tuple;
import controllers.PlayAgent;
import games.StateObservation;

import java.util.Arrays;

/**
 * A component that approximates the value v and the
 * vector of move probabilities p of a StateObservation by a wrapped PlayAgent.
 */
public final class PlayAgentApproximator implements Approximator {
    private final PlayAgent agent;

    public PlayAgentApproximator(final PlayAgent agent) {
        this.agent = agent;
    }

    /**
     * @return The approximator's estimate of the final score for that game state.
     */
    public double getScore(final StateObservation sob) {
        return agent.getScore(sob);
    }

    /**
     * Predicts the value v and the move probabilities p of a given StateObservation.
     *
     * @return A tuple containing the value v and an array for the vector p.
     */
    @Override
    public Tuple<Double, double[]> predict(final StateObservation stateObservation) {
        final var actions_vt = agent.getNextAction2(stateObservation, false, true);
        return new Tuple<>(
            actions_vt.getVBest(),
            moveProbabilitiesForVTable(actions_vt.getVTable(), stateObservation)
        );
    }

    private static double[] moveProbabilitiesForVTable(final double[] vTable, final StateObservation stateObservation) {
        return softmax(
            vTable.length > stateObservation.getNumAvailableActions() // For historical reasons the vTable is sometimes
                                                                      // larger by 1 than the number of available actions.
                ? Arrays.copyOfRange(vTable, 0, stateObservation.getNumAvailableActions())
                : vTable
        );
    }

    private static double[] softmax(final double[] values) {
        final var exponentialValues = Arrays.stream(values).map(
            Math::exp
        ).toArray();

        final var sum = Arrays.stream(exponentialValues).sum();

        return Arrays.stream(exponentialValues).map(
            v -> v / sum
        ).toArray();
    }
}
