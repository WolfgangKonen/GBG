package controllers.SB3;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * DTO used to transfer all needed data by a gymnasium's gym.Env interface after the player took an action (step(action: int) methode).
 */
public class Transition implements Serializable {
    private final int[] observationVector;
    private final double reward;
    private final boolean terminated;
    private final boolean truncated;
    private final Map<String, String> info;

    public int[] getObservationVector() {
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

    public Transition(int[] observationVector, double reward, boolean terminated, boolean truncated, Map<String, String> info) {
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
