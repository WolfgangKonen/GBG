package controllers.TD.ntuple2;

import java.util.ArrayList;
import java.util.List;

import games.StateObservation;
import games.CFour.StateObserverC4;
import tools.Types;

/**
 * Helper class for {@link TDNTuple2Agt}: calculate the Z-value in the case of SINGLE_UPDATE,
 * arbitrary nply.
 *
 * @author wolfgang
 */
public class ZValueSingleNPly implements ZValue {
	private TDNTuple2Agt tdnt;
	private int nply;
	
	public ZValueSingleNPly(TDNTuple2Agt tdntref, int nply) {
		this.tdnt=tdntref;
		this.nply=nply;
	}
	
    // calculate CurrentScore: 
	// g3_Eval_NPly is helper function for getNextAction3, if SINGLE_UPDATE, i.e. 1) MODE_3P==1 or 
    // 2) (MODE_3P==2 && N=2). In case 2) we have nply=1. 
    // (g3_Eval_NPly is function EVALUATENPLY in TR-TDNTuple.pdf, Algorithm 2)
	/**
	 * Calculate the Z-value r+gamma*V(s')
	 * (function EVALUATE in TR-TDNTuple.pdf, Algorithm 6). <br>
	 * r is the reward received when applying action {@code act} to state {@code so} and
	 * V(s') is the expected cumulative future reward for afterstate s'({@code so},{@code act}).
	 * 
	 * @param so
	 * @param act
	 * @param refer		rewards are calculated with reference to this state {@code refer} 
	 * @param nply
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
    	StateObservation NewSO;
    	StateObservation oldSO = so.copy();

		if (tdnt.randomSelect) {
			CurrentScore = tdnt.rand.nextDouble();
			return CurrentScore;
		} 
		
		for (int j=1; j<=nply; j++) {
	        NewSO = oldSO.copy();
	        
	        if (tdnt.getAFTERSTATE()) {
	        	NewSO.advanceDeterministic(act); 	// the afterstate
	        	agentScore = tdnt.getScore(NewSO,NewSO); // this is V(s')
	            NewSO.advanceNondeterministic(); 
	        } else { 
	        	// the non-afterstate logic for the case of single moves:
            	//System.out.println("NewSO: "+NewSO.stringDescr()+", act: "+act.toInt()+", j(nply)="+j); // DEBUG
	            NewSO.advance(act);
	        	agentScore = tdnt.getScore(NewSO,NewSO); // this is V(s'')
	        }
	        // both ways of calculating the agent score are the same for deterministic games (s'=s''),
	        // but they usually differ for nondeterministic games.
	        
	        //otilde = oldSO.getReward(oldSO,rgs);
	        //  -- the above line was a possible bug, it should be ...getReward(refer,...) always --
	        otilde = oldSO.getReward(refer,rgs);
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
    	        if (j==nply) {
    	        	if (NewSO instanceof StateObserverC4) 
    		        	assert rtilde==0 : "Oops, rtilde is not zero!";  
    	        		// an in-game state should have no reward	        	

    	        	return rtilde+kappa*tdnt.getGamma()*agentScore;		// normal return (n-ply recursion)
    	        }
        	} else {
    	        if (NewSO.isGameOver() || j==nply) {					// game over or n-ply-recursion over, 
    	        														// --> terminate for-loop
    	        	if (tdnt.TERNARY) {
    	        		return NewSO.isGameOver() ? rtilde : kappa*tdnt.getGamma()*agentScore;
    	        	}
    	        	return rtilde+kappa*tdnt.getGamma()*agentScore;		
    	        }
        	}
			
        	//
        	// we get here only in case nply>1 (and j<nply) :
        	//
        	
	        // find the best action for NewSO's player by
	        // maximizing the return from evalNPly
	        ArrayList<Types.ACTIONS> acts = NewSO.getAvailableActions();

	        assert acts.size()>0 : "Oops, no available action";
	        g3BestScore = -Double.MAX_VALUE;		// bug fix!
	        List<Types.ACTIONS> nextActions = new ArrayList<>();
	        for(int i = 0; i < acts.size(); ++i)
	        {
	            CurrentScore = evalNPly(NewSO,acts.get(i), rgs); 
	            
				//
				// Calculate g3BestScore and best action act.
				// If there are multiple best actions, select afterwards one of them randomly 
				// (better exploration)
				//
				if (g3BestScore < CurrentScore) {
					g3BestScore = CurrentScore;
	                nextActions.clear();
	                nextActions.add(acts.get(i));
				} else if (g3BestScore == CurrentScore) {
	                nextActions.add(acts.get(i));
				}
	        } // for (i)
	        act = nextActions.get(tdnt.rand.nextInt(nextActions.size()));
	        
	        oldSO = NewSO.copy();
	        
		} // for (j)
		
		throw new RuntimeException("ZValueSingleNPly: we should not arrive here!");
		// the return should happen in last pass through for-j-loop ('if (j==nply)')

    } // calculate

		
    // helper function for calculate
    // (function EVAL in TR-TDNTuple.pdf, Algorithm 2)
    private double evalNPly(StateObservation s_v, Types.ACTIONS a_v, boolean rgs) {
    	double agentScore,kappa,reward;
    	StateObservation NewSO;
    	
    	NewSO = s_v.copy();
        
        if (tdnt.getAFTERSTATE()) {
        	NewSO.advanceDeterministic(a_v); 	// the afterstate
        	agentScore = tdnt.getScore(NewSO,NewSO); // this is V(s')
            NewSO.advanceNondeterministic(); 
        } else { 
        	// the non-afterstate logic for the case of single moves:
            NewSO.advance(a_v);
        	agentScore = tdnt.getScore(NewSO,NewSO); // this is V(s'')
        }
        // both ways of calculating the agent score are the same for deterministic games (s'=s''),
        // but they usually differ for nondeterministic games.
        
    	kappa = (NewSO.getPlayer()==s_v.getPlayer()) ? +1 : -1;
    	reward = (NewSO.getReward(s_v,rgs)-s_v.getReward(s_v,rgs));
    	return reward + kappa*tdnt.getGamma()*agentScore;
    }

}
