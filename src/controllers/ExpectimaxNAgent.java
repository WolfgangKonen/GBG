package controllers;

import games.EWN.StateObserverEWN;
import games.StateObservation;
import games.Arena;
import games.StateObsNondeterministic;
import params.ParMaxN;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_VT;

import java.io.Serial;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

/**
 * The Expectimax-N agent implements the Expectimax-N algorithm via interface {@link PlayAgent}. 
 * Expectimax-N  is the generalization {@link MaxNAgent} to nondeterministic games. It works on  
 * {@link ScoreTuple}, an N-tuple of game scores (one score for each player 0,1,...,N-1).
 * It traverses the game tree up to a prescribed depth (default: 10, see {@link ParMaxN}).
 * <p>
 * {@link ExpectimaxNAgent} is for <b>non-deterministic</b> games. For deterministic games see 
 * {@link MaxNAgent}.
 * 
 * @author Wolfgang Konen, TH Koeln, 2017-2021
 * 
 * @see ScoreTuple
 * @see MaxNAgent
 * @see ExpectimaxNWrapper
 */
public class ExpectimaxNAgent extends AgentBase implements PlayAgent, Serializable
{
	private final Random rand;
	protected ParMaxN m_mpar;
	protected int m_depth=10;

	//	protected boolean m_rgs=true;  // use now AgentBase::m_oPar.getRewardIsGameScore()
	//private boolean m_useHashMap=true;		// don't use HashMap in ExpectimaxNAgent!
	//private HashMap<String,ScoreTuple> hm;
	protected int countTerminal;		// # of terminal node visits in getNextAction2
	protected int countMaxDepth;		// # of premature returns due to maxDepth in getNextAction2
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	@Serial
	private static final long  serialVersionUID = 12L;
		
	public ExpectimaxNAgent(String name)
	{
		super(name);
		super.setMaxGameNum(1000);		
		super.setGameNum(0);
        rand = new Random(System.currentTimeMillis());
        m_mpar = new ParMaxN();
		//hm = new HashMap<String, ScoreTuple>();
		setAgentState(AgentState.TRAINED);
	}
	
	public ExpectimaxNAgent(String name, ParMaxN mpar, ParOther opar)
	{
		this(name);
		m_depth = mpar.getMaxNDepth();
		m_mpar = mpar;
		m_oPar = opar;		// AgentBase::m_oPar
	}
	
	public ExpectimaxNAgent(String name, int nply)
	{
		this(name);
		m_depth = nply;
		m_mpar = new ParMaxN();
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
	 * @param so			current game state (not changed on return), has to implement
	 * 						interface {@link StateObsNondeterministic}
	 * @param random		needed for the interface, but ExpectimaxNAgent does not support random
	 * @param silent		operate w/o printouts
	 * @return actBest		the best action 
	 * @throws RuntimeException if {@code so} is not implementing {@link StateObsNondeterministic} or
	 * if {@code so}'s next action is not deterministic
	 * <p>						
	 * actBest has the members vTable and vBest to store the value for each available
	 * action (as returned by so.getAvailableActions()), the value for the best action actBest,
	 * and the best ScoreTuple scBest.
	 */	
	@Override
	public ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {

		if (!(so instanceof StateObsNondeterministic))
			throw new RuntimeException(" Error in "
					+"ExpectimaxNAgent.getNextAction2(so,...): param so has to implement StateObsNondeterministic");

		StateObsNondeterministic soND = (StateObsNondeterministic) so;
		assert soND.isLegalState() : "Not a legal state";
		if (!soND.isNextActionDeterministic())
			throw new RuntimeException(" Error in "
					+"ExpectimaxNAgent.getNextAction2(so,...): next action has to be deterministic!");

		if (random)
			System.out.println("WARNING: ExpectimaxNAgent does not support random==true");

//		if (!silent) {
//			System.out.println(this.getShortName()+" called for: ");
//			System.out.print(soND);
//		}

		int i;
		double bestValue= -Double.MAX_VALUE;
		double value;			// the quantity to be maximized
		ScoreTuple currScoreTuple;
		ScoreTuple scBest;
		StateObsNondeterministic NewSO;
		ACTIONS actBest;

		int player = soND.getPlayer();

		//
		// find the best next deterministic action for current player in state soND
		//
		ArrayList<ACTIONS> acts = soND.getAvailableActions();
		ArrayList<ACTIONS> bestActions = new ArrayList<>();
		scBest=new ScoreTuple(soND,true);		// make a new ScoreTuple with all values as low as possible
		ScoreTuple.CombineOP cOpMax = ScoreTuple.CombineOP.MAX;
		double[] vTable = new double[acts.size()];
		countMaxDepth = countTerminal = 0;

		for(i = 0; i < acts.size(); ++i)
		{
			NewSO = soND.copy();
			NewSO.advanceDeterministic(acts.get(i));

			currScoreTuple = getEAScoreTuple(NewSO, silent,1);

			vTable[i] = value = currScoreTuple.scTup[player];

			// always *maximize* P's element in the tuple currScoreTuple,
			// where P is the player to move in state soND:
			scBest.combine(currScoreTuple, cOpMax, player, 0.0);

			//
			// Calculate the best value and best action(s).
			// If there are multiple best actions, select afterwards one of them randomly
			// (better exploration)
			//
			if (bestValue < value) {
				bestValue = value;
				bestActions.clear();
				bestActions.add(acts.get(i));
			} else if (bestValue == value) {
				bestActions.add(acts.get(i));
			}

		} // for

		// if several actions have the same best value, select one of them randomly
		actBest = bestActions.get(rand.nextInt(bestActions.size()));
		assert actBest != null : "Oops, no best action actBest";

		if ((so instanceof StateObserverEWN) && !silent) {
			DecimalFormat frmAct = new DecimalFormat("0000");
			DecimalFormat frmVal = new DecimalFormat("+0.00;-0.00");
			System.out.println(
					 "so.diceVal="+soND.getNextNondeterministicAction().toInt()
					+", bestValue["+soND.getPlayer()+"]="+frmVal.format(bestValue)
					+", bestAction="+frmAct.format(actBest.toInt())
					+", countTerminal="+getCountTerminal()
					+", countMaxDepth="+getCountMaxDepth());
			// We better do NOT print NewSO here, but print soND at start of getNextAction2.
			// Because: the dice value of NewSO is not necessarily the dice value of soND in next call
//			System.out.println(this.getShortName()+" afterstate: ");
//			NewSO = soND.copy();
//			NewSO.advanceDeterministic(actBest);
//			NewSO.setAvailableActions();
//			System.out.print(NewSO);
		}

		ACTIONS_VT actBestVT = new ACTIONS_VT(actBest.toInt(), false, vTable, bestValue, scBest);

        return actBestVT;
	}

	/**
	 * Loop over all actions available for {@code soND} to find the action with the best
	 * score tuple (best score for {@code soND}'s player).
	 *
	 * @param soND		current game state (not changed on return)
	 * @param silent	operate w/o printouts
	 * @param depth		tree depth
	 * @return			score tuple for state {@code soND} as calculated by ExpectimaxNAgent (EA)
	 */
	private ScoreTuple getEAScoreTuple(StateObsNondeterministic soND, boolean silent, int depth) {
		boolean rgs = m_oPar.getRewardIsGameScore();
		boolean stopOnRoundOver= m_mpar.getStopOnRoundOver(); 		// /WK/03/2021: NEW
		boolean stopConditionMet = soND.isGameOver() || (stopOnRoundOver && soND.isRoundOver());
		if (stopConditionMet)
		{
			// this is the 1st place to terminate the recursion with a final game state:
			countTerminal++;
			return soND.getRewardTuple(rgs);
		}

		int i;
		ScoreTuple currScoreTuple;
        ScoreTuple scBest;
		ScoreTuple.CombineOP cOpMax = ScoreTuple.CombineOP.MAX;
		StateObsNondeterministic NewSO;

        assert soND.isLegalState() : "Not a legal state"; 

        int player = soND.getPlayer();
        
        if (soND.isNextActionDeterministic()) {
        	//
        	// find the best next deterministic action for current player in state soND
        	//
            ArrayList<ACTIONS> acts = soND.getAvailableActions();
        	scBest=new ScoreTuple(soND,true);		// make a new ScoreTuple with all values as low as possible
            for(i = 0; i < acts.size(); ++i)
            {
            	NewSO = soND.copy();
            	NewSO.advanceDeterministic(acts.get(i));

            	/////// debug only:
            	//System.out.print(NewSO);
            	//System.out.println("depth="+depth);

				currScoreTuple = getEAScoreTuple(NewSO, silent,depth+1);

				// this code before 2021-09-10 had the disadvantage that estimateGameValueTuple would be called
				// with nextActionNondeterministic states (!)
//            	if (depth<this.m_depth) {
//    				// here is the recursion:
//    				currScoreTuple = getEAScoreTuple(NewSO, silent,depth+1);
//    			} else {
//    				// this is the 2nd place to terminate the recursion:
//    				// (after finishing the for-loop for every element of acts)
//					stopConditionMet = NewSO.isGameOver() || (stopOnRoundOver && NewSO.isRoundOver());
//					// this check on stopConditionMet is needed when using this method from
//					// ExpectimaxNWrapper: If NewSO is a game-over state, we do not need to call the estimator
//					// (i.e. wrapped agent), we just take the final reward
//					if (stopConditionMet)
//					{
//						countTerminal++;
//						currScoreTuple =  NewSO.getRewardTuple(rgs);
//					} else {
//						countMaxDepth++;
//						NewSO.setAvailableActions();		// if a wrapped agent is called by estimateGameValueTuple,
//															// it might need the available actions (i.e. MC-N)
//						currScoreTuple = estimateGameValueTuple(NewSO, null);
//					}
//    			}
            	if (!silent && depth<0) printAfterstate(soND,acts.get(i),currScoreTuple,depth);

    			// always *maximize* P's element in the tuple currScoreTuple, 
    			// where P is the player to move in state soND:
    			scBest.combine(currScoreTuple, cOpMax, player, 0.0);
            } // for

			return scBest;

        } // if (isNextActionDeterministic)
        else 
        { // i.e. if next action is nondeterministic:
        	//
        	// average (or min) over all next nondeterministic actions 
        	//
            ArrayList<ACTIONS> rans = soND.getAvailableRandoms();
            assert (rans.size()>0) : "Error: getAvailableRandoms returns no actions";
    		ScoreTuple expecScoreTuple=new ScoreTuple(soND);	// a 0-ScoreTuple
    		// select one of the following two lines:
			ScoreTuple.CombineOP cOpND = ScoreTuple.CombineOP.AVG;
			//ScoreTuple.CombineOP cOpND = ScoreTuple.CombineOP.MIN;
			double currProbab;
			double sumProbab=0.0;
            for(i = 0; i < rans.size(); ++i)
            {
            	NewSO = soND.copy();
            	NewSO.advanceNondeterministic(rans.get(i));
				while(!NewSO.isNextActionDeterministic() && !NewSO.isRoundOver()){		// /WK/03/2021 NEW
					NewSO.advanceNondeterministic();
				}

				if (depth<this.m_depth) {
					// here is the recursion:
					currScoreTuple = getEAScoreTuple(NewSO, silent,depth);
					// was before called with depth+1, but we now increase depth only on deterministic moves (!)
				} else {
					NewSO.setAvailableActions();		// if a wrapped agent is called by estimateGameValueTuple,
														// it might need the available actions (i.e. MC-N)
					stopConditionMet = NewSO.isGameOver() || (stopOnRoundOver && NewSO.isRoundOver());
					if (stopConditionMet)
					{
						// this is the 2nd place to terminate the recursion with a final game state:
						countTerminal++;
						currScoreTuple =  NewSO.getRewardTuple(rgs);
					} else {
						// terminat the recursion with an in-game estimated ScoreTuple
						countMaxDepth++;
						currScoreTuple = estimateGameValueTuple(NewSO, null);
					}
				}

				currProbab = soND.getProbability(rans.get(i));
            	//if (!silent) printNondet(NewSO,currScoreTuple,currProbab,depth);
				sumProbab += currProbab;
				// if cOpND==AVG, expecScoreTuple will contain the average ScoreTuple
				// if cOpND==MIN, expecScoreTuple will contain the worst ScoreTuple for
				// player (this considers the environment as an adversarial player)
				expecScoreTuple.combine(currScoreTuple, cOpND, player, currProbab);
            }
			assert (Math.abs(sumProbab-1.0)<1e-8) : "Error: sum of probabilities is not 1.0";
			if (!silent && depth<0) printNondet(soND,expecScoreTuple,sumProbab,depth);

            return expecScoreTuple;
        } // else (isNextActionDeterministic)

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
		
		return getEAScoreTuple(soND, true,0).scTup[sob.getPlayer()];
	}
	@Override
	public ScoreTuple getScoreTuple(StateObservation sob, ScoreTuple prevTuple) {
		assert sob instanceof StateObsNondeterministic : "Error, sob must be of class StateObservationNondet";
		StateObsNondeterministic soND = (StateObsNondeterministic) sob;
		
		return getEAScoreTuple(soND, true,0);
	}
	
	/**
	 * When the recursion tree has reached its maximal depth m_depth, then return
	 * an estimate of the game score (tuple for all players). This function may be overridden 
	 * in a agent-specific way by classes derived from {@link ExpectimaxNAgent}. For example, 
	 * {@link ExpectimaxNWrapper} will override it to return the score tuple of the wrapped
	 * agent.
	 * <p>
	 * This  stub method just returns {@link StateObservation#getReward(int,boolean)} for all 
	 * players, which might be too simplistic for not-yet finished games, because the current 
	 * reward may not reflect future rewards.
	 * @param sob	the state observation
	 * @return		the estimated score tuple
	 * 
	 * @see ExpectimaxNWrapper
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

	public String getShortName() {
		return "EA";
	}

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

}