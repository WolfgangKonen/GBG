package games;

public interface StateObservationVectorFuncs {

    /**
     *
     * @return A flattened vector with discrete values representing the current stateObservation for SB3 Agent normally just the game table if no other features are added.
     * the contained valuse have to be of a distingt set of Integer from getObservationStartValue to getObservationStartValue + getObseravtion
     */
    int[] getStateObservationVector(StateObservation stateObservation);

    int[] getObservationVectorRanges();

    int[] getStateObservationVectorStarts();

    boolean isObservationDiscrete();
}
