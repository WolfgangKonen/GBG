package controllers.MCTS;

import controllers.AgentBase;
import controllers.ExpectimaxNWrapper;
import controllers.MaxN2Wrapper;
import controllers.PlayAgent;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import games.Arena;
import games.StateObservation;
import params.ParMCTS;
import params.ParOther;
import tools.ElapsedCpuTimer;
import tools.ElapsedCpuTimer.TimerType;
import tools.ScoreTuple;
import tools.Types;

import java.io.Serializable;
import java.util.List;
import java.util.Random;


/**
 * MCTSAgentT is the MCTS agent for <b>deterministic</b> 1- and 2-player games. 
 * <p>
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 * and adapted from Diego Perez MCTS implementation <br>
 * 		<a href="http://gvgai.net/cont.php">http://gvgai.net/cont.php</a><br>
 * (with a bug fix concerning the number of available actions and  
 *  extensions for 1- and 2-player games)
 * 
 * @author Wolfgang Konen, TH Koeln, 2016
 * 
 * @see MCTSExpectimaxAgt MCTSExpectimaxAgt for non-deterministic games
 */
public class MCTSAgentT extends AgentBase implements PlayAgent, Serializable 
{ 
    private transient ElapsedCpuTimer m_Timer;
    
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

//    @Deprecated
//    public MCTSAgentT(String name,StateObservation so, MCTSParams mcPar, OtherParams oPar) //, ElapsedCpuTimer elapsedTimer)
//    {
//    	super(name);
//    	initMCTSAgent(so, new ParMCTS(mcPar), new ParOther(oPar));
//    }
    public MCTSAgentT(String name,StateObservation so, ParMCTS parMCTS) //, ElapsedCpuTimer elapsedTimer)
    {
    	super(name);
    	initMCTSAgent(so, parMCTS, new ParOther());
    }
    
    public MCTSAgentT(String name,StateObservation so, ParMCTS parMCTS, ParOther oPar) //, ElapsedCpuTimer elapsedTimer)
    {
    	super(name);
    	initMCTSAgent(so, parMCTS, oPar);
    }
    
    private void initMCTSAgent(StateObservation so, ParMCTS parMCTS, ParOther oPar) {    	
        //Create the player.
        mctsPlayer = new SingleMCTSPlayer(this,new Random(),parMCTS);		
        //mctsPlayer = new SingleMCTSPlayer(this,new Random(1),mcPar);	// /WK/ reproducible debugging: seed 1
		m_oPar = oPar;		// AgentBase::m_oPar

        //Set the available actions for stateObs.
        if (so!=null) mctsPlayer.initActions(so);
        
        m_Timer = new ElapsedCpuTimer(TimerType.CPU_TIME);
        m_Timer.setMaxTimeMillis(40);
        setAgentState(AgentState.TRAINED);
    }

	public boolean instantiateAfterLoading() {
        m_Timer = new ElapsedCpuTimer(TimerType.CPU_TIME);
        m_Timer.setMaxTimeMillis(40);
        mctsPlayer.instantiateAfterLoading();
        return true;
	}
	
	/**
	 * After loading an agent from disk fill the param tabs of {@link Arena} according to the
	 * settings of this agent
	 * 
	 * @param n         fill the {@code n}th parameter tab
	 * @param m_arena	member {@code m_xab} has the param tabs
	 * 
	 * @see Arena#loadAgent
	 */
	public void fillParamTabsAfterLoading(int n, Arena m_arena) { 
		m_arena.m_xab.setMctsParFrom(n, this.getParMCTS() );
		m_arena.m_xab.setOParFrom(n, this.getParOther() );
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

//	/**
//	 * Get the best next action and return it
//	 * @param so			current game state (not changed on return)
//	 * @param random		allow epsilon-greedy random action selection	
//	 * @param vtable		the score for each available action (corresponding
//	 * 						to sob.getAvailableActions())
//	 * @param silent		verbosity control
//	 * @return actBest		the best action 
//	 */	
//	@Deprecated
//	@Override
//	public Types.ACTIONS getNextAction(StateObservation so, boolean random, double[] vtable, boolean silent) {
//
//		// this function selector is just intermediate, as long as we want to test getNextAction2 
//		// against getNextAction1 (the former getNextAction). Once everything works fine with
//		// getNextAction2, we should use only this function and make getNextAction deprecated 
//		// (requires appropriate changes in all other agents implementing interface PlayAgent).
//		if (!NEW_GNA) {
//	        return getNextAction1(so, vtable, silent);
//		} else {
//			Types.ACTIONS_VT actBestVT = getNextAction2(so.partialState(), random, silent);
//			double[] VTable = actBestVT.getVTable();
//			for (int i=0; i<VTable.length; i++) vtable[i] = VTable[i];
//			vtable[VTable.length] = actBestVT.getVBest();
//			return actBestVT;			
//		}
//    }
//
//    /**
//     * Get the best next action and return it (old version, NEW_GNA==false).
//     * Called by calcCertainty and getNextAction.
//     * 
//     * @param sob			current game state (not changed on return)
//     * @param vtable		must be an array of size n+1 on input, where
//     * 						n=sob.getNumAvailableActions(). On output,
//     * 						elements 0,...,n-1 hold the score for each available
//     * 						action (corresponding to sob.getAvailableActions())
//     * 						In addition, vtable[n] has the score for the
//     * 						best action.
//     * @return nextAction	the next action
//     */
//    private Types.ACTIONS getNextAction1(StateObservation so, double[] vtable, boolean silent) {
//		
//		//vtable = new double[so.getNumAvailableActions()];
//        // DON'T! The caller has to define vtable with the right length
//		
//		assert so.isLegalState() 
//			: "Not a legal state"; // e.g. player to move does not fit to Table
//		m_Timer.reset();
//		
//		// Ask MCTS for the best action ...
//		Types.ACTIONS actBest = act(so,m_Timer,vtable);
//		
//		actBest.setRandomSelect(false);		// the action was not a random move
//		
//    	return actBest;         
//	}

    /**
     * Get the best next action and return it (new version, NEW_GNA==true).
     * Called by calcCertainty and getNextAction.
     * 
     * @param so			current game state (not changed on return)
	 * @param random		currently not used (we act always as if random were false)
	 * @param silent		currently not used
     * @return actBest		the next action
	 * <p>						
	 * actBest has predicate isRandomAction() which is always false.<br>
	 * actBest has also the members vTable and vBest to store the value for each available
	 * action (as returned by so.getAvailableActions()) and the value for the best action actBest.
     */
    public Types.ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
		
        Types.ACTIONS actBest;
        Types.ACTIONS_VT actBestVT;
        List<Types.ACTIONS> actions = so.getAvailableActions();
		double[] VTable, vtable;
        vtable = new double[actions.size()];  
        VTable = new double[actions.size()+1];  // only for inner communication into method act()
	
		assert so.isLegalState() 
			: "Not a legal state"; // e.g. player to move does not fit to Table
		if (m_Timer==null) {
			m_Timer = new ElapsedCpuTimer(TimerType.CPU_TIME);
			m_Timer.setMaxTimeMillis(40);
		}
		m_Timer.reset();
		
		// Ask MCTS for the best action ...
		actBest = act(so,m_Timer,VTable);
		
		double bestScore = VTable[actions.size()];
		System.arraycopy(VTable, 0, vtable, 0, vtable.length);
		ScoreTuple scBest = new ScoreTuple(so,bestScore);
		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), false, vtable, bestScore, scBest);
		// we return with actBestVT an object whose vtable has length N = actions.size();
        return actBestVT;
	}


	/**
	 * Used by {@link #getScoreTuple(StateObservation, ScoreTuple)}
	 */
	@Override
	public double getScore(StateObservation so) {
		int nAct = so.getNumAvailableActions();
		double[] vtable = new double[nAct+1];
		
		assert so.isLegalState() 
		: "Not a legal state"; // e.g. player to move does not fit to Table
	
		// This if branch is vital: It was missing before, and if 'so' was a game-over state
		// this resulted in a NullpointerException later, because no child was added to root.
		// Now we fix this by returning so.getReward(so,rgs):
        if (so.isGameOver()) {
    		boolean rgs = this.getParOther().getRewardIsGameScore();
    		return so.getReward(so.getPlayer(), rgs);
//        	return so.getGameScore(so);
        } else {
        	
    		// Ask MCTS for the best action ...
    		act(so,m_Timer,vtable);

            return vtable[nAct];
        }
	}

	/**
	 * Return the agent's estimate of {@code sob}'s final game value (final reward) <b>for all players</b>. 
	 * Is called by the n-ply wrappers ({@link MaxN2Wrapper}, {@link ExpectimaxNWrapper}).
	 * @param so	the state s_t for which the value is desired
	 * 
	 * @return		an N-tuple with elements V(s_t|i), i=0,...,N-1, the agent's estimate of 
	 * 				the future score for s_t from the perspective of player i
	 */
	@Override
	public ScoreTuple getScoreTuple(StateObservation so, ScoreTuple prevTuple) {
		ScoreTuple sc = new ScoreTuple(so);
		int player;
		switch (so.getNumPlayers()) {
		case 1:
			sc.scTup[0] = this.getScore(so);
			break;
		case 2:
			player = so.getPlayer();
			int opponent = (player==0) ? 1 : 0;
			sc.scTup[player] = this.getScore(so);
			sc.scTup[opponent] = -sc.scTup[player];
			break;
		default:
			boolean rgs = mctsPlayer.getParOther().getRewardIsGameScore();
			player = so.getPlayer();

			// this is only partially correct, because for non-game-over states 'so' the tuple value for
			// other players than so.getPlayer() will be zero (at least for final-reward games). But
			// most methods that use getScoreTuple (like MaxN2Wrapper.estimateGameValueTuple) need only
			// the correct value for so.getPlayer(), which will be inserted into sc in the 3rd line:
			sc = so.getRewardTuple(rgs);
			if (!so.isGameOver())
				sc.scTup[player] = this.getScore(so);
				// return MCTS' estimate of the so's value for the player to move in so
		}
    	return sc;
	}

	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
		return this.getScoreTuple(sob, prevTuple);
	}
	
	@Override
	public String stringDescr() {
		String cs = getClass().getName();
		return cs + ": iterations:" + getParMCTS().getNumIter()
				+ ", rollout depth:" + getParMCTS().getRolloutDepth()
				+ ", K_UCT:"+ getParMCTS().getK_UCT()
				+ ", tree depth:" + getParMCTS().getTreeDepth();
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

}

