package tools;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import games.RubiksCube.CubeConfig;

/**
 *  {@link TStats} is a class to store a tuple of int's with diagnostic information
 *  about the last training episode: <ul>
 *  <li> <b>n</b> 			the episode counter (gameNum)
 *  <li> <b>p</b> 			the minimum episode length 
 *  <li> <b>moveNum</b>		the actual number of moves in this episode
 *  <li> <b>epiLength</b>	the maximum allowed episode length
 *  </ul>
 *  
 *  This class is mainly useful for game <b>RubiksCube</b>, but may not be completely useless
 *  for other (puzzle) games as well.
 */
public class TStats {
	public int n;
	public int p;
	public int moveNum;
	public int epiLength;
	
	public TStats(int n, int p, int moveNum, int epiLength) {
		this.n=n;
		this.p=p;
		this.moveNum=moveNum;
		this.epiLength=epiLength;
	}		

	public static void printTStatsList(ArrayList<TStats> csList) {
		DecimalFormat form = new DecimalFormat("000");
		for (TStats tint : csList) {
			System.out.println(form.format(tint.n) + ", " + form.format(tint.p) + ", " + tint.moveNum
					+ ", epiLength=" + tint.epiLength);
		}
	}

	public static void printLastTStats(ArrayList<TStats> csList) {
		DecimalFormat form = new DecimalFormat("000");
		TStats tint = csList.get(csList.size()-1);
	    System.out.println(form.format(tint.n) + ", " + form.format(tint.p) + ", "+ tint.moveNum 
	    		+ ", epiLength="+tint.epiLength);
	}

	/**
	 * Nested class for aggregating the results in a list of {@link TStats} objects: All objects 
	 * with a given {@code p} (minimum episode length) are aggregated to obtain: <ul>
	 *  <li> <b>size</b> 		the count
	 *  <li> <b>percSolved</b> 	the percentage of episodes solved in minimum episode length  
	 *  <li> <b>percLonger</b>	the percentage of longer episodes, but below max. episode length
	 *  <li> <b>epiLength</b>	the percentage of episodes with maximum episode length
	 *  </ul>
	 *  
	 *  This class is mainly useful for game <b>RubiksCube</b>, but may not be completely useless
	 *  for other (puzzle) games as well.
	 */
	public static class TAggreg {
		int size;
		int p;
		public double percSolved;
		double percLonger;
		double percNotSol;
		
		public TAggreg(ArrayList<TStats> tsList, int p) {
			Iterator<TStats> it = tsList.iterator();
			int nSolved=0;
			int nLonger=0;
			int nNot=0;
			int size=0;
		    while (it.hasNext()) {
			    TStats cs = it.next();
			    if (cs.p==p) {
			    	size++;
				    this.p = cs.p;
				    if (cs.moveNum<=cs.p) nSolved++;	// completed in p moves or less (Why 'or less'? - Although a 
				    				// scrambled cube may be created with p twist, it may happen, that it belongs
				    				// to distance set D[p-1] or lower and can be solved with p-1 twists or less.
				    if (cs.p<cs.moveNum && cs.moveNum<cs.epiLength) nLonger++;	// completed in p+1,...,epiLength-1 moves
				    if (cs.moveNum>=cs.epiLength) nNot++;	// did not complete after epiLength moves
			    }
	        } 
		    this.size = size;
			this.percSolved = ((double)nSolved)/size;
			this.percLonger = ((double)nLonger)/size;
			this.percNotSol = ((double)nNot)/size;
		}		

	} // nested class TAggreg
	
	public static void printTAggregList(ArrayList<TAggreg> taList) {
		DecimalFormat form = new DecimalFormat("000");
		DecimalFormat form2 = new DecimalFormat("0000");
		DecimalFormat fper = new DecimalFormat("000.0%"); 
		System.out.println("  p,  num: %solved, %longe, %unsolved");
		for (TAggreg tint : taList) {
			System.out.println(form.format(tint.p) + ", " + form2.format(tint.size) + ":  "
					+ fper.format(tint.percSolved) + ", "
					+ fper.format(tint.percLonger) + ", "
					+ fper.format(tint.percNotSol));
		}
	}
	
	/**
	 * @param taList a list of TAggreg objects
	 * @return the average 'solved' percentage of taList
	 */
	public static double avgResTAggregList(ArrayList<TAggreg> taList) {
		double res=0;
		for (TAggreg tagg : taList) {
			res += tagg.percSolved;
		}
		return res/taList.size();
	}

	/**
	 * @param taList a list of size {@link CubeConfig#pMax} with aggregated training results
	 * @param w a weight vector of length {@link CubeConfig#pMax}. Each entry in {@code taList} gets the 
	 * 			relative weight w[p]/sum(w[p])
	 * @param mode =0: percent solved within minimal twists, =1: percent solved below epiLength
	 * @return the weighted average of 'solved' percentages in {@code taList}
	 */
	public static double weightedAvgResTAggregList(ArrayList<TAggreg> taList, double[] w, int mode) {
		assert (w.length >= taList.size());
		double res=0;
		double wghtSum = 0.0;
		double val;
		int count=0;
		for (TAggreg tagg : taList) {
			val = (mode == 0) ? tagg.percSolved : (1 - tagg.percNotSol);
			wghtSum += w[count];
			res += val * w[count++];
		}
		return res/wghtSum;
	}

} // class TStats
