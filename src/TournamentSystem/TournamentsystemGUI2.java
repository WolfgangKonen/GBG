package TournamentSystem;

import games.Arena;
import games.ArenaTrain;
import tools.Types;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

        mTSAgentManager.addAgent( "randomCheckBox",    Types.GUI_AGENT_LIST[0], randomCheckBox);
        mTSAgentManager.addAgent("minimaxCheckBox",   Types.GUI_AGENT_LIST[1], minimaxCheckBox);
        mTSAgentManager.addAgent("maxNCheckBox",      Types.GUI_AGENT_LIST[2], maxNCheckBox);
        mTSAgentManager.addAgent("expectimaxNCheckBox", Types.GUI_AGENT_LIST[3], expectimaxNCheckBox);
        mTSAgentManager.addAgent("MCNCheckBox",       Types.GUI_AGENT_LIST[4], MCNCheckBox);
        mTSAgentManager.addAgent("MCTSCheckBox",      Types.GUI_AGENT_LIST[5], MCTSCheckBox);
        mTSAgentManager.addAgent("MCTSExpectimaxCheckBox", Types.GUI_AGENT_LIST[6], MCTSExpectimaxCheckBox);
        // GUI_AGENT_LIST[7] ist der Human Player
        mTSAgentManager.addAgent("TDNtuple2CheckBox", Types.GUI_AGENT_LIST[8], TDNtuple2CheckBox);
        mTSAgentManager.addAgent("TDSCheckBox",       Types.GUI_AGENT_LIST[9], TDSCheckBox);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JOptionPane.showMessageDialog(null, "Hello World");
                playPressed();
            }
        });

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
