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

   public void setPlayer(int player);
   public int getPlayer();

    public void setNextState(NextState4 ns);
    public NextState4 getNextState4();

    public void setSLast(StateObservation[] sLast);
    public StateObservation[] getSLast();

    public void setRLast(ScoreTuple rLast);
    public ScoreTuple getRLast();
    public double getPlayerRLast();

    public void setR(ScoreTuple R);
    public ScoreTuple getR();

}
