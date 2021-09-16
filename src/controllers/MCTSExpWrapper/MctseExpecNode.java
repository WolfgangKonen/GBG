package controllers.MCTSExpWrapper;

import controllers.MCTSWrapper.passStates.ApplicableAction;
import controllers.MCTSWrapper.passStates.GameStateIncludingPass;
import controllers.MCTSWrapper.passStates.RegularAction;
import controllers.MCTSWrapper.utils.Tuple;
import tools.ScoreTuple;
import tools.Types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a MCTS Expectimax (MCTSE) Expectimax node.
 * Each Expectimax node has multiple {@link MctseChanceNode} children and one {@link MctseChanceNode} parent.
 *
 * An Expectimax node is reached after a deterministic action. The next action is nondeterministic (added from
 * the environment).
 *
 * This node is implemented as a linked list and thus also represents a search tree at the same time.
 */
public final class MctseExpecNode {
    /**
     * The game state represented by the node.
     * The class GameStateIncludingPass is used instead of StateObservation
     * to also consider pass situations in the search tree.
     */
    public final GameStateIncludingPass gameState;

    public final Map<Integer, MctseChanceNode> childNodes;
    public final Map<Integer, Double> moveProbabilities;
    public final Map<Integer, Double> meanValues;
    public final Map<Integer, Integer> visitCounts;

    private boolean expanded;
    private ScoreTuple averageScoreTuple;
    private int selfVisits=0;

    public MctseExpecNode(final GameStateIncludingPass gameState) {
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

    public Tuple<ApplicableAction, MctseChanceNode> selectNondet() {
        final var r = gameState.getNextRandomAction();
        final var tup = gameState.advanceNondeterministic(r);

        final MctseChanceNode child;
        if (childNodes.containsKey(r.getId())) {
            child = childNodes.get(r.getId());
        } else {
            child = new MctseChanceNode(tup.element2);    // a new, non-expanded node
            childNodes.put(r.getId(), child);
        }

        return new Tuple<>(r, child);
    }

    /**
     * @param availableActions
     * @return the action argmax(getP(a)) (the first maximizing action, if there are more than one with the same max)
     */
    private ApplicableAction selectBestFromP(ApplicableAction[] availableActions) {
        var bestValue = Double.NEGATIVE_INFINITY;
        ApplicableAction bestAction = null;
        for (final var a : availableActions) {
            var value = getP(a);
            if (value > bestValue) {
                bestValue = value;
                bestAction = a;
            }
        }
        return bestAction;
    }

    public ScoreTuple setAverageTuple() {
        selfVisits++;
        averageScoreTuple = new ScoreTuple(gameState.getNumPlayers());  // initialize with 0's
        for (Map.Entry<Integer, MctseChanceNode> entry : childNodes.entrySet()) {
            Types.ACTIONS act = new Types.ACTIONS(entry.getKey());
            double prob = gameState.getProbability(act);
            ApplicableAction actionND = new RegularAction(act);
            MctseChanceNode child = entry.getValue();
            double weight = prob*selfVisits/(double)getN(actionND);    // TODO
            averageScoreTuple.combine(child.getSumOfScoreTuples(), ScoreTuple.CombineOP.AVG,0,weight);
        }
        return averageScoreTuple;
    }

    public ScoreTuple getAverageTuple() {
        return averageScoreTuple;
    }

    private static double sum(final Collection<Integer> values) {
        return values.stream().mapToInt(it -> it).sum();
    }

    double getQ(final ApplicableAction action) {
        return meanValues.getOrDefault(action.getId(), 0.0);
    }

    int getN(final ApplicableAction action) {
        return visitCounts.getOrDefault(action.getId(), 0);
    }

    double getP(final ApplicableAction action) {
        return moveProbabilities.getOrDefault(action.getId(), 0.0);
    }

    public ArrayList<Integer> getLastMoves() { return gameState.getLastMoves(); }

    void incrementVisitCount(final ApplicableAction action) {
        visitCounts.put(
            action.getId(),
            getN(action) + 1
        );
    }

}