package TournamentSystem;

import controllers.PlayAgent;
import games.StateObservation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * This class stores every rounds time measurements and gets instantiated for every game.
 * Time is measured in Nanoseconds via {@link System#nanoTime()} in {@link games.XArenaFuncs#competeTS(PlayAgent, PlayAgent, StateObservation, int, TSTimeStorage[], int)}.
 * Times are saved in dynamic ArrayLists. There also also methods to convert the Nanosecond values to Millisecond for improved readability.
 * <p>
 * This class is called in {@link TSAgentManager} and provides the data for the tournament time statistics.
 *
 * @author Felix Barsnick, University of Applied Sciences Cologne, 2018
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

    private ArrayList<Long> measuredTimesInNS = new ArrayList<>(); // all times of every draw in every round/game
    private ArrayList<Long> episodeTimesInNS = new ArrayList<>(); // time all moves per episode take
    private ArrayList<Long> tmpRoundTimesInNS = new ArrayList<>(); // every move time just of the current round
    private ArrayList<Integer> countMovesPerEpisode = new ArrayList<>(); // amount of moves per episode
    private int tmpEpisodeMoveCounter = 0; // counter for moves per episode, must be reset after episode finished

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
    public long getTotalTimeMS() {
        long total = 0;
        for (long l : measuredTimesInNS)
            total += nanoToMS(l);
        return total;
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

    public double getAverageRoundTimeMS() {
        long avRound = 0;
        if (episodeTimesInNS.size()==0)
            return -1;
        for (long val : episodeTimesInNS)
            avRound += val;
        avRound /= episodeTimesInNS.size();
        return nanoToMS(avRound);
    }

    public double getMedianRoundTimeMS() {
        long median;
        long[] tmp = new long[episodeTimesInNS.size()];
        if (tmp.length==0)
            return -1;

        for (int i = 0; i< episodeTimesInNS.size(); i++)
            tmp[i] = episodeTimesInNS.get(i);

        Arrays.sort(tmp);

        if (tmp.length % 2 == 0)
            median = (tmp[tmp.length/2] + tmp[tmp.length/2 - 1])/2;
        else
            median = tmp[tmp.length/2];

        return nanoToMS(median);
    }

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
        if (getAverageTimeForGameNS()==-1)
            return -1;
        return nanoToMS(getAverageTimeForGameNS());
    }

    private long[] getSortedArray() {
        long[] tmp = new long[measuredTimesInNS.size()];
        for (int i=0; i<measuredTimesInNS.size(); i++)
            tmp[i] = measuredTimesInNS.get(i);
        Arrays.sort(tmp);
        return tmp;
    }

    public double getMedianTimeForGameNS() {
        double median;
        long[] tmp = getSortedArray();
        if (tmp.length==0)
            return -1;

        if (tmp.length % 2 == 0)
            median = (tmp[tmp.length/2] + tmp[tmp.length/2 - 1])/2;
        else
            median = tmp[tmp.length/2];

        return median;
    }

    public double getMedianTimeForGameMS() {
        if (getMedianTimeForGameNS()==-1)
            return -1;
        return nanoToMS(getMedianTimeForGameNS());
    }

    public double getMaxTimeForGameNS() {
        double max = -1;

        long[] tmp = getSortedArray();
        if (tmp.length>0)
            max = tmp[tmp.length-1];

        return max;
    }

    public double getMaxTimeForGameMS() {
        if (getMaxTimeForGameNS()==-1)
            return -1;
        return nanoToMS(getMaxTimeForGameNS());
    }

    public double getMinTimeForGameNS() {
        double min = -1;

        long[] tmp = getSortedArray();
        if (tmp.length>0)
            min = tmp[0];

        return min;
    }

    public double getMinTimeForGameMS() {
        if (getMinTimeForGameNS()==-1)
            return -1;
        return nanoToMS(getMinTimeForGameNS());
    }

    public int getAverageCountOfMovesPerEpisode() {
        int avgMoves = 0;
        if (countMovesPerEpisode.size()==0)
            return -1;
        for (int val : countMovesPerEpisode)
            avgMoves += val;
        avgMoves /= countMovesPerEpisode.size();
        return avgMoves;
    }

    public int getMedianCountOfMovesPerEpisode() {
        if (countMovesPerEpisode == null)
            return -1;

        int  median;
        int[] tmp = new int[countMovesPerEpisode.size()];
        if (tmp.length==0)
            return -1;

        for (int i = 0; i< countMovesPerEpisode.size(); i++)
            tmp[i] = countMovesPerEpisode.get(i);

        Arrays.sort(tmp);

        if (tmp.length % 2 == 0)
            median = (tmp[tmp.length/2] + tmp[tmp.length/2 - 1])/2;
        else
            median = tmp[tmp.length/2];

        return median;
    }

    public int getMaxCountOfMovesPerEpisode() {
        return Collections.max(countMovesPerEpisode);
    }

    public int getMinCountOfMovesPerEpisode() {
        return Collections.min(countMovesPerEpisode);
    }
}
