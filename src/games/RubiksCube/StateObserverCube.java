package games.RubiksCube;

import java.io.Serial;
import java.util.*;

import controllers.PlayAgent;
import games.BoardVector;
import games.ObserverBase;
import games.StateObservation;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;

/**
 * This class handles states of Rubik's Cube game, both for 2x2x2 and 3x3x3 cubes, both for QTM and HTM
 * (see {@link CubeConfig}).
 * <p>
 * Class StateObserverCube observes the current state of the game, it has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link StateObservation#advance(ACTIONS, Random)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 * The private member {@link CubeState} {@code m_state} has most part of the state logic for Rubik's Cube.
 * Via {@link CubeStateFactory} the right cube state objects can be generated at runtime.
 *
 * @see CubeConfig
 */
public class StateObserverCube extends ObserverBase implements StateObservation {
	/**
	 * the inner cube state
	 */
	private final CubeState m_state;
	/**
	 * the action which led to {@link #m_state} (if unknown: {@link #iActUnknown} = 9 (2x2x2) or 18 (3x3x3)). The actions
	 * are numbered as follows: <br>
	 * <b>2x2x2 cube:</b>
	 * <br><pre>
	 *   key    { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9}
	 *   action {U1,U2,U3,L1,L2,L3,F1,F2,F3,unknown}
	 * </pre><br>
	 * <b>3x3x3 cube:</b>
	 * <br><pre>
	 *   key    { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,15,16,17,18}
	 *   action {U1,U2,U3,L1,L2,L3,F1,F2,F3,D1,D2,D3,R1,R2,R3,B1,B2,B3,unknown}
	 * </pre>
	 */
	protected ACTIONS m_action;
	/**
	 * the index for an unknown {@link #m_action}
	 */
	public static final int iActUnknown = (CubeConfig.cubeSize == CubeConfig.CubeSize.POCKET) ? 9 : 18;

	/**
	 * the possible QTM actions (twists)
	 */
	private static final int[] quarterActs = (CubeConfig.cubeSize == CubeConfig.CubeSize.POCKET) ?
			new int[]{0, 2, 3, 5, 6, 8} :  							//  {U1,U3,L1,L3,F1,F3}
			new int[]{0, 2, 3, 5, 6, 8, 9, 11, 12, 14, 15, 17};		//  {U1,U3,L1,L3,F1,F3,D1,D3,R1,R3,B1,B3}
	/**
	 * the possible HTM actions (twists)
	 */
	private static final int[] halfTurnActs = (CubeConfig.cubeSize == CubeConfig.CubeSize.POCKET) ?
			new int[]{0,1,2,3,4,5,6,7,8} :  						//  {U1,U2,U3,L1,L2,L3,F1,F2,F3}
			new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};	//  {U1,U2,U3,L1,L2,L3,F1,F2,F3,D1,D2,D3,R1,R2,R3,B1,B2,B3}
	private static final int[] allActs = (CubeConfig.twistType== CubeConfig.TwistType.QTM) ? quarterActs : halfTurnActs;
	//public static final int numAllActions = allActs.length;

	private static final Random rand = new Random(System.currentTimeMillis());
	private static final CubeStateFactory csFactory = new CubeStateFactory();
	private static final CubeState def = csFactory.makeCubeState(); // a solved cube as reference
	private static final StateObserverCube sdef = new StateObserverCube();
	private final ArrayList<ACTIONS> acts = new ArrayList<>();	// holds all available actions

	// deprecated, we have now negative cost-to-go (stepReward)
	@Deprecated
    public static final double REWARD_NEGATIVE = -1.0;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable, or you have to provide a special version transformation)
	 */
	@Serial
	private static final long serialVersionUID = 12L;

	public StateObserverCube() {
		super();
		m_state = csFactory.makeCubeState(); 		// default (solved) cube of type CubeConfig.cubeSize
		m_action = new ACTIONS(iActUnknown);		// iActUnknown (9 or 18) codes 'unknown' (for the generating last action)
		setAvailableActions();
	}

	@Deprecated
	public StateObserverCube(BoardVector boardVector) {
		super();
		m_state = csFactory.makeCubeState(boardVector);
		m_action = new ACTIONS(iActUnknown);		// iActUnknown (9 or 18) codes 'unknown' (for the generating last action)
		setAvailableActions();
	}
	
	// NOTE: this is NOT the copy constructor. See next method for copy constructor.
	public StateObserverCube(CubeState other) {
		super();
		m_state = csFactory.makeCubeState(other);
		m_action = new ACTIONS(iActUnknown);		// iActUnknown (9 or 18) codes 'unknown' (for the generating last action)
		setAvailableActions();
	}
	
	public StateObserverCube(StateObserverCube other) {
		super(other);		// copy members m_counter, lastMoves and stored*
		m_state = csFactory.makeCubeState(other.m_state);
		m_action = new ACTIONS(iActUnknown);		// iActUnknown (9 or 18) codes 'unknown' (for the generating last action)
		setAvailableActions();
	}

	public StateObserverCube copy() {
		return new StateObserverCube(this);
	}

	/**
	 * @return {@code this} with {@code m_action} set to 'unknown' and {@code m_counter=0}
	 */
	@Override
	public StateObservation clearedCopy() {
		this.m_action = ACTIONS.fromInt(iActUnknown);
		this.resetMoveCounter();
		return this;
		//return new StateObserverCubeCleared(this,this.getMinEpisodeLength());
	}

	public StateObservation clearedAction() {
		this.m_action = ACTIONS.fromInt(iActUnknown);
		return this;
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
		int hit=0;
		for (int allAct : allActs)
			if (act.toInt() == allAct) hit = 1;
		return (hit==1);
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
	 * @see CubeConfig#REWARD_POSITIVE
	 */
	public double getGameScore(int player) {
		if(isGameOver()) return CubeConfig.REWARD_POSITIVE;
//		return this.m_counter * CubeConfig.stepReward; // after 2020-09-15, before 2021-10
		return  0; //REWARD_NEGATIVE; // before 2020-09-15
	}

	public double getMinGameScore() { return REWARD_NEGATIVE; }
	public double getMaxGameScore() { return CubeConfig.REWARD_POSITIVE; }

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
	 *
	 * @param player the player, a number in 0,1,...,N-1.
	 * @param rewardIsGameScore (not used here) if true, use game score as reward; if false, use
	 * 		  a different, game-specific reward
	 * @return  the cumulative reward from the perspective of {@code player}
	 */
	public double getReward(int player, boolean rewardIsGameScore) {
		return (this.getCubeState().isEqual(def)) ?  CubeConfig.REWARD_POSITIVE : 0.0;
	}

	/**
	 * The tuple of rewards given by the game environment (excluding step reward).<br>
	 *
	 * @param rewardIsGameScore just for the interface, not relevant here
	 * @return	a score tuple of rewards (excluding step reward)
	 */
	public ScoreTuple getRewardTuple(boolean rewardIsGameScore) {
		//double val = (this.getCubeState().isEqual(def)) ?  CubeConfig.REWARD_POSITIVE : 0.0;
		double val = getReward(0,true);
		return new ScoreTuple(new double[]{val});
	}

	/**
	 * The tuple of step rewards given by the game environment.<p>
	 * The step reward is for transition into state {@code this} from a previous state. It is currently non-zero, only
	 * for {@link StateObserverCube}, namely {@link CubeConfig}.{@code stepReward}.
	 * It is zero for all other classes implementing {@link StateObservation}.
	 * <p>
	 * The step reward ensures that if there are two paths to the solved cube, the one with the lower number of twists
	 * (fewer step rewards) has the higher reward. This is important for tree-based agents, which may completely fail if
	 * they always select the ones with the longer path and never come to an end!
	 *
	 * @return	a score tuple (with as many entries as there are players)
	 */
	public ScoreTuple getStepRewardTuple() {
//	 * @param pa if it is of type {@link NTuple4Base} or {@link NTupleBase} then take stepReward from its element
//	 * 		{@link ParTD} {@code m_tdPar}. For all other agents, take {@link CubeConfig}.{@code stepReward}.
//		double val = (pa instanceof NTuple4Base) ?
//			  		 ((NTuple4Base) pa).getParTD().getStepReward() :
//					 (pa instanceof NTupleBase) ?
//					 ((NTupleBase) pa).getParTD().getStepReward() : CubeConfig.stepReward;
		double val =  CubeConfig.stepReward;
		return new ScoreTuple(new double[]{val});
	}

	// --- obsolete, use Arena().getGameName() instead
//	public String getName() { return "RubiksCube";	}

	/**
	 * Advance the current state with 'action' to a new state. 
	 * Set the available actions for the new state.
     * @param action : 0,1,2: UTw; 3,4,5: LTw; 6,7,8: FTw
     * @param cmpRand
     */
	@Override
	public void advance(ACTIONS action, Random cmpRand) {
		m_action = action;
		int iAction = action.toInt();

		//check validity of action
		int hit=0;
		for (int act : allActs)
			if (iAction == act) {
				hit = 1;
				break;
			}
		assert (hit==1)	: "iAction="+iAction+" is not in set of available actions";

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
        for (int act : allActs)
        	allActions.add(Types.ACTIONS.fromInt(act));
        
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
	 */
	public void setAvailableActions() {
		acts.clear();
		if (CubeConfig.twistType==CubeConfig.TwistType.HTM) {
			for (int halfturnact : halfTurnActs)
				acts.add(ACTIONS.fromInt(halfturnact));
		} else {   // the QTM case: add all allowed quarter twist actions
			for (int quarteract : quarterActs)
				acts.add(ACTIONS.fromInt(quarteract));
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

	/**
	 * Method to select a start state by doing p random twist on the default cube.
	 * @return a p-twisted cube with {@code m_action} set to 'unknown'
	 *
	 * @see DAVI4Agent
	 */
	public static StateObserverCube chooseNewState(int p) {
		//StateObserverCubeCleared d_so;
		assert (p>0) : "[selectByTwists] p has to be larger than 0.";
		int index;
		StateObserverCube so = new StateObserverCube(); // default cube
		int attempts=0;
		while (so.isEqual(sdef)) {		// do another round, if so is after twisting still default state
			attempts++;
			if (attempts % 100==0) {
				throw new RuntimeException("[selectByTwists] no cube different from default found -- may be p=0?? p="+p);
			}
			// make p twists and hope that we land in
			// distance set D[p] (which is often not true for p>5)
			for (int k=0; k<p; k++)  {
				index = rand.nextInt(StateObserverCube.allActs.length);
				so.advance(Types.ACTIONS.fromInt(allActs[index]), null);
			}
		}
		so.m_state.minTwists=p;

		//d_so = new StateObserverCubeCleared(so,p);
		so = (StateObserverCube) so.clearedCopy();

		return so;
	}


	/**
	 * Try to solve cube in {@code so_in} with agent {@code pa}. Return number of moves if solved, -1 if not solved.
	 * @param pa
	 * @param so_in
	 * @param value     the value V({@code so_in}) (only for optional printout)
	 * @param epiLength maximum length of episode (1)number of moves
	 * @param verbose   whether to do optional printout
	 * @return the solution length (# of moves). -1 if not solved in {@code epiLength} moves.
	 */
	public static int solveCube(PlayAgent pa, StateObservation so_in,
								double value, int epiLength, boolean verbose)
	{
		StateObservation so = so_in.copy();
		so.resetMoveCounter();

		pa.resetAgent();			// needed if pa is MCTSWrapperAgent

		while (!so.isGameOver() && so.getMoveCounter()<epiLength) {
			so.advance(pa.getNextAction2(so.partialState(), false, false, true), null);
		}

		int plength = so.getMoveCounter();
		if (so.isGameOver()) {
			if (verbose)
				System.out.print("Solved cube with value " + value + " in  " + so.getMoveCounter() + " twists.\n");
		} else {
			plength=-1;
			if (verbose)
				System.out.print("Could not solve cube with value " + value + " in  " + epiLength + " twists.\n");
		}

		return plength;
	} // solveCube

}
