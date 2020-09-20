package games;

import java.util.ArrayList;

import controllers.PlayAgent;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;

/**
 * Class {@link ObserverBase} implements as an <b>abstract</b> class all elements of the interface  
 * {@link StateObservation} that are common to all games (things related to advance, game score, 
 * reward, stored action and value info, ...).
 * <p>
 * This default behavior in {@link ObserverBase} - which may be overridden in derived classes -
 * is for deterministic, 2-player games, where reward and game score are the same. (If one of 
 * the {@code getReward}-functions in {@link ObserverBase} is called with 
 * 		{@code boolean rewardIsGameScore==false}, 
 * a warning is issued.)
 * 
 * @see StateObservation
 */
abstract public class ObserverBase implements StateObservation {
	protected int m_counter = 0;		// move counter
	protected int creatingPlayer = -1;
	
    protected Types.ACTIONS[] storedActions = null;
    protected Types.ACTIONS storedActBest = null;
    protected double[] storedValues = null;
    protected double storedMaxScore; 
    

    public ObserverBase() {    }
    
    public ObserverBase(ObserverBase other) {
		this.m_counter = other.m_counter;
		this.storedMaxScore = other.storedMaxScore;
		this.storedActBest = other.storedActBest;
		this.creatingPlayer = other.creatingPlayer;
		if (other.storedActions!=null) this.storedActions = other.storedActions.clone();
		if (other.storedValues!=null) this.storedValues = other.storedValues.clone();
    }

	public StateObservation clearedCopy() {
    	return this.copy();
	}

	/**
	 * Given the current state, store some useful information for inspecting the  
	 * action actBest and double[] vtable returned by a call to <br>
	 * {@code ACTION_VT} {@link PlayAgent#getNextAction2(StateObservation, boolean, boolean)}. 
	 *  
	 * @param actBest	the best action
	 * @param vtable	one double for each action in {@link #getAvailableActions()}:
	 * 					it stores the value of that action (as given by  <br>
	 * 					{@code double[]} {@link Types.ACTIONS_VT#getVTable()}) 
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
        if (actBest instanceof Types.ACTIONS_VT) {
        	storedMaxScore = ((Types.ACTIONS_VT) actBest).getVBest();
        } else {
            storedMaxScore = vtable[acts.size()];        	
        }
	}

	public Types.ACTIONS getStoredAction(int k) {
		return storedActions[k];
	}
	
	public Types.ACTIONS getStoredActBest() {
		return storedActBest;
	}
	
	public double[] getStoredValues() {
		return storedValues;
	}
	
	/**
	 * This is just to signal that derived classes will be either abstract or implement
	 * getAvailableActions(), as required by the interface {@link StateObservation}.
	 */
	abstract public ArrayList<ACTIONS> getAllAvailableActions();

	/**
	 * This is just to signal that derived classes will be either abstract or implement
	 * getAvailableActions(), as required by the interface {@link StateObservation}.
	 */
	abstract public ArrayList<ACTIONS> getAvailableActions();
	
	protected void advanceBase() {
		this.creatingPlayer = this.getPlayer();
	}

	/**
	 * This is just to signal that derived classes will be either abstract or implement
	 * advance(ACTIONS), as required by the interface {@link StateObservation}.
	 */
	abstract public void advance(Types.ACTIONS action);
	
	/**
     * Advance the current state to a new afterstate (do the deterministic part of advance)
     *
     * @param action the action
     */
    public void advanceDeterministic(Types.ACTIONS action) {
    	// since ObserverBase is for a deterministic game, advanceDeterministic()
    	// is the same as advance():
    	advance(action);
    }

    /**
     * Advance the current afterstate to a new state (do the nondeterministic part of advance)
     */
    public void advanceNondeterministic() {
    	// nothing to do here, since ObserverBase is for a deterministic game    	
    }

	abstract public int getPlayer();
	abstract public int getNumPlayers();
	
	public int getCreatingPlayer() {
		return this.creatingPlayer;
	}

	public int getMinEpisodeLength() {
		return 1;
	}
	
	/**
	 * @return number of moves in the episode where {@code this} is part of.
	 */
	public int getMoveCounter() {
		return m_counter;
	}

	public void resetMoveCounter() {
		m_counter = 0;
	}
	
	protected void incrementMoveCounter() {
		m_counter++;
	}

	/**
	 * The game score, seen from the perspective of {@code referingState}'s player. This 
	 * relativeness is usually only relevant for games with more than one player.
	 * <p>
	 * The keyword abstract signals that derived classes will be either abstract or implement
	 * this method, as required by the interface {@link StateObservation} as well.
	 * 
	 * @param referringState see below
	 * @return  The game score, seen from the perspective of {@code referingState}'s player.<br> 
	 * 			If referringState has opposite player (N=2), then it is getGameScore(this)*(-1). 
	 */
    abstract public double getGameScore(StateObservation referringState);
	

	/**
	 * The game score, seen from the perspective of player {@code player}. The 
	 * perspective shift is usually only relevant for games with more than one player.
	 * <p>
	 * This implementation in {@link ObserverBase} is only valid for 1- or 2-player games.
	 * 
	 * @param player the player whose perspective is taken, a number in 0,1,...,N.
	 * @return  If {@code player} and {@code this.player} are the same, then it is getGameScore().<br> 
	 * 			If they are different, then it is getGameScore()*(-1). 
	 */
	public double getGameScore(int player) {
    	assert (this.getNumPlayers()<=2) : "ObserverBase's implementation of getGameScore(int) is not valid for current class";
		return (this.getPlayer() == player ? this.getGameScore(this) : (-1)*this.getGameScore(this) );
	}
	
	/**
	 * This implementation is valid for all classes implementing {@link StateObservation}, once
	 * they have a valid implementation for {@link #getGameScore(int)}.
	 * 
	 * @return	a score tuple which has as {@code i}th value  {@link #getGameScore(int)}
	 * 			with {@code i} as argument
	 */
	public ScoreTuple getGameScoreTuple() {
		int N = this.getNumPlayers();
		ScoreTuple sc = new ScoreTuple(N);
		for (int i=0; i<N; i++)
			sc.scTup[i] = this.getGameScore(i);
		return sc;
	}

	/**
	 * The cumulative reward, seen from the perspective of {@code referingState}'s player. This 
	 * relativeness is usually only relevant for games with more than one player.
	 * <p> 
	 * The default implementation here in {@link ObserverBase} implements the reward as game score.
	 * 
	 * @param referringState
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different, 
	 * 		  game-specific reward
	 * @return the cumulative reward 
	 */
	public double getReward(StateObservation referringState, boolean rewardIsGameScore) {
		String sWarn = "WARNING getReward: Case rgs==false is not handled in Observerbase!";
		if (rewardIsGameScore==false) {
			System.out.println(sWarn);
//			throw new RuntimeException(sWarn);
		}
		return getGameScore(referringState);
	}

	/**
	 * Same as {@link #getReward(StateObservation,boolean)}, but with the player of referringState.
	 * <p>
	 * The default implementation here in {@link ObserverBase} implements the reward as game score.
	 *  It is only valid for N &le; 2. Games with N &gt; 2 have to override this method.
	 *  
	 * @param player the player of referringState, a number in 0,1,...,N.
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different, 
	 * 		  game-specific reward
	 * @return  the cumulative reward 
	 */
	public double getReward(int player, boolean rewardIsGameScore) {
    	assert (this.getNumPlayers()<=2) : "ObserverBase's implementation of getReward(int,boolean) is not valid for current class";
		return (this.getPlayer() == player ? this.getReward(this,rewardIsGameScore) 
									  : (-1)*this.getReward(this,rewardIsGameScore) );
	}
	
	/**
	 * This implementation is valid for all classes implementing {@link StateObservation}, once
	 * they have a valid implementation for {@link #getReward(int,boolean)}.
	 * 
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different, 
	 * 		  game-specific reward
	 * @return	a score tuple which has as {@code i}th value  
	 * 			{@link #getReward(int, boolean)} with {@code i} as first argument
	 */
	public ScoreTuple getRewardTuple(boolean rewardIsGameScore) {
		int N = this.getNumPlayers();
		ScoreTuple sc = new ScoreTuple(N);
		for (int i=0; i<N; i++)
			sc.scTup[i] = this.getReward(i,rewardIsGameScore);
		return sc;		
	}

	/**
	 * The tuple of delta rewards given by the game environment.<br>
	 * The delta reward is for transition into state {@code this} from a previous state.
	 * <p>
	 * NOTE: Currently the delta reward does NOT include the final reward, which is given by
	 * {@link this#getRewardTuple(boolean)}. On the long run this should be included in the delta reward and make
	 * {@link this#getRewardTuple(boolean)} (the cumulative reward) obsolete.
	 * <p>
	 * This is the default implementation for all classes implementing {@link StateObservation}, unless
	 * they override this method.
	 *
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different,
	 * 		  game-specific reward
	 * @return	a score tuple with all zeros
	 */
	public ScoreTuple getDeltaRewardTuple(boolean rewardIsGameScore) {
		int N = this.getNumPlayers();
		ScoreTuple sc = new ScoreTuple(N);
		for (int i=0; i<N; i++) sc.scTup[i] = 0.0;
		return sc;
	}


	/**
	 * This is just to signal that derived classes will be either abstract or implement
	 * stringDescr(), as required by the interface {@link StateObservation} as well.
	 * The definition of stringDescr() is needed here, because  {@link #toString()} needs it.
	 */
	abstract public String stringDescr();

	/**
	 * 
	 * @return a string representation of action {@code act}
	 */
	public String stringActionDescr(ACTIONS act) {
		return ""+act.toInt();
	}

    public String toString() {
        return stringDescr();
    }
    
    public boolean stopInspectOnGameOver() {
    	return true;
    }
    
}
