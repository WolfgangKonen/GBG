package controllers;

import java.io.Serializable;

import games.StateObservation;
import tools.Types.ScoreTuple;

/**
 * Wrapper based on {@link MaxNAgent} for n-ply look-ahead in nondeterministic games
 * 
 * @author Wolfgang Konen, TH K�ln, Dec'17
 * 
 * @see MaxNAgent
 */
public class MaxNWrapper extends MaxNAgent implements Serializable {
	private PlayAgent wrapped_pa;
	
	public MaxNWrapper(PlayAgent pa, int nply) {
		super("MaxNWrapper", nply);
		this.wrapped_pa = pa;
	}
	
	/**
	 * When the recursion tree has reached its maximal depth m_depth, then return
	 * an estimate of the game score (tuple for all players).  
	 * <p>
	 * Here we use the wrapper {@link PlayAgent} to return a score tuple.
	 * 
	 * @param sob	the state observation
	 * @return		the estimated score tuple
	 */
	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob) {
		boolean rgs = m_oPar.getRewardIsGameScore();
		ScoreTuple sc = new ScoreTuple(sob);
		sc = wrapped_pa.getScoreTuple(sob);
		for (int i=0; i<sob.getNumPlayers(); i++) 
			sc.scTup[i] += sob.getReward(i, rgs);
		return sc;
	}
	
	public PlayAgent getWrappedPlayAgent() {
		return wrapped_pa;
	}

}