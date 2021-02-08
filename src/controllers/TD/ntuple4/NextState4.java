package controllers.TD.ntuple4;

import games.StateObservation;
import tools.ScoreTuple;
import tools.Types;

/**
 * Class NextState bundles the different states in state advancing and two different modes of 
 * state advancing. Helper class for {@link TDNTuple4Agt} and {@link Sarsa4Agt}, all
 * three implementing interface {@link NTuple4Agt}.
 * <p>
 * If {@link NTuple4Agt#getAFTERSTATE()}==false, then {@code ns=new NextState(so,actBest)}
 * simply advances {@code so} and lets ns.getAfterState() and ns.getNextSO() return the same next 
 * state so.advance(actBest). 
 * <p>
 * If {@link NTuple4Agt#getAFTERSTATE()}==true, then {@code ns=new NextState(so,actBest)}
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
 * @see TDNTuple4Agt#trainAgent(StateObservation)
 * @see Sarsa4Agt#trainAgent(StateObservation)
 */
public class NextState4 {
		StateObservation refer;
		StateObservation afterState;
		StateObservation nextSO;
		NTuple4Agt tdAgt;		// the 'parent' - used to access the parameters in m_tdPar, m_ntPar

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
		NextState4(NTuple4Agt parent, StateObservation so, Types.ACTIONS actBest) {
			this.tdAgt = parent;
			refer = so.copy();
	        if (tdAgt.getAFTERSTATE()) {   // if checkbox "AFTERSTATE" is checked
	        	
            	// implement it in such a way that StateObservation so is *not* changed -> that is why 
            	// we copy *first* to afterState, then advance:
                afterState = so.copy();
                afterState.advanceDeterministic(actBest);
                nextSO = afterState.copy();
				// /WK/ commented out since isNextActionDeterministic() is not yet part of the general interface
				//while(!nextSO.isNextActionDeterministic() && !isRoundOver()) {
					nextSO.advanceNondeterministic();
				//}
	        	
	        } else {
                nextSO = so.copy();
                nextSO.advance(actBest);
				afterState = nextSO.copy();
	        }

	        // callback function to set this.nextReward and this.nextRewardTuple 
	        tdAgt.collectReward(this);

			if (nextSO.isRoundOver() && !nextSO.isGameOver()) nextSO.initRound();
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
													// r(s_{t+1}|p_{t+1}) for Sarsa4Agt, TDNTuple4Agt
			
			if (nextSO.isGameOver()) {
				tdAgt.setFinished(true);
				
				// only info
				tdAgt.incrementWinCounters(reward,this);
			}

			tdAgt.incrementMoveCounter();
			if (tdAgt.getMoveCounter()==epiLength) {
				reward=tdAgt.estimateGameValueTuple(nextSO, null).scTup[nextSO.getPlayer()];
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
//				if (!(nextSO instanceof StateObserverCube)) {
//					// for RubiksCube it would be wrong to return estimateGameValueTuple on a failed episode, we just
//					// return this.getNextRewardTuple() (usually the 0-tuple).
//					//
//					// We comment it out for other games as well, since no other game works with a maximum epi length
//					// so we will never arrive here
//					rewardTuple = tdAgt.estimateGameValueTuple(nextSO, null);
//				}
				tdAgt.setFinished(true);
				tdAgt.setEpiLengthStop(true);
			}
			
			return rewardTuple;
		}


} // class NextState
