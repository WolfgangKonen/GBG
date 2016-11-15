package tools;

import games.XArenaFuncs;

/** 
 *  A simple class for repeated measurements.
 *  Instantiate it, add measurements to it with {@link #add(double)}, then you can retrieve 
 *  the mean with {@link #getMean()}, the standard deviation with {@link #getStd()}, the 
 *  measurement count with {@link #getCount()} and the last value added with {@link #getVal()}.
 *   	
 *	@see XArenaFuncs
 */
public class Measure {
	private double val=Double.NaN;
	private double sum=0.0;
	private double sum2=0.0;
	private long count=0;
	
	public Measure() {}
	public void add(double value) {
		val = value;
		sum += value;
		sum2 += value*value;
		count++;
	}
	public long getCount() {
		return count;
	}
	public double getVal() {
		return val;
	}
	public double getMean() {
		return sum/count;
	}
	public double getStd() {
		double std2 = sum2/count - getMean()*getMean();
		assert (std2>=0) : "Assertion failed: std2 = " + std2 + " is below 0";
		return Math.sqrt(std2);
	}
}

