package controllers.ReplayBuffer.Selector;

import controllers.ReplayBuffer.Buffer.BaseBuffer;
import controllers.ReplayBuffer.Transition.ITransition;
import jdk.jshell.spi.ExecutionControl;

import java.security.SecureRandom;
import java.util.Random;


/**
 *  Base for Selectors.
 *  Each Selector must overwrite the methods
 *      ITransition[] selectBatch(int n);
 *      ITransition selectBatch();
 */
abstract class BaseSelector implements ISelector{

    private BaseBuffer buffer;
    protected Random random;
    public BaseSelector(BaseBuffer buffer){
        this.buffer = buffer;
        random = new SecureRandom();
    }

    protected ITransition[] getBuffer(){
        return buffer.getBuffer();
    }

    protected int getCapacity(){return buffer.getCapacity();}

    protected int getBatchSize(){return buffer.getBatchSize();}

    protected int getMaxBufferPosition(){
        return this.buffer.getMaxBufferIndex();
    }

    public abstract ITransition[] selectBatch();


}
