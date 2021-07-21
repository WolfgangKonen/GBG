package controllers.MCTSWrapper;

import controllers.MCTSWrapper.passStates.ApplicableAction;
import controllers.MCTSWrapper.passStates.GameStateIncludingPass;
import controllers.MCTSWrapper.utils.Tuple;

import java.util.ArrayList;
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
    public final Map<Integer, Integer> visitCounts;

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


    public Tuple<ApplicableAction, MCTSNode> selectChild(final double c_puct) {
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
            // In case visitCounst.size()>0, select according to the normal PUCT formula (EPS negligible, because |EPS| << 1)
            // In case visitCounts.size()==0 && EPS>0, select bestAction = argmax(getP(a)).
            // [This is because a non-visited node has getQ(a) = getN(a) = 0.]
            // This is the solution from Surag Nair, however, it is suboptimal for Othello. Why?
            var value = getQ(a) + c_puct * getP(a) * Math.sqrt(sum(visitCounts.values())+ConfigWrapper.EPS) / (1 + getN(a));
            // In case visitCounts.size()==0 && EPS==0, select the 1st action. This is the case originally
            // provided by JS, and it is better in the Othello-case.

            // In case visitCounts.size()==0 && EPS <0, select a random action
            // (experimental option: ... that is NOT argmax(getP(a))).
            if (vsz==0 && ConfigWrapper.EPS<0) {    // is EPS<0 on average as good as (EPS==0)-solution?
                value = Math.random();
                //if (a.getId()==bestP_act.getId()) value = -1;   // experimental: avoid the action argmax(getP(a))
            }

            if (value > bestValue) {
                bestValue = value;
                bestAction = a;
            }
        }

        assert bestAction != null;

//        // /WK/ debug check only
//        var bestValue2 = Double.NEGATIVE_INFINITY;
//        ApplicableAction bestAction2 = null;
//        if (ConfigWrapper.EPS>0) {
//            for (final var a : availableActions) {
//                double value;
//                // this variant is more closely to the Surag-Nair code. It should give exactly the same actions
//                if (getN(a)==0) {
//                    value = c_puct * getP(a) * Math.sqrt(sum(visitCounts.values()) + ConfigWrapper.EPS) ;
//                } else {
//                    value = getQ(a) + c_puct * getP(a) * Math.sqrt(sum(visitCounts.values())+ ConfigWrapper.EPS) / (1 + getN(a));
//                }
//                if (value > bestValue2) {
//                    bestValue2 = value;
//                    bestAction2 = a;
//                }
//            }
//            if (bestAction.getId()!=bestAction2.getId()) {
//                int dummy = 1;
//            }
//            assert (bestAction.getId()==bestAction2.getId()) : "Check bestAction2 failed!";
//            if (vsz==0)
//                assert (bestAction.getId()==selectBestFromP(availableActions).getId()) : "Check selectBestFromP failed!";
//        }

        final MCTSNode child;
        if (childNodes.containsKey(bestAction.getId())) {
            child = childNodes.get(bestAction.getId());
        } else {
            child = new MCTSNode(gameState.advance(bestAction));
            childNodes.put(bestAction.getId(), child);
        }

        return new Tuple<>(bestAction, child);
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