package controllers.ReplayBuffer.Pusher;

import controllers.ReplayBuffer.Buffer.BaseBuffer;
import controllers.ReplayBuffer.Transition.ITransition;

public class BasePusher implements IPusher{

    private BaseBuffer buffer;

    public BasePusher(BaseBuffer buffer){
        this.buffer = buffer;
    }


    @Override
    public boolean pushTransition(ITransition t) {
        return true;
    }


    public String toString(){
        return "Base Pusher";
    }

}
