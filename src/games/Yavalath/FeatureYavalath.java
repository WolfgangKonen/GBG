package games.Yavalath;

import games.Feature;
import games.StateObservation;

public class FeatureYavalath implements Feature {

    @Override
    public double[] prepareFeatVector(StateObservation so) {
        return new double[0];
    }

    @Override
    public String stringRepr(double[] featVec) {
        return null;
    }

    @Override
    public int getFeatmode() {
        return 0;
    }

    @Override
    public int[] getAvailFeatmode() {
        return new int[0];
    }

    @Override
    public int getInputSize(int featmode) {
        return 0;
    }
}
