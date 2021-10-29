package controllers.MCTSExpWrapper;

import controllers.MCTSExpWrapper.stateApproximation2.Approximator2;
import controllers.MCTSWrapper.passStates.ApplicableAction;
import controllers.MCTSWrapper.passStates.GameStateIncludingPass;
import controllers.MCTSWrapper.passStates.RegularAction;
import controllers.MCTSWrapper.utils.Tuple;
import tools.ScoreTuple;
import tools.Types.ACTIONS;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a MCTSE <b>Chance node</b>.
 * Each Chance node has multiple {@link MctseNode} children.
 * The MCTSE tree starts with a Chance node and has Chance nodes at all leaves.
 * <p>
 * A Chance node is (usually, but not always) reached after nondeterministic (environment) action(s).
 * But in any case, the next action is deterministic and calculated by the agent.
 * <p>
 * This node with its {@code childNodes} represents a linked list and thus also a search tree.
 */
public final class MctseChanceNode extends MctseNode {
    public final Map<Integer, MctseNode> childNodes;
    public final Map<Integer, Double> moveProbabilities;
    public final Map<Integer, Double> qValues;

    private boolean expanded = false;
    private ScoreTuple bestScoreTuple;      // score tuple belonging to the best action of this at time of expansion of this
//    private ScoreTuple sumOfScoreTuples;

    public MctseChanceNode(final GameStateIncludingPass gameState) {
        super(gameState);

        childNodes = new HashMap<>();
        moveProbabilities = new HashMap<>();
        qValues = new HashMap<>();
//        sumOfScoreTuples = new ScoreTuple(gameState.getFinalScoreTuple().scTup.length);  // initialize with all 0's
    }

    public void expand(Approximator2 approximator) {
        final var sTupleAndMoveProbabilities = gameState.getApproximatedValueAndMoveProbabilities(approximator);
        setMoveProbabilities(sTupleAndMoveProbabilities.element2);
        setExpanded();
        setBestScoreTuple(sTupleAndMoveProbabilities.element1);
    }

    private void setExpanded() {  expanded = true;  }

    public boolean isExpanded() {  return expanded;  }

    private void setBestScoreTuple(ScoreTuple sc) { bestScoreTuple = sc; }

    public ScoreTuple getBestScoreTuple() { return bestScoreTuple; }

//    public void addToSumOfScoreTuples(ScoreTuple sc) {
//        sumOfScoreTuples.combine(sc, ScoreTuple.CombineOP.SUM,0,0);
//    }

//    public ScoreTuple getSumOfScoreTuples() { return sumOfScoreTuples; }

    /**
     * Overrides the node's move probabilities.
     *
     * @param moveProps The new move probabilities.
     * @throws IllegalArgumentException If the moveProps array's size doesn't equal the count of available actions.
     */
    private void setMoveProbabilities(final double[] moveProps) {
        final var availableActions = gameState.getAvailableActionsIncludingPassActions();

        if (availableActions.length != moveProps.length)
            throw new IllegalArgumentException("The length of moveProps array has to match the count of available actions");

        for (int i = 0; i < moveProps.length; i++) {
            moveProbabilities.put(availableActions[i].getId(), moveProps[i]);
        }
    }

    /**
     * Try all available actions and select the one which maximizes the PUCT formula. Depending on the action and the
     * game, the resulting child might have a deterministic or a nondeterministic next action. Consequently, the returned
     * child may be a {@link MctseChanceNode} or {@link MctseExpecNode}, respectively. Thus, it is returned
     * as base class {@link MctseNode}.
     * @param c_puct parameter for PUCT formula. The larger, the larger the influence from the move probabilities
     *               of the wrapped agent.
     * @return a tuple with the selected action as 1st element and the selected child {@link MctseNode} as 2nd element.
     */
    public Tuple<ApplicableAction, MctseNode> selectChild(final double c_puct) {
        final var availableActions = gameState.getAvailableActionsIncludingPassActions();

//        // /WK/ debug check
//        if (visitCounts.size()==0) {
//            System.err.println("[selectChild] *** Warning: visitCounts.size==0 ! ***");
//            System.err.println("sumVC="+sum(visitCounts.values())+", Q(a_0)="+getQ(availableActions[0])); // should be both 0
//        }
//        if (meanValues.size()==0) {
//            System.err.println("[selectChild] *** Warning: meanValues.size==0 ! ***");
//        }

        var vsz = visitCounts.size();       // how often this node been has visited
        var bestValue = Double.NEGATIVE_INFINITY;
        ApplicableAction bestAction = null;

        // only needed for the experimental code below (search for bestP_act)
        //ApplicableAction bestP_act = null;
        //if (vsz==0 && ConfigWrapper.EPS<0) bestP_act = selectBestFromP(availableActions);

        for (final var a : availableActions) {
            // --- just optional  debug info ---
            //double pp = getP(a);
            //double nn = getN(a);

            // In case visitCounst.size()>0, select according to the normal PUCT formula (EPS negligible, because |EPS| << 1)
            // In case visitCounts.size()==0 && EPS>0, select bestAction = argmax(getP(a)).
            // [This is because a non-visited node has getQ(a) = getN(a) = 0.]
            // This is the solution from Surag Nair, and it is the *recommended* choice.
            var value = getQ(a)/(getN(a)+ConfigExpWrapper.EPS)      // bug fix 2021-10-29: divide by getN(a)
                    + c_puct * getP(a) * Math.sqrt(sum(visitCounts.values())+ConfigExpWrapper.EPS) / (1 + getN(a));
            // In case visitCounts.size()==0 && EPS==0, select the 1st action. This is the case originally
            // provided by JS, and it seemed first better in the Othello-case (but later we found that it is comparable to EPS>0).


            // In case visitCounts.size()==0 && EPS <0, select a random action
            // (experimental option: ... that is NOT argmax(getP(a))).
            if (vsz==0 && ConfigExpWrapper.EPS<0) {    // is EPS<0 on average as good as (EPS==0)-solution?
                value = Math.random();
                //if (a.getId()==bestP_act.getId()) value = -1;   // experimental: avoid the action argmax(getP(a))
            }

            if (value > bestValue) {
                bestValue = value;
                bestAction = a;
            }
        }

        assert bestAction != null;

        // Now we advance gameState by bestAction. Depending on the game and depending on bestAction, it might happen
        // that the next state requires a deterministic (KuhnPoker) or a non-deterministic (2048, EWN) action.
        // If the next state already exists in childNodes, it has the right class (MctseChanceNode or MctseExpecNode).
        // If not, the right class is created, depending on isNextActionDeterministic(), and returned as MctseNode.

        final MctseNode child;
        if (childNodes.containsKey(bestAction.getId())) {
            child = childNodes.get(bestAction.getId());
        } else {
            GameStateIncludingPass newGameState = gameState.advanceDeterministic(bestAction);
                                // note that advanceDeterministic will make a copy of the state before advancing,
                                // so that the former state is not changed
            if (newGameState.isNextActionDeterministic()) {
                child = new MctseChanceNode(newGameState);   // a new, non-expanded node
            } else {
                child = new MctseExpecNode(newGameState);    // a new, non-expanded node
            }
            childNodes.put(bestAction.getId(), child);
        }

        return new Tuple<>(bestAction, child);
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

    double getQ(final ApplicableAction action) {
        return qValues.getOrDefault(action.getId(), 0.0);
    }

    double getP(final ApplicableAction action) {
        return moveProbabilities.getOrDefault(action.getId(), 0.0);
    }

    /**
     * Check that {@code selfVisits}, the number of visits to {@code this} (excluding expand visits), as established
     * by the parent level, is the same as the sum of visitCounts of {@code this}.
     * <p>
     * Do this recursively for the whole branch of {@code this}.
     * @param selfVisits    number of visits to {@code this} (from parent)
     * @return the number of nodes in this branch (including {@code this}), just as information, no check
     */
    public int checkTree(int selfVisits) {
        int numNodes = 1;       // 1 for self
        int sumv = (int) sum(visitCounts.values());
        if (selfVisits>0 && sumv>0) {
            //System.out.println("Chance: "+selfVisits+", sumv="+sumv);
            assert selfVisits == sumv : "[MctseChanceNode.checkTree] Error: selfVisits=" + selfVisits + ", sum(visitCounts)=" + sumv;
        }
        for (Map.Entry<Integer, MctseNode> entry : childNodes.entrySet()) {
            RegularAction actreg = new RegularAction(new ACTIONS(entry.getKey()));
            if( entry.getValue() instanceof MctseExpecNode) {
                MctseExpecNode child = (MctseExpecNode) entry.getValue();
                numNodes += child.checkTree(getN(actreg));
            } else { // ... instanceof MctseChanceNode
                MctseChanceNode child = (MctseChanceNode) entry.getValue();
                numNodes += child.checkTree(getN(actreg)-1);
                // why getN(actreg)-1? - The first call expands, does not increment any visit count
            }
        }
        return numNodes;
    }

    public int numChilds(Map histo) {
        int numExpec = 0;       // do not count chance nodes
        for (Map.Entry<Integer, MctseNode> entry : childNodes.entrySet()) {
            if( entry.getValue() instanceof MctseExpecNode) {
                MctseExpecNode child = (MctseExpecNode) entry.getValue();
                numExpec += child.numChilds(histo);
            } else { // ... instanceof MctseChanceNode
                MctseChanceNode child = (MctseChanceNode) entry.getValue();
                numExpec += child.numChilds(histo);
            }
        }
        return numExpec;
    }


}