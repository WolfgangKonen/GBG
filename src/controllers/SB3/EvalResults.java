package controllers.SB3;

import org.json.JSONObject;

public class EvalResults {
    private final int wins;
    private final int ties;
    private final int losses;
    private final double averageReward;

    public int getWins() {
        return wins;
    }

    public int getTies() {
        return ties;
    }

    public int getLosses() {
        return losses;
    }

    public double getAverageReward() {
        return averageReward;
    }

    public EvalResults(int wins, int ties, int losses, double averageReward) {
        this.wins = wins;
        this.ties = ties;
        this.losses = losses;
        this.averageReward = averageReward;
    }

    public JSONObject toJson() {
        return new JSONObject(this);
    }
}
