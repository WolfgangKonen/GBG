package TournamentSystem;

import games.GameBoard;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class GridBagLayout {
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

    private JFrame mFrame;
    private GameBoard gameBoard;
    private ArrayList<TSAgent> checkBoxen;

    public GridBagLayout(GameBoard gameBoard) {
        mFrame = new JFrame("GridBagLayout");
        mFrame.setContentPane(mJPanel);
        //mFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mFrame.pack();
        mFrame.setVisible(true);

        this.gameBoard = gameBoard;

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
        int selected = 0;
        System.out.println("Startbutton clicked | checkbox states:");
        // durch alle checkboxen der agenten iterieren
        for (TSAgent agent : checkBoxen)
        {
            //System.out.println(agent.getAgentType() +" == "+ agent.getName());
            // pruefen fuer jede checkbox, ob sie selected ist oder nicht
            if(agent.guiCheckBox.isSelected())
            {
                System.out.println(agent.guiCheckBox.getText()+": selected");
                selected++;
            }
            else
            {
                System.out.println(agent.guiCheckBox.getText() + ": deselected");
            }
        }
        System.out.println("\n");
        if (selected < 2)
        {
            System.out.println("Error :: At least 2 Agents need to be selected for a tournament!");
        }
        else
        {
            // start tournament
            // ...
        }
    }

    /**
     * move the GUI to the front
     */
    public void show() {
        mFrame.setVisible(true);
        mFrame.setState(Frame.NORMAL);
        mFrame.toFront();
    }

    /**
     * For Testing Only
     */
    public static void main(String[] args) {
        new GridBagLayout(null);
    }
}
