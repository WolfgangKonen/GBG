package games.RubiksCube;

import java.util.ArrayList;

import controllers.TD.ntuple2.TDNTuple3Agt;
import games.BoardVector;
import games.ObserverBase;
import games.StateObservation;
import tools.ScoreTuple;
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
	private final CubeState m_state;
	/**
	 * the action which led to m_state (9 if not known)
	 */
	protected ACTIONS m_action;
	private static final CubeStateFactory csFactory = new CubeStateFactory();
	private static final CubeState def = csFactory.makeCubeState(); // a solved cube as reference
	/**
	 * The reward for the solved cube is 1.5. It is higher than the usual game-won reward 1.0, because some agents (e.g.
	 * {@link TDNTuple3Agt}) produce game values a bit higher than 1.0 for non-solved cube states. REWARD_POSITIVE should 
	 * be well higher than this, so that even with the {@code m_counter}-subtraction in {@link #getGameScore(StateObservation) 
	 * getGameScore} there remains a game score higher than for any non-solved cube state.
	 */
    public static final double REWARD_POSITIVE =  1.0; //see daviValue. Earlier it was 1.5;
	/**
	 * The game score as long as the solved cube is not found
	 */
    public static final double REWARD_NEGATIVE = -1.0;
	private final ArrayList<ACTIONS> acts = new ArrayList<>();	// holds all available actions
   
//	private double prevReward = 0.0;
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	public StateObserverCube() {
		m_state = csFactory.makeCubeState(); 		// default (solved) cube of type POCKET
		m_action = new ACTIONS(9);		// 9 codes 'not known'
		setAvailableActions();
	}

	@Deprecated
	public StateObserverCube(BoardVector boardVector) {
		m_state = csFactory.makeCubeState(boardVector);
		m_action = new ACTIONS(9);		// 9 codes 'not known'
		setAvailableActions();
	}
	
	// NOTE: this is NOT the copy constructor. See next method for copy constructor.
	public StateObserverCube(CubeState other) {
		m_state = csFactory.makeCubeState(other);
		m_action = new ACTIONS(9);		// 9 codes 'not known'
		setAvailableActions();
	}
	
	public StateObserverCube(StateObserverCube other) {
		super(other);		// copy members m_counter and stored*
		m_state = csFactory.makeCubeState(other.m_state);
		m_action = new ACTIONS(other.m_action);
		setAvailableActions();
	}
	
	public StateObserverCube copy() {
		return new StateObserverCube(this);
	}

	@Override
	public StateObservation clearedCopy() {
		return new StateObserverCubeCleared(this,this.getMinEpisodeLength());
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
//		return m_state.getTwistSeq();	// this would be necessary for hash map in MaxNAgent, but is problematic in other cases
	}
	
	public CubeState getCubeState() {
		return m_state;
	}

	public ACTIONS getLastAction() {
		return m_action;
	}

	/**
	 * The game score of state {@code this}, seen from the perspective of {@code refer}'s player. 
	 * For Rubik's Cube only the game-over state (solved cube) has a non-zero game score
	 * <pre>
	 *       REWARD_POSITIVE </pre>
	 * <p>
	 * 
	 * @param refer only needed for the interface, not relevant in this 1-person game
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 	
	 * @see #REWARD_POSITIVE		
	 */
	public double getGameScore(StateObservation refer) {
		if(isGameOver()) return REWARD_POSITIVE; // + this.m_counter * CubeConfig.stepReward;
//		return this.m_counter * CubeConfig.stepReward; // after 2020-09-15
		return  0; //REWARD_NEGATIVE;		// earlier version
	}

	public double getMinGameScore() { return REWARD_NEGATIVE; }
	public double getMaxGameScore() { return REWARD_POSITIVE; }

	/**
	 * The tuple of step rewards given by the game environment.<br>
	 * The step reward is for transition into state {@code this} from a previous state.
	 * <p>
	 * The step reward ensures that if there are two paths to the solved cube, the one with the lower number of twists
	 * (fewer step rewards) has the higher reward. This is important for tree-based agents, which may completely fail if
	 * they always select the ones with the longer path and never come to an end!
	 *
	 *
	 * @return	a score tuple
	 */
	public ScoreTuple getStepRewardTuple() {
		double val = CubeConfig.stepReward;
//		if (this.getCubeState().isEqual(def))
//			val += REWARD_POSITIVE;
		ScoreTuple sc = new ScoreTuple(new double[]{val});
		return sc;
	}

	/**
	 * The tuple of rewards given by the game environment (excluding step reward).<br>
	 *
	 * @param rewardIsGameScore just for the interface, not relevant here
	 * @return	a score tuple of rewards (excluding step reward)
	 */
	public ScoreTuple getRewardTuple(boolean rewardIsGameScore) {
		double val = (this.getCubeState().isEqual(def)) ?  REWARD_POSITIVE : 0.0;
		ScoreTuple sc = new ScoreTuple(new double[]{val});
		return sc;
	}

	public String getName() { return "RubiksCube";	}	// should be a valid directory name

	/**
	 * Advance the current state with 'action' to a new state. 
	 * Set the available actions for the new state.
	 * @param action: 0,1,2: UTw; 3,4,5: LTw; 6,7,8: FTw
	 */
	@Override
	public void advance(ACTIONS action) {
//		prevReward = this.getGameScore(this);		// prior to advance() we set prevReward to the game score of 
//													// the current state. In this way, the cumuluative game score 
//													// returned by getGameScore can accumulate the cost-to-go.
		m_action = action;
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
        ArrayList<ACTIONS> allActions = new ArrayList<>();
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
			// we change the former behavior and allow all actions, because it is cumbersome in the search to omit actions
			// (which might be the right ones due to earlier errors). Instead we forbid now only in DAVI3.getNextAction2
			// the inverse of the last action m_action in order to avoid cycles of 2.
//			if (m_state.lastTwist!=Twist.U) {
//				acts.add(Types.ACTIONS.fromInt(0));  // U1
//				acts.add(Types.ACTIONS.fromInt(1));  // U2
//				acts.add(Types.ACTIONS.fromInt(2));  // U3
//			}
//			if (m_state.lastTwist!=Twist.L) {
//				acts.add(Types.ACTIONS.fromInt(3));  // L1
//				acts.add(Types.ACTIONS.fromInt(4));  // L2
//				acts.add(Types.ACTIONS.fromInt(5));  // L3
//			}		
//			if (m_state.lastTwist!=Twist.F) {
//				acts.add(Types.ACTIONS.fromInt(6));  // F1
//				acts.add(Types.ACTIONS.fromInt(7));  // F2
//				acts.add(Types.ACTIONS.fromInt(8));  // F3
//			}		
			for (int i=0; i<9; i++) {
				acts.add(Types.ACTIONS.fromInt(i));  				
			}
		} else {   // the QUARTERTWISTS case: add all allowed quarter twist actions
			int[] quarteracts = {0,2,3,5,6,8};  // {U1,U3,L1,L3,F1,F3}
			for (int i=0; i<quarteracts.length; i++) {
				acts.add(Types.ACTIONS.fromInt(quarteracts[i]));  				
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

	/**
	 * Checks whether elements of members fcol, sloc and type are the same in {@code this} and {@code other}.
	 * (This differs from {@link Object#equals(Object)}, since the latter tests, whether 
	 * the objects are the same, not their content.)
	 */
	public boolean isEqual(StateObserverCube other) {
		return this.m_state.isEqual(other.m_state);
	}
	
	/**
	 * It is important that {@link Object#equals(Object)} is overwritten here, so that objects
	 * of class StateObserverCube which have the same m_state are considered as
	 * equal. The operation equals is the one that HashSet::add() relies on
	 * 
	 * @see #hashCode()
	 */
	@Override
	public boolean equals(Object other) {
		assert (other instanceof StateObserverCube) : "Object other is not of class StateObserverCube";
		return isEqual((StateObserverCube) other);
	}
	
	/**
	 * Like with {@link StateObserverCube#equals(Object)}, it is equally important that {@link Object#hashCode()} is overwritten here in such a way
	 * that it returns the same hash code for objects with the same content (in m_state). 
	 * Since the equality check for inserting an object into a Set (HashSet) is based on 
	 * sameness of equals() AND hashCode() (!!)  
	 * <p> 
	 * See <a href="https://stackoverflow.com/questions/6187294/java-set-collection-override-equals-method/11577351">
	 *     https://stackoverflow.com/questions/6187294/java-set-collection-override-equals-method/11577351</a>
	 *     
	 * @see Object#hashCode()    
	 * @see #equals(Object)    
	 */
	@Override
	public int hashCode() {
		return this.m_state.toString().hashCode();
	}
}
