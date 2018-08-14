package TournamentSystem;

import controllers.*;
import games.Arena;
import games.ArenaTrain;
import games.XArenaMenu;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * This class is the entrypoint to the tournament system and provides a GUI to configure and start a tournament.
 * It gets called by {@link XArenaMenu#generateTournamentMenu()}. The {@link TSAgentManager} is instantiated
 * and fed with data here.
 * <p>
 * This GUI was build with the IntelliJ GUI Designer.
 *
 * @author Felix Barsnick, University of Applied Sciences Cologne, 2018
 */
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
    private JButton selectAllHDDAgentsButton;
    private JButton unselectAllHDDAgentsButton;
    private JButton deleteSelectedHDDAgentsButton;
    private JCheckBox autoSaveAfterTSFinishedCheckBox;
    private JScrollPane checkBoxScrollPane;

    private Arena mArena;
    private TSAgentManager mTSAgentManager;
    private final String TAG = "[TSSettingsGUI2] ";
    final int numPlayers;

    public TSSettingsGUI2(Arena mArena) {
        super("TSSettingsGUI2");
        $$$setupUI$$$();
        JScrollPane scroll = new JScrollPane(mJPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //setContentPane(mJPanel);
        setContentPane(scroll);
        pack();
        setVisible(true);

        this.mArena = mArena;
        numPlayers = mArena.getGameBoard().getStateObs().getNumPlayers();
        mTSAgentManager = new TSAgentManager(numPlayers);

        mTSAgentManager.addAgent("StandardRandom", Types.GUI_AGENT_LIST[0], randomCheckBox, false, null);
        mTSAgentManager.addAgent("StandardMinimax", Types.GUI_AGENT_LIST[1], minimaxCheckBox, false, null);
        mTSAgentManager.addAgent("StandardMCN", Types.GUI_AGENT_LIST[4], MCNCheckBox, false, null);
        // GUI_AGENT_LIST[7] ist der Human Player
        mTSAgentManager.addAgent("StandardTDNtuple2", Types.GUI_AGENT_LIST[8], TDNtuple2CheckBox, false, null);
        TDNtuple2CheckBox.setVisible(false);
        mTSAgentManager.addAgent("StandardTDS", Types.GUI_AGENT_LIST[9], TDSCheckBox, false, null);
        TDSCheckBox.setVisible(false);


        if (mArena.getGameBoard().getDefaultStartState().isDeterministicGame()) {
            //System.out.println(TAG+"game is deterministic");
            mTSAgentManager.addAgent("StandardMaxN", Types.GUI_AGENT_LIST[2], maxNCheckBox, false, null);
            mTSAgentManager.addAgent("StandardMCTS", Types.GUI_AGENT_LIST[5], MCTSCheckBox, false, null);
            expectimaxNCheckBox.setVisible(false);
            MCTSExpectimaxCheckBox.setVisible(false);
        } else {
            //System.out.println(TAG+"game is not deterministic");
            mTSAgentManager.addAgent("StandardExpectimaxN", Types.GUI_AGENT_LIST[3], expectimaxNCheckBox, false, null);
            mTSAgentManager.addAgent("StandardMCTSExpectimax", Types.GUI_AGENT_LIST[6], MCTSExpectimaxCheckBox, false, null);
            maxNCheckBox.setVisible(false);
            MCTSCheckBox.setVisible(false);
        }

        // enable en/disabling of textfield/settings checkboxen later on in arena
        mTSAgentManager.gameNumJTF = gameNumTextField;
        mTSAgentManager.numOfMovesJTF = numOfMovesTextField;
        mTSAgentManager.nRandomJCB = addNRandomMovesCheckBox;
        mTSAgentManager.autoSaveAfterTSJCB = autoSaveAfterTSFinishedCheckBox;

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
                } else {
                    // not done - cannot be opened
                    System.out.println(TAG + "ERROR :: Tournament is not done, cannot reopen stats");
                    JOptionPane.showMessageDialog(null, "ERROR: Tournament is not done, cannot reopen stats");
                }
            }
        });
        saveResultsToDiskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mArena.taskState != ArenaTrain.Task.IDLE) {
                    System.out.println(TAG + "ERROR :: ARENA is not IDLE, cannot save tournament, aborting");
                    JOptionPane.showMessageDialog(null, "ERROR: Arena not IDLE");
                    return;
                }
                if (mTSAgentManager.results.tournamentDone) {
                    String str;
                    try {
                        mArena.tdAgentIO.saveTSResult(mTSAgentManager.results);
                        str = "Saved Tournament!";
                    } catch (IOException ioe) {
                        str = ioe.getMessage();
                    }
                    System.out.println(TAG + "[SaveTournament] " + str);
                } else {
                    JOptionPane.showMessageDialog(null, "ERROR: Tournament not done!");
                }
            }
        });
        loadResultsFromDiskButton.setVisible(false); // hide button for now to move it to TS Menu
        loadResultsFromDiskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mArena.taskState != ArenaTrain.Task.IDLE) {
                    System.out.println(TAG + "ERROR :: ARENA is not IDLE, cannot load tournament, aborting");
                    return;
                }
                JOptionPane.showMessageDialog(null, "Function not yet implemented");
            }
        });

        pack(); // resize window to adapt to all hidden elements

        selectAllHDDAgentsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mTSAgentManager.setAllHDDAgentsSelected(true);
            }
        });
        unselectAllHDDAgentsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mTSAgentManager.setAllHDDAgentsSelected(false);
            }
        });
        deleteSelectedHDDAgentsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mTSAgentManager.deleteAllHDDAgentsSelected();
                pack();
            }
        });
    }

    /**
     * loads previously saved agents from disk into the tournament system
     */
    private void loadAgentFromDisk() {
        if (mArena.taskState != ArenaTrain.Task.IDLE) {
            System.out.println(TAG + "ERROR :: ARENA is not IDLE, dont try to change data while its working, aborting");
            return;
        }

        TSDiskAgentDataTransfer agentsAndFileNames;

        try {
            //playAgent = mArena.tdAgentIO.loadGBGAgent(null); // opens file dialog to locate single agent
            agentsAndFileNames = mArena.tdAgentIO.loadMultipleGBGAgent(); // opens file dialog to locate multiple agents
        } catch (IOException /*| ClassNotFoundException*/ e) {
            e.printStackTrace();
            return;
        }

        if (agentsAndFileNames == null) // avoids crash when filedialog is closed with no file chosen
            return;

        for (int i = 0; i < agentsAndFileNames.getSize(); i++) {
            PlayAgent playAgent = agentsAndFileNames.getPlayAgent(i);
            String agentName = agentsAndFileNames.getFileName(i);
            String agentType = agentsAndFileNames.getPlayAgentType(i);

            /*
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
            */

            System.out.println(TAG + "INFO :: loading from Disk successful for agent: " + agentName + " with AgentState: " + playAgent.getAgentState() + " and type: " + agentType);

            // add agent to gui
            JCheckBox newAgent = new JCheckBox("HDD " + agentName);
            newAgent.setSelected(true); // set checkbox of new agent to selected
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

    /**
     * Prepares the set data for the tournament and starts it in {@link Arena} by changing the TastState
     */
    private void playPressed() {
        if (mArena.taskState != ArenaTrain.Task.IDLE) {
            System.out.println(TAG + "ERROR :: ARENA is not IDLE, cannot start tournament, aborting");
            return;
        }

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

        if (addNRandomMovesCheckBox.isSelected()) {
            try {
                mTSAgentManager.setNumberOfRandomStartMoves(Integer.parseInt(numOfMovesTextField.getText()));
            } catch (NumberFormatException n) {
                n.printStackTrace();
                System.out.println(TAG + "ERROR :: not a valid number was entered in NRandomStartMoves Textfield. Using value 0");
                mTSAgentManager.setNumberOfRandomStartMoves(0);
            }
        } else {
            mTSAgentManager.setNumberOfRandomStartMoves(0);
        }

        mTSAgentManager.setAutoSaveAfterTS(autoSaveAfterTSFinishedCheckBox.isSelected());
        mTSAgentManager.setResultsStartDate();

        System.out.println(TAG + "Startbutton clicked | checkbox states:");
        // durch alle checkboxen der agenten iterieren
        for (TSAgent agent : mTSAgentManager.results.mAgents) {
            //System.out.println(agent.getAgentType() +" == "+ agent.getName());
            // pruefen fuer jede checkbox, ob sie selected ist oder nicht
            if (agent.guiCheckBox.isSelected()) {
                System.out.println(TAG + agent.guiCheckBox.getText() + ": selected");
                countSelectedAgents++;
            } else {
                System.out.println(TAG + agent.guiCheckBox.getText() + ": deselected");
            }
        }


        if (countSelectedAgents < 2 && numPlayers == 2) {
            System.out.println(TAG + "Error :: At least 2 Agents need to be selected for a tournament!");
            JOptionPane.showMessageDialog(null, "ERROR: not enough agents were selected to play");
            return;
        }
        if (countSelectedAgents < 1 && numPlayers == 1) {
            System.out.println(TAG + "Error :: At least 1 Agent need to be selected for a tournament!");
            JOptionPane.showMessageDialog(null, "ERROR: not enough agents were selected to play");
            return;
        }
        mTSAgentManager.printGamePlan();

        if (mArena.taskState != ArenaTrain.Task.IDLE) {
            System.out.println(TAG + "ERROR :: could not start Tourmenent, Arena is not IDLE");
        } else {
            // hand over data
            mArena.tournamentAgentManager = mTSAgentManager;

            if (numPlayers > 1)
                mArena.taskState = ArenaTrain.Task.TRNEMNT;
            else {
                // single player TS...
                mTSAgentManager.runSinglePlayerTournament(mArena);
            }
        }
    }

    /**
     * automatically generated by IntelliJ GUI Designer
     */
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
        startButton.setForeground(new Color(-16732416));
        startButton.setText("Start");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 32;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(startButton, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Games per Match");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 23;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label2, gbc);
        gameNumTextField = new JTextField();
        gameNumTextField.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 24;
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
        gbc.gridy = 39;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mJPanel.add(spacer4, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 25;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mJPanel.add(spacer5, gbc);
        loadAgentFromDiskButton = new JButton();
        loadAgentFromDiskButton.setText("Load Agent from Disk");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 14;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(loadAgentFromDiskButton, gbc);
        addNRandomMovesCheckBox = new JCheckBox();
        addNRandomMovesCheckBox.setText("Add n random moves at start");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 26;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(addNRandomMovesCheckBox, gbc);
        numOfMovesTextField = new JTextField();
        numOfMovesTextField.setText("2");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 28;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(numOfMovesTextField, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Number of moves:");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 27;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label3, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 29;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mJPanel.add(spacer6, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 15;
        gbc.fill = GridBagConstraints.BOTH;
        mJPanel.add(checkBoxJPanel, gbc);
        saveResultsToDiskButton = new JButton();
        saveResultsToDiskButton.setText("Save Results to Disk");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 37;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(saveResultsToDiskButton, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 36;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 1, 0);
        mJPanel.add(spacer7, gbc);
        loadResultsFromDiskButton = new JButton();
        loadResultsFromDiskButton.setText("Load Results from Disk");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 38;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(loadResultsFromDiskButton, gbc);
        reopenStatisticsButton = new JButton();
        reopenStatisticsButton.setText("Reopen Statistics");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 35;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(reopenStatisticsButton, gbc);
        final JPanel spacer8 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 33;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        mJPanel.add(spacer8, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Choose Standard OR Disk Agents");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 12;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label4, gbc);
        final JPanel spacer9 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 11;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        mJPanel.add(spacer9, gbc);
        final JPanel spacer10 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 13;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        mJPanel.add(spacer10, gbc);
        final JPanel spacer11 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 22;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mJPanel.add(spacer11, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("After the Tournament:");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 34;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label5, gbc);
        selectAllHDDAgentsButton = new JButton();
        selectAllHDDAgentsButton.setText("select All HDD Agents");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 17;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(selectAllHDDAgentsButton, gbc);
        unselectAllHDDAgentsButton = new JButton();
        unselectAllHDDAgentsButton.setText("unselect All HDD Agents");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 19;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(unselectAllHDDAgentsButton, gbc);
        deleteSelectedHDDAgentsButton = new JButton();
        deleteSelectedHDDAgentsButton.setForeground(new Color(-65536));
        deleteSelectedHDDAgentsButton.setText("delete selected HDD Agents");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 21;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(deleteSelectedHDDAgentsButton, gbc);
        final JPanel spacer12 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 16;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        mJPanel.add(spacer12, gbc);
        final JPanel spacer13 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 18;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 1, 0);
        mJPanel.add(spacer13, gbc);
        final JPanel spacer14 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 20;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 1, 0);
        mJPanel.add(spacer14, gbc);
        autoSaveAfterTSFinishedCheckBox = new JCheckBox();
        autoSaveAfterTSFinishedCheckBox.setText("AutoSave after TS finished");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 30;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(autoSaveAfterTSFinishedCheckBox, gbc);
        final JPanel spacer15 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 31;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mJPanel.add(spacer15, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mJPanel;
    }
}
