package controllers.MCTSExpectimax;

import games.StateObservation;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This is adapted from Diego Perez MCTS reference implementation
 * 		http://gvgai.net/cont.php
 * (with a bug fix concerning the number of available actions and an 
 *  extension for 1- and 2-player games, adjusted for nondeterministic
 *  games and the return of VTable information.)
 *
 *  @author Johannes Kutsch
 */

public class MCTSExpectimaxChanceNode
{
    public static double epsilon = 1e-6;		                                // tiebreaker
    private StateObservation so = null;
    private Types.ACTIONS action = null;		                                // the action which leads from parent's state to this state
    private MCTSExpectimaxTreeNode parentNode = null;
    private List<MCTSExpectimaxTreeNode> childrenNodes = new ArrayList<>();
    private MCTSExpectimaxPlayer player = null;

    private List<Types.ACTIONS> notExpandedActions = new ArrayList<>();         // Actions that are not represented by a Note
    public double value = 0;                                                    // total value
    public int visits = 0;                                                      // total number of visits
    private Random random;
    public int depth;

    /**
     * 
     * @param so the state of the node
     * @param actions	the action which leads from parentNode's state to this state ({@code null} for root node)
     * @param parentNode the parentNode node ({@code null} for root node)
     * @param random   a random number generator
     * @param player a reference to the one MCTS agent where {@code this} is part of (needed
     * 				to access several parameters of the MCTS agent)
     */
    public MCTSExpectimaxChanceNode(StateObservation so, Types.ACTIONS action, MCTSExpectimaxTreeNode parentNode, Random random, MCTSExpectimaxPlayer player) {
        this.so = so;
        this.action = action;
        this.parentNode = parentNode;
        this.player = player;
        this.random = random;

        notExpandedActions = so.getAvailableActions();

        if(parentNode != null) {
            depth = parentNode.depth + 1;
        } else {
            depth = 0;
        }
    }


    //ToDo: beschreibung anpassen
    //ToDo: alle Kommentare checken
    /**
     * Perform an MCTS search, i.e. a selection of the best next action given the state 
     * in the root node of the tree. <br>
     * Called by {@link MCTSExpectimaxPlayer#run(double[])}.<p>
     *
     * Do for {@code player.NUM_ITERS} iterations:
     * <ul>
     * <li> select a leaf node via {@link #treePolicy()} (this includes {@link #expand()} of 
     * 		not fully expanded nodes, as long as the maximum tree depth is not yet reached),
     * <li> make a {@link #rollOut()} starting from this leaf node (a game with random actions 
     * 		until game is over or until the maximum rollout depth is reached)
     * <li> {@link #backUp(MCTSExpectimaxChanceNode, double)} the resulting score {@code delta} and
     * 		the number of visits for all nodes on {@code value} and {@code visits}.
     * 		Do this for all nodes on the path from the leaf up to the root.
     * </ul>
     * 
     * Once this method is completed, the method {@link #bestAction()} will return the index
     * {@code i} of the root's childrenNodes which maximizes
     * <pre> U(i) = childrenNodes[i].value/childrenNodes[i].visits </pre>
     *
     * @param VTable on input an array of length K+1, where K is the number of available 
     * 		  moves for the root state. Contains on output {@code U(i)} in the first 
     * 		  K entries and the maximum of all {@code U(i)} in {@code VTable[K]}
     */
    public void mctsSearch(double[] VTable) {
        int iterations = 0;

        while (iterations < player.getNUM_ITERS()) {
            //select an childNode
            MCTSExpectimaxTreeNode selected = treePolicy();

            //rollout the childNode
            double score = selected.rollOut();

            //backup the score
            selected.backUp(score);

            iterations++;
        }

        // fill VTable
        for (int k = 0; k < so.getNumAvailableActions(); k++) {
            for (MCTSExpectimaxTreeNode child : childrenNodes) {
                if (child.action == so.getAction(k)) {
                    VTable[k] = child.value / child.visits;
                }
            }
        }
    }

    /**
     * Select the next {@link MCTSExpectimaxTreeNode} that should be evaluated
     *
     * If the current Node is not fully expanded a random unexpanded {@link MCTSExpectimaxTreeNode} will be chosen
     * If the current Node is fully expanded the {@link MCTSExpectimaxTreeNode} will be select with uct()
     *
     * @return the {@link MCTSExpectimaxTreeNode} that should be evaluated
     */
    private MCTSExpectimaxTreeNode treePolicy() {
        if(so.isGameOver()) {
            return this.parentNode;                             //equivalent to this in mcts
        }
        if (notExpandedActions.size() != 0 && depth < player.getTREE_DEPTH()) {
            return expand();
        } else {
            return uct().treePolicy().treePolicy();

            //ToDo: test egreedy and other selectionstrategys
        }
    }

    /**
     * Select the childnode with the lowest uct value
     *
     * @return the selected childnode
     */
    public MCTSExpectimaxTreeNode uct() {
        //ToDo: Problem: if gamescore is normalised uct chooses the least visited node, if the gamescore is not normalised uct chooses the same node most of the time (99% +)
        MCTSExpectimaxTreeNode selected = null;
        double selectedValue = -Double.MAX_VALUE;

        for (MCTSExpectimaxTreeNode child : childrenNodes)
        {
            double uctValue = child.value / child.visits + player.getK() * Math.sqrt(Math.log(visits + 1) / (child.visits)) + random.nextDouble() * epsilon; // small sampleRandom numbers: break ties in unexpanded node

            if (uctValue > selectedValue) {
                selected = child;
                selectedValue = uctValue;
            }
        }

        return selected;
    }

    /**
     * Expand the current node {@code this}, by randomly selecting one unexplored childnode
     * 
     * @return the selected childnode
     */
    public MCTSExpectimaxTreeNode expand() {
        Types.ACTIONS action = notExpandedActions.get(random.nextInt(notExpandedActions.size()));
        notExpandedActions.remove(action);

        assert so !=null : "Warning: state is null!";

        StateObserver2048 childSo = (StateObserver2048) so.copy();

        MCTSExpectimaxTreeNode child = new MCTSExpectimaxTreeNode(childSo, action, this, random,player);
        childrenNodes.add(child);
        return child;
    }

    public void backUp(double score) {
        visits++;
        value += score;

        if (parentNode != null) {
            parentNode.backUp(score);
        }
    }

    public Types.ACTIONS bestAction() {
        //ToDo: test other strategys to choose the best action

        Types.ACTIONS selected = null;
        double bestValue = -Double.MAX_VALUE;
        double currentValue;

        for (MCTSExpectimaxTreeNode child: childrenNodes) {
            currentValue = child.value / child.visits + random.nextDouble() * epsilon;
            if (currentValue > bestValue) {
                bestValue = currentValue;
                selected = child.action;
            }
        }

        return selected;
    }
}
