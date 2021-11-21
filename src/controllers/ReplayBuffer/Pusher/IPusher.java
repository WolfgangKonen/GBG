package controllers.ReplayBuffer.Pusher;

import controllers.ReplayBuffer.Transition.ITransition;

public interface IPusher {

    public boolean pushTransition(ITransition t);
    public String toString();
}
