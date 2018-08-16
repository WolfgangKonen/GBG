package TournamentSystem;

import controllers.PlayAgent;
import games.StateObservation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

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
     */
    private static final long serialVersionUID = 1L;

    private ArrayList<Long> measuredTimesInNS = new ArrayList<>(); // all times of every draw in every round/game
    private ArrayList<Long> roundTimesInNS = new ArrayList<>(); // time all draws per round take
    private ArrayList<Long> tmpRoundTimesInNS = new ArrayList<>(); // every draws time just of the current round

    private double nanoToMS(double ns) {
        return ns/(1*Math.pow(10,6));
    }

    public void addNewTimeNS(long value) {
        measuredTimesInNS.add(value);
        tmpRoundTimesInNS.add(value);
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
        if (roundTime>0)
            roundTimesInNS.add(roundTime);

        tmpRoundTimesInNS = new ArrayList<>();
    }

    public double getAverageRoundTimeMS() {
        long avRound = 0;
        if (roundTimesInNS.size()==0)
            return -1;
        for (long val : roundTimesInNS)
            avRound += val;
        avRound /= roundTimesInNS.size();
        return nanoToMS(avRound);
    }

    public double getMedianRoundTimeMS() {
        long median;
        long[] tmp = new long[roundTimesInNS.size()];
        if (tmp.length==0)
            return -1;

        for (int i=0; i<roundTimesInNS.size(); i++)
            tmp[i] = roundTimesInNS.get(i);

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
}
