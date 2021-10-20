package games.EWN.gui;

import tools.Types;

import javax.swing.*;
import java.awt.*;

public class LegendGui extends JPanel {
    private Font menu = new Font("Calibri", Font.PLAIN, 20);
    private JLabel playerLabel, cubeLabel, playerDescription, cubeDescription;
    private int[] cubed = new int[]{0,0,0,0,0,0};

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
        cubed[cube]++;
        this.cubeLabel.setText(String.valueOf(cube+1));
        System.out.println(cubed[0] + " " + cubed[1] + " " +cubed[2] + " " +cubed[3] + " " +cubed[4] + " " +cubed[5]);
    }

}
