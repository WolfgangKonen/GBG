package controllers.MCTSExpWrapper;

import controllers.MCTSWrapper.passStates.ApplicableAction;
import controllers.MCTSWrapper.passStates.GameStateIncludingPass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class {@code MctseNode} is the base class of {@link MctseExpecNode} and {@link MctseChanceNode} and bundles common
 * functionalities.
 * <p>
 * It is also needed in {@link MctseChanceNode#selectChild(double)} to return a child node which may be either
 * {@link MctseExpecNode} or {@link MctseChanceNode} (as it may happen in games like KuhnPoker).
 */
public class MctseNode {
    /**
     * The game state represented by the node.
     * The class GameStateIncludingPass is used instead of StateObservation
     * to also consider pass situations in the search tree.
     */
    public final GameStateIncludingPass gameState;

    public final Map<Integer, Integer> visitCounts;

    MctseNode(final GameStateIncludingPass gameState) {
        this.gameState = gameState;
        visitCounts = new HashMap<>();
    }

    public ArrayList<Integer> getLastMoves() { return gameState.getLastMoves(); }

    public int getN(final ApplicableAction action) {
        return visitCounts.getOrDefault(action.getId(), 0);
    }

    protected static double sum(final Collection<Integer> values) {
        return values.stream().mapToInt(it -> it).sum();
    }

    public void incrementVisitCount(final ApplicableAction action) {
        visitCounts.put(
                action.getId(),
                getN(action) + 1
        );
    }

}
