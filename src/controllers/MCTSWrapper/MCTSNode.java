package controllers.MCTSWrapper;

import controllers.MCTSWrapper.passStates.ApplyableAction;
import controllers.MCTSWrapper.passStates.GameStateIncludingPass;
import controllers.MCTSWrapper.utils.Tuple;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Node of a monte carlo tree search.
 * This node is implemented as a linked list and thus also represents a search tree at the same time.
 */
public final class MCTSNode {
    /**
     * The game state represented by the node.
     * The class GameStateIncludingPass is used instead of StateObservation
     * to also consider pass situations in the search tree.
     */
    public final GameStateIncludingPass gameState;

    public final Map<Integer, MCTSNode> childNodes;
    public final Map<Integer, Double> moveProbabilities;
    public final Map<Integer, Double> meanValues;
    public final Map<Integer, Double> visitCounts;

    private boolean expanded;

    public MCTSNode(final GameStateIncludingPass gameState) {
        this.gameState = gameState;

        childNodes = new HashMap<>();
        moveProbabilities = new HashMap<>();
        meanValues = new HashMap<>();
        visitCounts = new HashMap<>();
    }

    public void setExpanded() {
        expanded = true;
    }

    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Overrides the node's move probabilities.
     *
     * @param moveProps The new move probabilities.
     * @throws IllegalArgumentException If the moveProps array's size doesn't equal the count of available actions.
     */
    public void setMoveProbabilities(final double[] moveProps) {
        final var availableActions = gameState.getAvailableActionsIncludingPassActions();

        if (availableActions.length != moveProps.length)
            throw new IllegalArgumentException("The length of moveProps array has to match the count of available actions");

        for (int i = 0; i < moveProps.length; i++) {
            moveProbabilities.put(availableActions[i].getId(), moveProps[i]);
        }
    }


    public Tuple<ApplyableAction, MCTSNode> selectChild(final double c_puct) {
        final var availableActions = gameState.getAvailableActionsIncludingPassActions();

        var bestValue = Double.NEGATIVE_INFINITY;
        ApplyableAction bestAction = null;

        for (final var a : availableActions) {
            final var value = getQ(a) + c_puct * getP(a) * Math.sqrt(sum(visitCounts.values())) / (1 + getN(a));
            if (value > bestValue) {
                bestValue = value;
                bestAction = a;
            }
        }

        assert bestAction != null;

        final MCTSNode child;
        if (childNodes.containsKey(bestAction.getId())) {
            child = childNodes.get(bestAction.getId());
        } else {
            child = new MCTSNode(gameState.advance(bestAction));
            childNodes.put(bestAction.getId(), child);
        }

        return new Tuple<>(bestAction, child);
    }

    private static double sum(final Collection<Double> values) {
        return values.stream().mapToDouble(it -> it).sum();
    }

    double getQ(final ApplyableAction action) {
        return meanValues.getOrDefault(action.getId(), 0.0);
    }

    double getN(final ApplyableAction action) {
        return visitCounts.getOrDefault(action.getId(), 0.0);
    }

    double getP(final ApplyableAction action) {
        return moveProbabilities.getOrDefault(action.getId(), 0.0);
    }

    void incrementVisitCount(final ApplyableAction action) {
        visitCounts.put(
            action.getId(),
            getN(action) + 1
        );
    }
}