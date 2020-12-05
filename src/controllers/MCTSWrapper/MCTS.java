package controllers.MCTSWrapper;

import controllers.MCTSWrapper.stateApproximation.Approximator;

/***
 * A class that encapsulates the algorithm for a monte carlo tree search for 2-player games.
 * The games must have separate states for situations where a player has to pass.
 *
 * This inspiration for this code comes from https://web.stanford.edu/~surag/posts/alphazero.html
 */
public final class MCTS {
    private final double c_puct;
    private final Approximator approximator;
    private final int maxDepth;
    public int largestDepth;    // the largest tree depth encountered during MCTS.search iterations

    /**
     * @param approximator A component that approximates the value of a given game state.
     * @param c_puct A PUCT parameter that controls the importance of
     *               exploring new nodes instead of exploiting known ones.
     * @param maxDepth Return from search, if depth==maxDepth. Set to -1, if search should not return because
     *                 of depth. (-1 will be transformed to Integer.MAX_VALUE.)
     */
    public MCTS(final Approximator approximator, final double c_puct, final int maxDepth) {
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
     *     ATTENTION: This method is not yet viable for N>2 players (!!)
     *
     * @param node Node where the tree search starts.
     * @return The evaluation of a reached leaf node's game state (negated on each recursion level for 2-player games).
     */
    public double search(final MCTSNode node, final int depth) {
        final int sign = node.gameState.getNumPlayers()==1 ? (+1) : (-1);   // /WK/ extension for 1-player games

        // If a terminating game state is reached, return its negated value (2-player game) or its value (1-player game)
        if (node.gameState.isFinalGameState())
            return sign * node.gameState.getFinalGameScore();       // /WK/ sign

        if (depth > this.maxDepth) {       // /WK/ testing for RubiksCube
            //System.out.println("maxDepth="+this.maxDepth+" reached. Returning");
            //return 0;           // return with reward 0. Alternative choice would be REWARD_NEGATIVE.
            return -1;
        }
        if (depth > largestDepth) largestDepth=depth;

        // If a non expanded node is reached, return its negated value (2-player game) or its value (1-player game)
        // after it got expanded and its move probabilities were set.
        if (!node.isExpanded()) {
            final var valueAndMoveProbabilities = node.gameState.getApproximatedValueAndMoveProbabilities(approximator);
            node.setMoveProbabilities(valueAndMoveProbabilities.element2);
            node.setExpanded();
            return sign * valueAndMoveProbabilities.element1;       // /WK/ sign
        }

        // Here the node is already expanded and doesn't contain a terminating game state.
        // Next step is to select a child node based on the PUCT algorithm.
        // This child node will be expanded in the next recursive call of this method.

        final var selected = node.selectChild(c_puct); // Returns a tuple containing the selected
                                                       // node and the action that leads to it.
        final var selectedAction = selected.element1;
        final var selectedNode = selected.element2;

        final var childValue = search(selectedNode, depth+1); // Recursive call of the tree search for the child node

        // Update the nodes mean value (Q) with the childValue.
        final var visitCount = node.getN(selectedAction);
        final var meanValue = node.getQ(selectedAction);
        node.meanValues.put(
            selectedAction.getId(),
            (visitCount * meanValue + childValue) / (visitCount + 1)
        );

        // Increment the nodes visit count (N).
        node.incrementVisitCount(selectedAction);

        return sign * childValue;                   // /WK/ sign
    }
}
