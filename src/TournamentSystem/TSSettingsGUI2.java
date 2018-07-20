package TournamentSystem;

import controllers.*;
import controllers.MC.MCAgent;
import controllers.MC.MCAgentN;
import controllers.MCTS.MCTSAgentT;
import controllers.TD.TDAgent;
import controllers.TD.ntuple2.TDNTuple2Agt;
import games.Arena;
import games.ArenaTrain;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class TSSettingsGUI2 extends JFrame {
    private JCheckBox randomCheckBox;
    private JCheckBox minimaxCheckBox;
    private JCheckBox maxNCheckBox;
    private JCheckBox expectimaxNCheckBox;
    private JCheckBox MCNCheckBox;
    private JCheckBox MCTSCheckBox;
    private JCheckBox MCTSExpectimaxCheckBox;
    private JCheckBox TDNtuple2CheckBox;
    private JCheckBox TDSCheckBox;
    private JButton startButton;
    private JPanel mJPanel;
    private JTextField gameNumTextField;
    private JButton loadAgentFromDiskButton;
    private JCheckBox addNRandomMovesCheckBox;
    private JTextField numOfMovesTextField;
    private JPanel checkBoxJPanel;
    private JButton saveResultsToDiskButton;
    private JButton loadResultsFromDiskButton;
    private JButton reopenStatisticsButton;
    private JScrollPane checkBoxScrollPane;

    private Arena mArena;
    private TSAgentManager mTSAgentManager;
    private final String TAG = "[TSSettingsGUI2] ";

    public TSSettingsGUI2(Arena mArena) {
        super("TSSettingsGUI2");
        $$$setupUI$$$();
        setContentPane(mJPanel);
        //setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        this.mArena = mArena;
        mTSAgentManager = new TSAgentManager();

        mTSAgentManager.addAgent("StandardRandom", Types.GUI_AGENT_LIST[0], randomCheckBox, false, null);
        mTSAgentManager.addAgent("StandardMinimax", Types.GUI_AGENT_LIST[1], minimaxCheckBox, false, null);
        mTSAgentManager.addAgent("StandardMaxN", Types.GUI_AGENT_LIST[2], maxNCheckBox, false, null);
        mTSAgentManager.addAgent("StandardExpectimaxN", Types.GUI_AGENT_LIST[3], expectimaxNCheckBox, false, null);
        mTSAgentManager.addAgent("StandardMCN", Types.GUI_AGENT_LIST[4], MCNCheckBox, false, null);
        mTSAgentManager.addAgent("StandardMCTS", Types.GUI_AGENT_LIST[5], MCTSCheckBox, false, null);
        mTSAgentManager.addAgent("StandardMCTSExpectimax", Types.GUI_AGENT_LIST[6], MCTSExpectimaxCheckBox, false, null);
        // GUI_AGENT_LIST[7] ist der Human Player
        mTSAgentManager.addAgent("StandardTDNtuple2", Types.GUI_AGENT_LIST[8], TDNtuple2CheckBox, false, null);
        mTSAgentManager.addAgent("StandardTDS", Types.GUI_AGENT_LIST[9], TDSCheckBox, false, null);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JOptionPane.showMessageDialog(null, "Hello World");
                playPressed();
            }
        });

        loadAgentFromDiskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAgentFromDisk();
            }
        });
        reopenStatisticsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mTSAgentManager.isTournamentDone()) {
                    // tournament done - open stats window
                    mTSAgentManager.makeStats();
                }
                else {
                    // not done - cannot be opened
                }
            }
        });
    }

    private void loadAgentFromDisk() {
        Object[][] agentsAndFileNames;

        try {
            //playAgent = mArena.tdAgentIO.loadGBGAgent(null); // opens file dialog to locate single agent
            agentsAndFileNames = mArena.tdAgentIO.loadMultipleGBGAgent(); // opens file dialog to locate multiple agents
        } catch (IOException /*| ClassNotFoundException*/ e) {
            e.printStackTrace();
            return;
        }

        // todo transfer loaded agents to GameManager or store there in the first place
        // todo add logic to turnament gen functions to add agents
        // todo add logic to compete functions to let loaded agents play

        for (int i = 0; i < agentsAndFileNames.length; i++) {
            PlayAgent playAgent;
            Object agent = agentsAndFileNames[i][0];
            String agentName = (String) agentsAndFileNames[i][1];
            String agentType;

            if (agent instanceof TDAgent) {
                playAgent = (TDAgent) agent;
                agentType = "TDAgent";
            } else if (agent instanceof TDNTuple2Agt) {
                playAgent = (TDNTuple2Agt) agent;
                agentType = "TDNTuple2Agt";
            } else if (agent instanceof MCTSAgentT) {
                playAgent = (MCTSAgentT) agent;
                agentType = "MCTSAgentT";
            } else if (agent instanceof MCAgent) {
                playAgent = (MCAgent) agent;
                agentType = "MCAgent";
            } else if (agent instanceof MCAgentN) {
                playAgent = (MCAgentN) agent;
                agentType = "MCAgentN";
            } else if (agent instanceof MinimaxAgent) {
                playAgent = (MinimaxAgent) agent;
                agentType = "MinimaxAgent";
            } else if (agent instanceof MaxNAgent) {
                playAgent = (MaxNAgent) agent;
                agentType = "MaxNAgent";
            } else if (agent instanceof ExpectimaxNAgent) {
                playAgent = (ExpectimaxNAgent) agent;
                agentType = "ExpectimaxNAgent";
            } else if (agent instanceof RandomAgent) {
                playAgent = (RandomAgent) agent;
                agentType = "RandomAgent";
            } else {
                //playAgent = null;
                //agentType = "NaN";
                System.out.println(TAG + "ERROR :: Unknown Agent Class");
                break;
            }

            System.out.println(TAG + "INFO :: loading from Disk successful for agent: " + agentName + " with AgentState: " + playAgent.getAgentState() + " and type: " + agentType);

            // add agent to gui
            JCheckBox newAgent = new JCheckBox("HDD " + agentName);
            checkBoxJPanel.add(newAgent);
            //checkBoxScrollPane.add(newAgent);

            // add to mTSAgentManager
            mTSAgentManager.addAgent("HDD " + agentName, "HDD " + agentType, newAgent, true, playAgent);
        } // for

        System.out.println(TAG + "Number of disk agents loaded into TS: " + mTSAgentManager.getNumDiskAgents());
        checkBoxJPanel.revalidate();
        mJPanel.revalidate();
        mJPanel.repaint();
        pack(); // resize window
    }

    private void playPressed() {
        int countSelectedAgents = 0;
        int numGamesPerMatch;
        try {
            numGamesPerMatch = Integer.parseInt(gameNumTextField.getText());
        } catch (NumberFormatException n) {
            n.printStackTrace();
            System.out.println(TAG + "ERROR :: not a valid number was entered in GameNum Textfield. Using value 1");
            numGamesPerMatch = 1;
        }
        System.out.println(TAG + "numGamesPerMatch: " + numGamesPerMatch);
        mTSAgentManager.setNumberOfGames(numGamesPerMatch);

        System.out.println(TAG + "Startbutton clicked | checkbox states:");
        // durch alle checkboxen der agenten iterieren
        for (TSAgent agent : mTSAgentManager.mAgents) {
            //System.out.println(agent.getAgentType() +" == "+ agent.getName());
            // pruefen fuer jede checkbox, ob sie selected ist oder nicht
            if (agent.guiCheckBox.isSelected()) {
                System.out.println(TAG + agent.guiCheckBox.getText() + ": selected");
                countSelectedAgents++;
            } else {
                System.out.println(TAG + agent.guiCheckBox.getText() + ": deselected");
            }
        }

        if (countSelectedAgents < 2) {
            System.out.println(TAG + "Error :: At least 2 Agents need to be selected for a tournament!");
        } else {
            mTSAgentManager.printGamePlan();

            if (mArena.taskState != ArenaTrain.Task.IDLE) {
                System.out.println(TAG + "ERROR :: could not start Tourmenent, Arena is not IDLE");
            } else {
                // hand over data
                mArena.tournamentAgentManager = mTSAgentManager;

                mArena.taskState = ArenaTrain.Task.TRNEMNT;
            }
        }
    }

    /**
     * For Testing Only
     */
    //public static void main(String[] args) { new TSSettingsGUI2(null); }
    private void createUIComponents() {
        // TODO: place custom component creation code here
        checkBoxJPanel = new JPanel();
        checkBoxJPanel.setLayout(new BoxLayout(checkBoxJPanel, BoxLayout.Y_AXIS));
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mJPanel = new JPanel();
        mJPanel.setLayout(new GridBagLayout());
        final JLabel label1 = new JLabel();
        label1.setText("Select Standard Agents to play:");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label1, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 50);
        mJPanel.add(spacer1, gbc);
        randomCheckBox = new JCheckBox();
        randomCheckBox.setText("Random");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(randomCheckBox, gbc);
        minimaxCheckBox = new JCheckBox();
        minimaxCheckBox.setText("Minimax");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(minimaxCheckBox, gbc);
        maxNCheckBox = new JCheckBox();
        maxNCheckBox.setText("Max-N");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(maxNCheckBox, gbc);
        expectimaxNCheckBox = new JCheckBox();
        expectimaxNCheckBox.setText("Expectimax-N");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(expectimaxNCheckBox, gbc);
        MCNCheckBox = new JCheckBox();
        MCNCheckBox.setText("MC-N");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(MCNCheckBox, gbc);
        MCTSCheckBox = new JCheckBox();
        MCTSCheckBox.setText("MCTS");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(MCTSCheckBox, gbc);
        MCTSExpectimaxCheckBox = new JCheckBox();
        MCTSExpectimaxCheckBox.setText("MCTS Expectimax");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(MCTSExpectimaxCheckBox, gbc);
        TDNtuple2CheckBox = new JCheckBox();
        TDNtuple2CheckBox.setText("TD-Ntuple-2");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(TDNtuple2CheckBox, gbc);
        TDSCheckBox = new JCheckBox();
        TDSCheckBox.setText("TDS");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(TDSCheckBox, gbc);
        startButton = new JButton();
        startButton.setText("Start");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 22;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(startButton, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Games per Match");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 15;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label2, gbc);
        gameNumTextField = new JTextField();
        gameNumTextField.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 16;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(gameNumTextField, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 50, 0, 0);
        mJPanel.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(10, 0, 0, 0);
        mJPanel.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 28;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mJPanel.add(spacer4, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 11;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mJPanel.add(spacer5, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 17;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mJPanel.add(spacer6, gbc);
        loadAgentFromDiskButton = new JButton();
        loadAgentFromDiskButton.setText("Load Agent from Disk");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 12;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(loadAgentFromDiskButton, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 14;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mJPanel.add(spacer7, gbc);
        addNRandomMovesCheckBox = new JCheckBox();
        addNRandomMovesCheckBox.setText("Add n random moves at start");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 18;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(addNRandomMovesCheckBox, gbc);
        numOfMovesTextField = new JTextField();
        numOfMovesTextField.setText("2");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 20;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(numOfMovesTextField, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Number of moves");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 19;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label3, gbc);
        final JPanel spacer8 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 21;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mJPanel.add(spacer8, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 13;
        gbc.fill = GridBagConstraints.BOTH;
        mJPanel.add(checkBoxJPanel, gbc);
        saveResultsToDiskButton = new JButton();
        saveResultsToDiskButton.setText("Save Results to Disk");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 26;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(saveResultsToDiskButton, gbc);
        final JPanel spacer9 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 25;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        mJPanel.add(spacer9, gbc);
        loadResultsFromDiskButton = new JButton();
        loadResultsFromDiskButton.setText("Load Results from Disk");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 27;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(loadResultsFromDiskButton, gbc);
        reopenStatisticsButton = new JButton();
        reopenStatisticsButton.setText("Reopen Statistics");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 24;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(reopenStatisticsButton, gbc);
        final JPanel spacer10 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 23;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        mJPanel.add(spacer10, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mJPanel;
    }
}
