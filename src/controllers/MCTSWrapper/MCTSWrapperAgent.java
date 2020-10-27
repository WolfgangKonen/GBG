package controllers.MCTSWrapper;

import controllers.AgentBase;
import controllers.MCTSWrapper.passStates.GameStateIncludingPass;
import controllers.MCTSWrapper.passStates.PassAction;
import controllers.MCTSWrapper.stateApproximation.Approximator;
import games.ObserverBase;
import games.Othello.StateObserverOthello;
import games.StateObservation;
import tools.Types;

import java.util.Comparator;
import java.util.Map;

/**
 * PlayAgent that performs a monte carlo tree search to calculate the next action to be selected.
 * This agent also wraps an approximator, which is used to evaluate game states in monte carlo simulations.
 */
public final class MCTSWrapperAgent extends AgentBase {
    private final int iterations;
    private final MCTS mcts;
    private final Approximator approximator;

    /**
     * @param iterations   Number of monte carlo iterations to be performed before the next action is selected.
     * @param c_puct       A PUCT parameter that controls the importance of exploring new nodes instead of exploiting known ones.
     * @param approximator A component that approximates the value of a given game state.
     * @param name         The name of the agent to be displayed.
     */
    public MCTSWrapperAgent(
        final int iterations,
        final double c_puct,
        final Approximator approximator,
        final String name
    ) {
        this.iterations = iterations;
        this.approximator = approximator;
        mcts = new MCTS(approximator, c_puct);
        setName(name);
        setAgentState(AgentState.TRAINED);
    }

    private int lastSelectedAction = Integer.MIN_VALUE;
    private MCTSNode lastSelectedNode;

    @Override
    public Types.ACTIONS_VT getNextAction2(
        final StateObservation sob,
        final boolean random,
        final boolean silent
    ) {
        final MCTSNode mctsNode;

        if (lastSelectedNode == null) {
            // There is no search tree yet.
            // So a new mcts node is created from the given game state sob.
            mctsNode = new MCTSNode(new GameStateIncludingPass(sob));
        } else {
            // There already exists a search-tree, which was built in the game's past mcts iterations.

            // In the following, the moves made since the last cache of the search tree are reconstructed
            // on the last saved mcts node. This should result in the mcts node belonging to the current state of the game.
            final var pastActions = ((ObserverBase) sob).getLastMoves();
//            final var pastActions = ((StateObserverOthello) sob).lastMoves;

            var node = lastSelectedNode;
            for (int i = 1 + pastActions.indexOf(lastSelectedAction); i < pastActions.size(); i++) {
                node = node.childNodes.get(pastActions.get(i));

                if (node == null) {
                    // In this case the current game state is not present in the previously expanded search tree,
                    // because it was not relevant enough in the mcts to be expanded.
                    // In this case a new monte carlo search tree is created based on this node.
                    node = new MCTSNode(new GameStateIncludingPass(sob));
                    break;
                }
            }
            mctsNode = node;
        }

        // At this point mctsNode represents the current game state's node in the monte carlo search tree.

        // Performs the given number of mcts iterations.
        for (int i = 0; i < iterations; i++) {
            mcts.search(mctsNode);
        }

        // Selects the int value of the action that leads to the child node that maximizes the visit count.
        // This value is also cached for further calls.
        lastSelectedAction = mctsNode.visitCounts.entrySet().stream().max(
            Comparator.comparingDouble(Map.Entry::getValue)
        ).orElseThrow().getKey();
        // Caches the child node belonging to the previously selected action.
        lastSelectedNode = mctsNode.childNodes.get(lastSelectedAction);

        // Pass states should not be cached.
        while (lastSelectedNode != null && lastSelectedNode.gameState.lazyMustPass.value()) {
            lastSelectedNode = lastSelectedNode.childNodes.get(new PassAction().getId());
        }

        return new Types.ACTIONS_VT(
            lastSelectedAction,
            false,
            mctsNode.moveProbabilities.values().stream().mapToDouble(v -> v).toArray()
        );
    }

    @Override
    public double getScore(final StateObservation sob) {
        return approximator.getScore(sob);
    }
}
