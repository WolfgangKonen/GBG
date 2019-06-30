package games.Othello;

import java.util.ArrayList;
import java.util.Arrays;

import games.ObserverBase;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.WINNER;
/**
 * Class {@link StateObserverOthello} holds any valid Othello game state. It's coded
 * in a two dimensional int[8][8] array, where each index represents either 
 * an empty cell = 2,
 * an White cell = 1,
 * an black cell = 0
 * <pre>
 * the black starts the game.
 * 	For example the starting state:
 * 												row
 * 			2	2	2	2	2	2	2	2   	0
 * 			2	2	2	2	2	2	2	2		1
 * 			2	2	2	2	3	2	2	2		2
 * 			2	2	2	1	0	3	2	2 		3
 * 			2	2	3	0	1	2	2	2		4
 * 			2	2	2	3	2	2	2	2		5	
 * 			2	2	2	2	2	2	2	2		6
 * 			2	2	2	2	2	2	2	2		7
 *
 *  	col	0	1	2	3	4	5	6	7	
 */
public class StateObserverOthello extends ObserverBase{

	
	
	public static final long serialVersionID = 12L;
	private static final double REWARD_NEGATIVE = -1, REWARD_POSITIVE = 1;
	
	private int[][] currentGameState;
	private int playerNextMove, countBlack, countWhite;
	private ArrayList<ACTIONS> availableActions = new ArrayList<ACTIONS>();
	private ArrayList<Integer> lastMoves;
	private int turn;
	
	public StateObserverOthello()
	{
		lastMoves = new ArrayList<Integer>();
		currentGameState= new int[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE];
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++)
			{
				currentGameState[i][j] = ConfigOthello.EMPTY;
			}
		}
		currentGameState[3][3] = 1;
		currentGameState[3][4] = BaseOthello.getOpponent(1);
		currentGameState[4][3] = BaseOthello.getOpponent(1);
		currentGameState[4][4] = 1;
//		BaseOthello.deepCopyGameState(ConfigOthello.DEBUG[2], currentGameState);
		playerNextMove = getOpponent(1);	// /WK/ the correct choice
		countBlack = 2;
		countWhite = 2;
		turn = 0;
		
		setAvailableActions();
	}
	
	public StateObserverOthello(int[][] gameState, int playerMove, ArrayList<Integer> lastMoves, int turn)
	{
		this.lastMoves = new ArrayList<Integer>();
		this.lastMoves = lastMoves;
		this.currentGameState= new int[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE];
		this.playerNextMove = playerMove;
		BaseOthello.deepCopyGameState(gameState, currentGameState);
		this.turn = turn;
		this.setAvailableActions();
	}
	
	public StateObserverOthello(int[][] gameState, int playerMove)
	{
		this.lastMoves = new ArrayList<Integer>();
		currentGameState= new int[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE];
		playerNextMove = playerMove;
		BaseOthello.deepCopyGameState(gameState, currentGameState);
		setAvailableActions();
	}
	
	public ArrayList<ACTIONS> getAllAvailableActions(){
		ArrayList<ACTIONS> retVal = new ArrayList<>();
		for(int i = 0, n = 0; i < currentGameState.length; i++) {
			for(int j = 0; j < currentGameState[i].length; j++,n++)
			{
				if(n != 27 && n != 28 && n != 35 && n != 36) 	
				// WK 2019-06-15: 1st fix: comment the preceding line out as bug fix for Sarsa: 
				// Although the actions 27, 38, 35, 36 will never happen in an Othello game, we add them
				// here to retVal. This is necessary to let getAllAvailableActions() return a list with size
				// 64. Only then nTuples will be constructed in SarsaAgt with numOutputs=64. This in turn is 
				// necessary, because actions with minimum 0 and maximum 63 can occur. Otherwise we would
				// get a OutOfBoundException in NTuple2ValueFunc.getQFunc() whenever equivAction has an
				// element >= 60 (60 would be retVal's size if the preceding line were NOT comented out)
				//
				// WK 2019-06-23: 2nd fix: The bug fix above is no longer necessary, because we changed in XArenaFuncs
				// what is done with allAvailableActions: now it will infer minimum 0 and maximum 63 from the 
				// set of actions and set numOutputs to 63-0+1=64, although the ArrayList may contain only 
				// 60 actions. Thus, both ways will work: commenting out preceding line or not.
				retVal.add(new ACTIONS(n));
			}
		}
		return retVal;
	}
	
	@Override
	public StateObserverOthello copy() {
		StateObserverOthello so = new StateObserverOthello(this.currentGameState, this.playerNextMove,this.lastMoves,  turn);
		return so;
	}
	
	/**
	 * @param return a boolean whether the game has no possible actions for any player.
	 */
	@Override
	public boolean isGameOver() {
		return (BaseOthello.possibleActions(currentGameState, playerNextMove).size() == 0 ) &&
				(BaseOthello.possibleActions(currentGameState, this.getOpponent(playerNextMove)).size() == 0);
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

	/**
	 * Determines the winner after the game is over by counting each players discs
	 */
	@Override
	public WINNER getGameWinner() {
		assert isGameOver() :"Game isn't over";
		int countPlayer = 0, countOpponent = 0;
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++)
		{
			for( int j = 0; j < ConfigOthello.BOARD_SIZE; j++)
			{
				if(currentGameState[i][j] == this.getPlayer()) countPlayer++;
				if(currentGameState[i][j] == (this.getOpponent(playerNextMove))) countOpponent++;
			}
		}
		if(countPlayer > countOpponent) return WINNER.PLAYER_WINS;
		else if(countPlayer == countOpponent) return WINNER.TIE;
		return WINNER.PLAYER_LOSES;
	}

	@Override
	public double getMinGameScore() {
		return -1;			// WK the right choice
	}

	@Override
	public double getMaxGameScore() {
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
		super.incrementMoveCounter();
		
		// Set playerNextMove.
		// The normal case: if the opponent of playerNextMove (the player who just advanced) has possible
		// actions, then playerNextMove will become this opponent. If however the opponent has no possible
		// moves, he has to pass, and playerNextMove will stay at the value it has (and the next advance
		// will be done by the same playerNextMove):
		if(BaseOthello.possibleActions(currentGameState, 
				this.getOpponent(playerNextMove)).size() != 0 ) {
			playerNextMove = getOpponent(playerNextMove); 
		}
		
		setAvailableActions();
		lastMoves.add(action.toInt());
		turn++;
	}

	/**
	* TODO: Determine if this logic is correct
	*/
	@Override
	public int getPlayer() {
		return playerNextMove;
	}

	@Override
	public int getNumPlayers() {
		return 2;
	}

	public int getLastMove() {
		if (lastMoves.size() == 0) return -1;
		return lastMoves.get(lastMoves.size()-1);
	}
	
	public void resetLastMoves() {
		this.lastMoves = new ArrayList<Integer>();		
	}
	
	
	/**
	 * Used to calculate the score for the current game state
	 */
	@Override
	public double getGameScore(StateObservation referringState) {
		int retVal = (referringState.getPlayer() == this.playerNextMove) ? 1 :(-1); // WK: probably the right choice
		if(this.isGameOver()) {		// 	Working correctly now	
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
	
	// WK obsolete (never used) and probably dangerous (may create invalid game states) 
	public void setPlayer(int p) {
		this.playerNextMove = p;
	}
	
	public int getOpponent(int player)
	{
		return BaseOthello.getOpponent(player);
	}
	
	public int getCountWhite() { return countWhite;}
	public int getCountBlack() { return countBlack;}
	public void setCountWhite(int w) {countWhite = w;}
	public void setCountBlack(int b) {countBlack = b;}
	public int getTurn() { return turn; }
}