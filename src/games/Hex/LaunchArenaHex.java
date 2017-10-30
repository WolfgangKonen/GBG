package games.Hex;

import games.Arena;
import games.ArenaTrain;
import games.XArenaFuncs;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * Launch class used to start game TicTacToe in class {@link Arena} via
 * a <b>main method</b>: <br>
 *
 * @author Wolfgang Konen, TH Cologne, Nov'16
 * @see Arena
 * @see ArenaTrain
 * @see XArenaFuncs
 */
public class LaunchArenaHex extends JFrame {

    private static final long serialVersionUID = 1L;
    public ArenaHex m_Arena;

    public LaunchArenaHex(String title) {
        super(title);
        m_Arena = new ArenaHex(this);
        setLayout(new BorderLayout(10, 10));
        setJMenuBar(m_Arena.m_menu);
        add(m_Arena, BorderLayout.CENTER);
        add(new Label(" "), BorderLayout.SOUTH);    // just a little space at the bottom
    }

    /**
     * @param args ...
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        LaunchArenaHex t_Frame = new LaunchArenaHex("General Board Game Playing");

        if (args.length == 0) {
            t_Frame.init();
        } else {
            throw new RuntimeException("[LaunchArenaHex.main] args=" + args + " not allowed. Use TicTacToeBatch.");
        }

    }

    /**
     * Initialize the frame and {@link #m_Arena}.
     */
    public void init() {
        addWindowListener(new WindowClosingAdapter());
        m_Arena.init();
        setSize(Types.GUI_ARENATRAIN_WIDTH, Types.GUI_ARENATRAIN_HEIGHT);
        setBounds(0, 0, Types.GUI_ARENATRAIN_WIDTH, Types.GUI_ARENATRAIN_HEIGHT);
        //pack();
        setVisible(true);
    }

    protected static class WindowClosingAdapter
            extends WindowAdapter {
        public WindowClosingAdapter() {
        }

        public void windowClosing(WindowEvent event) {
            event.getWindow().setVisible(false);
            event.getWindow().dispose();
            System.exit(0);
        }
    }

}
