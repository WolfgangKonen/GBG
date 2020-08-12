package controllers.MCTS;

import games.StateObservation;
import params.ParMCTS;
import params.ParOther;
import tools.ElapsedCpuTimer;
import tools.Types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * This is adapted from Diego Perez MCTS reference implementation<br>
 * 		<a href="http://gvgai.net/cont.php">http://gvgai.net/cont.php</a><br>
 * (with a bug fix concerning the number of available actions and  
 *  extensions for 1- and 2-player games)
 *  
 * @author Wolfgang Konen, TH Koeln, 2016-2020
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

    private int NUM_ACTIONS;
// --- this is now in ParMCTS: ---
//    private int ROLLOUT_DEPTH = DEFAULT_ROLLOUT_DEPTH;
//    private int TREE_DEPTH = DEFAULT_TREE_DEPTH;
//    private int NUM_ITERS = DEFAULT_NUM_ITERS;
//    private double K = DEFAULT_K;
//    private int verbose = DEFAULT_VERBOSITY; 
    int nRolloutFinished = 0;		// counts the number of rollouts ending with isGameOver==true
	
	/**
	 * Member {@link #m_parMCTS} is only needed for saving and loading the agent
	 * (to restore the agent with all its parameter settings)
	 */
	private ParMCTS m_parMCTS;
	/**
	 * Member {@link #m_parent} is only needed for access to {@link MCTSAgenT#getParOther()}
	 */
    // a bug before 2020-08-11 was that m_parent was transient. This is not o.k. since when loading an MCTS agent 
    // from disk (e.g. in tournament), it would result in m_parent being null, which leads to a runtime exception
    // when getParOther is called. Now fixed, we removed 'transient'.
    private MCTSAgentT m_parent;		

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 13L;


	/**
	 * Default constructor for SingleMCTSPlayer, needed for loading a serialized version
	 */
	public SingleMCTSPlayer() {
		m_parent = null;
		m_parMCTS = new ParMCTS();
		//m_mcPar = new MCTSParams();
        m_rnd = new Random();
        m_root = new SingleTreeNode(m_rnd,this);
	}

	/**
     * Creates the MCTS player. 
     * @param a_rnd 	random number generator object.
     * @param parMCTS		parameters for MCTS
     */
    public SingleMCTSPlayer(MCTSAgentT parent, Random a_rnd, ParMCTS parMCTS)
    {
    	m_parent = parent;
		m_parMCTS = parMCTS;
        m_rnd = a_rnd;
        m_root = new SingleTreeNode(a_rnd,this);
    }
   	

    /**
     * Set the available actions for state {@code so}.
     * Called from {@link MCTSAgentT#act(StateObservation, ElapsedCpuTimer, double[])}.
     * @param so
     */
    public void initActions(StateObservation so) {
        //Get the actions into an array.
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        this.actions = new Types.ACTIONS[acts.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = acts.get(i);
        }
        NUM_ACTIONS = actions.length;    	
    }

    /**
     * Initializes the tree with the new observation state in the root.
     * Called from {@link MCTSAgentT#act(StateObservation, ElapsedCpuTimer, double[])}.
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
    	this.nRolloutFinished=0;
    	
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

	public int getROLLOUT_DEPTH() {
		return m_parMCTS.getRolloutDepth();
	}

	public int getTREE_DEPTH() {
		return m_parMCTS.getTreeDepth();
	}

    public int getNUM_ITERS() {
		return m_parMCTS.getNumIter();
	}

	public double getK() {
		return m_parMCTS.getK_UCT();
	}

    public int getVerbosity() {
		return m_parMCTS.getVerbosity();
	}
    
    public boolean getNormalize() {
		return m_parMCTS.getNormalize();
	}
    
    public int getNRolloutFinished() {
        return nRolloutFinished;
    }

    public ParMCTS getParMCTS() {
		return m_parMCTS;
	}

    public ParOther getParOther() {
		assert m_parent != null : "[SingleMCTSPlayer] m_parent is null!";
		return m_parent.getParOther();
	}
}
