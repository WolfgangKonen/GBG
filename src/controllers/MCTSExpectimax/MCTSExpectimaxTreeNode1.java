package controllers.MCTSExpectimax;

import games.StateObservationNondeterministic;
import tools.Types;

import java.util.Random;
import java.util.TreeMap;

/**
 * Created by Johannes on 03.06.2017.
 */
public class MCTSExpectimaxTreeNode1 {
    public static double epsilon = 1e-6;		            // tiebreaker
    private StateObservationNondeterministic so = null;
    public Types.ACTIONS action = null;		                // the action which leads from parent's state to this state
    private MCTSExpectimaxChanceNode1 parentNode = null;
    private TreeMap<Types.ACTIONS, MCTSExpectimaxChanceNode1> childrenNodes = new TreeMap<>();
    private MCTSExpectimaxPlayer1 player = null;
    public double value = 0;                                   // total value
    public int visits = 0;                                     // total number of visits
    private Random random;
    public int depth;


    /**
     * This Class represents a MCTS Expectmiax Tree Node. (Min/Max Node)
     * Each Chance Node has multiple {@link MCTSExpectimaxChanceNode1} children and one {@link MCTSExpectimaxChanceNode1} parent.
     *
     * @param so         the unadvanced state of the parentNode
     * @param action	 the action which leads from parentNode's state to this state ({@code null} for root node)
     * @param parentNode the parentNode node
     * @param random     a random number generator
     * @param player     a reference to the one MCTS agent where {@code this} is part of (needed to access several parameters of the MCTS agent)
     */
    public MCTSExpectimaxTreeNode1(StateObservationNondeterministic so, Types.ACTIONS action, MCTSExpectimaxChanceNode1 parentNode, Random random, MCTSExpectimaxPlayer1 player) {
        this.so = so;
        this.action = action;
        this.parentNode = parentNode;
        this.player = player;
        this.random = random;
        this.depth = parentNode.depth;
    }

    /**
     * Select the next {@link MCTSExpectimaxChanceNode1} that should be evaluated
     *
     * Because the next boardstate is always random the child node will be chosen randomly using the action that this {@link MCTSExpectimaxTreeNode1} represents.
     *
     * @return the {@link MCTSExpectimaxTreeNode1} that should be evaluated
     */
    public MCTSExpectimaxChanceNode1 treePolicy() {
        return expand();
    }

    /**
     * Expand the current node, by randomly selecting one child node
     *
     * @return the selected child node
     */
    private MCTSExpectimaxChanceNode1 expand() {
        StateObservationNondeterministic childSo = so.copy();

        MCTSExpectimaxChanceNode1 child;
        Types.ACTIONS nondeterministicAction = childSo.getNextNondeterministicAction();

        if(!childrenNodes.containsKey(nondeterministicAction)) {
            //create a new child node
            childSo.advanceNondeterministic();
            child = new MCTSExpectimaxChanceNode1(childSo, nondeterministicAction, this, random, player);
            childrenNodes.put(nondeterministicAction, child);
        } else {
            //a child node representing this boardstate already exists
            child = childrenNodes.get(nondeterministicAction);
        }

        return child;
    }

    /**
     * starting from this leaf node a game with random actions will be played until the game is over or the maximum rollout depth is reached
     *
     * @return the {@link StateObservationNondeterministic#getGameScore()} after the rollout is finished
     */
    public double rollOut() {
        StateObservationNondeterministic rollerState = so.copy();
        int thisDepth = this.depth;

        while (!finishRollout(rollerState, thisDepth)) {
            rollerState.setAvailableActions();
            int action = random.nextInt(rollerState.getNumAvailableActions());
            rollerState.advance(rollerState.getAction(action));
            thisDepth++;
        }

        if (rollerState.isGameOver()) {
            player.nRolloutFinished++;
        }

        return rollerState.getGameScore(so);
    }

    /**
     * checks if a rollout is finished
     *
     * @param rollerState the current gamestate
     * @param depth the current rolloutdepth
     * @return true if the rollout is finished, false if not
     */
    public boolean finishRollout(StateObservationNondeterministic rollerState, int depth) {
        if (depth >= player.getROLLOUT_DEPTH()) {
            return true;
        }

        if (rollerState.isGameOver()) {
            return true;
        }

        return false;
    }

    /**
     * Backup the score through all parent nodes, until the root node is reached
     * calls itself recursively in each parent node
     *
     * @param score the score that we want to backup
     */
    public void backUp(double score) {
        if (so.getNumPlayers()==2) {
            score =- score;
        }

        visits++;
        value += score;

        if (parentNode != null) {
            parentNode.backUp(score);
        }
    }
}
