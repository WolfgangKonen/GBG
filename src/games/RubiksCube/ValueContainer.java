package games.RubiksCube;

import controllers.PlayAgent;
import games.StateObservation;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.OptionalDouble;

/**
 * A set of {@code cubes} with their predicted {@code values} and solution lengths {@code pSolve}. <br>
 * This object is also capable to calculate mean, std, min and max of array {@code values}
 *
 * @see #printHMapVC(HashMap, PrintWriter)
 */
public class ValueContainer {
    public double[] values;
    private final double[] sq_dev;
    public StateObservation[] cubes;
    public int[] pSolve;
    public double mean;
    public double std;
    public OptionalDouble min;
    public OptionalDouble max;

    public ValueContainer(int nump) {
        values = new double[nump];
        sq_dev = new double[nump];
        cubes = new StateObservation[nump];
        pSolve = new int[nump];
    }

    public void calcMeanStd() {
        mean = Arrays.stream(values).sum() / values.length;
        for (int n = 0; n < values.length; n++) sq_dev[n] = Math.pow(values[n] - mean, 2);
        std = Math.sqrt(Arrays.stream(sq_dev).sum() / (sq_dev.length - 1));
        min = Arrays.stream(values).min();
        max = Arrays.stream(values).max();

    }

    public ValueContainer takeSolvedCubes() {
        int countSolved = 0;
        for (int p : pSolve)
            if (p != -1) countSolved++;

        if (countSolved == 0) return null;

        ValueContainer vcSolved = new ValueContainer(countSolved);

        for (int n = 0, i = 0; n < pSolve.length; n++)
            if (pSolve[n] != -1) {
                vcSolved.cubes[i] = cubes[n];
                vcSolved.values[i] = values[n];
                vcSolved.pSolve[i] = pSolve[n];
                i++;
            }

        vcSolved.calcMeanStd();
        return vcSolved;
    }

    public ValueContainer takeUnsolvedCubes() {
        int countUnsolved = 0;
        for (int p : pSolve)
            if (p == -1) countUnsolved++;

        if (countUnsolved == 0) return null;

        ValueContainer vcUnsolved = new ValueContainer(countUnsolved);

        for (int n = 0, i = 0; n < pSolve.length; n++)
            if (pSolve[n] == -1) {
                vcUnsolved.cubes[i] = cubes[n];
                vcUnsolved.values[i] = values[n];
                vcUnsolved.pSolve[i] = pSolve[n];
                i++;
            }

        vcUnsolved.calcMeanStd();
        return vcUnsolved;
    }

    /**
     * Print a {@link HashMap} of {@link ValueContainer} objects
     * @param hm		the {@link HashMap}
     * @param mtWriter	where to print
     *
     * @see starters.MCubeIterSweep#predict_value(PlayAgent, int, int, GameBoardCube, PrintWriter)
     */
    public static void printHMapVC(HashMap<Integer, ValueContainer> hm, PrintWriter mtWriter) {
        DecimalFormat form = new DecimalFormat("000");
        DecimalFormat form2 = new DecimalFormat("0000");
        DecimalFormat fper = new DecimalFormat("00.0000");
        mtWriter.println("  p,  num:     mean    stdev      min      max");
        for (Integer p : hm.keySet()) {
            ValueContainer vc = hm.get(p);
            if (vc==null) {
                mtWriter.println(form.format(p) + ": vc is null");
            } else {
                mtWriter.println(form.format(p) + ", " + form2.format(vc.values.length) + ":  "
                        + fper.format(vc.mean) + ", "
                        + fper.format(vc.std) + ", "
                        + fper.format(vc.min.orElse(Double.NaN)) + ", "
                        + fper.format(vc.max.orElse(Double.NaN))
                );
            }
        }
    }

} // ValueContainer
