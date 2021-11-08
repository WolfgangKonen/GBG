package controllers.ReplayBuffer.Buffer;

import controllers.ReplayBuffer.ConfigReplayBuffer;
import controllers.ReplayBuffer.Selector.ISelector;
import controllers.ReplayBuffer.Selector.RandomSelector;
import controllers.ReplayBuffer.Transition.ITransition;
import controllers.ReplayBuffer.Transition.Transition;
import controllers.TD.ntuple4.NextState4;
import games.StateObsWithBoardVector;
import games.StateObservation;
import params.ParRB;
import params.RBParams;
import tools.ScoreTuple;
import tools.Types;

public class BaseBuffer{

    private int capacity;
    private ITransition[] buffer;
    private int indexPointer;
    private ISelector selector;
    private int bufferMaxPointer;
    private ITransition lastTransition;
    private RBParams m_params;

    public BaseBuffer(){
        ConfigReplayBuffer.COLLECTING = true;
        indexPointer = 0;
        bufferMaxPointer = 0;
        capacity = ConfigReplayBuffer.CAPACITY;
        buffer = new ITransition[ConfigReplayBuffer.CAPACITY];
        selector = setSelector(ConfigReplayBuffer.SELECTOR);
        lastTransition = null;
        assert selector instanceof RandomSelector;
    }
    public BaseBuffer(ParRB params) {
        ConfigReplayBuffer.COLLECTING = true;
        ConfigReplayBuffer.CAPACITY = params.getCapacity();
        ConfigReplayBuffer.USE_REPLAYBUFFER = params.getUseRB();
        indexPointer = 0;
        bufferMaxPointer = 0;
        capacity = params.getCapacity();
        buffer = new ITransition[ConfigReplayBuffer.CAPACITY];
        selector = setSelector(ConfigReplayBuffer.SELECTOR);
        lastTransition = null;
        assert selector instanceof RandomSelector;
    }


    private void incrementIndexPointer(){
        indexPointer = (indexPointer + 1) % capacity;
    }

    /**
     * Incrementing the pointer, which indicates the maximal position of the
     *  buffer from which can be selected
     */
    private void incrementBufferMaxPointer(){
        if(bufferMaxPointer == capacity) return;
        bufferMaxPointer = indexPointer +1;
    }

    public int getMaxBufferIndex(){
        return bufferMaxPointer;
    }

    public void setUseBuffer(boolean b){
        ConfigReplayBuffer.USE_REPLAYBUFFER = b;
    }

    public void createNewTransition(){
        lastTransition = new Transition(indexPointer);
    }

    public void saveTransition(){
        if(lastTransition != null
            && lastTransition.getNextState() != null&& lastTransition.getCurSOWB() != null && lastTransition.getRLast() != null
        ){
            addTransition(lastTransition);

        }
        else lastTransition = null;
    }

    public ITransition getLastTransition(){
        return lastTransition;
    }

    public void setCapacity(int n){
        ConfigReplayBuffer.CAPACITY = n;
    }

    public void setCollecting(boolean b){
        ConfigReplayBuffer.COLLECTING = b;
    }


    public ITransition getLastTransitionIndex(){
        return buffer[lastIndexPointer()];
    }

    public void addTransition(ITransition t){
        buffer[indexPointer] = t;
        incrementIndexPointer();
        incrementBufferMaxPointer();
    }

    private int lastIndexPointer(){
        if(indexPointer == 0) return capacity-1;
        return indexPointer;
    }

    public ITransition[] getBatches(){
        return new ITransition[]{selector.selectBatch()};
    }

    public ITransition[] getBatches(int n){
        return selector.selectBatch(n);
    }

    public ITransition[] getBuffer(){
        return buffer;
    }

    public int getCapacity(){
        return capacity;
    }

    public void resetBuffer(){
        buffer = new ITransition[ConfigReplayBuffer.CAPACITY];
        indexPointer = 0;
        bufferMaxPointer = 0;
    }

    private ISelector setSelector(int i){
        switch(i){
            case 0: return new RandomSelector(this);
            default: throw new RuntimeException("No Selector has been selected");
        }
    }




}
