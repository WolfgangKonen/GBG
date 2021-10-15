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
 * This class represents an MCTS Expectimax (MCTSE) Expectimax node.
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

    //--- never used ---
//   /**
//     * Overrides the node's move probabilities.
//     *
//     * @param moveProps The new move probabilities.
//     * @throws IllegalArgumentException If the moveProps array's size doesn't equal the count of available actions.
//     */
//    public void setMoveProbabilities(final double[] moveProps) {
//        final var availableActions = gameState.getAvailableActionsIncludingPassActions();
//
//        if (availableActions.length != moveProps.length)
//            throw new IllegalArgumentException("The length of moveProps array has to match the count of available actions");
//
//        for (int i = 0; i < moveProps.length; i++) {
//            moveProbabilities.put(availableActions[i].getId(), moveProps[i]);
//        }
//    }

    public Tuple<ApplicableAction, MctseChanceNode> selectNondet() {
        //--- old and wrong: this would always select the *same* random action---
        //final var r = gameState.getNextRandomAction();
        //final var tup = gameState.advanceNondeterministic(r);

        final var tup = gameState.advanceNondeterministic();
        // bug fix 2021-10-14: advanceNondeterministic() returns a tuple with
        //    1st element: varying nondeterministic actions according to the state's probability distribution
        //    2nd element: a *COPY* of the gameState that has this nondeterministic action applied
        // (It is important to make a COPY, so that the original gameState is not affected by advanceNondeterministic
        // and is still in (isNextActionDeterministic()==false)-condition.)
        final var r = tup.element2.getNextRandomAction();

        final MctseChanceNode child;
        if (childNodes.containsKey(r.getId())) {
            child = childNodes.get(r.getId());
        } else {
            child = new MctseChanceNode(tup.element2);    // a new, non-expanded node
            childNodes.put(r.getId(), child);
        }

        return new Tuple<>(r, child);
    }

    //--- never used ---
//    /**
//     * @param availableActions
//     * @return the action argmax(getP(a)) (the first maximizing action, if there are more than one with the same max)
//     */
//    private ApplicableAction selectBestFromP(ApplicableAction[] availableActions) {
//        var bestValue = Double.NEGATIVE_INFINITY;
//        ApplicableAction bestAction = null;
//        for (final var a : availableActions) {
//            var value = getP(a);
//            if (value > bestValue) {
//                bestValue = value;
//                bestAction = a;
//            }
//        }
//        return bestAction;
//    }

    /**
     * Loop over childNodes to calculate average {@link ScoreTuple}.
     * @return a weighted average {@link ScoreTuple} where the weights are the probability of each action
     *      and the {@link ScoreTuple}s are the <b>mean</b> tuples of each child
     */
    public ScoreTuple getAverageTuple() {
        int selfVisits = (int) sum(visitCounts.values());
        ScoreTuple averageScoreTuple = new ScoreTuple(gameState.getNumPlayers());  // initialize with 0's
        for (Map.Entry<Integer, MctseChanceNode> entry : childNodes.entrySet()) {
            Types.ACTIONS act = new Types.ACTIONS(entry.getKey());
            double prob = gameState.getProbability(act);
            ApplicableAction actionND = new RegularAction(act);
            MctseChanceNode child = entry.getValue();
            double weight = prob*selfVisits/(double)getN(actionND);    // TODO
            averageScoreTuple.combine(child.getSumOfScoreTuples(), ScoreTuple.CombineOP.AVG,0,weight);
            // note that child.getSumOfScoreTuples()/getN(actionND) is just the *mean* score tuple of the child.
            // The multiplication by selfVisits is just for the caller of this method, thus the effective weight
            // is just prob.
        }
        return averageScoreTuple;
    }

//    double getP(final ApplicableAction action) {
//        return moveProbabilities.getOrDefault(action.getId(), 0.0);
//    }
//
//    double getQ(final ApplicableAction action) {
//        return meanValues.getOrDefault(action.getId(), 0.0);
//    }

    int getN(final ApplicableAction action) {
        return visitCounts.getOrDefault(action.getId(), 0);
    }

    private static double sum(final Collection<Integer> values) {
        return values.stream().mapToInt(it -> it).sum();
    }

    public ArrayList<Integer> getLastMoves() { return gameState.getLastMoves(); }

    void incrementVisitCount(final ApplicableAction action) {
        visitCounts.put(
            action.getId(),
            getN(action) + 1
        );
    }

    public int checkTree(int selfVisits) {
        int numNodes=1;         // 1 for self
        if (selfVisits>0) {
            int sumv = (int) sum(visitCounts.values());
            //System.out.println("Expec: "+selfVisits+", sumv="+sumv);
            assert selfVisits == sumv : "[MctseExpecNode.checkTree] Error: selfVisits=" + selfVisits + ", sum(visitCounts)=" + sumv;
        }
        for (Map.Entry<Integer, MctseChanceNode> entry : childNodes.entrySet()) {
            MctseChanceNode child = entry.getValue();
            Types.ACTIONS act = new Types.ACTIONS(entry.getKey());
            RegularAction actreg = new RegularAction(act);
            numNodes += child.checkTree(getN(actreg)-1);
            // why getN(actreg)-1? - The first call expands, does not increment any visit count
        }
        return numNodes;
    }

}