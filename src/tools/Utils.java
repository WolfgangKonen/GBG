package tools;

import tools.Types;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.TreeMap;

public class Utils
{
	/**
	 * 
	 * @param elements an array
	 * @param rnd  a random number generator
	 * @return a random element from array {@code elements}
	 */
    public static Object choice(Object[] elements, Random rnd)
    {
        return elements[rnd.nextInt(elements.length)];
    }

//    public static Vector2d choice(ArrayList<Vector2d> elements, Random rnd)
//    {
//        return elements.get(rnd.nextInt(elements.size()));
//    }

    /**
     * 
     * @param str
     * @return formatted string: all non-newline whitespaces compressed to single space 
     * and whitespaces from beginning and end of lines are removed
     */
    public static String formatString(String str)
    {
        // 1st replaceAll: compresses all non-newline whitespaces to single space
        // 2nd replaceAll: removes spaces from beginning or end of lines
        return str.replaceAll("[\\s&&[^\\n]]+", " ").replaceAll("(?m)^\\s|\\s$", "");
    }


    /**
     * @return {@code (a_value - a_min)/(a_max - a_min)}, usually a value in [0,1]
     */
    public static double normalise(double a_value, double a_min, double a_max)
    {
        return (a_value - a_min)/(a_max - a_min);
    }

    public static int argmax (double[] values)
    {
        int maxIndex = -1;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < values.length; i++) {
            double elem = values[i];
            if (elem > max) {
                max = elem;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

	/**
	 * checks if a folder exists and creates a new one if it doesn't
	 *
	 * @param filePath the folder Path
	 * @return true if folder already exists
	 */
	public static boolean checkAndCreateFolder(String filePath) {
		File file = new File(filePath);
		boolean exists = file.exists();
		if(!file.exists()) {
			file.mkdirs();
		}
		return exists;
	}
	
    /**
     * calculate the average from an ArrayList of doubles
     * @param timesArray ArrayList with double values
     * @return the median
     */
    public static double calculateAverage(ArrayList<Double> timesArray) {
        double avg = 0.0;
        
        for (Double d : timesArray) avg += d.doubleValue();
        
        avg /= timesArray.size();

        return avg;
    }

    /**
     * calculate the median from an ArrayList of doubles
     * @param timesArray a list of double values
     * @return the median
     */
    public static double calculateMedian(ArrayList<Double> timesArray) {
        double median;

    	Collections.sort(timesArray);
    	int L = timesArray.size();
    	
        if (L % 2 == 0)
            median = (timesArray.get(L/2) + timesArray.get(L/2 - 1))/2;
        else
            median = timesArray.get((L-1)/2);

        return median;
    }

    // --- the follwoing two methods are commented out, because we have now
    // --- the simpler method calculateMedian(ArrayList<Double> timesArray)
    // ---
//    /**
//     * calculate the median from an ArrayList of doubles
//     * @param timesArray a list of double values
//     * @return the median
//     */
//    @Deprecated
//    public double calculateMedianOLD(ArrayList<Double> timesArray) {
//        double[] tmpD = new double[timesArray.size()];
//
//        for (int j=0; j<timesArray.size(); j++)
//            tmpD[j] = timesArray.get(j);
//
//        return calculateMedian(tmpD);
//    }
//
//    /**
//     * calculate the median from an array of doubles
//     * @param medianTimes array with double values
//     * @return the median
//     */
//    @Deprecated
//    public double calculateMedian(double[] medianTimes) {
//        double median;
//
//        Arrays.sort(medianTimes);
//
//        if (medianTimes.length % 2 == 0)
//            median = (medianTimes[medianTimes.length/2] + medianTimes[medianTimes.length/2 - 1])/2;
//        else
//            median = medianTimes[(medianTimes.length-1)/2];
//
//        return median;
//    }

}
