package games;

import controllers.PlayAgent;
import games.RubiksCube.CubeConfig;
import games.RubiksCube.GameBoardCube;

import java.util.LinkedList;
import java.util.Random;

/**
 * Each class implementing interface GameBoard has the board game GUI. 
 * It shows board game states, optionally the values of possible next actions,
 * and allows user interactions with the board to enter legal moves during 
 * game play or to enter board positions for which the agent reaction is 
 * inspected. 
 * <p>
 * {@link GameBoard} has an internal object derived from {@link StateObservation} which represents the 
 * current game state. This game state can be set (setStateObs()), retrieved (getStateObs()),
 * reset-retrieved ({@link #getDefaultStartState(Random)}), or a random start state can be retrieved
 * with {@link #chooseStartState()} or {@link #chooseStartState(PlayAgent)}.
 * <p>
 * {@link GameBoard} has a reference to an object derived from {@link Arena} which allows access to
 * {@link Arena}-specific settings (which players, params, ...)
 *
 * @author Wolfgang Konen, TH Koeln, 2016-2023
 *
 */
public interface GameBoard {
	/**
	 * things to be initialized prior to starting a training 
	 */
	void initialize();
	/**
	 * Set {@link GameBoard}'s state to the default start state and clear the game board
	 * @param boardClear    whether to clear the board
	 * @param vClear        whether to clear the value table
	 * @param cmpRand		if non-null, use this (reproducible) RNG instead of StateObservation's RNG
	 */
	void clearBoard(boolean boardClear, boolean vClear, Random cmpRand);

	/**
	 * update game-specific parameters from {@link Arena}'s param tabs
	 */
	void updateParams();

	/**
	 * things to be done when disposing a GameBoard object
	 */
	void destroy();

	/**
	 * Update {@link GameBoard}'s state to the given state {@code so} and show it and the associated values (labels)
	 * on the game board.
	 * 
	 * @param so	the game state
	 * @param withReset  if true, reset the board prior to updating it to state so
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in state {@code so}).
	 */
	void updateBoard(StateObservation so, boolean withReset
			, boolean showValueOnGameboard);
	void showGameBoard(Arena arena, boolean alignToMain);
	void toFront();
	/**
	 * Is an action requested from Arena (i.e. was human interaction done)?
	 * @return true if action from Arena is requested 
	 */
	boolean isActionReq();
	void setActionReq(boolean actionReq);
	void enableInteraction(boolean enable);

	/**
	 * If logs and agents should be placed in a subdirectory (e.g. Hex: BoardSize), then
	 * this method returns a suitable string. If it returns {@code null}, then logs and 
	 * agents are placed in the {@code gameName} directory directly. 
	 * @return subdir string
	 */
	String getSubDir();
	
	Arena getArena();
	
	/**
	 * @return the 'empty-board' start state
	 * @param cmpRand		if non-null, use this (reproducible) RNG instead of StateObservation's RNG
	 */
	StateObservation getDefaultStartState(Random cmpRand);

	/**
	 * Set {@link GameBoard}'s state to the given state {@code so}.
	 * @param so	the game state
	 */
	void setStateObs(StateObservation so);
	StateObservation getStateObs();
	LinkedList<StateObservation> getStateQueue();

	/**
	 * Choose a random start state. Used when training an agent via self-play.
	 * 
	 * @param pa the agent to be trained, we need it (currently only in Rubik's Cube) 
	 * 			for its {@link PlayAgent#getGameNum()} and {@link PlayAgent#getMaxGameNum()}
	 * 			in order to get the number of training games (this influences the returned
	 * 			state).
	 * @return a) for 2-player games: a start state which is with probability 0.5 the empty board 
	 * 		and with probability 0.5 one of the possible one-ply successors. <br>
	 *   b) for RubiksCube: a random start state which is p twists away from the solved cube. 
	 *      Which p from {1,...,{@link CubeConfig#pMax}} depends on the proportion of training games conducted.
	 *   
	 * @see #chooseStartState()     
	 * @see GameBoardCube    
	 */
	StateObservation chooseStartState(PlayAgent pa);

	/**
	 * Choose a random start state when playing a game.
	 * 
	 * @return a) for 2-player games: a start state which is with probability 0.5 the empty board 
	 * 		and with probability 0.5 one of the possible one-ply successors. <br>
	 *   b) for RubiksCube: a random start state which is p twists away from the solved cube. 
	 *      p is picked uniform-randomly from {1,...,{@link CubeConfig#pMax}}.
	 *      
	 * @see Arena#PlayGame()
	 */
	StateObservation chooseStartState();
}
