package controllers;

import controllers.PlayAgent;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Arena;
import games.StateObservation;
import games.XArenaMenu;
import params.MaxNParams;
import params.ParMaxN;
import params.ParOther;
import tools.MessageBox;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_ST;
import tools.Types.ACTIONS_VT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

/**
 * The Max-N agent implements the Max-N algorithm [Korf91] via interface {@link PlayAgent}. 
 * Max-N is the generalization of the well-known Minimax agent to N players. It works on {@link ScoreTuple}, 
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
		
        try {
        	this.estimateGameValueTuple(so, null);
        } catch (RuntimeException e) {
        	if (e.getMessage()==AgentBase.EGV_EXCEPTION_TEXT) {
            	String str = "MaxNWrapper: The wrapped agent does not implement estimateGameValueTuple "
            			+ "\n --> set Other pars: Wrapper nPly to 0";
    			MessageBox.show(null,str,
    					"MaxNAgent", JOptionPane.ERROR_MESSAGE);      
    			return null;
        	} else {
        		throw e;	// other exceptions: rethrow
        	}
        }

		ACTIONS_ST act_best = getBestAction(so, so,  random,  VTable,  silent, 0, null);
		
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
	 * @param prevTuple TODO
	 * @return		best action + score tuple
	 */
	private ACTIONS_ST getBestAction(StateObservation so, StateObservation refer, boolean random, 
			double[] VTable, boolean silent, int depth, ScoreTuple prevTuple) 
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
    	currScoreTuple = (prevTuple==null) ? new ScoreTuple(so) : prevTuple;
        int P = so.getPlayer();
        
        for(i = 0; i < acts.size(); ++i)
        {
        	actions[i] = acts.get(i);
        	NewSO = so.copy();
        	NewSO.advance(actions[i]);
        	
			if (depth<this.m_depth) {
				stringRep = NewSO.stringDescr();
	        	sc = retrieveFromHashMap(m_useHashMap,stringRep);
				if (sc==null) {
					// here is the recursion: getAllScores may call getBestAction back:
					currScoreTuple = getAllScores(NewSO,refer,depth+1, currScoreTuple);	
					
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
				currScoreTuple = estimateGameValueTuple(NewSO, currScoreTuple);
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
        	System.out.println("---Best Move: "+NewSO.stringDescr()+"   "+pMaxScore);
        }			

        VTable[actions.length] = pMaxScore;
        actBest.setRandomSelect(false);
        act_st = new ACTIONS_ST(actBest, scBest);
        return act_st;         
	}

	private ScoreTuple retrieveFromHashMap(boolean m_useHashMap, String stringRep) {
		ScoreTuple sc = null;
    	if (m_useHashMap) {
			// speed up MaxNAgent for repeated calls by storing/retrieving the 
			// scores of visited states in HashMap hm:
			sc = hm.get(stringRep); 		// returns null if not in hm
			//System.out.println(stringRep+":"+sc);
    	} 
    	
    	return sc;
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
		return getAllScores(sob,sob,0, null).scTup[sob.getPlayer()];
	}
	@Override
	public ScoreTuple getScoreTuple(StateObservation sob, ScoreTuple prevTuple) {
		return getAllScores(sob,sob,0, null);
	}
	private ScoreTuple getAllScores(StateObservation sob, StateObservation refer, int depth, ScoreTuple prevTuple) {
        ACTIONS_ST act_st = null;
		if (sob.isGameOver())
		{
			boolean rgs = m_oPar.getRewardIsGameScore();
			return sob.getRewardTuple(rgs);
		}
		
		int n=sob.getNumAvailableActions();
		double[] vtable	= new double[n+1];
		
		if (this instanceof MaxNWrapper) {
			if (((MaxNWrapper)this).getWrappedPlayAgent() instanceof TDNTuple3Agt)
				prevTuple = estimateGameValueTuple(sob, prevTuple);
				// this is for wrappedAgent==TDNTuple3Agt and the case (N>=3): fill in the game value estimate
				// for the player who created sob. Will be used by subsequent states as a surrogate for the 
				// then unknown value for that player. 
		}
		
		// here is the recursion: getBestAction calls getAllScores(...,depth+1):
		act_st = getBestAction(sob, refer, false,  vtable,  true, depth, prevTuple);  
				 // sets vtable[n]=iMaxScore
		
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
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
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