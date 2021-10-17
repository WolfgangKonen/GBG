package controllers.MCTSExpWrapper;

import controllers.MCTSWrapper.passStates.ApplicableAction;
import controllers.MCTSWrapper.passStates.GameStateIncludingPass;
import controllers.MCTSWrapper.passStates.RegularAction;
import controllers.MCTSWrapper.utils.Tuple;
import tools.Types;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents an MCTSE <b>Expectimax node</b>.
 * Each Expectimax node has multiple {@link MctseChanceNode} children and one {@link MctseChanceNode} parent.
 * <p>
 * An Expectimax node is reached after a deterministic action. The next action is nondeterministic (added from
 * the environment).
 * <p>
 * This node with its {@code childNodes} represents a linked list and thus also a search tree.
 */
public final class MctseExpecNode extends MctseNode {
    public final Map<Integer, MctseChanceNode> childNodes;

//    public final Map<Integer, Double> moveProbabilities;
//    public final Map<Integer, Double> meanValues;
//    private boolean expanded;

    public MctseExpecNode(final GameStateIncludingPass gameState) {
        super(gameState);

        childNodes = new HashMap<>();
        //moveProbabilities = new HashMap<>();
        //meanValues = new HashMap<>();
    }

    //--- never used ---
//    public void setExpanded() {
//        expanded = true;
//    }
//    public boolean isExpanded() {
//        return expanded;
//    }
//
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
        final var r = tup.element1; // tup.element2.getNextRandomAction();

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

    //--- never used ---
//    /**
//     * Loop over childNodes to calculate average {@link ScoreTuple}.
//     * @return a weighted average {@link ScoreTuple} where the weights are the probability of each action
//     *      and the {@link ScoreTuple}s are the <b>mean</b> tuples of each child
//     */
//    public ScoreTuple getAverageTuple() {
//        int selfVisits = (int) sum(visitCounts.values());
//        ScoreTuple averageScoreTuple = new ScoreTuple(gameState.getNumPlayers());  // initialize with 0's
//        for (Map.Entry<Integer, MctseChanceNode> entry : childNodes.entrySet()) {
//            Types.ACTIONS act = new Types.ACTIONS(entry.getKey());
//            double prob = gameState.getProbability(act);
//            ApplicableAction actionND = new RegularAction(act);
//            MctseChanceNode child = entry.getValue();
//            double weight = prob*selfVisits/(double)getN(actionND);    // TODO
//            averageScoreTuple.combine(child.getSumOfScoreTuples(), ScoreTuple.CombineOP.AVG,0,weight);
//            // note that child.getSumOfScoreTuples()/getN(actionND) is just the *mean* score tuple of the child.
//            // The multiplication by selfVisits is just for the caller of this method, thus the effective weight
//            // is just prob.
//        }
//        return averageScoreTuple;
//    }

//    double getP(final ApplicableAction action) {
//        return moveProbabilities.getOrDefault(action.getId(), 0.0);
//    }
//
//    double getQ(final ApplicableAction action) {
//        return meanValues.getOrDefault(action.getId(), 0.0);
//    }

    /**
     * Check that {@code selfVisits}, the number of visits to {@code this}, as established
     * by the parent level, is the same as the sum of visitCounts.
     * <p>
     * Do this recursively for the whole branch of {@code this}.
     * @param selfVisits    number of visits to {@code this} (from parent)
     * @return the number of nodes in this branch (including {@code this}), just as information, no check
     */
    public int checkTree(int selfVisits) {
        int numNodes=1;         // 1 for self
        if (selfVisits>0) {
            int sumv = (int) sum(visitCounts.values());
            //System.out.println("Expec: "+selfVisits+", sumv="+sumv);
            assert selfVisits == sumv : "[MctseExpecNode.checkTree] Error: selfVisits=" + selfVisits + ", sum(visitCounts)=" + sumv;
        }
        for (Map.Entry<Integer, MctseChanceNode> entry : childNodes.entrySet()) {
            MctseChanceNode child = entry.getValue();
            RegularAction actreg = new RegularAction(new Types.ACTIONS(entry.getKey()));
            numNodes += child.checkTree(getN(actreg)-1);
            // why getN(actreg)-1? - The first call expands, does not increment any visit count
        }
        return numNodes;
    }

    public int numChilds(Map histo) {
        int numVisits = (int) sum(visitCounts.values());
        double[] entry;
        if (histo.containsKey(numVisits)) {
            entry = (double[]) histo.get(numVisits);
        } else {
            int numRans = this.gameState.getAvailableRandoms().length;
            entry = new double[numRans+1];
        }
        entry[childNodes.size()] += 1;      // increment the histo count in bin childNodes.size()
        histo.put(numVisits,entry);

        int numExpec=1;         // 1 for self
        for (Map.Entry<Integer, MctseChanceNode> chance : childNodes.entrySet()) {
            MctseChanceNode child = chance.getValue();
            numExpec += child.numChilds(histo);
        }
        return numExpec;
    }

}