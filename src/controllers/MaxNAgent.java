package controllers;

import controllers.PlayAgent;
import controllers.MinimaxAgent;
import games.Arena;
import games.StateObservation;
import games.XArenaMenu;
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
 * The Max-N agent implements the Max-N algorithm [Korf91] via interface {@link PlayAgent}. 
 * Max-N is the generalization of {@link MinimaxAgent} to N players. It works on {@link ScoreTuple}, 
 * an N-tuple of game scores. It traverses the game tree up to a prescribed 
 * depth (default: 10, see {@link MaxNParams}). To speed up calculations, already 
 * visited states are stored in a HashMap.  
 * <p>
 * {@link MaxNAgent} is for <b>deterministic</b> games. For non-deterministic games see 
 * {@link ExpectimaxNAgent}.
 * <p>
 * See [Korf91] 
 * R. E. Korf: <em>Multi-Player Alpha-Beta Pruning</em>, Artificial Intelligence 48, 1991, 99-111.
 * 
 * @author Wolfgang Konen, TH Köln, Dec'17
 * 
 * @see ScoreTuple
 * @see ExpectimaxNAgent
 * @see MaxNParams
 */
public class MaxNAgent extends AgentBase implements PlayAgent, Serializable
{
	private Random rand;
	protected int m_depth=10;
	protected boolean m_useHashMap=false; //true;
	private HashMap<String,ScoreTuple> hm;
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;
		
	public MaxNAgent(String name)
	{
		super(name);
		super.setMaxGameNum(1000);		
		super.setGameNum(0);
        rand = new Random(System.currentTimeMillis());
		hm = new HashMap<String, ScoreTuple>();
		super.setAgentState(AgentState.TRAINED);
	}
	
	public MaxNAgent(String name, int depth)
	{
		this(name);
		m_depth = depth;
	}
		
	public MaxNAgent(String name, int depth, boolean useHashMap)
	{
		this(name);
		m_depth = depth;
		m_useHashMap = useHashMap;
	}
		
	public MaxNAgent(String name, ParMaxN mPar, ParOther oPar)
	{
		super(name,oPar);
		super.setMaxGameNum(1000);		
		super.setGameNum(0);
        rand = new Random(System.currentTimeMillis());
		hm = new HashMap<String, ScoreTuple>();
		super.setAgentState(AgentState.TRAINED);
		m_depth = mPar.getMaxNDepth();
		m_useHashMap = mPar.useMaxNHashmap();
//		m_oPar = new ParOther(oPar);		// AgentBase::m_oPar
	}
		
	/**
	 * After loading an agent from disk fill the param tabs of {@link Arena} according to the
	 * settings of this agent
	 * 
	 * @param n         fill the {@code n}th parameter tab
	 * @param m_arena	member {@code m_xab} has the param tabs
	 * 
	 * @see XArenaMenu#loadAgent
	 * @see XArenaTabs
	 */
	public void fillParamTabsAfterLoading(int n, Arena m_arena) { 
		m_arena.m_xab.setMaxNDepthFrom(n, this.getDepth() );
//		m_arena.m_xab.setOParFrom(n, this.getParOther() );		// do or don't?
	}
	
	/**
	 * Get the best next action and return it
	 * @param so			current game state (not changed on return)
	 * @param random		allow epsilon-greedy random action selection	
	 * @param silent
	 * @return actBest		the best action 
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
		
		ACTIONS_ST act_best = getBestAction(so, so,  random,  VTable,  silent, 0+1);
		
        return new ACTIONS_VT(act_best.toInt(), act_best.isRandomAction(), VTable);
	}

	/**
	 * Loop over all actions available for {@code so} to find the action with the best 
	 * score tuple (best score for {@code so}'s player).
	 * 
	 * @param so		current game state (not changed on return)
	 * @param refer		referring game state (=so on initial call)	
	 * @param random	allow epsilon-greedy random action selection	
	 * @param VTable	size so.getAvailableActions()+1
	 * @param silent
	 * @param depth		tree depth
	 * @return		best action + score tuple
	 */
	private ACTIONS_ST getBestAction(StateObservation so, StateObservation refer, boolean random, 
			double[] VTable, boolean silent, int depth) 
	{
		int i,j;
		ScoreTuple currScoreTuple=null;
        ScoreTuple sc, scBest=null;
		StateObservation NewSO;
        ACTIONS actBest = null;
        ACTIONS_ST act_st = null;
        String stringRep ="";

        assert so.isLegalState() : "Not a legal state"; 

//        if (this instanceof MaxNWrapper) 
//        	System.out.println("MaxN: depth="+depth+"  "+so.stringDescr());
        
        double pMaxScore = -Double.MAX_VALUE;
        ArrayList<ACTIONS> acts = so.getAvailableActions();
        ACTIONS[] actions = new ACTIONS[acts.size()];
    	scBest=new ScoreTuple(so);		// make a new ScoreTuple with lowest possible maxValue
        int P = so.getPlayer();
        
        for(i = 0; i < acts.size(); ++i)
        {
        	actions[i] = acts.get(i);
        	NewSO = so.copy();
        	NewSO.advance(actions[i]);
        	
        	if (m_useHashMap) {
    			// speed up MaxNAgent for repeated calls by storing/retrieving the 
    			// scores of visited states in HashMap hm:
    			stringRep = NewSO.stringDescr();
    			sc = hm.get(stringRep); 		// returns null if not in hm
    			//System.out.println(stringRep+":"+sc);
        	} else {
        		sc = null;
        	}
			if (depth<this.m_depth) {
				if (sc==null) {
					// here is the recursion: getAllScores may call getBestAction back:
					currScoreTuple = getAllScores(NewSO,refer,depth+1);	
					
					if (m_useHashMap) {
						hm.put(stringRep, currScoreTuple);
		    			//System.out.println(stringRep+":"+currScoreTuple);
					}
				} else {
					currScoreTuple = sc;
				}
			} else {
				// this terminates the recursion:
				// (after finishing the for-loop for every element of acts)
				currScoreTuple = estimateGameValueTuple(NewSO);
				// For derived class MaxNWrapper, estimateGameValueTuple returns
				// the score tuple of the wrapped agent. 
			}
        	VTable[i] = currScoreTuple.scTup[P];
			
			// always *maximize* P's element in the tuple currScoreTuple, 
			// where P is the player to move in state so:
			ScoreTuple.CombineOP cOP = ScoreTuple.CombineOP.MAX;
			scBest.combine(currScoreTuple, cOP, P, 0.0);            	
        } // for
        
        // There might be one or more than one action with pMaxScore. 
        // Break ties by selecting one of them randomly:
    	int selectJ = (int)(rand.nextDouble()*scBest.count);
    	pMaxScore = scBest.scTup[P];
    	for (i=0, j=0; i < actions.length; ++i) {
    		if (VTable[i]==pMaxScore) {
    			if ((j++)==selectJ) actBest = new ACTIONS(actions[i]);
    		}
    	}

        assert actBest != null : "Oops, no best action actBest";
        // optional: print the best action
        if (!silent) {
        	NewSO = so.copy();
        	NewSO.advance(actBest);
        	System.out.print("---Best Move: "+NewSO.stringDescr()+"   "+pMaxScore);
        }			

        VTable[actions.length] = pMaxScore;
        actBest.setRandomSelect(false);
        act_st = new ACTIONS_ST(actBest, scBest);
        return act_st;         
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
		return getAllScores(sob,sob,0).scTup[sob.getPlayer()];
	}
	@Override
	public ScoreTuple getScoreTuple(StateObservation sob) {
		return getAllScores(sob,sob,0);
	}
	private ScoreTuple getAllScores(StateObservation sob, StateObservation refer, int depth) {
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
		
		// here is the recursion: getBestAction calls getScore(...,depth+1):
		act_st = getBestAction(sob, refer, false,  vtable,  true, depth);  // sets vtable[n]=iMaxScore
		
		return act_st.m_st;		// return ScoreTuple for best action
	}

	/**
	 * When the recursion tree has reached its maximal depth m_depth, then return
	 * an estimate of the game score. This function may be overridden in a game-
	 * specific way by classes derived from {@link MaxNAgent}. 
	 * <p>
	 * This  stub method just returns {@link StateObservation#getReward(StateObservation, boolean)},
	 * which might be too simplistic for not-yet finished games, because the current reward does   
	 * not reflect future rewards.
	 * @param sob	the state observation
	 * @return		the estimated score
	 */
	@Override
	public double estimateGameValue(StateObservation sob) {
		return sob.getReward(sob,true);
	}

	/**
	 * When the recursion tree has reached its maximal depth m_depth, then return
	 * an estimate of the game score (tuple for all players). This function may be overridden 
	 * in a game-specific way by classes derived from {@link MaxNAgent}. 
	 * <p>
	 * This  stub method just returns {@link StateObservation#getReward(int,boolean)} for every 
	 * player, which might be too simplistic for not-yet finished games, because the current 
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
	

}