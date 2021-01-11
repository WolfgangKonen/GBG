package TournamentSystem;

import TournamentSystem.tools.TSDiskAgentDataTransfer;
import controllers.*;
import games.Arena;
import games.ArenaTrain;
import games.XArenaMenu;
import tools.Types;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * This class is the entry point to the tournament system and provides a GUI to configure and start 
 * a tournament. It gets called by {@link XArenaMenu#generateTournamentMenu()}. 
 * The {@link TSAgentManager} is instantiated and fed with data here.
 * <p>
 * This GUI was build with the IntelliJ GUI Designer.
 *
 * @author Felix Barsnick, Cologne University of Applied Sciences, 2018
 */
public class TSSettingsGUI2 extends JFrame {
    private JCheckBox randomCheckBox;
//    private JCheckBox minimaxCheckBox;
    private JCheckBox maxNCheckBox;
    private JCheckBox expectimaxNCheckBox;
    private JCheckBox MCNCheckBox;
    private JCheckBox MCTSCheckBox;
    private JCheckBox MCTSExpectimaxCheckBox;
    private JCheckBox TDNtuple2CheckBox;
    private JCheckBox TDSCheckBox;
    private JButton startButton;
    private JPanel mJPanel;
    private JTextField episodeNumTextField;
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
    private JRadioButton singleRoundRobinRadioButton;
    private JRadioButton doubleRoundRobinRadioButton;
    private JRadioButton variableDoubleRoundRobinRadioButton;
    private JSlider variableDRoundRobinSlider;
    private JLabel variableDRRInfoLabel;
    private JScrollPane checkBoxScrollPane;

    private Arena mArena;
    private TSAgentManager mTSAgentManager;
    private final String TAG = "[TSSettingsGUI2] ";
    final int numPlayers;

    public TSSettingsGUI2(Arena mArena) {
        super("Tournament System Launcher");   //("TSSettingsGUI2");
        $$$setupUI$$$();
        JScrollPane scroll = new JScrollPane(mJPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //setContentPane(mJPanel);
        setContentPane(scroll);
		pack();

        setVisible(true);

        this.mArena = mArena;
        numPlayers = mArena.getGameBoard().getStateObs().getNumPlayers();
        mTSAgentManager = new TSAgentManager(numPlayers);

        randomCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (mTSAgentManager.getNumAgentsSelected() > 1 && variableDoubleRoundRobinRadioButton.isSelected()) {
                    updateSliderAndLabel();
                }
            }
        });
        
        // /WK/04/2019 BugFix: in the following we use the explicit strings, e.g. "Random", "MCTS"
        // from Types.GUI_AGENT_LIST and NOT GUI_AGENT_LIST[0] or similar, because GUI_AGENT_LIST 
        // may change in the future and then the element GUI_AGENT_LIST[i] is no longer the same.
        //
        mTSAgentManager.addAgent("Standard Random", "Random"/*Types.GUI_AGENT_LIST[0]*/, randomCheckBox, false, null);
        
        // --- deprecated, since MinimaxAgent has become deprecated now ---
//        minimaxCheckBox.addChangeListener(new ChangeListener() {
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                if (mTSAgentManager.getNumAgentsSelected() > 1 && variableDoubleRoundRobinRadioButton.isSelected()) {
//                    updateSliderAndLabel();
//                }
//            }
//        });
//      mTSAgentManager.addAgent("Standard Minimax", "Minimax"/*Types.GUI_AGENT_LIST[1]*/, minimaxCheckBox, false, null);
//      minimaxCheckBox.setVisible(false);

        MCNCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (mTSAgentManager.getNumAgentsSelected() > 1 && variableDoubleRoundRobinRadioButton.isSelected()) {
                    updateSliderAndLabel();
                }
            }
        });
        mTSAgentManager.addAgent("Standard MCN", "MC-N"/*Types.GUI_AGENT_LIST[4]*/, MCNCheckBox, false, null);
        // GUI_AGENT_LIST[6] is Human player
        mTSAgentManager.addAgent("Standard TDNtuple2",  "TD-Ntuple-2"/*Types.GUI_AGENT_LIST[8]*/, TDNtuple2CheckBox, false, null);
        TDNtuple2CheckBox.setVisible(false);
        mTSAgentManager.addAgent("Standard TDS", "TDS"/*Types.GUI_AGENT_LIST[9]*/, TDSCheckBox, false, null);
        TDSCheckBox.setVisible(false);

        if (mArena.getGameBoard().getDefaultStartState().isDeterministicGame()) {
            //System.out.println(TAG+"game is deterministic");
            maxNCheckBox.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (mTSAgentManager.getNumAgentsSelected() > 1 && variableDoubleRoundRobinRadioButton.isSelected()) {
                        updateSliderAndLabel();
                    }
                }
            });
            mTSAgentManager.addAgent("Standard MaxN", "Max-N"/*Types.GUI_AGENT_LIST[2]*/, maxNCheckBox, false, null);
            MCTSCheckBox.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (mTSAgentManager.getNumAgentsSelected() > 1 && variableDoubleRoundRobinRadioButton.isSelected()) {
                        updateSliderAndLabel();
                    }
                }
            });
            mTSAgentManager.addAgent("Standard MCTS", "MCTS"/*Types.GUI_AGENT_LIST[4]*/, MCTSCheckBox, false, null);
            expectimaxNCheckBox.setVisible(false);
            MCTSExpectimaxCheckBox.setVisible(false);
        } else {
            //System.out.println(TAG+"game is not deterministic");
            expectimaxNCheckBox.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (mTSAgentManager.getNumAgentsSelected() > 1 && variableDoubleRoundRobinRadioButton.isSelected()) {
                        updateSliderAndLabel();
                    }
                }
            });
            mTSAgentManager.addAgent("Standard ExpectimaxN", "Expectimax-N"/*Types.GUI_AGENT_LIST[3]*/, expectimaxNCheckBox, false, null);
            MCTSExpectimaxCheckBox.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (mTSAgentManager.getNumAgentsSelected() > 1 && variableDoubleRoundRobinRadioButton.isSelected()) {
                        updateSliderAndLabel();
                    }
                }
            });
            mTSAgentManager.addAgent("Standard MCTSExpectimax", "MCTS Expectimax"/*Types.GUI_AGENT_LIST[6]*/, MCTSExpectimaxCheckBox, false, null);
            maxNCheckBox.setVisible(false);
            MCTSCheckBox.setVisible(false);
        }

        // /WK/ Status 2020-08-14: 
        // We disable all Standard Agents. It is simpler and it is more safe: Save every agent to be used in TS 
        // to disk, then load it as Disk Agent. Because then we know precisely which agent with which parameter 
        // settings is meant.
        randomCheckBox.setVisible(false);
        MCNCheckBox.setVisible(false);
        MCTSCheckBox.setVisible(false);
        maxNCheckBox.setVisible(false);
        expectimaxNCheckBox.setVisible(false);
        MCTSExpectimaxCheckBox.setVisible(false);

        if (numPlayers == 1) {
            singleRoundRobinRadioButton.setEnabled(false);
            doubleRoundRobinRadioButton.setEnabled(false);
            variableDoubleRoundRobinRadioButton.setEnabled(false);
        }
        variableDRRInfoLabel.setVisible(false);
        updateSliderAndLabel();

        // enable en/disabling of textfield/settings checkboxen later on in arena
        mTSAgentManager.gameNumJTF = episodeNumTextField;
        mTSAgentManager.numOfMovesJTF = numOfMovesTextField;
        mTSAgentManager.nRandomJCB = addNRandomMovesCheckBox;
        mTSAgentManager.autoSaveAfterTSJCB = autoSaveAfterTSFinishedCheckBox;
        mTSAgentManager.singleRR = singleRoundRobinRadioButton;
        mTSAgentManager.doubleRR = doubleRoundRobinRadioButton;

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
                loadAgentsFromDisk();
                if (mTSAgentManager.getNumAgentsSelected() > 1 && variableDoubleRoundRobinRadioButton.isSelected()) {
                    updateSliderAndLabel();
                }
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
        selectAllHDDAgentsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mTSAgentManager.setAllHDDAgentsSelected(true);
                updateSliderAndLabel();
            }
        });
        unselectAllHDDAgentsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mTSAgentManager.setAllHDDAgentsSelected(false);
                updateSliderAndLabel();
            }
        });
        deleteSelectedHDDAgentsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mTSAgentManager.deleteAllHDDAgentsSelected();
                updateSliderAndLabel();
                pack(); 					// resize window ...
                adjustComponentHeight();	// ... but constrained by 95% screen height	
            }
        });
        variableDRoundRobinSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                //System.out.println("min " + variableDRoundRobinSlider.getMinimum() + " max " + variableDRoundRobinSlider.getMaximum() + " val " + variableDRoundRobinSlider.getValue());
                //variableDRRInfoLabel.setText(variableDRoundRobinSlider.getValue() + " / " + variableDRoundRobinSlider.getMaximum() + " matches will be played");
                updateSliderAndLabel();
            }
        });
        variableDoubleRoundRobinRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                //System.out.println(variableDoubleRoundRobinRadioButton.isSelected());
                if (mTSAgentManager.getNumAgentsSelected() > numPlayers)
                    updateSliderAndLabel();
                variableDRRInfoLabel.setVisible(variableDoubleRoundRobinRadioButton.isSelected()); //todo not very efficient code, called too often
            }
        });

        pack(); // resize window to adapt to all hidden elements
        this.adjustComponentHeight();

    } // TSSettingsGUI2

    private void adjustComponentHeight() {
        // here we set the component height to not more than 95% of screen height --> 
        // this lets the component not stretch over the Windows task bar
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int height = (int)(screenSize.getHeight()*0.95);
		height = (height>this.getHeight()) ? this.getHeight() : height;
		setSize(this.getWidth(), height);
		setBounds(0,0,this.getWidth(), height);
    }
    
    /**
     * update the variable round robin slider limits to the current amount of selected agents in {@link TSSettingsGUI2}.
     * Update will just take place when the upper or lower limit actually changed.
     */
    private void updateSliderAndLabel() {
        int matches;
        int countSelectedAgents = mTSAgentManager.getNumAgentsSelected();
        matches = countSelectedAgents * (countSelectedAgents - 1);
        if (mTSAgentManager.getNumAgentsSelected() > 0) {
            int newMinimum = (countSelectedAgents / 2) + 1;
            if (newMinimum != variableDRoundRobinSlider.getMinimum()) { // avoid accessing the slider too often
                variableDRoundRobinSlider.setMinimum(newMinimum); // just change when the values really changed
            }
        } else {
            if (variableDRoundRobinSlider.getMinimum() != 0) {
                variableDRoundRobinSlider.setMinimum(0);
            }
        }
        if (variableDRoundRobinSlider.getMaximum() != matches) {
            variableDRoundRobinSlider.setMaximum(matches);
        }
        variableDRRInfoLabel.setText(variableDRoundRobinSlider.getValue() + " / " + variableDRoundRobinSlider.getMaximum() + " matches will be played");
    }

    /**
     * loads previously saved agents from disk into the tournament system
     */
    private void loadAgentsFromDisk() {
        if (mArena.taskState != ArenaTrain.Task.IDLE) {
            System.out.println(TAG + "ERROR :: ARENA is not IDLE, dont try to change data while its working, aborting");
            return;
        }

        TSDiskAgentDataTransfer agentsAndFileNames;

        try {
            //playAgent = mArena.tdAgentIO.loadGBGAgent(null); // opens file dialog to locate single agent
            agentsAndFileNames = mArena.tdAgentIO.loadMultipleGBGAgent(); // opens file dialog to locate multiple agents
        } catch (IOException e) {
            System.out.println(TAG + "ERROR :: No agent loaded from disk");
            //e.printStackTrace();
            return;
        } catch (Exception e) {
            System.out.println(TAG + "ERROR :: No agent loaded from disk");
            //e.printStackTrace();
            return;
        }

        if (agentsAndFileNames == null) // avoids crash when file dialog is closed with no file chosen
            return;

        for (int i = 0; i < agentsAndFileNames.getSize(); i++) {
            PlayAgent playAgent = agentsAndFileNames.getPlayAgent(i);
            String agentName = agentsAndFileNames.getFileName(i);
            String agentType = agentsAndFileNames.getPlayAgentType(i);

            System.out.println(TAG + "INFO :: loading from Disk successful for agent: " + agentName + " with AgentState: " + playAgent.getAgentState() + " and type: " + agentType);

            // add agent to gui
            String hddPrefix = "";  // was before: "HDD ", but we do not need this anymore, because we have no other agents than HDD
            JCheckBox newAgent = new JCheckBox(hddPrefix + agentName);
            newAgent.setSelected(true); // set checkbox of new agent to selected
            newAgent.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (mTSAgentManager.getNumAgentsSelected() > 1 && variableDoubleRoundRobinRadioButton.isSelected()) {
                        updateSliderAndLabel();
                    }
                }
            });
            checkBoxJPanel.add(newAgent);
            //checkBoxScrollPane.add(newAgent);

            // add to mTSAgentManager
            mTSAgentManager.addAgent(hddPrefix + agentName, hddPrefix + agentType, newAgent, true, playAgent);
        } // for

        System.out.println(TAG + "Number of disk agents loaded into TS: " + mTSAgentManager.getNumDiskAgents());
        checkBoxJPanel.revalidate();
        mJPanel.revalidate();
        mJPanel.repaint();
        pack(); 					// resize window ...
        adjustComponentHeight();	// ... but constrained by 95% screen height	
    }

    /**
     * Prepares the set data for the tournament and starts it in {@link Arena} by changing the TastState
     */
    private void playPressed() {
        if (mArena.taskState != ArenaTrain.Task.IDLE) {
            System.out.println(TAG + "ERROR :: ARENA is not IDLE, cannot start tournament, aborting");
            return;
        }

        // get and set number of Episodes per match
        int numEpisodesPerMatch;
        try {
            numEpisodesPerMatch = Integer.parseInt(episodeNumTextField.getText());
        } catch (NumberFormatException n) {
            n.printStackTrace();
            System.out.println(TAG + "ERROR :: not a valid number was entered in EpisodeNum Textfield. Using value 1");
            numEpisodesPerMatch = 1;
        }
        if (numEpisodesPerMatch < 1) {
            System.out.println(TAG + "ERROR :: not a valid number was entered in EpisodeNum Textfield, must be >=1. Using value 1");
            numEpisodesPerMatch = 1;
        }
        System.out.println(TAG + "numEpisodesPerMatch: " + numEpisodesPerMatch);
        mTSAgentManager.setNumberOfEpisodes(numEpisodesPerMatch);

        // get and set if random start moves are set
        if (addNRandomMovesCheckBox.isSelected()) {
            int randomStartMoves;
            try {
                randomStartMoves = Integer.parseInt(numOfMovesTextField.getText());
                if (randomStartMoves < 1) {
                    System.out.println(TAG + "ERROR :: not a valid number was entered in RandomStartMoves Textfield, must be >=0. Using value 0");
                    randomStartMoves = 0;
                }
                mTSAgentManager.setNumberOfRandomStartMoves(randomStartMoves);
            } catch (NumberFormatException n) {
                n.printStackTrace();
                System.out.println(TAG + "ERROR :: not a valid number was entered in NRandomStartMoves Textfield. Using value 0");
                mTSAgentManager.setNumberOfRandomStartMoves(0);
            }
        } else {
            mTSAgentManager.setNumberOfRandomStartMoves(0);
        }

        // set tournament mode
        if (singleRoundRobinRadioButton.isSelected()) {
            mTSAgentManager.setTournamentMode(0, -1);
        }
        if (doubleRoundRobinRadioButton.isSelected()) {
            mTSAgentManager.setTournamentMode(1, -1);
        }
        if (variableDoubleRoundRobinRadioButton.isSelected()) {
            mTSAgentManager.setTournamentMode(2, variableDRoundRobinSlider.getValue());
        }

        // set auto save of TS result aber finish
        mTSAgentManager.setAutoSaveAfterTS(autoSaveAfterTSFinishedCheckBox.isSelected());
        // save date and time for the result window
        mTSAgentManager.setResultsStartDate();

        int countSelectedAgents = 0;
        System.out.println(TAG + "Startbutton clicked | checkbox states:");
        // iterate through all agent check-boxes durch alle checkboxen der agenten iterieren
        for (TSAgent agent : mTSAgentManager.results.mAgents) {
            //System.out.println(agent.getAgentType() +" == "+ agent.getName());
            // look for each check-box whether it is selected or not
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
            if (numPlayers > 1) {
                // hand over data
                mArena.tournamentAgentManager = mTSAgentManager;
                mArena.taskState = ArenaTrain.Task.TRNEMNT;
            } else {
                // single player TS...
                mTSAgentManager.runSinglePlayerTournament(mArena);
            }
        }
    } // playPressed()

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
        label1.setText("Select Disk Agents to play:"); //("Select Standard Agents to play:");
        label1.setVisible(false);
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
//        minimaxCheckBox = new JCheckBox();
//        minimaxCheckBox.setText("Minimax");
//        gbc = new GridBagConstraints();
//        gbc.gridx = 1;
//        gbc.gridy = 3;
//        gbc.anchor = GridBagConstraints.WEST;
//        mJPanel.add(minimaxCheckBox, gbc);
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
        gbc.gridy = 39;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(startButton, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Episodes per Match");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 30;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label2, gbc);
        episodeNumTextField = new JTextField();
        episodeNumTextField.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 31;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(episodeNumTextField, gbc);
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
        gbc.gridy = 46;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mJPanel.add(spacer4, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 32;
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
        gbc.gridy = 33;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(addNRandomMovesCheckBox, gbc);
        numOfMovesTextField = new JTextField();
        numOfMovesTextField.setText("2");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 35;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(numOfMovesTextField, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Number of moves (ply):");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 34;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label3, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 36;
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
        gbc.gridy = 44;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(saveResultsToDiskButton, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 43;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 1, 0);
        mJPanel.add(spacer7, gbc);
        loadResultsFromDiskButton = new JButton();
        loadResultsFromDiskButton.setText("Load Results from Disk");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 45;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(loadResultsFromDiskButton, gbc);
        reopenStatisticsButton = new JButton();
        reopenStatisticsButton.setText("Reopen Statistics");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 42;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(reopenStatisticsButton, gbc);
        final JPanel spacer8 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 40;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        mJPanel.add(spacer8, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("<html><body>"//You can choose Standard and Disk Agents.<br>"
        		+ "\nLoad and select the Disk Agents to play:</body></html>\n");
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
        gbc.gridy = 41;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label5, gbc);
        selectAllHDDAgentsButton = new JButton();
        selectAllHDDAgentsButton.setText("Select All Agents");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 17;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(selectAllHDDAgentsButton, gbc);
        unselectAllHDDAgentsButton = new JButton();
        unselectAllHDDAgentsButton.setText("Unselect All Agents");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 19;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(unselectAllHDDAgentsButton, gbc);
        deleteSelectedHDDAgentsButton = new JButton();
        deleteSelectedHDDAgentsButton.setForeground(new Color(-65536));
        deleteSelectedHDDAgentsButton.setText("Remove Selected Agents from TS");
        deleteSelectedHDDAgentsButton.setToolTipText("Selected Agents are just removed from the tournament, this will not affect/alter the saved agent file on disk!");
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
        gbc.gridy = 37;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(autoSaveAfterTSFinishedCheckBox, gbc);
        final JPanel spacer15 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 38;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mJPanel.add(spacer15, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Tournament Style:");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 23;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(label6, gbc);
        final JPanel spacer16 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 29;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mJPanel.add(spacer16, gbc);
        singleRoundRobinRadioButton = new JRadioButton();
        singleRoundRobinRadioButton.setText("Single Round Robin");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 24;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(singleRoundRobinRadioButton, gbc);
        doubleRoundRobinRadioButton = new JRadioButton();
        doubleRoundRobinRadioButton.setSelected(true);
        doubleRoundRobinRadioButton.setText("Double Round Robin");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 25;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(doubleRoundRobinRadioButton, gbc);
        variableDoubleRoundRobinRadioButton = new JRadioButton();
        variableDoubleRoundRobinRadioButton.setText("Variable Double Round Robin");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 26;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(variableDoubleRoundRobinRadioButton, gbc);
        variableDRoundRobinSlider = new JSlider();
        variableDRoundRobinSlider.setPaintLabels(true);
        variableDRoundRobinSlider.setPaintTicks(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 27;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mJPanel.add(variableDRoundRobinSlider, gbc);
        variableDRRInfoLabel = new JLabel();
        variableDRRInfoLabel.setEnabled(true);
        variableDRRInfoLabel.setText("xx / xx will be played");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 28;
        gbc.anchor = GridBagConstraints.WEST;
        mJPanel.add(variableDRRInfoLabel, gbc);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(singleRoundRobinRadioButton);
        buttonGroup.add(doubleRoundRobinRadioButton);
        buttonGroup.add(variableDoubleRoundRobinRadioButton);
    }

//  /**
//   * @noinspection ALL
//   */
    public JComponent $$$getRootComponent$$$() {
        return mJPanel;
    }
}
