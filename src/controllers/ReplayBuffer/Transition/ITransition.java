package controllers.ReplayBuffer.Transition;

import controllers.TD.ntuple2.NextState;
import controllers.TD.ntuple4.NextState4;
import game.functions.ints.state.Next;
import game.functions.ints.state.Score;
import game.functions.ints.state.State;
import games.StateObsWithBoardVector;
import games.StateObservation;
import tools.ScoreTuple;
import tools.Types;

public interface ITransition {
   public void setSowb(StateObsWithBoardVector sowb);
   public StateObsWithBoardVector getSowb();

   public void setPlayer(int player);
   public int getPlayer();

   public void setVLast(double vlast);
   public double getVLast();

   public void setTarget(double target);
   public double getTarget();

   public void setRNext(double r_next);
   public double getRNext();

    public void setNextState(NextState4 ns);
    public NextState4 getNextState4();
    public StateObservation getNextState();

    public void setSLast(StateObservation[] sLast);
    public StateObservation[] getSLast();

    public void setRLast(ScoreTuple rLast);
    public ScoreTuple getRLast();
    public double getPlayerRLast();

    public void setR(ScoreTuple R);
    public ScoreTuple getR();

}
