package tools;

import tools.Types;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

/**
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Utils
{
    public static Object choice(Object[] elements, Random rnd)
    {
        return elements[rnd.nextInt(elements.length)];
    }

//    public static Vector2d choice(ArrayList<Vector2d> elements, Random rnd)
//    {
//        return elements.get(rnd.nextInt(elements.size()));
//    }

    public static String formatString(String str)
    {
        // 1st replaceAll: compresses all non-newline whitespaces to single space
        // 2nd replaceAll: removes spaces from beginning or end of lines
        return str.replaceAll("[\\s&&[^\\n]]+", " ").replaceAll("(?m)^\\s|\\s$", "");
    }


    //Normalizes a value between its MIN and MAX.
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
	
}
