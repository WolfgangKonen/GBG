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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class TournamentsystemGUI2 extends JFrame{
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
    private JCheckBox addNSpecificMovesCheckBox;

    //private GameBoard gameBoard;
    private Arena mArena;
    private TSAgentManager mTSAgentManager;
    private final String TAG = "[TSAgent] ";

    public TournamentsystemGUI2(Arena mArena) { //GameBoard gameBoard) {
        super("TournamentsystemGUI2");
        setContentPane(mJPanel);
        //setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        //this.gameBoard = gameBoard;
        this.mArena = mArena;
        mTSAgentManager = new TSAgentManager();

        mTSAgentManager.addAgent("randomCheckBox",    Types.GUI_AGENT_LIST[0], randomCheckBox, false, null);
        mTSAgentManager.addAgent("minimaxCheckBox",   Types.GUI_AGENT_LIST[1], minimaxCheckBox, false, null);
        mTSAgentManager.addAgent("maxNCheckBox",      Types.GUI_AGENT_LIST[2], maxNCheckBox, false, null);
        mTSAgentManager.addAgent("expectimaxNCheckBox", Types.GUI_AGENT_LIST[3], expectimaxNCheckBox, false, null);
        mTSAgentManager.addAgent("MCNCheckBox",       Types.GUI_AGENT_LIST[4], MCNCheckBox, false, null);
        mTSAgentManager.addAgent("MCTSCheckBox",      Types.GUI_AGENT_LIST[5], MCTSCheckBox, false, null);
        mTSAgentManager.addAgent("MCTSExpectimaxCheckBox", Types.GUI_AGENT_LIST[6], MCTSExpectimaxCheckBox, false, null);
        // GUI_AGENT_LIST[7] ist der Human Player
        mTSAgentManager.addAgent("TDNtuple2CheckBox", Types.GUI_AGENT_LIST[8], TDNtuple2CheckBox, false, null);
        mTSAgentManager.addAgent("TDSCheckBox",       Types.GUI_AGENT_LIST[9], TDSCheckBox, false, null);

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

        for (int i=0; i<agentsAndFileNames.length; i++) {
            PlayAgent playAgent = (PlayAgent) agentsAndFileNames[i][0];
            String agentName = (String)agentsAndFileNames[i][1];
            String agentType;

            if (playAgent instanceof TDAgent) {
                agentType = "TDAgent";
            } else if (playAgent instanceof TDNTuple2Agt) {
                agentType = "TDNTuple2Agt";
            } else if (playAgent instanceof MCTSAgentT) {
                agentType = "MCTSAgentT";
            } else if (playAgent instanceof MCAgent) {
                agentType = "MCAgent";
            } else if (playAgent instanceof MCAgentN) {
                agentType = "MCAgentN";
            } else if (playAgent instanceof MinimaxAgent) {
                agentType = "MinimaxAgent";
            } else if (playAgent instanceof MaxNAgent) {
                agentType = "MaxNAgent";
            } else if (playAgent instanceof ExpectimaxNAgent) {
                agentType = "ExpectimaxNAgent";
            } else if (playAgent instanceof RandomAgent) {
                agentType = "RandomAgent";
            } else {
                agentType = "NaN";
            }

            System.out.println(TAG + "INFO :: loading from Disk successful for agent: " + agentName + " with AgentState: " + playAgent.getAgentState() + " and type: " + agentType);

            // add agent to gui
            JCheckBox newAgent = new JCheckBox("HDD " + agentName);
            checkBoxJPanel.add(newAgent);

            // add to mTSAgentManager
            mTSAgentManager.addAgent("HDD " + agentName, "HDD " + agentType, newAgent, true, playAgent);
        } // for

        System.out.println(TAG + "Number of disk agents loaded into TS: " + mTSAgentManager.getNumDiskAgents());
        mJPanel.revalidate();
        mJPanel.repaint();
    }

    private void playPressed(){
        int countSelectedAgents = 0;
        int numGamesPerMatch;
        try {
            numGamesPerMatch = Integer.parseInt(gameNumTextField.getText());
        } catch(NumberFormatException n) {
            n.printStackTrace();
            System.out.println(TAG+"ERROR :: not a valid number was entered in GameNum Textfield. Using value 1");
            numGamesPerMatch = 1;
        }
        System.out.println(TAG+"numGamesPerMatch: "+numGamesPerMatch);
        mTSAgentManager.setNumberOfGames(numGamesPerMatch);

        System.out.println(TAG+"Startbutton clicked | checkbox states:");
        // durch alle checkboxen der agenten iterieren
        for (TSAgent agent : mTSAgentManager.mAgents)
        {
            //System.out.println(agent.getAgentType() +" == "+ agent.getName());
            // pruefen fuer jede checkbox, ob sie selected ist oder nicht
            if(agent.guiCheckBox.isSelected())
            {
                System.out.println(TAG+agent.guiCheckBox.getText()+": selected");
                countSelectedAgents++;
            }
            else
            {
                System.out.println(TAG+agent.guiCheckBox.getText() + ": deselected");
            }
        }

        if (countSelectedAgents < 2)
        {
            System.out.println(TAG+"Error :: At least 2 Agents need to be selected for a tournament!");
        }
        else
        {
            // determin 1v1 gameplan with selected agents
            //String selectedAGents[] = mTSAgentManager.getNamesAgentsSelected();
            //System.out.println("sel ag: "+ Arrays.toString(selectedAGents));
            //String gamePlan[][] = mTSAgentManager.getGamePlan();

            mTSAgentManager.printGamePlan();

            // send gameplan to custom multicompete to run competitions
            // save game results in TSAgent objects?
            if (mArena.taskState != ArenaTrain.Task.IDLE) {
                System.out.println(TAG+"ERROR :: could not start Tourmenent, Arena is not IDLE");
            }
            else {
                // hand over data
                mArena.tournamentAgentManager = mTSAgentManager;

                mArena.taskState = ArenaTrain.Task.TRNEMNT;
            }
        }
    }

    /**
     * For Testing Only
     */
    public static void main(String[] args) {
        new TournamentsystemGUI2(null);
    }
}
