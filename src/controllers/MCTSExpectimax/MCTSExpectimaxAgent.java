package controllers.MCTSExpectimax;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;
import params.MCTSParams;
import tools.Types;

import java.util.Random;


/**
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 * and adapted from Diego Perez MCTS implementation http://gvgai.net/cont.php
 *
 * @author Johannes Kutsch
 */
public class MCTSExpectimaxAgent extends AgentBase implements PlayAgent
{
	public MCTSParams params;
    private MCTSExpectimaxPlayer1 player;

	/**
	 * @param name	agent name, should be "MCTS Expectimax"
	 * @param mcPar Settings for the Agent
	 */
    public MCTSExpectimaxAgent(String name, MCTSParams mcPar)
    {
    	super(name);
		params = mcPar;

        player = new MCTSExpectimaxPlayer1(new Random(),mcPar);

        setAgentState(AgentState.TRAINED);
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
	 *
     * @param stateObs Observation of the current state.
	 * @param vtable		the score for each available action (corresponding
	 * 						to sob.getAvailableActions())
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, double[] vtable) {
    	
        //Set the state observation object as the new root of the tree.
        player.init(stateObs);

        //Determine the action using MCTS Expectimax and return it.
        return player.run(vtable);
    }

	/**
	 * Get the best next action and return it
	 *
	 * @param so			current game state (not changed on return)
	 * @param random		allow epsilon-greedy random action selection, currently not implemented
	 * @param vtable		the score for each available action (corresponding to sob.getAvailableActions())
	 * @param silent		verbosity control
	 *
	 * @return actBest		the best action 
	 */	
	@Override
	public Types.ACTIONS getNextAction(StateObservation so, boolean random, double[] vtable, boolean silent) {
		assert so.isLegalState() : "Not a legal state";
    	return act(so,vtable);
	}


	/**
	 * @return	returns true/false, whether the action suggested by last call 
	 * 			to getNextAction() was a random action. 
	 * 			Always false in the case of MCTS Expectimax
	 */
	public boolean wasRandomAction() {
		return false;
	}


	/**
	 * Get the best next action and return its score
	 *
	 * @param so 			current game state (not changed on return)
	 *
	 * @return				the score of the best action
	 */
	public double getScore(StateObservation so) {
		double[] vtable = new double[so.getNumAvailableActions()+1];
        double nextActionScore = Double.NEGATIVE_INFINITY;

        if (so.isGameOver()) {
        	return so.getGameScore(so);
        } else {
    		act(so,vtable);

            for (int i = 0; i < so.getNumAvailableActions(); i++) {
                if (nextActionScore <= vtable[i]) {
                    nextActionScore = vtable[i];
                }
            }

            return nextActionScore;
        }
	}

	public String stringDescr() {
		String cs = getClass().getName();
		return cs;
	}

	public MCTSParams getMCTSParams() {
		return player.getMCTSParams();
	}
	
    public int getNRolloutFinished() {
        return player.getNRolloutFinished();
    }

    public int getNIterations() {
        return player.getNUM_ITERS();
    }
}
