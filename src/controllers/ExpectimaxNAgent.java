package controllers;

import controllers.MCTSExpectimax.MCTSETreeNode;
import controllers.MCTSWrapper.utils.Tuple;
import games.StateObservation;
import games.Arena;
import games.StateObsNondeterministic;
import params.ParMaxN;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_VT;

import java.io.Serial;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * The Expectimax-N agent implements the Expectimax-N algorithm via interface {@link PlayAgent}. 
 * Expectimax-N  is the generalization {@link MaxNAgent} to nondeterministic games. It works on  
 * {@link ScoreTuple}, an N-tuple of game scores (one score for each player 0, 1,..., N-1).
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
	/**
	 * If {@code PARTIAL_IN_RECURSION==true}, form a partial state before starting the recursion in
	 * {@link #getScore(StateObservation)} and {@link #getScoreTuple(StateObservation, ScoreTuple)} (recommended)
	 * <p>
	 * If {@code PARTIAL_IN_RECURSION==false}, do not form partial states before starting the recursion.
	 */
	public static boolean PARTIAL_IN_RECURSION = true;

	/**
	 *
	 */
	enum ViewType {PLAYER, ROOT}

	private final Random rand;
	protected ParMaxN m_mpar;
	protected int m_depth=10;
	protected StateObsNondeterministic root;

	//	protected boolean m_rgs=true;  // use now AgentBase::m_oPar.getRewardIsGameScore()
	private boolean m_useHashMap=true;	// new 2021-10-18
	private final HashMap<String,ScoreTuple> hm;
	protected int countTerminal;		// # of terminal node visits in getNextAction2
	protected int countMaxDepth;		// # of premature returns due to maxDepth in getNextAction2
	private final boolean DBG_EWN = false;
	private final int DBG_EWN_DEPTH = 2;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable, or you have
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
		hm = new HashMap<>();
		setAgentState(AgentState.TRAINED);
	}
	
	public ExpectimaxNAgent(String name, ParMaxN mpar, ParOther opar)
	{
		super(name,opar);
		super.setMaxGameNum(1000);
		super.setGameNum(0);
		rand = new Random(System.currentTimeMillis());
		m_mpar = mpar;
		m_depth = mpar.getMaxNDepth();
		hm = new HashMap<>();
		m_useHashMap = mpar.getMaxNUseHashmap();
		setAgentState(AgentState.TRAINED);		// do again to set oPar's agent state to TRAINED
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
		m_arena.m_xab.setMaxNParFrom(n,this.m_mpar);
		m_arena.m_xab.setOParFrom(n, this.getParOther() );
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

		this.root = soND;

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

			if (MCTSETreeNode.WITH_PARTIAL && soND.isImperfectInformationGame()) {
				currScoreTuple = getEAScoreTuple_partial(
						new DuoStateND(NewSO,NewSO),
						silent,1, ViewType.ROOT).element2();
			} else {
				currScoreTuple = getEAScoreTuple(NewSO, silent,1);
			}

			vTable[i] = value = currScoreTuple.scTup[player];

			if (DBG_EWN) {
				int diceval = soND.getNextNondeterministicAction().toInt();
				DecimalFormat frm = new DecimalFormat("0000");
				System.out.println("[-] d=0"+": p"+player+"d"+diceval+" "
						+frm.format(acts.get(i).toInt())+" V="+currScoreTuple.scTup[player]);
			}

			// retain the ScoreTuple which is *maximum* in P's element,
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

		if (DBG_EWN) {
			if (!silent) {
				DecimalFormat frmAct = new DecimalFormat("0000");
				DecimalFormat frmVal = new DecimalFormat("+0.00;-0.00");
				System.out.println(
						"so.player="+player+" so.diceVal="+soND.getNextNondeterministicAction().toInt()
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
		}

		if (!silent) System.out.println("[ExpectimaxN] HashMap size: "+hm.size());

		ACTIONS_VT actBestVT = new ACTIONS_VT(actBest.toInt(), false, vTable, bestValue, scBest);

        return actBestVT;
	}

	/**
	 * For perfect-information games:
	 * Loop over all actions available for {@code soND} to find the action with the best
	 * score tuple (best score for {@code soND}'s player).
	 *
	 * @param soND		current game state (not changed on return)
	 * @param silent	operate w/o printouts
	 * @param depth		tree depth
	 * @return			score tuple for state {@code soND} as calculated by ExpectimaxNAgent (EA)
	 */
	private ScoreTuple getEAScoreTuple(StateObsNondeterministic soND, boolean silent, int depth) {
		assert soND.isLegalState() : "Not a legal state";
		int player = soND.getPlayer();

		boolean rgs = m_oPar.getRewardIsGameScore();
		boolean stopOnRoundOver= m_mpar.getStopOnRoundOver(); 		// /WK/03/2021: NEW
		boolean stopConditionMet = soND.isGameOver() || (stopOnRoundOver && soND.isRoundOver());
		if (stopConditionMet)
		{
			// this is the 1st place to terminate the recursion:
			countTerminal++;
			return soND.getRewardTuple(rgs);
		}

		int i;
		ScoreTuple currScoreTuple;
		ScoreTuple sc,scBest;
		ScoreTuple expecScoreTuple=new ScoreTuple(soND);	// a 0-ScoreTuple
		ScoreTuple.CombineOP cOpMax = ScoreTuple.CombineOP.MAX;
		StateObsNondeterministic NewSO;
		String stringRep;


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

				if (depth<this.m_depth) {
					stringRep = NewSO.uniqueStringDescr();
					sc = retrieveFromHashMap(m_useHashMap,stringRep);
					if (sc==null) {
						// here is the recursion:
						currScoreTuple = getEAScoreTuple(NewSO, silent,depth+1);
						if (m_useHashMap) {  hm.put(stringRep, currScoreTuple);  }
					} else {
						currScoreTuple = sc;
					}

					// --- version w/o HashMap hm:
//					// here is the recursion:
//					currScoreTuple = getEAScoreTuple(NewSO, silent,depth+1);
				} else {
					// this is the 2nd place to terminate the recursion:
					// (after finishing the for-loop for every element of acts)
					stopConditionMet = NewSO.isGameOver() || (stopOnRoundOver && NewSO.isRoundOver());
					// this check on stopConditionMet is needed when using this method from
					// ExpectimaxNWrapper: If NewSO is a game-over state, we do not need to call the estimator
					// (i.e. wrapped agent), we just take the final reward
					if (stopConditionMet)
					{
						countTerminal++;
						currScoreTuple =  NewSO.getRewardTuple(rgs);
					} else {
						countMaxDepth++;
						// setAvailableActions not needed any more (and leads for EWN to a wrong isNextActionDeterministic==false):
//						NewSO.setAvailableActions();		// The former problematic MC-N, which could not handle an
						// incoming NewSO with next-action-nondeterministic, does now handle it
						currScoreTuple = estimateGameValueTuple(NewSO, null);
					}
				}
				//if (!silent && depth<0) printAfterstate(soND,acts.get(i),currScoreTuple,depth);

				// *maximize* always P's element in the tuple currScoreTuple,
				// where P is the player to move in state soND:
				scBest.combine(currScoreTuple, cOpMax, player, 0.0);

				if (DBG_EWN && depth<=DBG_EWN_DEPTH) {
					int diceval = soND.getNextNondeterministicAction().toInt();
					DecimalFormat frm = new DecimalFormat("0000");
					System.out.print("[MAX]"); for (int z=0;z<depth;z++) System.out.print(" ");
					System.out.println("d="+depth+": p"+player+"d"+diceval+" "
							+frm.format(acts.get(i).toInt())+" V="+currScoreTuple.scTup[player]);
				}
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

				// here is the recursion:
				currScoreTuple = getEAScoreTuple(NewSO, silent,depth);
				// was before called with depth+1, but we now increase depth only on deterministic moves (!)

				currProbab = soND.getProbability(rans.get(i));
				//if (!silent) printNondet(NewSO,currScoreTuple,currProbab,depth);
				sumProbab += currProbab;
				// if cOpND==AVG, expecScoreTuple will contain the average ScoreTuple
				// if cOpND==MIN, expecScoreTuple will contain the worst ScoreTuple for
				// player (this considers the environment as an adversarial player)
				expecScoreTuple.combine(currScoreTuple, cOpND, player, currProbab);

				if (DBG_EWN && depth<=DBG_EWN_DEPTH) {
					System.out.print("[EXP]"); for (int z=0;z<depth;z++) System.out.print("  ");
					System.out.println("d="+depth+": p"+player+" "+rans.get(i).toInt()+" V="+currScoreTuple.scTup[player]);
				}

			}
			assert (Math.abs(sumProbab-1.0)<1e-8) : "Error: sum of probabilities is not 1.0";
			if (!silent && depth<0) printNondet(soND,expecScoreTuple,sumProbab,depth);

			return expecScoreTuple;
		} // else (isNextActionDeterministic)

	}

	/**
	 * For imperfect-information games (games with partial states):
	 * Loop over all actions available for {@code soND} to find the action with the best
	 * score tuple (best score for {@code soND}'s player).
	 *
	 * @param duoState	duo of states [ current game state {@code soND}, state root ] (not changed on return)
	 * @param silent	operate w/o printouts
	 * @param depth		tree depth
	 * @param fView     the view perspective (PLAYER or ROOT) to take when backing up the ScoreTuple
	 * @return			duo of {@link ScoreTuple}s for state {@code soND} as calculated by ExpectimaxNAgent (EA)
	 */
	private DuoScoreTuples getEAScoreTuple_partial(DuoStateND duoState,
											   boolean silent, int depth,
											   ViewType fView) {
		StateObsNondeterministic soND = duoState.element1();
		StateObsNondeterministic soRoot = duoState.element2();

		assert soND.isLegalState() : "Not a legal state";
		int player = soND.getPlayer();

		boolean rgs = m_oPar.getRewardIsGameScore();
		boolean stopOnRoundOver= m_mpar.getStopOnRoundOver(); 		// /WK/03/2021: NEW
		boolean stopConditionMet = soND.isGameOver() || (stopOnRoundOver && soND.isRoundOver());
		if (stopConditionMet)
		{
			// this is the 1st place to terminate the recursion:
			countTerminal++;
			return new DuoScoreTuples(soND.getRewardTuple(rgs),soRoot.getRewardTuple(rgs));
		}

		int i;
        ScoreTuple scBest,scView;
		ScoreTuple expecScoreTuple=new ScoreTuple(soND);	// a 0-ScoreTuple
		DuoScoreTuples duoExpecScTuple = new DuoScoreTuples(expecScoreTuple,expecScoreTuple);
		StateObsNondeterministic NewSO, NewRoot;

		if (MCTSETreeNode.WITH_PARTIAL && soND.isPartialState(soND.getPlayer())) {
			//
			// branch into the possible partial-state completions and average over them
			//
			int p = soND.getPlayer();
			if (p == root.getPlayer() && soND.isNextActionDeterministic()) {
				// if p is the root player, take only the root completion (average = root's value)
				StateObsNondeterministic sRoot = duoState.element2();
				return getEAScoreTuple_partial(new DuoStateND(sRoot,sRoot), silent, depth, fView);
			}
			// else, if p is not the root player, get all possible completions for player p
			// and take the weighted average of their resulting score tuples:
			ArrayList<ACTIONS> rans = soND.getAvailableCompletions(p);
			assert (rans.size()>0) : "Error: getAvailableCompletions returns no actions";
			double currProbab;
			double sumProbab=0.0;
			for(i = 0; i < rans.size(); ++i)
			{
				DuoStateND newDuoState = duoState.duoCompletePartial(soND.getPlayer(),rans.get(i));
				if (soND.isNextActionDeterministic())
					newDuoState = newDuoState.duoPartialState(root.getPlayer());

				// recursive call: newDuoState has the same player p = soND.getPlayer() and it is not partial w.r.t. p,
				// so this isPartialState-branch will *not* be called repeatedly:
				DuoScoreTuples duoScoreTup = getEAScoreTuple_partial(newDuoState, silent, depth, fView);

				currProbab = soND.getProbability(rans.get(i));
				//if (!silent) printNondet(NewSO,currScoreTuple,currProbab,depth);
				sumProbab += currProbab;
				duoExpecScTuple.combine(duoScoreTup,ScoreTuple.CombineOP.AVG,player, currProbab);
			}
			assert (Math.abs(sumProbab-1.0)<1e-8) : "[avg over partial states] Error: sum of probabilities is not 1.0";
//			if (!silent && depth<0) {
//				printNondet(soND,duoExpecScTuple.element1(),sumProbab,depth);
//				printNondet(soND,duoExpecScTuple.element2(),sumProbab,depth);
//			}

			return duoExpecScTuple;
		} // WITH_PARTIAL && isPartialState



		if (soND.isNextActionDeterministic()) {
        	//
        	// find the best next deterministic action for current player in state soND
        	//
            ArrayList<ACTIONS> acts = soND.getAvailableActions();
        	scBest=new ScoreTuple(soND,true);		// make a new ScoreTuple with all values as low as possible
			scView=new ScoreTuple(soND);
            for(i = 0; i < acts.size(); ++i)
            {
//            	NewSO = soND.copy();
//            	NewSO.advanceDeterministic(acts.get(i));

				DuoStateND newDuoState = duoState.duoAdvanceDet(acts.get(i));
				DuoScoreTuples duoScoreTup;
				NewSO = newDuoState.element1();
				NewRoot = newDuoState.element2();
				// player has changed, no partial state!

				/////// debug only:
            	//System.out.print(NewSO);
            	//System.out.println("depth="+depth);
            	
            	if (depth<this.m_depth) {
    				// here is the recursion:
					duoScoreTup = getEAScoreTuple_partial(newDuoState, silent,depth+1, ViewType.PLAYER);
    			} else {
    				// this is the 2nd place to terminate the recursion:
    				// (after finishing the for-loop for every element of acts)
					stopConditionMet = NewSO.isGameOver() || (stopOnRoundOver && NewSO.isRoundOver());
					// this check on stopConditionMet is needed when using this method from
					// ExpectimaxNWrapper: If NewSO is a game-over state, we do not need to call the estimator
					// (i.e. wrapped agent), we just take the final reward
					if (stopConditionMet)
					{
						countTerminal++;
						duoScoreTup = new DuoScoreTuples(NewSO.getRewardTuple(rgs),NewRoot.getRewardTuple(rgs));
					} else {
						countMaxDepth++;
						// setAvailableActions not needed anymore (and leads for EWN to a wrong isNextActionDeterministic==false):
//						NewSO.setAvailableActions();		// The former problematic MC-N, which could not handle an
												// incoming NewSO with next-action-nondeterministic, does now handle it
						duoScoreTup = new DuoScoreTuples(estimateGameValueTuple(NewSO, null),
					  	 								 estimateGameValueTuple(NewRoot, null));
					}
    			}
            	//if (!silent && depth<0) printAfterstate(soND,acts.get(i),currScoreTuple,depth);

    			// always *maximize* P's element in the tuple currScoreTuple, 
    			// where P is the player to move in state soND:
//				currScoreTuple = ;
//    			scBest.combine(currScoreTuple, cOpMax, player, 0.0);
				if (duoScoreTup.element1().scTup[player] > scBest.scTup[player] ) {
					scBest = duoScoreTup.element1().copy();
					scView = (fView!=ViewType.ROOT) ? duoScoreTup.element1() : duoScoreTup.element2();
				}
            } // for

			return new DuoScoreTuples(scView,scView);

        } // if (isNextActionDeterministic)
        else 
        { // i.e. if next action is nondeterministic:
        	//
        	// average (or min) over all next nondeterministic actions 
        	//
            ArrayList<ACTIONS> rans = soND.getAvailableRandoms();
            assert (rans.size()>0) : "Error: getAvailableRandoms returns no actions";
    		// select one of the following two lines:
			ScoreTuple.CombineOP cOpND = ScoreTuple.CombineOP.AVG;
			//ScoreTuple.CombineOP cOpND = ScoreTuple.CombineOP.MIN;
			double currProbab;
			double sumProbab=0.0;
            for(i = 0; i < rans.size(); ++i)
            {
				DuoStateND newDuoState = duoState.duoAdvanceNonDet(rans.get(i));
				if (MCTSETreeNode.WITH_PARTIAL) newDuoState = newDuoState.duoPartialState(root.getPlayer());

				// here is the recursion:
				DuoScoreTuples duoScoreTup = getEAScoreTuple_partial(newDuoState,
						silent, depth, fView);
						// was before called with depth+1, but now we increase depth only on deterministic moves (!)

				currProbab = soND.getProbability(rans.get(i));
            	//if (!silent) printNondet(NewSO,currScoreTuple,currProbab,depth);
				sumProbab += currProbab;
				// if cOpND==AVG, expecScoreTuple will contain the average ScoreTuple
				// if cOpND==MIN, expecScoreTuple will contain the worst ScoreTuple for
				// player (this considers the environment as an adversarial player)
				duoExpecScTuple.combine(duoScoreTup,cOpND, player, currProbab);
            }
			assert (Math.abs(sumProbab-1.0)<1e-8) : "Error: sum of probabilities is not 1.0";
//			if (!silent && depth<0) {
//				printNondet(soND,duoExpecScTuple.element1(),sumProbab,depth);
//				printNondet(soND,duoExpecScTuple.element2(),sumProbab,depth);
//			}

			return duoExpecScTuple;
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
		assert sob instanceof StateObsNondeterministic : "Error, sob must be of class StateObsNondeterministic";
		StateObsNondeterministic soND_p, soND = (StateObsNondeterministic) sob;
		if (!soND.isImperfectInformationGame()) {
			return getEAScoreTuple(soND, true, 0).scTup[sob.getPlayer()];
		} else {
			soND_p = PARTIAL_IN_RECURSION ? soND.partialState() : soND;
			return getEAScoreTuple_partial(new DuoStateND(soND_p,soND_p), true, 0,ViewType.ROOT)
					.element2()
					.scTup[sob.getPlayer()];
		}
	}
	@Override
	public ScoreTuple getScoreTuple(StateObservation sob, ScoreTuple prevTuple) {
		assert sob instanceof StateObsNondeterministic : "Error, sob must be of class StateObsNondeterministic";
		StateObsNondeterministic soND_p, soND = (StateObsNondeterministic) sob;
		if (!soND.isImperfectInformationGame()) {
			return getEAScoreTuple(soND, true, 0);
		} else {
			soND_p = PARTIAL_IN_RECURSION ? soND.partialState() : soND;
			return getEAScoreTuple_partial(new DuoStateND(soND_p,soND_p), true, 0,ViewType.ROOT)
					.element2();
		}
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

    // --- never used ---
//    private void printBestAfterstate(StateObsNondeterministic soND,ACTIONS actBest,
//    		double pMaxScore, int depth)
//    {
//		StateObsNondeterministic NewSO = soND.copy();
//    	NewSO.advanceDeterministic(actBest);
//    	System.out.println("---Best Move: "+NewSO.stringDescr()+"   "+pMaxScore+", depth="+depth);
//    }

    private void printNondet(StateObsNondeterministic NewSO,
    		ScoreTuple scTuple, double currProbab, int depth)
    {
    	System.out.println("---   Random: "+NewSO.stringDescr()+"   "+scTuple.toString()+
    			", p="+currProbab+", depth="+depth);
    }

	/**
	 * For games with partial states: a tuple of two states (the state itself and its 'root sibling')
	 * together with various methods to operate on and return such a tuple.
	 */
	static class DuoStateND {
		private final Tuple<StateObsNondeterministic,StateObsNondeterministic> duoTuple;

		DuoStateND(StateObsNondeterministic s1, StateObsNondeterministic s2) {
			duoTuple = new Tuple<>(s1.copy(),s2.copy());
		}

		DuoStateND(DuoStateND other) {
			StateObsNondeterministic s1 = other.element1().copy();
			StateObsNondeterministic s2 = other.element2().copy();
			duoTuple = new Tuple<>(s1,s2);
		}

		public DuoStateND duoPartialState(int proot) {
			StateObsNondeterministic s1 = this.element1().copy().partialState();
			StateObsNondeterministic s2 = this.element2().copy();
			s2 = (s2.getPlayer()!=proot) ? s2 : s2.partialState();
			return new DuoStateND(s1,s2);
		}

		public DuoStateND duoCompletePartial(int p, Types.ACTIONS ranAct) {
			StateObsNondeterministic s1 = this.element1().copy();
			StateObsNondeterministic s2 = this.element2().copy();
			s1.completePartialState(p,ranAct);
			s2.completePartialState(p,ranAct);
			return new DuoStateND(s1,s2);
		}

		public DuoStateND duoAdvanceDet(Types.ACTIONS act) {
			StateObsNondeterministic s1 = this.element1().copy();
			StateObsNondeterministic s2 = this.element2().copy();
			s1.advanceDeterministic(act);
			s2.advanceDeterministic(act);
			return new DuoStateND(s1,s2);
		}

		public DuoStateND duoAdvanceNonDet(Types.ACTIONS ranAct) {
			StateObsNondeterministic s1 = this.element1().copy();
			StateObsNondeterministic s2 = this.element2().copy();
			s1.advanceNondeterministic(ranAct);
			while(!s1.isNextActionDeterministic() && !s1.isRoundOver()){		// /WK/03/2021 NEW
				s1.advanceNondeterministic();
			}
			s2.advanceNondeterministic(ranAct);
			while(!s2.isNextActionDeterministic() && !s2.isRoundOver()){		// /WK/03/2021 NEW
				s2.advanceNondeterministic();
			}
			return new DuoStateND(s1,s2);
		}

		public StateObsNondeterministic element1() { return duoTuple.element1; }
		public StateObsNondeterministic element2() { return duoTuple.element2; }
	}

	/**
	 * For games with partial states: a tuple of two {@link ScoreTuple}s (from the state itself and its 'root sibling')
	 * together with various methods to operate on and return such a tuple.
	 */
	static class DuoScoreTuples {
		private final Tuple<ScoreTuple,ScoreTuple> duoTuple;

		DuoScoreTuples(ScoreTuple s1, ScoreTuple s2) {
			duoTuple = new Tuple<>(s1.copy(),s2.copy());
		}

		DuoScoreTuples(DuoScoreTuples other) {
			ScoreTuple s1 = other.element1().copy();
			ScoreTuple s2 = other.element2().copy();
			duoTuple = new Tuple<>(s1,s2);
		}

		public DuoScoreTuples combine(DuoScoreTuples duoScoreTup, ScoreTuple.CombineOP cOp, int player, double currProbab) {
			ScoreTuple s1 = duoScoreTup.element1();
			ScoreTuple s2 = duoScoreTup.element2();
			this.element1().combine(s1, cOp, player, currProbab);
			this.element2().combine(s2, cOp, player, currProbab);
			return this;
		}

		public ScoreTuple element1() { return duoTuple.element1; }
		public ScoreTuple element2() { return duoTuple.element2; }
	}

}