package params;

import controllers.TD.TDAgent;
import controllers.TD.ntuple2.TDNTuple3Agt;

import javax.swing.*;
import java.io.Serializable;

public class ParRB implements Serializable {

    public static boolean DEFAULT_USERB = true;
    public static int DEFAULT_CAPACITY = 200;
    public static int DEFAULT_BATCHSIZE = 1;
    public static int DEFAULT_COMBOBOXES = 0; // used for entry based on index;

    private int capacity = DEFAULT_CAPACITY;
    private boolean useRB = DEFAULT_USERB;
    private int batchSize = DEFAULT_BATCHSIZE;
    private int selector = DEFAULT_COMBOBOXES;
    private int pusher = DEFAULT_COMBOBOXES;

    private transient RBParams RBparams = null;

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .agt.zip containing this object will become
     * unreadable or you have to provide a special version transformation)
     */
    private static final long serialVersionUID = 1L;

    public ParRB() {	}

    public ParRB(boolean withUI) {
        if (withUI)
            RBparams = new RBParams();
    }

    public ParRB(ParRB ep) {
        this.setFrom(ep);
    }

    public ParRB(RBParams ep) {
        this.setFrom(ep);
    }

    public void setFrom(ParRB ep) {
        this.capacity = ep.getCapacity();
        this.useRB = ep.getUseRB();
        this.batchSize = ep.getBatchSize();
        this.selector = ep.getSelectorV();
        this.pusher = ep.getPusherV();
        if (RBparams!=null)
            RBparams.setFrom(this);
    }

    public void setFrom(RBParams ep) {
        this.capacity = ep.getCapacity();
        this.useRB = ep.getUseRb();
        this.batchSize = ep.getBatchSize();
        this.selector = ep.getSelector();
        this.pusher = ep.getPusher();
        if (RBparams!=null)
            RBparams.setFrom(this);
    }

    /**
     * Call this from XArenaFuncs constructAgent or fetchAgent to get the latest changes from GUI
     */
    public void pushFromRBParams() {
        if (RBparams!=null)
            this.setFrom(RBparams);
    }

    public JPanel getPanel() {
        if (RBparams!=null)
            return RBparams.getPanel();
        return null;
    }

    public int getBatchSize(){return batchSize;}

    public void setBatchSize(int i) {
        this.batchSize = i;
        if (RBparams!=null)
            RBparams.setBatchSize(i);

    }

    public int getCapacity() {
        return capacity;
    }

    public boolean getUseRB() {
        return useRB;
    }

    public int getBatchSizeV(){return batchSize;}
    public int getPusherV(){return pusher;}
    public int getSelectorV(){return selector;}

    public void setSelector(int i){
        this.selector=i;
        if (RBparams!=null)
            RBparams.setSelector(i);
    }

    public void setPusher(int i){
        this.pusher=i;
        if(RBparams!=null){
            RBparams.setPusher(i);
        }
    }

    public void setCapacity(int num) {
        this.capacity=num;
        if (RBparams!=null)
            RBparams.setCapacity(num);
    }

    public void setUseRB(boolean b) {
        this.useRB=b;
        if (RBparams!=null)
            RBparams.setUseRb(b);
    }

    /**
     * Set sensible parameters for a specific agent and specific game. By "sensible
     * parameters" we mean parameter producing good results. If with UI, some parameter
     * choices may be enabled or disabled.
     *
     * @param agentName either "TD-Ntuple-3" (for {@link TDNTuple3Agt}) or "TDS" (for {@link TDAgent})
     * @param gameName the string from {@link games.StateObservation#getName()}
     */
    public void setParamDefaults(String agentName, String gameName) {
        switch (gameName) {
            default:	//  all other
                break;
        }
        switch (agentName) {
            default:
                this.setCapacity(1);
                this.setUseRB(true);
                this.setBatchSize(1);
                this.setPusher(0);
                this.setSelector(0);
                break;
        }
    }

}
