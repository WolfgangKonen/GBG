package games.EWN.gui;

import games.Arena;
import tools.Types;

import javax.swing.*;
import java.awt.*;

public class LegendGui extends JPanel {
    private Font menu = new Font("Calibri", Font.PLAIN, 20);
    private JLabel playerLabel, cubeLabel, playerDescription, cubeDescription, cubeSpace;
    private JButton cubeDecr, cubeIncr;
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
        cubeSpace = new JLabel("   ");
        cubeDecr = new XrButton("-",gbgui.getM_gb());
        cubeIncr = new XrButton("+",gbgui.getM_gb());
        playerDescription.setFont(menu);
        playerLabel.setFont(menu);
        cubeDescription.setFont(menu);
        cubeLabel.setFont(menu);
        cubeDecr.setFont(menu);
        cubeIncr.setFont(menu);
        add(playerDescription);
        add(playerLabel);
        add(cubeDescription);
        add(cubeSpace);
        add(cubeLabel);
        add(cubeSpace);
        add(cubeDecr);
        add(cubeIncr);
        cubeDecr.setEnabled(false);
        cubeIncr.setEnabled(false);
        this.setBackground(Types.GUI_BGCOLOR);
        this.setVisible(true);
    }

    public void update(int player, int cube){
        this.playerDescription.setText("Player:" + String.valueOf(player+1));
        this.playerLabel.setBackground(Types.GUI_PLAYER_COLOR[player]);
        this.cubeLabel.setText(String.valueOf(cube+1));
        boolean isInspectV = (gbgui.getM_gb().m_Arena.taskState == Arena.Task.INSPECTV);
        cubeDecr.setVisible(isInspectV);
        cubeIncr.setVisible(isInspectV);
        cubeDecr.setEnabled(isInspectV);
        cubeIncr.setEnabled(isInspectV);
    }

}
