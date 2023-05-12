package games;

import controllers.MCTSWrapper.utils.Tuple;
import tools.Types;

import java.util.ArrayList;

/**
 * Interface PartialState encapsulates the partial-states part of interface {@link StateObservation}.
 * <p>
 * Partial states are needed for imperfect-information games like Poker, Blackjack, ...
 * <p>
 * For all perfect-information games, the partial state is identical to the normal state and is already complete.
 *
 * @see PartialPerfect
 */
public interface PartialState {
    /**
     * For perfect information games, the partial state is identical to {@code this}.
     * For imperfect information games: return a state with only the partial information that
     * the player-to-move is allowed to have.
     * <p>
     * E.g., for Blackjack, the partial state omits (replaces by null) the hole card of the dealer.
     * For Poker, the partial state omits (replaces by null) the hole cards of all other players.
     *
     * @return <ul>
     *     <li> For perfect-information games: just {@code this}, and the partial state flag remains false;
     *     <li> for imperfect-information games: a <b>copy</b> of {@code this} with only partial information
     *     		(and with partial state flag = true)
     * </ul>
     *
     */
    StateObservation partialState();

    /**
     * @param p		player
     * @return Is {@code this} partial with respect to player p? I.e. does it omit information that player p would have?
     */
    boolean isPartialState(int p);

    /**
     * @return Is {@code this} partial with respect to any player?
     */
    boolean isPartialState();

    void setPartialState(boolean pstate);

    /**
     * <ul>
     *   <li> For perfect-information games: return just {@code this} (see {@link ObserverBase}).
     *   <li> For imperfect-information games: if {@code this} is a partial state, complete the hidden elements with a
     * random fill-in method. Imperfect-information games have to <b>override</b> the default implementation in
     * {@link ObserverBase}.
     * </ul> <p>
     * Note that the randomly completed state in {@code this} is in general <b>NOT</b> (!) identical to the full state
     * from which the partial state was derived. It is - given the observable elements in the partial state -
     * <b>one</b> of the possibilities that the player has to take into account.
     * @return 	a {@link Tuple} where {@code element1} carries the randomly completed state and {@code element2} has
     * 			the probability that this random completion occurs.
     */
    Tuple<StateObservation,Double> completePartialState();

    /**
     * Same as {@link #completePartialState()}, but only the part of the state that is visible to player {@code p}
     * is completed. (This reduces the complexity for tree-based agents, since only the relevant part needs to be completed.)
     * <p>
     * Furthermore, complete the state for player p by a specific random action {@code ranAct}
     * <p>
     * Depending on whether the completed state contains unfilled elements or not, the partial state flag is set to
     * true or false.
     * @param p			the player number
     * @param ranAct	the random action
     * @return 	a {@link Tuple} where {@code element1} carries the partially completed state and {@code element2} has
     * 			the probability that this random completion occurs.
     */
    Tuple<StateObservation,Double> completePartialState(int p, Types.ACTIONS ranAct);

    // --- not needed anymore ---
//    /**
//     * Same as {@link #completePartialState()}, but only the part of the state that is visible to player {@code p}
//     * is completed. (This reduces the complexity for tree-based agents, since only the relevant part needs to be completed.)
//     * <p>
//     * Depending on whether the completed state contains unfilled elements or not, the partial state flag is set to
//     * true or false.
//     * @param p	the player number
//     * @return 	a {@link Tuple} where {@code element1} carries the partially completed state and {@code element2} has
//     * 			the probability that this random completion occurs.
//     */
//    Tuple<StateObservation,Double> completePartialState(int p);

    // --- not needed anymore ---
//    /**
//     * Complete a partial state by 'filling the holes' (for player p) from another state {@code root}
//     *
//     * @param p		 the player for which the state is completed
//     * @param root	 the state from which we fill the 'holes' for player p
//     * @return 	a {@link Tuple} where {@code element1} carries the completed state and {@code element2} has
//     * 			the probability that this completion occurs (1.0 in this case).
//     */
//    Tuple<StateObservation,Double> completePartialState(int p, StateObservation root);

    /**
     * For imperfect-information games: if {@link #isPartialState()}, then
     * return all possible completions.
     * <p>
     * For perfect-information games: an exception is thrown.
     *
     * @return a list with all possible completing actions (that can be sent to
     *         {@link StateObservation#advanceNondeterministic(java.util.Random) advanceNondeterministic()})
     */
    ArrayList<Types.ACTIONS> getAvailableCompletions();
    /**
     * Same as {@link #getAvailableCompletions()}, but take only the possible completions for player p
     * @param p  the player
     * @return list with all possible completing actions for player p
     */
    ArrayList<Types.ACTIONS> getAvailableCompletions(int p);

    // --- not needed anymore, we have the Tuples returned in StateObservation.completePartial... ---
//	/**
//	 * @param action  the completing action
//	 * @return the probability that the completing action {@code action} is selected.
//	 */
//	double getProbCompletion(ACTIONS action);

}
