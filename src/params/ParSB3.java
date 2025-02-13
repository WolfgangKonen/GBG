package params;

import controllers.SB3.SB3AgentConfig;

import javax.swing.*;
import java.io.Serializable;
import java.util.Map;


public class ParSB3 implements Serializable {


    public String agentType = SB3AgentConfig.DEFAULT_AGENT;
    public boolean selfPlay = SB3AgentConfig.DEFAULT_SELF_PLAY;
    public String[] enemyAgents = new String[0];
    public int trainTimeSteps = SB3AgentConfig.DEFAULT_TRAIN_TIME_STEPS;

    public Map<String, Object> parSB3Base;
    public Map<String, Object> parSB3Agent;
    public Map<String, Object> parSB3Network;
    public SelfPlayParameters parSB3SelfPlay = new SelfPlayParameters();
    public EvaluationOptions evaluationOptions;
    /**
     * This member is only constructed when the constructor {@link #ParSB3(boolean, String)} (boolean) ParMCTS(boolean withUI)}
     * called with {@code withUI=true}. It holds the GUI for {@link ParSB3}.
     */
    private transient SB3Params sb3Params = null;

    public ParSB3(boolean withUI, String gameName) {
        if (withUI)
            sb3Params = new SB3Params(gameName);
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
        enemyAgents = sb3Params.enemyAgentsParameters.getEnemyAgents();

        parSB3Base = sb3Params.baseParameters.getParams();
        parSB3Agent = sb3Params.agentParameters.getParams();
        parSB3Network = sb3Params.networkParameters.getParams();
        parSB3SelfPlay = sb3Params.baseParameters.getSelfPlayParameters();
        evaluationOptions = sb3Params.baseParameters.getEvaluationOptions();
    }

    public static class SelfPlayParameters {
        private int policyWindowSize = SB3AgentConfig.SelfPlayDefaultParameters.DEFAULT_POLICY_WINDOW_SIZE;
        private int addPolicyEveryXSteps = SB3AgentConfig.SelfPlayDefaultParameters.DEFAULT_ADD_POLICY_EVERY_X_STEPS;
        private double useLatestPolicy= SB3AgentConfig.SelfPlayDefaultParameters.DEFAULT_USE_LATEST_POLICY;

        public SelfPlayParameters() {}

        public SelfPlayParameters(int policyWindowSize, int addPolicyEveryXSteps, double useLatestPolicy) {
            this.policyWindowSize = policyWindowSize;
            this.addPolicyEveryXSteps = addPolicyEveryXSteps;
            this.useLatestPolicy = useLatestPolicy;
        }

        public int getPolicyWindowSize() {
            return policyWindowSize;
        }

        public int getAddPolicyEveryXSteps() {
            return addPolicyEveryXSteps;
        }

        public double getUseLatestPolicy() {
            return useLatestPolicy;
        }
    }

    public static class EvaluationOptions {
        private int evaluateEveryEpisodes;
        private int numberOfGames;
        private String opponent;
        private boolean safeBest;

        public EvaluationOptions(int evaluateEveryEpisodes, int numberOfGames, String opponent, boolean safeBest) {
            this.evaluateEveryEpisodes = evaluateEveryEpisodes;
            this.numberOfGames = numberOfGames;
            this.opponent = opponent;
            this.safeBest = safeBest;
        }

        public int getEvaluateEveryEpisodes() {
            return evaluateEveryEpisodes;
        }

        public int getNumberOfGames() {
            return numberOfGames;
        }

        public String getOpponent() {
            return opponent;
        }

        public boolean isSafeBest() {
            return safeBest;
        }
    }

}
