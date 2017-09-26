package controllers.MCTS;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;
import games.XArenaMenu;
import params.MCTSParams;
import params.OtherParams;
import params.ParMCTS;
import params.ParOther;
import tools.ElapsedCpuTimer;
import tools.ElapsedCpuTimer.TimerType;
import tools.Types;

import java.io.Serializable;
import java.util.List;
import java.util.Random;


/**
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 * and adapted from Diego Perez MCTS implementation http://gvgai.net/cont.php
 * 
 * This is the MCTS agent for 1- and 2-player games 
 * 
 * @author Wolfgang Konen, TH Köln, Nov'16
 */
public class MCTSAgentT extends AgentBase implements PlayAgent, Serializable 
{ 
    private transient ElapsedCpuTimer m_Timer;
    
    @Deprecated 
    // should use this.getParMCTS() instead 
	//private MCTSParams params;
    private ParMCTS pmcts; 
    
	private ParOther m_oPar = new ParOther();

	// if NEW_GNA==true: use the new functions getNextAction2... in getNextAction;
	// if NEW_GNA==false: use the old functions getNextAction1... in getNextAction;
	private static boolean NEW_GNA=false;	

    /**
     * The MCTS-UCT implementation
     */
    private SingleMCTSPlayer mctsPlayer;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 13L;

	/**
	 * Default constructor, needed for loading a serialized version
	 */
	public MCTSAgentT() {
    	this("MCTS", null, new ParMCTS());
	}

	/**
     * @param name	agent name, should be "MCTS" 
     * @param so 	state observation of the current game (may be null)
     * 'param elapsedTimer Timer for the controller creation.
     */
    public MCTSAgentT(String name,StateObservation so) //, ElapsedCpuTimer elapsedTimer)
    {
    	super(name);
    	initMCTSAgent(so, new ParMCTS(), new ParOther());
    }

    public MCTSAgentT(String name,StateObservation so, MCTSParams mcPar, OtherParams oPar) //, ElapsedCpuTimer elapsedTimer)
    {
    	super(name);
    	initMCTSAgent(so, new ParMCTS(mcPar), new ParOther(oPar));
    }
    public MCTSAgentT(String name,StateObservation so, ParMCTS parMCTS) //, ElapsedCpuTimer elapsedTimer)
    {
    	super(name);
    	initMCTSAgent(so, parMCTS, new ParOther());
    }
    
    private void initMCTSAgent(StateObservation so, ParMCTS parMCTS, ParOther oPar) {    	
        //Create the player.
        mctsPlayer = new SingleMCTSPlayer(new Random(),parMCTS);		
        //mctsPlayer = new SingleMCTSPlayer(new Random(1),mcPar);	// /WK/ reproducible debugging: seed 1
		m_oPar = oPar;

        //Set the available actions for stateObs.
        if (so!=null) mctsPlayer.initActions(so);
        
        m_Timer = new ElapsedCpuTimer(TimerType.CPU_TIME);
        m_Timer.setMaxTimeMillis(40);
        setAgentState(AgentState.TRAINED);
    }

    
    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 * @param vtable		the score for each available action (corresponding
	 * 						to sob.getAvailableActions())
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer
    						, double[] vtable) {

    	//Set the available actions for stateObs.
    	mctsPlayer.initActions(stateObs);				// /WK/ needed to get always the right 'actions'
    	
        //Set the state observation object as the new root of the tree.
        mctsPlayer.init(stateObs);

        //Determine the action using MCTS...
        int action = mctsPlayer.run(elapsedTimer, vtable);

        //... and return it.
        return mctsPlayer.actions[action];
    }

	/**
	 * Get the best next action and return it
	 * @param so			current game state (not changed on return)
	 * @param random		allow epsilon-greedy random action selection	
	 * @param vtable		the score for each available action (corresponding
	 * 						to sob.getAvailableActions())
	 * @param silent		verbosity control
	 * @return actBest		the best action 
	 */	
	@Override
	public Types.ACTIONS getNextAction(StateObservation so, boolean random, double[] vtable, boolean silent) {

		// this function selector is just intermediate, as long as we want to test getNextAction2 
		// against getNextAction1 (the former getNextAction). Once everything works fine with
		// getNextAction2, we should use only this function and make getNextAction deprecated 
		// (requires appropriate changes in all other agents implementing interface PlayAgent).
		if (!NEW_GNA) {
	        return getNextAction1(so, vtable, silent);
		} else {
			Types.ACTIONS_VT actBestVT = getNextAction2(so, random, silent);
			double[] VTable = actBestVT.getVTable();
			for (int i=0; i<VTable.length; i++) vtable[i] = VTable[i];
			vtable[VTable.length] = actBestVT.getVBest();
			return actBestVT;			
		}
    }

    /**
     * Get the best next action and return it (old version, NEW_GNA==false).
     * Called by calcCertainty and getNextAction.
     * 
     * @param sob			current game state (not changed on return)
     * @param vtable		must be an array of size n+1 on input, where
     * 						n=sob.getNumAvailableActions(). On output,
     * 						elements 0,...,n-1 hold the score for each available
     * 						action (corresponding to sob.getAvailableActions())
     * 						In addition, vtable[n] has the score for the
     * 						best action.
     * @return nextAction	the next action
     */
    private Types.ACTIONS getNextAction1(StateObservation so, double[] vtable, boolean silent) {
		
		//vtable = new double[so.getNumAvailableActions()];
        // DON'T! The caller has to define vtable with the right length
		
		assert so.isLegalState() 
			: "Not a legal state"; // e.g. player to move does not fit to Table
		m_Timer.reset();
		
		// Ask MCTS for the best action ...
		Types.ACTIONS actBest = act(so,m_Timer,vtable);
		
		actBest.setRandomSelect(false);		// the action was not a random move
		
    	return actBest;         
	}

    /**
     * Get the best next action and return it (new version, NEW_GNA==true).
     * Called by calcCertainty and getNextAction.
     * 
     * @param sob			current game state (not changed on return)
     * @return actBest		the next action
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable and vBest to store the value for each available
	 * action (as returned by so.getAvailableActions()) and the value for the best action actBest.
     */
    public Types.ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
		
        Types.ACTIONS actBest = null;
        Types.ACTIONS_VT actBestVT = null;
        List<Types.ACTIONS> actions = so.getAvailableActions();
		double[] VTable, vtable;
        vtable = new double[actions.size()];  
        VTable = new double[actions.size()+1];  
		
		assert so.isLegalState() 
			: "Not a legal state"; // e.g. player to move does not fit to Table
		m_Timer.reset();
		
		// Ask MCTS for the best action ...
		actBest = act(so,m_Timer,VTable);
		
		double bestScore = VTable[actions.size()];
		for (int i=0; i<vtable.length; i++) vtable[i]=VTable[i];
		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), false, vtable, bestScore);
        return actBestVT;
	}


	/**
	 * 
	 * @return	returns true/false, whether the action suggested by last call 
	 * 			to getNextAction() was a random action. 
	 * 			Always false in the case of MCTS based on SingleTreeNode.uct().
	 * <p>
	 * Use now {@link Types.ACTIONS#isRandomAction()}
	 */
	@Deprecated
	public boolean wasRandomAction() {
		return false;
	}


	@Override
	public double getScore(StateObservation so) {
		double[] vtable = new double[so.getNumAvailableActions()+1];
        double nextActionScore = Double.NEGATIVE_INFINITY;
		
		assert so.isLegalState() 
		: "Not a legal state"; // e.g. player to move does not fit to Table
	
		// This if branch is vital: It was missing before, and if 'so' was a game-over state
		// this resulted in a NullpointerException later, because no child was added to root.
		// Now we fix this by returning so.getGameScore(so):
        if (so.isGameOver()) {
        	return so.getGameScore(so);
        } else {
        	
    		// Ask MCTS for the best action ...
    		Types.ACTIONS actBest = act(so,m_Timer,vtable);

            for (int i = 0; i < so.getNumAvailableActions(); i++) {
                if (nextActionScore <= vtable[i]) {
                    nextActionScore = vtable[i];
                }
            }

            return nextActionScore;
        }
	}

	@Override
	public String stringDescr() {
		String cs = getClass().getName();
		String str = cs + ": iterations:" + getParMCTS().getNumIter() 
				+ ", rollout depth:" + getParMCTS().getRolloutDepth()
				+ ", K_UCT:"+ getParMCTS().getK_UCT()
				+ ", tree depth:" + getParMCTS().getTreeDepth();
		return str;
	}

	public ParMCTS getParMCTS() {
		return mctsPlayer.getParMCTS();
	}
	
    public int getNRolloutFinished() {
        return mctsPlayer.getNRolloutFinished();
    }

    public int getNIterations() {
        return mctsPlayer.getNUM_ITERS();
    }
	public double getK() {
		return mctsPlayer.getK();
	}

	/**
	 * Set defaults for m_oPar 
	 * (needed in {@link XArenaMenu.loadAgent} when loading older agents, where 
	 * m_oPar=null in the saved version).
	 */
	public void setDefaultOtherPar() {
		m_oPar = new ParOther();
	}
	public ParOther getOtherPar() {
		return m_oPar;
	}

}

