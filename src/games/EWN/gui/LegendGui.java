package games.EWN.gui;

import games.Arena;
import games.StateObservation;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class LegendGui extends JPanel {
    private Font menu = new Font("Calibri", Font.PLAIN, 20);
    private JLabel playerLabel, cubeLabel, playerDescription, cubeDescription, space1, space2;
    private JButton cubeDecr, cubeIncr;
    private JButton rollback;
    private GameBoardGuiEWN gbgui;

    public LegendGui(int player, int cube, GameBoardGuiEWN parent){
        super();
        gbgui = parent;
        setLayout(new FlowLayout(FlowLayout.LEADING,2,2));
        playerDescription = new JLabel("Player:");
        cubeDescription = new JLabel("The dice rolled:");
        playerLabel = new JLabel("    ");
        playerLabel.setOpaque(true); // Allow changing the bg color
        cubeLabel = new JLabel(String.valueOf(cube));
        space1 = new JLabel("   ");
        space2 = new JLabel("         ");
        cubeDecr = new XrButton("-",gbgui.getM_gb());
        cubeIncr = new XrButton("+",gbgui.getM_gb());
        rollback = new XrButton("back",gbgui.getM_gb());
        playerDescription.setFont(menu);
        playerLabel.setFont(menu);
        cubeDescription.setFont(menu);
        cubeLabel.setFont(menu);
        cubeDecr.setFont(menu);
        cubeIncr.setFont(menu);
        rollback.setFont(menu);
        add(playerDescription);
        add(playerLabel);
        add(cubeDescription);
        add(space1);
        add(cubeLabel);
        add(space1);
        add(cubeDecr);
        add(cubeIncr);
        add(space2);
        add(rollback);
        cubeDecr.setEnabled(false);
        cubeIncr.setEnabled(false);
        rollback.setEnabled(false);
        this.setBackground(Types.GUI_BGCOLOR);
        this.setVisible(true);
    }

    public void update(int player, int cube){
        this.playerDescription.setText("Player:" + String.valueOf(player+1));
        this.playerLabel.setBackground(Types.GUI_PLAYER_COLOR[player]);
        this.cubeLabel.setText(String.valueOf(cube+1));
        boolean isInspectV = (gbgui.getM_gb().getArena().taskState == Arena.Task.INSPECTV);
        cubeDecr.setVisible(isInspectV);
        cubeIncr.setVisible(isInspectV);
        rollback.setVisible(isInspectV);
        cubeDecr.setEnabled(isInspectV);
        cubeIncr.setEnabled(isInspectV);
        rollback.setEnabled(false);
        LinkedList<StateObservation> q = gbgui.getM_gb().getStateQueue();
        if (q != null) {
            rollback.setEnabled(isInspectV && q.size()>1);
        }
    }

}
