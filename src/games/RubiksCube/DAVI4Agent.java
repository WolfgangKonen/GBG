package games.RubiksCube;

import controllers.PlayAgent;
import controllers.TD.ntuple4.TDNTuple4Agt;
import games.Arena;
import games.StateObsWithBoardVector;
import games.StateObservation;
import games.XNTupleFuncs;
import params.ParNT;
import params.ParOther;
import params.ParTD;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_VT;

import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *  Implements the DAVI algorithm (Deep Approximate Value Iteration) for Rubik's Cube [Agostinelli19].
 *  <p>
 *  It extends {@link DAVI3Agent} but has the training set generation closer to [McAleer18], [Agostinelli19],
 *  it follows their ADI method (Autodidactic iteration).
 *
 */
public class DAVI4Agent extends DAVI3Agent implements PlayAgent {

	/**
	 *  Flag for the training set generation process: <br>
	 *  If true, follow [McAleer18]: Generate from the solved cube a scramble sequence of length k <br>
	 *  If false, follow [Agostinelli19]: Generate from the solved cube a k-times scrambled cube.
	 *  <p>
	 *  In both cases, k is in each episode uniform-randomly selected from {1,2,...,pMax}.
	 *  The [McAleer18] approach has more (p=1) samples than (p=2) samples than ..., because every k-sequence has a
	 *  (p=1) state, most have a (p=2) state and only a fraction (1/pMax) has a (p=pMax) state.
	 *  <p>
	 *  Both approaches have pMax/2 states per train episode (on average).
	 */
	public final static boolean TRAINSET_MCALEER = true;

	public final static boolean STACKED_TCL = false;

	private PlayAgent stacked_tcl;
	private String stackedFile="TCL4-p16-10M-120-7t-lam05.agt.zip";
	/**
	 * change the version ID for serialization only if a newer version is no longer
	 * compatible with an older one (older .agt.zip will become unreadable, or you have
	 * to provide a special version transformation)
	 */
	@Serial
	private static final long serialVersionUID = 12L;

	/**
	 * Create a new {@link DAVI4Agent}
	 *
	 * @param name			agent name
	 * @param tdPar			temporal difference parameters
	 * @param ntPar			n-tuples and temporal coherence parameter
	 * @param nTuples		the set of n-tuples
	 * @param xnf			contains game-specific n-tuple functions
	 * @param maxGameNum	maximum number of training games
	 */
	public DAVI4Agent(String name, ParTD tdPar, ParNT ntPar, ParOther oPar,
                      int[][] nTuples, XNTupleFuncs xnf, int maxGameNum, Arena arena) {
		super(name, tdPar, ntPar, oPar,
		 		nTuples, xnf, maxGameNum);
		stacked_tcl = arena.loadAgent(stackedFile);
	}

	/**
	 * Get the best next action and return it
	 *
	 * @param so			current game state (is returned unchanged)
	 * @param random		irrelevant here
	 * @param silent		if false, print best action
	 * @return actBest,		the best action. If several actions have the same
	 * 						score, break ties by selecting one of them at random.
	 * actBest has also the members vTable and vBest to store the V-value for each available
	 * action nd the V-value for the best action actBest, resp.
	 */
	@Override
	public ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
		if (!STACKED_TCL)
			return super.getNextAction2(so,random,silent);

		int i;
		StateObserverCube newSO;
		ArrayList<ACTIONS> acts = so.getAvailableActions();
		ACTIONS actBest;
		List<ACTIONS> bestActions = new ArrayList<>();
		double[] vTable = new double[acts.size()];
		double maxValue = -Double.MAX_VALUE;
		double value;
		boolean rgs=false;

		assert so.isLegalState() : "Not a legal state";
		assert so instanceof StateObserverCube : "Not a StateObserverCube object";

		for(i = 0; i < acts.size(); ++i)
		{
			ACTIONS thisAct = acts.get(i);

			// If an action is the inverse of the last action, it would lead to the previous state again, resulting
			// in a cycle of 2. We avoid such cycles and continue with next pass through for-loop
			// --> beneficial when searching for the solved cube in play & train.
			// If you do not want to skip any action - e.g. when inspecting states - then enter this method with
			// a 'cleared' state that has m_action (the action that led to this state) set to 'unknown'.
			if (thisAct.isEqualToInverseOfLastAction(so))
				continue;  // with next for-pass

			newSO = ((StateObserverCube) so).copy();
			newSO.advance(acts.get(i));

			// value is the r + V(s) for taking action i in state s='so'. Action i leads to state newSO.
			value = vTable[i] = newSO.getRewardTuple(rgs).scTup[0] +
					newSO.getStepRewardTuple().scTup[0] + daviValue(newSO);
			// this is a bit complicated for saying "stepReward
			// ( + REWARD_POSITIVE, if it's the solved cube)' but
			// in this way we have a common interface valid for all games:
			//    vTable = deltaReward + V(s) [expected future rewards]
			assert (!Double.isNaN(value)) : "Oops, daviValue returned NaN! Decrease alpha!";
			// Always *maximize* 'value'
			if (value==maxValue) bestActions.add(acts.get(i));
			if (value>maxValue) {
				maxValue = value;
				bestActions.clear();
				bestActions.add(acts.get(i));
			}
		} // for

		assert bestActions.size() > 0 : "Oops, no element in bestActions! ";
		// There might be one or more than one action with maxValue.
		// Break ties by selecting one of them randomly:
		actBest = bestActions.get(rand.nextInt(bestActions.size()));

		// optional: print the best action's after state newSO and its V(newSO) = delta reward + daviValue(newSO)
		if (!silent) {
			newSO = ((StateObserverCube) so).copy();
			newSO.advance(actBest);
			System.out.println("---Best Move: "+newSO.stringDescr()+"   "+maxValue);
		}

		ScoreTuple scBest = new ScoreTuple(new double[]{maxValue});

		return new ACTIONS_VT(actBest.toInt(), false, vTable, maxValue, scBest);
	}

	/**
     * Train the agent for one complete episode starting from state so
     * 
     * @param so 		start state
	 * @return			true, if agent raised a stop condition (only CMAPlayer - deprecated)	 
     */
    @Override
	public boolean trainAgent(StateObservation so) {
		return  (STACKED_TCL) ? trainAgent_stackedTCL(so) :
				(CubeConfig.REPLAYBUFFER) ? trainAgent_replayBuffer(so) :
				(TRAINSET_MCALEER) ? trainAgent_autodidactic_McA(so) : trainAgent_autodidactic_Ago(so);
	}

	/**
	 * stacked TCL training: baseline training with a stacked TCL net to try at each move
	 */
	public boolean trainAgent_stackedTCL(StateObservation so) {
		Types.ACTIONS_VT  a_t;
		StateObservation s_t = so.copy();
		int epiLength = m_oPar.getEpisodeLength();
		int curPlayer;
		double vLast,target;
		assert (epiLength != -1) : "trainAgent: Rubik's Cube should not be run with epiLength==-1 !";
		if (so.equals(def)) {
			System.err.println("trainAgent: cube should NOT be the default (solved) cube!");
			return false;
		}
		boolean m_finished = false;

		do {
			m_numTrnMoves++;		// number of train moves

			a_t = getNextAction2(s_t.partialState(), false, true);	// choose action a_t (agent-specific behavior)

			// update the network's response to current state s_t: Let it move towards the desired target:
			target = a_t.getVBest();
			StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(s_t, m_Net.xnf);
			curPlayer = s_t.getPlayer();
			vLast = m_Net.getScoreI(curSOWB,curPlayer);
			m_Net.updateWeightsTD(curSOWB, curPlayer, vLast, target,
					s_t.getStepRewardTuple().scTup[0],s_t);

			//System.out.println(s_t.stringDescr()+", "+a_t.getVBest());

			s_t.advance(a_t);		// advance the state
			s_t.storeBestActionInfo(a_t);	// /WK/ was missing before 2021-09-10. Now stored ScoreTuple is up-to-date.

			if (s_t.isGameOver()) m_finished = true;
			if (s_t.getMoveCounter()>=epiLength) {
				m_finished=true;
//				vLast = m_Net.getScoreI(curSOWB,curPlayer);
//				target=((StateObserverCube) s_t).getMinGameScore();
//				m_Net.updateWeightsTD(curSOWB, curPlayer, vLast, target,s_t.getDeltaRewardTuple(false).scTup[0],s_t);
			}

		} while(!m_finished);
		//System.out.println("Final state: "+s_t.stringDescr()+", "+a_t.getVBest());

		incrementGameNum();

		return false;
	}

	/**
	 * train with true autodidactic iteration as in [McAleer2018]
	 */
	public boolean trainAgent_autodidactic_McA(StateObservation so) {
		ACTIONS_VT  a_t;
		ACTIONS act;
		StateObserverCube s_t = def.copy();	// we do not use param so, but start from the default cube and twist
		// it step-by-step until epiLength is reached
		int curPlayer;
		double vLast,target;
		boolean m_finished = false;
		int epiLength = m_oPar.getEpisodeLength();
		assert (epiLength != -1) : "trainAgent: Rubik's Cube should not be run with epiLength==-1 !";
		int p = 1 + rand.nextInt(m_oPar.getpMaxRubiks()-1);

		do {
			m_numTrnMoves++;		// number of train moves

			// advance with an action that brings the cube one twist further away from the solved cube
			do {
				act = s_t.getAction(rand.nextInt(s_t.getNumAvailableActions()));
				s_t.advance(act);
			} while (s_t.isEqual(def));
			s_t = (StateObserverCube) s_t.clearedAction();

			// select the best action back according to the current policy
			a_t = getNextAction2(s_t.partialState(), false, true);

			// update the network's response to current state s_t: Let it move towards the desired target:
			target = a_t.getVBest();
			StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(s_t, m_Net.xnf);
			curPlayer = s_t.getPlayer();
			vLast = m_Net.getScoreI(curSOWB,curPlayer);
//			m_Net.w_updateWeightsTD(curSOWB, curPlayer, 1.0/s_t.getMoveCounter(),vLast, target,
//					s_t.getStepRewardTuple().scTup[0],s_t);
			m_Net.updateWeightsTD(curSOWB, curPlayer, vLast, target,
					s_t.getStepRewardTuple().scTup[0],s_t);

			//System.out.println(s_t.stringDescr()+", "+a_t.getVBest());

			s_t.storeBestActionInfo(a_t);	// /WK/ was missing before 2021-09-10. Now stored ScoreTuple is up-to-date.

			if (s_t.isGameOver()) {
				System.err.println("Game over should not happen in autodidactic iteration");
				m_finished = true;
			}
			if (s_t.getMoveCounter()>=p) {
				m_finished=true;
			}

		} while(!m_finished);

		incrementGameNum();

		return false;
	}


	/**
	 * train with true autodidactic iteration as in [Agostinelli2019]
	 */
	public boolean trainAgent_autodidactic_Ago(StateObservation so) {
		ACTIONS_VT  a_t;
		ACTIONS act;
		StateObserverCube s_t = def.copy();	// so is actually not used
		int curPlayer;
		double vLast,target;
		boolean m_finished = false;
		int epiLength = m_oPar.getEpisodeLength();
		assert (epiLength != -1) : "trainAgent: Rubik's Cube should not be run with epiLength==-1 !";

		for (int k=0; k<epiLength; k++) {
			m_numTrnMoves++;		// number of train moves

			// advance with an action that brings the cube one twist further away from the solved cube
			int p = rand.nextInt(epiLength)+1;
			s_t = StateObserverCube.chooseNewState(p);

			// select the best action back according to the current policy
			a_t = getNextAction2(s_t.partialState(), false, true);

			// update the network's response to current state s_t: Let it move towards the desired target:
			target = a_t.getVBest();
			StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(s_t, m_Net.xnf);
			curPlayer = s_t.getPlayer();
			vLast = m_Net.getScoreI(curSOWB,curPlayer);
			m_Net.w_updateWeightsTD(curSOWB, curPlayer, 1.0/p,vLast, target,
					s_t.getStepRewardTuple().scTup[0],s_t);

			//System.out.println(s_t.stringDescr()+", "+a_t.getVBest());

			s_t.storeBestActionInfo(a_t);	// /WK/ was missing before 2021-09-10. Now stored ScoreTuple is up-to-date.

			if (s_t.isGameOver()) {
				System.err.println("Game over should not happen in autodidactic iteration");
			}
		}

		incrementGameNum();

		return false;
	}

	/**
	 * replay buffer training: maintain a replay buffer of recent training experience with capacity
	 * {@link CubeConfig#replayBufferCapacity}. First play a whole episode and add it (conditionally) to
	 * the replay buffer. Then perform a training where a batch of samples is drawn randomly from the replay buffer.
	 * Batch size is {@link CubeConfig#batchSize}.
	 */
	public boolean trainAgent_replayBuffer(StateObservation so) {
		ACTIONS_VT  a_t;
		StateObservation s_t = so.copy();
		int epiLength = m_oPar.getEpisodeLength();
		LinkedList<TrainingItem> episodeList = new LinkedList<>();
		TrainingItem item;
		int index;

		int curPlayer;
		double vLast;
		assert (epiLength != -1) : "trainAgent: Rubik's Cube should not be run with epiLength==-1 !";
		if (so.equals(def)) {
			System.err.println("trainAgent: cube should NOT be the default (solved) cube!");
			return false;
		}
		boolean m_finished = false;

		do {
			m_numTrnMoves++;		// number of train moves

			a_t = getNextAction2(s_t.partialState(), false, true);	// choose action a_t (agent-specific behavior)

			// add a new TrainingElem to episodeList
			TrainingItem trainItem = new TrainingItem(
					new StateObsWithBoardVector(s_t.copy(), m_Net.xnf),
					a_t.getVBest(),
					this.getGameNum()
			);
			episodeList.addFirst(trainItem);
			//System.out.println(s_t.stringDescr()+", "+a_t.getVBest());

			s_t.advance(a_t);		// advance the state
			s_t.storeBestActionInfo(a_t);	// /WK/ was missing before 2021-09-10. Now stored ScoreTuple is up-to-date.

			if (s_t.isGameOver()) {
				m_finished = true;

				// successful episode --> put all episodeList elements into replayBuffer
				for (TrainingItem trainingItem : episodeList) {
					replayBuffer.addFirst(trainingItem);
					if (replayBuffer.size() > CubeConfig.replayBufferCapacity) replayBuffer.pollLast();
				}

			}
			if (s_t.getMoveCounter()>=epiLength) {
				m_finished=true;

				// unsuccessful episode --> reduce the target of all elements in episodeList by 'amount'
				// and put them then into replayBuffer
				double amount = m_oPar.getIncAmount(); //-0.03;
				for (TrainingItem trainingItem : episodeList) {
//					trainingItem.reduceTarget(amount);
					replayBuffer.addFirst(trainingItem.increaseTarget(amount));
					if (replayBuffer.size() > CubeConfig.replayBufferCapacity) replayBuffer.pollLast();
				}

			}

		} while(!m_finished);
		//System.out.println("Final state: "+s_t.stringDescr()+", "+a_t.getVBest());

		// train network from replayBuffer ...
		if (replayBuffer.size() < CubeConfig.batchSize) {
			// ... with all samples in replayBuffer
			for (TrainingItem trainingItem : replayBuffer) {
				item = trainingItem;
				curPlayer = item.sowb.getStateObservation().getPlayer();
				vLast = m_Net.getScoreI(item.sowb, curPlayer);
				m_Net.updateWeightsTD(item.sowb, curPlayer, vLast, item.target,
						s_t.getStepRewardTuple().scTup[0], item.sowb.getStateObservation());
			}
		} else {
			// ... with batchSize random samples
			for (int i=0; i<CubeConfig.batchSize; i++) {
				index = rand.nextInt(replayBuffer.size());
				item = replayBuffer.get(index);
				s_t = item.sowb.getStateObservation();
				vLast = m_Net.getScoreI(item.sowb,s_t.getPlayer());
				m_Net.updateWeightsTD(item.sowb, s_t.getPlayer(), vLast, item.target,
						s_t.getStepRewardTuple().scTup[0], s_t);
			}
		}

		incrementGameNum();

		return false;
	}


}

