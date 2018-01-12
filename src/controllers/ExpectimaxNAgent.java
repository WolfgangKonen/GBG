package controllers;

import controllers.PlayAgent;
import controllers.MinimaxAgent;
import games.StateObservation;
import games.StateObsNondeterministic;
import params.MaxNParams;
import params.ParMaxN;
import params.ParOther;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_ST;
import tools.Types.ACTIONS_VT;
import tools.Types.ScoreTuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * The Expectimax-N agent implements the Max-N algorithm [Korf91] via interface {@link PlayAgent}. 
 * Expectimax-N  is the generalization {@link MaxNAgent} to nondeterministic games. It works on  
 * {@link ScoreTuple}, an N-tuple of game scores. It traverses the game tree up to a prescribed 
 * depth (default: 10, see {@link MaxNParams}).   
 * <p>
 * {@link ExpectimaxNAgent} is for <b>nondeterministic</b> games. For deterministic games see 
 * {@link MaxNAgent}.
 * <p>
 * See [Korf91] 
 * R. E. Korf: <em>Multi-Player Alpha-Beta Pruning</em>, Artificial Intelligence 48, 1991, 99-111.
 * 
 * @author Wolfgang Konen, TH Köln, Dec'17
 * 
 * @see ScoreTuple
 * @see MaxNAgent
 */
public class ExpectimaxNAgent extends AgentBase implements PlayAgent, Serializable
{
	private Random rand;
	protected int m_depth=10;
//	protected boolean m_rgs=true;  // use now AgentBase::m_oPar.getRewardIsGameScore()
	//private boolean m_useHashMap=true;
	//private HashMap<String,ScoreTuple> hm;
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;
		
	public ExpectimaxNAgent(String name)
	{
		super(name);
		super.setMaxGameNum(1000);		
		super.setGameNum(0);
        rand = new Random(System.currentTimeMillis());
		//hm = new HashMap<String, ScoreTuple>();
		setAgentState(AgentState.TRAINED);
	}
	
	public ExpectimaxNAgent(String name, ParMaxN mpar, ParOther opar)
	{
		this(name);
		m_depth = mpar.getMaxnDepth();
		m_oPar = opar;		// AgentBase::m_oPar
	}
	
	public ExpectimaxNAgent(String name, int nply)
	{
		this(name);
		m_depth = nply;
	}
		
	/**
	 * Get the best next action and return it
	 * @param so			current game state (not changed on return), has to be an 
	 * 						object of class StateObservationNondet
	 * @param random		allow epsilon-greedy random action selection	
	 * @param silent
	 * @return actBest		the best action 
	 * @throws RuntimeException, if {@code so} is not of class StateObservationNondet
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
            	
            	if (depth<this.m_depth) {
    				// here is the recursion: getAllScores may call getBestAction back:
    				currScoreTuple = getAllScores(NewSO,refer,silent,depth+1);						
    			} else {
    				// this terminates the recursion:
    				// (after finishing the for-loop for every element of acts)
    				currScoreTuple = estimateGameValueTuple(NewSO);
    			}
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
				// player (this considers environment as adversarial player)
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
//			double[] res = new double[sob.getNumPlayers()];
//			for (int i=0; i<sob.getNumPlayers(); i++) res[i] = sob.getReward(i, rgs);
//			return new ScoreTuple(res); 	
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
	public ScoreTuple getScoreTuple(StateObservation sob) {
		assert sob instanceof StateObsNondeterministic : "Error, sob must be of class StateObservationNondet";
		StateObsNondeterministic soND = (StateObsNondeterministic) sob;
		
		return getAllScores(soND,sob,true,0);
	}
	
	/**
	 * When the recursion tree has reached its maximal depth m_depth, then return
	 * an estimate of the game score. This function may be overridden in a game-
	 * specific way by classes derived from {@link ExpectimaxNAgent}. <p>
	 * This  stub method just returns {@link StateObservation#getReward(boolean)}, which might 
	 * be too simplistic for not-yet finished games, because the current reward does not reflect  
	 * future returns.
	 * @param sob	the state observation
	 * @return		the estimated score
	 */
	@Override
	public double estimateGameValue(StateObservation sob) {
		boolean rgs = m_oPar.getRewardIsGameScore();
		return sob.getReward(rgs);
	}

	/**
	 * When the recursion tree has reached its maximal depth m_depth, then return
	 * an estimate of the game score (tuple for all players). This function may be overridden 
	 * in a game-specific way by classes derived from {@link ExpectimaxNAgent}. <p>
	 * This  stub method just returns {@link StateObservation#getReward(boolean)} for all 
	 * players, which might be too simplistic for not-yet finished games, because the current 
	 * reward may not reflect future rewards.
	 * @param sob	the state observation
	 * @return		the estimated score tuple
	 */
	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob) {
		boolean rgs = m_oPar.getRewardIsGameScore();
		ScoreTuple sc = new ScoreTuple(sob);
		for (int i=0; i<sob.getNumPlayers(); i++) 
			sc.scTup[i] = sob.getReward(i, rgs);
		return sc;
	}

	@Override
	public String stringDescr() {
		String cs = getClass().getName();
		cs = cs + ", depth:"+m_depth;
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

	
	// currently not used:
//  private ScoreTuple scoreFromHashMap(StateObservationNondet NewSO)	{
//    	ScoreTuple sc = null;
//        String stringRep ="";
//    	if (m_useHashMap) {
//			// speed up MinimaxPlayer for repeated calls by storing/retrieving the 
//			// scores of visited states in HashMap hm:
//			stringRep = NewSO.stringDescr();
//			//System.out.println(stringRep);
//			sc = hm.get(stringRep); 		// returns null if not in hm
//    	} 
//    	return sc;
//    }
//	private void scoreToHashMap(ScoreTuple CurrScoreTuple, StateObservationNondet NewSO) {
//		if (m_useHashMap) {
//			String stringRep = NewSO.stringDescr();
//			hm.put(stringRep, CurrScoreTuple);
//		}
//	}



}