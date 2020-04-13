package games.RubiksCube;

import games.StateObservation;

/**
 * A class derived from {@link StateObserverCube}. It ensures that whenever an 
 * object of type StateObserverCubeCleared is created, its {@link CubeState} members lastTwist
 * and lastTimes are cleared. This is essential when such an object is used as a start state of 
 * an episode: It guarantees that <b>all</b> possible actions are considered as available.
 * 
 * @see #clearLast(int)
 */
public class StateObserverCubeCleared extends StateObserverCube implements StateObservation {

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;
	
	public StateObserverCubeCleared() {
		super();
		this.resetMoveCounter();
		this.clearLast(0);
	}

	public StateObserverCubeCleared(StateObserverCubeCleared other) {
		super(other);
		this.resetMoveCounter();
		this.clearLast(this.getCubeState().minTwists);
	}

	public StateObserverCubeCleared(StateObserverCube other, int p) {
		super(other);
		this.resetMoveCounter();
		this.clearLast(p);
	}

	/**
	 * Clear {@link CubeState} members lastTwist and lastTimes (which we do not know 
	 * for the initial state in an episode). Then set the available actions, which causes all
	 * 9 actions to be added to {@code this.acts}. We need to test all 9 actions when looking 
	 * for the best next action on {@code this}.
	 * <p>
	 * (If instead lastTwist were set, 3 actions would be excluded. This we do not want for a 
	 * start state.)
	 * @param p
	 * @return
	 */
	public StateObserverCubeCleared clearLast(int p) {
		this.getCubeState().minTwists = p; 
		this.getCubeState().clearLast(); 	// clear lastTwist and lastTimes (which we do not know 
											// for the initial state in an episode)	
		this.setAvailableActions();	// then set the available actions which causes all
									// 9 actions to be added to m_so.acts. We need this
									// to test all 9 actions when looking for the best
									// next action.
		// (If lastTwist were set, 3 actions would be excluded
		// which we do not want for a start state.) 
		return this;
	}

}
