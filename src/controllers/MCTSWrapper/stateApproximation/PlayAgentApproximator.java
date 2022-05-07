package controllers.MCTSWrapper.stateApproximation;

import controllers.MCTSWrapper.ConfigWrapper;
import controllers.MCTSWrapper.utils.Tuple;
import controllers.PlayAgent;
import games.StateObservation;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A component that approximates the value v and the
 * vector of move probabilities <b>p</b> of a state as predicted by a wrapped {@link PlayAgent}.
 */
public final class PlayAgentApproximator implements Approximator, Serializable {
    private final PlayAgent agent;

    public PlayAgentApproximator(final PlayAgent agent) {
        this.agent = agent;
    }

    /**
     * Predicts the value v and the move probabilities p of a given state.
     *
     * @return A tuple containing the value v for the best action and an array for the vector <b>p</b>.
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
        final var minV = stateObservation.getMinGameScore();
        final var maxV = stateObservation.getMaxGameScore();
        return optSoftmax(vTable,minV,maxV);
    }

    private static double[] optSoftmax(final double[] values, final double minV, final double maxV) {
        return (ConfigWrapper.USESOFTMAX) ? softmax(values) : minmaxmap(values,minV,maxV);
    }

    /*
     * map values by softmax function. This ensures that sum(values)=1, i.e. values can be interpreted as probability
     * distribution
     */
    private static double[] softmax(final double[] values) {
        final var exponentialValues = Arrays.stream(values).map(
            Math::exp
        ).toArray();

        final var sum = Arrays.stream(exponentialValues).sum();

        return Arrays.stream(exponentialValues).map(
            v -> v / sum
        ).toArray();
    }

    /*
     * map values into range [0,1]
     */
    private static double[] minmaxmap(final double[] values, final double minV, final double maxV) {
        final var vrange = maxV-minV;
        assert vrange > 0 : "[minmaxmap] vrange is not positive!";

        return Arrays.stream(values).map(
                v -> (v-minV) / vrange
        ).toArray();
    }

//    /**
//     * @return The approximator's estimate of the final score for that game state.
//     */
//    public double getScore(final StateObservation sob) {
//        return agent.getScore(sob);
////        int P = sob.getPlayer();        // suggestion WK - but probably not needed yet
////        return sob.isGameOver() ? sob.getRewardTuple(true).scTup[P]
////                                : agent.getScore(sob) + sob.getStepRewardTuple().scTup[P];
//    }
//    // /WK/ If the method were really used (currently it is not), it would be perhaps necessary to include the reward
//    //      and step reward as indicated in the now commented lines

    public String getName() { return agent.getName(); }

    public PlayAgent getWrappedPlayAgent() {
        return agent;
    }


}
