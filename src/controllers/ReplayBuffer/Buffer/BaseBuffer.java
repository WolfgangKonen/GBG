package controllers.ReplayBuffer.Buffer;

import controllers.ReplayBuffer.ConfigReplayBuffer;
import controllers.ReplayBuffer.Pusher.BasePusher;
import controllers.ReplayBuffer.Pusher.IPusher;
import controllers.ReplayBuffer.Selector.ISelector;
import controllers.ReplayBuffer.Selector.RandomSelector;
import controllers.ReplayBuffer.Transition.ITransition;
import controllers.ReplayBuffer.Transition.Transition;
import controllers.TD.ntuple4.NextState4;
import game.functions.ints.state.Score;
import games.StateObsWithBoardVector;
import games.StateObservation;
import org.apache.batik.dom.svg.AbstractSVGNormPathSegList;
import params.ParRB;
import params.RBParams;
import tools.ScoreTuple;
import tools.Types;

import javax.swing.plaf.nimbus.State;

public class BaseBuffer{


    private ITransition[] buffer;
    private ITransition lastTransition;
    private int indexPointer;
    private int bufferMaxPointer;  // Highest possible index -> Should be max capacity-1
    private boolean collecting;
    private int[] episodeLengths;
    private int capacity;
    private int batchSize;
    private int episodeLength;
    private ISelector selector;
    private IPusher pusher;

    public BaseBuffer(int capacity, int selector, int pusher,int batchSize){
        collecting = true;
        this.batchSize = batchSize;
        this.capacity = capacity;
        buffer = new ITransition[capacity];
        indexPointer = 0;
        bufferMaxPointer = 0;
        lastTransition = null;
        episodeLength =-1;
        episodeLengths = new int[capacity];
        this.selector = setSelector(selector);
        this.pusher = setPusher(pusher);
    }


    public BaseBuffer(ParRB params) {
        this(params.getCapacity(),params.getSelectorV(),params.getPusherV(),params.getBatchSize());
        if(ConfigReplayBuffer.DBG) System.out.println(this);
    }


    private void incrementIndexPointer(){
        if(ConfigReplayBuffer.DBG) System.out.println("Incrementing the indexPointer from: " +indexPointer + " to : "+  (indexPointer + 1) % capacity);
        indexPointer = (indexPointer + 1) % capacity;
    }

    /**
     * Incrementing the pointer, which indicates the maximal position of the
     *  buffer from which can be selected
     */
    private void incrementBufferMaxPointer(){
        if(ConfigReplayBuffer.DBG) System.out.println("Incrementing the BufferMaxPtr from: " +bufferMaxPointer + " to : "+  (bufferMaxPointer +1));
        if(bufferMaxPointer == capacity){
           if(episodeLength == -1) calculateEpisodeLength();
            return;
        }
        bufferMaxPointer = bufferMaxPointer +1;
    }



    /**
     * Add a transition to the replay buffer and update the pointers
     * @param t Transition
     */
    public void addTransition(ITransition t){
        if(!pusher.pushTransition(t)) return;
        if(ConfigReplayBuffer.DBG) System.out.println("Adding transition at point: " + indexPointer );
        buffer[indexPointer] = t;
        incrementIndexPointer();
        incrementBufferMaxPointer();
    }

    /**
     * Used to get the last transition index
     * @return Integer
     */
    private int lastIndexPointer(){
        if(indexPointer == 0) return capacity-1;
        return indexPointer;
    }

    /**
     * Return batches using selector
     * @return
     */
    public ITransition[] getBatches(){
        return selector.selectBatch();
    }

    /**
     * Empty the buffer and reset the pointers
     */
    public void resetBuffer(){
        buffer = new ITransition[capacity];
        indexPointer = 0;
        bufferMaxPointer = 0;
    }

    /**
     * Choose selector based on index
     * @param i Integer
     * @return Instance implements ISelector
     */
    private ISelector setSelector(int i){
        switch(i){
            case 0: return new RandomSelector(this);
            default: throw new RuntimeException("No Selector has been selected");
        }
    }

    /**
     * Choose pusher based on index
     * @param i Intger
     * @return Instance implements IPusher
     */
    private IPusher setPusher(int i){
        switch(i){
            case 0: return new BasePusher(this);
            default: throw new RuntimeException("No Pusher has been selected");
        }
    }

   public void updateEpisodeCounter(int episodeLength){
       episodeLengths[bufferMaxPointer-1] = episodeLength;
   }

    public void addToLastTransition(
            StateObsWithBoardVector sowb,
            int player,
            double vLast,
            double target,
            double r_next,
            NextState4 ns,
            StateObservation[] sLast,
            ScoreTuple rLast
            ){
        sLast = copySLast(sLast);

        lastTransition = new Transition(sowb,player,vLast,target,r_next,ns,sLast,rLast.copy());
        addTransition(lastTransition);
    }


    private StateObservation[] copySLast(StateObservation[] copy){
        StateObservation[] returnValue = new StateObservation[copy.length];
        for(int i = 0; i < copy.length; i++ ){
            returnValue[i] = copy[i].copy();
        }
        return returnValue;
    }

    private void calculateEpisodeLength(){
        double result;
        int sum = 0;
        for(int i : episodeLengths){
            sum += i;
        }
        result = sum / episodeLengths.length;
        episodeLength = (int) result;
    }

    // Getter and Setter here
    public void setCollecting(boolean b){collecting = b;}
    public int getCapacity(){return this.capacity;}
    public int getMaxBufferIndex(){
        return bufferMaxPointer;
    }

    public boolean getCollecting(){return collecting;}
    public ITransition getLastTransition(){
        return lastTransition;
    }
    public ITransition getLastTransitionIndex(){
        return buffer[lastIndexPointer()];
    }
    public ITransition[] getBuffer(){
        return buffer;
    }
    public int getBatchSize(){return batchSize;}





    public String toString(){
        String s = "-------------------REPLAY-BUFFER------------------\n";
        s += "Capacity: " + capacity + "\n";
        s += "Batch Size: " + batchSize + "\n";
        s += "Selctor: " + selector.toString() + "\n";
        s += "Pusher: " + pusher.toString() + "\n";
        s += "--------------------------------------------------";
        return s;
    }


}
