package controllers.ReplayBuffer.Transition;

import controllers.TD.ntuple4.NextState4;
import game.functions.ints.state.Next;
import game.functions.ints.state.Score;
import game.functions.ints.state.State;
import games.StateObsWithBoardVector;
import games.StateObservation;
import games.TicTacToe.StateObserverTTT;
import tools.ScoreTuple;
import tools.Types;

public class Transition implements ITransition {

    private StateObsWithBoardVector sowb;
    private NextState4 next_state;
    private StateObservation[] sLast;
    private int player;
    private ScoreTuple rLast,R;
    private double vLast, target,r_next;

    public Transition(){};

    public Transition(StateObsWithBoardVector sowb,int player, double vLast, double target,double r_next, NextState4 ns, StateObservation[] sLast, ScoreTuple rLast){
        this.sowb = sowb;
        this.next_state = ns;
        this.sLast = sLast;
        this.player = player;
        this.rLast = rLast;
        this.target = target;
        this.vLast = vLast;
        this.r_next = r_next;
    }

    @Override
    public void setSowb(StateObsWithBoardVector sowb) {
        this.sowb = sowb;
    }

    @Override
    public StateObsWithBoardVector getSowb() {
        return sowb;
    }

    @Override
    public void setPlayer(int player) {
        this.player = player;
    }

    @Override
    public int getPlayer() {
        return player;
    }

    @Override
    public void setVLast(double vlast) {
        this.vLast = vlast;
    }

    @Override
    public double getVLast() {
        return vLast;
    }

    @Override
    public void setTarget(double target) {
        this.target = target;
    }

    @Override
    public double getTarget() {
        return target;
    }

    @Override
    public void setRNext(double r_next) {
        this.r_next = r_next;
    }

    @Override
    public double getRNext() {
        return r_next;
    }


    @Override
    public void setNextState(NextState4 ns) {
        this.next_state = ns;
    }

    @Override
    public NextState4 getNextState4() {
        return next_state;
    }

    public StateObservation getNextState(){
        return next_state.getNextSO();
    }

    @Override
    public void setSLast(StateObservation[] sLast) {
        this.sLast = sLast;
    }

    @Override
    public StateObservation[] getSLast() {
        return sLast;
    }

    @Override
    public void setRLast(ScoreTuple rLast) {
        this.rLast = rLast;
    }

    @Override
    public double getPlayerRLast(){
        return rLast.scTup[player];
    }

    @Override
    public void setR(ScoreTuple R) {this.R = R;
    }

    @Override
    public ScoreTuple getR() {
        return R;
    }

    @Override
    public ScoreTuple getRLast() {
        return rLast;
    }
}
