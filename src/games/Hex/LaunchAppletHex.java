package games.Hex;

import tools.Types;

import javax.swing.*;
import java.awt.*;

public class LaunchAppletHex extends JApplet {
    public ArenaTrainHex game;

    public LaunchAppletHex() {
        game = new ArenaTrainHex();
        setLayout(new BorderLayout(10,10));
        setJMenuBar(game.m_menu);
        add(game,BorderLayout.CENTER);
        add(new Label(" "),BorderLayout.SOUTH);	// just a little space at the bottom
    }

    public void init() {
        game.init();
        setSize(Types.GUI_ARENATRAIN_WIDTH,Types.GUI_ARENATRAIN_HEIGHT);
        setBounds(0,0,Types.GUI_ARENATRAIN_WIDTH,Types.GUI_ARENATRAIN_HEIGHT);
        //pack();
        //setVisible(true);
    }

    public void run() {
        game.run();
    }
}
