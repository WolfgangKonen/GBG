package games;

import java.util.ArrayList;

import TournamentSystem.TSTimeStorage;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
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
	protected boolean m_partialState = false;
	protected boolean m_roundOver=false;

    protected Types.ACTIONS[] storedActions = null;
    protected Types.ACTIONS storedActBest = null;
    protected double[] storedValues = null;
    protected double storedMaxScore;

	public static final long serialVersionUID = 12L;

	/**
	 * The list of last moves in an episode. Each move is stored as {@link Integer} {@code iAction}.
	 */
	public ArrayList<Integer> lastMoves;
	//public transient ArrayList<Integer> lastMoves;
	// /WK/2021-08-15 removed the former 'transient' because the Log facility will not work properly if lastMoves is transient
	// (cannot access lastMoves in getLastMove when re-playing a certain move from the log).
	// The consequence is a larger mem size of StateObservation objects, but we have to live with this.

	public ObserverBase() {
		lastMoves = new ArrayList<Integer>();
	}
    
    public ObserverBase(ObserverBase other) {
		this.m_counter = other.m_counter;
		this.creatingPlayer = other.creatingPlayer;
		this.m_partialState = other.m_partialState;
		this.m_roundOver = other.m_roundOver;
		this.lastMoves = (ArrayList<Integer>) other.lastMoves.clone();		// WK: bug fix, added missing .clone()
		this.storedMaxScore = other.storedMaxScore;
		this.storedActBest = other.storedActBest;
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
	
	protected void advanceBase(ACTIONS action) {
		this.creatingPlayer = this.getPlayer();
		this.addToLastMoves(action);
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

	/**
	 * Advance the current afterstate to a new state (do the nondeterministic part of advance)
	 */
	public void advanceNondeterministic(ACTIONS randAction) {
		// nothing to do here, since ObserverBase is for a deterministic game
	}

	/**
	 * Default implementation for deterministic games, returns always true
	 */
	@Override
	public boolean isNextActionDeterministic() {
		return true;
	}

	/**
	 * Default implementation for deterministic games: the state and its preceding afterstate are the same,
	 * thus return just {@code this}. <br>
	 * Nondeterministic games have to override this method.
	 *
	 * @return the afterstate preceding {@code this}.
	 */
	@Override
	public StateObservation precedingAfterstate() { return this; }

	/**
	 * Default implementation for perfect information games: the partial state that the player-to-move observes is
	 * identical to {@code this}. <br>
	 * Games with imperfect information have to override this method.
	 *
	 * @return the partial information state (here: identical to {@code this})
	 */
	public StateObservation partialState() { return this; }

	public boolean isPartialState() { return m_partialState; }
	public void setPartialState(boolean p) { m_partialState = p; }

	public boolean isImperfectInformationGame() { return false; }

	/**
	 * Default implementation for  perfect information games: there is nothing to do, the perfect-information state
	 * is already complete.
	 *
	 * @return the state {@code this}, it is the completed state
	 */
	public StateObservation randomCompletion() {
		if (isImperfectInformationGame())
			throw new RuntimeException("ObserverBase.randomCompletion is NOT valid for imperfect-information games!");
		return this;
	}

	public boolean isRoundOver() { return m_roundOver; }
	public void setRoundOver(boolean p) { m_roundOver=p; }

	public void initRound() {
		throw new RuntimeException("Games that call initRound should override it!");
	}

	abstract public int getPlayer();
	abstract public int getNumPlayers();

	/**
	 * If the current player cannot move (or decides not to move), this method passes the state to the next allowed
	 * player.
	 */
	public void passToNextPlayer() {
		setPlayer( getNextPlayer() );
	}

	/**
	 * This method implements the simple version to get the next player in cyclic order, assuming that all players stay
	 * in the game until game-over. Other games which have more advanced schemes (e.g. 3-player Sim, where one player
	 * can lose early) have to override this method.
	 *
	 * @return the next player
	 */
	protected int getNextPlayer() {
		return ((getPlayer()+1) % getNumPlayers());
	}

	/**
	 * This method is only needed for games that allow the 'pass-to-next-player'-option (e.g. Othello). It is only
	 * called when passToNextPlayer() is called. It then has to be overridden in the derived class
	 *
	 * We provide a default implementation here that throws a RuntimeException. Why do we not declare it abstract? -
	 * We want GBG to compile even if derived classes that have NO pass-option do not implement setPlayer.
	 * (This results in safer software: Users cannot call unwantedly a dummy implementation of setPlayer().
	 * And they get an Exception if they call passToNextPlayer() on a game that has no pass option.)
	 *
	 * @param p 	the next player
	 */
	public void setPlayer(int p) {
		throw new RuntimeException("setPlayer(int) needs to be overridden by specific game class");
	}
	
	public int getCreatingPlayer() {
		return this.creatingPlayer;
	}

	public void addToLastMoves(ACTIONS act) {
		if (act!=null)
			lastMoves.add(act.toInt());
	}

	public int getLastMove() {
		if (lastMoves.size() == 0) return -1;
		return lastMoves.get(lastMoves.size()-1);
	}

	public ArrayList<Integer> getLastMoves() {
		return lastMoves;
	}

	// never used:
//	public void resetLastMoves() {
//		this.lastMoves = new ArrayList<>();
//	}

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
	 * *** This method is deprecated, use instead getGameScore(referringState.getPlayer()) ***
	 * <p>
	 * The game score, seen from the perspective of {@code referringState}'s player. The
	 * perspective is only relevant for games with more than one player.
	 * <p>
	 * The keyword abstract signals that derived classes will be either abstract or implement
	 * this method, as required by the interface {@link StateObservation} as well.
	 * 
	 * @param referringState see below
	 * @return  The game score, seen from the perspective of {@code referringState}'s player.<br>
	 * 			If referringState has opposite player (N=2), then it is getGameScore(this)*(-1). 
	 */
	@Deprecated
    public double getGameScore(StateObservation referringState) {
		return getGameScore(referringState.getPlayer());
	}

	/**
	 * The game score, seen from the perspective of player {@code player}. The 
	 * perspective shift is usually only relevant for games with more than one player.
	 *
	 * @param player the player of referringState, a number in 0, 1,..., N={@link #getNumPlayers()}.
	 * @return  If {@code player} and {@code this.player} are the same, then it is getGameScore().<br> 
	 * 			If they are different, then it is getGameScore()*(-1). 
	 */
	abstract public double getGameScore(int player);
//	public double getGameScore(int player) {
//    	assert (this.getNumPlayers()<=2) : "ObserverBase's implementation of getGameScore(int) is not valid for current class";
//		return (this.getPlayer() == player ? this.getGameScore(this) : (-1)*this.getGameScore(this) );
//	}
	
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
	 * *** This method is deprecated, use instead getReward(referringState.getPlayer(), rgs) ***
	 * <p>
	 * The cumulative reward, seen from the perspective of {@code referringState}'s player. The
	 * perspective is only relevant for games with more than one player.
	 * <p> 
	 * The default implementation here in {@link ObserverBase} implements the reward as game score.
	 * 
	 * @param referringState	gives the perspective
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different, 
	 * 		  game-specific reward
	 * @return the cumulative reward 
	 */
	@Deprecated
	public double getReward(StateObservation referringState, boolean rewardIsGameScore) {
		String sWarn = "WARNING getReward: Case rgs==false is not handled in ObserverBase!";
		if (!rewardIsGameScore) {
			System.out.println(sWarn);
//			throw new RuntimeException(sWarn);
		}
		return getGameScore(referringState.getPlayer());
	}

	/**
	 * The cumulative reward, seen from the perspective of {@code player}. The
	 * perspective shift is only relevant for games with more than one player.
	 * <p>
	 * The default implementation here in {@link ObserverBase} implements the reward as game score.
	 *
	 * @param player the player of referringState, a number in 0, 1,..., N={@link #getNumPlayers()}.
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different,
	 * 		  game-specific reward
	 * @return  the cumulative reward 
	 */
	public double getReward(int player, boolean rewardIsGameScore) {
		String sWarn = "WARNING getReward: Case rgs==false is not handled in ObserverBase!";
		if (!rewardIsGameScore) {
			System.out.println(sWarn);
//			throw new RuntimeException(sWarn);
		}
		return getGameScore(player);
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
	 * The tuple of step rewards given by the game environment.<br>
	 * The step reward is for transition into state {@code this} from a previous state.
	 * <p>
	 * NOTE: Currently the step reward does NOT include the final reward, which is given by
	 * {@link #getRewardTuple(boolean)}. It is non-null only for StateObserverCube. It is a separate
	 * method, because MaxN2Wrapper needs the separate step reward when returning from recursion
	 * <p>
	 * This is the default implementation for all classes implementing {@link StateObservation}, unless
	 * they override this method.
	 *
	 * @return	a score tuple with 0.0 for all elements
	 */
	public ScoreTuple getStepRewardTuple() {
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

	@Deprecated     // see StateObservation for reason. Better use stringDescr() directly.
    public String toString() {
        return stringDescr();
    }
    
    public boolean stopInspectOnGameOver() {
    	return true;
    }

    public boolean isRoundBasedGame(){return false;}

	/**
	 * Signals for {@link XArenaFuncs#competeNPlayer(PlayAgtVector, StateObservation, int, int, TSTimeStorage[]) XArenaFuncs.competeNPlayer}
	 * whether the start state needs randomization when doing such a competition.
	 *
	 * @return true or false
	 */
	public boolean needsRandomization(){return false;}

	/**
	 *  Randomize the start state in {@link XArenaFuncs#competeNPlayer(PlayAgtVector, StateObservation, int, int, TSTimeStorage[]) XArenaFuncs.competeNPlayer}
	 *  if {@link #needsRandomization()} returns true
	 */
	public void randomizeStartState() { }

}
