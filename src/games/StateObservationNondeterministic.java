package games;

import tools.Types;

/**
 * Class StateObservationNondeterministic is used to implement <b>nondeterministic</b> games 
 * (like 2048). It extends the normal {@link StateObservation} interface.<p>
 * 
 * @see StateObservation
 * 
 * @author Johannes Kutsch, Wolfgang Konen, TH Koeln, Feb'17
 */
public interface StateObservationNondeterministic extends StateObservation {
    /**
     * Advance the current state to a new afterstate (do the deterministic part of advance)
     *
     * @param action the action
     */
    public void advanceDeterministic(Types.ACTIONS action);

    /**
     * Advance the current afterstate to a new state (do the nondeterministic part of advance)
     */
    public void advanceNondeterministic();

    /**
     * Get the next nondeterministic action
     *
     * @return the action, null if the next action is deterministic
     */
    public Types.ACTIONS getNextNondeterministicAction();

    public StateObservationNondeterministic copy();
}
