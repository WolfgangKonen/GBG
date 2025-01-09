package controllers.SB3;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class FirstObservation {
    private final List<Double> observationVector;
    private final Map<String, String> info;

    public FirstObservation(List<Double> observationVector, Map<String, String> info) {
        this.observationVector = observationVector;
        this.info = info;
    }

    public List<Double> getObservationVector() {
        return observationVector;
    }

    public Map<String, String> getInfo() {
        return info;
    }

    public JSONObject toJson() {
        return new JSONObject(this);
    }

}
