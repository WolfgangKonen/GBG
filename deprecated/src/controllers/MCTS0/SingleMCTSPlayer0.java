package controllers.MCTS0;

import games.StateObservation;
//import params.MCTSParams;
import params.ParMCTS;
import tools.ElapsedCpuTimer;
import tools.Types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * --- THIS IS THE SNAPSHOT FROM 2019-01-22 ----------------------------------
 * 
 * This is adapted from Diego Perez MCTS reference implementation<br>
 * 		<a href="http://gvgai.net/cont.php">http://gvgai.net/cont.php</a><br>
 * (with a bug fix concerning the number of available actions and  
 *  extensions for 1- and 2-player games)
 *  
 * @author Wolfgang Konen, TH Koeln, Nov'16
 */
public class SingleMCTSPlayer0 implements Serializable
{
    /**
     * Root of the tree.
     */
    public transient SingleTreeNode0 m_root;

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
	//private MCTSParams m_mcPar;
	private ParMCTS m_parMCTS;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 13L;


	/**
	 * Default constructor for SingleMCTSPlayer, needed for loading a serialized version
	 */
	public SingleMCTSPlayer0() {
		m_parMCTS = new ParMCTS();
		//m_mcPar = new MCTSParams();
        m_rnd = new Random();
        m_root = new SingleTreeNode0(m_rnd,this);
	}

	/**
     * Creates the MCTS player. 
     * @param a_rnd 	random number generator object.
     * @param parMCTS		parameters for MCTS
     */
    public SingleMCTSPlayer0(Random a_rnd, ParMCTS parMCTS)
    {
		m_parMCTS = parMCTS;
        m_rnd = a_rnd;
        m_root = new SingleTreeNode0(a_rnd,this);
    }
   	
//	/**
//     * Creates the MCTS player. 
//     * @param a_rnd 	random number generator object.
//     * @param mcPar		parameters for MCTS
//     */
//    @Deprecated
//    public SingleMCTSPlayer(Random a_rnd, MCTSParams mcPar)
//    {
    	// --- OLD, can be deleted, when the new version is tested and works:  ---
    	//
    	// Why do we have m_mcpar and the several single parameters?
    	// We need both, m_mcPar for saving to disk and re-loading (use setFrom() to set
    	// the values in the params tab). And the single parameters for computational
    	// efficient access from the nodes of the tree.
    	//
    	// The setters are responsible for updating the parameters in both locations (!)    	
//		m_mcPar = new MCTSParams();
//		m_mcPar.setFrom(mcPar);
//    	m_mcPar = new MCTSParams();
//    	if (mcPar!=null) {
//            this.setK(mcPar.getK_UCT());
//            this.setNUM_ITERS(mcPar.getNumIter());
//            this.setROLLOUT_DEPTH(mcPar.getRolloutDepth());
//            this.setTREE_DEPTH(mcPar.getTreeDepth());
//            this.setVerbosity(mcPar.getVerbosity());
//    	}
    	
//		m_parMCTS = new ParMCTS(mcPar);
//        m_rnd = a_rnd;
//        m_root = new SingleTreeNode(a_rnd,this);
//    }

    /**
     * Set the available actions for state {@code so}.
     * Called from {@link MCTSAgentT0#act(StateObservation, ElapsedCpuTimer, double[])}.
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
     * Called from {@link MCTSAgentT0#act(StateObservation, ElapsedCpuTimer, double[])}.
     * @param so current state of the game.
     */
    public void init(StateObservation so)
    {
    	//--- only for debug ---
    	//System.out.println(a_gameState.toString());

    	//Set the game observation to a newly root node.
        //m_root = new SingleTreeNode(m_rnd,this);
        //m_root.state = so;
    	m_root = new SingleTreeNode0(so,null,null,m_rnd,this);// /WK/ bug fix: needed if a_gameState  
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
//	public void setNUM_ACTIONS(int nUM_ACTIONS) {
//		NUM_ACTIONS = nUM_ACTIONS;
//	}
	public int getROLLOUT_DEPTH() {
		return m_parMCTS.getRolloutDepth();
	}
//	public void setROLLOUT_DEPTH(int rOLLOUT_DEPTH) {
//		ROLLOUT_DEPTH = rOLLOUT_DEPTH;
//		m_mcPar.setRolloutDepth(rOLLOUT_DEPTH);
//	}
	public int getTREE_DEPTH() {
		return m_parMCTS.getTreeDepth();
	}
//	public void setTREE_DEPTH(int tREE_DEPTH) {
//		TREE_DEPTH = tREE_DEPTH;
//		m_mcPar.setTreeDepth(tREE_DEPTH);
//	}
    public int getNUM_ITERS() {
		return m_parMCTS.getNumIter();
	}
//	public void setNUM_ITERS(int nUM_ITERS) {
//		NUM_ITERS = nUM_ITERS;
//		m_mcPar.setNumIter(nUM_ITERS);
//	}
	public double getK() {
		return m_parMCTS.getK_UCT();
	}
//	public void setK(double k) {
//		K = k;
//		m_mcPar.setK_UCT(k);
//	}
    public int getVerbosity() {
		return m_parMCTS.getVerbosity();
	}
//	public void setVerbosity(int verbosity) {
//		verbose = verbosity;
//		m_mcPar.setVerbosity(verbosity);;
//	}
    
    public boolean getNormalize() {
		return m_parMCTS.getNormalize();
	}
    
    public int getNRolloutFinished() {
        return nRolloutFinished;
    }

    public ParMCTS getParMCTS() {
		return m_parMCTS;
	}
}
