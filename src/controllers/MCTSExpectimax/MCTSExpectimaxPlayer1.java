package controllers.MCTSExpectimax;

import games.StateObservation;
import games.StateObservationNondeterministic;
import params.MCTSParams;
import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This is adapted from Diego Perez MCTS reference implementation
 * 		http://gvgai.net/cont.php
 * (with a bug fix concerning the number of available actions and an 
 *  extension for 1- and 2-player games, adjusted for nondeterministic
 *  games and the return of VTable information.)
 *
 * @author Johannes Kutsch
 */
public class MCTSExpectimaxPlayer1
{
    private transient MCTSExpectimaxChanceNode1 rootNode;
    private Random random;
    public transient List<Types.ACTIONS> actions = new ArrayList<>();

    private int ROLLOUT_DEPTH = 200; //default values
    private int TREE_DEPTH = 10;
    private int NUM_ITERS = 1000;
    private double K = Math.sqrt(2);
    private int verbose = 0;

    int nRolloutFinished = 0;		// counts the number of rollouts ending with isGameOver==true

	/**
	 * Member {@link #mctsParams} is only needed for saving and loading the agent
	 * (to restore the agent with all its parameter settings)
	 */
	private MCTSParams mctsParams;

	/**
     * Creates the MCTSExpectimax player.
	 *
     * @param random 	random number generator object.
     * @param mcPar		parameters for MCTS, can be null
     */
    public MCTSExpectimaxPlayer1(Random random, MCTSParams mcPar)
    {
    	if (mcPar!=null) {
            this.setK(mcPar.getK_UCT());
            this.setNUM_ITERS(mcPar.getNumIter());
            this.setROLLOUT_DEPTH(mcPar.getRolloutDepth());
            this.setTREE_DEPTH(mcPar.getTreeDepth());
            this.verbose = mcPar.getVerbosity();
    	} else {
			mctsParams = new MCTSParams();
			mctsParams.setFrom(mcPar);
		}

        this.random = random;
    }

    /**
     * Initializes the tree with the new observation state in the root.
     * Called from {@link MCTSExpectimaxAgent#act(StateObservation, double[])}.
	 *
     * @param so current state of the game.
     */
    public void init(StateObservation so)
    {
    	if(so instanceof StateObservationNondeterministic) {
			actions = so.getAvailableActions();

			//at first it seems confusing that the root node is a Chance Node, but because the first generation of children are are nodes the root node has to be a Chance Node
			rootNode = new MCTSExpectimaxChanceNode1((StateObservationNondeterministic)so, null, null, random, this);
		} else {
    		throw new RuntimeException("Please implement the StateObservationNondeterministic interface before using MCTS Expectimax");
		}
    }

    /**
     * Runs MCTS to decide the action to take. It does not reset the tree.
	 *
	 * @param vtable		the score for each available action (corresponding
	 * 						to sob.getAvailableActions())
	 *
     * @return the action to execute in the game.
     */
    public Types.ACTIONS run(double[] vtable)
    {
    	this.nRolloutFinished=0;
    	
        //Do the search
        rootNode.mctsSearch(vtable);

        //Determine the best action to take and return it
        //(Choose one of the following two lines)
        //int action = rootNode.mostVisitedAction();
		Types.ACTIONS action = rootNode.bestAction();
        
        return action;
    }

	public int getROLLOUT_DEPTH() {
		return ROLLOUT_DEPTH;
	}
	public void setROLLOUT_DEPTH(int rOLLOUT_DEPTH) {
		ROLLOUT_DEPTH = rOLLOUT_DEPTH;
	}
	public int getTREE_DEPTH() {
		return TREE_DEPTH;
	}
	public void setTREE_DEPTH(int tREE_DEPTH) {
		TREE_DEPTH = tREE_DEPTH;
	}
    public int getNUM_ITERS() {
		return NUM_ITERS;
	}
	public void setNUM_ITERS(int nUM_ITERS) {
		NUM_ITERS = nUM_ITERS;
	}
	public double getK() {
		return K;
	}
	public void setK(double k) {
		K = k;
	}
    public int getVerbosity() {
		return verbose;
	}
	public void setVerbosity(int verbosity) {
		verbose = verbosity;
	}
    public int getNRolloutFinished() {
        return nRolloutFinished;
    }
    public MCTSParams getMCTSParams() {
		return mctsParams;
	}
}
