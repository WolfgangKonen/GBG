package games.TicTacToe;

import java.applet.*;
import java.awt.BorderLayout;
import java.awt.Label;

import javax.swing.JApplet;

import games.Arena;
import games.ArenaTrain;
import tools.Types;

/**
 * Launch class used to run {@link ArenaTrain} as <b>applet</b>. <br> 
 * 
 * @see Arena
 * @see ArenaTrain
 *  
 * @author Wolfgang Konen, TH Koeln, Nov'16
 *
 */
public class LaunchAppletTTT extends JApplet {
	private static final long serialVersionUID = 1L;
	public ArenaTrainTTT t_Game;

	public LaunchAppletTTT() {
		t_Game = new ArenaTrainTTT();
		setLayout(new BorderLayout(10,10));
		setJMenuBar(t_Game.m_menu);
		add(t_Game,BorderLayout.CENTER);
		add(new Label(" "),BorderLayout.SOUTH);	// just a little space at the bottom
	}
	
	/**
	 * Initialize the applet and {@link #t_Game}.
	 */
	public void init()
	{	
		t_Game.init();
		setSize(Types.GUI_ARENATRAIN_WIDTH,Types.GUI_ARENATRAIN_HEIGHT);		
		setBounds(0,0,Types.GUI_ARENATRAIN_WIDTH,Types.GUI_ARENATRAIN_HEIGHT);
		//pack();
		//setVisible(true);
	}	
	
	public void run() {
		t_Game.run();
	}
	
}
