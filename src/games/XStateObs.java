package games;

import TournamentSystem.TSTimeStorage;
import controllers.PlayAgtVector;

import java.util.ArrayList;

/**
 * This class is used for better logging (see
 * {@link XArenaFuncs#competeNPlayer(PlayAgtVector, int, StateObservation, int, int, TSTimeStorage[], ArrayList)
 * XArenaFuncs.competeNPlayer} and its argument {@code finalSobList}
 */
public class XStateObs {
    private final StateObservation finalState;
    private final int k;
    private final int p0Role;

    public XStateObs(StateObservation sob, int k, int p0Role) {
        this.k = k;
        this.p0Role = p0Role;
        this.finalState = sob;
    }

    public XStateObs(XStateObs other) {
        this.k = other.k;
        this.p0Role = other.p0Role;
        this.finalState = other.finalState;
    }

    public StateObservation select(int k, int p) {
        return (this.k==k && this.p0Role==p) ?
                this.finalState : null;
    }

    public StateObservation getFinalState() {
        return finalState;
    }
}
