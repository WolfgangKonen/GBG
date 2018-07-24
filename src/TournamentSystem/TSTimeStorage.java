package TournamentSystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class TSTimeStorage implements Serializable {
    private ArrayList<Long> measuredTimesInNS = new ArrayList<>();
    private ArrayList<Long> roundTimesInNS = new ArrayList<>();
    private ArrayList<Long> tmpRoundTimesInNS = new ArrayList<>();

    public double nanoToMS(double ns) {
        return ns/(1*Math.pow(10,6));
    }

    public void addNewTimeNS(long value) {
        measuredTimesInNS.add(value);
        tmpRoundTimesInNS.add(value);
    }

    public void roundFinished() {
        long roundTime = 0;
        for (long time : tmpRoundTimesInNS){
            roundTime += time;
        }
        roundTimesInNS.add(roundTime);

        tmpRoundTimesInNS = new ArrayList<>();
    }

    public double getAverageRoundTimeMS() {
        long avRound = 0;
        for (long val : roundTimesInNS)
            avRound += val;
        avRound /= roundTimesInNS.size();
        return nanoToMS(avRound);
    }

    public double getMedianRoundTimeMS() {
        long median;
        long[] tmp = new long[roundTimesInNS.size()];

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

        for (long l : measuredTimesInNS)
            out += l;
        out /= measuredTimesInNS.size();

        return out;
    }

    public double getAverageTimeForGameMS() {
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

        if (tmp.length % 2 == 0)
            median = (tmp[tmp.length/2] + tmp[tmp.length/2 - 1])/2;
        else
            median = tmp[tmp.length/2];

        return median;
    }

    public double getMedianTimeForGameMS() {
        return nanoToMS(getMedianTimeForGameNS());
    }

    public double getMaxTimeForGameNS() {
        double max;

        long[] tmp = getSortedArray();
        max = tmp[tmp.length-1];

        return max;
    }

    public double getMaxTimeForGameMS() {
        return nanoToMS(getMaxTimeForGameNS());
    }

    public double getMinTimeForGameNS() {
        double min;

        long[] tmp = getSortedArray();
        min = tmp[0];

        return min;
    }

    public double getMinTimeForGameMS() {
        return nanoToMS(getMinTimeForGameNS());
    }
}
