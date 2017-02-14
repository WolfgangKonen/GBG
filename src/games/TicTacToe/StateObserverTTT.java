package games.TicTacToe;

import java.util.ArrayList;

import controllers.PlayAgent;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;

/**
 * Class StateObservation observes the current state of the game, it has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link #advance(ACTIONS)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 *
 */
public class StateObserverTTT implements StateObservation {
    private static final double REWARD_NEGATIVE = -1.0;
    private static final double REWARD_POSITIVE =  1.0;
	private int[][] m_Table;		// current board position
	private int m_Player;			// Player who makes the next move 
    protected Types.ACTIONS[] actions;
    
    public Types.ACTIONS[] storedActions = null;
    public Types.ACTIONS storedActBest = null;
    public double[] storedValues = null;
    public double storedMaxScore; 
	
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
		return new StateObserverTTT(m_Table,m_Player);
	}

	public boolean isGameOver() {
		return TicTDBase.isGameOver(m_Table);
	}

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

    @Override
	public double getGameValue() { return getGameScore(); }

	/**
	 * Same as getGameScore(), but relative to referingState. This relativeness
	 * is usually only relevant for 2-player games
	 * @param referingState
	 * @return  If the referingState was created by White (and Black is to move), 
	 * 			then it is getGameScore(). If referingState was created by Black,
	 * 			then it is getGameScore()*(-1). 
	 */
	public double getGameScore(StateObservation referingState) {
		double rawScore = this.getGameScore();
		assert (referingState instanceof StateObserverTTT) 
			: "referingState is not of class StateObserverTTT";
		if (getNumPlayers()==2) {
			// if refering state has another player than this, then multiply by -1: 
			rawScore *=  this.getPlayerPM()*((StateObserverTTT)referingState).getPlayerPM();
		}
		return rawScore;
	}
	
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
    		m_Player = m_Player; break;
    	case (2): 
    		m_Player = m_Player*(-1);    // 2-player games: 1,-1,1,-1,...
    		break;
    	default: 
    		m_Player = (m_Player+1) % n;  // many-player games: 0,1,...,n-1,0,1,...
    		break;
    	}   		
	}

	public ArrayList<ACTIONS> getAvailableActions() {
		ArrayList<ACTIONS> availAct = new ArrayList<ACTIONS>();
		if (m_Table[0][0]==0)  availAct.add(Types.ACTIONS.ACTION_00);
		if (m_Table[0][1]==0)  availAct.add(Types.ACTIONS.ACTION_01);
		if (m_Table[0][2]==0)  availAct.add(Types.ACTIONS.ACTION_02);
		if (m_Table[1][0]==0)  availAct.add(Types.ACTIONS.ACTION_03);
		if (m_Table[1][1]==0)  availAct.add(Types.ACTIONS.ACTION_04);
		if (m_Table[1][2]==0)  availAct.add(Types.ACTIONS.ACTION_05);
		if (m_Table[2][0]==0)  availAct.add(Types.ACTIONS.ACTION_06);
		if (m_Table[2][1]==0)  availAct.add(Types.ACTIONS.ACTION_07);
		if (m_Table[2][2]==0)  availAct.add(Types.ACTIONS.ACTION_08);
		return availAct;
	}
	
	public int getNumAvailableActions() {
		return actions.length;
	}

	/**
	 * Given the current state in m_Table, what are the available actions? 
	 * Set them in member ACTIONS[] actions.
	 */
	public void setAvailableActions() {
        // /WK/ Get the available actions in an array.
		// *TODO* Does this work if acts.size()==0 ?
        ArrayList<Types.ACTIONS> acts = this.getAvailableActions();
        actions = new Types.ACTIONS[acts.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = acts.get(i);
        }
		
	}
	
	public Types.ACTIONS getAction(int i) {
		return actions[i];
	}

	/**
	 * Given the current state, store some info useful for inspecting the  
	 * action actBest and double[] vtable returned by a call to 
	 * {@link PlayAgent#getNextAction(StateObservation, boolean, double[], boolean)}
	 *  
	 * @param actBest	the best action
	 * @param vtable	one double for each action in this.getAvailableActions():
	 * 					it stores the value of that action (as given by the double[] 
	 * 					in getNextAction) 
	 */
	public void storeBestActionInfo(ACTIONS actBest, double[] vtable) {
        ArrayList<Types.ACTIONS> acts = this.getAvailableActions();
        storedActions = new Types.ACTIONS[acts.size()];
        storedValues = new double[acts.size()];
        for(int i = 0; i < storedActions.length; ++i)
        {
        	storedActions[i] = acts.get(i);
        	storedValues[i] = vtable[i];
        }
        storedActBest = actBest;
        storedMaxScore = vtable[acts.size()];
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
	/**
	 * @return 	{+1,-1} for the player to move next
	 * 			Player +1 is X, the player who starts the game. Player -1 is O.
	 */
	public int getPlayerPM() {
		return m_Player;
	}
	
	public int getNumPlayers() {
		return 2;				// TicTacToe is a 2-player game
	}


}
