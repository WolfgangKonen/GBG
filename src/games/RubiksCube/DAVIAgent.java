package games.RubiksCube;

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
 *  It simplifies DAVI by replacing the neural net with state-based value iteration (thus it is only viable 
 *  for smaller systems and cannot generalize to unseen states).<br>
 *  It <b>minimizes</b> the cost-to-go J(s) where each step (twist) adds a positive step cost to J(s). Only 
 *  the solved cube s* has J(s*)=0.
 *  <p>
 *
 */
public class DAVIAgent extends AgentBase implements PlayAgent {

	private static final long serialVersionUID = 12L;

	private static StateObserverCube def = new StateObserverCube();   // default (solved) cube
	
	private static double HIGH_J = 9.0;		// a high cost value for all states not present in HashMap
	private static double stepCost = 0.01;
	
	/**
	 * HashMap for the cost-to-go values J(s)
	 */
	private HashMap<String,Double> jm;

	private Random rand;

	public DAVIAgent(String name, ParOther oPar) {
		super(name, oPar);
		super.setMaxGameNum(1000);		
		super.setGameNum(0);		
		setAgentState(AgentState.INIT);
        rand = new Random(System.currentTimeMillis());
		jm = new HashMap<String, Double>();
	}
	
	@Override
	public ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
		int i,j;
		StateObserverCube newSO;
        ArrayList<ACTIONS> acts = so.getAvailableActions();
        ACTIONS actBest = null;
        List<Types.ACTIONS> bestActions = new ArrayList<>();
		double[] vTable = new double[acts.size()+1];  
        double minValue = Double.MAX_VALUE;
        double value;

        assert so.isLegalState() : "Not a legal state"; 
        assert so instanceof StateObserverCube : "Not a StateObserverCube object";
        
        for(i = 0; i < acts.size(); ++i)
        {
        	newSO = ((StateObserverCube) so).copy();
        	newSO.advance(acts.get(i));
        	
        	// value is the cost-to-go J(s) for for taking action i in state s='so'. Action i leads to state newSO.
        	value = vTable[i] = stepCost + daviValue(newSO);
        	// Always *minimize* 'value' 
        	if (value==minValue) bestActions.add(acts.get(i));
        	if (value<minValue) {
        		minValue = value;
        		bestActions.clear();
        		bestActions.add(acts.get(i));
        	}
        } // for
        
        // There might be one or more than one action with minValue. 
        // Break ties by selecting one of them randomly:
        actBest = bestActions.get(rand.nextInt(bestActions.size()));

        // optional: print the best action
        if (!silent) {
        	newSO = ((StateObserverCube) so).copy();
        	newSO.advance(actBest);
        	System.out.println("---Best Move: "+newSO.stringDescr()+"   "+minValue);
        }			

        vTable[acts.size()] = minValue;
        
        return new ACTIONS_VT(actBest.toInt(), false, vTable);
	}

	/**
	 * This is the simple version: maintain a full hash table for all visited states. This is only viable for cubes 
	 * with not too large state spaces.
	 * @param so
	 * @return 0, if {@code so} is the solved state, HIGH_J if {@code so} is unknown in the HashMap. In all
	 * 		   other cases, return the HashMap value of {@code so}.
	 */
	public double daviValue(StateObserverCube so) {
		Double dvalue = null;
		if (so.isEqual(def)) return 0;
		String stringRep = so.stringDescr();
		dvalue = jm.get(stringRep); 		// returns null if not in jm
		return (dvalue==null) ? HIGH_J : dvalue;
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
		assert (epiLength != -1) : "trainAgent: Rubik's Cube should not be run with epiLength==-1 !";
		if (so.equals(def)) {
			System.err.println("trainAgent: cube should NOT be the default (solved) cube!");
			return false;
		}
		boolean m_finished = false;
		
		do {
	        m_numTrnMoves++;		// number of train moves 
	        
			a_t = getNextAction2(s_t, true, true);	// choose action a_t (agent-specific behavior)
	        // put the best cost-to-go for state s_t into the HashMap
	        jm.put(s_t.stringDescr(), a_t.getVBest());
	        
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
		return getClass().getSimpleName()+": pMax="+CubeConfig.pMax + ", jm.size="+jm.size(); 
	}

    @Override
	public String stringDescr2() {
		return getClass().getName() + ": pMax="+CubeConfig.pMax + ", epiLength="+m_oPar.getEpisodeLength()
									+ ", jm.size="+jm.size();
	}

}
