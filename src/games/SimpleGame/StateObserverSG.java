package games.SimpleGame;

import games.ObsNondetBase;
import games.ObserverBase;
import games.StateObsNondeterministic;
import games.StateObservation;
import tools.Types.ACTIONS;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class observes the current state of the game, it has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link #advance(ACTIONS)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 */
public class StateObserverSG extends ObsNondetBase implements StateObsNondeterministic {
	public static final int UPPER = 9;
	public static final double REWARD_NEGATIVE = 0;
	public static final double REWARD_POSITIVE = UPPER;
	private static final Random rand = new Random();

	private int m_sum;				// current sum
	private boolean m_gameOver=false;
	private ACTIONS m_action = null;
	protected ArrayList<ACTIONS> availableActions = new ArrayList<>();	// holds all available actions: 0: HIT, 1: STAND

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	@Serial
	private static final long serialVersionUID = 12L;

	public StateObserverSG() {
		super();
		m_sum = (int)(rand.nextDouble() * UPPER)+1;		// a random number from 1,2,...,UPPER
		setAvailableActions();
	}

	public StateObserverSG(StateObserverSG other)
	{
		super(other);		// copy members m_counter, lastMoves and stored*
		m_sum = other.m_sum;
		m_gameOver = other.isGameOver();
		m_action = other.m_action;
		if (other.availableActions!=null)	// this check is needed when loading older logs
			this.availableActions = (ArrayList<ACTIONS>) other.availableActions.clone();
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
	 * @param player only needed for the interface, not relevant in this 1-person game
	 * @return 	the game score, i.e. the sum of rewards for the current state.
	 */
	public double getGameScore(int player) {
        if(isGameOver()) {
        	return (m_sum>UPPER)?0:m_sum;
        }
        return 0;
	}

	public double getMinGameScore() { return REWARD_NEGATIVE; }
	public double getMaxGameScore() { return REWARD_POSITIVE; }

	// --- obsolete, use Arena().getGameName() instead
//	public String getName() { return "SimpleGame";	}

	/**
	 * Advance the current state with 'action' to a new state
	 * @param action	0: HIT or 1: STAND
	 */
	public void advance(ACTIONS action) {
		m_action = action;
		advanceNondeterministic();
	}

	@Override
	public void advanceDeterministic(ACTIONS action) {
		m_action = action;
	}

	@Override
	public ACTIONS advanceNondeterministic() {
		if (m_action.toInt()==0) {    // HIT
			int iCard = (int)(rand.nextDouble() * UPPER)+1;		// a random number from 1,2,...,UPPER
			m_sum += iCard;
		}
		m_gameOver=true;	// the game is always finished after one advance
		super.addToLastMoves(m_action);
		super.incrementMoveCounter();
		return m_action;
	}

	@Override
	public ACTIONS advanceNondeterministic(ACTIONS randAction) {
		advanceNondeterministic();
		return randAction;
	}

	@Override
	public ACTIONS getNextNondeterministicAction() {
		return null;
	}

	@Override
	public ArrayList<ACTIONS> getAvailableRandoms() {
		ArrayList<ACTIONS> availRandoms = new ArrayList<>();
		for (int i=0; i<UPPER; i++)
			availRandoms.add(ACTIONS.fromInt(i));
		return availRandoms;
	}

	@Override
	public int getNumAvailableRandoms() {
		return UPPER;
	}

	/**
	 * @param randAction  the nondeterministic action
	 * @return the probability that the random {@code randAction} is selected by a
	 *         nondeterministic advance.
	 */
	@Override
	public double getProbability(ACTIONS randAction) {
		return 1.0/UPPER;
	}

	@Override
    public ArrayList<ACTIONS> getAllAvailableActions() {
		setAvailableActions();
		return availableActions;
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
