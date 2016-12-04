package games.ZweiTausendAchtundVierzig;

import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class LaunchArenaZTAV extends JFrame{
    private static final long serialVersionUID = 1L;
    public ArenaZTAV m_Arena;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        LaunchArenaZTAV t_Frame = new LaunchArenaZTAV("General buttons Game Playing");

        if (args.length==0) {
            t_Frame.init();
        } else {
            throw new RuntimeException("[LaunchArenaZTAV.main] args="+args+" not allowed. Use TicTacToeBatch.");
        }

    }

    /**
     * Initialize the frame and {@link #m_Arena}.
     */
    public void init()
    {
        addWindowListener(new LaunchArenaZTAV.WindowClosingAdapter());
        m_Arena.init();
        setSize(Types.GUI_ARENATRAIN_WIDTH,Types.GUI_ARENATRAIN_HEIGHT);
        setBounds(0,0,Types.GUI_ARENATRAIN_WIDTH,Types.GUI_ARENATRAIN_HEIGHT);
        //pack();
        setVisible(true);
    }

    public LaunchArenaZTAV(String title) {
        super(title);
        m_Arena = new ArenaZTAV(this);
        setLayout(new BorderLayout(10,10));
        setJMenuBar(m_Arena.m_menu);
        add(m_Arena,BorderLayout.CENTER);
        add(new Label(" "),BorderLayout.SOUTH);	// just a little space at the bottom
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
