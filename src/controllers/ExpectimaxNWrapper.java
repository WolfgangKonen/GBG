package controllers;

import java.io.Serializable;

import games.StateObservation;
import tools.ScoreTuple;
import tools.Types;

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
	 * @param sob		the state observation
	 * @param prevTuple	the previous score tuple
	 * @return			the tuple of estimated scores
	 */
	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
		return newEstimateGameValueTuple(sob,prevTuple);

//		return wrapped_pa.getScoreTuple(sob, prevTuple);		// /WK/ 2021-09-10: old and flawed
	}

	private ScoreTuple newEstimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
		// this is just for safety: if sob needs a nondeterministic next move, take a random one from the environment
		if (!sob.isNextActionDeterministic()) {
			System.err.println("WARNING: estimateGameVauleTuple called with an sob that has next action = non-deterministic!");
			sob.advanceNondeterministic();
			while(!sob.isNextActionDeterministic() && !sob.isRoundOver()){		// /WK/03/2021 NEW
				sob.advanceNondeterministic();
			}
			if (sob.isGameOver()) return sob.getGameScoreTuple();
		}

		Types.ACTIONS_VT actBest = wrapped_pa.getNextAction2(sob,false,true);
		return actBest.getScoreTuple();

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
