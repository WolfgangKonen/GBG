package games;

import java.util.ArrayList;

import controllers.PlayAgent;
import tools.Types;
import tools.Types.ACTIONS;

/**
 * Class StateObservation observes the current state of the game, it has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link #advance(ACTIONS)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * <li> and others.
 * </ul><p>
 * 
 * Note: The five methods {@link #getNumCells()}, {@link #getNumPositionValues()}, 
 * {@link #getBoardVector()}, {@link #symmetryVectors(int[])} and {@link #fixedNTuples()} are 
 * only required for the n-tuple interface. If an implementing class does not need that part 
 * (i. e. if it does not plan to use {@link TDNTupleAgt}), it may just code stubs
 * returning 0, {@code null}, or throwing a RuntimeException.
 *
 * @author Wolfgang Konen, TH Köln, Feb'17
 */
public interface StateObservation {
    //Types.ACTIONS[] actions=null;
	
	public StateObservation copy();

	public boolean isGameOver();

	public boolean isLegalState();
	
	/**
	 * 
	 * @return a string representation of the current state
	 */
	@Deprecated
	// Why? - Because java.Object has already a default for toString() and thus it can
	// go unnoticed if a class implementing StateObservation does not implement toString().
	// Better use stringDescr()
	public String toString();

	/**
	 * 
	 * @return a string representation of the current state
	 */
	public String stringDescr();

	/**
	 * This method should be only called if game is over. The player is 
	 * the player who would be next in turn (if the game were not over) 
	 * @return PLAYER_LOSES(-1), TIE(0), PLAYER_WINS(1)
	 */
	public Types.WINNER getGameWinner();

	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For a 2-player game the score is often only non-zero for a 
	 * 			game-over state.  
	 * 			For a 1-player game it may be the cumulative reward.
	 */
	public double getGameScore();
	
	/**
	 * @return 	the game value, i.e. an <em>estimate of the final score</em> which 
	 * 			will be reached from this state (assuming perfect play).  
	 * 			This member function can be {@link StateObservation}'s heuristic  
	 * 			for the <em>potential</em> of that state. If such a heuristic is not known, 
	 * 			{@link #getGameValue()} might simply return {@link #getGameScore()}.
	 */
	public double getGameValue();
	
	/**
	 * Same as getGameScore(), but relative to referingState. This relativeness
	 * is usually only relevant for 2-player games.
	 * @param referingState
	 * @return  If the referingState was created by White (and Black is to move), 
	 * 			then it is getGameScore(). If referingState was created by Black,
	 * 			then it is getGameScore()*(-1). 
	 */
	public double getGameScore(StateObservation referingState);
	
	public double getMinGameScore();
	public double getMaxGameScore();

	/**
	 * Advance the current state with 'action' to a new state
	 * @param action
	 */
	public void advance(ACTIONS action);

	public ArrayList<ACTIONS> getAvailableActions();

	public int getNumAvailableActions();

	/**
	 * Given the current state, what are the available actions? 
	 * Set them in member ACTIONS[] actions.
	 */
	public void setAvailableActions();
	
	public Types.ACTIONS getAction(int i);
	
	/**
	 * Given the current state, store some info useful for inspecting the  
	 * action actBest and double[] vtable returned by a call to 
	 * {@link PlayAgent#getNextAction(StateObservation, boolean, double[], boolean)}
	 *  
	 * @param actBest	the best action
	 * @param vtable	one double for each action in {@link #getAvailableActions()}:
	 * 					it stores the value of that action (as given by the double[] 
	 * 					in {@code PlayAgent#getNextAction}) 
	 */
	public void storeBestActionInfo(ACTIONS actBest, double[] vtable); 
	
	/**
	 * 
	 * @return  {0,1,...,n-1} for an n-player game: who moves next
	 */
	public int getPlayer();

	/**
	 * @return  1 for a 1-player game (e.g. 2048),  
	 * 			{+1,-1} for a 2-player game (e.g. TicTacToe): who moves next
	 * 			{0,1,...,n-1} for an n-player game: who moves next
	 */
	@Deprecated
	public int getPlayerPM();

	/**
	 * @return  1 for a 1-player game (e.g. 2048), 2 for a 2-player game
	 * 			(e.g. TicTacToe) and so on
	 */
	public int getNumPlayers();
	
}
