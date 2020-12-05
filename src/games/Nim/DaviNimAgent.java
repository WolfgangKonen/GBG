package games.Nim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import games.StateObservation;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_ST;
import tools.Types.ACTIONS_VT;

/**
 *  Implements DAVI algorithm (Deep Approximate Value Iteration) for Rubiks Cube [Agnostelli2019].
 *  <p>
 *  It simplifies DAVI by replacing the neural net with state-based value iteration. It maintains a table or  
 *  {@link HashMap} of the value of all seen states. Thus, it is probably only viable 
 *  for smaller systems and cannot generalize to unseen states).<br>
 *  It <b>maximizes</b> the (non-positive) value V(s) where each step (twist) adds a negative step reward to V(s).  
 *  Only the solved cube s* has V(s*)=0.
 *
 */
public class DaviNimAgent extends AgentBase implements PlayAgent {

	private static final long serialVersionUID = 12L;

	private static double[] LOW_V = {-9.0,-9.0,-9.0};		// a low V-value for all states not present in HashMap
//	private static double stepReward = -0.01;
	
	/**
	 * HashMap for the V-values V(s)
	 */
	private HashMap<String,ScoreTuple> vm;

	private Random rand;

//	public class ScoreTupleWithPlayer extends ScoreTuple {
//		
//		int player = 0;
//		
//		/**
//		 * @param N number of players
//		 */
//		public ScoreTupleWithPlayer(int N) {
//			super(N);
//		}
//		public ScoreTupleWithPlayer(StateObservation sob) {
//			super(sob);
//		}
////		public ScoreTupleWithPlayer(StateObservation sob,boolean lowest) {
////			this.scTup = new double[sob.getNumPlayers()];
////			for (int i=0; i<scTup.length; i++) scTup[i]=-Double.MAX_VALUE;
////		}
//		public ScoreTupleWithPlayer(double [] res, int player) {
//			super(res);
//			this.player = player;
//		}
//		public ScoreTupleWithPlayer(ScoreTupleWithPlayer other) {
//			super(other);
//			this.player = other.player;
//		}
//		
//	}
	
	public DaviNimAgent(String name, ParOther oPar) {
		super(name, oPar);
		super.setMaxGameNum(1000);		
		super.setGameNum(0);		
		setAgentState(AgentState.INIT);
        rand = new Random(System.currentTimeMillis());
		vm = new HashMap<String, ScoreTuple>();
	}
	
	@Override
	public ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
		int i,j;
		StateObserverNim3P newSO;
        ACTIONS_ST actBest = null;
        ACTIONS_ST act_st;
        ArrayList<ACTIONS> acts = so.getAvailableActions();
        ArrayList<ACTIONS_ST> bestActions = new ArrayList<>();
        ArrayList<ScoreTuple> bestTuples = new ArrayList<>();
		double[] vTable = new double[acts.size()];
        double maxValue = -Double.MAX_VALUE;
        double value;
        ScoreTuple scBest = null;

        assert so.isLegalState() : "Not a legal state"; 
        assert so instanceof StateObserverNim3P : "Not a StateObserverNim3P object";
        
        for(i = 0; i < acts.size(); ++i)
        {
        	newSO = ((StateObserverNim3P) so).copy();
        	newSO.advance(acts.get(i));
        	
        	// value is the V(s) for for taking action i in state s='so'. Action i leads to state newSO.
        	ScoreTuple sc = daviValue(newSO);
        	act_st = new ACTIONS_ST(acts.get(i),sc);
        	value = vTable[i] = sc.scTup[so.getPlayer()];
        	// Always *maximize* 'value' 
        	if (value==maxValue) {
        		bestActions.add(act_st);
        		bestTuples.add(sc);
        	}
        	if (value>maxValue) {
        		maxValue = value;
        		bestActions.clear();
        		bestTuples.clear();
        		bestActions.add(act_st);
        		bestTuples.add(sc);
        	}
        } // for
        
        // There might be one or more than one action with minValue. 
        // Break ties by selecting one of them randomly:
        int r = rand.nextInt(bestActions.size());
        actBest = bestActions.get(r);
        scBest = actBest.m_st;

        // optional: print the best action
        if (!silent) {
        	newSO = ((StateObserverNim3P) so).copy();
        	newSO.advance(actBest);
        	System.out.println("---Best Move: "+newSO.stringDescr()+"   "+maxValue);
        }			

        return new ACTIONS_VT(actBest.toInt(), false, vTable, maxValue, actBest.m_st);
	}

	/**
	 * This is the simple version: maintain a full hash table for all visited states. This is only viable for games 
	 * with not too large state spaces.
	 * @param so
	 * @return 0, if {@code so} is the solved state, LOW_V if {@code so} is unknown in the HashMap. In all
	 * 		   other cases, return the HashMap value of {@code so}.
	 */
	public ScoreTuple daviValue(StateObserverNim3P so) {
		Double dvalue = null;
		ScoreTuple tuple;
		if (so.isGameOver()) {
			return so.getRewardTuple(true);
		}
		String stringRep = so.stringDescr();
		tuple = vm.get(stringRep); 		// returns null if not in vm
		if (tuple!=null) {
			int dummy = 1;
		}
		ScoreTuple x = (tuple==null) ? new ScoreTuple(LOW_V) : tuple;
		return x; // - LOW_V;
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
		boolean m_finished = false;
		
		do {
	        m_numTrnMoves++;		// number of train moves 
	        
			a_t = getNextAction2(s_t.partialState(), true, true);	// choose action a_t (agent-specific behavior)
	        // put the best V-table value for state s_t into the HashMap
			if (a_t.getScoreTuple().scTup[0]!=-9.0)
				vm.put(s_t.stringDescr(), a_t.getScoreTuple());
	        
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
        assert (so instanceof StateObserverNim3P) : "Not a StateObserverNim3P object";
        ScoreTuple sc = daviValue((StateObserverNim3P) so);
		return sc.scTup[so.getPlayer()];
	}

	@Override
	public ScoreTuple getScoreTuple(StateObservation so, ScoreTuple prevTuple) {
		if (so.isGameOver()) return so.getRewardTuple(true);
		String stringRep = so.stringDescr();
		return vm.get(stringRep); 		// returns null if not in vm
	}

	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation so, ScoreTuple prevTuple) {
		if (so.isGameOver()) return so.getRewardTuple(true);
		String stringRep = so.stringDescr();
		return vm.get(stringRep); 		// returns null if not in vm
	}

	@Override
	public boolean isTrainable() {
		return true;
	}

    @Override
	public String printTrainStatus() {
		return getClass().getSimpleName()+", vm.size="+vm.size(); 
	}

    @Override
	public String stringDescr2() {
		return getClass().getName() + ", vm.size="+vm.size();
	}

}
