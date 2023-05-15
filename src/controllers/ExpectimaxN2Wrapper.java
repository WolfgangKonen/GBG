package controllers;

import games.Arena;
import games.StateObsNondeterministic;
import games.StateObservation;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_ST;
import tools.Types.ACTIONS_VT;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * An alternative to {@link ExpectimaxNWrapper}, which uses wrapped_pa.getNextAction2 at end of recursion,
 * similar to {@link MaxN2Wrapper}.
 * <p>
 * *** But it does not work yet. Use instead {@link ExpectimaxNWrapper} ***
 *
 * @author Wolfgang Konen, TH Koeln, 2020
 * 
 * @see ExpectimaxNWrapper
 */
public class ExpectimaxN2Wrapper extends AgentBase implements PlayAgent, Serializable
{
	private Random rand;
	protected int m_depth=10;
	private PlayAgent wrapped_pa;
	int countTerminal;		// # of terminal node visits in getNextAction2
	int countMaxDepth;		// # of premature returns due to maxDepth in getNextAction2

	/**
	 * change the version ID for serialization only if a newer version is no longer
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	public ExpectimaxN2Wrapper(PlayAgent pa, int nply) {
		super("Expectimax2Wrapper");
		this.m_oPar.setWrapperNPly(nply); 	// deprecated
		this.m_wrPar.setWrapperNPly(nply);
		m_depth = nply;
		this.wrapped_pa = pa;
	}

	public ExpectimaxN2Wrapper(String name)
	{
		super(name);
		super.setMaxGameNum(1000);
		super.setGameNum(0);
        rand = new Random(System.currentTimeMillis());
		//hm = new HashMap<String, ScoreTuple>();
		setAgentState(AgentState.TRAINED);
	}

	public ExpectimaxN2Wrapper(String name, int nply)
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
		super.fillParamTabsAfterLoading(n, m_arena);
		m_arena.m_xab.setMaxNDepthFrom(n, this.getDepth() );
	}
	
	/**
	 * Get the best next action and return it
	 * @param so            current game state (not changed on return), has to be an
	 * 						object of class {@link StateObsNondeterministic}
	 * @param random        allow epsilon-greedy random action selection
	 * @param deterministic
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
	public ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean deterministic, boolean silent) {
        List<ACTIONS> actions = so.getAvailableActions();
		double[] vTable = new double[actions.size()];
//		silent=false;
		
		if (!(so instanceof StateObsNondeterministic)) 
			throw new RuntimeException("Error, Expectimax-N only usable for nondeterministic games");
		StateObsNondeterministic soND = (StateObsNondeterministic) so;
		countMaxDepth = countTerminal = 0;

		if (!silent) {
			System.out.println("EW called for: ");
			System.out.print(soND);
		}

		ACTIONS_VT actBest = getBestAction(soND, so,  random,  vTable,  silent, 1);

		if (!silent) {
			DecimalFormat frmAct = new DecimalFormat("0000");
			DecimalFormat frmVal = new DecimalFormat("+0.00;-0.00");
			System.out.println(
					"so.diceVal="+soND.getNextNondeterministicAction().toInt()
							+", bestValue["+soND.getPlayer()+"]="+frmVal.format(actBest.getVBest())
							+", bestAction="+frmAct.format(actBest.toInt())
							+", countTerminal="+getCountTerminal()
							+", countMaxDepth="+getCountMaxDepth());
		}

		return actBest;
	}

	/**
	 * Loop over all actions available for {@code soND} to find the action with the best 
	 * score tuple (best score for {@code soND}'s player).
	 * 
	 * @param soND		current game state (not changed on return)
	 * @param refer		referring game state (=soND on initial call)	
	 * @param random	allow epsilon-greedy random action selection	
	 * @param vTable	size soND.getAvailableActions()+1
	 * @param silent
	 * @param depth		tree depth
	 * @return best action + V-table + vBest + score tuple. Note that best action, V-table and vBest
	 *		   are only relevant if {@code soND.isNextActionDeterministic}
	 */
	private ACTIONS_VT getBestAction(StateObsNondeterministic soND, StateObservation refer, boolean random,
			double[] vTable, boolean silent, int depth)
	{
		int i,j;
		double vBest;
		ScoreTuple currScoreTuple=null;
        ScoreTuple sc, scBest=null;
		StateObsNondeterministic NewSO;
        ACTIONS actBest = null;
        ACTIONS_ST act_st = null;

        assert soND.isLegalState() : "Not a legal state"; 

        int player = soND.getPlayer();

		if (depth>=this.m_depth) {
			countMaxDepth++;
			// this terminates the recursion. It returns the right ScoreTuple based on r(s)+gamma*V(s).
			ACTIONS_VT act_vt = this.getWrappedPlayAgent().getNextAction2(soND.partialState(), random, false, true);
			return act_vt;
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
            	//if (!silent && depth<3) printAfterstate(soND,actions[i],currScoreTuple,depth);
            	vTable[i] = currScoreTuple.scTup[player];
            	
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
        		if (vTable[i]==pMaxScore) {
        			if ((j++)==selectJ) actBest = new ACTIONS(actions[i]);
        		}
        	}
            
            vBest = pMaxScore;
            //if (!silent && depth<3) printBestAfterstate(soND,actBest,pMaxScore,depth);

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
            	NewSO.advanceNondetSpecific(actions[i]);
            	
				// here is the recursion: getAllScores may call getBestAction back:
				currScoreTuple = getAllScores(NewSO,refer,silent,depth+1);		
				
				currProbab = soND.getProbability(actions[i]);
            	//if (!silent) printNondet(NewSO,currScoreTuple,currProbab,depth);
				sumProbab += currProbab;
				// if cOP==AVG, expecScoreTuple will contain the average ScoreTuple
				// if cOP==MIN, expecScoreTuple will contain the worst ScoreTuple for 
				// player (this considers the environment as an adversarial player)
				expecScoreTuple.combine(currScoreTuple, cOP, player, currProbab);
            }
            assert (Math.abs(sumProbab-1.0)<1e-8) : "Error: sum of probabilities is not 1.0";
        	//if (!silent) printNondet(soND,expecScoreTuple,sumProbab,depth);
			scBest = expecScoreTuple;
			actBest = rans.get(0); 		// this is just a dummy
			vBest = 0.0;				// this is just a dummy
        } // else (isNextActionDeterministic)

        assert actBest != null : "Oops, no best action actBest";

        actBest.setRandomSelect(false);
		ACTIONS_VT act_vt = new ACTIONS_VT(actBest.toInt(), false, vTable, vBest, scBest);
        return act_vt;
	}

	private ScoreTuple getAllScores(StateObsNondeterministic sob, StateObservation refer, boolean silent, int depth) {
		if (sob.isGameOver())
		{
			countTerminal++;
			boolean rgs = m_oPar.getRewardIsGameScore();
			return sob.getRewardTuple(rgs);
		}
				
		int n=sob.getNumAvailableActions();
		double[] vTable	= new double[n];
		
		// here is the recursion: getBestAction calls getAllScores(...,depth+1):
		ACTIONS_VT act_vt = getBestAction(sob, refer, false,  vTable,  silent, depth);

		return act_vt.getScoreTuple();		// return ScoreTuple for best action
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
//		assert sob instanceof StateObsNondeterministic : "Error, sob must be of class StateObservationNondet";
//		StateObsNondeterministic soND = (StateObsNondeterministic) sob;
//
//		return getAllScores(soND,sob,true,0).scTup[sob.getPlayer()];
//	}
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
		Types.ACTIONS_VT actBest = wrapped_pa.getNextAction2(sob,false, false, true);
		return actBest.getScoreTuple();
//		return wrapped_pa.getScoreTuple(sob, prevTuple);		// /WK/ 2021-09-10: old and flawed
	}

	@Override
	public String stringDescr() {
		String cs = wrapped_pa.getClass().getSimpleName();
		cs = cs + "[nPly="+m_depth+"]";
		return cs;
	}

	// getName: use method ObserverBase::getName()

	public int getDepth() {
		return m_depth;
	}

	public int getCountTerminal() {
		return countTerminal;
	}

	public int getCountMaxDepth() {
		return countMaxDepth;
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