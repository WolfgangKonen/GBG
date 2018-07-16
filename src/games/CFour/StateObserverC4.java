package games.CFour;

import java.util.ArrayList;

import controllers.PlayAgent;
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
 *
 */
public class StateObserverC4 extends ObserverBase implements StateObservation {
    private static final double REWARD_NEGATIVE = -1.0;
    private static final double REWARD_POSITIVE =  1.0;
	private int m_Player;			// Player who makes the next move (0 or 1)
	private C4Base m_C4;
	private ArrayList<Types.ACTIONS> acts = new ArrayList();	// holds all available actions
	private boolean gameOver = false;
    
    // --- this is now in ObserverBase ---
//    public Types.ACTIONS[] storedActions = null;
//    public Types.ACTIONS storedActBest = null;
//    public double[] storedValues = null;
//    public double storedMaxScore; 
	
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

	public StateObserverC4(int[][] board) {
		m_C4 = new C4Base(board);
		m_counter = m_C4.countPieces();
		m_Player = (m_C4.countPieces() % 2 == 0) ? 0 : 1;
		setAvailableActions();		
	}
	
	public StateObserverC4 copy() {
		StateObserverC4 sot = new StateObserverC4(m_C4.getBoard());
		sot.m_counter = this.m_counter;
		sot.m_Player = (m_C4.countPieces() % 2 == 0) ? 0 : 1;
		sot.gameOver = this.gameOver;
		sot.setAvailableActions();
		return sot;
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
	 * "X" is player 1, "o" is player 2 and "-" is empty.
	 */
	@Override
    public String stringDescr() {
		String sout = "";
		String str[] = new String[3]; 
		str[0] = "-"; str[1]="X"; str[2]="o";
		
		int[][] board = m_C4.getBoard();
		for (int i=0;i<C4Base.COLCOUNT;i++) {
			sout = sout + "|";
			for (int j=0;j<C4Base.ROWCOUNT;j++) {
				sout = sout + (str[board[i][j]]);
			}
		}
		sout = sout + "|";
		
 		return sout;
	}
	
	/**
	 * 
	 * @return true, if the current position is a win (for either player)
	 */
	public boolean win()
	{
		return (isGameOver() && !m_C4.isDraw());
	}
	

	public Types.WINNER getGameWinner() {
		assert isGameOver() : "Game is not yet over!";
		if (m_C4.isDraw()) 
			return Types.WINNER.TIE;
		
		// if we arrive here, the game is over and it is a win for the other player: 
		return Types.WINNER.PLAYER_LOSES;
		// (the player who is to move is not the winner if game is over)
	}

	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For C4 only game-over states have a non-zero game score. 
	 * 			It is the reward for the player who *would* move next (if 
	 * 			the game were not over). 
	 */
	public double getGameScore() {
        boolean gameOver = this.isGameOver();
        if(gameOver) {
            Types.WINNER win = this.getGameWinner();
        	switch(win) {
        	case PLAYER_LOSES:
                return REWARD_NEGATIVE;
        	case TIE:
                return 0;
        	case PLAYER_WINS:
                return REWARD_POSITIVE;
            default:
            	throw new RuntimeException("Wrong enum for Types.WINNER win !");
        	}
        }
        
        return 0; 
	}

	public double getMinGameScore() { return REWARD_NEGATIVE; }
	public double getMaxGameScore() { return REWARD_POSITIVE; }

	public String getName() { return "ConnectFour";	}

	/**
	 * Advance the current state with 'action' to a new state
	 * @param action
	 */
	public void advance(ACTIONS action) {
		int iAction = action.toInt();
		
		assert (0<=iAction && iAction<C4Base.COLCOUNT) : "iAction is not in 0,1,...,"+C4Base.COLCOUNT+".";
		assert (m_C4.getColHeight(iAction)<C4Base.ROWCOUNT) : "desired move colum "+iAction+" is full!";
		
//		System.out.println(iAction+", "+m_C4.getColHeight(iAction)+ ", "+ C4Base.ROWCOUNT);
//		m_C4.printBoard();

		gameOver = m_C4.canWin(iAction);
//		System.out.println("can win: "+ gameOver);
		
//		if (gameOver) {
//			try {
//				Thread.sleep(250);
//				// strange, but we need a certain waiting time here, otherwise
//				// the state will not be the right one during PLAY (??)
//				// --- the strange effect is gone after we replace gameOver in 
//				// --- C4GameGui with isGameOver(), which returns 
//				// --- gameBoardC4.getStateObs().isGameOver()
//			} catch (Exception e) {
//				System.out.println("Thread 1");
//			}
//		}
		m_C4.putPiece(iAction);
		if(!gameOver) gameOver = m_C4.isDraw();	// if game is not a win, test on draw
		
    	setAvailableActions(); 			// IMPORTANT: adjust the available actions 
    	
    	
		super.incrementMoveCounter();   // increment m_counter
		// NOTE: Be aware that m_counter is not always the same as m_C4.countPieces() (!)
		// m_counter counts the moves *after* the start board, i.e. if the start board has 
		// one piece already set, it has m_counter=0, but m_C4.countPieces()=1.
		// CONSEQUENCE: do not infer m_Player from m_counter, but only from m_C4.countPieces().
		m_Player = (m_C4.countPieces() % 2 == 0) ? 0 : 1; 
		
//			System.out.println("player="+this.getPlayer()+", moveCounter="+this.getMoveCounter());
	}

    /**
     * Return the afterstate preceding {@code this}. 
     */
    @Override
    public StateObservation getPrecedingAfterstate() {
    	// for deterministic games, this state and its preceding afterstate are the same
    	return this;
    }

	public ArrayList<ACTIONS> getAvailableActions() {
		return acts;
	}
	
	public int getNumAvailableActions() {
		return acts.size();
	}

	/**
	 * Given the current state in m_Table, what are the available actions? 
	 * Set them in member ACTIONS[] actions.
	 */
	public void setAvailableActions() {
		acts.clear();
		int[] moves = m_C4.generateMoves(false);
		for (int j=0; j<moves.length; j++)
			acts.add(Types.ACTIONS.fromInt(moves[j]));
	}
	
	public Types.ACTIONS getAction(int i) {
		return acts.get(i);
	}

    // --- this is now in ObserverBase ---
//	/**
//	 * Given the current state, store some info useful for inspecting the  
//	 * action actBest and double[] vtable returned by a call to <br>
//	 * {@code ACTION_VT} {@link PlayAgent#getNextAction2(StateObservation, boolean, boolean)}. 
//	 *  
//	 * @param actBest	the best action
//	 * @param vtable	one double for each action in this.getAvailableActions():
//	 * 					it stores the value of that action (as given by the double[] 
//	 * 					from {@link Types.ACTIONS_VT#getVTable()}) 
//	 */
//	public void storeBestActionInfo(ACTIONS actBest, double[] vtable) {
//        ArrayList<Types.ACTIONS> acts = this.getAvailableActions();
//        storedActions = new Types.ACTIONS[acts.size()];
//        storedValues = new double[acts.size()];
//        for(int i = 0; i < storedActions.length; ++i)
//        {
//        	storedActions[i] = acts.get(i);
//        	storedValues[i] = vtable[i];
//        }
//        storedActBest = actBest;
//        if (actBest instanceof Types.ACTIONS_VT) {
//        	storedMaxScore = ((Types.ACTIONS_VT) actBest).getVBest();
//        } else {
//            storedMaxScore = vtable[acts.size()];        	
//        }
//	}

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
	
	public int getNumPlayers() {
		return 2;				// TicTacToe is a 2-player game
	}


}
