package games.CFour;


import games.StateObservation;
import games.StateObservationVectorFuncs;

public class StateObservationVectorFuncsC4 implements StateObservationVectorFuncs {

    @Override
    public int[] getStateObservationVector(StateObservation stateObservation) {
        return new int[0];
    }

    @Override
    public int[] getObservationVectorRanges() {
        return new int[0];
    }

    @Override
    public int[] getStateObservationVectorStarts() {
        return new int[0];
    }

    @Override
    public boolean isObservationDiscrete() {
        return false;
    }
}
