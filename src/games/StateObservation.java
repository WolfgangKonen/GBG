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

/**
 * Class StateObservation observes the current state of the game, it has utility functions for
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
 * @see StateObsNondeterministic
 * 
 * @author Wolfgang Konen, TH Koeln, 2017
 */
public interface StateObservation extends Serializable{
    //Types.ACTIONS[] actions=null;
	
	StateObservation copy();

	/**
	 * Some classes implementing StateObservation store information about the history that led to this state.
	 * This is useful in some situations (e.g. Rubik's Cube, when searching the twist sequence to the solved cube
	 * then it is useful to avoid the inverse twist of the last twist taken). But in some other situations,
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
	
//	/**
//	 * @return this predicate is true only for 2-player games where the reward of player 0
//	 * is always the negative of the reward of player 1. E.g. TTT, Hex, where a reward of +1
//	 * for one player means -1 for the other. 
//	 */
//	public boolean has2OppositeRewards();
	
	/**
	 * 
	 * @return a string representation of the current state
	 */
	@Deprecated
	// Why? - Because java.Object has already a default for toString() and thus it can
	// go unnoticed if a class implementing StateObservation does not implement toString().
	// Better use stringDescr()
	String toString();

	/**
	 * 
	 * @return a string representation of the current state
	 */
	String stringDescr();

	/**
	 * 
	 * @return a string representation of action {@code act}
	 */
	String stringActionDescr(ACTIONS act);

	/**
	 * *** This method is deprecated, use instead getGameScore(referringState.getPlayer()) ***
	 * <p>
	 * The game score, seen from the perspective of {@code referringState}'s player. The
	 * perspective shift is only relevant for games with more than one player.
	 * <p>
	 * The keyword abstract signals that derived classes will be either abstract or implement
	 * this method, as required by the interface {@link StateObservation} as well.
	 * 
	 * @param referringState gives the perspective
	 * @return  The game score, seen from the perspective of {@code referringState}'s player.<br>
	 * 			If referringState has opposite player (N=2), then it is getGameScore(this)*(-1). 
	 */
	@Deprecated
	double getGameScore(StateObservation referringState);
	
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

	/**
	 * *** This method is deprecated, use instead getReward(referringState.getPlayer(), rgs) ***
	 * <p>
	 * The cumulative reward, seen from the perspective of {@code referringState}'s player. The
	 * perspective shift is only relevant for games with more than one player.
	 * @param referringState	gives the perspective
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different, 
	 * 		  game-specific reward
	 * @return  the cumulative reward 
	 */
	@Deprecated
	double getReward(StateObservation referringState, boolean rewardIsGameScore);

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
	 */
	ScoreTuple getStepRewardTuple();

	double getMinGameScore();
	double getMaxGameScore();
	
	int getMinEpisodeLength();

	/**
	 * @return number of moves in the episode where {@code this} is part of.
	 */
	int getMoveCounter();

	void resetMoveCounter();
	
	/**
	 *
	 * @return the name of the Game (should be a valid directory name)
	 */
	String getName();

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
     * For deterministic games, the afterstate is identical to {@code this}. For 
     * nondeterministic games, it depends on the game: E.g. for 2048 it is usually not known, 
     * if we know only the current state {@code this}. For Backgammon it is the preceding 
     * board position (if known) without the nondeterministic dice part. 
     */
    StateObservation precedingAfterstate();

	/**
	 * For perfect information games, the partial state is identical to {@code this}).
	 * For imperfect information games: return a state with only the partial information that
	 * the player who moves in this state is allowed to have.
	 * <p>
	 * E.g., for Blackjack, the partial state omits (replaces by null) the hole card of the dealer.
	 * For Poker, the partial state omits (replaces by null) the hole cards of all other players.
	 *
	 * @return <ul>
	 *     <li> For perfect-information games: just {@code this}, and the partial state flag remains false;
	 *     <li> for imperfect-information games: a <b>copy</b> of {@code this} with only partial information
	 *     		(and with partial state flag = true)
	 * </ul>
	 *
	 */
	StateObservation partialState();

	boolean isPartialState();
	void setPartialState(boolean p);

	/**
	 * <ul>
	 *   <li> For perfect-information games: return just {@code this} (see {@link ObserverBase}).
	 *   <li> For imperfect-information games: if {@code this} is a partial state, complete the hidden elements with a
	 * random fill-in method. Imperfect-information games have to <b>override</b> the default implementation in
	 * {@link ObserverBase}.
	 * </ul> <p>
	 * Note that the randomly completed state is in general <b>NOT</b> (!) identical to the full state from which the
	 * partial state was derived. It is - given the observable elements in the partial state -
	 * <b>one</b> of the possibilities that the player has to take into account.
	 * @return the randomly completed state
	 */
	StateObservation randomCompletion();

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
	 * @param vtable	one double for each action in {@link #getAvailableActions()}:
	 * 					it stores the value of that action (as given by the double[] 
	 * 					from {@link Types.ACTIONS_VT#getVTable()}) 
	 */
	void storeBestActionInfo(ACTIONS actBest, double[] vtable);
	
	/**
	 * @return  {0,1,...,n-1} for an n-player game: <b>who moves in this state</b>
	 */
	int getPlayer();
	
	/**
	 * @return the player who created this state
	 */
	int getCreatingPlayer();

//	/**
//	 * @return  1 for a 1-player game (e.g. 2048),  
//	 * 			{+1,-1} for a 2-player game (e.g. TicTacToe): who moves next
//	 * 			{0,1,...,n-1} for an n-player game: who moves next
//	 */
//	@Deprecated
//	public int getPlayerPM();

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
	 * Currently only used by {@link games.Poker.StateObserverPoker}
	 */
	void randomizeStartState();

}
