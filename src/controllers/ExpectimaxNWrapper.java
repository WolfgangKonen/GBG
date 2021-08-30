package controllers;

import java.io.Serializable;

import games.StateObservation;
import tools.ScoreTuple;

/**
 * Wrapper based on {@link ExpectimaxNAgent} for n-ply look-ahead in nondeterministic games.
 * Wrap agent {@code pa} into an {@link ExpectimaxNAgent} with {@code nply} plies look-ahead.
 * Override {@link #estimateGameValueTuple(StateObservation, ScoreTuple)} such that it returns the score tuple
 * of the wrapped agent {@code pa}.
 * 
 * @author Wolfgang Konen, TH Koeln, 2017
 * 
 * @see ExpectimaxNAgent
 */
public class ExpectimaxNWrapper extends ExpectimaxNAgent implements Serializable {
	private final PlayAgent wrapped_pa;
	
	public ExpectimaxNWrapper(PlayAgent pa, int nply) {
		super("ExpectimaxWrapper", nply);
		this.m_oPar.setWrapperNPly(nply);
		this.wrapped_pa = pa;
	}
	
	/**
	 * When the recursion tree has reached its maximal depth m_depth, then return
	 * an estimate of the game score (tuple for all players).  
	 * <p>
	 * Here we use the wrapped {@link PlayAgent} to return a tuple of game values.
	 * 
	 * @param sob	the state observation
	 * @return		the tuple of estimated score 
	 */
	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
		return wrapped_pa.getScoreTuple(sob, prevTuple);
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

	public String getShortName() {
		return "EW";
	}

	// getName: use method ObserverBase::getName()

	// --- never used ---
//	public String getFullName() {
//		String cs = wrapped_pa.getClass().getSimpleName();
//		cs = cs + "[nPly="+m_depth+"]";
//		return cs;
//	}

}
