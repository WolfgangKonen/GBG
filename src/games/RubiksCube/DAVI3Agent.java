package games.RubiksCube;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import controllers.TD.ntuple2.NTuple2ValueFunc;
import controllers.TD.ntuple2.NTupleAgt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import controllers.TD.ntuple2.NTupleAgt.EligType;
import controllers.TD.ntuple2.NTupleBase;
import controllers.TD.ntuple2.NextState;
import games.StateObservation;
import games.XNTupleFuncs;
import params.ParNT;
import params.ParOther;
import params.ParTD;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_ST;
import tools.Types.ACTIONS_VT;

/**
 *  Implements DAVI algorithm (Deep Approximate Value Iteration) for Rubiks Cube [Agnostelli2019].
 *  <p>
 *  It implements DAVI by using an n-tuple network as the neural net. It simplifies DAVI by updating 
 *  the net in each step only with the actual (target,state) pair. 
 *  It <b>maximizes</b> the (non-positive) value V(s) where each step (twist) adds a negative step reward to V(s).  
 *  Only the solved cube s* has V(s*)=0.
 *
 */
public class DAVI3Agent extends NTupleBase implements PlayAgent {

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	private static StateObserverCube def = new StateObserverCube();   // default (solved) cube
	
	private static double LOW_V = -9.0;		// a low V-value for all states not present in HashMap
	private static double stepReward = -0.01;
	
	private Random rand;

	private NTupleAgt.EligType m_elig;
	
	private int numPlayers;

	private boolean RANDINITWEIGHTS = false;// If true, initialize weights of value function randomly

	private boolean m_DEBG = false; //false;true;

	/**
	 * Create a new {@link DAVI3Agent}
	 * 
	 * @param name			agent name
	 * @param tdPar			temporal difference parameters
	 * @param ntPar			n-tuples and temporal coherence parameter
	 * @param nTuples		the set of n-tuples
	 * @param xnf			contains game-specific n-tuple functions
	 * @param maxGameNum	maximum number of training games
	 * @throws IOException
	 */
	public DAVI3Agent(String name, ParTD tdPar, ParNT ntPar, ParOther oPar, 
			int[][] nTuples, XNTupleFuncs xnf, int maxGameNum) throws IOException {
		super(name);
		this.numPlayers = xnf.getNumPlayers();
		initNet(ntPar,tdPar,oPar, nTuples, xnf, maxGameNum);			
	}

	/**
	 * 
	 * @param tdPar			temporal difference parameters
	 * @param ntPar			n-tuples and temporal coherence parameter
	 * @param nTuples		the set of n-tuples
	 * @param xnf			contains game-specific n-tuple functions
	 * @param numOutputs	the number of outputs of the n-tuple network (=number of all
	 * 						available actions)
	 * @param maxGameNum	maximum number of training games
	 * @throws IOException
	 */
	private void initNet(ParNT ntPar, ParTD tdPar, ParOther oPar,  
			int[][] nTuples, XNTupleFuncs xnf, int maxGameNum) throws IOException {
		m_tdPar = new ParTD(tdPar);			// m_tdPar is in NTupleBase
		m_ntPar = new ParNT(ntPar);			// m_ntPar is in NTupleBase
		m_oPar = new ParOther(oPar);		// m_oPar is in AgentBase
		m_elig = (m_tdPar.getEligMode()==0) ? EligType.STANDARD : EligType.RESET;
		rand = new Random(System.currentTimeMillis()); //(System.currentTimeMillis()); (42); 
		
		int posVals = xnf.getNumPositionValues();
		int numCells = xnf.getNumCells();
		
		m_Net = new NTuple2ValueFunc(this,nTuples, xnf, posVals,
				RANDINITWEIGHTS,ntPar,numCells,1);
		
		setNTParams(ntPar);
		setTDParams(tdPar, maxGameNum);
		m_Net.setHorizon();
		
		setAgentState(AgentState.INIT);
	}

	@Override
	public ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
		int i,j;
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
        	newSO = ((StateObserverCube) so).copy();
        	newSO.advance(acts.get(i));
        	
        	// value is the V(s) for for taking action i in state s='so'. Action i leads to state newSO.
        	value = vTable[i] = stepReward + daviValue(newSO);
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
        // There might be one or more than one action with minValue. 
        // Break ties by selecting one of them randomly:
        actBest = bestActions.get(rand.nextInt(bestActions.size()));

        // optional: print the best action
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

	/**
	 * This is the simple version: maintain a full hash table for all visited states. This is only viable for cubes 
	 * with not too large state spaces.
	 * @param so
	 * @return 0, if {@code so} is the solved state, LOW_V if {@code so} is unknown in the HashMap. In all
	 * 		   other cases, return the HashMap value of {@code so}.
	 */
	public double daviValue(StateObserverCube so) {
		double score;
		if (so.isEqual(def)) return 0;
		int[] bvec = m_Net.xnf.getBoardVector(so).bvec;
		score = m_Net.getScoreI(bvec,so.getPlayer());
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
		Types.ACTIONS_VT  a_t;
		StateObservation s_t = so.copy();
		int epiLength = m_oPar.getEpisodeLength();
		int[] curBoard;
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
	        
			a_t = getNextAction2(s_t, true, true);	// choose action a_t (agent-specific behavior)

	        // update the network's response to current state s_t: Let it move towards the desired target:
			target = a_t.getVBest();        		
			curBoard = m_Net.xnf.getBoardVector(s_t).bvec; 
			curPlayer = s_t.getPlayer();
        	vLast = m_Net.getScoreI(curBoard,curPlayer);
			m_Net.updateWeightsTD(curBoard, curPlayer, vLast, target,stepReward,s_t);
			
			//System.out.println(s_t.stringDescr()+", "+a_t.getVBest());
	        
			s_t.advance(a_t);		// advance the state 

			if (s_t.isGameOver()) m_finished = true;
			if (s_t.getMoveCounter()>=epiLength) m_finished=true;

		} while(!m_finished);			
		//System.out.println("Final state: "+s_t.stringDescr()+", "+a_t.getVBest());
				
		incrementGameNum();
		if (this.getGameNum() % 500 == 0) System.out.println("gameNum: "+this.getGameNum());
		
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

}
