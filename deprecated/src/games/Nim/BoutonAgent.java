package games.Nim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import games.StateObservation;
import params.ParOther;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_ST;
import tools.Types.ACTIONS_VT;
import tools.Types.ScoreTuple;

/**
 *  Implements the perfect player for game Nim according to Bouton's theory.
 *  <p>
 *  The relevant method to calculate a state's value is in
 *  {@link StateObserverNim#boutonValue(int[])}.
 *
 */
public class BoutonAgent extends AgentBase implements PlayAgent {

	private static final long serialVersionUID = 12L;

	private Random rand;

	public BoutonAgent(String name) {
		super(name);
		super.setMaxGameNum(1000);		
		super.setGameNum(0);		
		super.setAgentState(AgentState.TRAINED);
        rand = new Random(System.currentTimeMillis());
	}
	
	@Override
	public ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
		int i,j;
        ArrayList<ACTIONS> acts = so.getAvailableActions();
		double[] VTable = new double[acts.size()+1];  
//		ScoreTuple currScoreTuple=new ScoreTuple(2);
//      ScoreTuple scBest=new ScoreTuple(so);		// make a new ScoreTuple, which starts by default with the lowest possible maxValue
//      ACTIONS_ST act_best = null;
//    	String stringRep ="";
		StateObserverNim NewSO;
        ACTIONS actBest = null;
        List<Types.ACTIONS> bestActions = new ArrayList<>();

        assert so.isLegalState() : "Not a legal state"; 
        assert so instanceof StateObserverNim : "Not a StateObserverNim object";

        double maxValue = -1.0;
        double value;
        int player = so.getPlayer();
//  	int opponent = (player==0) ? 1 : 0;
        
        for(i = 0; i < acts.size(); ++i)
        {
        	NewSO = ((StateObserverNim) so).copy();
        	NewSO.advance(acts.get(i));
        	
        	value = VTable[i] = NewSO.boutonValue(NewSO.getHeaps());
        	// 'value' will be either +1.0 or -1.0 in game Nim.
        	// Always *maximize* 'value' which is the value for the player to move in 'so'.
        	if (value>maxValue) maxValue = value;
        	if (value==1.0) bestActions.add(acts.get(i));
            // --- old version, too complicated ---
//			currScoreTuple.scTup[player] = NewSO.boutonValue(NewSO.getHeaps());
//			currScoreTuple.scTup[opponent] = -currScoreTuple.scTup[player];
//			
//        	VTable[i] = currScoreTuple.scTup[player];
//			
//			// always *maximize* P's element in the tuple currScoreTuple, 
//			// where P is the player to move in state so:
//			ScoreTuple.CombineOP cOP = ScoreTuple.CombineOP.MAX;
//			scBest.combine(currScoreTuple, cOP, player, 0.0);            	
        } // for
        if (maxValue==-1.0) 	// 'so' is a loosing state --> all available actions are equally bad,
        	bestActions = acts;	// thus all are equally possible	
        
        // There might be one or more than one action with maxValue. 
        // Break ties by selecting one of them randomly:
        actBest = bestActions.get(rand.nextInt(bestActions.size()));
        // --- old version, too complicated ---
//    	int selectJ = (int)(rand.nextDouble()*scBest.count);
//    	//System.out.println(selectJ + " of " + scBest.count);
//    	maxValue = scBest.scTup[player];
//    	for (i=0, j=0; i < acts.size(); ++i) {
//    		if (VTable[i]==maxValue) {
//    			if ((j++)==selectJ) actBest = new ACTIONS(acts.get(i));
//    		}
//    	}
//    	assert actBest != null : "Oops, no best action actBest";

        // optional: print the best action
        if (!silent) {
        	NewSO = ((StateObserverNim) so).copy();
        	NewSO.advance(actBest);
        	System.out.print("---Best Move: "+NewSO.stringDescr()+"   "+maxValue);
        }			

        VTable[acts.size()] = maxValue;
        actBest.setRandomSelect(false);
//        act_best = new ACTIONS_ST(actBest, scBest);
		
        return new ACTIONS_VT(actBest.toInt(), actBest.isRandomAction(), VTable);
	}

	@Override
	public double getScore(StateObservation so) {
        assert (so instanceof StateObserverNim) : "Not a StateObserverNim object";
		StateObserverNim soN = (StateObserverNim) so;
		return -soN.boutonValue(soN.getHeaps());
		// Why "-"? - boutonValue returns the value of soN for the player who generated
		// soN, but getScore() returns the value of soN for the player who has to move on soN.
		// This value is just the opposite of boutonValue.
	}

	@Override
	public ScoreTuple getScoreTuple(StateObservation so) {
        int player = so.getPlayer();
        int opponent = (player==0) ? 1 : 0;
		ScoreTuple sTuple = new ScoreTuple(2);
		sTuple.scTup[player] = this.getScore(so);
		sTuple.scTup[opponent] = -sTuple.scTup[player];
		return sTuple;
	}

	@Override
	public double estimateGameValue(StateObservation so) {
		return getScore(so);
	}

	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation so) {
		return getScoreTuple(so);
	}

	@Override
	public boolean isTrainable() {
		return false;
	}

	// --- take default implementation from AgentBase ---
//	@Override
//	public String stringDescr() {
//		String cs = getClass().getSimpleName();
//		return cs;
//	}
//
//	@Override
//	public String stringDescr2() {
//		return getClass().getName() + ":";
//	}

//	@Override
//	public int getMaxGameNum() {
//		return 0;
//	}

//	@Override
//	public int getGameNum() {
//		return 0;
//	}

//	@Override
//	public void setMaxGameNum(int num) {
//
//	}

//	@Override
//	public void setGameNum(int num) {
//	}

//	@Override
//	public AgentState getAgentState() {
//		return m_agentState;
//	}
//
//	@Override
//	public void setAgentState(AgentState aState) {
//		m_agentState = aState;
//	}

//	@Override
//	public String getName() {
//		return m_name;
//	}
//
//	@Override
//	public void setName(String name) {
//		m_name = name;
//	}

}
