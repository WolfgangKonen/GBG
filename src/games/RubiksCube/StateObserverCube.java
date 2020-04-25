package games.RubiksCube;

import java.util.ArrayList;

import controllers.PlayAgent;
import games.BoardVector;
import games.ObserverBase;
import games.StateObservation;
import games.RubiksCube.CubeState.Twist;
import tools.Types;
import tools.Types.ACTIONS;

/**
 * Class StateObserverCube observes the current state of the game, it has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link #advance(Types.ACTIONS)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 * The private member {@link CubeState} {@code m_state} has most part of the state logic for 
 * Rubik's Cube.
 * 
 * @see StateObserverCubeCleared
 */
public class StateObserverCube extends ObserverBase implements StateObservation {
	private CubeState m_state;
	/**
	 * the action which led to m_state (9 if not known)
	 */
	private ACTIONS m_action; 		
	private static CubeState def = new CubeState(); // a solved cube as reference
    private static final double REWARD_POSITIVE =  1.0;
	private ArrayList<ACTIONS> acts = new ArrayList();	// holds all available actions
    
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	public StateObserverCube() {
		m_state = new CubeState(); 		// default (solved) cube of type POCKET
		m_action = new ACTIONS(9);		// 9 codes 'not known'
		setAvailableActions();
	}

	/**
	 * 
	 * @param fcol is an array with face colors, see {@link CubeState}.
	 */
	public StateObserverCube(BoardVector boardVector) {
		m_state = new CubeState(boardVector);
		m_action = new ACTIONS(9);		// 9 codes 'not known'
		setAvailableActions();
	}
	
	// NOTE: this is NOT the copy constructor. See next method for copy constructor.
	public StateObserverCube(CubeState other) {
		m_state = new CubeState(other);
		m_action = new ACTIONS(9);		// 9 codes 'not known'
		setAvailableActions();
	}
	
	public StateObserverCube(StateObserverCube other) {
		super(other);		// copy members m_counter and stored*
		m_state = new CubeState(other.m_state);
		m_action = new ACTIONS(other.m_action);
		setAvailableActions();
	}
	
	public StateObserverCube copy() {
		return new StateObserverCube(this);
	}

    @Override
	public int getMinEpisodeLength() {
		return m_state.minTwists;
	}

    @Override
	public boolean isGameOver() {
    	boolean pred = (this.m_state.equals(def));
//    	if (pred) {
//    		int dummy=1;	// this is only for a conditional breakpoint
//    	}
		return pred;
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
    	if (!m_state.twistSeq.equals("")) {
    		return m_state.assertTwistSequence();
    	}
		return true;
	}
	
	public boolean isLegalAction(ACTIONS act) {
		return (0<=act.toInt() && act.toInt()<9); 
		
	}

	@Override
    public String stringDescr() {
 		return m_state.toString();
	}
	
	public CubeState getCubeState() {
		return m_state;
	}
	
	/**
	 * @param refer only needed for the interface, not relevant in this 1-person game
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For Rubik's Cube only the game-over state (solved cube) has a non-zero game score. 
	 */
	public double getGameScore(StateObservation refer) {
        if(isGameOver()) return REWARD_POSITIVE;
        return 0; 
	}

	public double getMinGameScore() { return 0; }
	public double getMaxGameScore() { return REWARD_POSITIVE; }

	public String getName() { return "RubiksCube";	}	// should be a valid directory name

	/**
	 * Advance the current state with 'action' to a new state. 
	 * Set the available actions for the new state.
	 * @param action: 0,1,2: UTw; 3,4,5: LTw; 6,7,8: FTw
	 */
	@Override
	public void advance(ACTIONS action) {
		int iAction = action.toInt();
		assert (0<=iAction && iAction<9) : "iAction is not in 0,1,...,8.";
		int j=iAction%3;
		int i=(iAction-j)/3;		// reverse: iAction = 3*i + j
		
		switch(i) {
		case 0: m_state.UTw(j+1); break;
		case 1: m_state.LTw(j+1); break;
		case 2: m_state.FTw(j+1); break;
		}
		this.setAvailableActions();
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

    @Override
	public ArrayList<ACTIONS> getAllAvailableActions() {
        ArrayList allActions = new ArrayList<>();
        for (int j = 0; j < 9; j++) 
        	allActions.add(Types.ACTIONS.fromInt(j));
        
        return allActions;
	}
	
	@Override
	public ArrayList<ACTIONS> getAvailableActions() {
		return acts;
	}
	
	@Override
	public int getNumAvailableActions() {
		return acts.size();
	}

	/**
	 * Given the current state in {@link #m_state}, what are the available actions? 
	 * Set them in member {@code ArrayList<ACTIONS> acts}.
	 * <p>
	 * Note that actions with the same flavor (U,L,F) as the last twist would be in principle also available,
	 * but they could be subsumed with the last twist. So they are omitted in order to reduce complexity. This 
	 * takes place only in the {@link CubeConfig#twistType}{@code ==ALLTWISTS} case. <br>
	 * In the {@code QUARTERTWISTS} case nothing is omitted, since it may be necessary to do the same action again.  
	 */
	public void setAvailableActions() {
		acts.clear();
		if (CubeConfig.twistType==CubeConfig.TwistType.ALLTWISTS) {
			if (m_state.lastTwist!=Twist.U) {
				acts.add(Types.ACTIONS.fromInt(0));  // U1
				acts.add(Types.ACTIONS.fromInt(1));  // U2
				acts.add(Types.ACTIONS.fromInt(2));  // U3
			}
			if (m_state.lastTwist!=Twist.L) {
				acts.add(Types.ACTIONS.fromInt(3));  // L1
				acts.add(Types.ACTIONS.fromInt(4));  // L2
				acts.add(Types.ACTIONS.fromInt(5));  // L3
			}		
			if (m_state.lastTwist!=Twist.F) {
				acts.add(Types.ACTIONS.fromInt(6));  // F1
				acts.add(Types.ACTIONS.fromInt(7));  // F2
				acts.add(Types.ACTIONS.fromInt(8));  // F3
			}		
		} else {   // the QUARTERTWISTS case: add all actions
			for (int i=0; i<9; i++) {
				acts.add(Types.ACTIONS.fromInt(i));  				
			}
		}
	}
	
	public Types.ACTIONS getAction(int i) {
		return acts.get(i);
	}

    public int getPlayer() {
        return 0;
    }

	public int getNumPlayers() {
		return 1;				// the Cube is a one-player puzzle
	}

    public boolean stopInspectOnGameOver() {
    	return false;
    }

}
