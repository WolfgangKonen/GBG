package controllers.SB3;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Transition implements Serializable {
    private final List<Double> observationVector;
    private final double reward;
    private final boolean terminated;
    private final boolean truncated;
    private final Map<String, String> info;

    public List<Double> getObservationVector() {
        return observationVector;
    }

    public double getReward() {
        return reward;
    }

    public boolean getTerminated() {
        return terminated;
    }

    public boolean getTruncated() {
        return truncated;
    }

    public Map<String, String> getInfo() {
        return info;
    }

    public Transition(List<Double> observationVector, double reward, boolean terminated, boolean truncated, Map<String, String> info) {
        this.observationVector = observationVector;
        this.reward = reward;
        this.terminated = terminated;
        this.truncated = truncated;
        this.info = info;
    }

    public JSONObject toJson() {
        return new JSONObject(this);
    }
}
