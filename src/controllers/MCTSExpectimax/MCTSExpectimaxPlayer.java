package controllers.MCTSExpectimax;

import games.StateObservation;
import params.MCTSParams;
import tools.Types;

import java.io.Serializable;
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
public class MCTSExpectimaxPlayer implements Serializable
{
    private transient MCTSExpectimaxChanceNode m_root;
    private Random m_rnd;
    public transient List<Types.ACTIONS> actions = new ArrayList<>();

    private int ROLLOUT_DEPTH = 200;
    private int TREE_DEPTH = 10;
    private int NUM_ITERS = 1000;
    private double K = Math.sqrt(2);
    private int verbose = 0;

    int nRolloutFinished = 0;		// counts the number of rollouts ending with isGameOver==true

	/**
	 * Member {@link #m_mcPar} is only needed for saving and loading the agent
	 * (to restore the agent with all its parameter settings)
	 */
	private MCTSParams m_mcPar;
	private static final long serialVersionUID = 123L;

	/**
     * Creates the MCTSExpectimax player.
     * @param a_rnd 	random number generator object.
     * @param mcPar		parameters for MCTS
     */
    public MCTSExpectimaxPlayer(Random a_rnd, MCTSParams mcPar)
    {
    	if (mcPar!=null) {
            this.setK(mcPar.getK_UCT());
            this.setNUM_ITERS(mcPar.getNumIter());
            this.setROLLOUT_DEPTH(mcPar.getRolloutDepth());
            this.setTREE_DEPTH(mcPar.getTreeDepth());
            this.verbose = mcPar.getVerbosity();
    	}

		m_mcPar = new MCTSParams();
		m_mcPar.setFrom(mcPar);

        m_rnd = a_rnd;
    }

    /**
     * Initializes the tree with the new observation state in the root.
     * Called from {@link MCTSExpectimaxAgent#act(StateObservation, double[])}.
     * @param so current state of the game.
     */
    public void init(StateObservation so)
    {
        //Get the actions into an array.
        actions = so.getAvailableActions();

    	m_root = new MCTSExpectimaxChanceNode(so,null,null,m_rnd,this);
    }

    /**
     * Runs MCTS to decide the action to take. It does not reset the tree.
	 * @param vtable		the score for each available action (corresponding
	 * 						to sob.getAvailableActions())
     * @return the action to execute in the game.
     */
    public Types.ACTIONS run(double[] vtable)
    {
    	this.nRolloutFinished=0;
    	
        //Do the search
        m_root.mctsSearch(vtable);

        //Determine the best action to take and return it
        //(Choose one of the following two lines)
        //int action = m_root.mostVisitedAction();
		Types.ACTIONS action = m_root.bestAction();
        
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
		return m_mcPar;
	}
}
