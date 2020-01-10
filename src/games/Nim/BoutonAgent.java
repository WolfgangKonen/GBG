package games.Nim;

import java.util.ArrayList;
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
		StateObserverNim NewSO;
        ACTIONS actBest = null;
        List<Types.ACTIONS> bestActions = new ArrayList<>();

        assert so.isLegalState() : "Not a legal state"; 
        assert so instanceof StateObserverNim : "Not a StateObserverNim object";

        double maxValue = -1.0;
        double value;
        int player = so.getPlayer();
        
        for(i = 0; i < acts.size(); ++i)
        {
        	NewSO = ((StateObserverNim) so).copy();
        	NewSO.advance(acts.get(i));
        	
        	value = VTable[i] = NewSO.boutonValue(NewSO.getHeaps());
        	// 'value' will be either +1.0 or -1.0 in game Nim.
        	// Always *maximize* 'value' which is the value for the player to move in 'so'.
        	if (value>maxValue) maxValue = value;
        	if (value==1.0) bestActions.add(acts.get(i));
        } // for
        if (maxValue==-1.0) 	// 'so' is a loosing state --> all available actions are equally bad,
        	bestActions = acts;	// thus all are equally possible	
        
        // There might be one or more than one action with maxValue. 
        // Break ties by selecting one of them randomly:
        actBest = bestActions.get(rand.nextInt(bestActions.size()));

        // optional: print the best action
        if (!silent) {
        	NewSO = ((StateObserverNim) so).copy();
        	NewSO.advance(actBest);
        	System.out.println("---Best Move: "+NewSO.stringDescr()+"   "+maxValue);
        }			

        VTable[acts.size()] = maxValue;
        
        return new ACTIONS_VT(actBest.toInt(), false, VTable);
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
	public ScoreTuple getScoreTuple(StateObservation so, ScoreTuple prevTuple) {
        int player = so.getPlayer();
        int opponent = (player==0) ? 1 : 0;
		ScoreTuple sTuple = new ScoreTuple(2);
		sTuple.scTup[player] = this.getScore(so);
		sTuple.scTup[opponent] = -sTuple.scTup[player];
		return sTuple;
	}

	@Override
	public double estimateGameValue(StateObservation so) {
		return so.getGameScore(so.getPlayer());
	}

	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation so, ScoreTuple prevTuple) {
		return getScoreTuple(so, null);
	}

	@Override
	public boolean isTrainable() {
		return false;
	}

}
