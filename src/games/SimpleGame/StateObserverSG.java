package games.SimpleGame;

import games.ObserverBase;
import games.StateObsNondeterministic;
import games.StateObservation;
import tools.Types.ACTIONS;

import java.util.ArrayList;
import java.util.Random;

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
public class StateObserverSG extends ObserverBase implements StateObsNondeterministic {
	public static final int UPPER = 9;
	public static final double REWARD_NEGATIVE = 0;
	public static final double REWARD_POSITIVE = UPPER;
	private static Random rand = new Random();

	private int[][] m_Table;		// current board position
	private int m_sum;				// current sum
	private int m_Player;			// player who makes the next move (+1 or -1)
	private boolean m_gameOver=false;
	protected ArrayList<ACTIONS> availableActions = new ArrayList();	// holds all available actions: 0: HIT, 1: STAND

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	public StateObserverSG() {
		super();
		m_Table = new int[3][3]; 
		m_Player = 1;
		m_sum = (int)(rand.nextDouble() * UPPER)+1;		// a random number from 1,2,...,UPPER
		setAvailableActions();
	}

	public StateObserverSG(int[][] Table, int Player) {
		super();
		m_Table = new int[3][3];
		m_Player = Player;
		setAvailableActions();
	}

	public StateObserverSG(int sum) {
		super();
		m_sum = sum;
		setAvailableActions();
	}

	public StateObserverSG(StateObserverSG other)
	{
		super(other);		// copy members m_counter, lastMoves and stored*
		m_sum = other.m_sum;
		m_Player = other.m_Player;
		if (other.availableActions!=null)	// this check is needed when loading older logs
			this.availableActions = (ArrayList<ACTIONS>) other.availableActions.clone();
				// Note that clone does only clone the ArrayList, but not the contained ACTIONS, they are 
				// just copied by reference. However, these ACTIONS are never altered, so it is o.k.
//		setAvailableActions();		// this would be a bit slower
	}
	
	public StateObserverSG copy() {
		return new StateObserverSG(this);
	}

    @Override
	public boolean isGameOver() {
		return m_gameOver;
	}

    @Override
	public boolean isDeterministicGame() {
		return false;
	}
	
    @Override
	public boolean isFinalRewardGame() {
		return true;
	}

    @Override
	public boolean isLegalState() {
		return true;
	}
	
	public boolean isLegalAction(ACTIONS act) {
		return act.toInt()==0 || act.toInt()==1;
	}

	@Override
	public boolean stopInspectOnGameOver() {
		return true;
	}

	@Deprecated
    public String toString() {
    	return stringDescr();
    }
	
	@Override
    public String stringDescr() {
		String sout = "(" + m_sum +") " + (m_gameOver?"F":"O");	// (F)inal : (O)pen
 		return sout;
	}
	
	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For TTT only game-over states have a non-zero game score. 
	 * 			It is the reward from the perspective of {@code refer}.
	 */
	public double getGameScore(StateObservation refer) {
        if(isGameOver()) {
        	return (m_sum>UPPER)?0:m_sum;
        }
        return 0;
	}

	public double getMinGameScore() { return REWARD_NEGATIVE; }
	public double getMaxGameScore() { return REWARD_POSITIVE; }

	public String getName() { return "SimpleGame";	}

	/**
	 * Advance the current state with 'action' to a new state
	 * @param action
	 */
	public void advance(ACTIONS action) {

		if (action.toInt()==0) {    // HIT
			int iCard = (int)(rand.nextDouble() * UPPER)+1;		// a random number from 1,2,...,UPPER
			m_sum += iCard;
		} else {
			// nothing to do in action STAND
		}
		m_gameOver=true;	// the game is always finished after one advance

		super.addToLastMoves(action);
		super.incrementMoveCounter();
	}

	@Override
	public void advanceNondeterministic(ACTIONS action) {
		if (action.toInt()==0) {    // HIT
			int iCard = (int)(rand.nextDouble() * UPPER)+1;		// a random number from 1,2,...,UPPER
			m_sum += iCard;
		}
	}

	@Override
	public ACTIONS getNextNondeterministicAction() {
		return null;
	}

	@Override
	public ArrayList<ACTIONS> getAvailableRandoms() {
		ArrayList availRandoms = new ArrayList<>();
		for (int i=0; i<UPPER; i++)
			availRandoms.add(ACTIONS.fromInt(i));
		return availRandoms;
	}

	@Override
	public int getNumAvailableRandoms() {
		return UPPER;
	}

	/**
	 * @param action  the nondeterministic action
	 * @return the probability that the random {@code action} is selected by a
	 *         nondeterministic advance.
	 */
	@Override
	public double getProbability(ACTIONS action) {
		return 1.0/UPPER;
	}

	@Override
    public ArrayList<ACTIONS> getAllAvailableActions() {
        ArrayList allActions = new ArrayList<>();
		for (int i=0; i<2; i++)
			allActions.add(ACTIONS.fromInt(i));
        
        return allActions;
    }
    
	public ArrayList<ACTIONS> getAvailableActions() {
		return availableActions;
	}
	
	public int getNumAvailableActions() {
		return availableActions.size();
	}

	/**
	 * The available actions are 0 = HIT, 1 = STAND
	 */
	public void setAvailableActions() {
		availableActions.clear();
		for (int i=0; i<2; i++)
			availableActions.add(ACTIONS.fromInt(i));
	}
	
	public ACTIONS getAction(int i) {
		return availableActions.get(i);
	}

	public int[][] getTable() {
		return m_Table;
	}

	public int get_sum() {
		return m_sum;
	}

	public int getPlayer() {
		return 0;
	}
	
	public int getNumPlayers() {
		return 1;				// SimpleGame is a 1-player game
	}


}
