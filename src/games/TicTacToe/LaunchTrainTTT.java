package games.TicTacToe;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import games.Arena;
import games.ArenaTrain;
import games.XArenaFuncs;
import tools.Types;

/**
 * Launch class used to start game TicTacToe in class {@link ArenaTrain} via 
 * a <b>main method</b>. <br> 
 *  
 * @author Wolfgang Konen, TH Köln , Nov'16
 * 
 * @see Arena
 * @see ArenaTrain
 * @see XArenaFuncs
 */
public class LaunchTrainTTT extends JFrame {

	private static final long serialVersionUID = 1L;
	public ArenaTrainTTT m_Arena;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		LaunchTrainTTT t_Frame = new LaunchTrainTTT("General Board Game Playing");

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

	public LaunchTrainTTT(String title) {
		super(title);
		m_Arena = new ArenaTrainTTT(this);
		setLayout(new BorderLayout(0,0));
		setJMenuBar(m_Arena.m_menu);
		add(m_Arena,BorderLayout.CENTER);		
		
		/*
		// invoke the following lines only during the one-time transformation v12 -> v13 
		// of saved TDNTupleAgt agents:
		TransformTdAgents m_transformTD 
		//	= new TransformTdAgents(TransformTdAgents.TRANSFORMDIRECTION.V12ToTemp,this);
			= new TransformTdAgents(TransformTdAgents.TRANSFORMDIRECTION.TempToV13,this);
		*/
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
