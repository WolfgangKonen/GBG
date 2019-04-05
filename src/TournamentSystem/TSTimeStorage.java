package TournamentSystem;

import controllers.PlayAgent;
import games.StateObservation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * This class stores time measurements for every round in a tournament and gets instantiated every game.
 * Time is measured in nanoseconds via {@link System#nanoTime()}, see 
 * {@link games.XArenaFuncs#compete(PlayAgent, PlayAgent, StateObservation, int, TSTimeStorage[], int) XArenaFuncs.compete()}.
 * <p>
 * The times are saved in dynamic ArrayLists. There are also methods to convert nanoseconds to 
 * milliseconds or seconds for improved readability.
 * <p>
 * This class is called in {@link TSAgentManager} and provides the data for the tournament time statistics.
 *
 * @author Felix Barsnick, Cologne University of Applied Sciences, 2018
 */
public class TSTimeStorage implements Serializable {
    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .tsr.zip will become unreadable or you have
     * to provide a special version transformation)
     *
     * History:
     * 1L - initial
     * 2L - added countMovesPerEpisode and tmpEpisodeMoveCounter and the accompanying methods
     */
    private static final long serialVersionUID = 2L;

    private ArrayList<Long> measuredTimesInNS = new ArrayList<>(); // all times of every move in every round/episode
    private ArrayList<Long> episodeTimesInNS = new ArrayList<>(); // time that all moves per episode take
    private ArrayList<Long> tmpRoundTimesInNS = new ArrayList<>(); // every move time just of the current round
    private ArrayList<Integer> countMovesPerEpisode = new ArrayList<>(); // number of moves per episode
    private int tmpEpisodeMoveCounter = 0; // counter for moves per episode, must be reset when episode finishes

    /**
     * convert nanosecond vale to millisecond
     * @param ns nanosecond value
     * @return millisecond value
     */
    private double nanoToMS(double ns) {
        return ns/(1*Math.pow(10,6));
    }

    /**
     * convert nanosecond vale to second
     * @param ns nanosecond value
     * @return second value
     */
    private double nanoToS(double ns) {
        return ns/(1*Math.pow(10,9));
    }

    /**
     * add new time measurement of a move
     * @param value time measurement in nanosecond resolution
     */
    public void addNewTimeNS(long value) {
        measuredTimesInNS.add(value);
        tmpRoundTimesInNS.add(value);
        tmpEpisodeMoveCounter++;
    }

    /**
     * get the total time of all episodes moves from this agent in this match
     * @return amount of time in nanoseconds
     */
    public long getTotalTimeNS() {
        long total = 0;
        for (long l : measuredTimesInNS)
            total += l;
        return total;
    }
    /**
     * get the total time of all episodes moves from this agent in this match
     * @return amount of time in milliseconds
     */
    public double getTotalTimeMS() {
//        long total = 0;
//        for (long l : measuredTimesInNS)
//            total += nanoToMS(l);
//        return total;
        return nanoToMS(getTotalTimeNS());
    }

    public double getTotalTimeS() {
        return nanoToS(getTotalTimeNS());
    }

    /**
     * important to call this method when a round of a game is finished to have accurate round times.
     */
    public void roundFinished() {
        long roundTime = 0;
        for (long time : tmpRoundTimesInNS){
            roundTime += time;
        }
        // just save time if the agent really made a move
        if (roundTime>0) {
            episodeTimesInNS.add(roundTime);
            countMovesPerEpisode.add(tmpEpisodeMoveCounter);
        }

        tmpRoundTimesInNS = new ArrayList<>();
        tmpEpisodeMoveCounter = 0;
    }

    /**
     * @return average time per episode in ns (average over all episodes in this match)
     */
    public double getAverageRoundTimeMS() {
        double avRound = 0;
        if (episodeTimesInNS.size()==0)
            return -1;
        for (long val : episodeTimesInNS)
            avRound += val;
        avRound /= episodeTimesInNS.size();
        return nanoToMS(avRound);
    }

    /**
     * @return median time per episode in ns (median over all episodes in this match)
     */
    public double getMedianRoundTimeMS() {
        double median;
        double[] tmp = new double[episodeTimesInNS.size()];
        if (tmp.length==0)
            return -1;

        for (int i = 0; i< episodeTimesInNS.size(); i++)
            tmp[i] = episodeTimesInNS.get(i);

        Arrays.sort(tmp);

        if (tmp.length % 2 == 0)
            median = (tmp[tmp.length/2] + tmp[tmp.length/2 - 1])/2;
        else
            median = tmp[(tmp.length-1)/2];

        return nanoToMS(median);
    }

    /**
     * @return average time per move in ns (average over all moves in all episodes)
     */
    public double getAverageTimeForGameNS() {
        double out = 0;
        if (measuredTimesInNS.size()==0)
            return -1;

        for (long l : measuredTimesInNS)
            out += l;
        out /= measuredTimesInNS.size();

        return out;
    }

    public double getAverageTimeForGameMS() {
    	double z = getAverageTimeForGameNS(); 
        return (z==-1) ? -1 : nanoToMS(z);
    }

    private double[] getSortedMeasuredTimesInNS() {
    	double[] tmp = new double[measuredTimesInNS.size()];
        for (int i=0; i<measuredTimesInNS.size(); i++)
            tmp[i] = measuredTimesInNS.get(i);
        Arrays.sort(tmp);
        return tmp;
    }

    /**
     * @return median time per move in ns (median over all moves in all episodes)
     */
    public double getMedianTimeForGameNS() {
        double median;
        double[] tmp = getSortedMeasuredTimesInNS();
        if (tmp.length==0)
            return -1;

        if (tmp.length % 2 == 0)
            median = (tmp[tmp.length/2] + tmp[tmp.length/2 - 1])/2;
        else
            median = tmp[(tmp.length-1)/2];

        return median;
    }

    public double getMedianTimeForGameMS() {
    	double z = getMedianTimeForGameNS(); 
        return (z==-1) ? -1 : nanoToMS(z);
//        if (getMedianTimeForGameNS()==-1)
//            return -1;
//        return nanoToMS(getMedianTimeForGameNS());
    }

    public double getMaxTimeForGameNS() {
        double max = -1;

        double[] tmp = getSortedMeasuredTimesInNS();
        if (tmp.length>0)
            max = tmp[tmp.length-1];

        return max;
    }

    public double getMaxTimeForGameMS() {
    	double z = getMaxTimeForGameNS(); 
        return (z==-1) ? -1 : nanoToMS(z);
//        if (getMaxTimeForGameNS()==-1)
//            return -1;
//        return nanoToMS(getMaxTimeForGameNS());
    }

    public double getMinTimeForGameNS() {
        double min = -1;

        double[] tmp = getSortedMeasuredTimesInNS();
        if (tmp.length>0)
            min = tmp[0];

        return min;
    }

    public double getMinTimeForGameMS() {
    	double z = getMinTimeForGameNS(); 
        return (z==-1) ? -1 : nanoToMS(z);
//        if (getMinTimeForGameNS()==-1)
//            return -1;
//        return nanoToMS(getMinTimeForGameNS());
    }

    public double getAverageCountOfMovesPerEpisode() {
    	double avgMoves = 0;
        if (countMovesPerEpisode.size()==0)
            return -1;
        for (int val : countMovesPerEpisode)
            avgMoves += val;
        avgMoves /= countMovesPerEpisode.size();
        return avgMoves;
    }

    public double getMedianCountOfMovesPerEpisode() {
        if (countMovesPerEpisode == null)
            return -1;

        double  median;
        double[] tmp = new double[countMovesPerEpisode.size()];
        if (tmp.length==0)
            return -1;

        for (int i = 0; i< countMovesPerEpisode.size(); i++)
            tmp[i] = countMovesPerEpisode.get(i);

        Arrays.sort(tmp);

        if (tmp.length % 2 == 0)
            median = (tmp[tmp.length/2] + tmp[tmp.length/2 - 1])/2;
        else
            median = tmp[(tmp.length-1)/2];

        return median;
    }

    public int getMaxCountOfMovesPerEpisode() {
        return Collections.max(countMovesPerEpisode);
    }

    public int getMinCountOfMovesPerEpisode() {
        return Collections.min(countMovesPerEpisode);
    }
}
