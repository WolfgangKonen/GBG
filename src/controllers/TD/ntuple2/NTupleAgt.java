package controllers.TD.ntuple2;

import games.StateObservation;
import params.ParNT;
import params.ParTD;
import tools.ScoreTuple;

/**
 * Interface NTupleAgt is needed to make {@link NTuple2ValueFunc} and {@link NextState} usable for 
 * all agents {@link TDNTuple3Agt} and {@link SarsaAgt}.
 */
abstract public interface NTupleAgt {
	
	/**
	 * EligType characterizes the eligibility trace mode: <ul>
	 * <li> <b>STANDARD</b>: nothing happens on random move (RM) with the elig traces (only no update on RM)
	 * <li> <b>RESET</b>: all elig traces are reset after RM (because a later 
	 * 		reward has no meaning for states prior to RM)
	 * </ul>
	 * @see NTuple2ValueFunc#clearEligList(EligType)
	 */
	enum EligType {STANDARD, RESET}; // may later contain also REPLACE, RESREP

	// methods needed in class NTuple2ValueFunc:
	public ParTD getParTD();
	public ParNT getParNT();
	
	// methods needed in class NextState:
	public boolean getAFTERSTATE();
	//public double estimateGameValue(StateObservation sob);
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple);
	public int getMoveCounter();
	// the compiler requires that the following four methods are public, although
	// they should be used only from class NextState (and not by anybody else):
	public void incrementMoveCounter(); 
	public void incrementWinCounters(double reward, NextState ns);
	public void collectReward(NextState ns);
	public void setFinished(boolean f);
	public void setEpiLengthStop(boolean f);
}
