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
    private int capacity;
    private int batchSize;
    private ISelector selector;
    private IPusher pusher;

    public BaseBuffer(int capacity, int selector, int pusher,int batchSize){
        this.batchSize = batchSize;
        this.capacity = capacity;
        buffer = new ITransition[capacity];
        indexPointer = 0;
        bufferMaxPointer = 0;
        lastTransition = null;
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
        if(bufferMaxPointer == capacity) return;
        bufferMaxPointer = bufferMaxPointer +1;
    }





    /**
     * Used to get the last transition index
     * @return Integer
     */
    public int lastIndexPointer(){
        if(indexPointer == 0) return capacity-1;
        return indexPointer;
    }

    /**
     * Returns one batch using the selectors policy
     * The batch size is set via {@code batchSize}
     * @return
     */
    public ITransition[] getBatch(){
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
     * Entry point for the ISelector interface
     * Choose selector based on index
     *
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
     * Entry point for the IPusher Interface
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


    /**
     * This Method accepts multiple values to create a transition and stores it.
     * @param player player to whom the transition belongs to
     * @param ns    Nextstate4 object
     * @param sLast
     * @param rLast
     * @param R
     */
    public void addTransition(
            int player,
            NextState4 ns,
            StateObservation sLast,
            ScoreTuple rLast,
            ScoreTuple R,
            int isFinalTransition
    ){
        sLast = sLast.copy();
        lastTransition = new Transition(player,ns,sLast,rLast.copy(),R.copy(),isFinalTransition);
        savetransition(lastTransition);
    }

    /**
     * This method updates the internal pointers and adds the transition to the
     * replaybuffer
     *
     * @param t Transition
     */
    private void savetransition(ITransition t){
        if(!pusher.pushTransition(t)) return;
        if(ConfigReplayBuffer.DBG) System.out.println("Adding transition at point: " + indexPointer );
        buffer[indexPointer] = t;
        incrementIndexPointer();
        incrementBufferMaxPointer();
    }


    // Getter and Setter here
    public int getCapacity(){return this.capacity;}

    public int getMaxBufferIndex(){
        return bufferMaxPointer;
    }


    /**
     * @return returns the buffer array.
     */
    public ITransition[] getBuffer(){
        return buffer;
    }

    /**
     * @return the batch size
     */
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

