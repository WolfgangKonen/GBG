package games.EWN.gui;

import tools.Types;

import javax.swing.*;
import java.awt.*;

public class LegendGui extends JPanel {
    private Font menu = new Font("Calibri", Font.PLAIN, 20);
    private JLabel playerLabel, cubeLabel, playerDescription, cubeDescription;

    public LegendGui(int player, int cube){
        super();
        setLayout(new FlowLayout(FlowLayout.LEADING,2,2));
        playerDescription = new JLabel("Player:");
        cubeDescription = new JLabel("The dice rolled:");
        playerLabel = new JLabel("    ");
        playerLabel.setOpaque(true); // Allow changing the bg color
        cubeLabel = new JLabel(String.valueOf(cube));
        playerDescription.setFont(menu);
        cubeDescription.setFont(menu);
        cubeLabel.setFont(menu);
        playerLabel.setFont(menu);
        add(playerDescription);
        add(playerLabel);
        add(cubeDescription);
        add(cubeLabel);
        this.setBackground(Types.GUI_BGCOLOR);
        this.setVisible(true);
    }

    public void update(int player, int cube){
        this.playerDescription.setText("Player:" + String.valueOf(player+1));
        this.playerLabel.setBackground(Types.GUI_PLAYER_COLOR[player]);
        this.cubeLabel.setText(String.valueOf(cube+1));
    }

}
