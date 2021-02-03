package controllers.MCTSWrapper.stateApproximation;

import controllers.MCTSWrapper.ConfigWrapper;
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
//        int P = sob.getPlayer();        // suggestion WK - but probably not needed yet
//        return sob.isGameOver() ? sob.getRewardTuple(true).scTup[P]
//                                : agent.getScore(sob) + sob.getStepRewardTuple().scTup[P];
    }
    // /WK/ If the method were really used (currently it is not), it would be perhaps necessary to include the reward
    //      and step reward as indicated in the now commented lines

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
        assert (vTable.length == stateObservation.getNumAvailableActions()) : "Ooops, wrong size for vTable!";
        return optSoftmax(vTable);
//        return optSoftmax(
//            vTable.length > stateObservation.getNumAvailableActions() // For historical reasons the vTable is sometimes
//                                                                      // larger by 1 than the number of available actions.
//                ? Arrays.copyOfRange(vTable, 0, stateObservation.getNumAvailableActions())
//                : vTable
//        );
    }

    private static double[] optSoftmax(final double[] values) {
        return (ConfigWrapper.USESOFTMAX) ? softmax(values) : values;
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

    public String getName() { return agent.getName(); }
}
