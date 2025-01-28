package params;

import controllers.SB3.SB3AgentConfig;

import javax.swing.*;
import java.io.Serializable;
import java.util.Map;


public class ParSB3 implements Serializable {


    public String agentType = SB3AgentConfig.DEFAULT_AGENT;
    public boolean selfPlay = SB3AgentConfig.DEFAULT_SELF_PLAY;
    public int trainTimeSteps = SB3AgentConfig.DEFAULT_TRAIN_TIME_STEPS;

    public Map<String, Object> parSB3Base;
    public Map<String, Object> parSB3Police;
    public Map<String, Object> parSB3Network;
    /**
     * This member is only constructed when the constructor {@link #ParSB3(boolean)} (boolean) ParMCTS(boolean withUI)}
     * called with {@code withUI=true}. It holds the GUI for {@link ParSB3}.
     */
    private transient SB3Params sb3Params = null;

    public ParSB3(boolean withUI) {
        if (withUI)
            sb3Params = new SB3Params();
    }

    public JPanel getPanel() {
        if (sb3Params !=null)
            return sb3Params.getPanel();
        return null;
    }

    public void pushFromSB3Params() {
        if (sb3Params!=null)
            this.setFrom(sb3Params);
    }

    public void setFrom(SB3Params sb3Params) {
        agentType = sb3Params.baseParameters.getAgent();
        selfPlay = sb3Params.baseParameters.getSelfPlay();
        trainTimeSteps = sb3Params.baseParameters.getTrainTimeSteps();

        parSB3Base = sb3Params.baseParameters.getParams();
        parSB3Police = sb3Params.agentParameters.getParams();
        parSB3Network = sb3Params.networkParameters.getParams();
    }

}
