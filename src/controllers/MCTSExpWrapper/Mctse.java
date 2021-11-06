package controllers.MCTSExpWrapper;

import controllers.MCTSExpWrapper.stateApproximation2.Approximator2;
import tools.ScoreTuple;

/***
 * A class that encapsulates the algorithm for Monte Carlo Tree Search Expectimax for 2-player games.
 * The games must have separate states for situations where a player has to pass.
 *
 * This inspiration for this code comes from https://web.stanford.edu/~surag/posts/alphazero.html
 */
public final class Mctse {
    private final double c_puct;
    private final Approximator2 approximator;
    private final int maxDepth;
    public int largestDepth;    // the largest tree depth encountered during MCTS.search iterations

    /**
     * @param approximator A component that approximates the value of a given game state.
     * @param c_puct A PUCT parameter that controls the importance of
     *               exploring new nodes instead of exploiting known ones.
     * @param maxDepth Return from search, if depth==maxDepth. Set to -1, if search should not return because
     *                 of depth. (-1 will be transformed to Integer.MAX_VALUE.)
     */
    public Mctse(final Approximator2 approximator, final double c_puct, final int maxDepth) {
        this.approximator = approximator;
        this.c_puct = c_puct;
        this.maxDepth = (maxDepth==-1) ? Integer.MAX_VALUE : maxDepth;
    }

    /**
     * Recursive Monte Carlo tree search that is applicable to 1- and 2-player games
     * which have separate states for situations where a player has to pass.
     * <p>
     * Values are negated in 2-player games because they are viewed from the previous player's perspective.
     * <p>
     * ATTENTION: This method is not yet viable for N &gt; 2 players (!!)
     *
     * @param node Node where the tree search starts.
     * @return The evaluation of a reached leaf node's game state (negated on each recursion level for 2-player games).
     */
    public ScoreTuple search(final MctseChanceNode node, final int depth) {
        // If a terminating game state is reached, return its negated value (2-player game) or its value (1-player game)
        if (node.gameState.isFinalGameState())
            return node.gameState.getFinalScoreTuple();

        // If a non-expanded CHANCE node is reached, expand it (set its move probabilities) and return its best ScoreTuple
        if (!node.isExpanded()) {
            node.expand(approximator);
            return node.getBestScoreTuple();
        }

        // just info: what is the largest depth encountered?
        if (depth > largestDepth) largestDepth=depth;
        // normally, maxDepth==Integer.MAX_VALUE, so the following if-branch will remain inactive;
        // but if maxDepth is reached, returned the stored bestScoreTuple
        if (depth > this.maxDepth) {
            return node.getBestScoreTuple();
        }

        // Here the node is already expanded and doesn't contain a terminating game state.
        // Next step is to select a child node based on the PUCT algorithm.
        // If the child is an EXPECTIMAX node, select as well a grandchild CHANCE node, based on a random action from
        // the environment. This grandchild node will be expanded in the next recursive call of this method.
        // If the child is a CHANCE node, pass it directly to the next recursive call of this method.

        final var selected = node.selectChild(c_puct); // Returns a tuple containing the selected
                                                       // node and the action that leads to it.
        final var selectedAction = selected.element1;

        int player = node.gameState.getPlayer();
        final MctseChanceNode selectedCNode;
        if (selected.element2 instanceof MctseExpecNode) {
            final var selectedENode = (MctseExpecNode) selected.element2;

            // optional: a fresh, non-expanded EXPECTIMAX node may be expanded
            if (ConfigExpWrapper.DO_EXPECTIMAX_EXPAND) {
                if (selectedENode.gameState.isFinalGameState()) {
                    // do nothing (no expand), if selectedENode contains a game-over state

                    int dummy=1;    // just optional debug stop
                } else {
                    // If a non-expanded, non-final EXPECTIMAX node is reached, expand it (create all CHANCE children and
                    // expand them) and return the probability-weighted average of their score tuples.
                    if (!selectedENode.isExpanded()) {
                        return selectedENode.expand(approximator);
                    }
                }
            }

            // Here, selectedENode is already expanded (or we do not expand EXPECTIMAX nodes)
            // In any case, we select one of its CHANCE children randomly
            final var selectedNondet = selectedENode.selectNondet();
            selectedENode.incrementVisitCount(selectedNondet.element1);
            selectedCNode = selectedNondet.element2;
        } else {
            assert selected.element2 instanceof MctseChanceNode;
            selectedCNode = (MctseChanceNode) selected.element2;
        }

        // Recursive call of the tree search for the CHANCE child node. childValue is 'Delta' in pseudo-code.
        final var childValue = search(selectedCNode, depth+1);

        final var qValue = node.getQ(selectedAction);
        node.qValues.put(
                selectedAction.getId(),
                qValue +      // this is here because we **increment** the node's Q(a) by ['Delta']_p
                childValue.scTup[player]);

        // Increment the node's visit count (N).
        node.incrementVisitCount(selectedAction);

        return childValue;
    }
}
