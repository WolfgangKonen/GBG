package controllers.TD.ntuple2;

import controllers.TD.ntuple2.TDNTuple2Agt.UpdateType;
import games.StateObservation;
import tools.Types;
import tools.Types.ScoreTuple;

/**
 * Class NextState bundles the different states in state advancing and two different modes of 
 * state advancing. Helper class for {@link TDNTuple2Agt}, {@link TDNTuple2Agt} and {@link SarsaAgt}, both 
 * implementing interface {@link NTupleAgt}.
 * <p>
 * If {@link NTupleAgt#getAFTERSTATE()}==false, then {@code ns=new NextState(so,actBest)}  
 * simply advances {@code so} and lets ns.getAfterState() and ns.getNextSO() return the same next 
 * state so.advance(actBest). 
 * <p>
 * If {@link NTupleAgt#getAFTERSTATE()}==true, then {@code ns=new NextState(so,actBest)}  
 * advances {@code so} in two steps: ns.getAfterState() returns the <b>afterstate s'</b> (after  
 * the deterministic advance (e.g. the merge in case of 2048)) and ns.getNextSO() returns  
 * the next state <b>s''</b> (after adding the nondeterministic part (e.g. adding the random 
 * tile in case of 2048)).
 * <p>
 * The nondeterministic part is done once at the time of constructing an object of class
 * {@code NextState}, so multiple calls to {@code ns.getNextSO()} are guaranteed to return
 * the  same state.
 * <p> 
 * For deterministic games, the behavior is identical to the case with 
 * {@code getAFTERSTATE()==false}: ns.getAfterState() and ns.getNextSO() return the same  
 * next state so.advance(actBest).
 * 
 * @see TDNTuple2Agt#getAFTERSTATE()
 * @see TDNTuple2Agt#trainAgent(StateObservation)
 * @see TDNTuple3Agt#trainAgent(StateObservation)
 * @see SarsaAgt#trainAgent(StateObservation)
 */
public class NextState {
		StateObservation refer;
		StateObservation afterState;
		StateObservation nextSO;
		NTupleAgt tdAgt;		// the 'parent' - used to access the parameters in m_tdPar, m_ntPar

		/**
		 * the reward of next state <b>s''</b> from the perspective of state <b>s</b> = {@code so} 
		 */
		double nextReward;
		
		/**
		 * a ScoreTuple holding  the rewards of 
		 * {@code nextSO} from the perspective of player 0,1,...N-1
		 */
		ScoreTuple nextRewardTuple = null;
		
		/**
		 * Advance state <b>s</b> = {@code so} by action {@code actBest}. Store the afterstate 
		 * <b>s'</b> and the next state <b>s''</b>. 
		 * <p>
		 * Collect the reward(s) for taking action {@code actBest} in state {@code so}.
		 */
		NextState(NTupleAgt parent, StateObservation so, Types.ACTIONS actBest) {
			this.tdAgt = parent;
			refer = so.copy();
			int nPlayers= refer.getNumPlayers();
	        if (tdAgt.getAFTERSTATE()) {   // if checkbox "AFTERSTATE" is checked
	        	
            	// implement it in such a way that StateObservation so is *not* changed -> that is why 
            	// we copy *first* to afterState, then advance:
                afterState = so.copy();
                afterState.advanceDeterministic(actBest);
                nextSO = afterState.copy();
                nextSO.advanceNondeterministic(); 
	        	
	        } else {
                nextSO = so.copy();
                nextSO.advance(actBest);
				afterState = nextSO.copy();
	        }
	        
	        // callback function to set this.nextReward and this.nextRewardTuple 
	        tdAgt.collectReward(this);
	        
		}

		/**
		 * @return the original state <b>s</b> = {@code so}
		 */
		public StateObservation getSO() {
			return refer;
		}

		/**
		 * @return the afterstate <b>s'</b>
		 */
		public StateObservation getAfterState() {
			return afterState;
		}

		/**
		 * @return the next state <b>s''</b> (with random part from environment added).
		 */
		public StateObservation getNextSO() {
			return nextSO;
		}

		/**
		 * @return the reward of next state <b>s''</b> from the perspective of state <b>s</b> = {@code so}
		 */
		public double getNextReward() {
			return nextReward;
		}

		public double getNextRewardCheckFinished(int epiLength) {
			double reward = this.getNextReward();  	// r(s_{t+1}|p_t) for TDNTuple2Agt
													// r(s_{t+1}|p_{t+1}) for SarsaAgt, TDNTuple3Agt
			
			if (nextSO.isGameOver()) {
				tdAgt.setFinished(true);
				
				// only info
				tdAgt.incrementWinCounters(reward,this);
			}

			tdAgt.incrementMoveCounter();
			if (tdAgt.getMoveCounter()==epiLength) {
				reward=tdAgt.estimateGameValue(nextSO);
				tdAgt.setFinished(true); 
			}
			
			return reward;
		}

		/**
		 * @return the tuple of rewards of {@code nextSO} from the perspective of player 0,1,...N-1
		 */
		public ScoreTuple getNextRewardTuple() {
			return nextRewardTuple;
		}

		public ScoreTuple getNextRewardTupleCheckFinished(int epiLength) {
			ScoreTuple rewardTuple = this.getNextRewardTuple();
			
			if (nextSO.isGameOver()) {
				tdAgt.setFinished(true);
				
				// only info
				tdAgt.incrementWinCounters(rewardTuple.scTup[nextSO.getPlayer()],this);
			}

			tdAgt.incrementMoveCounter();
			if (tdAgt.getMoveCounter()>=epiLength) {
				rewardTuple=tdAgt.estimateGameValueTuple(nextSO);
				tdAgt.setFinished(true); 
			}
			
			return rewardTuple;
		}


} // class NextState
