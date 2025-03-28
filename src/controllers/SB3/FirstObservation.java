package controllers.SB3;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * DTO used to transfer all needed data by a gymnasium's gym.Env interface after a new game round/ episode had started (reset() methode).
 */
public class FirstObservation {
    private final int[] observationVector;
    private final Map<String, String> info;

    public FirstObservation(int[] observationVector, Map<String, String> info) {
        this.observationVector = observationVector;
        this.info = info;
    }

    public int[] getObservationVector() {
        return observationVector;
    }

    public Map<String, String> getInfo() {
        return info;
    }

    public JSONObject toJson() {
        return new JSONObject(this);
    }

}
