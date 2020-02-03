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
 * @author Wolfgang Konen, TH Koeln, Nov'16
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

        if (args.length==0) {
            t_Frame.init();
        } else {
            throw new RuntimeException("[LaunchTrainTTT.main] args="+args+" not allowed. Use batch facility.");
        }
    }

    public LaunchTrainHex(String title) {
        super(title);
        m_Arena = new ArenaTrainHex(this);
        setLayout(new BorderLayout(0,0));
        setJMenuBar(m_Arena.m_menu);
        add(m_Arena,BorderLayout.CENTER);
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
