package games.Nim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import games.StateObservation;
import params.ParOther;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_ST;
import tools.Types.ACTIONS_VT;
import tools.Types.ScoreTuple;

public class BoutonAgent extends AgentBase implements PlayAgent {

	private static final long serialVersionUID = 12L;

//	private AgentState m_agentState = AgentState.TRAINED;
//	private String m_name = "BoutonAgent";
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
		ScoreTuple currScoreTuple=new ScoreTuple(2);
        ScoreTuple scBest=null;
		StateObserverNim NewSO;
        ACTIONS actBest = null;
        ACTIONS_ST act_best = null;
        String stringRep ="";

        assert so.isLegalState() : "Not a legal state"; 
        assert so instanceof StateObserverNim : "Not a StateObserverNim object";

        double pMaxScore = -Double.MAX_VALUE;
    	scBest=new ScoreTuple(so);		// make a new ScoreTuple, which starts by default with the lowest possible maxValue
        int player = so.getPlayer();
        int opponent = (player==0) ? 1 : 0;
        
        for(i = 0; i < acts.size(); ++i)
        {
        	NewSO = ((StateObserverNim) so).copy();
        	NewSO.advance(acts.get(i));
        	
			currScoreTuple.scTup[player] = NewSO.boutonValue(NewSO.getHeaps());
			currScoreTuple.scTup[opponent] = -currScoreTuple.scTup[player];
			
			
        	VTable[i] = currScoreTuple.scTup[player];
			
			// always *maximize* P's element in the tuple currScoreTuple, 
			// where P is the player to move in state so:
			ScoreTuple.CombineOP cOP = ScoreTuple.CombineOP.MAX;
			scBest.combine(currScoreTuple, cOP, player, 0.0);            	
        } // for
        
        // There might be one or more than one action with pMaxScore. 
        // Break ties by selecting one of them randomly:
    	int selectJ = (int)(rand.nextDouble()*scBest.count);
    	//System.out.println(selectJ + " of " + scBest.count);
    	pMaxScore = scBest.scTup[player];
    	for (i=0, j=0; i < acts.size(); ++i) {
    		if (VTable[i]==pMaxScore) {
    			if ((j++)==selectJ) actBest = new ACTIONS(acts.get(i));
    		}
    	}

        assert actBest != null : "Oops, no best action actBest";
        // optional: print the best action
        if (!silent) {
        	NewSO = ((StateObserverNim) so).copy();
        	NewSO.advance(actBest);
        	System.out.print("---Best Move: "+NewSO.stringDescr()+"   "+pMaxScore);
        }			

        VTable[acts.size()] = pMaxScore;
        actBest.setRandomSelect(false);
        act_best = new ACTIONS_ST(actBest, scBest);
		
        return new ACTIONS_VT(act_best.toInt(), act_best.isRandomAction(), VTable);
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
		sTuple.scTup[player] = getScore(so);
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
	public boolean trainAgent(StateObservation so) {
		return false;
	}

	@Override
	public String printTrainStatus() {
		return "BoutonAgent::printTrain"; // dummy stub
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

	@Override
	public byte getSize() {
		return 1;  // dummy stub (for size of agent, see LoadSaveTD.saveTDAgent)
	}

//	@Override
//	public int getMaxGameNum() {
//		// TODO Auto-generated method stub
//		return 0;
//	}

//	@Override
//	public int getGameNum() {
//		// TODO Auto-generated method stub
//		return 0;
//	}

	@Override
	public long getNumLrnActions() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getNumTrnMoves() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMoveCounter() {
		// TODO Auto-generated method stub
		return 0;
	}

//	@Override
//	public void setMaxGameNum(int num) {
//		// TODO Auto-generated method stub
//
//	}

//	@Override
//	public void setGameNum(int num) {
//		// TODO Auto-generated method stub
//
//	}

	@Override
	public ParOther getParOther() {
		return m_oPar;		// AgentBase::m_oPar
	}

	@Override
	public int getNumEval() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setNumEval(int num) {
		// TODO Auto-generated method stub
	}

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
