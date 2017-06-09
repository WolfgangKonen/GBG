package controllers.MCTSExpectimax;

import games.StateObservationNondeterministic;
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

public class MCTSExpectimaxChanceNode1
{
    public static double epsilon = 1e-6;		                                // tiebreaker
    private StateObservationNondeterministic so = null;
    private Types.ACTIONS action = null;		                                // the action which leads from parent's state to this state
    private MCTSExpectimaxTreeNode1 parentNode = null;
    private List<MCTSExpectimaxTreeNode1> childrenNodes = new ArrayList<>();
    private MCTSExpectimaxPlayer1 player = null;

    private List<Types.ACTIONS> notExpandedActions = new ArrayList<>();         // Actions that are not represented by a Note
    public double value = 0;                                                    // total value
    public int visits = 0;                                                      // total number of visits
    private Random random;
    public int depth;

    /**
     * This Class represents a MCTS Expectmiax Chance Node.
     * Each Chance Node has multiple {@link MCTSExpectimaxTreeNode1} children and one {@link MCTSExpectimaxTreeNode1} parent.
     *
     * @param so            the state of the node
     * @param action	    the action which leads from parentNode's state to this state ({@code null} for root node)
     * @param parentNode    the parentNode node ({@code null} for root node)
     * @param random        a random number generator
     * @param player        a reference to the one MCTS agent where {@code this} is part of (needed to access several parameters of the MCTS agent)
     */
    public MCTSExpectimaxChanceNode1(StateObservationNondeterministic so, Types.ACTIONS action, MCTSExpectimaxTreeNode1 parentNode, Random random, MCTSExpectimaxPlayer1 player) {
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

    /**
     * Perform an MCTS Expectimax search, i.e. a selection of the best next action given the state
     * in the root node of the tree. The tree consists of alternating layers of {@link MCTSExpectimaxTreeNode1}
     * and {@link MCTSExpectimaxChanceNode1}.
     *
     * Called by {@link MCTSExpectimaxPlayer1#run(double[])}.
     *
     * Do for {@code player.NUM_ITERS} iterations
     * --- select a {@link MCTSExpectimaxTreeNode1} leaf node via {@link #treePolicy()} (this
     *     includes {@link #expand()} of not fully expanded nodes, as long as the maximum tree
     *     depth is not yet reached)
     * --- make a {@link MCTSExpectimaxTreeNode1#rollOut()} starting from this leaf node (a game
     *     with random actions until game is over or until the maximum rollout depth is reached)
     * --- {@link #backUp(double)} the resulting score {@code delta} and
     *     the number of visits for all nodes on {@code value} and {@code visits}.
     * --- Do this for all nodes on the path from the leaf up to the root.
     *
     * 
     * Once this method is completed, the method {@link #bestAction()} will return the
     * action {@link Types.ACTIONS} of the root's childrenNodes which maximizes
     * U(i) = childrenNodes[i].value/childrenNodes[i].visits
     *
     * @param vTable on input an array of length K+1, where K is the number of available
     * 		  moves for the root state. Contains on output {@code U(i)} in the first 
     * 		  K entries and the maximum of all {@code U(i)} in {@code vTable[K]}
     */
    public void mctsSearch(double[] vTable) {
        int iterations = 0;

        while (iterations < player.getNUM_ITERS()) {
            //select an childNode
            MCTSExpectimaxTreeNode1 selected = treePolicy();

            //rollout the childNode
            double score = selected.rollOut();

            //backup the score
            selected.backUp(score);

            iterations++;
        }

        // fill vTable
        for (int k = 0; k < so.getNumAvailableActions(); k++) {
            for (MCTSExpectimaxTreeNode1 child : childrenNodes) {
                if (child.action == so.getAction(k)) {
                    vTable[k] = child.value / child.visits;
                }
            }
        }
    }

    /**
     * Select the next {@link MCTSExpectimaxTreeNode1} that should be evaluated
     *
     * If the current Node is not fully expanded a random unexpanded {@link MCTSExpectimaxTreeNode1} will be chosen
     * If the current Node is fully expanded the {@link MCTSExpectimaxTreeNode1} will be select with uct()
     *
     * @return the {@link MCTSExpectimaxTreeNode1} that should be evaluated
     */
    private MCTSExpectimaxTreeNode1 treePolicy() {
        if(so.isGameOver()) {
            return parentNode;                             //equivalent to this in mcts
        }
        if (notExpandedActions.size() != 0 && depth < player.getTREE_DEPTH()) {
            return expand();
        } else {
            //recursively go down the three
            //select a child with uct(), select/create a chance node with treePolicy() and call this method in the new chance node
            return uct().treePolicy().treePolicy();

            //ToDo: test egreedy and other selectionstrategys
        }
    }

    /**
     * Select the child node with the highest uct value
     *
     * @return the selected child node
     */
    private MCTSExpectimaxTreeNode1 uct() {
        //ToDo: Problem: if gamescore is normalised uct always chooses the least visited node, if the gamescore is not normalised uct chooses the same node most of the time (99.9% +)
        MCTSExpectimaxTreeNode1 selected = null;
        double selectedValue = -Double.MAX_VALUE;

        for (MCTSExpectimaxTreeNode1 child : childrenNodes)
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
     * Expand the current node, by randomly selecting one unexplored child node
     * 
     * @return the selected child node
     */
    private MCTSExpectimaxTreeNode1 expand() {
        Types.ACTIONS action = notExpandedActions.get(random.nextInt(notExpandedActions.size()));
        notExpandedActions.remove(action);

        StateObservationNondeterministic childSo = so.copy();
        childSo.advanceDeterministic(action);

        MCTSExpectimaxTreeNode1 child = new MCTSExpectimaxTreeNode1(childSo, action, this, random,player);
        childrenNodes.add(child);
        return child;
    }

    /**
     * Backup the score through all parent nodes, until the root node is reached
     * calls itself recursively in each parent node
     *
     * @param score the score that we want to backup
     */
    public void backUp(double score) {
        visits++;
        value += score;

        if (parentNode != null) {
            parentNode.backUp(score);
        }
    }

    /**
     * Selects the best Action of an expanded tree
     *
     * @return the {@link Types.ACTIONS} of the root's childrenNodes which maximizes U(i) = childrenNodes[i].value/childrenNodes[i].visits
     */
    public Types.ACTIONS bestAction() {
        //ToDo: test other strategys to choose the best action

        Types.ACTIONS selected = null;
        double bestValue = -Double.MAX_VALUE;
        double currentValue;

        for (MCTSExpectimaxTreeNode1 child: childrenNodes) {
            currentValue = child.value / child.visits + random.nextDouble() * epsilon;
            if (currentValue > bestValue) {
                bestValue = currentValue;
                selected = child.action;
            }
        }

        return selected;
    }
}
