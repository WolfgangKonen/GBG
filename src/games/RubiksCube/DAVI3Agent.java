package games.RubiksCube;

import java.io.Serializable;
import java.util.*;

import agentIO.LoadSaveGBG;
import controllers.PlayAgent;
import controllers.TD.ntuple2.NTuple2ValueFunc;
import controllers.TD.ntuple2.NTupleBase;
import controllers.TD.ntuple2.NextState;
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

/**
 *  Implements the DAVI algorithm (Deep Approximate Value Iteration) for Rubik's Cube [Agnostelli2019].
 *  <p>
 *  It simplifies DAVI by replacing the deep neural net with a (shallow but wide) n-tuple network.
 *  <ul>
 *      <li> If {@link CubeConfig#REPLAYBUFFER} is true, simplify DAVI further by updating the net in each step only with
 *      the actual (state, target) pair </li>
 *      <li> If {@link CubeConfig#REPLAYBUFFER} is false, maintain a replay buffer of {@link TrainingItem}s and train the net
 *      in batches sampled from this replay buffer. </li>
 *  </ul>
 *  It <b>maximizes</b> the (non-positive) value V(s) where each step (twist) adds a negative step reward to V(s).
 *  Only the solved cube s* has V(s*)=0.
 *
 */
public class DAVI3Agent extends NTupleBase implements PlayAgent {

	private static StateObserverCube def = new StateObserverCube();   // default (solved) cube

	private Random rand;

	private transient LinkedList replayBuffer;

//	private NTupleAgt.EligType m_elig;
//	private int numPlayers;

	private final boolean RANDINITWEIGHTS = false;// If true, initialize weights of value function randomly

	private final boolean m_DEBG = false; //false;true;

	/**
	 * change the version ID for serialization only if a newer version is no longer
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	/**
	 * Create a new {@link DAVI3Agent}
	 * 
	 * @param name			agent name
	 * @param tdPar			temporal difference parameters
	 * @param ntPar			n-tuples and temporal coherence parameter
	 * @param nTuples		the set of n-tuples
	 * @param xnf			contains game-specific n-tuple functions
	 * @param maxGameNum	maximum number of training games
	 */
	public DAVI3Agent(String name, ParTD tdPar, ParNT ntPar, ParOther oPar, 
			int[][] nTuples, XNTupleFuncs xnf, int maxGameNum) {
		super(name);
//		this.numPlayers = xnf.getNumPlayers();
		initNet(ntPar,tdPar,oPar, nTuples, xnf, maxGameNum);			
	}

	/**
	 * 
	 * @param tdPar			temporal difference parameters
	 * @param ntPar			n-tuples and temporal coherence parameter
	 * @param oPar			other params
	 * @param nTuples		the set of n-tuples
	 * @param xnf			contains game-specific n-tuple functions
	 * @param maxGameNum	maximum number of training games
	 */
	private void initNet(ParNT ntPar, ParTD tdPar, ParOther oPar,
			int[][] nTuples, XNTupleFuncs xnf, int maxGameNum) {
		m_tdPar = new ParTD(tdPar);			// m_tdPar is in NTupleBase
		m_ntPar = new ParNT(ntPar);			// m_ntPar is in NTupleBase
		m_oPar = new ParOther(oPar);		// m_oPar is in AgentBase
//		m_elig = (m_tdPar.getEligMode()==0) ? EligType.STANDARD : EligType.RESET;
		rand = new Random(System.currentTimeMillis()); //(System.currentTimeMillis()); (42); 
		
		int posVals = xnf.getNumPositionValues();
		int numCells = xnf.getNumCells();
		
		m_Net = new NTuple2ValueFunc(this,nTuples, xnf, posVals,
				RANDINITWEIGHTS,ntPar,numCells,1);
		
		setNTParams(ntPar);
		setTDParams(tdPar, maxGameNum);
		m_Net.setHorizon();

		replayBuffer  = new LinkedList();

		setAgentState(AgentState.INIT);
	}

	/**
	 * If agents need a special treatment after being loaded from disk (e. g. instantiation
	 * of transient members), put the relevant code in here.
	 * 
	 * @see LoadSaveGBG#transformObjectToPlayAgent
	 */
	public boolean instantiateAfterLoading() {
		this.m_Net.xnf.instantiateAfterLoading();
		assert (m_Net.getNTuples()[0].getPosVals()==m_Net.xnf.getNumPositionValues()) : "Error getPosVals()";
		assert (this.getParTD().getHorizonCut()!=0.0) : "Error: horizonCut==0";
		
		// set certain elements in td.m_Net (withSigmoid, useSymmetry) from tdPar and ntPar
		// (they would stay otherwise at their default values, would not 
		// get the loaded values)
		this.setTDParams(this.getParTD(), this.getMaxGameNum());
		this.setNTParams(this.getParNT());
		this.weightAnalysis(null);
		
		// initialize transient members (in case a further training should take place --> see ValidateAgentTest) 
		this.m_Net.instantiateAfterLoading();   // instantiate transient eList and nTuples

		replayBuffer  = new LinkedList();

		return true;
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
		int i;
		StateObserverCube newSO;
        ArrayList<ACTIONS> acts = so.getAvailableActions();
        ACTIONS actBest = null;
        List<Types.ACTIONS> bestActions = new ArrayList<>();
		double[] vTable = new double[acts.size()+1];  
        double maxValue = -Double.MAX_VALUE;
        double value;

        assert so.isLegalState() : "Not a legal state"; 
        assert so instanceof StateObserverCube : "Not a StateObserverCube object";
// try {       
        for(i = 0; i < acts.size(); ++i)
        {
        	ACTIONS thisAct = acts.get(i);
        	ACTIONS inverseAct = inverseAction(((StateObserverCube) so).getLastAction());
        	if (!thisAct.equals(inverseAct)) {
        		// we skip the action which is the inverse of the last action;
				// this is to avoid cycles of 2

				newSO = ((StateObserverCube) so).copy();
				newSO.advance(acts.get(i));

				// value is the V(s) for for taking action i in state s='so'. Action i leads to state newSO.
				value = vTable[i] = CubeConfig.stepReward + daviValue(newSO);
				assert (!Double.isNaN(value)) : "Oops, daviValue returned NaN! Decrease alpha!";
				// Always *maximize* 'value'
				if (value==maxValue) bestActions.add(acts.get(i));
				if (value>maxValue) {
					maxValue = value;
					bestActions.clear();
					bestActions.add(acts.get(i));
				}
			}
        } // for
        
        assert bestActions.size() > 0 : "Oops, no element in bestActions! ";
        // There might be one or more than one action with maxValue.
        // Break ties by selecting one of them randomly:
        actBest = bestActions.get(rand.nextInt(bestActions.size()));

        // optional: print the best action's after state newSO and its V(newSO) = stepReward + daviValue(newSO)
        if (!silent) {
        	newSO = ((StateObserverCube) so).copy();
        	newSO.advance(actBest);
        	System.out.println("---Best Move: "+newSO.stringDescr()+"   "+maxValue);
        }			

        vTable[acts.size()] = maxValue;
// } catch (Exception e) {
//	 e.printStackTrace();
// }
        return new ACTIONS_VT(actBest.toInt(), false, vTable);
	}

	private ACTIONS inverseAction(ACTIONS act) {
		int[] inverseActs = {2,1,0, 5,4,3, 8,7,6, 9};	// '9' codes 'not known' --> we return 'not known'
		int iAction = act.toInt();
		return new ACTIONS(inverseActs[iAction]);
	}

	/**
	 * This is the NN version: Ask the neural net (here: an ntuple network) to predict the value of {@code so}
	 * @param so	the state
	 * @return REWARD_POSITIVE, if {@code so} is the solved state. In all other cases, return the prediction of {@link #m_Net}.
	 */
	public double daviValue(StateObserverCube so) {
		double score;
		if (so.isEqual(def)) return StateObserverCube.REWARD_POSITIVE;
		StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(so,m_Net.xnf);
		score = m_Net.getScoreI(curSOWB,so.getPlayer());
		return score;
	}
	
    /**
     * Train the agent for one complete episode starting from state so
     * 
     * @param so 		start state
	 * @return			true, if agent raised a stop condition (only CMAPlayer - deprecated)	 
     */
    @Override
	public boolean trainAgent(StateObservation so) {
		return (CubeConfig.REPLAYBUFFER) ? trainAgent_replayBuffer(so) : trainAgent_baseline(so);
	}

	public boolean trainAgent_baseline(StateObservation so) {
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
	        
			a_t = getNextAction2(s_t, false, true);	// choose action a_t (agent-specific behavior)

	        // update the network's response to current state s_t: Let it move towards the desired target:
			target = a_t.getVBest();        		
    		StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(s_t, m_Net.xnf);
			curPlayer = s_t.getPlayer();
        	vLast = m_Net.getScoreI(curSOWB,curPlayer);
			m_Net.updateWeightsTD(curSOWB, curPlayer, vLast, target,CubeConfig.stepReward,s_t);
			
			//System.out.println(s_t.stringDescr()+", "+a_t.getVBest());
	        
			s_t.advance(a_t);		// advance the state 

			if (s_t.isGameOver()) m_finished = true;
			if (s_t.getMoveCounter()>=epiLength) {
				m_finished=true;
//				vLast = m_Net.getScoreI(curSOWB,curPlayer);
//				target=((StateObserverCube) s_t).getMinGameScore();
//				m_Net.updateWeightsTD(curSOWB, curPlayer, vLast, target,CubeConfig.stepReward,s_t);
			}

		} while(!m_finished);			
		//System.out.println("Final state: "+s_t.stringDescr()+", "+a_t.getVBest());
				
		incrementGameNum();
		if (this.getGameNum() % 2000 == 0) System.out.println("gameNum: "+this.getGameNum());
		
		return false;		
	}

	public boolean trainAgent_replayBuffer(StateObservation so) {
		Types.ACTIONS_VT  a_t;
		StateObservation s_t = so.copy();
		int epiLength = m_oPar.getEpisodeLength();
		LinkedList episodeList = new LinkedList();
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

			a_t = getNextAction2(s_t, false, true);	// choose action a_t (agent-specific behavior)

			// add a new TrainingElem to episodeList
			TrainingItem trainItem = new TrainingItem(
					new StateObsWithBoardVector(s_t.copy(), m_Net.xnf),
					a_t.getVBest(),
					this.getGameNum()
			);
			episodeList.addFirst(trainItem);
			//System.out.println(s_t.stringDescr()+", "+a_t.getVBest());

			s_t.advance(a_t);		// advance the state

			if (s_t.isGameOver()) {
				m_finished = true;

				// successful episode --> put all episodeList elements into replayBuffer
				ListIterator<DAVI3Agent.TrainingItem> iter = episodeList.listIterator();
				while(iter.hasNext()) {
					replayBuffer.addFirst(iter.next());
					if (replayBuffer.size() > CubeConfig.replayBufferCapacity) replayBuffer.pollLast();
				}

			}
			if (s_t.getMoveCounter()>=epiLength) {
				m_finished=true;

				// unsuccessful episode --> reduce the target of all elements in episodeList by 'amount'
				// and put them then into replayBuffer
				double amount = m_oPar.getIncAmount(); //-0.03;
				ListIterator<DAVI3Agent.TrainingItem> iter = episodeList.listIterator();
				while(iter.hasNext()) {
//					TrainingItem ti = iter.next();
//					ti.reduceTarget(amount);
					replayBuffer.addFirst(iter.next().increaseTarget(amount));
					if (replayBuffer.size() > CubeConfig.replayBufferCapacity) replayBuffer.pollLast();
				}

			}

		} while(!m_finished);
		//System.out.println("Final state: "+s_t.stringDescr()+", "+a_t.getVBest());

		// train network from replayBuffer ...
		if (replayBuffer.size() < CubeConfig.batchSize) {
			// ... with all samples in replayBuffer
			ListIterator<TrainingItem> iter = replayBuffer.listIterator();
			while(iter.hasNext()) {
				item = iter.next();
				curPlayer = item.sowb.getStateObservation().getPlayer();
				vLast = m_Net.getScoreI(item.sowb,curPlayer);
				m_Net.updateWeightsTD(item.sowb, curPlayer, vLast, item.target,CubeConfig.stepReward,item.sowb.getStateObservation());
			}
		} else {
			// ... with batchSize random samples
			for (int i=0; i<CubeConfig.batchSize; i++) {
				index = rand.nextInt(replayBuffer.size());
				item = (TrainingItem)replayBuffer.get(index);
				s_t = item.sowb.getStateObservation();
				vLast = m_Net.getScoreI(item.sowb,s_t.getPlayer());
				m_Net.updateWeightsTD(item.sowb, s_t.getPlayer(), vLast, item.target,CubeConfig.stepReward,s_t);
			}
		}

		incrementGameNum();
		if (this.getGameNum() % 2000 == 0) System.out.println("gameNum: "+this.getGameNum());

		return false;
	}

	@Override
	public double getScore(StateObservation so) {
        assert (so instanceof StateObserverCube) : "Not a StateObserverCube object";
		StateObserverCube soC = (StateObserverCube) so;
		return daviValue(soC);
	}

	@Override
	public ScoreTuple getScoreTuple(StateObservation so, ScoreTuple prevTuple) {
		ScoreTuple sTuple = new ScoreTuple(1);
		sTuple.scTup[0] = this.getScore(so);
		return sTuple;
	}

	@Override
	public double estimateGameValue(StateObservation so) {
		return so.getGameScore(so.getPlayer());
	}

	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation so, ScoreTuple prevTuple) {
		return so.getRewardTuple(true);  //getScoreTuple(so, null);
	}

	@Override
	public boolean isTrainable() {
		return true;
	}

    @Override
	public String printTrainStatus() {
    	int[] res = m_Net.activeWeights();
		return getClass().getSimpleName()+": pMax="+CubeConfig.pMax + ", active weights="+res[1]; 
	}

	@Override
	public String stringDescr() {
		m_Net.setHorizon();
		String cs = getClass().getSimpleName();
		String str = cs + ": USESYM:" + (m_ntPar.getUSESYMMETRY()?"true":"false")
				+ ", P:" + (m_Net.getXnf().getNumPositionValues())
				+ ", NORMALIZE:" + (m_tdPar.getNormalize()?"true":"false")
				+ ", sigmoid:"+(m_Net.hasSigmoid()? "tanh":"none")
				+ ", alpha:" + m_Net.getAlpha()
				+ ", pMax:" + m_oPar.getpMaxRubiks()
				+ ", EE:" + m_oPar.getStopEval()		// Eval EpiLength
				+ ", qMode:" + m_oPar.getQuickEvalMode()
				+ ", incAmount:" + m_oPar.getIncAmount()
				;
		return str;
	}

	@Override
	public String stringDescr2() {
		m_Net.setHorizon();
		int[] res = m_Net.activeWeights();
		return getClass().getName() + ": pMax="+CubeConfig.pMax + ", epiLength="+m_oPar.getEpisodeLength()
									+ ", active weights="+res[1] + ", horizon="+m_Net.getHorizon();
	}

	// Callback function from constructor NextState(NTupleAgt,StateObservation,ACTIONS). 
	// Currently only dummy to make the interface NTupleAgt (which NTupleBase has to implement) happy!
	public void collectReward(NextState ns) {
	}

	// class TrainingItem is needed for replayBuffer (see trainAgent_replayBuffer(so))
	private class TrainingItem implements Serializable {
		StateObsWithBoardVector sowb;
		double target;
		int numEpisode;

		TrainingItem(StateObsWithBoardVector sowb, double target, int numEpisode) {
			this.sowb = sowb;
			this.target = target;
			this.numEpisode = numEpisode;
		}

		TrainingItem increaseTarget(double amount) {
			this.target += amount;
			return this;
		}
	}

}

