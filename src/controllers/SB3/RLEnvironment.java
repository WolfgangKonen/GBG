package controllers.SB3;

import games.StateObservation;

import java.util.List;
import java.util.Map;

public interface RLEnvironment {
    public void initiateNewGame(StateObservation stateObservation);
    public void trainingFinished();
    public List<Double> getObservation();
    public double getReward();
    public boolean terminated();
    public boolean truncated();
    public Map<String, String> getInfo();
    public FirstObservation reset();
    public Transition step(int action);
}
