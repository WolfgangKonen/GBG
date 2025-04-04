package games.TicTacToe;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import games.ObserverBase;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;

/**
 * Class StateObserverTTT observes the current state of the game.<p>
 * It has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link StateObservation#advance(ACTIONS, Random)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 *
 */
public class StateObserverTTT extends ObserverBase implements StateObservation {
    private static final double REWARD_NEGATIVE = -1.0;
    private static final double REWARD_POSITIVE =  1.0;
	private int[][] m_Table;		// current board position
	private int m_Player;			// what we fill into m_Table for the current player's move:
									// +1 for X (this.getPlayer()==0), -1 for O (this.getPlayer()==1)
	protected ArrayList<Types.ACTIONS> availableActions = new ArrayList<>();	// holds all available actions
    
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	@Serial
	private static final long serialVersionUID = 12L;

	public StateObserverTTT() {
		super();
		m_Table = new int[3][3]; 
		m_Player = 1;
		setAvailableActions();
	}

	public StateObserverTTT(int[][] Table, int Player) {
		super();
		m_Table = new int[3][3];
		TicTDBase.copyTable(Table,m_Table); 
		m_Player = Player;
		setAvailableActions();
	}
	
	public StateObserverTTT(StateObserverTTT other)
	{
		super(other);		// copy members m_counter, lastMoves and stored*
		this.m_Table = new int[3][3];
		TicTDBase.copyTable(other.m_Table,m_Table); 
		m_Player = other.m_Player;
		if (other.availableActions!=null)	// this check is needed when loading older logs
			this.availableActions = (ArrayList<ACTIONS>) other.availableActions.clone();
				// Note that clone does only clone the ArrayList, but not the contained ACTIONS, they are 
				// just copied by reference. However, these ACTIONS are never altered, so it is o.k.
//		setAvailableActions();		// this would be a bit slower
	}
	
	public StateObserverTTT copy() {
		return new StateObserverTTT(this);
	}

    @Override
	public boolean isGameOver() {
		return TicTDBase.isGameOver(m_Table);
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
		return TicTDBase.legalState(m_Table,m_Player);
	}
	
	public boolean isLegalAction(ACTIONS act) {
		int iAction = act.toInt();
		int j=iAction%3;
		int i=(iAction-j)/3;		// reverse: iAction = 3*i + j
		
		return (m_Table[i][j]==0); 
		
	}

//	@Deprecated
//    public String toString() {
//    	return stringDescr();
//    }
	
	@Override
    public String stringDescr() {
		StringBuilder sout = new StringBuilder();
		String[] str = new String[3];
		str[0] = "o"; str[1]="-"; str[2]="X";
		
		for (int i=0;i<3;i++) 
			for (int j=0;j<3;j++)
				sout.append(str[this.m_Table[i][j] + 1]);
		
 		return sout.toString();
	}
	
	/**
	 * 
	 * @return true, if the current position is a win (for either player)
	 */
	public boolean win()
	{
		if (TicTDBase.Win(this.getTable(),+1)) return true;
		return TicTDBase.Win(this.getTable(),-1);
	}
	

//	public Types.WINNER getGameWinner() {
//		assert isGameOver() : "Game is not yet over!";
//		if (TicTDBase.Win(m_Table, -m_Player))		// why -m_Player? advance() has changed m_player (although game is over) 
//			return Types.WINNER.PLAYER_LOSES;
//		if (TicTDBase.tie(m_Table)) 
//			return Types.WINNER.TIE;
//		
//		throw new RuntimeException("Unexpected case: we cannot have a win for the player to move!");
//	}

	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For TTT only game-over states can have a non-zero game score.
	 * 			It is the reward from the perspective of {@code player}.
	 */
	public double getGameScore(int player) {
		int sign = (player==this.getPlayer()) ? 1 : (-1);
        if(isGameOver()) {
        	// if the game is over and not a tie, it is a win for the player who made the action towards
        	// state 'this' --> it is a loss for this.getPlayer() and a win for the opponent.
        	if (TicTDBase.Win(m_Table,+1))
        		return -sign;
        	if (TicTDBase.Win(m_Table,-1))
        		return -sign;

        	// if the game is over and no player has won, it is a tie (0) --> we fall through the if branch and return 0

// --- Bug fix 2023-04: the logic behind the following lines was wrong: If a last move filled the board and was a win
// --- for either player, the logic would count this (wrongly) as a tie!
//    		if (TicTDBase.tie(m_Table))
//    			return 0;
//        	// if the game is over and not a tie, it is a win for the player who made the action towards
//        	// state 'this' --> it is a loss for this.getPlayer().
//        	return -sign;
        }
        
        return 0; 
	}

	public double getMinGameScore() { return REWARD_NEGATIVE; }
	public double getMaxGameScore() { return REWARD_POSITIVE; }

	// --- obsolete, use Arena().getGameName() instead
//	public String getName() { return "TicTacToe";	}

	/**
	 * Advance the current state with 'action' to a new state
     * @param action
     * @param cmpRand
     */
	public void advance(ACTIONS action, Random cmpRand) {
		int iAction = action.toInt();
		assert (0<=iAction && iAction<9) : "iAction is not in 0,1,...,8.";
		int j=iAction%3;
		int i=(iAction-j)/3;		// reverse: iAction = 3*i + j
		
		assert m_Table[i][j]==0 : "The desired move would alter an already occupied field!";
		m_Table[i][j] = m_Player;
    	
    	setAvailableActions(); 		// IMPORTANT: adjust the available actions (have reduced by one)
    	
		m_Player = m_Player*(-1);    // 2-player games: 1,-1,1,-1,...

		super.addToLastMoves(action);
		super.incrementMoveCounter();
	}

    @Override
    public ArrayList<Types.ACTIONS> getAllAvailableActions() {
        ArrayList<Types.ACTIONS> allActions = new ArrayList<>();
		for (int j = 0; j < 3; j++)
			for (int i = 0; i < 3; i++)
            	allActions.add(Types.ACTIONS.fromInt(i * 3 + j));
		Collections.sort(allActions);
        return allActions;
    }
    
	public ArrayList<ACTIONS> getAvailableActions() {
		return availableActions;
	}
	
	public int getNumAvailableActions() {
		return availableActions.size();
	}

	/**
	 * Given the current state in m_Table, what are the available actions? 
	 * Set them in member ACTIONS[] actions.
	 */
	public void setAvailableActions() {
		if (availableActions==null) {	// safety check, needed when called from LogManagerGUI
			availableActions = new ArrayList<>();
		}
		availableActions.clear();
		if (m_Table[0][0]==0)  availableActions.add(Types.ACTIONS.fromInt(0));
		if (m_Table[0][1]==0)  availableActions.add(Types.ACTIONS.fromInt(1));
		if (m_Table[0][2]==0)  availableActions.add(Types.ACTIONS.fromInt(2));
		if (m_Table[1][0]==0)  availableActions.add(Types.ACTIONS.fromInt(3));
		if (m_Table[1][1]==0)  availableActions.add(Types.ACTIONS.fromInt(4));
		if (m_Table[1][2]==0)  availableActions.add(Types.ACTIONS.fromInt(5));
		if (m_Table[2][0]==0)  availableActions.add(Types.ACTIONS.fromInt(6));
		if (m_Table[2][1]==0)  availableActions.add(Types.ACTIONS.fromInt(7));
		if (m_Table[2][2]==0)  availableActions.add(Types.ACTIONS.fromInt(8));
//        // /WK/ Get the available actions in an array.
//		// *TODO* Does this work if acts.size()==0 ?
//        ArrayList<Types.ACTIONS> acts = this.getAvailableActions();
//        actions = new Types.ACTIONS[acts.size()];
//        for(int i = 0; i < actions.length; ++i)
//        {
//            actions[i] = acts.get(i);
//        }
		
	}
	
	public Types.ACTIONS getAction(int i) {
		return availableActions.get(i);
	}

	public int[][] getTable() {
		return m_Table;
	}

	/**
	 * @return 	{0,1} for the player to move in this state 
	 * 			Player 0 is X, the player who starts the game (m_Player=+1). Player 1 is O (m_Player=-1).
	 */
	public int getPlayer() {
		//return (-m_Player+1)/2;
		return (m_Player==1) ? 0 : 1;
	}
	
	public int getNumPlayers() {
		return 2;				// TicTacToe is a 2-player game
	}


}
