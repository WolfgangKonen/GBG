package controllers.MCTSExpectimax;

import controllers.MCTSExpectimax.MCTSExpectimax1.MCTSE1ChanceNode;
import games.StateObservation;
import games.StateObsNondeterministic;
import games.ZweiTausendAchtundVierzig.Heuristic.HeuristicSettings2048;
import params.MCTSExpectimaxParams;
import params.MCTSParams;
import params.ParMCTSE;
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
public class MCTSEPlayer
{
    private transient MCTSEChanceNode rootNode;
    private Random random;
    public transient List<Types.ACTIONS> actions = new ArrayList<>();

    private int ROLLOUT_DEPTH = MCTSExpectimaxConfig.DEFAULT_ROLLOUTDEPTH; //default values
    private int TREE_DEPTH = MCTSExpectimaxConfig.DEFAULT_TREEDEPTH;
    private int NUM_ITERS = MCTSExpectimaxConfig.DEFAULT_ITERATIONS;
    private double K = MCTSExpectimaxConfig.DEFAULT_K;
    private int MAX_NODES = MCTSExpectimaxConfig.DEFAULT_MAXNODES;
    private HeuristicSettings2048 heuristicSettings2048;

    public int nRolloutFinished = 0;		// counts the number of rollouts ending with isGameOver==true

	/**
	 * Member {@link #mctsExpectimaxParams} is only needed for saving and loading the agent
	 * (to restore the agent with all its parameter settings)
	 */
	private ParMCTSE mctsExpectimaxParams;

	/**
     * Creates the MCTSExpectimax player.
	 *
     * @param random 					random number generator object.
     * @param mctsExpectimaxParams		parameters for MCTSExpectimax, can be null
     */
    public MCTSEPlayer(Random random, ParMCTSE mctsExpectimaxParams)
    {
    	if (mctsExpectimaxParams!=null) {
            this.setK(mctsExpectimaxParams.getK_UCT());
            this.setNUM_ITERS(mctsExpectimaxParams.getNumIter());
            this.setROLLOUT_DEPTH(mctsExpectimaxParams.getRolloutDepth());
            this.setTREE_DEPTH(mctsExpectimaxParams.getTreeDepth());
            this.setMaxNodes(mctsExpectimaxParams.getMaxNodes());
			this.heuristicSettings2048 = mctsExpectimaxParams.getHeuristicSettings2048();
			this.mctsExpectimaxParams = mctsExpectimaxParams;
    	} else {
    		// /WK/ ?? unclear who sets K, NUM_ITERS and others in this case
			this.mctsExpectimaxParams = new ParMCTSE();
			//mctsExpectimaxParams.setFrom(mctsExpectimaxParams);
		}

        this.random = random;
    }

    /**
     * Initializes the tree with the new observation state in the root.
     * Called from {@link MCTSExpectimaxAgt#act(StateObservation, double[])}.
	 *
     * @param so current state of the game.
     */
    public void init(StateObservation so)
    {
        actions = so.getAvailableActions();

        //at first it seems confusing that the root node is a chance node, but because the first generation of children are tree nodes the root node has to be a chance node
		if(!mctsExpectimaxParams.getAlternativeVersion()) {
			rootNode = new MCTSEChanceNode(so, null, null, random, this);
		} else {
			if(so instanceof StateObsNondeterministic) {
				StateObsNondeterministic son = (StateObsNondeterministic) so;
				rootNode = new MCTSE1ChanceNode(son, null, null, random, this);
			} else {
				throw new RuntimeException("You need to implement the \"StateObservationNondeterministic\" interface to use the alternative version");
			}
		}
    }

    /**
     * Runs MCTSE to decide the action to take. It does not reset the tree.
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
    public int getMaxNodes() {
		return MAX_NODES;
	}
	public void setMaxNodes(int value) {
		MAX_NODES = value;
	}
    public int getNRolloutFinished() {
        return nRolloutFinished;
    }
	public HeuristicSettings2048 getHeuristicSettings2048() {
		return heuristicSettings2048;
	}
    public ParMCTSE getParMCTSE() {
		return mctsExpectimaxParams;
	}
	public MCTSEChanceNode getRootNode() {
    	return rootNode;
	}
}
