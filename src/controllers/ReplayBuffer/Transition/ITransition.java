package controllers.ReplayBuffer.Transition;

import controllers.TD.ntuple4.NextState4;
import games.StateObsWithBoardVector;
import games.StateObservation;
import tools.ScoreTuple;
import tools.Types;

public interface ITransition {
    public StateObservation getState();
    public Types.ACTIONS getActions();
    public NextState4 getNextState();
    public StateObservation[] getSLast();
    public ScoreTuple getRLast();
    public ScoreTuple getReward();
    public StateObsWithBoardVector getCurSOWB();
    public void setCurSOWB(StateObsWithBoardVector curSOWB);
    public int getPlayer();
    public void setPlayer(int p);
    public double getVLast();
    public void setVLast(double v);
    public double getTarget();
    public void setTarget(double t);
    public void setReward2(double r);
    public double getReward2();
    public void setState(StateObservation state);
    public void setAction(Types.ACTIONS action);
    public void setNextState(NextState4 nextState);
    public void setSLast(StateObservation[] sLast);
    public void setLastReward(ScoreTuple lastReward);
    public void setReward(ScoreTuple reward);

}
