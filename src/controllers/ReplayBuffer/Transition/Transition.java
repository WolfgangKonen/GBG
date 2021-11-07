package controllers.ReplayBuffer.Transition;

import controllers.TD.ntuple4.NextState4;
import game.functions.ints.state.Next;
import game.functions.ints.state.Score;
import games.StateObsWithBoardVector;
import games.StateObservation;
import tools.ScoreTuple;
import tools.Types;

public class Transition implements ITransition {

    private int bufferIndex, player;
    private StateObservation state;
    private Types.ACTIONS action;
    private NextState4 nextState;
    private StateObservation[] sLast;
    private ScoreTuple lastReward;
    private StateObsWithBoardVector curSOWB;
    private double vLast, target, reward2;
    private ScoreTuple reward;
    public Transition(int i){
        bufferIndex = i;
    }



    public int getBufferIndex() {
        return bufferIndex;
    }

    public void setBufferIndex(int bufferIndex) {
        this.bufferIndex = bufferIndex;
    }

    public void setState(StateObservation state) {
        this.state = state;
    }

    public void setAction(Types.ACTIONS action) {
        this.action = action;
    }

    public void setNextState(NextState4 nextState) {
        this.nextState = nextState;
    }

    public void setSLast(StateObservation[] sLast) {
        this.sLast = sLast;
    }

    public void setLastReward(ScoreTuple lastReward) {
        this.lastReward = lastReward;
    }

    @Override
    public void setReward(ScoreTuple reward) {
        this.reward = reward;
    }


    @Override
    public StateObservation getState() {
        return state;
    }

    @Override
    public Types.ACTIONS getActions() {
        return action;
    }

    @Override
    public NextState4 getNextState() {
        return nextState;
    }

    @Override
    public StateObservation[] getSLast() {
        return sLast;
    }

    @Override
    public ScoreTuple getRLast() {
        return lastReward;
    }

    @Override
    public ScoreTuple getReward() {
        return reward;
    }

    @Override
    public StateObsWithBoardVector getCurSOWB() {
        return curSOWB;
    }

    @Override
    public void setCurSOWB(StateObsWithBoardVector curSOWB) {
        this.curSOWB = curSOWB;
    }

    @Override
    public int getPlayer() {
        return player;
    }

    @Override
    public void setPlayer(int p) {
        player =p;
    }

    @Override
    public double getVLast() {
        return vLast;
    }

    @Override
    public void setVLast(double v) {
        vLast = v;
    }

    @Override
    public double getTarget() {
        return target;
    }

    @Override
    public void setTarget(double t) {
        target = t;
    }

    @Override
    public void setReward2(double r) {
        reward2 = r;
    }

    @Override
    public double getReward2() {
        return reward2;
    }
}
