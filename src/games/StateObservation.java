package games;

import java.io.Serializable;
import java.util.ArrayList;

import TournamentSystem.TSTimeStorage;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.TD.ntuple2.*;
import controllers.TD.ntuple4.*;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import controllers.ExpectimaxNAgent;
import controllers.MaxNAgent;


/**
 * Interface StateObservation observes the current state of the game, it has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link #advance(Types.ACTIONS)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * <li> and others.
 * </ul><p>
 * 
 * StateObservation is for deterministic games.
 *
 * @see PartialState
 * @see StateObsNondeterministic
 *
 * @author Wolfgang Konen, TH Koeln, 2017-2021
 */
public interface StateObservation extends PartialState, Serializable{

	StateObservation copy();

	/**
	 * Some classes implementing StateObservation store information about the history that led to this state.
	 * This is useful in some situations (e.g. Rubik's Cube: When searching the twist sequence to the solved cube,
	 * it is useful to avoid the inverse twist of the last twist taken). But in some other situations,
	 * history information should be cleared, i.e. a cleared copy is needed.
	 * <p>
	 * ObserverBase provides a default implementation which just returns a copy of {@code this}.
	 * @return a cleared copy of {@code this}
	 */
	StateObservation clearedCopy();

	boolean isGameOver();

	boolean isDeterministicGame();
	boolean isImperfectInformationGame();
	boolean isRoundBasedGame();


	/**
	 * @return true, if the game emits rewards only in a game-over game position
	 */
	boolean isFinalRewardGame();

	boolean isLegalState();

	/**
	 * @return true, if inspection is stopped on game over (the normal case), false if not (e.g. the Rubik's Cube case,
	 * because here we start inspection with the solved cube)
	 */
	boolean stopInspectOnGameOver();
	
	/**
	 * Why is {@code toString} deprecated? - Because java.Object implements already {@code toString}  and thus it can
	 * go unnoticed if a class implementing StateObservation does not implement {@code toString}.
	 * Better use {@code stringDescr} instead.
	 *
	 * @return a string representation of the current state
	 */
	@Deprecated
	String toString();

	/**
	 * @return a string representation of the current state
	 */
	String stringDescr();

	/**
	 * Two different states need to have a different string description AND each state should have only one string
	 * description. Needed for hash map in {@link MaxNAgent} and {@link ExpectimaxNAgent}.
	 * <p>
	 * (For round-based games we follow the convention that the string should uniquely describe the state of the current
	 * round, not of the whole episode.)
	 *
	 * @return	a unique string description of the state
	 */
	String uniqueStringDescr();

	/**
	 * 
	 * @return a string representation of action {@code act}
	 */
	String stringActionDescr(ACTIONS act);

//	/**
//	 * *** This method is deprecated, use instead getGameScore(referringState.getPlayer()) ***
//	 * <p>
//	 * The game score, seen from the perspective of {@code referringState}'s player. The
//	 * perspective shift is only relevant for games with more than one player.
//	 * <p>
//	 * The keyword abstract signals that derived classes will be either abstract or implement
//	 * this method, as required by the interface {@link StateObservation} as well.
//	 *
//	 * @param referringState gives the perspective
//	 * @return  The game score, seen from the perspective of {@code referringState}'s player.<br>
//	 * 			If referringState has opposite player (N=2), then it is getGameScore(this)*(-1).
//	 */
//	@Deprecated
//	double getGameScore(StateObservation referringState);
	
	/**
	 * The game score, seen from the perspective of {@code player}. The
	 * perspective shift is only relevant for games with more than one player.
	 * <p>
	 * @param player the player whose perspective is taken, a number in 0, 1,..., N={@link #getNumPlayers()}.
	 * @return  The game score, seen from the perspective of {@code player}.<br>
	 * 			For 2-player games we have usually {@code getGameScore(0) = -getGameScore(1)}.
	 */
	double getGameScore(int player);
	
	/**
	 * @return	a score tuple which has as {@code i}th value  {@link #getGameScore(int)} 
	 * 			with {@code i} as argument
	 */
	ScoreTuple getGameScoreTuple();

//	/**
//	 * *** This method is deprecated, use instead getReward(referringState.getPlayer(), rgs) ***
//	 * <p>
//	 * The cumulative reward, seen from the perspective of {@code referringState}'s player. The
//	 * perspective shift is only relevant for games with more than one player.
//	 * @param referringState	gives the perspective
//	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different,
//	 * 		  game-specific reward
//	 * @return  the cumulative reward
//	 */
//	@Deprecated
//	double getReward(StateObservation referringState, boolean rewardIsGameScore);

	/**
	 * The cumulative reward, seen from the perspective of {@code player}. The
	 * perspective shift is only relevant for games with more than one player.
	 *
	 * @param player the player of referringState, a number in 0, 1,..., N={@link #getNumPlayers()}.
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different, 
	 * 		  game-specific reward
	 * @return  the cumulative reward 
	 */
	double getReward(int player, boolean rewardIsGameScore);

	/**
	 * The tuple of cumulative rewards.
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different, 
	 * 		  game-specific reward
	 * @return	a score tuple which has as {@code i}th value  
	 * 			{@link #getReward(int, boolean)} with {@code i} as first argument
	 */
	ScoreTuple getRewardTuple(boolean rewardIsGameScore);

	/**
	 * The tuple of step rewards given by the game environment.<br>
	 * The step reward is for transition into state {@code this} from a previous state.
	 * <p>
	 * NOTE: Currently the step reward does NOT include the final reward, which is given by
	 * {@link #getRewardTuple(boolean)}. It is non-null only for StateObserverCube. It is a separate
	 * method, because MaxN2Wrapper needs the separate step reward when returning from recursion
	 *
	 * @return	a score tuple
	 * @param pa
	 */
	ScoreTuple getStepRewardTuple(PlayAgent pa);

	double getMinGameScore();
	double getMaxGameScore();
	
	int getMinEpisodeLength();

	/**
	 * @return number of moves in the episode where {@code this} is part of.
	 */
	int getMoveCounter();

	void resetMoveCounter();
	
	// --- obsolete, use Arena().getGameName() instead
//	/**
//	 *
//	 * @return the name of the Game (should be a valid directory name)
//	 */
//	String getName();

	/**
	 * Advance the current state with {@code action} to a new state
	 * 
	 * @param action the action
	 */
	void advance(ACTIONS action);

    /**
     * Advance the current state to a new afterstate (do the deterministic part of advance).<p>
     *
     * (This method is not really necessary for deterministic games - it does the same as 
     * {@link #advance(ACTIONS)} - but we have it in the interface to allow the same syntax in 
     * {@link TDNTuple3Agt} when making an action for any StateObservation, deterministic 
     * or nondeterministic.)
     * 
     * @param action the action
     */
	void advanceDeterministic(ACTIONS action);

    /**
     * Advance the current afterstate to a new state (do the nondeterministic part of advance).<p>
     * 
     * (This method is not really necessary for deterministic games - then it does just nothing - but we
     * have it here to allow the same syntax in {@link TDNTuple3Agt} and {@link TDNTuple4Agt} when making an action for
     * any StateObservation, deterministic or nondeterministic.)
     */
	Types.ACTIONS advanceNondeterministic();

	/**
	 * Return true if the next action is deterministic.<p>
	 *
	 * (This method is not really necessary for deterministic games - then it returns always true - but we
	 * have it here to allow the same syntax in {@link TDNTuple3Agt} and {@link TDNTuple4Agt} when making an action for
	 * any StateObservation, deterministic or nondeterministic.)
	 */
	boolean isNextActionDeterministic();

	/**
     * Return the afterstate preceding {@code this}. The afterstate is the state resulting 
     * after the deterministic part of the preceding action. Return {@code null}, if this
     * afterstate is not known.
     *  
     * @return the afterstate or {@code null}. <p>
     * 
     * For deterministic games, the preceding afterstate is identical to {@code this}. For
     * nondeterministic games, it depends on the game: E.g. for 2048 it is usually not known
     * if we know only the current state {@code this}. For Backgammon, it is the preceding
     * board position (if known) without the nondeterministic dice part. 
     */
    StateObservation precedingAfterstate();

	boolean isRoundOver();
	void setRoundOver(boolean p);

	/**
	 * For games with rounds: If we have a round-over state, it holds the final state of a round (just for display
	 * purposes). With initRound we advance such a state to the initial state of a new round.
	 * <p>
	 * For games w/o rounds, this method should be never called.
	 */
	void initRound();

    /**
     * Return all available actions (all actions that can ever become possible in this game)
     * @return {@code ArrayList<ACTIONS>}
     */
	ArrayList<ACTIONS> getAllAvailableActions();

	/**
	 * Return the actions available in this specific state
	 * @return {@code ArrayList<ACTIONS>}
	 */
	ArrayList<ACTIONS> getAvailableActions();

	int getNumAvailableActions();

	/**
	 * Given the current state, what are the available actions? 
	 * Set them in member ACTIONS[] actions.
	 */
	void setAvailableActions();
	
	Types.ACTIONS getAction(int i);

	/**
	 * If the current player cannot move (or decides not to move), this method passes the state to the next allowed
	 * player.
	 */
	void passToNextPlayer();

	/**
	 * Given the current state, store some info useful for inspecting the  
	 * action actBest and double[] vtable returned by a call to <br>
	 * {@code ACTION_VT} {@link PlayAgent#getNextAction2(StateObservation, boolean, boolean)}. 
	 *  
	 * @param actBest	the best action
	 */
// -- this is obsolete now:
//	 		* @param vtable	one double for each action in {@link #getAvailableActions()}:
//			* 					it stores the value of that action (as given by  <br>
//			* 					{@code double[]} {@link Types.ACTIONS_VT#getVTable()})
	void storeBestActionInfo(Types.ACTIONS_VT actBest); //, double[] vtable);

	Types.ACTIONS_VT getStoredActBest();

	ScoreTuple getStoredBestScoreTuple();

	/**
	 * @return  {0,1,...,n-1} for an n-player game: <b>who moves in this state</b>
	 */
	int getPlayer();
	
	/**
	 * @return the player who created this state
	 */
	int getCreatingPlayer();

	/**
	 * @return  1 for a 1-player game (e.g. 2048), 2 for a 2-player game
	 * 			(e.g. TicTacToe) and so on
	 */
	int getNumPlayers();

	/**
	 * Signals for {@link XArenaFuncs#competeNPlayer(PlayAgtVector, StateObservation, int, int, TSTimeStorage[]) XArenaFuncs.competeNPlayer}
	 * whether the start state needs randomization when doing such a competition.
	 * <p>
	 * Currently only used by {@link games.Poker.StateObserverPoker}
	 *
	 * @return true or false
	 */
	boolean needsRandomization();

	/**
	 *  Randomize the start state in {@link XArenaFuncs#competeNPlayer(PlayAgtVector, StateObservation, int, int, TSTimeStorage[]) XArenaFuncs.competeNPlayer}
	 *  if {@link #needsRandomization()} returns true
	 * <p>
	 * Currently, only used by {@link games.Poker.StateObserverPoker}
	 */
	void randomizeStartState();

}
