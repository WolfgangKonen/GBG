package controllers.ReplayBuffer.Selector;

import controllers.ReplayBuffer.Buffer.BaseBuffer;
import controllers.ReplayBuffer.ConfigReplayBuffer;
import controllers.ReplayBuffer.Transition.ITransition;

/**
 * Selector, which samples random elements from the replaybuffer
 */
public class RandomSelector extends BaseSelector{
    public RandomSelector(BaseBuffer buffer) {
        super(buffer);
    }


    @Override
    public ITransition[] selectBatch() {

        ITransition[] transitions = new ITransition[getBatchSize()];
        if(ConfigReplayBuffer.DBG) System.out.println("Maximal buffer size: " + getMaxBufferPosition());
        for(int i = 0; i < getBatchSize(); i++){
            transitions[i] =  getBuffer()[random.nextInt(getMaxBufferPosition())];;
        }
        return transitions;
    }

    public String toString(){
        return "Random Selector";
    }
}
