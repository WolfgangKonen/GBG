package games.Othello;

import games.ObserverBase;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.WINNER;

import java.util.ArrayList;
/**
 * This class holds valid Othello game state objects. It is coded
 * as a two-dimensional int[8][8] array, where each index represents either
 * <ul>
 * <li>a  Black cell ("O") = 0,
 * <li>a  White cell ("X") = 1,
 * <li>an empty cell = 2,
 * <li>a  reachable empty cell = 3.
 * </ul>
 * This last case "3" is for {@link XNTupleFuncsOthello#getNumPositionValues()}{@code =4}: Every empty 
 * cell which can be taken by the player to move is coded with "3" instead of "2".
 * <p>
 * Black ("O") starts the game.<br>
 * For example the starting state is coded in case {@link XNTupleFuncsOthello#getNumPositionValues()}{@code =4} as:
 * <pre>
 *                                            row
 *         2   2   2   2   2   2   2   2       0
 *         2   2   2   2   2   2   2   2       1
 *         2   2   2   3   2   2   2   2       2
 *         2   2   2   1   0   2   2   2       3
 *         2   2   3   0   1   3   2   2       4
 *         2   2   2   2   3   2   2   2       5
 *         2   2   2   2   2   2   2   2       6
 *         2   2   2   2   2   2   2   2       7
 *
 *     col 0   1   2   3   4   5   6   7
 *  </pre>
 *  The action number for board cell {@code (i,j)} is given by {@code 8*i+j}.
 *  Thus, the possible action numbers are:
 * <pre>
 *                                            row
 *        00  01  02  03  04  05  06  07       0
 *        08  09  10  11  12  13  14  15       1
 *        16  17  18  19  20  21  22  23       2
 *        24  25  26  27  28  29  30  31       3
 *        32  33  34  35  36  37  38  39       4
 *        40  41  42  43  44  45  46  47       5
 *        48  49  50  51  52  53  54  55       6
 *        56  57  58  59  60  61  62  63       7
 *
 *     col 0   1   2   3   4   5   6   7
 *  </pre>
 */
public class StateObserverOthello extends ObserverBase{

	
	
	public static final long serialVersionUID = 12L;
	private static final double REWARD_NEGATIVE = -1, REWARD_POSITIVE = 1;
	
	protected int[][] currentGameState;
	protected int playerNextMove; 	// the player to move in the current state
	private int countBlack, countWhite;	// probably never really needed
	private ArrayList<ACTIONS> availableActions = new ArrayList<>();
//	public ArrayList<Integer> lastMoves;		// this is now in ObserverBase
//	private int turn;			// use super.getMoveCounter() instead
	
	public StateObserverOthello()
	{
		super();
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
//		turn = 0;
		
		setAvailableActions();
	}
	
	public StateObserverOthello(StateObserverOthello other)
	{
		super(other);		// copy members m_counter, lastMoves and stored*
		this.currentGameState= new int[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE];
		BaseOthello.deepCopyGameState(other.currentGameState, currentGameState);
		// /WK/ wouldn't "this.currentGameState = other.currentGameState.clone();" do the same as the preceding two lines? 
		//      No, that would not work, because it would not do a deep copy on int[][], it would only make a new int[8]
		//      and hang the lines [0],...,[7] from other.currentGameState into this. What would work really 'deep':
		// for (int i=0; i<ConfigOthello.BOARD_SIZE; i++) this.currentGameState[i] = other.currentGameState[i].clone(); 
		//      but this is not faster, even slightly slower than the above deepCopyGameState. 
		//		Anyhow, the real time-burner was 'this.setAvailableActions()' below, and we made the code 16x faster (!!)
		// 	 	by simply cloning other.availableActions instead 
		this.playerNextMove = other.playerNextMove;
		this.countBlack = other.countBlack;
		this.countWhite = other.countWhite;
//		this.turn = other.turn;
		if (other.availableActions!=null)	// this check is needed when loading older logs
			this.availableActions = (ArrayList<ACTIONS>) other.availableActions.clone();
					// Note that clone does only clone the ArrayList, but not the contained ACTIONS, they are 
					// just copied by reference. However, these ACTIONS are never altered, so it is o.k.
//		this.setAvailableActions();		// this as replacement for availableActions.clone() would be very slow!
	}
	
	// never used:
//	public StateObserverOthello(int[][] gameState, int playerMove)
//	{
//		this.lastMoves = new ArrayList<Integer>();
//		this.currentGameState= new int[ConfigOthello.BOARD_SIZE][ConfigOthello.BOARD_SIZE];
//		BaseOthello.deepCopyGameState(gameState, currentGameState);
//		// /WK/ wouldn't "this.currentGameState = gameState.clone();" do the same as the preceding two lines? 
//		playerNextMove = playerMove;
//		setAvailableActions();
//	}
	
    /**
     * Return all available actions (all actions that can ever become possible in this game)
     * @return {@code ArrayList<ACTIONS>}
     */
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
				// necessary, because actions with minimum 0 and maximum 63 can occur. Otherwise, we would
				// get a OutOfBoundException in NTuple2ValueFunc.getQFunc() whenever equivAction has an
				// element >= 60 (60 would be retVal's size if the preceding line were NOT commented out)
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
		return new StateObserverOthello(this);
	}
	
	/**
	 * @return a boolean whether the game has no possible actions for any player.
	 */
	@Override
	public boolean isGameOver() {
//		return (BaseOthello.possibleActions(currentGameState, playerNextMove).size() == 0 ) &&
//				(BaseOthello.possibleActions(currentGameState, getOpponent(playerNextMove)).size() == 0);
		// /WK/ this does the same as above, but should be faster (possibleActions is a costly method):
		if  (availableActions.size() == 0 ) 
			return (BaseOthello.possibleActions(currentGameState, getOpponent(playerNextMove)).size() == 0);
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

	/**
	 * Determines the winner after the game is over by counting each player's discs
	 */
	public WINNER winStatus() {
		assert isGameOver() :"Game isn't over";
		//assert this.getPlayer()==this.playerNextMove : "Oops, this.getPlayer() differs from playerNextMove!";
		int countPlayer = 0, countOpponent = 0;
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++)
		{
			for( int j = 0; j < ConfigOthello.BOARD_SIZE; j++)
			{
				if(currentGameState[i][j] == this.getPlayer()) countPlayer++;
				if(currentGameState[i][j] == (this.getOpponent(this.getPlayer()))) countOpponent++;
			}
		}
		if(countPlayer > countOpponent) return WINNER.PLAYER_WINS;
		else if(countPlayer == countOpponent) return WINNER.TIE;
		return WINNER.PLAYER_LOSES;
	}

	public void setPieceCounters() {
		this.countBlack=0;
		this.countWhite=0;
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++)
		{
			for( int j = 0; j < ConfigOthello.BOARD_SIZE; j++)
			{
				if(currentGameState[i][j] == 0) this.countBlack++;
				if(currentGameState[i][j] == 1) this.countWhite++;
			}
		}
	}
	
	@Override
	public double getMinGameScore() {
		return -1;			// WK the right choice
	}

	@Override
	public double getMaxGameScore() {
		return +1;			// WK the right choice
	}

	// --- obsolete, use Arena().getGameName() instead
//	public String getName() {return "Othello";}

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
	 * @param act		the action to test
	 * @return boolean  whether it is legal
	 */
	public boolean isLegalAction(ACTIONS act)
	{
		return availableActions.contains(act);
	}
	
	/**
	 * Change the game state with a valid action.
	 * Update the available actions.
	 * Check if one player has to pass.
	 */
	@Override
	public void advance(ACTIONS action) {
		int iAction = action.toInt();
		int j = iAction % ConfigOthello.BOARD_SIZE;
		int i = (iAction-j) / ConfigOthello.BOARD_SIZE;
		BaseOthello.flip(currentGameState, i, j, playerNextMove);
		currentGameState[i][j] = playerNextMove;
		int prevPlayer = playerNextMove;
		
		// Set playerNextMove.
		// The normal case: if the opponent of playerNextMove (playerNextMove is the player who just advanced) has 
		// possible actions, then playerNextMove will become this opponent. If however the opponent has no possible
		// moves, he has to pass, and playerNextMove will stay at the value it has (and the next advance
		// will be done by the same playerNextMove):
		availableActions = BaseOthello.possibleActions(currentGameState, this.getOpponent(playerNextMove));
		if(availableActions.size() > 0 ) {
			playerNextMove = getOpponent(playerNextMove);  // the normal case
		}

		if (playerNextMove==prevPlayer)
			setAvailableActions();	// yes, we have to call possibleActions (inside setAvailableActions) a 2nd time
									// in the rare cases where playerNextMove is identical prevPlayer (there was no 
									// possible next move for opponent AND the possible actions for prevPlayer have
									// changed).
									// In all other cases we can skip setAvailableActions: the member availableActions
									// calculated above is valid!
		super.addToLastMoves(action);
		super.incrementMoveCounter();
		this.setPieceCounters();	// /WK/2021-12/ added for safety and for logging
		//turn++;
		// // /WK/ If the following assertion never fires, we can abandon turn in favor of ObserverBase::moveCounter:
		//assert (turn==this.getMoveCounter()) : "Oops, turn="+turn+" and moveCounter="+this.getMoveCounter()+" differ!";
	}

	@Override
	public int getPlayer() {
		return playerNextMove;
	}

	@Override
	public int getNumPlayers() {
		return 2;
	}


	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state.
	 * 			For Othello only game-over states have a non-zero game score.
	 * 			It is the reward from the perspective of {@code player}.
	 */
	@Override
	public double getGameScore(int player) {
		int sign = (player == this.playerNextMove) ? 1 :(-1);
		if(this.isGameOver()) {		// 	Working correctly now	
			Types.WINNER win = this.winStatus();
			double res = 0.0;
			switch (win) {
				case PLAYER_LOSES:
				case PLAYER_DISQ: res = sign * REWARD_NEGATIVE; break;
				case PLAYER_WINS: res = sign * REWARD_POSITIVE; break;
				case TIE: res = 0.0; break;
			}
			return res;
		}
		return 0.0;
	}

	@Override
	public String stringDescr() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++) {
			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++) {
				sb.append( (currentGameState[i][j] == BaseOthello.getOpponent(1))
						? "O" : (currentGameState[i][j] == 1) ? "X": "-");
			}
		}
		return sb.toString();
	}

	public void toString2() {
		
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++) {
			System.out.println();
			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++) {
				System.out.printf( currentGameState[i][j] +" ");
			}
		}
	}
	
	public String toEdaxString() {
		String sout = "";	
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++) {
			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++) {
				sout += ConfigOthello.EDAX_STRING[currentGameState[i][j]]; 
			}
		}
		sout += ConfigOthello.EDAX_STRING[this.getPlayer()];		// last char: player to move next
//		System.out.println(sout);
		return sout;
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
	
	// WK now needed to get the right playerNextMove for AsStateObserverOthello (!)
	// 	  Also needed by passToNextPlayer() in ObserverBase
	public void setPlayer(int p) {
		this.playerNextMove = p;
	}
	
	public int getOpponent(int player)
	{
		return BaseOthello.getOpponent(player);
	}
	
	public int getCountWhite() { return countWhite;}
	public int getCountBlack() { return countBlack;}
	//public void setCountWhite(int w) {countWhite = w;}
	//public void setCountBlack(int b) {countBlack = b;}
	//public int getTurn() { return turn; }
}