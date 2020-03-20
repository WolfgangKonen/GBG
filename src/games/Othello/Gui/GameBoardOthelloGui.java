package games.Othello.Gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controllers.PlayAgent;
import games.Arena;
import games.GameBoard;
import games.StateObservation;
import games.Nim.GameBoardNim;
import games.Othello.ConfigOthello;
import games.Othello.GameBoardOthello;
import games.Othello.StateObserverOthello;
import games.Othello.Gui.GameStats;
import games.Othello.Gui.Legend;
import games.Othello.Gui.Tile;
import tools.Types;


/**
 * Class GameBoardOthelloGui has the board game GUI. 
 * <p>
 * It shows board game states, optionally the values of possible next actions,
 * and allows user interactions with the board to enter legal moves during 
 * game play or to enter board positions for which the agent reaction is 
 * inspected. 
 * 
 * @author Julian Coeln, Yannick Dittmar, TH Koeln, 2019
 */
public class GameBoardOthelloGui extends JFrame {

	/**
	 * SerialNumber
	 */
	private static final long serialVersionUID = 12L;
	
	/**
	 * a reference to the 'parent' {@link GameBoardOthello} object
	 */
	private GameBoardOthello m_gb=null;
	
//	private int[][] gameState;  // 1 = White   2 = Black // never used
	private double[][] vGameState;	
	private GameStats gameStats; 	// Displaying Game information
	private Legend legend;
	private JPanel boardPanel; 		// Container for the 8 x 8 game board
	private Tile[][] board;			// representation of the game board
	
	private int counterWhite, counterBlack;
	
	public GameBoardOthelloGui(GameBoardOthello gb)
	{
		super("Othello");
		m_gb = gb;
		initGameBoard("");
	}
	
	/**
	 * Initialize the game board gui using {@link #initBoard()} and other game relevant information
	 * @param title
	 */
	public void initGameBoard(String title)
	{
		// Initializing necessary elements
		board = new Tile[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE];
//		gameState = new int[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE]; // never used
		vGameState = new double[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE];
		// Northern display of the JFrame containing the game stats
		gameStats = new GameStats();
		add(gameStats, BorderLayout.NORTH);
		// Center display of the JFrame containing the game board
		boardPanel = initBoard();
		add(boardPanel, BorderLayout.CENTER);
		// Southern display of the JFrame containing the legend
		legend = new Legend();
		add(legend, BorderLayout.SOUTH);
		
		setSize((int)Types.GUI_SCALING_FACTOR_X * 1000,(int) Types.GUI_SCALING_FACTOR_Y  * 1000);
		setBackground(Types.GUI_BGCOLOR);
		pack();
		setVisible(false);
	}
	
	/**
	 * Initializing the board representation.
	 * @return JPanel containing the 8x8 grid {@link Tile} stored in {@code board}
	 */
	private JPanel initBoard()
	{
		JPanel retVal = new JPanel();
		retVal.setLayout(new GridLayout(ConfigOthello.BOARD_SIZE,ConfigOthello.BOARD_SIZE,1,1));
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++){
			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++){
				board[i][j] = new Tile(m_gb,i,j);
				retVal.add(board[i][j]);
			}
		}
		return retVal;
	}
	
	
	/**
	 * Resets the game to it starting state {@link StateObserverOthello}
	 */
	public void clearBoard(boolean boardClear, boolean vClear) {
		if(boardClear) {
//			m_so = new StateObserverOthello();
		}
		if(vClear) {
			vGameState = new double[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE];
			for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++){
				for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++){
					vGameState[i][j] = Double.NaN;
				}
			}
		}
		updateBoard(m_gb.getStateObs(),false,true);
	}

	/**
	 * Using the following helper methods to update the user interface
	 * <ol>
	 * <li> {@link #updateGameStats(int, int, int, int)} to update all turn based label </li>
	 * </ol>
	 */
	public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard) {
		if(so != null) {
			assert ( so instanceof StateObserverOthello) : "so is not an instance of StateOberverOthello";
			StateObserverOthello sot = (StateObserverOthello) so;
			sot.setCountBlack(counterBlack);
			sot.setCountWhite(counterWhite);
			
			updateGameStats(counterBlack,counterWhite,sot.getTurn(),sot.getPlayer());
		
			
			if(showValueOnGameboard && sot.getStoredValues() != null) {
				for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++)
					for( int j = 0; j < ConfigOthello.BOARD_SIZE; j++)
						vGameState[i][j] = Double.NaN; 
						
				for(int y = 0 ; y < sot.getStoredValues().length; y++)
				{
					Types.ACTIONS action = sot.getStoredAction(y);
					int iAction = action.toInt();
					int jFirst= iAction%ConfigOthello.BOARD_SIZE;
					int iFirst= (iAction-jFirst)/ConfigOthello.BOARD_SIZE;
					vGameState[iFirst][jFirst] = sot.getStoredValues()[y];
				}
			}
			updateCells(sot,showValueOnGameboard);
		}
	}
	
	
	/**
	 * Updating the {@link GameStats} object.
	 * @param blackDiscs	Amount of black discs placed on the board
	 * @param whiteDiscs	Amount of white discs placed on the board
	 * @param turnCount		turn counter range 1-60
	 * @param player		player who has to make the next move
	 */
	private void updateGameStats(int blackDiscs, int whiteDiscs, int turnCount, int player) {
		gameStats.setWhiteCount(whiteDiscs);
		gameStats.setBlackCount(blackDiscs);
		gameStats.setTurnCount(turnCount);
		switch(player) {
		case(0):
			gameStats.changeNextMove("Next move: Black");
			break;
		case(1): 
			gameStats.changeNextMove("Next move: White");
			break;
		}
	}

	/**
	 * Updating the cell's color and text.
	 * Updating the DiscCounters for both player.
	 */
	private void updateCells(StateObserverOthello sot, boolean showValueOnGameboard)
	{
		
		double value, maxValue= Double.NEGATIVE_INFINITY;
		int maxI = 0, maxJ = 0;
		String valueText;
		counterBlack = 0;
		counterWhite = 0;
		for(int i = 0; i < board.length; i++)
		{
			for(int j = 0; j < board[i].length; j++)
			{
				//Not necessary
				if(vGameState==null) {
					value = Double.NaN;
				}else {
					value = vGameState[i][j];
				}
				if (Double.isNaN(value)) {
					valueText = "   ";
				} else {
					valueText = " "+(int)(value*100);
					if (value<0) valueText = ""+(int)(-value *100);
					if (value>maxValue) {
						maxValue=value;
						maxI=i;
						maxJ=j;
					}
				}
				// Disable every button.
				board[i][j].setEnabled(false);
				board[i][j].setForeground(Color.RED);
				board[i][j].setBackground(ConfigOthello.BOARDCOLOR);
				board[i][j].setBorder((sot.getLastMove() == (i * ConfigOthello.BOARD_SIZE + j)));
				board[i][j].setText("");
				if(sot.getCurrentGameState()[i][j] == ConfigOthello.WHITE) {
					board[i][j].setBackground(Color.WHITE);
					board[i][j].setText("");
					counterWhite++;
				}
				else if(sot.getCurrentGameState()[i][j] == ConfigOthello.BLACK) {
					board[i][j].setBackground(Color.BLACK);
					board[i][j].setText("");
					counterBlack++;
				}
				else {
					// Enable buttons, which are valid for a move
					board[i][j].markAsPossiblePlacement(
							sot.getAvailableActions().contains(
									new Types.ACTIONS(i*ConfigOthello.BOARD_SIZE + j)));
					if(showValueOnGameboard) board[i][j].setText(valueText);
				}
				
			}
		}
		this.repaint();
	}
	
	public void showGameBoard(Arena arena, boolean alignToMain) {
		this.setVisible(true);
		if (alignToMain) {
			// place window with game board to the left of the main window
			int x = arena.m_ArenaFrame.getX() + arena.m_xab.getWidth() + 18;
			int y = arena.m_ArenaFrame.getY();
			this.setLocation(x,y);	
		}		
	}

	public void toFront() {
		super.setState(JFrame.NORMAL);
		super.toFront();
	}

	public void enableInteraction(boolean enable) {	}

	public void destroy() {
		this.setVisible(false);
		this.dispose();
	}

	
}