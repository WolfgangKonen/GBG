package controllers;

import games.StateObservation;
import games.GameBoard;
import tools.Types;

/**
 * The abstract interface for the game playing agents.
 * <p>
 * Known implementations: <ul>
 * <li> {@link controllers.MinimaxAgent}, 
 * 		{@link controllers.RandomAgent}, 
 * 		{@link controllers.HumanPlayer},
 * 		{@link controllers.MC.MCAgent}		
 * 		{@link controllers.MCTS.MCTSAgentT}		
 * 		{@link controllers.TD.TDAgent}, 
 * </ul> 
 *
 * @author Wolfgang Konen, TH Köln, Nov'16
 */
public interface PlayAgent {
	public enum AgentState {RAW, INIT, TRAINED};
	
	/**
	 * Get the best next action and return it
	 * @param sob			current game state (not changed on return)
	 * @param random		allow epsilon-greedy random action selection	
	 * @param vtable		must be an array of size n+1 on input, where 
	 * 						n=sob.getNumAvailableActions(). On output,
	 * 						elements 0,...,n-1 hold the score for each available 
	 * 						action (corresponding to sob.getAvailableActions())
	 * 						In addition, vtable[n] has the score for the 
	 * 						best action.
	 * @param silent
	 * @return actBest		the best action 
	 * 
	 * Side effect: sets member randomSelect (true: if action was selected 
	 * at random, false: if action was selected by agent).
	 * See {@link #wasRandomAction()}.
	 */	
	public Types.ACTIONS getNextAction(StateObservation sob, boolean random, 
			double[] vtable, boolean silent);
	
	/**
	 * Return the agent's score for that game state.
	 * @param sob			the current game state;
	 * @return				the agent's estimate of the final score for that state. 
	 * 						For 2-player games this is usually the probability 
	 * 						that the player to move wins from that state.
	 * 						If game is over: the score for the player who *would*
	 * 						move (if the game were not over).<p>
	 * Each player wants to maximize *its* score.	 
	 * 				    	
	 */
	public double getScore(StateObservation sob);
	
	/**
	 * Return the agent's estimate of the final game value (final reward) 
	 * @param sob			the current game state;
	 * @return				the agent's estimate of the final reward. This may be 
	 * 						the same as {@link #getScore(StateObservation)} (as 
	 * 						implemented in {@link AgentBase}). But it may as well be 
	 * 						overridden by derived classes.
	 */
	public double estimateGameValue(StateObservation sob);
	
	/**
	 * 
	 * @return	returns true/false, whether the action suggested by last call 
	 * 			to {@link #getNextAction(StateObservation, boolean, double[], boolean)} 
	 *          was a random action 
	 */
	public boolean wasRandomAction(); 
	
	/**
	 * Train the Agent for one complete game episode. <p>
	 * Side effects: Increment m_GameNum by +1. Change the agent's internal  
	 * parameters (weights and so on).
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState01()} to get
	 * 					some exploration of different game paths)
// --- epiLength, learnFromRM are now available via the agent's member ParOther m_oPar: ---
//	 * @param epiLength	maximum number of moves in an episode. If reached, stop training 
//	 * 					prematurely.  
//	 * @param learnFromRM if true, learn from random moves during training
	 * @return			true, if agent raised a stop condition (only CMAPlayer)	 
	 */
	public boolean trainAgent(StateObservation so /*, int epiLength, boolean learnFromRM*/);
	
	public String printTrainStatus();
	public String stringDescr();
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
	 * (probably obsolete now since we have ParOther m_oPar as part of every agent)
	 * 
	 * @return During training: Call the Evaluator after this number of training games
	 */
	@Deprecated
	public int getNumEval();
	public void setMaxGameNum(int num);
	public void setGameNum(int num);
	public void setNumEval(int num);
	
	public AgentState getAgentState(); 
	public void setAgentState(AgentState aState);

	public String getName();
	public void setName(String name);
	
}
