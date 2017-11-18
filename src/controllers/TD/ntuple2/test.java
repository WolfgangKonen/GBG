package controllers.TD.ntuple2;

import controllers.TD.ntuple2.TDNTuple2Agt.NextState;
import games.StateObservation;

public class test {
	/**
	 * This is for the new target logic, but for {@link VER_3P}=false (i.e. only for 1- and 2-player games)
	 * 
	 * @return reward
	 */
	private double trainNewTargetLogic2(NextState ns,
			int[] curBoard, 
			boolean learnFromRM, int epiLength,  
			double oldReward, NTuple2ValueFunc my_Net) 
	{
		double reward;
		
		StateObservation thisSO=ns.getSO();
		StateObservation nextSO=ns.getNextSO();
		int[] nextBoard = m_Net.xnf.getBoardVector(ns.getAfterState());
		int thisPlayer= thisSO.getPlayer();
		int nextPlayer= nextSO.getPlayer();
		
//		assert (VER_3P==false);
		
		//reward = fetchReward(nextSO,thisSO,Types.PLAYER_PM[thisSO.getPlayer()]);
		reward = ns.getNextReward();
		
		if (nextSO.isGameOver()) {
			m_finished = true;
		}

		m_counter++;
		if (m_counter==epiLength) {
			reward=estimateGameValue(nextSO);
			//epiCount++;
			m_finished = true; 
		}
		
		if (m_randomMove && !learnFromRM) {
			// no training, go to next move.
			if (m_DEBG)  // only for diagnostics:
				pstream.println("random move");
		} else {
			// do one training step (NEW target)
			if (curBoard!=null) {
				double target;
				int sign=1;
				if (NEW_2P==true) sign=-1;
				// target is (reward + GAMMA * value of the after-state) for non-final states
				target = reward-oldReward + sign*getGamma() * my_Net.getScoreI(nextBoard,nextPlayer);
				my_Net.updateWeightsNew(curBoard, thisPlayer, nextBoard, nextPlayer,
						reward-oldReward,target,thisSO);
			}
		}
		
		return reward;
		
	} 

}
