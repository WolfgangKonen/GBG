package controllers.ReplayBuffer.Selector;

import controllers.ReplayBuffer.Buffer.BaseBuffer;
import controllers.ReplayBuffer.Transition.ITransition;

/**
 * Selector, which samples random elements from the replaybuffer
 */
public class RandomSelector extends BaseSelector{
    public RandomSelector(BaseBuffer buffer) {
        super(buffer);
    }

    @Override
    public ITransition selectBatch() {
        return getBuffer()[random.nextInt(getMaxBufferPosition())];
    }

    @Override
    public ITransition[] selectBatch(int n) {

        ITransition[] transitions = new ITransition[n];
        for(int i = 0; i < n; i++){
            transitions[i] = selectBatch();
        }
        return transitions;
    }


}
