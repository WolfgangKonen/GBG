package games.ZweiTausendVierundAchzig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import games.ArenaTrain;
import games.XArenaFuncs;
import tools.Types;

/**
 * Created by Johannes on 29.11.2016.
 */
public class LaunchTrainZTVA extends JFrame {
    private static final long serialVersionUID = 1L;
    public ArenaTrainZTVA m_Arena;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        LaunchTrainZTVA t_Frame = new LaunchTrainZTVA("General buttons Game Playing");

// ---  just for analysis: compute the state space & game tree complexity ---
//		System.out.println("Rough approximation for nStates = "+(int) Math.pow(3, 9)+ " = (3^9)");
//		TicTDBase.countStates2(false);
//		TicTDBase.countStates2(true);

        if (args.length == 0) {
            t_Frame.init();
        } else {
            throw new RuntimeException("[LaunchTrainZTVA.main] args=" + args + " not allowed. Use TicTacToeBatch.");
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

    public LaunchTrainZTVA(String title) {
        super(title);
        m_Arena = new ArenaTrainZTVA(this);
        setLayout(new BorderLayout(10, 10));
        setJMenuBar(m_Arena.m_menu);
        add(m_Arena, BorderLayout.CENTER);
        add(new Label(" "), BorderLayout.SOUTH);    // just a little space at the bottom

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