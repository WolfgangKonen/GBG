package games.Othello;

import java.util.ArrayList;
import java.util.Arrays;

import games.ObserverBase;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.WINNER;

public class StateObserverOthello extends ObserverBase{

	/**
	 * Class {@link StateObserverOthello} holds any valid Othello game state. It's coded
	 * in a two dimensional int[8][8] array, where each index represents either 
	 * an empty cell = 0,
	 * an White cell = 1,
	 * an black cell = -1
	 * <pre>
	 * 	For example the starting state:
	 * 												row
	 * 			0	0	0	0	0	0	0	0   	0
	 * 			0	0	0	0	0	0	0	0		1
	 * 			0	0	0	0	0	0	0	0		2
	 * 			0	0	0	0	0	0	0	0 		3
	 * 			0	0	0	0	0	0	0	0		4
	 * 			0	0	0	0	0	0	0	0		5	
	 * 			0	0	0	0	0	0	0	0		6
	 * 			0	0	0	0	0	0	0	0		7
	 *
	 *  	col	0	1	2	3	4	5	6	7	
	 */
	
	public static final long serialVersionUID = 12L;
	private static final double REWARD_NEGATIVE = -1, REWARD_POSITIVE = 1;
	
	private int[][] currentGameState;
	private int playerNextMove;
	private ArrayList<ACTIONS> availableActions = new ArrayList<ACTIONS>();
	private boolean isGameOver;
	
	public StateObserverOthello()
	{
		currentGameState= new int[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE];
		currentGameState[3][3] = 1;
		currentGameState[3][4] = BaseOthello.getOpponent(1);
		currentGameState[4][3] = BaseOthello.getOpponent(1);
		currentGameState[4][4] = 1;
//		playerNextMove = 2;
		playerNextMove = getOpponent(1);	// /WK/ the correct choice
		setAvailableActions();
	}
	
	public StateObserverOthello(int[][] gameState, int playerMove)
	{
		currentGameState= new int[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE];
		playerNextMove = playerMove;
		BaseOthello.deepCopyGameState(gameState, currentGameState);
		setAvailableActions();
	}
	
	public ArrayList<ACTIONS> getAllAvailableActions(){
		ArrayList<ACTIONS> retVal = new ArrayList<>();
		for(int i = 0, n = 0; i < currentGameState.length; i++) {
			for(int j = 0; j < currentGameState[0].length; j++,n++)
			{
				if(n != 27 && n != 28 && n != 35 && n != 36) retVal.add(new ACTIONS(n));
			}
		}
		return retVal;
	}
	
	@Override
	public StateObservation copy() {
		return new StateObserverOthello(currentGameState,playerNextMove);
	}
	
	/**
	 * @param return a boolean whether the game has no possible actions for either player.
	 */
	@Override
	public boolean isGameOver() {
		if(availableActions.size() == 0) {
			playerNextMove = getOpponent(playerNextMove); // Used for passing a turn
			setAvailableActions();
			if(availableActions.size() == 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isDeterministicGame() {
		return true;
	}

	@Override
	public boolean isFinalRewardGame() {
		return true;
	}

	@Override
	public boolean isLegalState() {
		return true;
	}

	@Override
	public WINNER getGameWinner() {
		assert isGameOver() :"Game isn't over";
		int countPlayer = 0, countOpponent = 0;
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++)
		{
			for( int j = 0; j < ConfigOthello.BOARD_SIZE; j++)
			{
				if(currentGameState[i][j] == playerNextMove) countPlayer++;
				if(currentGameState[i][j] == (getOpponent(playerNextMove))) countOpponent++;
			}
		}
		if(countPlayer > countOpponent) return WINNER.PLAYER_WINS;
		else if(countPlayer == countOpponent) return WINNER.TIE;
		return WINNER.PLAYER_LOSES;
	}

	@Override
	public double getMinGameScore() {
//		return 0;
		return -1;			// WK the right choice
	}

	@Override
	public double getMaxGameScore() {
//		return 64;
		return +1;			// WK the right choice
	}

	@Override
	public String getName() {
		return "Othello";
	}

	/**
	 *
	 * @return the afterState for deterministic games is the same.
	 */
	@Override
	public StateObservation getPrecedingAfterstate() {
			return this;
	}

	@Override
	public int getNumAvailableActions() {
		return availableActions.size();
	}

	/**
	 * Called by advance();
	 * updating the ArrayList, which contains all possible actions for the actual 
	 * players move.
	 */
	@Override
	public void setAvailableActions() {
		availableActions.clear();
		availableActions = BaseOthello.possibleActions(currentGameState, playerNextMove);
	}
	/**
	 * 
	 * @param i  	index of an action in availableActions. 
	 * @return 		ACTION
	 */
	@Override
	public ACTIONS getAction(int i) {
		return availableActions.get(i);
	}

	@Override
	public ArrayList<ACTIONS> getAvailableActions() {
		return availableActions;
	}

	/**
	 * Check for legal Action if the requested Actions is available 
	 * to the current game state.
	 * @param act
	 * @return boolean
	 */
	public boolean isLegalAction(ACTIONS act)
	{
		return availableActions.contains(act) ? true : false;
	}
	
	/**
	 * Changing the game state with a valid Action
	 * updating the availableActions
	 * checking if one player has to pass this turn.
	 */
	@Override
	public void advance(ACTIONS action) {
		int iAction = action.toInt();
		int j = iAction % ConfigOthello.BOARD_SIZE;
		int i = (iAction-j) / ConfigOthello.BOARD_SIZE;
		BaseOthello.flip(currentGameState, i, j, playerNextMove);
		currentGameState[i][j] = playerNextMove;
		playerNextMove = getOpponent(playerNextMove);
		setAvailableActions();
		super.incrementMoveCounter();
		if(availableActions.size() == 0) {
			playerNextMove = getOpponent(playerNextMove); // Used for passing a turn
			setAvailableActions();
		}
	}

	/**
	* TODO: Determine if this logic is correct
	*/
	@Override
	public int getPlayer() {
		return playerNextMove == getOpponent(1) ? 0 : 1;
	}

	@Override
	public int getNumPlayers() {
		return 2;
	}

	/**
	 * Used to calculate the score for the current game state
	 */
	@Override
	public double getGameScore(StateObservation referringState) {
//		int retVal = (referringState.getPlayer() == this.playerNextMove) ? 1 : (2);
		int retVal = (referringState.getPlayer() == this.playerNextMove) ? 1 :(-1); // WK: probably the right choice
		StateObserverOthello so = (StateObserverOthello)referringState;
//		if(BaseOthello.isGameOver(so.getCurrentGameState())) {			// WRONG!!!
		if(BaseOthello.isGameOver(this.getCurrentGameState())) {		// WK bug fix		
			Types.WINNER win = this.getGameWinner();
			switch(win) {
			case PLAYER_LOSES:
				return retVal * REWARD_NEGATIVE;
			case PLAYER_WINS:
				return retVal * REWARD_POSITIVE;
			case TIE:
				return 0.0;
			default:
				throw new RuntimeException("Wrong enum");
			}
		}
		return 0.0;
	}

	@Override
	public String stringDescr() {
		String sout = "";
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++) {
			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++) {
				sout += (currentGameState[i][j] == BaseOthello.getOpponent(1)) ? "O" : (currentGameState[i][j] == +1) ? "X": "-";
			}
		}
		return sout;
	}

	public void toString2() {
		
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++) {
			System.out.println("");
			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++) {
				System.out.printf( Integer.toString(currentGameState[i][j]) +" ");
			}
		}
	}
	
	public int[][] getCurrentGameState(){return currentGameState;}
	
	/**
	 * Helper Method
	 * @param i	index on current game state.
	 * @param j index on current game state.
	 * @return the game state value either EMPTY, BLACK, WHITE as String.
	 */
	public String getCurrentGameState(int i, int j)
	{
		return currentGameState[i][j] == 0 ? "Empty" : currentGameState[i][j]  == 1 ? "White" : "Black";
	}
	
	public void setPlayer(int p) {
		this.playerNextMove = p;
	}
	
	private int getOpponent(int player)
	{
		return BaseOthello.getOpponent(player);
	}
}
