package controllers.TD.ntuple4;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import agentIO.LoadSaveGBG;
import controllers.ReplayBuffer.Buffer.BaseBuffer;
import controllers.ReplayBuffer.ConfigReplayBuffer;
import controllers.ReplayBuffer.Transition.ITransition;
import controllers.TD.ntuple2.NTuple2;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.BlackJack.BasicStrategyBlackJackAgent;
import games.BlackJack.StateObserverBlackJack;
import params.ParNT;
import params.ParOther;
import params.ParRB;
import params.ParTD;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS_VT;
import controllers.AgentBase;
import controllers.ExpectimaxNWrapper;
import controllers.MaxNAgent;
import controllers.MaxN2Wrapper;
import controllers.PlayAgent;
import games.GameBoard;
import games.StateObservation;
import games.StateObsWithBoardVector;
import games.XNTupleFuncs;
import games.Sim.StateObserverSim;

/**
 * TD-Learning {@link PlayAgent} (Temporal Difference reinforcement learning) <b>with n-tuples</b>.
 * <p>
 * It has a one-layer (perceptron-like) neural network with or without output-nonlinearity  
 * {@code tanh} to model the Q-function. 
 * The net follows closely the (pseudo-)code by [SuttonBarto98]. 
 * <p>
 * Some functionality is packed in the superclasses 
 * {@link AgentBase} (gameNum, maxGameNum, AgentState, ...) and
 * {@link NTuple4Base} (finishUpdateWeights, increment*Counters, isTrainable, normalize2, ...)
 * <p>
 * {@link TDNTuple4Agt} is very similar to {@link TDNTuple3Agt}. The only difference is that {@link TDNTuple4Agt}
 * uses {@link NTuple4} n-tuples while {@link TDNTuple3Agt} uses {@link NTuple2}. <br>
 * This results in less memory consumption.
 *
 * @see PlayAgent
 * @see AgentBase
 * @see NTuple4Base
 *
 * @author Wolfgang Konen, TH Koeln, 2020
 */
public class TDNTuple4Agt extends NTuple4Base implements PlayAgent, NTuple4Agt,Serializable {

	private NTuple4Agt.EligType m_elig;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable, or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 13L;

	private int numPlayers;
	/**
	 * sLast[curPlayer] stores the last afterstate that curPlayer generated in his previous move
	 * (initially null)
	 */
	transient private StateObservation[] sLast;	// last state of player p
	transient private boolean[] randLast;		// whether last action of player p was a random action
	transient private ScoreTuple rLast;

	private final boolean RANDINITWEIGHTS = false;// If true, initialize weights of value function randomly

	private final boolean m_DEBG = false; //false;true;
	// debug printout in collectReward:
	public static boolean DBG_REWARD=false;
	public static boolean DBG_BJ=false;		// debugging for Blackjack

	// variable TERNARY is normally false. If true, use ternary target in update rule:
	private boolean TERNARY=false;		// If true, it remains true only for final-reward-games (see getNextAction2)
	// variable FINALADAPTAGENTS is normally true (use finalAdaptAgents). Set only to false if you want to test how agents behave otherwise:
	private final boolean FINALADAPTAGENTS=true; //false;true;
	// variable FINALADAPT_PART2 is normally true. Set only to false if you want to test how agents behave otherwise.
	// If false, do not run through part 'adapt value(s_final) towards 0'. Only relevant, if FINALADAPTAGENTS==true.
	private final boolean FINALADAPT_PART2=true; //false;

	private int acount=0;	// just for debug: counter to stop debugger after every X adaptation steps

	private transient BaseBuffer replayBuffer = null;

	/**
	 * Default constructor for {@link TDNTuple4Agt}, needed for loading a serialized version
	 */
	public TDNTuple4Agt() {
		super();
		ParTD tdPar = new ParTD();
		ParNT ntPar = new ParNT();
		ParOther oPar = new ParOther();
		ParRB rbPar = new ParRB();
		initNet(ntPar, tdPar, oPar, null, null, 1000);
	}

	/**
	 * Create a new {@link TDNTuple4Agt}
	 *
	 * @param name			agent name
	 * @param tdPar			temporal difference parameters
	 * @param ntPar			n-tuples and temporal coherence parameter
	 * @param rbPar         replay buffer parameter
	 * @param nTuples		the set of n-tuples
	 * @param xnf			contains game-specific n-tuple functions
	 * @param maxGameNum	maximum number of training games
	 */
	public TDNTuple4Agt(String name, ParTD tdPar, ParNT ntPar, ParOther oPar, ParRB rbPar,
						int[][] nTuples, XNTupleFuncs xnf, int maxGameNum) {
		super(name,oPar,rbPar);
		this.numPlayers = xnf.getNumPlayers();
		this.sLast = new StateObservation[numPlayers];
		this.randLast = new boolean[numPlayers];
		initNet(ntPar,tdPar,oPar, nTuples, xnf, maxGameNum);
		if (rbPar.getUseRB()){
			replayBuffer = new BaseBuffer(rbPar);
			ConfigReplayBuffer.USE_REPLAYBUFFER = true;
		} else {
			replayBuffer = null;
			ConfigReplayBuffer.USE_REPLAYBUFFER = false;

		}
	}

	/**
	 *
	 * @param tdPar			temporal difference parameters
	 * @param ntPar			n-tuples and temporal coherence parameter
	 * @param nTuples		the set of n-tuples
	 * @param xnf			contains game-specific n-tuple functions
	 * @param maxGameNum	maximum number of training games
	 */
	private void initNet(ParNT ntPar, ParTD tdPar, ParOther oPar,
						 int[][] nTuples, XNTupleFuncs xnf, int maxGameNum)  {
		m_tdPar = new ParTD(tdPar);
		m_ntPar = new ParNT(ntPar);
		m_oPar = new ParOther(oPar);		// m_oPar is in AgentBase
		m_elig = (m_tdPar.getEligMode()==0) ? EligType.STANDARD : EligType.RESET;
		rand = new Random(System.currentTimeMillis()); //(System.currentTimeMillis()); (42); 

		int[] posVals = xnf.getPositionValuesVector();
		int numCells = xnf.getNumCells();

		m_Net = new NTuple4ValueFunc(this,nTuples, xnf, posVals,
				RANDINITWEIGHTS,ntPar,numCells,1);

		setNTParams(ntPar);
		setTDParams(tdPar, maxGameNum);
		setMaxGameNum(maxGameNum);
		m_Net.setHorizon();

		setAgentState(AgentState.INIT);
	}

	/**
	 * If agents need a special treatment after being loaded from disk (e.g. instantiation
	 * of transient members), put the relevant code in here.
	 *
	 * @see LoadSaveGBG#transformObjectToPlayAgent
	 */
	public boolean instantiateAfterLoading() {
		super.instantiateAfterLoading();
		this.m_Net.xnf.instantiateAfterLoading();
		for (int i=0; i<m_Net.xnf.getPositionValuesVector().length; i++)
			assert (m_Net.getNTuples()[0].getPosVals(i)==m_Net.xnf.getPositionValuesVector()[i]) : "Error getPosVals("+i+")";
		assert (this.getParTD().getHorizonCut()!=0.0) : "Error: horizonCut==0";

		// set certain elements in td.m_Net (withSigmoid, useSymmetry) from tdPar and ntPar
		// (they would stay otherwise at their default values, would not 
		// get the loaded values)
		this.setTDParams(this.getParTD(), this.getMaxGameNum());
		this.setNTParams(this.getParNT());
		this.weightAnalysis(null);

		// initialize transient members (in case a further training should take place --> see ValidateAgentTest) 
		this.sLast = new StateObservation[numPlayers];
		this.randLast = new boolean[numPlayers];
		this.m_Net.instantiateAfterLoading();   // instantiate transient eList and nTuples

		return true;
	}

	/**
	 * Get the best next action and return it 
	 *
	 * @param so			current game state (is returned unchanged)
	 * @param random		allow random action selection with probability m_epsilon
	 * @param silent		no output
	 * @return actBest,		the best action. If several actions have the same
	 * 						score, break ties by selecting one of them at random. 
	 * <p>						
	 * actBest has the predicate isRandomAction()  (true: if action was selected
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable and vBest to store the Q-value for each available
	 * action (as returned by so.getAvailableActions()) and the Q-value for the best action actBest, resp.
	 */
	@Override
	public Types.ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
		int i;
		double bestValue;
		double value;			// the quantity to be maximized
		double otilde, rtilde;
		boolean rgs = this.getParOther().getRewardIsGameScore();
		if (!so.isFinalRewardGame()) this.TERNARY=false;		// we use TD target r + gamma*V
		StateObservation NewSO;
		Types.ACTIONS thisAct;
		Types.ACTIONS actBest;
		Types.ACTIONS_VT actBestVT;
		bestValue = -Double.MAX_VALUE;
		double[] VTable;

		otilde = so.getRewardTuple(rgs).scTup[so.getPlayer()];

		// just debug:
//        if (so.isGameOver()) {
//        	System.out.println("Game over: "+so.getRewardTuple(true));
//        }

		boolean randomSelect = false;		// true signals: the next action is a randomly selected one
		if (random) {
			randomSelect = (rand.nextDouble() < m_epsilon);
		}

		ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
		List<Types.ACTIONS> bestActions = new ArrayList<>();

		VTable = new double[acts.size()];

		if (DBG_BJ) {
			System.out.println("so "+ so.stringDescr());
			System.out.println("so reward:" + so.getRewardTuple(rgs));
		}

		assert acts.size()>0 : "Oops, no available action";
		for(i = 0; i < acts.size(); ++i)
		{
			thisAct = acts.get(i);

			// this is just relevant for game RubiksCube: If an action is the inverse of the last action, it would
			// lead to the previous state again, resulting in a cycle of 2. We avoid such cycles and continue with
			// next pass through for-loop --> beneficial when searching for the solved cube in play & train.
			// If you do not want to skip any action - e.g. when inspecting states - then enter this method with
			// a 'cleared' state {@link StateObserverCubeCleared} {@code so} (lastAction==9).
			//
			// For all other games, usually no return to the previous state is possible. For those games
			// isEqualToInverseOfLastAction returns always false.
			if (thisAct.isEqualToInverseOfLastAction(so))
				continue;	// with next for-pass

			NewSO = so.copy();

			if (randomSelect) {
				value = rand.nextDouble();
			} else {
				if (this.getAFTERSTATE()) {
					// if parameter "AFTERSTATE" is checked in ParNT, i.e. we use afterstate logic:
					//
					NewSO.advanceDeterministic(thisAct); 	// generate the afterstate
					value = this.getScore(NewSO,so); // this is V(s') from so-perspective
					while (!NewSO.isNextActionDeterministic() && !NewSO.isRoundOver()) {	// /WK/ NEW/03/2021
						NewSO.advanceNondeterministic();
					}
				} else {
					// the non-afterstate logic for the case of single moves:
					//System.out.println("NewSO: "+NewSO.stringDescr()+", act: "+act.toInt()); // DEBUG
					NewSO.advance(acts.get(i));
					value = this.getScore(NewSO,so); // this is V(s'') from the perspective of so
				}
				// both ways of calculating the agent score are the same for deterministic games (s'=s''),
				// but they usually differ for nondeterministic games.

				rtilde  = (NewSO.getRewardTuple(rgs).scTup[so.getPlayer()]-otilde)
						+ so.getStepRewardTuple().scTup[so.getPlayer()];
				if (TERNARY) {
					value = NewSO.isGameOver() ? rtilde : getGamma()*value;
				} else {
					value = rtilde + getGamma()*value;
				}

				if (DBG_BJ) {
					DecimalFormat form = new DecimalFormat("#0.000");
					System.out.println("NewSO "+i+" "+StateObserverBlackJack.BlackJackActionDet.values()[thisAct.toInt()].name() + ", "
							+ NewSO.stringDescr());
					System.out.println("NewSO reward:" + NewSO.getRewardTuple(rgs)
							+ ", deltaR: "+form.format(rtilde)+", value: "+ this.getScore(NewSO,so)
							+ ", gPhase="+((StateObserverBlackJack)NewSO).getgPhase()
							+ ", board vec=" + m_Net.getXnf().getBoardVector(NewSO));
				}

			}

			// just a debug check:
			if (Double.isInfinite(value)) System.out.println("value(NewSO) is infinite!");

			value = normalize2(value,so);
			VTable[i] = value;

			//
			// Calculate the best value and actBest.
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
		actBest = bestActions.get(rand.nextInt(bestActions.size()));
		// if several actions have the same best value, select one of them randomly

		assert actBest != null : "Oops, no best action actBest";

		if (DBG_BJ) {
			BasicStrategyBlackJackAgent bsbja = new BasicStrategyBlackJackAgent();
			Types.ACTIONS actBsbja = bsbja.getNextAction2(so,false,true);
			System.out.print("getNextAction2:  "+StateObserverBlackJack.BlackJackActionDet.values()[actBest.toInt()].name() );
			System.out.println("  [BSBJA: "+ StateObserverBlackJack.BlackJackActionDet.values()[actBsbja.toInt()].name()+" ]");
		}

		NewSO = so.copy();
		NewSO.advance(actBest);
		if (!silent) {
			printDebugInfo(so,NewSO,bestValue,VTable);
		}

		// determine the ScoreTuple scBest (needed when we wrap this agent with MCTS(Exp)Wrapper):
		ScoreTuple scBest = new ScoreTuple(so,bestValue);
//		double[] res = {bestValue};  				// old (before 2020-09-09) and wrong - the reason it was undetected was
//		ScoreTuple scBest = new ScoreTuple(res);	// only that scBest was never really used before MaxN2Wrapper change 2020-09-09
		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), randomSelect, VTable, bestValue, scBest);
		return actBestVT;
	}

	// helper for getNextAction2
	private void printDebugInfo(StateObservation so, StateObservation NewSO, double bestValue, double[] VTable){
		System.out.println("---Best Move: " + NewSO.stringDescr() + ", " + (bestValue));

		boolean DBG_SIM = false;        // set to true only for K6 (!!)
		if (DBG_SIM && so instanceof StateObserverSim) {
			String[] pl = new String[so.getNumPlayers()];
			if (so.getNumPlayers() > 2) {
				pl[0] = "(P0)";
				pl[1] = "(P1)";
				pl[2] = "(P2)";
			} else {
				pl[0] = "(X)";
				pl[1] = "(O)";
			}
			MaxNAgent maxNAgent = new MaxNAgent("Max-N", 15, true);
			// if there are 12 moves to go, MaxNAgent(...,12,false) will take 1-2 minutes,
			// but MaxNAgent(...,12,true) only 2 seconds. If there are 15 moves to go, then
			// MaxNAgent(...,15,true) will still take only a few seconds.
			// So: activating the hash map is important for MaxN-speed!
			ACTIONS_VT actMax = maxNAgent.getNextAction2(so.partialState(), false, true);
			double[] VTableMax = actMax.getVTable();
			System.out.print("MaxN : ");
			for (int i = 0; i < VTableMax.length - 1; i++)
				System.out.printf("% .4f; ", VTableMax[i]);
			// "% .4f" means: a space before positive numbers, so that negative numbers
			// (with an additional minus sign) are printed aligned
			System.out.println(pl[so.getPlayer()]);
			System.out.print("TDNT3: ");
			for (double v : VTable) System.out.printf("% .4f; ", v);
			System.out.println(pl[so.getPlayer()]);
		}
	}


//	/**
//	 * Return the agent's estimate of the score for that afterstate {@code so}.
//	 * For 2-player games like TTT, the score is V(), the probability that the player to move
//	 * wins from that after state. V(s_t|p_t) learns this probability for every t.
//	 * V(s_t|p_t) is the quantity to be maximized by getNextAction2.
//	 * For 1-player games like 2048 it is the estimated (total or future) reward.
//	 * <p>
//	 * NOTE: For {@link TDNTuple4Agt} and N &gt; 1, this method should be never called, since the score
//	 * of {@code so} from perspective of player {@code so.getPlayer()} is never trained. What is trained
//	 * is the score of {@code so} from perspective of the player preceding {@code so.getPlayer()}, see
//	 * {@link #getScore(StateObservation, StateObservation)}.
//	 * <p>
//	 * This method is only implemented here to satisfy {@link PlayAgent}'s interface. If called,
//	 * it throws an exception.
//	 *
//	 * @param so		   the (after-)state for which the value is desired
//	 * @return V(s_t|p_t), the agent's estimate of the future score for that after state (its value)
//	 */
//	@Deprecated
//	public double getScore(StateObservation so) {
//		throw new RuntimeException("getScore(so) is not valid for TDNTuple4Agt --> use getScore(so,PreSO) instead");
////		int[] bvec = m_Net.xnf.getBoardVector(so);
////		double score = m_Net.getScoreI(bvec,so.getPlayer());
////		return score;
//	}

	/**
	 * Return the agent's estimate of the score for that afterstate {@code so}. 
	 * Return V(s_t|p_refer), that is the value function from the perspective of the player
	 * who moves in state {@code refer}. 
	 * For 1-player games like 2048 it is the estimated (total or future) reward.
	 * <p>
	 * NOTE: For {@link TDNTuple4Agt} and N &gt; 1, this method should be only called in the form
	 * {@code getScore(NextSO,so)}. This is actually the case, 
	 * see {@link #getNextAction2(StateObservation, boolean, boolean)}.
	 *
	 * @param so	the (after-)state s_t for which the value is desired
	 * @param refer	the referring state
	 * @return		V(s_t|p_refer), the agent's estimate of the future score for s_t
	 * 				from the perspective of the player in state {@code refer}
	 */
	public double getScore(StateObservation so, StateObservation refer) {
		double score;
		//if (so instanceof StateObserverCube)
		if (so.isGameOver())
			return 0.0;		// This is very needed for RubiksCube (no expected future rewards for game-over states).
		// We assume from now on (09/2020) that it is at least not harmful for all other games.
		StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(so,m_Net.xnf);
		score = m_Net.getScoreI(curSOWB,refer.getPlayer());
		return score;
	}

	/**
	 * Return the agent's estimate of {@code sob}'s final game value (final reward) <b>for all players</b>. 
	 * Is called by the n-ply wrappers ({@link MaxN2Wrapper}, {@link ExpectimaxNWrapper}).
	 * @param so			the state s_t for which the value is desired
	 * @param prevTuple		for N &ge; 3 player, we only know the game value for the player who <b>created</b>
	 * 						{@code sob}. To provide also values for other players, {@code prevTuple} allows
	 * 						passing in such other players' value from previous states, which may serve
	 * 						as surrogate for the unknown values in {@code sob}. {@code prevTuple} may be {@code null}. 
	 *
	 * @return		an N-tuple with elements V(s_t|i), i=0,...,N-1, the agent's estimate of 
	 * 				the future score for s_t from the perspective of player i
	 */
	@Override
	public ScoreTuple getScoreTuple(StateObservation so, ScoreTuple prevTuple) {
//		// for this to work, actBest=getNextAction2(so,...); so.storeBestActionInfo(actBest);
//		// has to be called beforehand
//		// --> dangerous design!
//		// --> should we do these calls in here, to be on the safe side??
//		return so.getStoredBestScoreTuple();	// WARNING: might return an all-zeros ScoreTuple

		return _getScoreTuple(so,prevTuple);
	}

	private ScoreTuple _getScoreTuple(StateObservation so, ScoreTuple prevTuple) {

		// --- the old version before 2021-09-10 ---
		ScoreTuple sc = new ScoreTuple(so);
		StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(so,m_Net.xnf);
		switch (so.getNumPlayers()) {
			case 1 -> sc.scTup[0] = m_Net.getScoreI(curSOWB,so.getPlayer());
			case 2 -> {
				int player = so.getPlayer();
				int opponent = (player == 0) ? 1 : 0;
				//			sc.scTup[player] = m_Net.getScoreI(bvec,player);	// wrong before 2019-03-10
				//			sc.scTup[opponent] = -sc.scTup[player];
				//
				// This is an important bug fix (2019-03-10) for TDNTuple4Agt:
				// If we want to get the score tuple for state 'so' where
				// 'player' has to move, we may *NOT* ask for m_Net.getScoreI(bvec,player),
				// because the net did never learn this, it was trained on getScore(so,refer), where
				// refer is the player who *created* 'so' (the opponent). We construct the score
				// tuple by starting with m_Net.getScoreI(bvec,opponent), the value that bvec has
				// for opponent, and infer from this the player's value by negation:
				//
				sc.scTup[opponent] = m_Net.getScoreI(curSOWB, opponent);
				sc.scTup[player] = -sc.scTup[opponent];
			}
			default -> {
				//
				// the new logic in the case of 3-,4-,...,N-player games: starting from a previous ScoreTuple
				// prevTuple, fill in the game value for the player for which TDNTuple4Agt has learned the value:
				// This is the player who *created* so. (We do not know the game values from the perspective
				// of the other players, therefore we re-use the estimates from earlier states in prevTuple.)
				//
				if (prevTuple != null) sc = new ScoreTuple(prevTuple);
				int cp = so.getCreatingPlayer();
				if (cp != -1) {
					sc.scTup[cp] = m_Net.getScoreI(curSOWB, cp);
				}
			}
		}

		return sc;
	}

	/**
	 * Return the agent's estimate of {@code sob}'s final game value (final reward) <b>for all players</b>. <br>
	 * Is called by the n-ply wrappers ({@link MaxN2Wrapper}, {@link ExpectimaxNWrapper}).
	 * Is called when training an agent in multi-update mode AND the maximum episode length
	 * is reached. 
	 *
	 * @param sob			the current game state
	 * @param prevTuple		for N &ge; 3 player, we only know the game value for the player who <b>created</b>
	 * 						{@code sob}. To provide also values for other players, {@code prevTuple} allows
	 * 						passing in such other players' value from previous states, which may serve
	 * 						as surrogate for the unknown values in {@code sob}. {@code prevTuple} may be {@code null}.  
	 * @return				the agent's estimate of the final game value (score-so-far plus score-to-come)
	 * 						<b>for all players</b>. The return value is a tuple containing
	 * 						{@link StateObservation#getNumPlayers()} {@code double}'s.
	 */
	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
		// first call getScoreTuple(sob,...): the reward-to-come, as estimated by this agent
		ScoreTuple sc = getScoreTuple(sob, prevTuple);
		boolean rgs = m_oPar.getRewardIsGameScore();
		// then add sob.getRewardTuple(rgs): the reward obtained so far, since the net predicts
		// with getScoreI only the expected future reward.
		sc.combine(sob.getRewardTuple(rgs), ScoreTuple.CombineOP.SUM,0,0);
		sc.combine(sob.getStepRewardTuple(), ScoreTuple.CombineOP.SUM,0,0);

		// old version (2019), not recommended:
//		boolean rgs = m_oPar.getRewardIsGameScore();
//		ScoreTuple sc = new ScoreTuple(sob);
//		for (int i=0; i<sob.getNumPlayers(); i++) 
//			sc.scTup[i] = sob.getReward(i, rgs);
//			// this is valid, but it may be a bad estimate in games where the reward is only 
//			// meaningful for game-over-states.

		return sc;
	}

	private void assertStateForSim(StateObservation s_next, StateObservation s_last) {
		if (s_next instanceof StateObserverSim) {
			// just some assertion code for adaptAgentV in the case of game Sim to check that 
			// numPlayer moves have happened between s_last=sLast[curPlayer] and s_next
			String strNext=s_next.stringDescr();
			String strLast=s_last.stringDescr();
			int movesLeftNext=0,movesLeftLast=0;
			for (int i=0; i<strNext.length(); i++) {
				if (strNext.charAt(i)=='_') movesLeftNext++;
				if (strLast.charAt(i)=='_') movesLeftLast++;
			}
			assert(movesLeftLast-movesLeftNext==s_next.getNumPlayers());
		}
	}


	/**
	 * Adapt the n-tuple weights for state {@code sLast[curPlayer]}, the last afterstate that current
	 * player generated, towards the target derived from {@code s_next}.
	 *
	 * @param curPlayer		the player to move in state {@code ns.getSO()}
	 * @param R				the (cumulative) reward tuple received when moving into {@code s_next}
	 * @param ns			the {@code NextState} object holds the afterstate {@code ns.getAfterState()}
	 * 						and the next state {@code s_next=ns.getNextSO()} which are observed when the 
	 * 						selected action is applied to {@code ns.getSO()}
	 */
	private void adaptAgentV(int curPlayer, ScoreTuple R, NextState4 ns) {
		StateObservation s_after = ns.getAfterState();
		StateObservation s_next = ns.getNextSO();
//		int[] curBoard;
		double v_next,vLast,vLastNew,target;
		boolean learnFromRM = m_oPar.getLearnFromRM();

		// calculate v_next which is the estimated game value for the afterstate generated by
		// curPlayer from state ns.getSO(), seen from the perspective of curPlayer
		if (s_next.isGameOver()) {
			v_next = 0.0;
		} else {
			StateObsWithBoardVector nextSOWB = new StateObsWithBoardVector(s_after,m_Net.xnf);
			v_next = m_Net.getScoreI(nextSOWB,curPlayer);
		}
		// SLast is not used yet
		if (sLast[curPlayer]!=null) {
			// delta reward from curPlayer's perspective when moving into s_next
			double r_next;
			r_next  = (R.scTup[curPlayer] - rLast.scTup[curPlayer])
					+ s_next.getStepRewardTuple().scTup[curPlayer];
			if (TERNARY) {
				target = s_next.isGameOver() ? r_next : getGamma()*v_next;
			} else {
				target = r_next + getGamma()*v_next;
			}

//			if (target==-1.0) {
//				int dummy=0;
//			}
//			if (Math.abs(v_next)>0.7) {
//				int dummy=0;
//			}

			// note that curBoard is NOT the board vector of state ns.getSO(), but of state
			// sLast[curPlayer] (one round earlier!)
			StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(sLast[curPlayer], m_Net.xnf);
//			curBoard = curSOWB.getBoardVector().bvec;
			vLast = m_Net.getScoreI(curSOWB,curPlayer);

//    		if (randLast[curPlayer] && !learnFromRM && !s_next.isGameOver()) {
			// the above line was the statement before 2019-09-02. But it is wrong (at
			// least for the game Sim, and probably also other games) to learn on s_next.isGameOver():
			// Because in Sim a game-over situation means that curPlayer has lost --> it is not
			// correct to learn on such a random move, because another move might be a winning or
			// tie move --> sLast[curPlayer] would learn the wrong (negative) target!
			// That is why we learn now *never* on a random move (except if learnFromRM is true):
			if (randLast[curPlayer] && !learnFromRM) {
				// if last action (of curPlayer) leading to s_next was a random move:
				// no training, go to next move.
				if (m_DEBG)
					System.out.println("random move --> no train");

				m_Net.clearEligList(m_elig);	// the list is only cleared if m_elig==RESET

			} else {

				m_Net.updateWeightsTD(curSOWB, curPlayer, vLast, target,r_next,ns.getSO());
			}

			//debug only:
			if (m_DEBG) {
				if (s_next.isGameOver()) {
					//vLastNew = m_Net.getScoreI(curSOWB,curPlayer);
					int dummy=1;
				}
				assertStateForSim(s_next, sLast[curPlayer]);	// this is only for debugging Sim
//            	ScoreTuple sc1 = s_next.getGameScoreTuple();
//           	ScoreTuple sc2 = this.getScoreTuple(s_next, null);
				String s1 = sLast[curPlayer].stringDescr();
				String s2 = s_next.stringDescr();
				if (target!=0.0) {//(target==-1.0) { //(s_next.stringDescr()=="XoXX-oXo-") {
					vLastNew = m_Net.getScoreI(curSOWB,curPlayer);
					System.out.println(s1+" "+s2+", "
							+String.format("%.4f",vLast)+" -> "+String.format("%.4f",vLastNew)
							+" target="+String.format("%.4f",target)
							+" player="+(curPlayer==0 ? "X" : "O"));
					if (++acount % 50 ==0) {
						int dummy=1;
					}
				}
			}

		}  // if(sLast[..]!=null)

		// --- not needed here, see else-branch in finalAdaptAgents, where we do the same ---
//		if (s_next.isGameOver()) {
//			// if s_next is terminal, adapt towards target 0 the afterstate that precedes s_next 
//			curBoard = m_Net.xnf.getBoardVector(s_after); 
//        	vLast = m_Net.getScoreI(curBoard,curPlayer);
//        	target = 0.0;
//			m_Net.updateWeightsTD(curBoard, curPlayer, vLast, target,R.scTup[curPlayer],ns.getSO());
//		}
	}

	/**
	 * This function is called when {@code ns.getNextSO()} is terminal. It does two things:
	 * <ol>
	 * <li> It takes for each player <b>other than</b> {@code curPlayer} the final delta reward r (from the perspective of 
	 * 		this other player) and adapts the value of the last state of this other player towards r.<br>
	 * 		[This first part is irrelevant for 1-player games where we have no other players.]
	 * <li> For {@code curPlayer} <b>only</b>: Adapt the value of {@code ns.getAfterState()}, the afterstate preceding 
	 * 		the terminal state, towards 0. <br>
	 * 		[This second part is only relevant if TERNARY==false in source code.]
	 * </ol>
	 *
	 * @param curPlayer the player to move in @{@code ns.getSO()} = the player who generates {@code ns.getNextSO()}
	 * @param R         the reward tuple for {@code ns.getNextSO()}
	 * @param ns		the next state object when applying action {@code a_t} in state {@code s_t}
	 */
	private void finalAdaptAgents(int curPlayer, ScoreTuple R, NextState4 ns) {
		double target,vLast,vLastNew;
//		int[] curBoard, nextBoard;
		StateObservation s_after = ns.getAfterState();
		StateObservation s_next = ns.getNextSO();

		for (int n=0; n<numPlayers; n++) {
			if (n!=curPlayer) {
				// This 1st part of finalAdaptAgents is only relevant for games with more than one player:
				//
				// adapt the value of the last state sLast[n] of each player other than curPlayer
				// towards the reward received when curPlayer did his terminal move.
				//
				if (sLast[n]!=null ) {
					target  = (R.scTup[n] - rLast.scTup[n]) 		// delta reward
							+ s_next.getStepRewardTuple().scTup[n];
					StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(sLast[n], m_Net.xnf);
					vLast = m_Net.getScoreI(curSOWB,n);

					m_Net.updateWeightsTD(curSOWB, n, vLast, target, R.scTup[n], ns.getSO());

					//debug only:
					if (m_DEBG) {
						assert s_next.isLegalState() : "s_next is not legal";
//	    	    		if (s_next.isGameOver()) {
//	    	            	vLastNew = m_Net.getScoreI(curSOWB,n);
//	    	            	int dummy=1;
//	    	    		}
						String s1 = sLast[n].stringDescr();
						String s2 = s_next.stringDescr();
						if (target!=0.0) { //(target==-1.0) { //(s_next.stringDescr()=="XoXX-oXo-") {
							vLastNew = m_Net.getScoreI(curSOWB,n);
							System.out.println(s1+" "+s2+", "
									+String.format("%.4f",vLast)+" -> "+String.format("%.4f",vLastNew)
									+" target="+String.format("%.4f",target)
									+" player="+(n==0 ? "X" : "O")+" (f)"+this.getGameNum());
							if (++acount % 50 ==0) {
								int dummy=1;
							}
						}
					}
				}
			} else { // if n==curPlayer
				if (FINALADAPT_PART2 && !this.epiLengthStop) {
					// The following is equivalent to TDNTuple2Agt's call of m_Net.updateWeightsNewTerminal():
					//
					// If s_next is terminal, adapt the value of the *afterstate* that
					// curPlayer observed after he did his final move. Adapt it towards
					// target 0. (This is only relevant for TERNARY==false, since
					// only then the value of this afterstate is used in getNextAction2.)
					//
					// 09/2019: We do the final part2 adaptation only, if the game-over condition is not due ot an
					// epiLengthStop (then the final state has no special meaning and should NOT be adapted towards
					// target 0
					StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(s_after, m_Net.xnf);
//					curBoard = curSOWB.getBoardVector().bvec;
					vLast = m_Net.getScoreI(curSOWB,curPlayer);
					m_Net.updateWeightsTD(curSOWB, curPlayer, vLast, 0.0, R.scTup[curPlayer], s_next);
				}

			}
		} // for

	}

	/**
	 * Train the agent for one complete game episode <b>using self-play</b>.
	 * <p>
	 * Side effects: Increment gameNum by +1.
	 * Change the agent's internal parameters (weights and so on).
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState(PlayAgent)} to get
	 * 					some exploration of different game paths)
	 * @return			true, if agent raised a stop condition (only CMAPlayer)
	 */
	public boolean trainAgent(StateObservation so) {
		return trainAgent(so,this);
	}

	/**
	 * Train the agent for one complete game episode <b>using self-play</b>. <p>
	 * Side effects: Increment m_GameNum and {@code acting_pa}'s gameNum by +1.
	 * Change the agent's internal parameters (weights and so on).
	 * <p>
	 * This method is used by the wrappers: They call it with {@code this} being the wrapped agent (it has the internal
	 * parameters) and {@code acting_pa} being the wrapper.
	 *
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState(PlayAgent)} to get
	 * 					some exploration of different game paths)
	 * @param acting_pa the agent to be called when an action is requested ({@code getNextAction2})
	 * @return			true, if agent raised a stop condition (only CMAPlayer - deprecated)
	 */
	public boolean trainAgent(StateObservation so, PlayAgent acting_pa) {
		Types.ACTIONS_VT a_t;
		int curPlayer;
		NextState4 ns;
		ScoreTuple R;
		rLast = new ScoreTuple(so);

		int epiLength = m_oPar.getEpisodeLength();
		if (epiLength==-1) epiLength = Integer.MAX_VALUE;

		StateObservation s_t = so.copy();
		for (int n=0; n<numPlayers; n++) {
			sLast[n] = null;
		}
		int kk= (so.getPlayer()-1+numPlayers)%numPlayers;	// p=0,N=1 --> kk=0; p=0,N=2 --> kk=1
		sLast[kk] = so.precedingAfterstate();
		// The player kk who generated 'so' gets so's preceding afterstate as its sLast.
		// (This is important for RubiksCube, in order to learn from the first move on in
		// this deterministic single-player game: The player who generated 'so' is so.getPlayer()
		// itself, thus kk=0, and the preceding afterstate is 'so' itself.)

		m_counter=0;		// /WK/bug fix 2019-05-21
		m_finished=false;	// /WK/bug fix 2019-05-21
		do {
			m_numTrnMoves++;		// number of train moves (including random moves)
			// choose action a_t, using epsilon-greedy policy based on V
			a_t = acting_pa.getNextAction2(s_t.partialState(), true, true);

			// take action a_t and observe reward & next state
			ns = new NextState4(this,s_t,a_t);
			curPlayer = ns.getSO().getPlayer();

			assert curPlayer<randLast.length : "Oops, randLast too small!";
			randLast[curPlayer] = a_t.isRandomAction(); // /WK/ bug fix: has to come before adaptAgentV (!)
			// Remark: This line was missing before 2019-09-02, it was only done 4 lines later, after
			// adaptAgentV(). As a result, in the old version randLast[curPlayer] would be false, even
			// if a_t was a random action. (Only in the next round, when a_t might be greedy again,
			// randLast[curPlayer] would be true.). This was the wrong behavior, which is now fixed.
			R = ns.getNextRewardTupleCheckFinished(epiLength);	// this may set m_finished

			// Check for replaybuffer
			if(ConfigReplayBuffer.USE_REPLAYBUFFER) {
				adaptAgentV2(curPlayer,R,ns);
			}else{
				adaptAgentV(curPlayer, R, ns);
			}
			sLast[curPlayer] = ns.getAfterState();
			rLast.scTup[curPlayer] = R.scTup[curPlayer];
			// we differentiate between the afterstate (on which we learn) and the
			// next state s_t, which may have environment random elements added and from which
			// we advance.
			// (for deterministic games, ns.getAfterState() and ns.getNextSO() are the same)

			s_t = ns.getNextSO();

			if (s_t.isRoundOver() && !getParTD().hasStopOnRoundOver()) {	// /WK/ NEW 03/2021
				s_t.initRound();
			}
		} while(!m_finished);			// simplification: m_finished is set by ns.getNextRewardTupleCheckFinished

		if (FINALADAPTAGENTS) {
			// check for replay buffer
			if(ConfigReplayBuffer.USE_REPLAYBUFFER) {
				finalAdaptAgents2(curPlayer,R,ns);
				learnFromReplayBuffer();
			}else finalAdaptAgents(curPlayer, R, ns);
		}
		// start learning

		try {
			this.finishUpdateWeights();		// adjust learn params ALPHA & m_epsilon
		} catch (Throwable e) {
			e.printStackTrace();
		}

		//System.out.println("episode: "+getGameNum()+", moveNum="+m_counter);
		incrementGameNum();
		acting_pa.setGameNum(this.getGameNum());

		return false;
	} // trainAgent


	/**
	 * This function is called when {@code ns.getNextSO()} is terminal. It does two things:
	 * <ol>
	 * <li> It takes for each player <b>other than</b> {@code curPlayer} the final delta reward r (from the perspective of
	 * 		this other player) and adapts the value of the last state of this other player towards r.<br>
	 * 		[This first part is irrelevant for 1-player games where we have no other players.]
	 * <li> For {@code curPlayer} <b>only</b>: Adapt the value of {@code ns.getAfterState()}, the afterstate preceding
	 * 		the terminal state, towards 0. <br>
	 * 		[This second part is only relevant if TERNARY==false in source code.]
	 * </ol>
	 *
	 * @param curPlayer the player to move in @{@code ns.getSO()} = the player who generates {@code ns.getNextSO()}
	 * @param R         the reward tuple for {@code ns.getNextSO()}
	 * @param ns		the next state object when applying action {@code a_t} in state {@code s_t}
	 */
	private void finalAdaptAgents2(int curPlayer, ScoreTuple R, NextState4 ns) {
		for (int n=0; n<numPlayers; n++) {
			if (n!=curPlayer) {
				if (sLast[n]!=null ) {
					// Add the final adaptation to the replay buffer from the perspective of all other players
					replayBuffer.addTransition(n,ns,sLast[n],rLast,R,1);
					//learnFromReplayBuffer();
				}
			} else {
				if (FINALADAPT_PART2 && !this.epiLengthStop) {
					// Add the final adaptation to the replay buffer from the perspective of curplayer
					replayBuffer.addTransition(curPlayer,ns,ns.getAfterState(),rLast,R,2);
				}
			}
		}
	}




	private void adaptAgentV2(int curPlayer, ScoreTuple R, NextState4 ns) {
		boolean learnFromRM = m_oPar.getLearnFromRM();
		if (sLast[curPlayer]!=null) {
			if (randLast[curPlayer] && !learnFromRM) {
				m_Net.clearEligList(m_elig);	// the list is only cleared if m_elig==RESET
			} else {
				replayBuffer.addTransition(curPlayer,ns,sLast[curPlayer],rLast,R,0);

			}
		}
	}



	private void learnFromReplayBuffer(){
		// Awaiting the replay buffer to be full prior learning attempt
		//if(replayBuffer.getMaxBufferIndex() != replayBuffer.getCapacity()) return;
		// Awaiting the replay buffer to contain at least #batchSize transitions
		if(replayBuffer.getMaxBufferIndex() < replayBuffer.getBatchSize()) return;
		ITransition[] transitions = replayBuffer.getBatch();
		for(ITransition t : transitions){
			int player = t.getPlayer();
			StateObsWithBoardVector sowb = new StateObsWithBoardVector(t.getSLast(), m_Net.xnf);
			double vLast = m_Net.getScoreI(sowb,player);
			double target;
			double r_next;
			StateObservation s_next = t.getNextState4().getNextSO();
			StateObservation s_after = t.getNextState4().getAfterState();
			double v_next = calculate_v_next(s_next,s_after,player);
			r_next  = (t.getR().scTup[player] - t.getRLast().scTup[player])
					+ s_next.getStepRewardTuple().scTup[player];
			switch(t.isFinalTransition()){
				case 0:{
					if (TERNARY) {
						target = s_next.isGameOver() ? r_next : getGamma()*v_next;
					} else {
						target = r_next + getGamma()*v_next;
					}
					break;
				}
				case 1:{
					target = r_next;
					break;
				}
				case 2:{
					target = 0.0;
					break;
				}
				default: throw new RuntimeException("isFinalTransition error: Value is " + t.isFinalTransition());
			}
			if(ConfigReplayBuffer.DBG){
				String s = "";
				s += "FinalState:" +sowb.getStateObservation().isGameOver()+"\n";
				s+= "Player:"+player+"\nvLast:"+ vLast +"\nTarget:" +target+"\nReward:"+r_next;
				System.out.println(s);

			}
			// Update the weights
			m_Net.updateWeightsTD(sowb,player,vLast,target,r_next,t.getNextState4().getNextSO());
		}
	}

	/**
	 *
	 * @param s_next
	 * @param s_after
	 * @param player
	 * @return
	 */
	private double calculate_v_next(StateObservation s_next, StateObservation s_after,int player){
		double v_next = 0.0;
		if (s_next.isGameOver()) {
			v_next = 0.0;
		} else {
			StateObsWithBoardVector nextSOWB = new StateObsWithBoardVector(s_after,m_Net.xnf);
			v_next = m_Net.getScoreI(nextSOWB,player);
		}
		return v_next;
	}

	@Override
	public String stringDescr() {
		m_Net.setHorizon();
		String cs = getClass().getSimpleName();
		return       cs + ": USESYM:" + (m_ntPar.getUSESYMMETRY()?"true":"false")
				+ ", P:" + (m_Net.getXnf().getNumPositionValues())
				+ ", NORMALIZE:" + (m_tdPar.getNormalize()?"true":"false")
				+ ", sigmoid:"+(m_Net.hasSigmoid()? "tanh":"none")
				+ ", lambda:" + m_Net.getLambda()
				+ ", horizon:" + m_Net.getHorizon()
				+ ", AFTERSTATE:" + (m_ntPar.getAFTERSTATE()?"true":"false")
				+ ", learnFromRM: " + (m_oPar.getLearnFromRM()?"true":"false");
	}

	@Override
	public String stringDescr2() {
		String cs = getClass().getSimpleName();
		return       cs + ": alpha_init->final:" + m_tdPar.getAlpha() + "->" + m_tdPar.getAlphaFinal()
				+ ", epsilon_init->final:" + m_tdPar.getEpsilon() + "->" + m_tdPar.getEpsilonFinal()
				+ ", gamma: " + m_tdPar.getGamma()
				+ ", "+stringDescrNTuple()		// see NTupleBase
				+ ", evalQ: " + m_oPar.getQuickEvalMode() + ", evalT: " + m_oPar.getTrainEvalMode();
	}

	// Callback function from constructor NextState4(NTupleAgt,StateObservation,ACTIONS).
	// It sets various elements of NextState4 ns (nextReward, nextRewardTuple).
	// It is part of TDNTuple4Agt (and not part of NextState4), because it uses various elements
	// private to TDNTuple4Agt (DBG_REWARD, referringState, normalize2)
	public void collectReward(NextState4 ns) {
		boolean rgs = m_oPar.getRewardIsGameScore();
		ns.nextRewardTuple = new ScoreTuple(ns.refer);
		for (int i=0; i<ns.refer.getNumPlayers(); i++) {
			ns.nextRewardTuple.scTup[i] = normalize2(ns.nextSO.getReward(i,rgs),ns.nextSO);
		}

		// for completeness, ns.nextReward is not really needed in TDNTuple4Agt
		ns.nextReward = normalize2(ns.nextSO.getReward(ns.nextSO.getPlayer(),rgs),ns.refer);

		if (DBG_REWARD && ns.nextSO.isGameOver()) {
			System.out.print("Rewards: ");
			System.out.print(ns.nextRewardTuple.toString());
//			System.out.print("Reward: "+ns.nextReward);
			System.out.println("   ["+ns.nextSO.stringDescr()+"]  " + ns.nextSO.getGameScore(ns.nextSO.getPlayer())
					+ " for player " + ns.nextSO.getPlayer());
		}
	}


}
