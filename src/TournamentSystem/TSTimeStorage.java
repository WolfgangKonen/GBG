package TournamentSystem;

import java.util.ArrayList;

public class TSTimeStorage {
    public ArrayList<Long> measuredTimesInNS = new ArrayList<>();

    public double nanoToMS(double ns) {
        return ns/(1*Math.pow(10,6));
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
}
