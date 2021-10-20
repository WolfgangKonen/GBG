package games;

import controllers.MCTSWrapper.utils.Tuple;
import tools.Types;

import java.util.ArrayList;

/**
 * Default implementation of interface {@link PartialState} for perfect-information games.
 * <p>
 * Imperfect-information games have to override the methods implemented here.
 */
public class PartialPerfect implements PartialState {
    protected boolean m_partialState = false;

    /**
     * For perfect-information games: the partial state that the player-to-move observes is
     * identical to {@code this}. <br>
     *
     * @return the partial information state (here: identical to {@code this})
     */
    public StateObservation partialState() { return (StateObservation) this; }

    public boolean isPartialState(int p) { return m_partialState; }
    public boolean isPartialState() { return m_partialState; }
    public void setPartialState(boolean pstate) { m_partialState = pstate; }

    public boolean isImperfectInformationGame() { return false; }

    /**
     * For  perfect-information games: there is nothing to do, the perfect-information state
     * is already complete.
     *
     * @return 	a {@link Tuple} where {@code element1} carries the randomly completed state and {@code element2} has
     * 			the number of possible completions.
     */
    public Tuple<StateObservation,Double> completePartialState() {
        if (isImperfectInformationGame())
            throw new RuntimeException("ObserverBase.completePartialState() is NOT valid for imperfect-information games!");
        return new Tuple<>((StateObservation) this,1.0);
    }
    public Tuple<StateObservation,Double>  completePartialState(int p, Types.ACTIONS ranAct) {
        if (isImperfectInformationGame())
            throw new RuntimeException("ObserverBase.completePartialState(int,ACTIONS) is NOT valid for imperfect-information games!");
        return new Tuple<>((StateObservation) this,1.0);
    }

    // --- not needed anymore ---
//    public Tuple<StateObservation,Double>  completePartialState(int p) {
//        if (isImperfectInformationGame())
//            throw new RuntimeException("ObserverBase.completePartialState(int) is NOT valid for imperfect-information games!");
//        return new Tuple<>((StateObservation) this,1.0);
//    }
//    public Tuple<StateObservation,Double>  completePartialState(int p, StateObservation root) {
//        if (isImperfectInformationGame())
//            throw new RuntimeException("ObserverBase.completePartialState(int,StateObservation) is NOT valid for imperfect-information games!");
//        return new Tuple<>((StateObservation) this,1.0);
//    }

    /**
     * Default implementations for perfect-information games: throw a RuntimeException, since perfect-information games
     * should not call this method.
     */
    public ArrayList<Types.ACTIONS> getAvailableCompletions() {
        throw new RuntimeException("[getAvailableCompletions] Games with imperfect information (partial states) need to override this method. "+
                "Perfect-information games should never call it");
    }
    /**
     * Default implementations for perfect-information games: throw a RuntimeException, since perfect-information games
     * should not call this method.
     */
    public ArrayList<Types.ACTIONS> getAvailableCompletions(int p) {
        throw new RuntimeException("[getAvailableCompletions] Games with imperfect information (partial states) need to override this method. "+
                "Perfect-information games should never call it");
    }

    // --- not needed anymore, we have the Tuples returned in StateObservation.completePartial... ---
//  public double getProbCompletion(Types.ACTIONS action) { return 1.0; }

}
