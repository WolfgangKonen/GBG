package controllers.TD.ntuple2;

import games.StateObservation;
import tools.Types;

/**
 * Helper class for {@link TDNTuple2Agt}: calculate the Z-value in the case of MULTI_UPDATE
 *
 * @author wolfgang
 */
public class ZValueMulti implements ZValue {
	private TDNTuple2Agt tdnt;
	
	public ZValueMulti(TDNTuple2Agt tdntref) {
		tdnt=tdntref;
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
    public double calculate(	StateObservation so, Types.ACTIONS act, 
    							StateObservation refer, boolean silent) {
    	double CurrentScore,agentScore;
		int rplayer = refer.getPlayer();
		boolean rgs = tdnt.getParOther().getRewardIsGameScore();

        double referReward = refer.getReward(refer,rgs); // 0; 
    	StateObservation NewSO;
    	StateObservation refer2 = refer; 
        double kappa = 1.0;
    			
		if (tdnt.randomSelect) {
			CurrentScore = tdnt.rand.nextDouble();
			return CurrentScore;
		} 
		
        NewSO = so.copy();
    	if (tdnt.VER_3P==true && tdnt.MODE_3P==2 && so.getNumPlayers()==2) {
    		refer2 = NewSO;
        	kappa = (NewSO.getPlayer()==refer.getPlayer()) ? +1.0 : -1.0;
    	}
    	// This will normally result in kappa = -1.0. Only in the case of multi-moves, 
    	// where NewSO.getPlayer()==so.getPlayer() can happen, we may have kappa = +1.0.
        
        if (tdnt.getAFTERSTATE()) {
        	NewSO.advanceDeterministic(act); 	// the afterstate
        	agentScore = tdnt.getScore(NewSO,refer2); // this is V(s')
            NewSO.advanceNondeterministic(); 
        } else { 
        	// the non-afterstate logic for the case of single moves:
            NewSO.advance(act);
        	agentScore = tdnt.getScore(NewSO,refer2); // this is V(s'')
        }
        // both ways of calculating the agent score are the same for deterministic games (s'=s''),
        // but they usually differ for nondeterministic games.
        
        boolean CUBE_DBG=false;
        if (CUBE_DBG && !tdnt.getParNT().getUSESYMMETRY()) {
        	System.out.println(so + " <|> " +NewSO + " : " + agentScore);
        }
        
        agentScore *= kappa; 
        // kappa is -1 (and not 1) only for VER_3P==true && MODE_3P==2 && so.getNumPlayers()==2 
        // and only if NewSO and refer have different players.


        // the recursive part (only for deterministic games) is for the case of 
        // multi-moves: the player who just moved gets from StateObservation 
        // the signal for one (or more) additional move(s)
        if (so.isDeterministicGame() && so.getNumPlayers()>1 && !NewSO.isGameOver()) {
            if (NewSO.getPlayer()==rplayer) {
            	Types.ACTIONS_VT actBestVT = tdnt.getNextAction3(NewSO, refer, false, silent);
            	NewSO.advance(actBestVT);
            	CurrentScore = actBestVT.getVBest();
            	return CurrentScore;
            }
        }
	            
		int playerPM = tdnt.calculatePlayerPM(refer); 	
		// new target logic:
		// the score is the reward received for the transition from refer to NewSO 
		// 		(NewSO.getReward(refer)-referReward)
		// plus the estimated future rewards until game over (agentScore=getScore(NewSO), 
		// the agent's value function for NewSO)
		double rtilde = (NewSO.getReward(refer,rgs) - referReward);
    	if (tdnt.TERNARY) {
    		CurrentScore = (NewSO.isGameOver() ? rtilde : tdnt.getGamma()*playerPM*agentScore);
    	}
    	CurrentScore =  rtilde+tdnt.getGamma()*playerPM*agentScore;		

		if (!silent || tdnt.DBG_REWARD) {
			//System.out.println(NewSO.stringDescr()+", "+(2*CurrentScore*playerPM-1));
			System.out.println(NewSO.stringDescr()+", "+CurrentScore+", "+rtilde);
			//print_V(Player, NewSO.getTable(), 2 * CurrentScore * Player - 1);
		}

		return CurrentScore;
    }  // calculate


}
