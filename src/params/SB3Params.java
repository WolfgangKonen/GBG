package params;

import controllers.SB3.SB3Agent;
import controllers.SB3.SB3AgentConfig;
import gui.MessageBox;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class SB3Params extends Frame implements Serializable {
    public JPanel mPanel;
    public BaseParameters baseParameters;
    public SB3Parameters agentParameters;
    public NetworkParameters networkParameters;
    public EnemyAgentsParameters enemyAgentsParameters;
    public EvaluationOptionsPanel evaluationOptionsPanel;

    public String gameName;

    transient private SB3Agent sb3Agent = null;

    public SB3Params(String gameName) {
        super("SB3 Parameter");

        this.mPanel = new JPanel();

        this.gameName = gameName;

        setLayout(new BorderLayout(10,0));				// rows,columns,hgap,vgap
        mPanel.setLayout(new GridLayout(0,2,10,10));

        baseParameters = new BaseParameters();
        mPanel.add(baseParameters);

        JPanel policyParameters = new DQN();
        this.agentParameters = (SB3Parameters) policyParameters;
        mPanel.add(policyParameters);

        enemyAgentsParameters = new EnemyAgentsParameters(gameName);
        networkParameters = new NetworkParameters();

        setUseOwnParametersForBaseAndAgent(!SB3AgentConfig.DEFAULT_USE_STANDARD_SB3_PARMAS);
        networkParameters.setUseOwnParameters(!SB3AgentConfig.DEFAULT_USE_STANDARD_SB3_PARMAS);

        pack();
        // setSize(1000, 1000);
        setVisible(false);
    }

    private void changeAgentPane(String newAgent) {

            System.out.println("change!" + newAgent);
            mPanel.remove(1);
            JPanel agentParams = getAgentPanel(newAgent);
            agentParameters = (SB3Parameters) agentParams;
            mPanel.add(agentParams, 1);
            agentParameters.setUseOwnParameters(!baseParameters.standardSB3ParamsCheckBox.isSelected());

            mPanel.revalidate();
            mPanel.repaint();

    }

    private JPanel getAgentPanel(String agent) {
        return switch (agent) {
            case "DQN" -> new DQN();
            case "PPO" -> new PPO();
            case "MPPO" -> new MaskablePPO();
            default -> new DQN();
        };
    }

    public void setUseOwnParametersForBaseAndAgent(boolean enabled) {
        baseParameters.setUseOwnParameters(enabled);
        agentParameters.setUseOwnParameters(enabled);
    }

    public void setFrom(ParSB3 parSB3, SB3Agent sb3Agent) {
        this.sb3Agent = sb3Agent;
        baseParameters.agentComboBox.setSelectedItem(parSB3.agentType);
        baseParameters.trainTimeStepsText.setText(String.valueOf(parSB3.trainTimeSteps));

        baseParameters.setFrom(parSB3.parSB3Base);
        enemyAgentsParameters.setFrom(parSB3.parSB3SelfPlay, parSB3.enemyAgents);
        agentParameters.setFrom(parSB3.parSB3Agent);
        networkParameters.setFrom(parSB3.parSB3Network);
        evaluationOptionsPanel.setFrom(parSB3.evaluationOptions);

    }

    public JPanel getPanel() {
        return mPanel;
    }

    public interface SB3Parameters {
        Map<String, Object> getParams();
        void setFrom(Map<String, Object> parameters);
        void setUseOwnParameters(boolean enabled);
    }

    public void setSb3Agent(SB3Agent sb3Agent) {
        this.sb3Agent = sb3Agent;
    }

    public class BaseParameters extends JPanel implements SB3Parameters{
        private JLabel titel;

        private JLabel agentLabel;
        private JComboBox agentComboBox;

        // Further Options / Params
        private JLabel evaluationOptionsLabel;
        private JButton evaluationOptionsButton;
        private JLabel loadPolicyLabel;
        private JButton loadPolicyButton;
        private JLabel opponentOptionsLabel;
        private JButton opponentOptionsButton;
        private JLabel networkParametersLabel;
        private JButton networkParametersButton;
        // Further Options / Params
        private JLabel trainTimeStepsLabel;
        private JTextField trainTimeStepsText;

        private JLabel standardSB3ParamsLabel;
        private JCheckBox standardSB3ParamsCheckBox;


        // Params Specific for SB3 (will be stored in Map)
        private JLabel learningRateLabel;
        private JTextField learningRateText;
        private JLabel statsWindowSizeLabel;
        private JTextField statsWindowSizeText;
        private JLabel tensorboardLogLabel;
        private JTextField tensorboardLogText;
        private JLabel verboseLabel;
        private JTextField verboseText;
        private JLabel seedLabel;
        private JTextField seedText;
        private JLabel deviceLabel;
        private JTextField deviceText;

        private boolean useOwnSB3Params;

        public BaseParameters() {
            titel = new JLabel("Base Parameters", JLabel.CENTER);
            Font font = titel.getFont();
            titel.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));

            agentLabel = new JLabel("Agent: ");
            agentComboBox = new JComboBox(SB3AgentConfig.DEFAULT_AGENT_OPTIONS);
            agentComboBox.addActionListener(e -> {
                if (agentComboBox.getSelectedItem() instanceof String newAgent) {
                    changeAgentPane(newAgent);
                }
            });

            // Further Options / Params
            evaluationOptionsPanel = new EvaluationOptionsPanel();
            evaluationOptionsLabel = new JLabel("Evaluation Options: ");
            evaluationOptionsButton = new JButton("Evaluation Options");
            evaluationOptionsButton.addActionListener(e -> openEvaluationOptions());

            loadPolicyLabel = new JLabel("Load a specific policy: ");
            loadPolicyButton = new JButton("Load policy");
            loadPolicyButton.addActionListener(e -> loadPolicy());

            opponentOptionsLabel = new JLabel("Manage opponents/ Self Play: ");
            opponentOptionsButton = new JButton("Opponent Options");
            opponentOptionsButton.addActionListener(e -> openOpponentOptions());

            networkParametersLabel = new JLabel("Network parameters: ");
            networkParametersButton = new JButton("Network parameters");
            networkParametersButton.addActionListener(e -> openNetworkParameters() );
            // Further Options / Params

            trainTimeStepsLabel = new JLabel("Train time steps: ");
            trainTimeStepsText = new JTextField(Integer.toString(SB3AgentConfig.DEFAULT_TRAIN_TIME_STEPS));

            standardSB3ParamsLabel = new JLabel("Use Standard Parameters by SB3?");
            standardSB3ParamsCheckBox = new JCheckBox();
            standardSB3ParamsCheckBox.setSelected(SB3AgentConfig.DEFAULT_USE_STANDARD_SB3_PARMAS);
            useOwnSB3Params = SB3AgentConfig.DEFAULT_USE_STANDARD_SB3_PARMAS;
            standardSB3ParamsCheckBox.addItemListener(e -> setUseOwnParametersForBaseAndAgent(
                    !standardSB3ParamsCheckBox.isSelected())
            );



            // Params Specific for SB3 (will be stored in Map)
            learningRateLabel = new JLabel("Learning rate: ");
            learningRateText = new JTextField(Double.toString(SB3AgentConfig.DEFAULT_LEARNING_RATE));

            statsWindowSizeLabel = new JLabel("Stats window size: ");
            statsWindowSizeText = new JTextField(Integer.toString(SB3AgentConfig.DEFAULT_STATS_WINDOW_SIZE));

            tensorboardLogLabel = new JLabel("Tensorboard log: ");
            tensorboardLogText = new JTextField(String.valueOf(SB3AgentConfig.DEFAULT_TENSORBOARD_LOG));


            verboseLabel = new JLabel("Verbose: ");
            verboseText = new JTextField(Integer.toString(SB3AgentConfig.DEFAULT_VERBOSE));

            seedLabel = new JLabel("Seed: ");
            seedText = new JTextField(String.valueOf(SB3AgentConfig.DEFAULT_SEED));

            deviceLabel = new JLabel("Device: ");
            deviceText = new JTextField(SB3AgentConfig.DEFAULT_DEVICE);

            // Set tool tips
            agentLabel.setToolTipText(SB3AgentConfig.TIP_AGENT);
            trainTimeStepsLabel.setToolTipText(SB3AgentConfig.TIP_TRAIN_TIMESTEPS);
            standardSB3ParamsLabel.setToolTipText(SB3AgentConfig.TIP_STANDARD_SB3_PARAMS);
            learningRateLabel.setToolTipText(SB3AgentConfig.TIP_LEARNING_RATE);
            statsWindowSizeLabel.setToolTipText(SB3AgentConfig.TIP_STATS_WINDOW_SIZE);
            tensorboardLogLabel.setToolTipText(SB3AgentConfig.TIP_TENSORBOARD_LOG);
            verboseLabel.setToolTipText(SB3AgentConfig.TIP_VERBOSE);
            seedLabel.setToolTipText(SB3AgentConfig.TIP_SEED);
            deviceLabel.setToolTipText(SB3AgentConfig.TIP_DEVICE);

            this.setLayout(new GridLayout(0,2,10,10));

            this.add(titel);
            this.add(new JLabel());

            this.add(agentLabel);
            this.add(agentComboBox);

            // Further Options / Params
            this.add(evaluationOptionsLabel);
            this.add(evaluationOptionsButton);
            this.add(loadPolicyLabel);
            this.add(loadPolicyButton);
            this.add(opponentOptionsLabel);
            this.add(opponentOptionsButton);
            this.add(networkParametersLabel);
            this.add(networkParametersButton);
            // Further Options / Params

            this.add(trainTimeStepsLabel);
            this.add(trainTimeStepsText);

            this.add(standardSB3ParamsLabel);
            this.add(standardSB3ParamsCheckBox);

            // Params Specific for SB3 (will be stored in Map)
            this.add(learningRateLabel);
            this.add(learningRateText);
            this.add(statsWindowSizeLabel);
            this.add(statsWindowSizeText);
            this.add(tensorboardLogLabel);
            this.add(tensorboardLogText);
            this.add(verboseLabel);
            this.add(verboseText);
            this.add(seedLabel);
            this.add(seedText);
            this.add(deviceLabel);
            this.add(deviceText);
        }

        public void setUseOwnParameters(boolean enabled) {
            useOwnSB3Params = enabled;

            learningRateText.setEnabled(enabled);
            statsWindowSizeText.setEnabled(enabled);
            tensorboardLogText.setEnabled(enabled);
            verboseText.setEnabled(enabled);
            seedText.setEnabled(enabled);
            deviceText.setEnabled(enabled);

            learningRateLabel.setEnabled(enabled);
            statsWindowSizeLabel.setEnabled(enabled);
            tensorboardLogLabel.setEnabled(enabled);
            verboseLabel.setEnabled(enabled);
            seedLabel.setEnabled(enabled);
            deviceLabel.setEnabled(enabled); // TODO: DO we need device?
        }

        public void setFrom(Map<String, Object> baseParameters) {
            learningRateText.setText(String.valueOf(baseParameters.get("learning_rate")));
            statsWindowSizeText.setText(String.valueOf(baseParameters.get("stats_window_size")));
            tensorboardLogText.setText(String.valueOf(baseParameters.get("tensorboard_log")));
            verboseText.setText(String.valueOf(baseParameters.get("verbose")));
            seedText.setText(String.valueOf(baseParameters.get("seed")));
        }

        @Override
        public Map<String, Object> getParams() {
            Map<String, Object> params = new HashMap<>();

            if(!useOwnSB3Params) return params;

            params.put("learning_rate", getLearningRate());
            params.put("stats_window_size", getStatsWindowSize());
            params.put("tensorboard_log", getTensorboardLog());
            params.put("verbose", getVerbose());
            params.put("seed", getSeed());

            return params;
        }

        public String getAgent() {
            if (agentComboBox.getSelectedItem() instanceof String policy) {
                return policy;
            }
            return SB3AgentConfig.DEFAULT_AGENT;
        }

        public int getTrainTimeSteps() {
            return Integer.parseInt(trainTimeStepsText.getText());
        }

        public double getLearningRate() {
            return Double.parseDouble(learningRateText.getText());
        }

        public int getStatsWindowSize() {
            return Integer.parseInt(statsWindowSizeText.getText());
        }

        public String getTensorboardLog() {
            if(tensorboardLogText.getText() == null || Objects.equals(tensorboardLogText.getText(), "") || Objects.equals(tensorboardLogText.getText(), "null")) return null;
            return tensorboardLogText.getText();
        }

        public int getVerbose() {
            return Integer.parseInt(verboseText.getText());
        }

        public Integer getSeed() {
            if(seedText.getText() == null || Objects.equals(seedText.getText(), "") || Objects.equals(seedText.getText(), "null")) return null;
            return Integer.parseInt(seedText.getText());
        }

        public String getDevice() {
            return deviceText.getText();
        }

        public ParSB3.EvaluationOptions getEvaluationOptions() {
            return evaluationOptionsPanel.getEvaluationOptions();
        }

        private void openEvaluationOptions() {
            JFrame opponentSettings = new JFrame("Evaluation Options for SB3 agent");
            opponentSettings.add(evaluationOptionsPanel);
            opponentSettings.setSize(500, 300);
            opponentSettings.setVisible(true);
        }

        private void openOpponentOptions() {
            JFrame opponentOptionsFrame = new JFrame("Opponent options");
            opponentOptionsFrame.add(enemyAgentsParameters);
            opponentOptionsFrame.setSize(500, 500);
            opponentOptionsFrame.setVisible(true);
        }

        private void openNetworkParameters() {
            JFrame networkParametersFrame = new JFrame("Network Parameters");
            networkParametersFrame.add(networkParameters);
            networkParametersFrame.setSize(500, 400);
            networkParametersFrame.setVisible(true);
        }



        private void loadPolicy() {
            if (sb3Agent == null) {
                System.out.println("No SB3Agent set.");
                //TODO popo up no sccess
                return;
            }

            String path = Types.GUI_DEFAULT_DIR_AGENT+"/"+gameName + "/" + "SB3Agent" +"/" + sb3Agent.getAgentType() + "/" + sb3Agent.getId();
            if (!Files.exists(Paths.get(path))) {
                System.out.println(path);
                System.out.println("For this agent no SB3 policy have been saved.");
                //TODO pop up no success
                return;
            }

            JFileChooser fileChooser = new JFileChooser(Types.GUI_DEFAULT_DIR_AGENT+"/"+gameName + "/" + "SB3Agent" +"/" + sb3Agent.getAgentType() + "/" + sb3Agent.getId());
            int returnVal = fileChooser.showOpenDialog(this);
            String filePath = null;
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                filePath = fileChooser.getSelectedFile().getPath();
            }
            sb3Agent.loadSB3PolicyFromPath(filePath);
            // TODO: pop up success
        }
    }

    public class EnemyAgentsParameters extends JPanel {
        private JLabel enemiesLabel;
        private DefaultListModel<String> listModel;
        private JList enemies;

        private JLabel addEnemyLabel;
        private JComboBox addEnemyComboBox;

        private JButton addEnemyButton;
        private JButton removeEnemyButton;

        private JButton loadEnemyButton;

        // Self Play parameters
        private JLabel selfPLayLabel;
        private JLabel selfPlayPolicyWindowSizeLabel;
        private JTextField selfPlayPolicyWindowSizeText;
        private JLabel addSelfPlayPolicyEveryXStepsLabel;
        private JTextField addSelfPlayPolicyEveryXStepsText;
        private JLabel useLatestSelfPLayPolicyLabel;
        private JTextField useLatestSelfPLayPolicyText;


        private String gameName;

        public EnemyAgentsParameters(String gameName) {
            this.gameName = gameName;

            enemiesLabel = new JLabel("Opponents:");
            listModel = new DefaultListModel<>();
            enemies = new JList(listModel);

            addEnemyLabel = new JLabel("Choose an opponent then add or load an opponent: ");
            addEnemyComboBox = new JComboBox(SB3AgentConfig.EnemyAgentsDefaultValues.DEFAULT_ENEMIES);

            addEnemyButton = new JButton("Add opponent");
            addEnemyButton.addActionListener(e -> addEnemy());

            removeEnemyButton = new JButton("Remove");
            removeEnemyButton.addActionListener(e -> removeEnemy());

            loadEnemyButton = new JButton("Load opponent");
            loadEnemyButton.addActionListener(e -> loadEnemy());

            // Self Play
            selfPLayLabel = new JLabel("Parameters for self play");
            Font font = selfPLayLabel.getFont();
            selfPLayLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));

            selfPlayPolicyWindowSizeLabel = new JLabel("Window size of past policies for self play:");
            selfPlayPolicyWindowSizeText = new JTextField(Integer.toString(SB3AgentConfig.SelfPlayDefaultParameters.DEFAULT_POLICY_WINDOW_SIZE));

            addSelfPlayPolicyEveryXStepsLabel = new JLabel("add policy for self play ervery:");
            addSelfPlayPolicyEveryXStepsText = new JTextField(Integer.toString(SB3AgentConfig.SelfPlayDefaultParameters.DEFAULT_ADD_POLICY_EVERY_X_STEPS));

            useLatestSelfPLayPolicyLabel = new JLabel("Use latest policy ration:");
            useLatestSelfPLayPolicyText = new JTextField(Double.toString(SB3AgentConfig.SelfPlayDefaultParameters.DEFAULT_USE_LATEST_POLICY));
            // Self Play

            this.setLayout(new GridLayout(0, 2, 10, 10));

            this.add(enemiesLabel);
            this.add(new JScrollPane(enemies));

            this.add(addEnemyLabel);
            this.add(addEnemyComboBox);

            this.add(addEnemyButton);
            this.add(removeEnemyButton);

            this.add(loadEnemyButton);
            this.add(new JLabel());

            // Self Play
            this.add(selfPLayLabel);
            this.add(new JLabel());

            this.add(selfPlayPolicyWindowSizeLabel);
            this.add(selfPlayPolicyWindowSizeText);
            this.add(addSelfPlayPolicyEveryXStepsLabel);
            this.add(addSelfPlayPolicyEveryXStepsText);
            this.add(useLatestSelfPLayPolicyLabel);
            this.add(useLatestSelfPLayPolicyText);
            // Self Play

        }

        private void addEnemy() {
            listModel.addElement((String) addEnemyComboBox.getSelectedItem());
        }

        private void removeEnemy() {
            int index = enemies.getSelectedIndex();
            if (index >= 0) {
                listModel.remove(index);
            } else {
                MessageBox.show(this, "Please select an enemy.", "select an enemy", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void loadEnemy() {
            JFileChooser fileChooser = new JFileChooser(Types.GUI_DEFAULT_DIR_AGENT+"/"+gameName);
            int returnVal = fileChooser.showOpenDialog(this);
            String filePath = null;
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                filePath = fileChooser.getSelectedFile().getName();
            }
            listModel.addElement(filePath);
        }

        public String[] getEnemyAgents() {
            if (listModel.isEmpty()) return new String[0];
            return Arrays.copyOf(listModel.toArray(), listModel.size(), String[].class);
        }

        public void setFrom(ParSB3.SelfPlayParameters selfPlayParameters, String[] enemyAgents) {
            listModel.clear();
            for (String opponent: enemyAgents) {
                listModel.addElement(opponent);
            }

            selfPlayPolicyWindowSizeText.setText(Integer.toString(selfPlayParameters.getPolicyWindowSize()));
            addSelfPlayPolicyEveryXStepsText.setText(Integer.toString(selfPlayParameters.getAddPolicyEveryXSteps()));
            useLatestSelfPLayPolicyText.setText(Double.toString(selfPlayParameters.getUseLatestPolicy()));
        }

        public ParSB3.SelfPlayParameters getSelfPlayParameters() {
            return new ParSB3.SelfPlayParameters(
                    Integer.parseInt(selfPlayPolicyWindowSizeText.getText()),
                    Integer.parseInt(addSelfPlayPolicyEveryXStepsText.getText()),
                    Double.parseDouble(useLatestSelfPLayPolicyText.getText())
            );
        }
    }

    public class DQN extends JPanel implements SB3Parameters {
        private JLabel titel;

        private JLabel bufferSizeLabel;
        private JTextField bufferSizeText;
        private JLabel learningStartsLabel;
        private JTextField learningStartsText;
        private JLabel batchSizeLabel;
        private JTextField batchSizeText;
        private JLabel tauLabel;
        private JTextField tauText;
        private JLabel gammaLabel;
        private JTextField gammaText;
        private JLabel trainFreqLabel;
        private JTextField trainFreqText;
        private JLabel gradientStepsLabel;
        private JTextField gradientStepsText;
        private JLabel optimizeMemoryUsageLabel;
        private JCheckBox optimizeMemoryUsageCheckBox;
        private JLabel targetUpdateIntervalLabel;
        private JTextField targetUpdateIntervalText;
        private JLabel explorationFractionLabel;
        private JTextField explorationFractionText;
        private JLabel explorationInitialEpsLabel;
        private JTextField explorationInitialEpsText;
        private JLabel explorationFinalEpsLabel;
        private JTextField explorationFinalEpsText;
        private JLabel maxGradNormLabel;
        private JTextField maxGradNormText;

        private boolean useOwnSB3Params;

        public DQN() {
            titel = new JLabel("DQN Parameters");
            Font font = titel.getFont();
            titel.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));

            bufferSizeLabel = new JLabel("Replay buffer size: ");
            bufferSizeText = new JTextField(Integer.toString(SB3AgentConfig.DQNDefaultValues.DEFAULT_BUFFERSIZE));

            learningStartsLabel = new JLabel("Learning starts at: ");
            learningStartsText = new JTextField(Integer.toString(SB3AgentConfig.DQNDefaultValues.DEFAULT_LEARNING_STARTS));

            batchSizeLabel = new JLabel("Batch size: ");
            batchSizeText = new JTextField(Integer.toString(SB3AgentConfig.DQNDefaultValues.DEFAULT_BATCH_SIZE));

            tauLabel = new JLabel("Tau: ");
            tauText = new JTextField(Double.toString(SB3AgentConfig.DQNDefaultValues.DEFAULT_TAU));

            gammaLabel = new JLabel("Gamma: ");
            gammaText = new JTextField(Double.toString(SB3AgentConfig.DQNDefaultValues.DEFAULT_GAMMA));

            trainFreqLabel = new JLabel("Train frequency: ");
            trainFreqText = new JTextField(Integer.toString(SB3AgentConfig.DQNDefaultValues.DEFAULT_TRAIN_FREQ));

            gradientStepsLabel = new JLabel("Gradient steps: ");
            gradientStepsText = new JTextField(Integer.toString(SB3AgentConfig.DQNDefaultValues.DEFAULT_GRADIENT_STEPS));

            optimizeMemoryUsageLabel = new JLabel("Optimize memory usage: ");
            optimizeMemoryUsageCheckBox = new JCheckBox();
            optimizeMemoryUsageCheckBox.setSelected(SB3AgentConfig.DQNDefaultValues.DEFAULT_OPTIMIZE_MEMORY_USAGE);

            targetUpdateIntervalLabel = new JLabel("Target update interval: ");
            targetUpdateIntervalText = new JTextField(Integer.toString(SB3AgentConfig.DQNDefaultValues.DEFAULT_TARGET_UPDATE_INTERVAL));

            explorationFractionLabel = new JLabel("Exploration fraction: ");
            explorationFractionText = new JTextField(Double.toString(SB3AgentConfig.DQNDefaultValues.DEFAULT_EXPLORATION_FRACTION));

            explorationInitialEpsLabel = new JLabel("Exploration initial epsilon: ");
            explorationInitialEpsText = new JTextField(Double.toString(SB3AgentConfig.DQNDefaultValues.DEFAULT_EXPLORATION_INITIAL_EPS));

            explorationFinalEpsLabel = new JLabel("Exploration final epsilon: ");
            explorationFinalEpsText = new JTextField(Double.toString(SB3AgentConfig.DQNDefaultValues.DEFAULT_EXPLORATION_FINAL_EPS));

            maxGradNormLabel = new JLabel("Max gradient norm: ");
            maxGradNormText = new JTextField(Double.toString(SB3AgentConfig.DQNDefaultValues.DEFAULT_MAX_GRAD_NORM));

            bufferSizeLabel.setToolTipText(SB3AgentConfig.DQNDefaultValues.TIP_BUFFER_SIZE);
            learningStartsLabel.setToolTipText(SB3AgentConfig.DQNDefaultValues.TIP_LEARNING_STARTS);
            batchSizeLabel.setToolTipText(SB3AgentConfig.DQNDefaultValues.TIP_BATCH_SIZE);
            tauLabel.setToolTipText(SB3AgentConfig.DQNDefaultValues.TIP_TAU);
            gammaLabel.setToolTipText(SB3AgentConfig.DQNDefaultValues.TIP_GAMMA);
            trainFreqLabel.setToolTipText(SB3AgentConfig.DQNDefaultValues.TIP_TRAIN_FREQ);
            gradientStepsLabel.setToolTipText(SB3AgentConfig.DQNDefaultValues.TIP_GRADIENT_STEPS);
            optimizeMemoryUsageLabel.setToolTipText(SB3AgentConfig.DQNDefaultValues.TIP_OPTIMIZE_MEMORY_USAGE);
            targetUpdateIntervalLabel.setToolTipText(SB3AgentConfig.DQNDefaultValues.TIP_TARGET_UPDATE_INTERVAL);
            explorationFractionLabel.setToolTipText(SB3AgentConfig.DQNDefaultValues.TIP_EXPLORATION_FRACTION);
            explorationInitialEpsLabel.setToolTipText(SB3AgentConfig.DQNDefaultValues.TIP_EXPLORATION_INITIAL_EPS);
            explorationFinalEpsLabel.setToolTipText(SB3AgentConfig.DQNDefaultValues.TIP_EXPLORATION_FINAL_EPS);
            maxGradNormLabel.setToolTipText(SB3AgentConfig.DQNDefaultValues.TIP_MAX_GRAD_NORM);


            useOwnSB3Params = !SB3AgentConfig.DEFAULT_USE_STANDARD_SB3_PARMAS;

            this.setLayout(new GridLayout(0,2,10,10));

            this.add(titel);
            this.add(new JLabel());

            this.add(bufferSizeLabel);
            this.add(bufferSizeText);
            this.add(learningStartsLabel);
            this.add(learningStartsText);
            this.add(batchSizeLabel);
            this.add(batchSizeText);
            this.add(tauLabel);
            this.add(tauText);
            this.add(gammaLabel);
            this.add(gammaText);
            this.add(trainFreqLabel);
            this.add(trainFreqText);
            this.add(gradientStepsLabel);
            this.add(gradientStepsText);
            this.add(optimizeMemoryUsageLabel);
            this.add(optimizeMemoryUsageCheckBox);
            this.add(targetUpdateIntervalLabel);
            this.add(targetUpdateIntervalText);
            this.add(explorationFractionLabel);
            this.add(explorationFractionText);
            this.add(explorationInitialEpsLabel);
            this.add(explorationInitialEpsText);
            this.add(explorationFinalEpsLabel);
            this.add(explorationFinalEpsText);
            this.add(maxGradNormLabel);
            this.add(maxGradNormText);
        }

        public void setUseOwnParameters(boolean enabled) {
            useOwnSB3Params = enabled;

            bufferSizeText.setEnabled(enabled);
            learningStartsText.setEnabled(enabled);
            batchSizeText.setEnabled(enabled);
            tauText.setEnabled(enabled);
            gammaText.setEnabled(enabled);
            trainFreqText.setEnabled(enabled);
            gradientStepsText.setEnabled(enabled);
            optimizeMemoryUsageCheckBox.setEnabled(enabled);
            targetUpdateIntervalText.setEnabled(enabled);
            explorationFractionText.setEnabled(enabled);
            explorationInitialEpsText.setEnabled(enabled);
            explorationFinalEpsText.setEnabled(enabled);
            maxGradNormText.setEnabled(enabled);

            bufferSizeLabel.setEnabled(enabled);
            learningStartsLabel.setEnabled(enabled);
            batchSizeLabel.setEnabled(enabled);
            tauLabel.setEnabled(enabled);
            gammaLabel.setEnabled(enabled);
            trainFreqLabel.setEnabled(enabled);
            gradientStepsLabel.setEnabled(enabled);
            optimizeMemoryUsageLabel.setEnabled(enabled);
            targetUpdateIntervalLabel.setEnabled(enabled);
            explorationFractionLabel.setEnabled(enabled);
            explorationInitialEpsLabel.setEnabled(enabled);
            explorationFinalEpsLabel.setEnabled(enabled);
            maxGradNormLabel.setEnabled(enabled);
        }

        public void setFrom(Map<String, Object> dqnParameters) {
            bufferSizeText.setText(String.valueOf(dqnParameters.get("buffer_size")));
            learningStartsText.setText(String.valueOf(dqnParameters.get("learning_starts")));
            batchSizeText.setText(String.valueOf(dqnParameters.get("batch_size")));
            tauText.setText(String.valueOf(dqnParameters.get("tau")));
            gammaText.setText(String.valueOf(dqnParameters.get("gamma")));
            trainFreqText.setText(String.valueOf(dqnParameters.get("train_freq")));
            gradientStepsText.setText(String.valueOf(dqnParameters.get("gradient_steps")));
            optimizeMemoryUsageCheckBox.setSelected(Boolean.parseBoolean(String.valueOf(dqnParameters.get("optimize_memory_usage"))));
            targetUpdateIntervalText.setText(String.valueOf(dqnParameters.get("target_update_interval")));
            explorationFractionText.setText(String.valueOf(dqnParameters.get("exploration_fraction")));
            explorationInitialEpsText.setText(String.valueOf(dqnParameters.get("exploration_initial_eps")));
            explorationFinalEpsText.setText(String.valueOf(dqnParameters.get("exploration_final_eps")));
            maxGradNormText.setText(String.valueOf(dqnParameters.get("max_grad_norm")));
        }

        @Override
        public Map<String, Object> getParams() {
            Map<String, Object> params = new HashMap<>();

            if(!useOwnSB3Params) return params;

            params.put("buffer_size", getBufferSize());
            params.put("learning_starts", getLearningStarts());
            params.put("batch_size", getBatchSize());
            params.put("tau", getTau());
            params.put("gamma", getGamma());
            params.put("train_freq", getTrainFreq());
            params.put("gradient_steps", getGradientSteps());
            params.put("optimize_memory_usage", getOptimizeMemoryUsage());
            params.put("target_update_interval", getTargetUpdateInterval());
            params.put("exploration_fraction", getExplorationFraction());
            params.put("exploration_initial_eps", getExplorationInitialEps());
            params.put("exploration_final_eps", getExplorationFinalEps());
            params.put("max_grad_norm", getMaxGradNorm());

            return params;
        }

        public int getBufferSize() {
            return Integer.parseInt(bufferSizeText.getText());
        }

        public int getLearningStarts() {
            return Integer.parseInt(learningStartsText.getText());
        }

        public int getBatchSize() {
            return Integer.parseInt(batchSizeText.getText());
        }

        public double getTau() {
            return Double.parseDouble(tauText.getText());
        }

        public double getGamma() {
            return Double.parseDouble(gammaText.getText());
        }

        public int getTrainFreq() {
            return Integer.parseInt(trainFreqText.getText());
        }

        public int getGradientSteps() {
            return Integer.parseInt(gradientStepsText.getText());
        }

        public boolean getOptimizeMemoryUsage() {
            return optimizeMemoryUsageCheckBox.isSelected();
        }

        public int getTargetUpdateInterval() {
            return Integer.parseInt(targetUpdateIntervalText.getText());
        }

        public double getExplorationFraction() {
            return Double.parseDouble(explorationFractionText.getText());
        }

        public double getExplorationInitialEps() {
            return Double.parseDouble(explorationInitialEpsText.getText());
        }

        public double getExplorationFinalEps() {
            return Double.parseDouble(explorationFinalEpsText.getText());
        }

        public double getMaxGradNorm() {
            return Double.parseDouble(maxGradNormText.getText());
        }
    }

    public class PPO extends DefaultPPO {

        private JLabel useSdeLabel;
        private JCheckBox useSdeCheckBox;
        private JLabel sdeSampleFreqLabel;
        private JTextField sdeSampleFreqText;
        public PPO() {
            super();

            useSdeLabel = new JLabel("Use SDE: ");
            useSdeCheckBox = new JCheckBox();
            useSdeCheckBox.setSelected(SB3AgentConfig.PPODefaultValues.DEFAULT_USE_SDE);

            sdeSampleFreqLabel = new JLabel("SDE sample frequency: ");
            sdeSampleFreqText = new JTextField(Integer.toString(SB3AgentConfig.PPODefaultValues.DEFAULT_SDE_SAMPLE_FREQ));

            useSdeLabel.setToolTipText(SB3AgentConfig.PPODefaultValues.TIP_USE_SDE);
            sdeSampleFreqLabel.setToolTipText(SB3AgentConfig.PPODefaultValues.TIP_SDE_SAMPLE_FREQ);
            this.add(useSdeLabel);
            this.add(useSdeCheckBox);
            this.add(sdeSampleFreqLabel);
            this.add(sdeSampleFreqText);

        }

        @Override
        public void setUseOwnParameters(boolean enabled) {
            super.setUseOwnParameters(enabled);
            useSdeCheckBox.setEnabled(enabled);
            sdeSampleFreqText.setEnabled(enabled);

            useSdeLabel.setEnabled(enabled);
            sdeSampleFreqLabel.setEnabled(enabled);
        }

        @Override
        public void setFrom(Map<String, Object> ppoParameters) {
            super.setFrom(ppoParameters);
            useSdeCheckBox.setSelected(Boolean.parseBoolean(String.valueOf(ppoParameters.get("use_sde"))));
            sdeSampleFreqText.setText(String.valueOf(ppoParameters.get("sde_sample_freq")));
        }

        @Override
        public Map<String, Object> getParams() {
            Map<String, Object> params = super.getParams();
            params.put("use_sde", getUseSde());
            params.put("sde_sample_freq", getSdeSampleFreq());
            return params;
        }

        public boolean getUseSde() {
            return useSdeCheckBox.isSelected();
        }

        public int getSdeSampleFreq() {
            return Integer.parseInt(sdeSampleFreqText.getText());
        }
    }

    public class MaskablePPO extends DefaultPPO {
        public MaskablePPO() {
            super();
            title.setText("MaskablePPO Parameters");

        }
    }

    public class DefaultPPO extends JPanel implements SB3Parameters {
        protected JLabel title;

        private JLabel nStepsLabel;
        private JTextField nStepsText;
        private JLabel batchSizeLabel;
        private JTextField batchSizeText;
        private JLabel nEpochsLabel;
        private JTextField nEpochsText;
        private JLabel gammaLabel;
        private JTextField gammaText;
        private JLabel gaeLambdaLabel;
        private JTextField gaeLambdaText;
        private JLabel clipRangeLabel;
        private JTextField clipRangeText;
        private JLabel clipRangeVfLabel;
        private JTextField clipRangeVfText;
        private JLabel normalizeAdvantageLabel;
        private JCheckBox normalizeAdvantageCheckBox;
        private JLabel entCoefLabel;
        private JTextField entCoefText;
        private JLabel vfCoefLabel;
        private JTextField vfCoefText;
        private JLabel maxGradNormLabel;
        private JTextField maxGradNormText;
        private JLabel targetKlLabel;
        private JTextField targetKlText;

        private boolean useOwnSB3Params;

        public DefaultPPO() {
            title = new JLabel("PPO Parameters");
            Font font = title.getFont();
            title.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));

            nStepsLabel = new JLabel("n_steps: ");
            nStepsText = new JTextField(Integer.toString(SB3AgentConfig.PPODefaultValues.DEFAULT_N_STEPS));

            batchSizeLabel = new JLabel("Batch size: ");
            batchSizeText = new JTextField(Integer.toString(SB3AgentConfig.PPODefaultValues.DEFAULT_BATCH_SIZE));

            nEpochsLabel = new JLabel("n_epochs: ");
            nEpochsText = new JTextField(Integer.toString(SB3AgentConfig.PPODefaultValues.DEFAULT_N_EPOCHS));

            gammaLabel = new JLabel("Gamma: ");
            gammaText = new JTextField(Double.toString(SB3AgentConfig.PPODefaultValues.DEFAULT_GAMMA));

            gaeLambdaLabel = new JLabel("gae_lambda: ");
            gaeLambdaText = new JTextField(Double.toString(SB3AgentConfig.PPODefaultValues.DEFAULT_GAE_LAMBDA));

            clipRangeLabel = new JLabel("clip_range: ");
            clipRangeText = new JTextField(Double.toString(SB3AgentConfig.PPODefaultValues.DEFAULT_CLIP_RANGE));

            clipRangeVfLabel = new JLabel("clip_range_vf: ");
            clipRangeVfText = (SB3AgentConfig.PPODefaultValues.DEFAULT_CLIP_RANGE_VF == null) ? new JTextField():
                new JTextField(Double.toString(SB3AgentConfig.PPODefaultValues.DEFAULT_CLIP_RANGE_VF));

            normalizeAdvantageLabel = new JLabel("Normalize advantage: ");
            normalizeAdvantageCheckBox = new JCheckBox();
            normalizeAdvantageCheckBox.setSelected(SB3AgentConfig.PPODefaultValues.DEFAULT_NORMALIZE_ADVANTAGE);

            entCoefLabel = new JLabel("Entropy coefficient: ");
            entCoefText = new JTextField(Double.toString(SB3AgentConfig.PPODefaultValues.DEFAULT_ENT_COEF));

            vfCoefLabel = new JLabel("Value function coefficient: ");
            vfCoefText = new JTextField(Double.toString(SB3AgentConfig.PPODefaultValues.DEFAULT_VF_COEF));

            maxGradNormLabel = new JLabel("Max gradient norm: ");
            maxGradNormText = new JTextField(Double.toString(SB3AgentConfig.PPODefaultValues.DEFAULT_MAX_GRAD_NORM));

            targetKlLabel = new JLabel("Target KL: ");
            targetKlText = (SB3AgentConfig.PPODefaultValues.DEFAULT_TARGET_KL == null) ? new JTextField():
                new JTextField(Double.toString(SB3AgentConfig.PPODefaultValues.DEFAULT_TARGET_KL));

            useOwnSB3Params = SB3AgentConfig.DEFAULT_USE_STANDARD_SB3_PARMAS;

            // set tool tips
            nStepsLabel.setToolTipText(SB3AgentConfig.PPODefaultValues.TIP_N_STEPS);
            batchSizeLabel.setToolTipText(SB3AgentConfig.PPODefaultValues.TIP_BATCH_SIZE);
            nEpochsLabel.setToolTipText(SB3AgentConfig.PPODefaultValues.TIP_N_EPOCHS);
            gammaLabel.setToolTipText(SB3AgentConfig.PPODefaultValues.TIP_GAMMA);
            gaeLambdaLabel.setToolTipText(SB3AgentConfig.PPODefaultValues.TIP_GAE_LAMBDA);
            clipRangeLabel.setToolTipText(SB3AgentConfig.PPODefaultValues.TIP_CLIP_RANGE);
            clipRangeVfLabel.setToolTipText(SB3AgentConfig.PPODefaultValues.TIP_CLIP_RANGE_VF);
            normalizeAdvantageLabel.setToolTipText(SB3AgentConfig.PPODefaultValues.TIP_NORMALIZE_ADVANTAGE);
            entCoefLabel.setToolTipText(SB3AgentConfig.PPODefaultValues.TIP_ENT_COEF);
            vfCoefLabel.setToolTipText(SB3AgentConfig.PPODefaultValues.TIP_VF_COEF);
            maxGradNormLabel.setToolTipText(SB3AgentConfig.PPODefaultValues.TIP_MAX_GRAD_NORM);
            targetKlLabel.setToolTipText(SB3AgentConfig.PPODefaultValues.TIP_TARGET_KL);

            this.setLayout(new GridLayout(0, 2, 10, 10));

            this.add(title);
            this.add(new JLabel());

            this.add(nStepsLabel);
            this.add(nStepsText);
            this.add(batchSizeLabel);
            this.add(batchSizeText);
            this.add(nEpochsLabel);
            this.add(nEpochsText);
            this.add(gammaLabel);
            this.add(gammaText);
            this.add(gaeLambdaLabel);
            this.add(gaeLambdaText);
            this.add(clipRangeLabel);
            this.add(clipRangeText);
            this.add(clipRangeVfLabel);
            this.add(clipRangeVfText);
            this.add(normalizeAdvantageLabel);
            this.add(normalizeAdvantageCheckBox);
            this.add(entCoefLabel);
            this.add(entCoefText);
            this.add(vfCoefLabel);
            this.add(vfCoefText);
            this.add(maxGradNormLabel);
            this.add(maxGradNormText);
            this.add(targetKlLabel);
            this.add(targetKlText);
        }

        public void setUseOwnParameters(boolean enabled) {
            useOwnSB3Params = enabled;

            nStepsText.setEnabled(enabled);
            batchSizeText.setEnabled(enabled);
            nEpochsText.setEnabled(enabled);
            gammaText.setEnabled(enabled);
            gaeLambdaText.setEnabled(enabled);
            clipRangeText.setEnabled(enabled);
            clipRangeVfText.setEnabled(enabled);
            normalizeAdvantageCheckBox.setEnabled(enabled);
            entCoefText.setEnabled(enabled);
            vfCoefText.setEnabled(enabled);
            maxGradNormText.setEnabled(enabled);
            targetKlText.setEnabled(enabled);

            nStepsLabel.setEnabled(enabled);
            batchSizeLabel.setEnabled(enabled);
            nEpochsLabel.setEnabled(enabled);
            gammaLabel.setEnabled(enabled);
            gaeLambdaLabel.setEnabled(enabled);
            clipRangeLabel.setEnabled(enabled);
            clipRangeVfLabel.setEnabled(enabled);
            normalizeAdvantageLabel.setEnabled(enabled);
            entCoefLabel.setEnabled(enabled);
            vfCoefLabel.setEnabled(enabled);
            maxGradNormLabel.setEnabled(enabled);
            targetKlLabel.setEnabled(enabled);
        }

        public void setFrom(Map<String, Object> ppoParameters) {
            nStepsText.setText(String.valueOf(ppoParameters.get("n_steps")));
            batchSizeText.setText(String.valueOf(ppoParameters.get("batch_size")));
            nEpochsText.setText(String.valueOf(ppoParameters.get("n_epochs")));
            gammaText.setText(String.valueOf(ppoParameters.get("gamma")));
            gaeLambdaText.setText(String.valueOf(ppoParameters.get("gae_lambda")));
            clipRangeText.setText(String.valueOf(ppoParameters.get("clip_range")));
            clipRangeVfText.setText(String.valueOf(ppoParameters.get("clip_range_vf")));
            normalizeAdvantageCheckBox.setSelected(Boolean.parseBoolean(String.valueOf(ppoParameters.get("normalize_advantage"))));
            entCoefText.setText(String.valueOf( ppoParameters.get("ent_coef")));
            vfCoefText.setText(String.valueOf(ppoParameters.get("vf_coef")));
            maxGradNormText.setText(String.valueOf(ppoParameters.get("max_grad_norm")));
            targetKlText.setText(String.valueOf(ppoParameters.get("target_kl")));
        }

        @Override
        public Map<String, Object> getParams() {
            Map<String, Object> params = new HashMap<>();

            if (!useOwnSB3Params) return params;

            params.put("n_steps", getNSteps());
            params.put("batch_size", getBatchSize());
            params.put("n_epochs", getNEpochs());
            params.put("gamma", getGamma());
            params.put("gae_lambda", getGaeLambda());
            params.put("clip_range", getClipRange());
            params.put("clip_range_vf", getClipRangeVf());
            params.put("normalize_advantage", getNormalizeAdvantage());
            params.put("ent_coef", getEntCoef());
            params.put("vf_coef", getVfCoef());
            params.put("max_grad_norm", getMaxGradNorm());
            params.put("target_kl", getTargetKl());

            return params;
        }

        public int getNSteps() {
            return Integer.parseInt(nStepsText.getText());
        }

        public int getBatchSize() {
            return Integer.parseInt(batchSizeText.getText());
        }

        public int getNEpochs() {
            return Integer.parseInt(nEpochsText.getText());
        }

        public double getGamma() {
            return Double.parseDouble(gammaText.getText());
        }

        public double getGaeLambda() {
            return Double.parseDouble(gaeLambdaText.getText());
        }

        public double getClipRange() {
            return Double.parseDouble(clipRangeText.getText());
        }

        public Double getClipRangeVf() {
            if (clipRangeVfText.getText() == null || clipRangeVfText.getText().equals("")) return null;
            return Double.parseDouble(clipRangeVfText.getText());
        }

        public boolean getNormalizeAdvantage() {
            return normalizeAdvantageCheckBox.isSelected();
        }

        public double getEntCoef() {
            return Double.parseDouble(entCoefText.getText());
        }

        public double getVfCoef() {
            return Double.parseDouble(vfCoefText.getText());
        }

        public double getMaxGradNorm() {
            return Double.parseDouble(maxGradNormText.getText());
        }

        public Double getTargetKl() {
            if (targetKlText.getText() == null ||targetKlText.getText().equals("")) return null;
            return Double.parseDouble(targetKlText.getText());
        }
    }


    public class NetworkParameters extends JPanel implements SB3Parameters {
        private JLabel standardNetworkParamsLabel;
        private JCheckBox standardNetworkParamsCheckBox;

        private JLabel layersLabel;
        private DefaultListModel<String> listModel;
        private JList layers;

        private JLabel addLayerLabel;
        private JTextField addLayerText;

        private JButton addLayerButton;
        private JButton removeLayerButton;

        private JLabel chooseActivationFunctionLabel;
        private JComboBox chooseActivationFunction;

        private boolean useOwnNetworkParameters;

        public NetworkParameters() {
            standardNetworkParamsLabel = new JLabel("Use Standard Network Parameters by SB3?");
            standardNetworkParamsCheckBox = new JCheckBox();
            standardNetworkParamsCheckBox.setSelected(SB3AgentConfig.DEFAULT_USE_STANDARD_NETWORK_PARAMS);
            standardNetworkParamsCheckBox.addItemListener(e -> SB3Params.this.networkParameters.setUseOwnParameters(
                    !standardNetworkParamsCheckBox.isSelected()
            ));

            layersLabel = new JLabel("Current Layers:");
            listModel = new DefaultListModel<>();
            for (int neurons: SB3AgentConfig.DEFAULT_LAYERS) {
                listModel.addElement(Integer.toString(neurons));
            }
            layers = new JList(listModel);

            addLayerLabel = new JLabel("Add layer with x neurons: ");
            addLayerText = new JTextField(Integer.toString(SB3AgentConfig.DEFAULT_NUM_NEURONS));


            addLayerButton = new JButton("Add");
            addLayerButton.addActionListener(e -> addLayer());

            removeLayerButton = new JButton("Remove");
            removeLayerButton.addActionListener(e -> removeLayer());

            chooseActivationFunctionLabel = new JLabel("Choose activation function:");
            chooseActivationFunction = new JComboBox(SB3AgentConfig.DEFAULT_ACTIVATION_FUNCTIONS);

            this.setLayout(new GridLayout(0,2,10,10));

            this.add(standardNetworkParamsLabel);
            this.add(standardNetworkParamsCheckBox);

            this.add(layersLabel);
            this.add(new JScrollPane(layers));

            this.add(addLayerLabel);
            this.add(addLayerText);

            this.add(addLayerButton);
            this.add(removeLayerButton);

            this.add(chooseActivationFunctionLabel);
            this.add(chooseActivationFunction);
        }

        private void addLayer() {
            int neurons = 64;
            try {
                neurons = Integer.parseInt(addLayerText.getText());
            } catch (Exception exception) {
                MessageBox.show(this, "Please provide vaild number of neurons (Integer).", "Wrong neuron input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            listModel.addElement(Integer.toString(neurons));
        }

        private void removeLayer() {
            int index = layers.getSelectedIndex();
            if (index >= 0) {
                listModel.remove(index);
            } else {
                MessageBox.show(this, "Please select a layer.", "select a layer", JOptionPane.WARNING_MESSAGE);
            }
        }

        public List<Integer> getLayers() {
            List<Integer> layers = new ArrayList<>();
            for (int i = 0; i < listModel.getSize(); i++) {
                layers.add(Integer.parseInt(listModel.get(i)));
            }
            return layers;
        }

        public String getActivationFunction() {
            if (chooseActivationFunction.getSelectedItem() instanceof String activationFunction) {
                return activationFunction;
            }
            return SB3AgentConfig.DEFAULT_ACTIVATION_FUNCTION;
        }

        public void setFrom(Map<String, Object> networkParameters) {
            chooseActivationFunction.setSelectedItem(networkParameters.get("activation_fn"));
            listModel.clear();
            if (networkParameters.containsKey("net_arch")) {
                for (int layer : (List<Integer>) networkParameters.get("net_arch")) {
                    listModel.addElement(Integer.toString(layer));
                }
            }
        }

        @Override
        public Map<String, Object> getParams() {
            Map<String, Object> networkParmas = new HashMap<>();

            if (!useOwnNetworkParameters) return networkParmas;

            networkParmas.put("activation_fn", getActivationFunction());
            networkParmas.put("net_arch", getLayers());

            return networkParmas;
        }

        @Override
        public void setUseOwnParameters(boolean enabled) {
            useOwnNetworkParameters = enabled;

            layersLabel.setEnabled(enabled);
            layers.setEnabled(enabled);
            addLayerLabel.setEnabled(enabled);
            addLayerText.setEnabled(enabled);
            addLayerButton.setEnabled(enabled);
            removeLayerButton.setEnabled(enabled);
            chooseActivationFunctionLabel.setEnabled(enabled);
            chooseActivationFunction.setEnabled(enabled);
        }
    }

    private class LoadOptions extends JPanel { // TODO remove
        private JLabel chosenAgentLabel;
        private JTextField  chosenAgentText;

        private JButton chooseAgent;
        private JButton loadAgent;

        public LoadOptions() {
            chosenAgentLabel = new JLabel("Chosen Agent: ");
            chosenAgentText = new JTextField("No agent selected");

            chooseAgent = new JButton("Choose Agent");
            chooseAgent.addActionListener(e -> chooseAgent());
            loadAgent = new JButton("Load Agent");
            loadAgent.addActionListener(e -> loadAgent());

            this.setLayout(new GridLayout(0,2,10,10));

            this.add(chosenAgentLabel);
            this.add(chosenAgentText);

            this.add(chooseAgent);
            this.add(loadAgent);
        }

        private void chooseAgent() {
            JFileChooser fileChooser = new JFileChooser();
            int returnVal = fileChooser.showOpenDialog(this);
            String filePath = null;
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                filePath = fileChooser.getSelectedFile().getName();
            }
            chosenAgentText.setText(filePath);
        }

        private void loadAgent() {

        }
    }

    private class EvaluationOptionsPanel extends JPanel {
        private JLabel evaluateEveryEpisodesLabel;
        private JTextField evaluateEveryEpisodesText;

        private JLabel numberOfGamesLabel;
        private JTextField numberOfGamesText;

        private JLabel opponentLabel;
        private JComboBox opponentComboBox;

        private JLabel saveBestLabel;
        private JCheckBox saveBestCheckBox;

        public EvaluationOptionsPanel() {
            evaluateEveryEpisodesLabel = new JLabel("Evaluate every X Episodes: ");
            evaluateEveryEpisodesText = new JTextField(Integer.toString(SB3AgentConfig.DefaultEvaluationOptions.DEFAULT_EVALUATE_EVERY_EPISODES));

            numberOfGamesLabel = new JLabel("Games per Evaluation: ");
            numberOfGamesText = new JTextField(Integer.toString(SB3AgentConfig.DefaultEvaluationOptions.DEFAULT_NUMBER_GAMES));

            opponentLabel = new JLabel("Choose opponent for evaluation: ");
            opponentComboBox = new JComboBox(SB3AgentConfig.DefaultEvaluationOptions.DEFAULT_OPPONENTS);

            saveBestLabel = new JLabel("Safe model after evaluation if better?");
            saveBestCheckBox = new JCheckBox();
            saveBestCheckBox.setSelected(SB3AgentConfig.DefaultEvaluationOptions.DEFAULT_SAFE_BEST_MODEL);

            this.setLayout(new GridLayout(0,2,10,10));

            this.add(evaluateEveryEpisodesLabel);
            this.add(evaluateEveryEpisodesText);

            this.add(numberOfGamesLabel);
            this.add(numberOfGamesText);

            this.add(opponentLabel);
            this.add(opponentComboBox);

            this.add(saveBestLabel);
            this.add(saveBestCheckBox);
        }

        public void setFrom(ParSB3.EvaluationOptions evaluationOptions) {
            evaluateEveryEpisodesText.setText(Integer.toString(evaluationOptions.getEvaluateEveryEpisodes()));
            numberOfGamesText.setText(Integer.toString(evaluationOptions.getNumberOfGames()));
            opponentComboBox.setSelectedItem(evaluationOptions.getOpponent());
            saveBestCheckBox.setSelected(evaluationOptions.isSaveBest());
        }

        public ParSB3.EvaluationOptions getEvaluationOptions() {
            return new ParSB3.EvaluationOptions(
                    Integer.parseInt(evaluateEveryEpisodesText.getText()),
                    Integer.parseInt(numberOfGamesText.getText()),
                    (String) opponentComboBox.getSelectedItem(),
                    saveBestCheckBox.isSelected()
            );
        }
    }
}
