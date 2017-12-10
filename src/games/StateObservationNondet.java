package games;

import java.util.ArrayList;

import tools.Types;
import tools.Types.ACTIONS;

/**
 * Class {@link StateObservationNondet} is used to implement <b>nondeterministic</b> games 
 * (like 2048). It extends the normal {@link StateObservation} interface.<p>
 * 
 * @see StateObservation
 * 
 * @author Johannes Kutsch, Wolfgang Konen, TH Köln, Feb'17
 */
public interface StateObservationNondet extends StateObservation {
    /**
     * Advance the current state to a new afterstate (do the deterministic part of advance)
     *
     * @param action    the action
     */
    public void advanceDeterministic(ACTIONS action);

    /**
     * Advance the current afterstate to a new state (do the nondeterministic part of advance)
     */
    public void advanceNondeterministic();

    /**
     * Advance the current afterstate to a new state (with a specific nondeterministic action)
     * @param action   the nondeterministic action
     */
    public void advanceNondeterministic(ACTIONS action);
    
    public boolean isNextActionDeterministic();
    
    /**
     * Get the next nondeterministic action
     *
     * @return the action, null if the next action is deterministic
     */
    public ACTIONS getNextNondeterministicAction();

	public ArrayList<ACTIONS> getAvailableRandoms();

	public int getNumAvailableRandoms();
	
	public double getProbability(ACTIONS action);

	public StateObservationNondet copy();
}
