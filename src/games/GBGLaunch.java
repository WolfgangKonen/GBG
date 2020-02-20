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
import games.Hex.ArenaHex;
import games.Hex.ArenaTrainHex;
import games.Hex.HexConfig;
import games.Nim.ArenaNim;
import games.Nim.ArenaTrainNim;
import games.Othello.ArenaTrainOthello;
import games.RubiksCube.ArenaTrainCube;
import games.Sim.ArenaSim;
import games.Sim.ArenaTrainSim;
import games.Sim.ConfigSim;
import games.TicTacToe.ArenaTrainTTT;
import games.ZweiTausendAchtundVierzig.ArenaTrain2048;
import gui.SolidBorder;
import tools.Types;

/**
 * This class is a general launcher for GBG. The user may select via launcher UI or predefine  
 * via command line arguments which GBG game will be started. 
 * <p>
 * For scalable games, once the game is selected, the launcher will allow to set the scalable 
 * parameters (i.e. number of players, number of nodes for Sim). For non-scalable games (i.e. 
 * TicTacToe), the scalable parameter selector boxes will be disabled.
 * <p>
 * When GBG's Arena finishes, the launcher UI will show up again and allow to select another game.
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

	private JLabel scaPar1_L;
	private JLabel scaPar2_L;
	private JLabel scaPar3_L;
	private JComboBox choiceScaPar1;
	private JComboBox choiceScaPar2;
	private JComboBox choiceScaPar3;

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

	private static void startGBGame(String selectedGame, GBGLaunch t_Launch) {
		String title = "General Board Game Playing";
		String s1 = (String) t_Launch.choiceScaPar1.getSelectedItem();
		String s2 = (String) t_Launch.choiceScaPar2.getSelectedItem();
		String s3 = (String) t_Launch.choiceScaPar3.getSelectedItem();
		boolean withUI = true;
		switch(selectedGame) {
		case "2048": 
			t_Game = new ArenaTrain2048(title,withUI);
			break;
		case "ConnectFour": 
			t_Game = new ArenaTrainC4(title,withUI);
			break;
		case "Hex": 
			// Set HexConfig.BOARD_SIZE *prior* to calling constructor ArenaTrainHex, 
			// which will directly call Arena's constructor where the game board and
			// the Arena buttons are constructed 
			ArenaHex.setBoardSize(Integer.parseInt(s1));
			t_Game = new ArenaTrainHex(title,withUI);
			break;
		case "Nim": 
			// Set NimConfig.{NUMBER_HEAPS,HEAP_SIZE,MAX_MINUS} *prior* to calling constructor  
			// ArenaTrainNim, which will directly call Arena's constructor where the game board and
			// the Arena buttons are constructed 
			ArenaNim.setNumHeaps(Integer.parseInt(s1));
			ArenaNim.setHeapSize(Integer.parseInt(s2));
			ArenaNim.setMaxMinus(Integer.parseInt(s3));
			t_Game = new ArenaTrainNim(title,withUI);
			break;
		case "Othello": 
			t_Game = new ArenaTrainOthello(title,withUI);
			break;
		case "RubiksCube": 
			t_Game = new ArenaTrainCube(title,withUI);
			break;
		case "Sim": 
			// Set ConfigSim.{NUM_PLAYERS,NUM_NODES} *prior* to calling constructor ArenaTrainSim, 
			// which will directly call Arena's constructor where the game board and
			// the Arena buttons are constructed 
			ArenaSim.setNumPlayers(Integer.parseInt(s1));
			ArenaSim.setNumNodes(Integer.parseInt(s2));
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
	protected GBGLaunch() {
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
		
		scaPar1_L = new JLabel("");
		choiceScaPar1 = new JComboBox();
		scaPar2_L = new JLabel("");
		choiceScaPar2 = new JComboBox();
		scaPar3_L = new JLabel("");
		choiceScaPar3 = new JComboBox();
		JPanel scaPar1Panel = new JPanel();
		scaPar1Panel.setLayout(new GridLayout(1,0,2,2));		// rows,columns,hgap,vgap
		scaPar1Panel.add(scaPar1_L);
		scaPar1Panel.add(choiceScaPar1);
		JPanel scaPar2Panel = new JPanel();
		scaPar2Panel.setLayout(new GridLayout(1,0,2,2));		// rows,columns,hgap,vgap
		scaPar2Panel.add(scaPar2_L);
		scaPar2Panel.add(choiceScaPar2);
		JPanel scaPar3Panel = new JPanel();
		scaPar3Panel.setLayout(new GridLayout(1,0,2,2));		// rows,columns,hgap,vgap
		scaPar3Panel.add(scaPar3_L);
		scaPar3Panel.add(choiceScaPar3);

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
		launcherUI.setLayout(new GridLayout(0,1,3,3));		// rows,columns,hgap,vgap
		launcherUI.add(titlePanel);
		launcherUI.add(choiceGame);
		launcherUI.add(scaPar1Panel);
		launcherUI.add(scaPar2Panel);
		launcherUI.add(scaPar3Panel);
		launcherUI.add(StartG);
		launcherUI.add(Exit);
		launcherUI.addWindowListener(new WindowClosingAdapter());

		launcherUI.setSize(200,300);
		launcherUI.setBounds(400,300,200,300);		// x,y,width,height
		launcherUI.pack();
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
		
		choiceGame.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{	
						selectedGame = (String)choiceGame.getSelectedItem();
						adjustScaParGuiPart();						
					}
				}	
		);
		
	}

	/**
	 * Adjust the scalable parameters of a game after the game selector has changed.
	 */
	public void adjustScaParGuiPart() {
		switch(selectedGame) {
		case "Hex": 
			scaPar1_L.setText("Board Size");
			scaPar2_L.setText("");
			scaPar3_L.setText("");
			setScaPar1List(new int[]{2,3,4,5,6,7,8});
			setScaPar2List(new int[]{});
			setScaPar3List(new int[]{});
			break;
		case "Nim": 
			scaPar1_L.setText("Heaps");
			scaPar2_L.setText("Heap Size");
			scaPar3_L.setText("Max Minus");
			setScaPar1List(new int[]{2,3,4,5});
			setScaPar2List(new int[]{5,6,7,8,9,10,20,50});
			setScaPar3List(new int[]{2,3,4,5});
			break;
		case "Sim": 
			scaPar1_L.setText("Players");
			scaPar2_L.setText("Nodes");
			scaPar3_L.setText("");
			setScaPar1List(new int[]{2,3});
			setScaPar2List(new int[]{6,7,8,9});
			setScaPar3List(new int[]{});
			break;
		case "2048": 
		case "ConnectFour": 
		case "Othello": 
		case "RubiksCube": 
		case "TicTacToe": 
			scaPar1_L.setText("");
			scaPar2_L.setText("");
			scaPar3_L.setText("");
			setScaPar1List(new int[]{});
			setScaPar2List(new int[]{});
			setScaPar3List(new int[]{});
			break;
		default: 
			System.err.println("[GBGLaunch] "+selectedGame+": This game is unknown.");
			System.exit(1);
		}
		
	}
	
	public void setScaPar1List(int[] modeList) {
		choiceScaPar1.removeAllItems();
		for (int i : modeList)
			choiceScaPar1.addItem(Integer.toString(i));
	}

	public void setScaPar2List(int[] modeList) {
		choiceScaPar2.removeAllItems();
		for (int i : modeList)
			choiceScaPar2.addItem(Integer.toString(i));
	}

	public void setScaPar3List(int[] modeList) {
		choiceScaPar3.removeAllItems();
		for (int i : modeList)
			choiceScaPar3.addItem(Integer.toString(i));
	}

	public void setScaPar1Tooltip(String str) {
		choiceScaPar1.setToolTipText(str);
	}

	public void setScaPar2Tooltip(String str) {
		choiceScaPar2.setToolTipText(str);
	}

	public void setScaPar3Tooltip(String str) {
		choiceScaPar3.setToolTipText(str);
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
