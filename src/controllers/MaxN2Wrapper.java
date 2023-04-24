package controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Arena;
import games.StateObservation;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_VT;

/**
 * Wrapper class for n-ply look-ahead in deterministic games. This class is NOT derived from {@link MaxNAgent}
 * (but applies the same MaxN principles).   <br>
 * [The former, now deprecated, {@code class MaxNWrapper extends MaxNAgent} was found to be error-prone and too complicated  
 * to maintain as good and simple software.]
 * 
 * @author Wolfgang Konen, TH Koeln, 2020
 */
public class MaxN2Wrapper extends AgentBase implements PlayAgent, Serializable {
	private final PlayAgent wrapped_pa;
	
	private final Random rand;
	protected int m_depth;

//	private final boolean OLDVERSION = false;  // normally false, true just for debug
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	// --- never used ---
//	public MaxN2Wrapper(PlayAgent pa, int nply) {
//		super("MaxN2Wrapper");
//		super.setMaxGameNum(1000);		
//		super.setGameNum(0);
//        rand = new Random(System.currentTimeMillis());
//		super.setAgentState(AgentState.TRAINED);
//		this.wrapped_pa = pa;
//		this.m_depth = nply;
//	}
	
	// XArenaFuncs::wrapAgent is now based on this agent to get other params 
	// from ParOther oPar
	public MaxN2Wrapper(PlayAgent pa, int nPly, ParOther oPar) {
		super("MaxN2Wrapper",oPar);
		super.setMaxGameNum(1000);		
		super.setGameNum(0);
        rand = new Random(System.currentTimeMillis());
		super.setAgentState(AgentState.TRAINED);
		m_depth = nPly;
		this.wrapped_pa = pa;
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
		super.fillParamTabsAfterLoading(n, m_arena);
		m_arena.m_xab.setMaxNDepthFrom(n, m_depth );
	}

	/**
	 * Get the best next action and return it
	 * @param so_in			current game state (not changed on return)
	 * @param random		allow epsilon-greedy random action selection	
	 * @param silent		controls printout
	 * @return actBest		the best action 
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable and vBest to store the value for each available
	 * action (as returned by so.getAvailableActions()) and the value for the best action actBest.
	 * 
	 */	
	@Override
	public ACTIONS_VT getNextAction2(StateObservation so_in, boolean random, boolean silent) {
		StateObservation so = so_in.copy(); // just for safety

        assert so.isLegalState() : "Not a legal state";
        
        // this starts the recursion:
		ACTIONS_VT act_best = getBestAction(so/*.clearedCopy()*/, random,  silent, 0, null);
											// bug fix 2020-09-25: clearedCopy leads to inferior MaxN2Wrapper[nply=0] (!)

        return act_best;
	}

	/**
	 * Loop over all actions available for {@code so} to find in a recursive tree search 
	 * the action with the best game value (best score for {@code so}'s player).
	 * 
	 * @param so		current game state (not changed on return)
	 * @param random	(not used currently) allow epsilon-greedy random action selection	
	 * @param silent    controls printout
	 * @param depth		tree depth
	 * @param prevTuple previous score tuple, contains scores for the other players
	 * @return		best action + V-table + score tuple
	 */
	private ACTIONS_VT getBestAction(StateObservation so, boolean random, 
			boolean silent, int depth, ScoreTuple prevTuple) 
	{
		int i;
		ScoreTuple currScoreTuple;
		StateObservation NewSO;
		ScoreTuple scBest = null;
        ACTIONS actBest;
        ACTIONS_VT act_vt;
        ArrayList<ACTIONS> bestActions = new ArrayList<>();
        double maxValue = -Double.MAX_VALUE;
        double value;

//		assert so.isLegalState() : "Not a legal state"; 		// only debug

		if (depth>=this.m_depth) {
			// this terminates the recursion. It returns the right ScoreTuple based on r(s)+gamma*V(s).
			return this.getWrappedPlayAgent().getNextAction2(so.partialState()/*.clearedCopy()*/, random, true);
		}

		ArrayList<ACTIONS> acts = so.getAvailableActions();
        double[] VTable =  new double[acts.size()];
        int P = so.getPlayer();

        for(i = 0; i < acts.size(); ++i)
        {
        	NewSO = so.copy();
        	NewSO.advance(acts.get(i));
        	
    		if (NewSO.isGameOver())
    		{
    			boolean rgs = m_oPar.getRewardIsGameScore();
    			currScoreTuple = NewSO.getRewardTuple(rgs);
    		} else {
				if (this.getWrappedPlayAgent() instanceof TDNTuple3Agt)
					prevTuple = estimateGameValueTuple(NewSO, prevTuple);
				// prevTuple is for wrappedAgent==TDNTuple3Agt and the case (N>=3): fill in the game value estimate
				// for the player who created sob. Will be used by subsequent states as a surrogate for the
				// then unknown value for that player.

				// here is the recursion: call this method again with depth+1:
				act_vt = getBestAction(NewSO/*.clearedCopy()*/, random, silent, depth+1, prevTuple);
				currScoreTuple = act_vt.getScoreTuple();

				currScoreTuple.combine(NewSO.getStepRewardTuple(), ScoreTuple.CombineOP.SUM,0,0);
				// NewSO.getStepRewardTuple returns 0.0, except for Rubik's Cube, where it returns CubeConfig.stepReward.
				// The increment by stepReward is very important for Rubik's Cube, because there every depth level means
				// an additional twist, thus additional costs (stepReward is negative). Otherwise, MaxN2Wrapper won't work.
				// The former implementation of the above line:
//				     if (so instanceof StateObserverCube)
//		  		          currScoreTuple.scTup[P] += CubeConfig.stepReward;
				// was not so nice SW design, because we had to clutter the generic MaxN2Wrapper code with
				// cube-specific code.]
			}

			// only debug for RubiksCube:
//			System.out.println(depth+": "+((StateObserverCube)NewSO).getCubeState().getTwistSeq()+", "+currScoreTuple);

			value = VTable[i] = currScoreTuple.scTup[P];
			// always *maximize* P's element in the tuple currScoreTuple, 
			// where P is the player to move in state so:
        	if (value==maxValue) bestActions.add(acts.get(i));
        	if (value>maxValue) {
        		maxValue = value;
        		scBest = new ScoreTuple(currScoreTuple);	// make a copy
        		bestActions.clear();
        		bestActions.add(acts.get(i));
        	}
        } // for
        
        // There might be one or more than one action with minValue. 
        // Break ties by selecting one of them randomly:
        actBest = bestActions.get(rand.nextInt(bestActions.size()));

        assert actBest != null : "Oops, no best action actBest";
        // optional: print the best action
        if (!silent) {
        	NewSO = so.copy();
        	NewSO.advance(actBest);
        	if (depth<=0)
        		System.out.println("--- "+depth+": Best Move: "+NewSO.stringDescr()+"   "+maxValue);
        }			

        act_vt = new ACTIONS_VT(actBest.toInt(), false, VTable, maxValue, scBest);
        return act_vt;         
	} // getBestAction

	// This older version is plain wrong, because it had the misconception that we should use the wrapped agent's method
	// estimateGameValueTuple. But this does not work for DAVI3Agent and RubiksCube (and it also slightly wrong in
	// general for all games since it returns for nPly=1 just what the wrapped agent would do). The right thing is to
	// use the wrapped agent's method getNextAction2 (see getBestAction above), which really has an effect already
	// for nPly=1 (and which passes the test MaxN2WrapperTest.nPlyEqualsZeroTest, that nPly=0 does the same as using the
	// wrapped agent directly).
	// The second thing wrong was the missing increment by getStepRewardTuple when returning from a recursion level.
//	/**
//	 * Loop over all actions available for {@code so} to find in a recursive tree search
//	 * the action with the best game value (best score for {@code so}'s player).
//	 *
//	 * @param so		current game state (not changed on return)
//	 * @param random	(not used currently) allow epsilon-greedy random action selection
//	 * @param silent
//	 * @param depth		tree depth
//	 * @param prevTuple previous score tuple, contains scores for the other players
//	 * @return		best action + V-table + score tuple
//	 */
//	private ACTIONS_VT getBestAction_OLD(StateObservation so, boolean random,
//									 boolean silent, int depth, ScoreTuple prevTuple)
//	{
//		int i,j;
//		ScoreTuple currScoreTuple=null;
//		StateObservation NewSO;
//		ScoreTuple scBest = null;
//		ACTIONS actBest = null;
//		ACTIONS_VT act_vt = null;
//		ArrayList<ACTIONS> bestActions = new ArrayList<>();
//		double maxValue = -Double.MAX_VALUE;
//		double value;
//
//		ArrayList<ACTIONS> acts = so.getAvailableActions();
//		double[] VTable =  new double[acts.size()+1];
//		boolean lowest = true;
//		currScoreTuple = (prevTuple==null) ? new ScoreTuple(so,lowest) : prevTuple;
//		int P = so.getPlayer();
//
//		for(i = 0; i < acts.size(); ++i)
//		{
//			NewSO = so.copy();
//			NewSO.advance(acts.get(i));
//
//			if (NewSO.isGameOver())
//			{
//				boolean rgs = m_oPar.getRewardIsGameScore();
//				currScoreTuple = NewSO.getRewardTuple(rgs);
//			} else {
//				if (depth<this.m_depth) {
//					if (this.getWrappedPlayAgent() instanceof TDNTuple3Agt)
//						prevTuple = estimateGameValueTuple(NewSO, prevTuple);
//					// prevTuple is for wrappedAgent==TDNTuple3Agt and the case (N>=3): fill in the game value estimate
//					// for the player who created sob. Will be used by subsequent states as a surrogate for the
//					// then unknown value for that player.
//
//					// here is the recursion: call this method again with depth+1:
//					currScoreTuple = getBestAction(NewSO, random, silent, depth+1, prevTuple).getScoreTuple();
//
//				} else {
//					// this terminates the recursion:
//					// (after finishing the for-loop for every element of acts)
//					currScoreTuple = estimateGameValueTuple(NewSO, currScoreTuple);
//					// estimateGameValueTuple returns the score tuple of the wrapped agent.
//				}
//			}
//
//			value = VTable[i] = currScoreTuple.scTup[P];
//			// always *maximize* P's element in the tuple currScoreTuple,
//			// where P is the player to move in state so:
//			if (value==maxValue) bestActions.add(acts.get(i));
//			if (value>maxValue) {
//				maxValue = value;
//				scBest = new ScoreTuple(currScoreTuple);
//				bestActions.clear();
//				bestActions.add(acts.get(i));
//			}
//		} // for
//
//		// There might be one or more than one action with minValue.
//		// Break ties by selecting one of them randomly:
//		actBest = bestActions.get(rand.nextInt(bestActions.size()));
//
//		assert actBest != null : "Oops, no best action actBest";
//		// optional: print the best action
//		if (!silent) {
//			NewSO = so.copy();
//			NewSO.advance(actBest);
//			if (depth<=0)
//				System.out.println("--- "+depth+": Best Move: "+NewSO.stringDescr()+"   "+maxValue);
//		}
//
//		VTable[acts.size()] = maxValue;
//		act_vt = new ACTIONS_VT(actBest.toInt(), false, VTable, maxValue, scBest);
//		return act_vt;
//	} // getBestAction_OLD

//	/**
//	 * DEPRECATED, use {@link #estimateGameValueTuple(StateObservation, ScoreTuple)} instead.
//	 * <p>
//	 * When the recursion tree has reached its maximal depth m_depth, then return
//	 * an estimate of the game score.
//	 * <p>
//	 * Here we use the wrapped {@link PlayAgent} to return a game value.
//	 *
//	 * @param sob	the state observation
//	 * @return		the estimated score
//	 */
//	@Override
//	@Deprecated
//	public double estimateGameValue(StateObservation sob) {
//		return this.estimateGameValueTuple(sob, null).scTup[sob.getPlayer()];
//	}

	/**
	 * When the recursion tree has reached its maximal depth m_depth, then return
	 * an estimate of the game score (tuple for all players).  
	 * <p>
	 * Here we use the wrapped {@link PlayAgent#estimateGameValueTuple(StateObservation, ScoreTuple)}
	 * to return a tuple of game values. This method returns V(s) + R(s) (score-to-come plus score-so-far)
	 * 
	 * @param sob		the state observation
	 * @param prevTuple	for N &ge; 3 player, we only know the game value for the player who <b>created</b>
	 * 					{@code sob}. To provide also values for other players, {@code prevTuple} allows
	 * 					passing in such other players' value from previous states, which may serve
	 * 					as surrogate for the unknown values in {@code sob}. {@code prevTuple} may be {@code null}.
	 * @return			the agent's estimate of the final game value (score-so-far plus score-to-come)
	 * 					<b>for all players</b>. The return value is a tuple containing
	 * 					{@link StateObservation#getNumPlayers()} {@code double}'s.
	 */
	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
		return wrapped_pa.estimateGameValueTuple(sob, prevTuple);
//		return wrapped_pa.getScoreTuple(sob, prevTuple);		// /WK/ 2021-09-10: old and flawed
//		return wrapped_pa.getNextAction2(sob,false,true).getScoreTuple(); // /WK/ 2021-09-13: also flawed
			// (because getNextAction2's ScoreTuple does not include score-so-far, but a final game state does)
	}
	
//	/**
//	 * Return the agent's score for that after state.
//	 * @param sob			the current game state;
//	 * @return				the probability that the player to move wins from that
//	 * 						state. If game is over: the score for the player who
//	 * 						*would* move (if the game were not over).
//	 * Each player wants to maximize its score
//	 */
//	@Override
//	public double getScore(StateObservation sob) {
//		return getBestAction(sob, false, true, 0, null).getScoreTuple().scTup[sob.getPlayer()];
//	}
	@Override
	public ScoreTuple getScoreTuple(StateObservation sob, ScoreTuple prevTuple) {
		return getBestAction(sob, false, true, 0, null).getScoreTuple();
	}

	@Override
	public String stringDescr() {
		String cs = wrapped_pa.getClass().getSimpleName();
		cs = cs + "[nPly="+m_depth+"]";
		return cs;
	}

    @Override
	public String stringDescr2() {
		return getClass().getSimpleName()+"["+wrapped_pa.getClass().getSimpleName()+"]" + ", nPly="+m_depth+"]";
	}
    
	// override AgentBase::getName()
	@Override
	public String getName() {
		String cs = super.getName();
		cs = cs + "["+wrapped_pa.getName()+","+m_depth+"]";
		return cs;
	}


	// --- never used ---
//	public String getFullName() {
//		String cs = wrapped_pa.getName();
//		cs = cs + "[nPly="+m_depth+"]";
//		return cs;
//	}

	@Override
	public PlayAgent getWrappedPlayAgent() {
		return wrapped_pa;
	}

	@Override
	public boolean isWrapper() { return true; }

	/**
	 * Train this agent for one episode, starting from state {@code so}.
	 * Train the inner (wrapped) agent, but use the outer agent (the wrapper) for selecting the next action.
	 *
	 * @param so    the start state of the episode
	 * @return	true, if agent raised a stop condition (deprecated)
	 */
	@Override
	public boolean trainAgent(StateObservation so) {
		resetAgent();
		return getWrappedPlayAgent().trainAgent(so,this);

	}

}
