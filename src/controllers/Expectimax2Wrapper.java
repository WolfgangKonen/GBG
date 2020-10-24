package controllers;

import games.Arena;
import games.StateObsNondeterministic;
import games.StateObservation;
import params.ParMaxN;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_ST;
import tools.Types.ACTIONS_VT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * An alternative to {@link ExpectimaxWrapper}, which uses wrapped_pa.getNextAction2 at end of recursion,
 * similar to {@link MaxN2Wrapper}. But it does not work yet.
 *
 * @author Wolfgang Konen, TH Koeln, 2020
 * 
 * @see ExpectimaxWrapper
 */
public class Expectimax2Wrapper extends AgentBase implements PlayAgent, Serializable
{
	private Random rand;
	protected int m_depth=10;
	private PlayAgent wrapped_pa;

	/**
	 * change the version ID for serialization only if a newer version is no longer
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	public Expectimax2Wrapper(PlayAgent pa, int nply) {
		super("Expectimax2Wrapper");
		this.m_oPar.setWrapperNPly(nply);
		m_depth = nply;
		this.wrapped_pa = pa;
	}

	public Expectimax2Wrapper(String name)
	{
		super(name);
		super.setMaxGameNum(1000);
		super.setGameNum(0);
        rand = new Random(System.currentTimeMillis());
		//hm = new HashMap<String, ScoreTuple>();
		setAgentState(AgentState.TRAINED);
	}

	public Expectimax2Wrapper(String name, int nply)
	{
		this(name);
		m_depth = nply;
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
		m_arena.m_xab.setMaxNDepthFrom(n, this.getDepth() );
//		m_arena.m_xab.setOParFrom(n, this.getParOther() );		// do or don't?
	}
	
	/**
	 * Get the best next action and return it
	 * @param so			current game state (not changed on return), has to be an 
	 * 						object of class {@link StateObsNondeterministic}
	 * @param random		allow epsilon-greedy random action selection	
	 * @param silent
	 * @return actBest		the best action 
	 * @throws RuntimeException if {@code so} is not of class {@link StateObsNondeterministic}
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable and vBest to store the value for each available
	 * action (as returned by so.getAvailableActions()) and the value for the best action actBest.
	 * 
	 */	
	@Override
	public ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
        List<ACTIONS> actions = so.getAvailableActions();
		double[] VTable = new double[actions.size()+1];  
//		silent=false;
		
		if (!(so instanceof StateObsNondeterministic)) 
			throw new RuntimeException("Error, Expectimax-N only usable for nondeterministic games");
		StateObsNondeterministic soND = (StateObsNondeterministic) so;
		
		ACTIONS_ST act_best = getBestAction(soND, so,  random,  VTable,  silent, 1);
		
        return new ACTIONS_VT(act_best.toInt(), act_best.isRandomAction(), VTable);
	}

	/**
	 * Loop over all actions available for {@code soND} to find the action with the best 
	 * score tuple (best score for {@code soND}'s player).
	 * 
	 * @param soND		current game state (not changed on return)
	 * @param refer		referring game state (=soND on initial call)	
	 * @param random	allow epsilon-greedy random action selection	
	 * @param VTable	size soND.getAvailableActions()+1
	 * @param silent
	 * @param depth		tree depth
	 * @return		best action + score tuple
	 */
	private ACTIONS_ST getBestAction(StateObsNondeterministic soND, StateObservation refer, boolean random, 
			double[] VTable, boolean silent, int depth) 
	{
		int i,j;
		ScoreTuple currScoreTuple=null;
        ScoreTuple sc, scBest=null;
		StateObsNondeterministic NewSO;
        ACTIONS actBest = null;
        ACTIONS_ST act_st = null;

        assert soND.isLegalState() : "Not a legal state"; 

        int player = soND.getPlayer();

		if (depth>=this.m_depth) {
			// this terminates the recursion. It returns the right ScoreTuple based on r(s)+gamma*V(s).
			ACTIONS_VT act_vt = this.getWrappedPlayAgent().getNextAction2(soND.partialState(), random, true);
			return new ACTIONS_ST(act_vt);
		}

		if (soND.isNextActionDeterministic()) {
        	//
        	// find the best next deterministic action for current player in state soND
        	//
            ArrayList<ACTIONS> acts = soND.getAvailableActions();
            ACTIONS[] actions = new ACTIONS[acts.size()];
        	scBest=new ScoreTuple(soND);		// make a new ScoreTuple with lowest possible maxValue
            for(i = 0; i < acts.size(); ++i)
            {
            	actions[i] = acts.get(i);
            	NewSO = soND.copy();
            	NewSO.advanceDeterministic(actions[i]);
            	
//           	if (depth<this.m_depth) {
    				// here is the recursion: getAllScores may call getBestAction back:
    				currScoreTuple = getAllScores(NewSO,refer,silent,depth+1);						
//    			} else {
//    				// this terminates the recursion:
//    				// (after finishing the for-loop for every element of acts)
//    				currScoreTuple = estimateGameValueTuple(NewSO, null);
//    				// For derived class ExpectimaxWrapper, estimateGameValueTuple returns
//    				// the score tuple of the wrapped agent.
//    			}
            	if (!silent && depth<3) printAfterstate(soND,actions[i],currScoreTuple,depth);
            	VTable[i] = currScoreTuple.scTup[player];
            	
    			// always *maximize* P's element in the tuple currScoreTuple, 
    			// where P is the player to move in state soND:
    			ScoreTuple.CombineOP cOP = ScoreTuple.CombineOP.MAX;
    			scBest.combine(currScoreTuple, cOP, player, 0.0);            	
            } // for
            
            // There might be one or more than one action with pMaxScore. 
            // Break ties by selecting one of them randomly:
        	double pMaxScore = scBest.scTup[player];
        	int selectJ = (int)(rand.nextDouble()*scBest.count);
        	for (i=0, j=0; i < actions.length; ++i) {
        		if (VTable[i]==pMaxScore) {
        			if ((j++)==selectJ) actBest = new ACTIONS(actions[i]);
        		}
        	}
            
            VTable[actions.length] = pMaxScore;
            if (!silent && depth<3) printBestAfterstate(soND,actBest,pMaxScore,depth);

        } // if (isNextActionDeterministic)
        else 
        { // i.e. if next action is nondeterministic:
        	//
        	// average (or min) over all next nondeterministic actions 
        	//
            ArrayList<ACTIONS> rans = soND.getAvailableRandoms();
            assert (rans.size()>0) : "Error: getAvailableRandoms returns no actions";
            ACTIONS[] actions = new ACTIONS[rans.size()];
    		ScoreTuple expecScoreTuple=new ScoreTuple(soND);
    		// select one of the following two lines:
			ScoreTuple.CombineOP cOP = ScoreTuple.CombineOP.AVG;
			//ScoreTuple.CombineOP cOP = ScoreTuple.CombineOP.MIN;
			double currProbab;
			double sumProbab=0.0;
            for(i = 0; i < rans.size(); ++i)
            {
            	actions[i] = rans.get(i);
            	NewSO = soND.copy();
            	NewSO.advanceNondeterministic(actions[i]);
            	
				// here is the recursion: getAllScores may call getBestAction back:
				currScoreTuple = getAllScores(NewSO,refer,silent,depth+1);		
				
				currProbab = soND.getProbability(actions[i]);
            	if (!silent) printNondet(NewSO,currScoreTuple,currProbab,depth);
				sumProbab += currProbab;
				// if cOP==AVG, expecScoreTuple will contain the average ScoreTuple
				// if cOP==MIN, expecScoreTuple will contain the worst ScoreTuple for 
				// player (this considers the environment as an adversarial player)
				expecScoreTuple.combine(currScoreTuple, cOP, player, currProbab);
            }
            assert (Math.abs(sumProbab-1.0)<1e-8) : "Error: sum of probabilites is not 1.0";
        	if (!silent) printNondet(soND,expecScoreTuple,sumProbab,depth);
            scBest = expecScoreTuple;	
            actBest = rans.get(0); 		// this is just a dummy 
        } // else (isNextActionDeterministic)

        assert actBest != null : "Oops, no best action actBest";

        actBest.setRandomSelect(false);
        act_st = new ACTIONS_ST(actBest, scBest);
        return act_st;         
	}

	private ScoreTuple getAllScores(StateObsNondeterministic sob, StateObservation refer, boolean silent, int depth) {
        ACTIONS_ST act_st = null;
		if (sob.isGameOver())
		{
			boolean rgs = m_oPar.getRewardIsGameScore();
			return sob.getRewardTuple(rgs);
		}
				
		int n=sob.getNumAvailableActions();
		double[] vtable	= new double[n+1];
		
		// here is the recursion: getBestAction calls getAllScores(...,depth+1):
		act_st = getBestAction(sob, refer, false,  vtable,  silent, depth);  // sets vtable[n]=iMaxScore
		
		return act_st.m_st;		// return ScoreTuple for best action
	}

	/**
	 * Return the agent's score for that after state.
	 * @param sob			the current game state;
	 * @return				the probability that the player to move wins from that 
	 * 						state. If game is over: the score for the player who 
	 * 						*would* move (if the game were not over).
	 * Each player wants to maximize its score	 
	 */
	@Override
	public double getScore(StateObservation sob) {
		assert sob instanceof StateObsNondeterministic : "Error, sob must be of class StateObservationNondet";
		StateObsNondeterministic soND = (StateObsNondeterministic) sob;
		
		return getAllScores(soND,sob,true,0).scTup[sob.getPlayer()];
	}
	@Override
	public ScoreTuple getScoreTuple(StateObservation sob, ScoreTuple prevTuple) {
		assert sob instanceof StateObsNondeterministic : "Error, sob must be of class StateObservationNondet";
		StateObsNondeterministic soND = (StateObsNondeterministic) sob;
		
		return getAllScores(soND,sob,true,0);
	}
	
	/**
	 * When the recursion tree has reached its maximal depth m_depth, then return
	 * an estimate of the game score (tuple for all players).
	 * <p>
	 * Here we use the wrapped {@link PlayAgent} to return a tuple of game values.
	 *
	 * @param sob	the state observation
	 * @return		the tuple of estimated score
	 */
	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
		return wrapped_pa.getScoreTuple(sob, prevTuple);
	}

	public PlayAgent getWrappedPlayAgent() {
		return wrapped_pa;
	}

	@Override
	public String stringDescr() {
		String cs = wrapped_pa.getClass().getSimpleName();
		cs = cs + "[nPly="+m_depth+"]";
		return cs;
	}

	// getName: use method ObserverBase::getName()

	public String getFullName() {
		String cs = wrapped_pa.getClass().getSimpleName();
		cs = cs + "[nPly="+m_depth+"]";
		return cs;
	}

	public int getDepth() {
		return m_depth;
	}

    private void printAfterstate(StateObsNondeterministic soND,ACTIONS actBest,
    		ScoreTuple scTuple, int depth)
    {
		StateObsNondeterministic NewSO = soND.copy();
    	NewSO.advanceDeterministic(actBest);
    	System.out.println("---     Move: "+NewSO.stringDescr()+"   "+scTuple.toString()+", depth="+depth);
    }	 

    private void printBestAfterstate(StateObsNondeterministic soND,ACTIONS actBest,
    		double pMaxScore, int depth)
    {
		StateObsNondeterministic NewSO = soND.copy();
    	NewSO.advanceDeterministic(actBest);
    	System.out.println("---Best Move: "+NewSO.stringDescr()+"   "+pMaxScore+", depth="+depth);
    }	

    private void printNondet(StateObsNondeterministic NewSO,
    		ScoreTuple scTuple, double currProbab, int depth)
    {
    	System.out.println("---   Random: "+NewSO.stringDescr()+"   "+scTuple.toString()+
    			", p="+currProbab+", depth="+depth);
    }	 

}