package starters;

import games.Arena;
import gui.SolidBorder;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This class is a general launcher for GBG. The user may select via launcher UI or predefine  
 * via command line arguments (see {@link #main(String[])}) which GBG game will be started. 
 * <p>
 * For scalable games, once a game is chosen, the launcher will allow to select the scalable 
 * parameters (i.e. number of players, number of nodes for Sim). For non-scalable games (i.e. 
 * TicTacToe), the scalable parameter select boxes will be disabled.
 * <p>
 * When 'Start Game' is clicked, the launcher UI becomes invisible and GBG's Arena shows up.<br>
 * When GBG's Arena finishes, the launcher UI will show up again and allow to choose another game.
 * 
 * @author Wolfgang Konen, TH Koeln, 2020
 * 
 * @see GBGBatch
 * @see SetupGBG
 */
public class GBGLaunch extends SetupGBG {
	/**
	 *  The possible games:
	 */
	String[] game_list = {"2048","Blackjack","ConnectFour","EWN","Hex","KuhnPoker","Nim","Nim3P",
						  "Othello","Poker","RubiksCube","Sim","TicTacToe", "Yavalath"};
	
	public enum LaunchTask {
		STARTSELECTOR, SELECTGAME,	STARTGAME, EXITSELECTOR, IDLE
	}
	public LaunchTask launcherState = LaunchTask.SELECTGAME;	// also used in Arena.destroy()
	
	//private static final long serialVersionUID = 1L;
	public static Arena t_Game;
	private final JFrame launcherUI;
	private static String selectedGame = "TicTacToe";

	private final JLabel scaPar0_L;
	private final JLabel scaPar1_L;
	private final JLabel scaPar2_L;
	private final JComboBox<String> choiceScaPar0;
	private final JComboBox<String> choiceScaPar1;
	private final JComboBox<String> choiceScaPar2;

	/**
	 * Starts the  general launcher for GBG. The user may select via launcher UI or predefine  
	 * via command line arguments which GBG game will be started. When GBG's Arena finishes, the 
	 * launcher UI will show up again and allow to select another game.
	 * 
	 * @param args <br>
	 *          [0] 0: direct start w/o launcher UI, 1: with launcher UI (default is 1)<br>
	 * 			[1] the name of the game, one out of {@link #game_list} (default is "TicTacToe") 
	 * 			[2] either "T" (with train rights) or "P" (just play) (default is "T") 
	 * <p>  
	 * Examples:       
	 * <ul>
	 * <li>	{@code GBGLaunch} : start launcher, with TicTacToe preselected
	 * <li>	{@code GBGLaunch 1 Nim} : start launcher, with Nim preselected
	 * <li>	{@code GBGLaunch 0 Hex P} : start directly Hex, no launcher, no train rights
	 * <li>	{@code GBGLaunch 0} : start directly TicTacToe, no launcher
	 * </ul>
	 * When a scalable game is started w/o launcher, the method {@link SetupGBG#setDefaultScaPars(String)} will set
	 * appropriate default scalable parameters for the game in question.
	 */
	public static void main(String[] args) {
		GBGLaunch t_Launch;
		boolean withLauncherUI = true;
		boolean withTrainRights = true;

		if (args.length>0) {
			withLauncherUI = (Integer.parseInt(args[0])==1);
		}
		if (args.length>1) {
			selectedGame = args[1];
		}
		if (args.length>2) {
			withTrainRights = (args[2].equals("T"));
		}

		if (withLauncherUI) {
			t_Launch = new GBGLaunch();
			t_Launch.adjustScaParGuiPart();
			while(true) {
				switch(t_Launch.launcherState) {
				case STARTSELECTOR:
					t_Launch.launcherUI.setVisible(true);
					t_Launch.launcherState = LaunchTask.SELECTGAME;
					break;
				case STARTGAME:
					t_Launch.launcherUI.setVisible(false);

					// start selectedGame with launcherUI-loop
					startGBGame(selectedGame,t_Launch,withTrainRights);

					t_Launch.launcherState = LaunchTask.IDLE;
					break;
				case EXITSELECTOR:
					t_Launch.launcherUI.setVisible(false);
					t_Launch.launcherUI.dispose();
					System.exit(0);
					break;
				case IDLE:
				case SELECTGAME:
				default:
					// this state is left when button StartG ('Start Game') in launcher is hit
					try {
						Thread.sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		} else {

			// start selectedGame without launcherUI-loop
			startGBGame(selectedGame,null,withTrainRights);

		}

	}

	/**
	 * Start a game with or without train rights
	 * @param selectedGame		the game name
	 * @param t_Launch			the launcher
	 * @param withTrainRights	whether Arena is trainable or not
	 *
	 * @see SetupGBG
	 */
	private static void startGBGame(String selectedGame, GBGLaunch t_Launch, boolean withTrainRights) {
		String x, title = "General Board Game Playing";
		String[] scaPar = new String[3];
		for (int i=0; i<3; i++) scaPar[i]="";
		if (t_Launch==null) {
			scaPar = setDefaultScaPars(selectedGame);
		} else {
			// replace scaPar[i] only, if the selected item is not null (to avoid NullPointerException when later using scaPar[i])
			x = (String) t_Launch.choiceScaPar0.getSelectedItem(); if (x!=null) scaPar[0]=x;
			x = (String) t_Launch.choiceScaPar1.getSelectedItem(); if (x!=null) scaPar[1]=x;
			x = (String) t_Launch.choiceScaPar2.getSelectedItem(); if (x!=null) scaPar[2]=x;
		}

		// SetupGBG.setupSelectedGame has the switch statement over all games:
		t_Game = setupSelectedGame(selectedGame, scaPar, title,true,withTrainRights);

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
		Font tFont = new Font("Arial", Font.BOLD, Types.GUI_TITLEFONTSIZE);
		m_title.setFont(tFont);
		titlePanel.add(Blank);
		titlePanel.add(m_title);
		
		JComboBox<String> choiceGame = new JComboBox<>(game_list);
		choiceGame.setSelectedItem(selectedGame);
		
		scaPar0_L = new JLabel("");
		choiceScaPar0 = new JComboBox<>();
		scaPar1_L = new JLabel("");
		choiceScaPar1 = new JComboBox<>();
		scaPar2_L = new JLabel("");
		choiceScaPar2 = new JComboBox<>();
		JPanel scaPar0Panel = new JPanel();
		scaPar0Panel.setLayout(new GridLayout(1,0,2,2));		// rows,columns,hgap,vgap
		scaPar0Panel.add(scaPar0_L);
		scaPar0Panel.add(choiceScaPar0);
		JPanel scaPar1Panel = new JPanel();
		scaPar1Panel.setLayout(new GridLayout(1,0,2,2));		// rows,columns,hgap,vgap
		scaPar1Panel.add(scaPar1_L);
		scaPar1Panel.add(choiceScaPar1);
		JPanel scaPar2Panel = new JPanel();
		scaPar2Panel.setLayout(new GridLayout(1,0,2,2));		// rows,columns,hgap,vgap
		scaPar2Panel.add(scaPar2_L);
		scaPar2Panel.add(choiceScaPar2);

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
		launcherUI.add(scaPar0Panel);
		launcherUI.add(scaPar1Panel);
		launcherUI.add(scaPar2Panel);
		launcherUI.add(StartG);
		launcherUI.add(Exit);
		launcherUI.addWindowListener(new WindowClosingAdapter());

		launcherUI.setSize(300,300);
		launcherUI.setBounds(400,300,300,300);		// x,y,width,height
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
		
		choiceScaPar1.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{	
						selectedGame = (String)choiceGame.getSelectedItem();
						if (selectedGame!=null) {
							if (selectedGame.equals("Nim")) {
								String heapSize = (String)choiceScaPar1.getSelectedItem();
								assert heapSize!=null : "heapSize is null!";
								int iHeapSize = Integer.parseInt(heapSize);
								choiceScaPar2.removeAllItems();
								if (iHeapSize==3) {
									setScaPar2List(new int[]{2,3});
								} else {
									setScaPar2List(new int[]{2,3,4,5});
									if (iHeapSize>5) choiceScaPar2.addItem(heapSize);
								}
							}
						}
					}
				}	
		);
		
	}

	/**
	 * helper class for {@link GBGLaunch#GBGLaunch()}
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

	/**
	 * Adjust the scalable parameters of a game after the game selector has changed.
	 */
	public void adjustScaParGuiPart() {
		switch(selectedGame) {
		case "Hex": 
			scaPar0_L.setText("Board Size");
			scaPar1_L.setText("");
			scaPar2_L.setText("");
			setScaPar0List(new int[]{2,3,4,5,6,7,8});
			setScaPar1List(new int[]{});
			setScaPar2List(new int[]{});
			choiceScaPar0.setSelectedItem("6");		// the initial (recommended) value	
			break;
		case "Nim": 
		case "Nim3P":
			scaPar0_L.setText("Heaps");
			scaPar1_L.setText("Heap Size");
			setScaPar0List(new int[]{2,3,4,5});				// int values are converted to 
			setScaPar1List(new int[]{3,5,6,7,8,9,10,20,50});// Strings in ChoiceBoxes 
			choiceScaPar0.setSelectedItem("3");		// 
			choiceScaPar1.setSelectedItem("5");		// the initial (recommended) values
			if (selectedGame.equals("Nim")) {
				scaPar2_L.setText("Max Minus");
				setScaPar2List(new int[]{2,3,4,5});			
				choiceScaPar2.setSelectedItem("5");					
			} else { // i.e. "Nim3P"
				scaPar2_L.setText("Extra Rule");
				setScaPar2List(new String[]{"true","false"});
				choiceScaPar2.setSelectedItem("true");					
			}
			break;
		case "Sim": 
			scaPar0_L.setText("Players");
			scaPar1_L.setText("Nodes");
			scaPar2_L.setText("Coalition");
			scaPar0_L.setToolTipText("Number of players");
			scaPar1_L.setToolTipText("Number of nodes in Sim graph");
			scaPar2_L.setToolTipText("'1-2': coalition of player 1+2 against player 0 (only 3-player)");
			setScaPar0List(new int[]{2,3});
			setScaPar1List(new int[]{4,5,6,7,8,9,10,12,15,20});
			setScaPar2List(new String[]{"None","1-2"});
			choiceScaPar0.setSelectedItem("2");		// 
			choiceScaPar1.setSelectedItem("6");		// the initial (recommended) values 
			choiceScaPar2.setSelectedItem("None");	// 
			break;
		case "RubiksCube": 
			scaPar0_L.setText("Cube Type");
			scaPar1_L.setText("Board Vec");
			scaPar2_L.setText("Twist Type");
			scaPar0_L.setToolTipText("Pocket Cube or Rubik's Cube");
			scaPar1_L.setToolTipText("Type of board vectors (only n-tuple agents)");
			scaPar2_L.setToolTipText("<html>Type of twists: half-turn metric (HTM) <br>" +
					"or quarter-turn metric (QTM)</html>");
			//setScaPar0List(new int[]{});
			//setScaPar1List(new int[]{});
			//setScaPar2List(new int[]{});
			setScaPar0List(new String[]{"2x2x2","3x3x3"});
			setScaPar1List(new String[]{"CSTATE","CPLUS","STICKER","STICKER2"});
			setScaPar2List(new String[]{"HTM","QTM"});
			//choiceScaPar0.addItem("3x3x3");
			choiceScaPar0.setSelectedItem("2x2x2");			//
			choiceScaPar1.setSelectedItem("STICKER2");		// the initial (recommended) values
			choiceScaPar2.setSelectedItem("QTM");			//
			break;
		case "EWN":
			scaPar0_L.setText("Settings:");
			setScaPar0List(new String[]{"3x3 2-Player", "5x5 2-Player","4x4 2-Player","6x6 3-Player", "4x4 4-Player","6x6 4-Player"});
			choiceScaPar0.setSelectedItem("3x3 2-Player");
			scaPar1_L.setText("Position values:");
			setScaPar1List(new String[]{"[0,..,n]", "[0,1],[2,3],[4,5]"});
			choiceScaPar1.setSelectedItem("[0,..,n]");
			scaPar2_L.setText("Random starting:");
			setScaPar2List(new String[]{"True", "False"});
			choiceScaPar2.setSelectedItem("False");
			break;
		case "2048":
		case "Blackjack":
		case "ConnectFour":
		case "Othello":
		case "Poker":
		case "KuhnPoker":
		case "TicTacToe":
			//
			// games with no scalable parameters
			//
			scaPar0_L.setText("");
			scaPar1_L.setText("");
			scaPar2_L.setText("");
			setScaPar0List(new int[]{});
			setScaPar1List(new int[]{});
			setScaPar2List(new int[]{});
			break;
		case "Yavalath":
			scaPar0_L.setText("Players");
			scaPar1_L.setText("Board Size");
			setScaPar0List(new String[]{ "2","3"});
			setScaPar1List(new String[]{ "3","4","5","6"});
			choiceScaPar0.setSelectedItem("2");
			choiceScaPar1.setSelectedItem("5");
			scaPar0_L.setToolTipText("Number of players");
			scaPar1_L.setToolTipText("Size of the board game (Meaning length of its edge)");
			break;
		default: 
			System.err.println("[GBGLaunch] "+selectedGame+": This game is unknown.");
			System.exit(1);
		}
		
	}
	
	public void setScaPar0List(int[] modeList) {
		choiceScaPar0.removeAllItems();
		for (int i : modeList)
			choiceScaPar0.addItem(Integer.toString(i));
	}

	public void setScaPar1List(int[] modeList) {
		try {
			choiceScaPar1.removeAllItems();
		} catch (NumberFormatException e) {
			// this avoids a strange bug: When selecting game Sim, and then game Nim, a NumberFormatException fires
			// when removeAllItems is executed, unclear why. Strange enough, this happens ONLY for game Nim not for 
			// the nearly identically Nim3P or any other game. We cure this bug, which is internal to JComboBox, by
			// catching the NumberFormatException silently.
		}
		for (int i : modeList)
			choiceScaPar1.addItem(Integer.toString(i));
	}

	public void setScaPar2List(int[] modeList) {
		choiceScaPar2.removeAllItems();
		for (int i : modeList)
			choiceScaPar2.addItem(Integer.toString(i));
	}

	public void setScaPar0List(String[] modeList) {
		choiceScaPar0.removeAllItems();
		for (String s : modeList)
			choiceScaPar0.addItem(s);
	}

	public void setScaPar1List(String[] modeList) {
		choiceScaPar1.removeAllItems();
		for (String s : modeList)
			choiceScaPar1.addItem(s);
	}

	public void setScaPar2List(String[] modeList) {
		choiceScaPar2.removeAllItems();
		for (String s : modeList)
			choiceScaPar2.addItem(s);
	}

	//
	// *TODO*
	//

	public void setScaPar0Tooltip(String str) { choiceScaPar0.setToolTipText(str); }

	public void setScaPar1Tooltip(String str) {
		choiceScaPar1.setToolTipText(str);
	}

	public void setScaPar2Tooltip(String str) {
		choiceScaPar2.setToolTipText(str);
	}

}
