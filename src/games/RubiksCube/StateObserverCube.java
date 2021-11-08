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
	 * the action which led to m_state (numAllActions (9 or 18) if not known)
	 */
	protected ACTIONS m_action;
	private static final CubeStateFactory csFactory = new CubeStateFactory();
	private static final CubeState def = csFactory.makeCubeState(); // a solved cube as reference
	/**
	 * The reward for the solved cube is 1.5. It is higher than the usual game-won reward 1.0, because some agents (e.g.
	 * {@link TDNTuple3Agt}) produce game values a bit higher than 1.0 for non-solved cube states. REWARD_POSITIVE should 
	 * be well higher than this, so that even with the {@code m_counter}-subtraction in {@link #getGameScore(int)
	 * getGameScore} there remains a game score higher than for any non-solved cube state.
	 */
    public static final double REWARD_POSITIVE =  1.0; //see daviValue. Earlier it was 1.5;
	/**
	 * The game score as long as the solved cube is not found
	 */
    public static final double REWARD_NEGATIVE = -1.0;
	private final ArrayList<ACTIONS> acts = new ArrayList<>();	// holds all available actions

	public int numAllActions;
   
//	private double prevReward = 0.0;
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	public StateObserverCube() {
		super();
		m_state = csFactory.makeCubeState(); 		// default (solved) cube of type CubeConfig.cubeType
		numAllActions = (CubeConfig.cubeType== CubeConfig.CubeType.POCKET) ? 9 : 18;
		m_action = new ACTIONS(numAllActions);		// numAllActions (9 or 18) codes 'not known'
		setAvailableActions();
	}

	@Deprecated
	public StateObserverCube(BoardVector boardVector) {
		super();
		m_state = csFactory.makeCubeState(boardVector);
		numAllActions = (CubeConfig.cubeType== CubeConfig.CubeType.POCKET) ? 9 : 18;
		m_action = new ACTIONS(numAllActions);		// numAllActions (9 or 18) codes 'not known'
		setAvailableActions();
	}
	
	// NOTE: this is NOT the copy constructor. See next method for copy constructor.
	public StateObserverCube(CubeState other) {
		super();
		m_state = csFactory.makeCubeState(other);
		numAllActions = (CubeConfig.cubeType== CubeConfig.CubeType.POCKET) ? 9 : 18;
		m_action = new ACTIONS(numAllActions);		// numAllActions (9 or 18) codes 'not known'
		setAvailableActions();
	}
	
	public StateObserverCube(StateObserverCube other) {
		super(other);		// copy members m_counter, lastMoves and stored*
		m_state = csFactory.makeCubeState(other.m_state);
		numAllActions = (CubeConfig.cubeType== CubeConfig.CubeType.POCKET) ? 9 : 18;
		m_action = new ACTIONS(numAllActions);		// numAllActions (9 or 18) codes 'not known'
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
		return (0<=act.toInt() && act.toInt()<this.numAllActions);
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
	 * all other states have game score 0.
	 *
	 * @param player only needed for the interface, not relevant in this 1-person game
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 	
	 * @see #REWARD_POSITIVE		
	 */
	public double getGameScore(int player) {
		if(isGameOver()) return REWARD_POSITIVE; // + this.m_counter * CubeConfig.stepReward;
//		return this.m_counter * CubeConfig.stepReward; // after 2020-09-15
		return  0; //REWARD_NEGATIVE;		// earlier version
	}

	public double getMinGameScore() { return REWARD_NEGATIVE; }
	public double getMaxGameScore() { return REWARD_POSITIVE; }

//	/**
//	 * *** This method is deprecated, use instead getReward(referringState.getPlayer(), rgs) ***
//	 * The cumulative reward, seen from the perspective of {@code referringState}'s player. This
//	 * relativeness is usually only relevant for games with more than one player.
//	 * <p>
//	 * The default implementation here in {@link ObserverBase} implements the reward as game score.
//	 *
//	 * @param referringState	the player's perspective
//	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different,
//	 * 		  game-specific reward
//	 * @return the cumulative reward
//	 */
//	@Deprecated
//	public double getReward(StateObservation referringState, boolean rewardIsGameScore) {
//		return (this.getCubeState().isEqual(def)) ?  REWARD_POSITIVE : 0.0;
//	}

	/**
	 * The cumulative reward, seen from the perspective of {@code player}. This
	 * relativeness is usually only relevant for games with more than one player.
	 * <p>
	 * The default implementation here in {@link ObserverBase} implements the reward as game score.
	 *  It is only valid for N &le; 2. Games with N &gt; 2 have to override this method.
	 *
	 * @param player the player, a number in 0,1,...,N.
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different,
	 * 		  game-specific reward
	 * @return  the cumulative reward from the perspective of {@code player}
	 */
	public double getReward(int player, boolean rewardIsGameScore) {
		return (this.getCubeState().isEqual(def)) ?  REWARD_POSITIVE : 0.0;
	}

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
		return new ScoreTuple(new double[]{val});
	}

	/**
	 * The tuple of rewards given by the game environment (excluding step reward).<br>
	 *
	 * @param rewardIsGameScore just for the interface, not relevant here
	 * @return	a score tuple of rewards (excluding step reward)
	 */
	public ScoreTuple getRewardTuple(boolean rewardIsGameScore) {
		double val = (this.getCubeState().isEqual(def)) ?  REWARD_POSITIVE : 0.0;
		return new ScoreTuple(new double[]{val});
	}

	// --- obsolete, use Arena().getGameName() instead
//	public String getName() { return "RubiksCube";	}

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
		assert (0<=iAction && iAction<numAllActions) : "iAction is not in 0,1,...,"+numAllActions;
		int j=iAction%3;
		int i=(iAction-j)/3;		// reverse: iAction = 3*i + j

		switch (i) {
			case 0 -> m_state.UTw(j + 1);
			case 1 -> m_state.LTw(j + 1);
			case 2 -> m_state.FTw(j + 1);
			case 3 -> m_state.DTw(j + 1);
			case 4 -> m_state.RTw(j + 1);
			case 5 -> m_state.BTw(j + 1);
		}
		this.setAvailableActions();
		super.addToLastMoves(action);
		super.incrementMoveCounter();
	}

    @Override
	public ArrayList<ACTIONS> getAllAvailableActions() {
        ArrayList<ACTIONS> allActions = new ArrayList<>();
        for (int j = 0; j < numAllActions; j++)
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
			for (int i=0; i<this.numAllActions; i++) {
				acts.add(Types.ACTIONS.fromInt(i));  				
			}
		} else {   // the QUARTERTWISTS case: add all allowed quarter twist actions
			int[] quarteracts = {0,2,3,5,6,8};  						//  {U1,U3,L1,L3,F1,F3}
			if (CubeConfig.cubeType== CubeConfig.CubeType.RUBIKS)
				quarteracts = new int[]{0,2,3,5,6,8,9,11,12,14,15,17};	//+ {D1,D3,R1,R3,B1,B3}
			for (int quarteract : quarteracts) {
				acts.add(ACTIONS.fromInt(quarteract));
			}
		}
	}
	
	public Types.ACTIONS getAction(int i) {
		return acts.get(i);
	}

    public int getPlayer() {
        return 0;
    }

	// When activating "Wrapper MCTS" then we need setPlayer to avoid an exception caused by execution of a pass-branch.
	// But we should not get with RubiksCube in a pass-branch at all!! --> Clarify! /WK/
//	public void setPlayer(int p) {
//		// dummy, needed for testing MCTSWrapperAgent
//	}

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
