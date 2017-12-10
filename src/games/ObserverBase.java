package games;

import controllers.PlayAgent;
import tools.Types;

/**
 * Class {@link ObserverBase} implements functionality of the interface {@link StateObservation} 
 * common to all agents (things related to advance, game value, reward, ...).
 * <p>
 * The default behavior in {@link ObserverBase} - which may be overridden in derived classes -
 * is for deterministic, 2-player games.
 * 
 * @see StateObservation
 */
abstract public class ObserverBase {
	
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

	/**
	 * This is just to signal that derived classes will be either abstract or implement
	 * getGameScore(), getPlayer(), as required by the interface {@link StateObservation} as well.
	 */
	abstract public double getGameScore();
	abstract public int getPlayer();

	/**
	 * Same as getGameScore(), but relative to referingState. This relativeness
	 * is usually only relevant for games with more than one player.
	 * <p>
	 * This implementation in ObserverBase is valid for 2-player games.
	 * 
	 * @param referringState see below
	 * @return  If referringState has the same player as this, then it is getGameScore().<br> 
	 * 			If referringState has opposite player, then it is getGameScore()*(-1). 
	 */
    public double getGameScore(StateObservation referringState) {
        return (this.getPlayer() == referringState.getPlayer() ? getGameScore() : getGameScore() * (-1));
    }
	

    public double getGameValue() {
        return getGameScore();
    }

	/**
	 * The cumulative reward, usually the same as getGameScore()
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different, 
	 * 		  game-specific reward
	 * @return the cumulative reward
	 */
	public double getReward(boolean rewardIsGameScore) {
		return getGameScore();
	}
	
	/**
	 * Same as getReward(), but relative to referringState. 
	 * @param referringState
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different, 
	 * 		  game-specific reward
	 * @return the cumulative reward 
	 */
	public double getReward(StateObservation referringState, boolean rewardIsGameScore) {
		return getGameScore(referringState);
	}

	/**
	 * Same as getReward(referringState), but with the player of referringState. 
	 * @param player the player of referringState, a number in 0,1,...,N.
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different, 
	 * 		  game-specific reward
	 * @return  the cumulative reward 
	 */
	public double getReward(int player, boolean rewardIsGameScore) {
        return (this.getPlayer() == player ? getGameScore() : getGameScore() * (-1));
	}

	/**
	 * This is just to signal that derived classes will be either abstract or implement
	 * stringDescr(), as required by the interface {@link StateObservation} as well.
	 * The definition of stringDescr() is needed here, because  {@link #toString()} needs it.
	 */
	abstract public String stringDescr();

    public String toString() {
        return stringDescr();
    }
    
}
