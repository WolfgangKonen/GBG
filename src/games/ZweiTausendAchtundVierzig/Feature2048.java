package games.ZweiTausendAchtundVierzig;

import games.Feature;
import games.StateObservation;

/**
 * Created by Johannes on 21.06.2017.
 */
public class Feature2048 implements Feature{
    int featMode = 0;

    public Feature2048(int featMode) {
        this.featMode = featMode;
    }

    @Override
    public double[] prepareFeatVector(StateObservation sob) {
        assert sob instanceof StateObserver2048 : "Input 'so' is not of class StateObserver2048";
        StateObserver2048 so = (StateObserver2048) sob;

        switch (featMode){
            case 0:
                //ToDo: what is a Feat Vector and is this correct?
                return intArrayToDoubleArray(so.getBoardVector());
            default:
                throw new RuntimeException("Unknown featmode: " + featMode);
        }
    }

    @Override
    public String stringRepr(double[] featVec) {
        StringBuilder sb = new StringBuilder();
        for (double aFeatVec : featVec) {
            sb.append(aFeatVec);
            sb.append(", ");
        }

        sb.delete(sb.length()-2, sb.length());

        return sb.toString();
    }

    @Override
    public int getFeatmode() {
        return featMode;
    }

    @Override
    public int[] getAvailFeatmode() {
        return new int[]{0};
    }

    @Override
    public int getInputSize(int featmode) {
        switch (featmode){
            case 0:
                return 16;
            default:
                throw new RuntimeException("Unknown featmode: " + featmode);
        }
    }

    public static double[] intArrayToDoubleArray(int[] intArray) {
        double[] doubleArray = new double[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            doubleArray[i] = intArray[i];
        }
        return doubleArray;
    }
}
