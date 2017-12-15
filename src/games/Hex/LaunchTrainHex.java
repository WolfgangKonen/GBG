package games.Hex;

import tools.Types;

import javax.swing.*;

import games.Arena;
import games.ArenaTrain;
import games.XArenaFuncs;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * Launch class used to start game Hex in class {@link ArenaTrain} via 
 * a <b>main method</b>. <br> 
 *  
 * @author Wolfgang Konen, TH Cologne, Nov'16
 * 
 * @see Arena
 * @see ArenaTrain
 * @see XArenaFuncs
 */
public class LaunchTrainHex extends JFrame {
    private static final long serialVersionUID = 1L;
    public ArenaTrainHex m_Arena;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        LaunchTrainHex t_Frame = new LaunchTrainHex("General Board Game Playing");

// ---  just for analysis: compute the state space & game tree complexity ---
//		System.out.println("Rough approximation for nStates = "+(int) Math.pow(3, 9)+ " = (3^9)");
//		TicTDBase.countStates2(false);
//		TicTDBase.countStates2(true);

        if (args.length==0) {
            t_Frame.init();
        } else {
            throw new RuntimeException("[LaunchTrainTTT.main] args="+args+" not allowed. Use TicTacToeBatch.");
        }
    }

    public LaunchTrainHex(String title) {
        super(title);
        m_Arena = new ArenaTrainHex(this);
        setLayout(new BorderLayout(10,10));
        setJMenuBar(m_Arena.m_menu);
        add(m_Arena,BorderLayout.CENTER);
        add(new Label(" "),BorderLayout.SOUTH);	// just a little space at the bottom
    }

    /**
     * Initialize the frame and {@link #m_Arena}.
     */
    public void init()
    {
        addWindowListener(new WindowClosingAdapter());
        m_Arena.init();
        setSize(Types.GUI_ARENATRAIN_WIDTH,Types.GUI_ARENATRAIN_HEIGHT);
        setBounds(0,0,Types.GUI_ARENATRAIN_WIDTH,Types.GUI_ARENATRAIN_HEIGHT);
        //pack();
        setVisible(true);
    }

    protected static class WindowClosingAdapter
            extends WindowAdapter
    {
        public WindowClosingAdapter()  {  }

        public void windowClosing(WindowEvent event)
        {
            event.getWindow().setVisible(false);
            event.getWindow().dispose();
            System.exit(0);
        }
    }
}
