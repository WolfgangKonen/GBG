package controllers.ReplayBuffer.Selector;

import controllers.ReplayBuffer.Transition.ITransition;

public interface ISelector {
    public ITransition[] selectBatch();
    public String toString();
}
