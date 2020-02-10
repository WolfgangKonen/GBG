package games;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import games.Arena.Task;
import games.CFour.ArenaTrainC4;
import games.Hex.ArenaTrainHex;
import games.Nim.ArenaTrainNim;
import games.Othello.ArenaTrainOthello;
import games.RubiksCube.ArenaTrainCube;
import games.Sim.ArenaTrainSim;
import games.TicTacToe.ArenaTrainTTT;
import games.ZweiTausendAchtundVierzig.ArenaTrain2048;
import gui.SolidBorder;
import tools.Types;

/**
 * This class is a general launcher for GBG. The user may select via launcher UI or predefine  
 * via command line arguments which GBG game will be started. When GBG's Arena finishes, the 
 * launcher UI will show up again and allow to select another game.
 * 
 */
public class GBGLaunch {
	/**
	 *  The possible games: {"2048","ConnectFour","Hex","Nim","Othello","RubiksCube","Sim","TicTacToe"} 
	 */
	String[] game_list = {"2048","ConnectFour","Hex","Nim","Othello","RubiksCube","Sim","TicTacToe"};
	
	enum LaunchTask {
		STARTSELECTOR, SELECTGAME,	STARTGAME, EXITSELECTOR, IDLE
	};
	LaunchTask launcherState = LaunchTask.SELECTGAME;	// also used in Arena.destroy()
	
	private static final long serialVersionUID = 1L;
	public static ArenaTrain t_Game;
	private JFrame launcherUI = null;
	private static String selectedGame = "TicTacToe";

	/**
	 * Starts the  general launcher for GBG. The user may select via launcher UI or predefine  
	 * via command line arguments which GBG game will be started. When GBG's Arena finishes, the 
	 * launcher UI will show up again and allow to select another game.
	 * 
	 * @param args <br>
	 *          [0] 0: direct start w/o launcher UI, 1: with launcher UI (default is 1)<br>
	 * 			[1] the name of the game, one out of {@link #game_list} (default is "TicTacToe") 
	 * <p>  
	 * Examples:       
	 * <ul>
	 * <li>	{@code GBGLaunch 1 Nim} : start launcher, with Nim preselected
	 * <li>	{@code GBGLaunch} : start launcher, with TicTacToe preselected
	 * <li>	{@code GBGLaunch 0 TicTacToe} : start directly TicTacToe, no launcher
	 * <li>	{@code GBGLaunch 0} : the same
	 * </ul>
	 *          	
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		GBGLaunch t_Launch;
		boolean withLauncherUI = true;

		if (args.length>0) {
			withLauncherUI = (Integer.parseInt(args[0])==1);
		}
		if (args.length>1) {
			selectedGame = args[1];
		}

		if (withLauncherUI) {
			t_Launch = new GBGLaunch();
			while(true) {
				switch(t_Launch.launcherState) {
				case STARTSELECTOR:
					t_Launch.launcherUI.setVisible(true);
					t_Launch.launcherState = LaunchTask.SELECTGAME;
					break;
				case SELECTGAME:
					// this state is left when button StartG ('Start Game') in launcher is hit
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
					break;
				case STARTGAME:
					t_Launch.launcherUI.setVisible(false);
					startGBGame(selectedGame,t_Launch);
					t_Launch.launcherState = LaunchTask.IDLE;
					break;
				case EXITSELECTOR:
					t_Launch.launcherUI.setVisible(false);
					t_Launch.launcherUI.dispose();
					System.exit(0);
					break;
				case IDLE:
				default: 
					// this state is left when the GBG Arena finishes, it then
					// sets the state to STARTSELECTOR
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				}
				
			}
		} else {
			// start selectedGame without launcherUI-loop
			startGBGame(selectedGame,null);
		}

	}

	static void startGBGame(String selectedGame, GBGLaunch t_Launch) {
		String title = "General Board Game Playing";
		boolean withUI = true;
		switch(selectedGame) {
		case "2048": 
			t_Game = new ArenaTrain2048(title,withUI);
			break;
		case "ConnectFour": 
			t_Game = new ArenaTrainC4(title,withUI);
			break;
		case "Hex": 
			t_Game = new ArenaTrainHex(title,withUI);
			break;
		case "Nim": 
			t_Game = new ArenaTrainNim(title,withUI);
			break;
		case "Othello": 
			t_Game = new ArenaTrainOthello(title,withUI);
			break;
		case "RubiksCube": 
			t_Game = new ArenaTrainCube(title,withUI);
			break;
		case "Sim": 
			t_Game = new ArenaTrainSim(title,withUI);
			break;
		case "TicTacToe": 
			t_Game = new ArenaTrainTTT(title,withUI);
			break;
		default: 
			System.err.println("[GBGLaunch] "+selectedGame+": This game is unknown.");
			System.exit(1);
		}

		t_Game.setLauncherObj(t_Launch);
		t_Game.init();
	}
	
	/**
	 * creates the launcher UI
	 */
	public GBGLaunch() {
		// the colors of the TH Koeln logo (used for button coloring):
		Color colTHK1 = new Color(183,29,13);
		Color colTHK2 = new Color(255,137,0);
		Color colTHK3 = new Color(162,0,162);
		SolidBorder bord = new SolidBorder();

		JPanel titlePanel = new JPanel();
		titlePanel.setBackground(Types.GUI_BGCOLOR);
		JLabel Blank = new JLabel(" "); // a little bit of space
		JLabel  m_title = new JLabel("GBG Launcher", SwingConstants.CENTER);
		m_title.setForeground(Color.black);
		Font tFont = new Font("Arial", 1, Types.GUI_TITLEFONTSIZE);
		m_title.setFont(tFont);
		titlePanel.add(Blank);
		titlePanel.add(m_title);
		
		JComboBox choiceGame = new JComboBox(game_list);
		choiceGame.setSelectedItem(selectedGame);
		
		JButton StartG=new JButton("Start Game");
		StartG.setBorder(bord);
		StartG.setEnabled(true);
		StartG.setForeground(Color.white);
		StartG.setBackground(colTHK2);		

		JButton Exit=new JButton("Exit");
		Exit.setBorder(bord);
		Exit.setEnabled(true);
		Exit.setForeground(Color.white);
		Exit.setBackground(colTHK2);	
		
		launcherUI = new JFrame("");
		launcherUI.setLayout(new GridLayout(0,1,10,10));		// rows,columns,hgap,vgap
		launcherUI.add(titlePanel);
		launcherUI.add(choiceGame);
		launcherUI.add(StartG);
		launcherUI.add(Exit);
		launcherUI.addWindowListener(new WindowClosingAdapter());

		launcherUI.setSize(200,200);
		launcherUI.setBounds(400,300,200,200);
		//launcherUI.pack();
		launcherUI.setVisible(true);
		
		StartG.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{	
						selectedGame = (String)choiceGame.getSelectedItem();
						launcherState = LaunchTask.STARTGAME;						
					}
				}	
		);

		Exit.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{	
						launcherState = LaunchTask.EXITSELECTOR;						
					}
				}	
		);
		
	}
	
	/**
	 * helper class for {@link GBGLaunch#GBGLaunch(String)}
	 */
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
