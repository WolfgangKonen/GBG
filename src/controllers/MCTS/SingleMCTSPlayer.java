package controllers.MCTS;

import games.StateObservation;
import params.MCTSParams;
import params.TDParams;
import tools.ElapsedCpuTimer;
import tools.Types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * This is adapted from Diego Perez MCTS reference implementation
 * 		http://gvgai.net/cont.php
 * (with a bug fix concerning the number of available actions and an 
 *  extensions for 1- and 2-player games)
 * @author Wolfgang Konen, TH Köln, Nov'16
 */
public class SingleMCTSPlayer implements Serializable
{
    /**
     * Root of the tree.
     */
    public transient SingleTreeNode m_root;

    /**
     * Random generator.
     */
    public Random m_rnd;

    public transient Types.ACTIONS[] actions; 			

    public static int DEFAULT_ROLLOUT_DEPTH =200;
    public static int DEFAULT_TREE_DEPTH = 10;
    public static int DEFAULT_NUM_ITERS = 1000;
    public static double DEFAULT_K = Math.sqrt(2);
    public static int DEFAULT_VERBOSITY = 0;
    public int NUM_ACTIONS;
	public int ROLLOUT_DEPTH = DEFAULT_ROLLOUT_DEPTH;
	public int TREE_DEPTH = DEFAULT_TREE_DEPTH;
    public int NUM_ITERS = DEFAULT_NUM_ITERS;
	public double K = DEFAULT_K;
	public int verbose = DEFAULT_VERBOSITY; 
	
	/**
	 * Member {@link #m_mcPar} is only needed for saving and loading the agent
	 * (to restore the agent with all its parameter settings)
	 */
	private MCTSParams m_mcPar;
	private static final long serialVersionUID = 123L;

	/**
	 * Default constructor for SingleMCTSPlayer, needed for loading a serialized version
	 */
	public SingleMCTSPlayer() {
		m_mcPar = new MCTSParams();
        m_rnd = new Random();
        m_root = new SingleTreeNode(m_rnd,this);
	}

	/**
     * Creates the MCTS player with a sampleRandom generator object.
     * @param a_rnd 	sampleRandom generator object.
     * @param mcPar		parameters for MCTS
     */
    public SingleMCTSPlayer(Random a_rnd, MCTSParams mcPar)
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
        m_root = new SingleTreeNode(a_rnd,this);
    }

    public void initActions(StateObservation so) {
        //Get the actions into an array.
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        this.actions = new Types.ACTIONS[acts.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = acts.get(i);
        }
        this.setNUM_ACTIONS(actions.length);    	
    }

    /**
     * Inits the tree with the new observation state in the root.
     * @param so current state of the game.
     */
    public void init(StateObservation so)
    {
    	//--- only for debug ---
    	//System.out.println(a_gameState.toString());

    	//Set the game observation to a newly root node.
        //m_root = new SingleTreeNode(m_rnd,this);
        //m_root.state = so;
    	m_root = new SingleTreeNode(so,null,null,m_rnd,this);// /WK/ bug fix: needed if a_gameState  
    														 // allows fewer actions than MCTSAgentT.NUM_ACTIONS 

    }

    /**
     * Runs MCTS to decide the action to take. It does not reset the tree.
     * @param elapsedTimer Timer when the action returned is due.
	 * @param vtable		the score for each available action (corresponding
	 * 						to sob.getAvailableActions())
     * @return the action to execute in the game.
     */
    public int run(ElapsedCpuTimer elapsedTimer, double[] vtable)
    {
        //Do the search within the available time.
        m_root.mctsSearch(elapsedTimer, vtable);

        //Determine the best action to take and return it.
        //(Choose one of the following two lines)
        //int action = m_root.mostVisitedAction();
        int action = m_root.bestAction();
        
        return action;
    }

    public int getNUM_ACTIONS() {
		return NUM_ACTIONS;
	}
	public void setNUM_ACTIONS(int nUM_ACTIONS) {
		NUM_ACTIONS = nUM_ACTIONS;
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
	public MCTSParams getMCTSParams() {
		return m_mcPar;
	}
}
