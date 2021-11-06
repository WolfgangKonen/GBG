package controllers.MCTSExpWrapper;

import controllers.MCTSExpWrapper.stateApproximation2.Approximator2;
import controllers.MCTSWrapper.passStates.ApplicableAction;
import controllers.MCTSWrapper.passStates.GameStateIncludingPass;
import controllers.MCTSWrapper.passStates.RegularAction;
import controllers.MCTSWrapper.utils.Tuple;
import tools.ScoreTuple;
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


    public MctseExpecNode(final GameStateIncludingPass gameState) {
        super(gameState);

        childNodes = new HashMap<>();
    }

    // --- now in MctseNode ---
//    public void setExpanded() {
//        expanded = true;
//    }
//    public boolean isExpanded() {
//        return expanded;
//    }

    public ScoreTuple expand(Approximator2 approximator) {
        ScoreTuple avgScoreTuple = new ScoreTuple(gameState.getNumPlayers());
        for (ApplicableAction r : gameState.getAvailableRandoms()) {
            final double weight = gameState.getProbability(r);
            final var tuple = gameState.advanceNondeterministic(r);
            final MctseChanceNode child = new MctseChanceNode(tuple.element2);    // a new, non-expanded node
            child.expand(approximator);
            childNodes.put(r.getId(), child);
            ScoreTuple delta = child.getBestScoreTuple();
            avgScoreTuple.combine(delta, ScoreTuple.CombineOP.AVG,0,weight);
        }
        setExpanded();
        return avgScoreTuple;
    }

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

    /**
     * Check that {@code selfVisits}, the number of visits to {@code this}, as established
     * by the parent level, is the same as the sum of visitCounts.
     * <p>
     * Do this recursively for the whole branch of {@code this}.
     * <p>
     * Do this check only for
     * {@link ConfigExpWrapper#DO_EXPECTIMAX_EXPAND DO_EXPECTIMAX_EXPAND}{@code =false} because in the other case the number of visits
     * differ by one or two nodes.
     *
     * @param selfVisits    number of visits to {@code this} (from parent)
     * @return the number of nodes in this branch (including {@code this}), just as information, no check
     */
    public int checkTree(int selfVisits) {
        int numNodes=1;         // 1 for self
        if (selfVisits>0) {
            int sumv = (int) sum(visitCounts.values());
//            if (selfVisits!=sumv)
//                System.err.println("Expec: "+selfVisits+", sumv="+sumv);
            if (!ConfigExpWrapper.DO_EXPECTIMAX_EXPAND) // the assertion works only in this setting
                assert selfVisits == sumv : "[MctseExpecNode.checkTree] Error: selfVisits=" + selfVisits + ", sum(visitCounts)=" + sumv;
        }
        // loop over all children (they are always CHANCE nodes):
        for (Map.Entry<Integer, MctseChanceNode> entry : childNodes.entrySet()) {
            MctseChanceNode child = entry.getValue();
            RegularAction actreg = new RegularAction(new Types.ACTIONS(entry.getKey()));
            numNodes += child.checkTree(getN(actreg)-1);
            // why getN(actreg)-1? - The first call expands, does not increment any visit count
        }
        return numNodes;
    }

    /**
     * Form a histogram in {@code Map histo} that counts for EXPECTIMAX nodes with the same number of visits (that is
     * the Map's key) how many nodes have 0, 1, ..., {@code numRans} children where {@code numRans} is the multiplicity
     * of EXPECTIMAX nodes, i.e. the number of available nondeterministic actions.
     * <p>
     * NOTE: This method might fail, if EXPECTIMAX nodes with the same number of visits differ in their {@code numRans}.
     *
     * @param histo a {@code Map<Integer, double[]>} that is modified in the EXPECTIMAX recursive call
     * @return the number of EXPECTIMAX nodes in this branch
     */
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