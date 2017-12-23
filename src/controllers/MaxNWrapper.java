package controllers;

import java.io.Serializable;

import games.StateObservation;
import tools.Types.ScoreTuple;

/**
 * Wrapper based on {@link MaxNAgent} for n-ply look-ahead in deterministic games
 * 
 * @author Wolfgang Konen, TH Köln, Dec'17
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
	 * Here we use the wrapped {@link PlayAgent} to return a score tuple.
	 * 
	 * @param sob	the state observation
	 * @return		the estimated score tuple
	 */
	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob) {
		return wrapped_pa.getScoreTuple(sob);
		//
		// the following would be too specific to TDNTuple2Agt, we delegate it to *its* 
		// getScoreTuple:
//		boolean rgs = m_oPar.getRewardIsGameScore();
//		ScoreTuple sc = new ScoreTuple(sob);
//		sc = wrapped_pa.getScoreTuple(sob);
//		for (int i=0; i<sob.getNumPlayers(); i++) 
//			sc.scTup[i] += sob.getReward(i, rgs);
//		return sc;
	}
	
	public PlayAgent getWrappedPlayAgent() {
		return wrapped_pa;
	}

	@Override
	public String stringDescr() {
		String cs = wrapped_pa.getClass().getSimpleName();
		cs = cs + "[nPly="+m_depth+"]";
		return cs;
	}

	@Override
	public String getName() {
		String cs = wrapped_pa.getClass().getSimpleName();
		cs = cs + "[nPly="+m_depth+"]";
		return cs;
	}

}
