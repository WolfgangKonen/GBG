package controllers;

import games.StateObservation;
import games.XArenaMenu;
import params.ParOther;
import agentIO.LoadSaveGBG;
import games.Arena;
import games.Evaluator;
import games.GameBoard;
import tools.ScoreTuple;
import tools.Types;

/**
 * The abstract interface for game playing agents.
 * <p>
 * See {@link AgentBase} for a base implementation of this interface. Other implementing classes 
 * should usually inherit from {@link AgentBase}.
 *
 * @author Wolfgang Konen, TH Koeln
 */
public interface PlayAgent {
	public enum AgentState {RAW, INIT, TRAINED};
	
//	/**
//	 * <em> This function is now deprecated. Use instead: </em>
//	 * {@code ACTION_VT} {@link PlayAgent#getNextAction2(StateObservation, boolean, boolean)}. 
//	 * <p>
//	 * Get the best next action and return it
//	 * @param sob			current game state (not changed on return)
//	 * @param random		allow epsilon-greedy random action selection	
//	 * @param vtable		must be an array of size n+1 on input, where 
//	 * 						n=sob.getNumAvailableActions(). On output,
//	 * 						elements 0,...,n-1 hold the score for each available 
//	 * 						action (corresponding to sob.getAvailableActions())
//	 * 						In addition, vtable[n] has the score for the 
//	 * 						best action.
//	 * @param silent
//	 * @return actBest		the best action 
//	 * 
//	 * Side effect: sets member randomSelect (true: if action was selected 
//	 * at random, false: if action was selected by agent).
//	 * 
//	 */	
//	@Deprecated
//	public Types.ACTIONS getNextAction(StateObservation sob, boolean random, 
//			double[] vtable, boolean silent);
	
	/**
	 * Get the best next action and return it 
	 * (NEW version: returns ACTIONS_VT and has a recursive part for multi-moves)
	 * 
	 * @param sob			current game state (is returned unchanged)
	 * @param random		allow random action selection with probability m_epsilon
	 * @param silent
	 * @return actBest		the best action. If several actions have the same
	 * 						score, break ties by selecting one of them at random. 
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable to store the value for each available
	 * action (as returned by so.getAvailableActions()) and vBest to store the value for the best action actBest.
	 */
	public Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent);
	
	/**
	 * Return the agent's estimate of the final score for that game state.
	 * @param sob			the current game state;
	 * @return				the agent's estimate of the final score for that state. 
	 * 						For 2-player games this is usually the probability 
	 * 						that the player to move wins from that state.
	 * 						If game is over: the score for the player who *would*
	 * 						move (if the game were not over).<p>
	 * Each player wants to maximize *its* score.	 
	 */
	public double getScore(StateObservation sob);
	
	/**
	 * 
	 * @param sob			the current game state
	 * @param prevTuple		for N &ge; 3 player, we only know the game value for the player who <b>created</b>
	 * 						{@code sob}. To provide also values for other players, {@code prevTuple} allows
	 * 						to pass in such other players' value from previous states, which may serve 
	 * 						as surrogate for the unknown values in {@code sob}. {@code prevTuple} may be {@code null}. 
	 * @return				the agent's estimate of the final reward <b>for all players</b>. 
	 * 						The return value is a tuple containing  
	 * 						{@link StateObservation#getNumPlayers()} {@code double}'s. 
	 */
	public ScoreTuple getScoreTuple(StateObservation sob, ScoreTuple prevTuple);
	
	/**
	 * Return the agent's estimate of the final game value (final reward). Is called when
	 * maximum episode length (TD) or maximum tree depth for certain agents ({@link MaxNAgent}, 
	 * {@link ExpectimaxNAgent}) is reached.
	 * 
	 * @param sob			the current game state;
	 * @return				the agent's estimate of the final reward. This may be 
	 * 						the same as {@link #getScore(StateObservation)} (as 
	 * 						implemented in {@link AgentBase}). But it may as well be 
	 * 						overridden by derived classes.
	 */
	public double estimateGameValue(StateObservation sob);
	
	/**
	 * Return the agent's estimate of {@code sob}'s final game value (final reward) <b>for all players</b>. 
	 * Is called when maximum episode length (TD) or maximum tree depth for certain agents 
	 * ({@link MaxNAgent}, {@link ExpectimaxNAgent}) is reached.
	 * 
	 * @param sob			the current game state
	 * @param prevTuple		for N &ge; 3 player, we only know the game value for the player who <b>created</b>
	 * 						{@code sob}. To provide also values for other players, {@code prevTuple} allows
	 * 						to pass in such other players' value from previous states, which may serve 
	 * 						as surrogate for the unknown values in {@code sob}. {@code prevTuple} may be {@code null}. 
	 * @return				the agent's estimate of the final reward <b>for all players</b>. 
	 * 						The return value is a tuple containing  
	 * 						{@link StateObservation#getNumPlayers()} {@code double}'s. 
	 */
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple);
	
	/**
	 * Train the Agent for one complete game episode. <p>
	 * Side effects: Increment m_GameNum by +1. Change the agent's internal  
	 * parameters (weights and so on).
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState(PlayAgent)} to get
	 * 					some exploration of different game paths)
	 * @return			true, if agent raised a stop condition (only CMAPlayer - deprecated)	 
	 */
	public boolean trainAgent(StateObservation so /*, int epiLength, boolean learnFromRM*/);
	
	public String printTrainStatus();
	
	/**
	 * @return true, if it is a trainable agent
	 */
	public boolean isTrainable();
	
	/**
	 * @return true, if it was retrained (default: false) <br>
	 * [An agent is called 'retrained' if it was - after 1st training - trained again for a 
	 * second, third, ... time. This possibility for retraining is however for the future, 
	 * it is not yet implemented.]
	 */
	public boolean isRetrained(); 
	
	public void incrementDurationTrainingMs(long incr);	
	public void incrementDurationEvaluationMs(long incr);
	
	/**
	 * Time [ms] needed for training this agent
	 */
	public long getDurationTrainingMs();
	/**
	 * 
	 * Time [ms] needed for evaluations during training
	 */
	public long getDurationEvaluationMs();
	
	/**
	 * If agents need a special treatment after being loaded from disk (e. g. instantiation
	 * of transient members), put the relevant code in here.
	 * @return true on success
	 * 
	 * @see LoadSaveGBG#transformObjectToPlayAgent
	 */
	public boolean instantiateAfterLoading();
	
	/**
	 * After loading an agent from disk fill the param tabs of {@link Arena} according to the
	 * settings of this agent
	 * 
	 * @param m_arena	member {@code m_xab} has the param tabs
	 * 
	 * @see Arena#loadAgent
	 */
	public void fillParamTabsAfterLoading(int n, Arena m_arena);
	
	/**
	 * @return a string with information about this agent
	 * @see #stringDescr2()
	 */
	public String stringDescr();
	/**
	 * @return a string with additional information about this agent
	 * @see #stringDescr()
	 */
	public String stringDescr2();
	
	public byte getSize();		// estimated size of agent object

	/**
	 * @return maximum number of training games
	 */
	public int getMaxGameNum();
	/**
	 * @return number of training games that this agent actually has performed
	 */
	public int getGameNum();
	/**
	 * 
	 * @return number of learn actions (calls to update()) for trainable agents.
	 * (For non-trainable agents, {@link AgentBase} will return 0L.)
	 */
	public long getNumLrnActions();
	/**
	 * 
	 * @return number of training moves for trainable agents. Difference to 
	 * {@link #getNumLrnActions()}: it is incremented on random actions as well.
	 * (For non-trainable agents, {@link AgentBase} will return 0L.)
	 */
	public long getNumTrnMoves();
	
	/**
	 * @return number of moves in the last train episode for trainable agents.
	 * (For non-trainable agents, {@link AgentBase} will return 0.)
	 */
	public int getMoveCounter();

	public void setMaxGameNum(int num);
	public void setGameNum(int num);
	
	public ParOther getParOther();
	
	/**
	 * @return During training: Call {@link Evaluator} after this number of training games
	 */
	public int getNumEval();
	public void setNumEval(int num);	
	public void setStopEval(int num);
	
	public AgentState getAgentState(); 
	public void setAgentState(AgentState aState);

	public String getName();
	public void setName(String name);
	
}
