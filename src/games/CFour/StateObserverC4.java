package games.CFour;

import java.io.Serializable;
import java.util.ArrayList;

import games.ObserverBase;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;

/**
 * Class StateObserverC4 observes the current state of the game, it has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link #advance(Types.ACTIONS)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 * See class {@link C4Base} for details of state coding.
 *
 * Note: If a state is a win (draw) can only be detected when this state was formed via advance. This is because
 * only when an action is made (via advance) then it is detected whether the piece set by this action generates a
 * 4-in-a-row or whether it completes the board (without any 4-in-a-row) and thus is a draw. It is assumed that
 * the pre-advance state was not a game-over state.
 */
public class StateObserverC4 extends ObserverBase implements StateObservation {
    private static final double REWARD_NEGATIVE = -1.0;
    private static final double REWARD_POSITIVE =  1.0;
	private int m_Player;			// Player who makes the next move (0 or 1)
	private final C4Base m_C4;
	private ArrayList<Types.ACTIONS> availableActions = new ArrayList<>();	// holds all available actions
	private boolean gameOver = false;
	private boolean isWin = false;

	private LastCell lastCell = new LastCell();
	private LastCell prevCell = new LastCell();
    
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	public StateObserverC4() {
		m_C4 = new C4Base();
		m_counter=0;
		m_Player = (m_C4.countPieces() % 2 == 0) ? 0 : 1;
		setAvailableActions();
	}


	/**
	 * Note that setting a state by this constructor may fail to detect that this state is already a win for either
	 * player. Wins are only detected if they are reached via {@link #advance(ACTIONS)}.
	 *
	 * @param board array [COLCOUNT][ROWCOUNT], i.e. [7][6]
	 */
	// needed for testing:
	public StateObserverC4(int[][] board) {
		m_C4 = new C4Base(board);
		m_counter = m_C4.countPieces();
		m_Player = (m_C4.countPieces() % 2 == 0) ? 0 : 1;
		setAvailableActions();
		assert this.isLegalState();
	}
	
	public StateObserverC4(StateObserverC4 other) {
		super(other);		// copy members m_counter and stored*
		this.m_C4 = new C4Base(other.getBoard());
		this.m_Player = other.m_Player;
		this.gameOver = other.gameOver;
		this.isWin = other.isWin;	// bug fix (!) 2020-08-25
		if (other.lastCell!=null) 			// this check is needed when loading older logs
			lastCell = new LastCell(other.lastCell);
		if (other.prevCell!=null) 			// this check is needed when loading older logs
			prevCell = new LastCell(other.prevCell);
		if (other.availableActions!=null)	// this check is needed when loading older logs
			this.availableActions = (ArrayList<ACTIONS>) other.availableActions.clone();
				// Note that clone does only clone the ArrayList, but not the contained ACTIONS, they are 
				// just copied by reference. However, these ACTIONS are never altered, so it is o.k.
//		setAvailableActions();		// this would be  slower
	}
	
	public StateObserverC4 copy() {
		return new StateObserverC4(this);
	}

	public int countPieces() {
		return m_C4.countPieces();
	}
	
    @Override
	public boolean isGameOver() {
		return gameOver;
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
		return m_C4.isLegalBoard(m_C4.getBoard());
	}
	
	public boolean isLegalAction(ACTIONS act) {
		return m_C4.isLegalMove(act.toInt());
	}

	@Deprecated
    public String toString() {
    	return stringDescr();
    }
	
	/**
	 * One-row string description of state: 
	 * <pre>
	 * 		|Xo----|XXX---|...|
	 * </pre>
	 * where the first |Xo----| is a representation of column_0, from row 0 up to row 
	 * {@link C4Base#ROWCOUNT}, and so on, for all {@link C4Base#COLCOUNT} columns. <br>
	 * "X" is first player, "o" is second player and "-" is empty.
	 */
	@Override
    public String stringDescr() {
		StringBuilder sout = new StringBuilder();
		String[] str = new String[] {"-","X","o"};
//		String[] str = new String[3];
//		str[0] = "-"; str[1]="X"; str[2]="o";
		
		int[][] board = m_C4.getBoard();
		for (int i=0;i<C4Base.COLCOUNT;i++) {
			sout.append("|");
			for (int j=0;j<C4Base.ROWCOUNT;j++) {
				sout.append(str[board[i][j]]);
			}
		}
		sout.append("|");
		
 		return sout.toString();
	}
	
	/**
	 * 
	 * @return true, if the current position is a win (for either player)
	 */
	public boolean win()
	{
		return isWin;

		// this former statement (before 2020-08-23) was buggy: if the last piece which completely fills the board
		// is a win for either player, this would be undetected, because m_C4.isDraw checks only on board full:
//		return (isGameOver() && !m_C4.isDraw());
	}
	

	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For C4 only game-over states have a non-zero game score. 
	 * 			It is the reward from the perspective of {@code player}.
	 */
	public double getGameScore(int player) {
		int sign = (player==this.getPlayer()) ? 1 : (-1);
        if(isGameOver()) {
        	if (isWin) 
            	// if the game is over and a win for the player who made the action towards state 'this'
            	// --> it is a loss for this.getPlayer().
        		return -sign;
        	assert m_C4.isDraw(); // if game is over, but not a win, it must be a draw
        	return 0;
        }
        
        return 0; 
	}

	public double getMinGameScore() { return REWARD_NEGATIVE; }
	public double getMaxGameScore() { return REWARD_POSITIVE; }

	public String getName() { return "ConnectFour";	}

	/**
	 * Advance the current state with 'action' to a new state
	 * @param action the action to take
	 */
	public void advance(ACTIONS action) {
		super.advanceBase(action);		//		includes addToLastMoves(action)
		int iAction = action.toInt();

		assert (0<=iAction && iAction<C4Base.COLCOUNT) : "iAction is not in 0,1,...,"+C4Base.COLCOUNT+".";
		assert (m_C4.getColHeight(iAction)<C4Base.ROWCOUNT) : "desired move colum "+iAction+" is full!";

		gameOver = isWin = m_C4.canWin(iAction);

		m_C4.putPiece(iAction);
		if(!gameOver) gameOver = m_C4.isDraw();	// if game is not a win, test on draw
		
    	setAvailableActions(); 			// IMPORTANT: adjust the available actions 

		super.incrementMoveCounter();   // increment m_counter
		// NOTE: Be aware that m_counter is not always the same as m_C4.countPieces() (!)
		// m_counter counts the moves *after* the start board, i.e. if the start board has 
		// one piece already set, it has m_counter=0, but m_C4.countPieces()=1.
		// CONSEQUENCE: do not infer m_Player from m_counter, but only from m_C4.countPieces().
		m_Player = (m_C4.countPieces() % 2 == 0) ? 0 : 1; // player to move in this

		this.prevCell = this.lastCell;
		int lastPlayer = (m_Player==0) ? 1 : 0; // player who acted in this advance()
		this.lastCell = new LastCell(iAction, m_C4.getColHeight(iAction)-1, lastPlayer);

//			System.out.println("player="+this.getPlayer()+", moveCounter="+this.getMoveCounter());
	}

	public ArrayList<ACTIONS> getAvailableActions() {
		return availableActions;
	}
	
	public ArrayList<ACTIONS> getAllAvailableActions() {
        ArrayList<ACTIONS> allActions = new ArrayList<>();
        for (int j = 0; j < C4Base.COLCOUNT; j++) 
        	allActions.add(Types.ACTIONS.fromInt(j));
        
        return allActions;
	}
	
	public int getNumAvailableActions() {
		return availableActions.size();
	}

	/**
	 * Given the current state in m_Table, what are the available actions? 
	 * Set them in member ACTIONS[] actions.
	 */
	public void setAvailableActions() {
		availableActions.clear();
		int[] moves = m_C4.generateMoves(false);
		for (int move : moves) availableActions.add(ACTIONS.fromInt(move));
	}
	
	public Types.ACTIONS getAction(int i) {
		return availableActions.get(i);
	}

	public int[][] getBoard() {
		return m_C4.getBoard();
	}
	
	/**
	 * @return 	{0,1} for the player to move next. 
	 * 			Player 0 is X, the player who starts the game. Player 1 is O.
	 */
	public int getPlayer() {
		return m_Player;
	}
	
	public int getNumPlayers() { return 2; }

	public LastCell getLastCell() { return lastCell; }
	public LastCell getPrevCell() { return prevCell; }

	public class LastCell implements Serializable {
		public int c;
		public int r;
		public int p;
		private final boolean valid;
		public LastCell() {
			valid=false;
		}
		public LastCell(int col, int row, int player) {
			c=col;
			r=row;
			p=player;
			valid=true;
		}
		public LastCell(LastCell other) {
			this.c = other.c;
			this.r = other.r;
			this.p = other.p;
			this.valid = other.valid;
		}
		public boolean isValid() {  return valid; }
	}

}
