package games.Othello;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import games.Othello.Gui.Gamestats;
import games.Othello.Gui.Legend;
import games.Othello.Gui.Tile;
import tools.Types;


public class GameBoardOthello extends JFrame implements GameBoard {

	/**
	 * SerialNumber
	 */
	private static final long serialVersionUID = 12L;
	
	/**
	 * Game Attributes
	 */
	public Arena m_Arena;
	private StateObserverOthello m_so;
	private int[][] gameState;  // 1 = White   2 = Black
	private double[][] vGameState;
	private boolean arenaActReq = false;
	protected Random rand;
	
	
	private Gamestats gameStats; // Displaying Game information
	private Legend legend;
	private JPanel boardPanel; 	// Container for the 8 x 8 game board
	private Tile[][] board;		// representation of the game board
	
	private int counterWhite, counterBlack;

	
	public GameBoardOthello()
	{
	}
	
	public GameBoardOthello(Arena arena)
	{
		super("Othello");
		initGameBoard(arena);
		setSize((int)Types.GUI_SCALING_FACTOR_X * 1000,(int) Types.GUI_SCALING_FACTOR_Y  * 1000);
		setBackground(Types.GUI_BGCOLOR);
		pack();
		setVisible(true);
	}
	
	/**
	 * Initialising the game board using {@code initBoard()} and other game relevant information
	 * @param arena
	 */
	public void initGameBoard(Arena arena)
	{
		// Initializing necessary elements
		m_Arena = arena;
		board = new Tile[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE];
		gameState = new int[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE];
		vGameState = new double[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE];
		m_so = new StateObserverOthello();
		rand = new Random();
		// Northern display of the JFrame containing the game stats
		gameStats = new Gamestats();
		add(gameStats, BorderLayout.NORTH);
		// Center display of the JFrame containing the game board
		boardPanel = initBoard();
		add(boardPanel, BorderLayout.CENTER);
		// Southern display of the JFrame containing the legend
		legend = new Legend();
		add(legend, BorderLayout.SOUTH);
		
		
		updateBoard(m_so, false, true);
	}
	
	/**
	 * Initialising the displaying representation of the game board.
	 * @return JPanel containing the 8x8 grid {@link Tile} stored in {@code board}
	 */
	private JPanel initBoard()
	{
		JPanel retVal = new JPanel();
		retVal.setLayout(new GridLayout(ConfigOthello.BOARD_SIZE,ConfigOthello.BOARD_SIZE,1,1));
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++){
			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++){
				board[i][j] = new Tile(this,i,j);
				retVal.add(board[i][j]);
			}
		}
		updateCells(true);
		return retVal;
	}
	
	
	@Override
	public void initialize() {}

	/**
	 * Resets the game to it starting state {@link StateObserverOthello}
	 */
	@Override
	public void clearBoard(boolean boardClear, boolean vClear) {
		if(boardClear) {
			m_so = new StateObserverOthello();
		}
		if(vClear) {
			vGameState = new double[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE];
			for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++){
				for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++){
					vGameState[i][j] = Double.NaN;
				}
			}
		}
		updateCells(false);
	}

	/**
	 * Using the following helper methods:<ol>
	 * <li> {@link #updatePlayersMove(int)} to update the label from {@link Gamestats#setGameWinner(int)} </li>
	 * </ol>
	 * -- updatePlayersMove
	 * -- update Count
	 */
	@Override
	public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard) {
		if(so != null) {
			assert ( so instanceof StateObserverOthello) : "so is not an instance of StateOberverOthello";
			StateObserverOthello sot = ((StateObserverOthello) so);
			m_so = sot;
			updateCells(showValueOnGameboard);
			int player=Types.PLAYER_PM[m_so.getPlayer()];
			updatePlayersMove(player);
			m_so.setCountBlack(counterBlack);
			m_so.setCountWhite(counterWhite);
			updateGameStats(m_so.getCountBlack(),m_so.getCountWhite(),m_so.getTurn());
			if(m_so.isGameOver()) 
			{
				if(m_so.getCountBlack() > m_so.getCountWhite()) {
					gameStats.setGameWinner("Winner: Black");
				}else if(m_so.getCountBlack() == m_so.getCountWhite()) {
					gameStats.setGameWinner("Tie");
				}else {
					gameStats.setGameWinner("Winner: Black");
				}
			}
			
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
		}
		//updateCells(showValueOnGameboard);
	}
	
	private void updatePlayersMove(int player) {
		switch(player) {
		case(1):
			gameStats.changeNextMove("Next move: Black");
			break;
		case(-1): 
			gameStats.changeNextMove("Next move: White");
			break;
		}
	}
	
	/**
	 * Updating the {@code Gamestats}
	 * @param blackDiscs	Amount of black discs placed on the board
	 * @param whiteDiscs	Amount of white discs placed on the board
	 * @param turnCount		turn counter range 1-60
	 */
	private void updateGameStats(int blackDiscs, int whiteDiscs, int turnCount) {
		gameStats.setWhiteCount(whiteDiscs);
		gameStats.setBlackCount(blackDiscs);
		gameStats.setTurnCount(turnCount);
	}
	
	
	
	
	

	/**
	 * Updating the cell's color and text.
	 * Updating the tokenCounters for both player.
	 */
	private void updateCells(boolean showValueOnGameboard)
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
				board[i][j].setBorder((m_so.getLastMove() == (i * ConfigOthello.BOARD_SIZE + j)));
				if(m_so.getCurrentGameState()[i][j] == 1) {
					
					board[i][j].setBackground(Color.WHITE);
					counterWhite++;
				}
				else if(m_so.getCurrentGameState()[i][j] == BaseOthello.getOpponent(1)) {
					board[i][j].setBackground(Color.BLACK);
					counterBlack++;
				}
				else {
					board[i][j].markAsPossiblePlacement(
							m_so.getAvailableActions().contains(
									new Types.ACTIONS(i*ConfigOthello.BOARD_SIZE + j)));
					board[i][j].setForeground(Color.RED);
					if(showValueOnGameboard) board[i][j].setText(valueText);
				}
			}
		}
	}
	
	/**
	 * Human places a disc on board[x][y]
	 * @param x	index for board[x][]
	 * @param y index for board[][y]
	 */
	public void hGameMove(int x, int y) {
		int iAction = ConfigOthello.BOARD_SIZE * x + y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		if( m_so.isLegalAction(act)) {			
			m_so.advance(act);
			(m_Arena.getLogManager()).addLogEntry(act, m_so, m_Arena.getLogSessionID());
			arenaActReq = true;	
			updateBoard(m_so, false, false);
		}
		else {
			System.out.println("Not Allowed: illegal Action");
		}
	}
	
	public void inspectMove(int x, int y)
	{
		int iAction = ConfigOthello.BOARD_SIZE * x + y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		if(m_so.isLegalAction(act)) {
			m_Arena.setStatusMessage("Inspecting the value function ...");
			m_so.advance(act);
		}else m_Arena.setStatusMessage("Desired Action is not legal");
		arenaActReq = true;
	}
	
	
	@Override
	public void showGameBoard(Arena arena, boolean alignToMain) {
		this.setVisible(true);
	}

	@Override
	public void toFront() {
	}

	@Override
	public boolean isActionReq() {
		return arenaActReq;
	}

	@Override
	public void setActionReq(boolean actionReq) {
		arenaActReq=actionReq;
	}

	@Override
	public void enableInteraction(boolean enable) {
	}

	@Override
	public StateObservation getStateObs() {
		return m_so;
	}

	@Override
	public String getSubDir() {
		return null;
	}

	@Override
	public Arena getArena() {
		return m_Arena;
	}

	@Override
	public StateObservation getDefaultStartState() {
		clearBoard(true,true);
		return m_so;
	}

	@Override
	public StateObservation chooseStartState(PlayAgent pa) {
		return chooseStartState();
	}

	@Override
	public StateObservation chooseStartState() {
		clearBoard(true,true);
		return m_so;
	}
	
	
}