package controllers.MCTSExpectimax;

import games.StateObservation;
import games.StateObsNondeterministic;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import games.ZweiTausendAchtundVierzig.Heuristic.HeuristicSettings2048;
//import params.MCTSExpectimaxParams;
//import params.MCTSParams;
import params.ParMCTSE;
import params.ParOther;
import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import controllers.MCTS.SingleMCTSPlayer;

/**
 * This is adapted from {@link SingleMCTSPlayer} for the <b>non-deterministic</b> case.
 *
 * @author Johannes Kutsch
 */
public class MCTSEPlayer
{
    private transient MCTSEChanceNode rootNode;
    public transient List<Types.ACTIONS> actions = new ArrayList<>();
	boolean rgs;		// rewardIsGameScore, package-wide visible
    private Random random;

    // --- not needed anymore, we use the getters of mctsExpectimaxParams: --- 
//    private int ROLLOUT_DEPTH = MCTSExpectimaxConfig.DEFAULT_ROLLOUTDEPTH; //default values
//    private int TREE_DEPTH = MCTSExpectimaxConfig.DEFAULT_TREEDEPTH;
//    private int NUM_ITERS = MCTSExpectimaxConfig.DEFAULT_ITERATIONS;
//    private double K = MCTSExpectimaxConfig.DEFAULT_K;
//    private int MAX_NODES = MCTSExpectimaxConfig.DEFAULT_MAXNODES;
//    private HeuristicSettings2048 heuristicSettings2048;
//    private int verbosity = 0; 

    public int nRolloutFinished = 0;		// counts the number of rollouts ending with isGameOver==true

	/**
	 * Member {@link #mctsExpectimaxParams} is only needed for saving and loading the agent
	 * (to restore the agent with all its parameter settings)
	 */
	private ParMCTSE mctsExpectimaxParams;
    private transient MCTSExpectimaxAgt m_parent;	// to get access to m_oPar

	/**
     * Creates the MCTSExpectimax player.
	 *
     * @param random 					random number generator object.
     * @param mctsExpectimaxParams		parameters for MCTSExpectimax, can be null
     */
    public MCTSEPlayer(MCTSExpectimaxAgt parent, Random random, ParMCTSE mctsExpectimaxParams)
    {
    	this.m_parent = parent;
    	rgs = m_parent.getParOther().getRewardIsGameScore();
    	if (mctsExpectimaxParams!=null) {
//            this.setK(mctsExpectimaxParams.getK_UCT());
//            this.setNUM_ITERS(mctsExpectimaxParams.getNumIter());
//            this.setROLLOUT_DEPTH(mctsExpectimaxParams.getRolloutDepth());
//            this.setTREE_DEPTH(mctsExpectimaxParams.getTreeDepth());
//            this.setMaxNodes(mctsExpectimaxParams.getMaxNodes());
//			  this.heuristicSettings2048 = mctsExpectimaxParams.getHeuristicSettings2048();
			this.mctsExpectimaxParams = mctsExpectimaxParams;
    	} else {
			this.mctsExpectimaxParams = new ParMCTSE();
		}

        this.random = random;
    }

    /**
     * Initializes the tree with the new {@link StateObservation} {@code so} in the root.
	 *
     * @param so current state of the game.
     * 
     * @see MCTSExpectimaxAgt#act(StateObservation, double[])
     */
    public void init(StateObservation so)
    {
        actions = so.getAvailableActions();

        //at first it seems confusing that the root node is a chance node, but because the first 
        //generation of children are tree nodes, the root node has to be a chance node.
		if(!mctsExpectimaxParams.getAlternateVersion()) {
			// /WK/ this is the recommended version:
			rootNode = new MCTSEChanceNode(so, null, null, random, this);
		} else {
			// /WK/ currently NOT recommended, see comment childrenNodes in AltTreeNode (!):
			if(so instanceof StateObsNondeterministic) {
				System.out.println("MCTSE: Using alternate version ...");
				StateObsNondeterministic son = (StateObsNondeterministic) so;
				rootNode = new AltChanceNode(son, null, null, random, this);
			} else {
				throw new RuntimeException("You need to implement the \"StateObservationNondeterministic\" interface to use MCTS-Expectimax");
			}
		}
		
    	if (this.getNormalize() && so instanceof StateObserver2048) {
    		// make a quick mctseSearch to establish a state-dependent estimate of maxRolloutScore
    		rootNode.maxRolloutScore=1e-5;
    		int numIters=this.getNUM_ITERS();
    		this.mctsExpectimaxParams.setNumIter(100);
    		double[] VTable = new double[actions.size()+1];
    		this.getRootNode().mctseSearch(VTable);
    		double maxRolloutScore = rootNode.maxRolloutScore;
    		
    		// generate a 'fresh' rootNode and set its maxRolloutScore:
			rootNode = new MCTSEChanceNode(so, null, null, random, this);
			rootNode.maxRolloutScore = maxRolloutScore;
    		this.mctsExpectimaxParams.setNumIter(numIters);
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
        rootNode.mctseSearch(vtable);

        //Determine the best action to take and return it
		Types.ACTIONS action = rootNode.bestAction();
        
        return action;
    }

	public int getROLLOUT_DEPTH() {
		return this.mctsExpectimaxParams.getRolloutDepth();
	}
	public int getTREE_DEPTH() {
		return this.mctsExpectimaxParams.getTreeDepth();
	}
    public int getNUM_ITERS() {
		return this.mctsExpectimaxParams.getNumIter();
	}
	public double getK() {
		return this.mctsExpectimaxParams.getK_UCT();
	}
    public int getMaxNodes() {
		return this.mctsExpectimaxParams.getMaxNodes();
	}
	public int getVerbosity() {
		return this.mctsExpectimaxParams.getVerbosity();
	}
	public boolean getNormalize() {
		return this.mctsExpectimaxParams.getNormalize();
	}
	public HeuristicSettings2048 getHeuristicSettings2048() {
		return this.mctsExpectimaxParams.getHeuristicSettings2048();
	}
    public ParMCTSE getParMCTSE() {
		return mctsExpectimaxParams;
	}
    public ParOther getParOther() {
		return m_parent.getParOther();
	}
    public int getNRolloutFinished() {
        return nRolloutFinished;
    }
	public MCTSEChanceNode getRootNode() {
    	return rootNode;
	}
//	public void setROLLOUT_DEPTH(int rOLLOUT_DEPTH) {
//		ROLLOUT_DEPTH = rOLLOUT_DEPTH;
//	}
//	public void setTREE_DEPTH(int tREE_DEPTH) {
//		TREE_DEPTH = tREE_DEPTH;
//	}
//	public void setNUM_ITERS(int nUM_ITERS) {
//		NUM_ITERS = nUM_ITERS;
//	}
//	public void setK(double k) {
//		K = k;
//	}
//	public void setMaxNodes(int value) {
//		MAX_NODES = value;
//	}
}
