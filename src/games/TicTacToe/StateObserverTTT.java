package games.TicTacToe;

import java.util.ArrayList;

import controllers.PlayAgent;
import games.ObserverBase;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;

/**
 * Class StateObservation observes the current state of the game, it has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link #advance(Types.ACTIONS)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 *
 */
public class StateObserverTTT extends ObserverBase implements StateObservation {
    private static final double REWARD_NEGATIVE = -1.0;
    private static final double REWARD_POSITIVE =  1.0;
	private int[][] m_Table;		// current board position
	private int m_Player;			// Player who makes the next move 
	private ArrayList<Types.ACTIONS> acts = new ArrayList();	// holds all available actions
//	protected Types.ACTIONS[] actions;
    
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	public StateObserverTTT() {
		m_Table = new int[3][3]; 
		m_Player = 1;
		setAvailableActions();
	}

	public StateObserverTTT(int[][] Table) {
		int pieceCount=0;
		for (int i=0; i<3; i++) 
			for (int j=0; j<3; j++) {
				pieceCount += Table[i][j];
			}
		m_Player = (pieceCount%2==0 ? +1 : -1);
		m_Table = new int[3][3];
		TicTDBase.copyTable(Table,m_Table); 
		setAvailableActions();
	}
	
	public StateObserverTTT(int[][] Table, int Player) {
		m_Table = new int[3][3];
		TicTDBase.copyTable(Table,m_Table); 
		m_Player = Player;
		setAvailableActions();
	}
	
	public StateObserverTTT copy() {
		StateObserverTTT sot = new StateObserverTTT(m_Table,m_Player);
		sot.m_counter = this.m_counter;
		return sot;
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

//    @Override
//	public boolean has2OppositeRewards() {
//		return true;
//	}

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

	@Deprecated
    public String toString() {
    	return stringDescr();
    }
	
	@Override
    public String stringDescr() {
		String sout = "";
		String str[] = new String[3]; 
		str[0] = "o"; str[1]="-"; str[2]="X";
		
		for (int i=0;i<3;i++) 
			for (int j=0;j<3;j++)
				sout = sout + (str[this.m_Table[i][j]+1]);
		
 		return sout;
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
	

	public Types.WINNER getGameWinner() {
		assert isGameOver() : "Game is not yet over!";
		if (TicTDBase.Win(m_Table, -m_Player))		// why -m_Player? advance() has changed m_player (although game is over) 
			return Types.WINNER.PLAYER_LOSES;
		if (TicTDBase.tie(m_Table)) 
			return Types.WINNER.TIE;
		if (TicTDBase.Win(m_Table, m_Player)) {		
			int dummy=1; // this should normally not happen in TTT
						 // (the player who is to move is not the winner if game is over)
			return Types.WINNER.PLAYER_WINS;
		}
		return null;
	}

	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For TTT only game-over states have a non-zero game score. 
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

	public String getName() { return "TicTacToe";	}

	/**
	 * Advance the current state with 'action' to a new state
	 * @param action
	 */
	public void advance(ACTIONS action) {
		int iAction = action.toInt();
		assert (0<=iAction && iAction<9) : "iAction is not in 0,1,...,8.";
		int j=iAction%3;
		int i=(iAction-j)/3;		// reverse: iAction = 3*i + j
		
		assert m_Table[i][j]==0 : "The desired move would alter an already occupied field!";
    	m_Table[i][j] = m_Player;
    	
    	setAvailableActions(); 			// IMPORTANT: adjust the available actions (have reduced by one)
    	
    	// set up player for next advance()
    	int n=this.getNumPlayers();
    	switch (n) {
    	case (1): 
    		m_Player = m_Player; 
    		break;
    	case (2): 
    		m_Player = m_Player*(-1);    // 2-player games: 1,-1,1,-1,...
    		break;
    	default: 
    		m_Player = (m_Player+1) % n;  // many-player games: 0,1,...,n-1,0,1,...
    		break;
    	}   		
		super.incrementMoveCounter();
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
		if (m_Table[0][0]==0)  acts.add(Types.ACTIONS.fromInt(0));
		if (m_Table[0][1]==0)  acts.add(Types.ACTIONS.fromInt(1));
		if (m_Table[0][2]==0)  acts.add(Types.ACTIONS.fromInt(2));
		if (m_Table[1][0]==0)  acts.add(Types.ACTIONS.fromInt(3));
		if (m_Table[1][1]==0)  acts.add(Types.ACTIONS.fromInt(4));
		if (m_Table[1][2]==0)  acts.add(Types.ACTIONS.fromInt(5));
		if (m_Table[2][0]==0)  acts.add(Types.ACTIONS.fromInt(6));
		if (m_Table[2][1]==0)  acts.add(Types.ACTIONS.fromInt(7));
		if (m_Table[2][2]==0)  acts.add(Types.ACTIONS.fromInt(8));
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
		return acts.get(i);
	}

	public int[][] getTable() {
		return m_Table;
	}

	/**
	 * @return 	{0,1} for the player to move next. 
	 * 			Player 0 is X, the player who starts the game. Player 1 is O.
	 */
	public int getPlayer() {
		return (-m_Player+1)/2;
	}
//	/**
//	 * @return 	{+1,-1} for the player to move next
//	 * 			Player +1 is X, the player who starts the game. Player -1 is O.
//	 */
//	public int getPlayerPM() {
//		return m_Player;
//	}
	
	public int getNumPlayers() {
		return 2;				// TicTacToe is a 2-player game
	}


}
