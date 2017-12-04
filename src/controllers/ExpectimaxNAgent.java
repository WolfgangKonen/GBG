package controllers;

import controllers.PlayAgent;
import controllers.MinimaxAgent;
import games.StateObservation;
import games.StateObservationNondet;
import params.OtherParams;
import params.TDParams;
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
 * Expectimax-N  is the generalization of Expectiminimax to N players. It works on {@link ScoreTuple}, 
 * an N-tuple of game scores. It traverses the game tree up to a prescribed 
 * depth (default: 10, see {@link OtherParams}).   
 * <p>
 * {@link ExpectimaxNAgent} is for <b>nondeterministic</b> games. For deterministic games see 
 * {@link MaxNAgent}.
 * <p>
 * See [Korf91] 
 * R. E. Korf: <em>Multi-Player Alpha-Beta Pruning</em>, Artificial Intelligence 48, 1991, 99-111.
 * 
 * @author Wolfgang Konen, TH Köln, Nov'17
 * 
 * @see ScoreTuple
 * @see MaxNAgent
 */
public class ExpectimaxNAgent extends AgentBase implements PlayAgent, Serializable
{
	private Random rand;
	private int m_depth=10;
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
	
	public ExpectimaxNAgent(String name, OtherParams opar)
	{
		this(name);
		m_depth = opar.getMinimaxDepth();
		//m_useHashMap = opar.useMinimaxHashmap();
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
		
		if (!(so instanceof StateObservationNondet)) 
			throw new RuntimeException("Error, so must be of class StateObservationNondet");
		StateObservationNondet soND = (StateObservationNondet) so;
		
		ACTIONS_ST act_best = getBestAction(soND, so,  random,  VTable,  silent, 0);
		
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
	private ACTIONS_ST getBestAction(StateObservationNondet soND, StateObservation refer, boolean random, 
			double[] VTable, boolean silent, int depth) 
	{
		int i,j;
		ScoreTuple currScoreTuple=null;
        ScoreTuple sc, scBest=null;
		StateObservationNondet NewSO;
		int count = 1; // counts the moves with same iMaxScore
        ACTIONS actBest = null;
        ACTIONS_ST act_st = null;

        assert soND.isLegalState() : "Not a legal state"; 

        int player = soND.getPlayer();
        
        if (soND.isNextActionDeterministic()) {
        	//
        	// find the best next deterministic action for current player in state soND
        	//
            double iMaxScore = -Double.MAX_VALUE;
            ArrayList<ACTIONS> acts = soND.getAvailableActions();
            ACTIONS[] actions = new ACTIONS[acts.size()];
            for(i = 0; i < acts.size(); ++i)
            {
            	actions[i] = acts.get(i);
            	NewSO = soND.copy();
            	NewSO.advanceDeterministic(actions[i]);
            	
            	if (depth<this.m_depth) {
    				// here is the recursion: getAllScores may call getBestAction back:
    				currScoreTuple = getAllScores(NewSO,refer,depth+1);						
    			} else {
    				// this terminates the recursion:
    				currScoreTuple = estimateGameValueTuple(NewSO);
    			}
    			
    			// always *maximize* csVal, that is P's element in the tuple CurrScoreTuple, 
    			// where P is the player to move in state soND:
    			double csVal = currScoreTuple.scTup[player];
            	VTable[i] = csVal;
            	if (iMaxScore < csVal) {
            		iMaxScore = csVal;
            		scBest = new ScoreTuple(currScoreTuple);
            		count = 1;
            	} else  {
            		if (iMaxScore == csVal) count++;	        
            	}
            } // for
            
            // There might be one or more than one action with iMaxScore. 
            // Break ties by selecting one of them randomly:
        	int selectJ = (int)(rand.nextDouble()*count);
        	for (i=0, j=0; i < actions.length; ++i) {
        		if (VTable[i]==iMaxScore) {
        			if ((j++)==selectJ) actBest = new ACTIONS(actions[i]);
        		}
        	}
            
            VTable[actions.length] = iMaxScore;
            if (!silent) printBestAfterstate(soND,actBest,iMaxScore);

        } // if (isNextActionDeterministic)
        else 
        { // i.e. if next action is nondeterministic:
        	//
        	// average (or min) over all next nondeterministic actions 
        	//
            ArrayList<ACTIONS> acts = soND.getAvailableRandoms();
            assert (acts.size()>0) : "Error: getAvailableRandoms returns no actions";
            ACTIONS[] actions = new ACTIONS[acts.size()];
    		ScoreTuple expecScoreTuple=new ScoreTuple(soND);
			ScoreTuple.CombineOP cOP = ScoreTuple.CombineOP.AVG;
			double currProbab;
			double sumProbab=0.0;
            for(i = 0; i < acts.size(); ++i)
            {
            	actions[i] = acts.get(i);
            	NewSO = soND.copy();
            	NewSO.advanceNondeterministic(actions[i]);
            	
				// here is the recursion: getAllScores may call getBestAction back:
				currScoreTuple = getAllScores(NewSO,refer,depth+1);		
				
				currProbab = soND.getProbability(actions[i]);
				sumProbab += currProbab;
				// if cOP==AVG, expecScoreTuple will contain the average ScoreTuple
				// if cOP==MIN, expecScoreTuple will contain the worst ScoreTuple for 
				// player (consider environment as adversarial player)
				expecScoreTuple.combine(currScoreTuple, cOP, player, currProbab);
            }
            assert (Math.abs(sumProbab-1.0)<1e-8) : "Error: sum of probabilites is not 1.0";
            scBest = expecScoreTuple;	
            actBest = acts.get(0); 		// this is just a dummy 
        } // else (isNextActionDeterministic)

        assert actBest != null : "Oops, no best action actBest";

        actBest.setRandomSelect(false);
        act_st = new ACTIONS_ST(actBest, scBest);
        return act_st;         
	}

	private ScoreTuple getAllScores(StateObservationNondet sob, StateObservation refer, int depth) {
        ACTIONS_ST act_st = null;
		if (sob.isGameOver())
		{
			boolean rewardIsGameScore=true; // TODO!!
			double[] res = new double[sob.getNumPlayers()];
			for (int i=0; i<sob.getNumPlayers(); i++) res[i] = sob.getReward(i, rewardIsGameScore);
			return new ScoreTuple(res); 	
		}
		
		
		int n=sob.getNumAvailableActions();
		double[] vtable	= new double[n+1];
		
		// here is the recursion: getBestAction calls getAllScores(...,depth+1):
		act_st = getBestAction(sob, refer, false,  vtable,  true, depth);  // sets vtable[n]=iMaxScore
		
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
		assert sob instanceof StateObservationNondet : "Error, sob must be of class StateObservationNondet";
		StateObservationNondet soND = (StateObservationNondet) sob;
		
		return getAllScores(soND,sob,0).scTup[sob.getPlayer()];
	}
	@Override
	public ScoreTuple getScoreTuple(StateObservation sob) {
		assert sob instanceof StateObservationNondet : "Error, sob must be of class StateObservationNondet";
		StateObservationNondet soND = (StateObservationNondet) sob;
		
		return getAllScores(soND,sob,0);
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
		return sob.getReward(true);
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
		boolean rewardIsGameScore=true; // TODO!!
		ScoreTuple sc = new ScoreTuple(sob);
		for (int i=0; i<sob.getNumPlayers(); i++) 
			sc.scTup[i] = sob.getReward(i, rewardIsGameScore);
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

	
    private void printBestAfterstate(StateObservationNondet soND,ACTIONS actBest,double iMaxScore)
    {
		StateObservationNondet NewSO = soND.copy();
    	NewSO.advanceDeterministic(actBest);
    	System.out.print("---Best Move: "+NewSO.stringDescr()+"   "+iMaxScore);
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