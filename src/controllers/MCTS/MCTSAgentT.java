package controllers.MCTS;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;
import params.MCTSParams;
import tools.ElapsedCpuTimer;
import tools.ElapsedCpuTimer.TimerType;
import tools.Types;

import java.io.Serializable;
import java.util.Random;


/**
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 * and adapted from Diego Perez MCTS implementation http://gvgai.net/cont.php
 * 
 * This is the MCTS agent for 1- and 2-player games 
 * 
 * @author Wolfgang Konen, TH K�ln, Nov'16
 */
public class MCTSAgentT extends AgentBase implements PlayAgent, Serializable 
{ 
	private static final long serialVersionUID = 123L;
    private transient ElapsedCpuTimer m_Timer;
	public MCTSParams params;

    /**
     * The MCTS-UCT implementation
     */
    private SingleMCTSPlayer mctsPlayer;

	/**
	 * Default constructor, needed for loading a serialized version
	 */
	public MCTSAgentT() {
    	this("MCTS", null, null);
	}

	/**
     * @param name	agent name, should be "MCTS" 
     * @param so 	state observation of the current game (may be null)
     * 'param elapsedTimer Timer for the controller creation.
     */
    public MCTSAgentT(String name,StateObservation so) //, ElapsedCpuTimer elapsedTimer)
    {
    	this(name, so, null);
    }

    public MCTSAgentT(String name,StateObservation so, MCTSParams mcPar) //, ElapsedCpuTimer elapsedTimer)
    {
    	super(name);
		params = mcPar;
        
        //Create the player.
        mctsPlayer = new SingleMCTSPlayer(new Random(),mcPar);		
        //mctsPlayer = new SingleMCTSPlayer(new Random(1),mcPar);	// /WK/ reproducible debugging: seed 1

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
		
		//VTable = new double[so.getNumAvailableActions()];
        // DON'T! The caller has to define VTable with the right length
		
		assert so.isLegalState() 
			: "Not a legal state"; // e.g. player to move does not fit to Table
		m_Timer.reset();
		
		// Ask MCTS for the best action ...
		Types.ACTIONS actBest = act(so,m_Timer,vtable);
		
    	return actBest;         // the action was not a random move
	}


	/**
	 * 
	 * @return	returns true/false, whether the action suggested by last call 
	 * 			to getNextAction() was a random action. 
	 * 			Always false in the case of MCTS based on SingleTreeNode.uct().
	 */
	public boolean wasRandomAction() {
		return false;
	}


	@Override
	public double getScore(StateObservation so) {
		double[] vtable = new double[so.getNumAvailableActions()+1];
		
		assert so.isLegalState() 
		: "Not a legal state"; // e.g. player to move does not fit to Table
	
		// Ask MCTS for the best action ...
		Types.ACTIONS actBest = act(so,m_Timer,vtable);
		
		return vtable[vtable.length];
	}

	@Override
	public String stringDescr() {
		String cs = getClass().getName();
		return cs;
	}

	public MCTSParams getMCTSParams() {
		return mctsPlayer.getMCTSParams();
	}
	
    public int getNRolloutFinished() {
        return mctsPlayer.getNRolloutFinished();
    }

    public int getNIterations() {
        return mctsPlayer.getNUM_ITERS();
    }


}
