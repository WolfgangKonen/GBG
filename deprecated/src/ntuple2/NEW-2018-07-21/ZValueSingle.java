package controllers.TD.ntuple2;

import java.util.ArrayList;
import java.util.List;

import games.StateObservation;
import games.CFour.StateObserverC4;
import tools.Types;

/**
 * Helper class for {@link TDNTuple2Agt}: calculate the Z-value in the case of SINGLE_UPDATE,
 * nply=1.
 * (This class is not strictly necessary, it is the same as ZValueSingleNPly with nply=1.)
 *
 * @author wolfgang
 */
public class ZValueSingle implements ZValue {
	private TDNTuple2Agt tdnt;
	
	public ZValueSingle(TDNTuple2Agt tdntref) {
		this.tdnt=tdntref;
	}
	
	/**
	 * Calculate the Z-value r+gamma*V(s')
	 * (function EVALUATE in TR-TDNTuple.pdf, Algorithm 6). <br>
	 * r is the reward received when applying action {@code act} to state {@code so} and
	 * V(s') is the expected cumulative future reward for afterstate s'({@code so},{@code act}).
	 * 
	 * @param so
	 * @param act
	 * @param refer		rewards are calculated with reference to this state {@code refer} 
	 * @param silent
	 * @return
	 * 
	 * @see TDNTuple2Agt#getNextAction3(StateObservation, StateObservation, boolean, boolean)
	 */
    public double calculate(   StateObservation so, Types.ACTIONS act, 
    							StateObservation refer, boolean silent) {
    	double CurrentScore,agentScore;
    	double g3BestScore = -Double.MAX_VALUE;
		int rplyer = refer.getPlayer();
		boolean rgs = tdnt.getParOther().getRewardIsGameScore();

        double referReward = refer.getReward(refer,rgs); // 0; 
        double rtilde,otilde;
        double kappa;

    	StateObservation NewSO = so.copy();
        
    	// --- this is now handled in getNextAction3 directly ---
		if (tdnt.randomSelect) {
			CurrentScore = tdnt.rand.nextDouble();
			return CurrentScore;
		} 
		
        if (tdnt.getAFTERSTATE()) {
        	NewSO.advanceDeterministic(act); 	// the afterstate
        	agentScore = tdnt.getScore(NewSO,NewSO); // this is V(s')
            NewSO.advanceNondeterministic(); 
        } else { 
        	// the non-afterstate logic for the case of single moves:
        	//System.out.println("NewSO: "+NewSO.stringDescr()+", act: "+act.toInt()); // DEBUG
            NewSO.advance(act);
        	agentScore = tdnt.getScore(NewSO,NewSO); // this is V(s'')
        }
        // both ways of calculating the agent score are the same for deterministic games (s'=s''),
        // but they usually differ for nondeterministic games.
        
        otilde = so.getReward(refer,rgs);
        rtilde = NewSO.getReward(refer,rgs)-otilde;
    	kappa = (NewSO.getPlayer()==refer.getPlayer()) ? +1 : -1;
        
    	boolean TST_VERSION=false;
    	if (TST_VERSION) {
    		// this longer debug version is only to make some extra assertions for C4
    		// or other 'only-final-reward' games
	        if (NewSO.isGameOver()) {
	        	if (NewSO instanceof StateObserverC4) 
	        		assert otilde==0 : "Oops, otilde is not zero!";  
	        		// the state before the game-over state NewSO should have no reward
	        	
	        	return rtilde+kappa*tdnt.getGamma()*agentScore;		// game over, terminate for-loop
	        }
        	if (NewSO instanceof StateObserverC4) 
	        	assert rtilde==0 : "Oops, rtilde is not zero!";  
        		// an in-game state should have no reward	        	

        	return rtilde+kappa*tdnt.getGamma()*agentScore;		// normal return 1-ply
    	} else {
        	if (tdnt.TERNARY) {
        		return NewSO.isGameOver() ? rtilde : kappa*tdnt.getGamma()*agentScore;
        	}
        	return rtilde+kappa*tdnt.getGamma()*agentScore;		// normal return 1-ply
    	}
		
    } 
		
}
