package TournamentSystem;

import games.Arena;
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
    private ArrayList<TSAgent> checkBoxen;

    public TournamentsystemGUI2(Arena mArena) { //GameBoard gameBoard) {
        super("TournamentsystemGUI2");
        setContentPane(mJPanel);
        //setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        //this.gameBoard = gameBoard;
        this.mArena = mArena;

        checkBoxen = new ArrayList<>();
        checkBoxen.add(new TSAgent("randomCheckBox",    Types.GUI_AGENT_LIST[0], randomCheckBox));
        checkBoxen.add(new TSAgent("minimaxCheckBox",   Types.GUI_AGENT_LIST[1], minimaxCheckBox));
        checkBoxen.add(new TSAgent("maxNCheckBox",      Types.GUI_AGENT_LIST[2], maxNCheckBox));
        checkBoxen.add(new TSAgent("expectimaxNCheckBox", Types.GUI_AGENT_LIST[3], expectimaxNCheckBox));
        checkBoxen.add(new TSAgent("MCNCheckBox",       Types.GUI_AGENT_LIST[4], MCNCheckBox));
        checkBoxen.add(new TSAgent("MCTSCheckBox",      Types.GUI_AGENT_LIST[5], MCTSCheckBox));
        checkBoxen.add(new TSAgent("MCTSExpectimaxCheckBox", Types.GUI_AGENT_LIST[6], MCTSExpectimaxCheckBox));
        // GUI_AGENT_LIST[7] ist der Human Player
        checkBoxen.add(new TSAgent("TDNtuple2CheckBox", Types.GUI_AGENT_LIST[8], TDNtuple2CheckBox));
        checkBoxen.add(new TSAgent("TDSCheckBox",       Types.GUI_AGENT_LIST[9], TDSCheckBox));

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
            System.out.println("ERROR :: not a valid number was entered in GameNum Textfield. Using value 1");
            numGamesPerMatch = 1;
        }
        System.out.println("numGamesPerMatch: "+numGamesPerMatch);

        System.out.println("Startbutton clicked | checkbox states:");
        // durch alle checkboxen der agenten iterieren
        for (TSAgent agent : checkBoxen)
        {
            //System.out.println(agent.getAgentType() +" == "+ agent.getName());
            // pruefen fuer jede checkbox, ob sie selected ist oder nicht
            if(agent.guiCheckBox.isSelected())
            {
                System.out.println(agent.guiCheckBox.getText()+": selected");
                countSelectedAgents++;
            }
            else
            {
                System.out.println(agent.guiCheckBox.getText() + ": deselected");
            }
        }
        System.out.println("\n");
        if (countSelectedAgents < 2)
        {
            System.out.println("Error :: At least 2 Agents need to be selected for a tournament!");
        }
        else
        {
            // determin 1v1 gameplan with selected agents
            String selectedAGents[] = new String[countSelectedAgents]; // just selected agents
            int tmp = 0;
            for (TSAgent agent : checkBoxen) {
                if (agent.guiCheckBox.isSelected()) {
                    selectedAGents[tmp++] = agent.getAgentType();
                }
            }
            //System.out.println("sel ag: "+ Arrays.toString(selectedAGents));

            String gamePlan[][] = new String[countSelectedAgents*(countSelectedAgents-1)][2]; // games to be played
            int tmpGame = 0;
            for (int i=0; i<countSelectedAgents; i++) {
                for (int j=0; j<countSelectedAgents; j++) {
                    if (i!=j) { // avoid agent to play against itself
                        gamePlan[tmpGame][0] = selectedAGents[i];
                        gamePlan[tmpGame++][1] = selectedAGents[j];
                    }
                }
            }

            System.out.println("Games to play: "+gamePlan.length);
            for (String round[] : gamePlan)
                System.out.println("["+round[0]+"] vs ["+round[1]+"]");

            // send gameplan to custom multicompete to run competitions
            // save game results in TSAgent objects?
        }
    }

    /**
     * For Testing Only
     */
    public static void main(String[] args) {
        new TournamentsystemGUI2(null);
    }
}
