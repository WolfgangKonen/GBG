package games.TicTacToe;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;

import games.Arena;
import games.ArenaTrain;
import games.XArenaFuncs;
import tools.Types;

/**
 * Launch class used to start game TicTacToe in class {@link Arena} via 
 * a <b>main method</b>: <br> 
 *  
 * @author Wolfgang Konen, TH Cologne, Nov'16
 * 
 * @see Arena
 * @see ArenaTrain
 * @see XArenaFuncs
 */
public class LaunchArenaTTT extends JFrame {

	private static final long serialVersionUID = 1L;
	public ArenaTTT m_Arena;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		LaunchArenaTTT t_Frame = new LaunchArenaTTT("General Board Game Playing");

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[LaunchArenaTTT.main] args="+args+" not allowed. Use TicTacToeBatch.");
		}

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

	public LaunchArenaTTT(String title) {
		super(title);
		m_Arena = new ArenaTTT(this);
		setLayout(new BorderLayout(10,10));
		setJMenuBar(m_Arena.m_menu);
		add(m_Arena,BorderLayout.CENTER);
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
