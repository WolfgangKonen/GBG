package controllers.MCTSExpectimax;

import games.StateObservation;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import tools.TestCompare;
import tools.Types;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by Johannes on 03.06.2017.
 */
public class MCTSETreeNode {
    public static double epsilon = 1e-6;		            // tiebreaker
    private StateObservation so = null;
    public Types.ACTIONS action = null;		                // the action which leads from parent's state to this state
    private MCTSEChanceNode parentNode = null;
    private HashSet<MCTSEChanceNode> childrenNodes = new HashSet<>();
    public MCTSEPlayer player = null;
    public double value = 0;                                   // total value
    public int visits = 0;                                     // total number of visits
    public Random random;
    public int depth;


    /**
     * This Class represents a MCTS Expectmiax Tree Node. (Min/Max Node)
     * Each Tree Node has multiple {@link MCTSEChanceNode} children and one {@link MCTSEChanceNode} parent.
     *
     * @param so         the unadvanced state of the parentNode
     * @param action	 the action which leads from parentNode's state to this state ({@code null} for root node)
     * @param parentNode the parentNode node
     * @param random     a random number generator
     * @param player     a reference to the one MCTS agent where {@code this} is part of (needed to access several parameters of the MCTS agent)
     */
    public MCTSETreeNode(StateObservation so, Types.ACTIONS action, MCTSEChanceNode parentNode, Random random, MCTSEPlayer player) {
        this.so = so;
        this.action = action;
        this.parentNode = parentNode;
        this.player = player;
        this.random = random;
        this.depth = parentNode.depth;

        player.getRootNode().numberTreeNodes++;
    }

    /**
     * Select the next {@link MCTSEChanceNode} that should be evaluated
     *
     * Because the next boardstate is always random the child node will be chosen randomly using the action that this {@link MCTSETreeNode} represents.
     *
     * @return the {@link MCTSETreeNode} that should be evaluated
     */
    public MCTSEChanceNode treePolicy() {
        return expand();
    }

    /**
     * Expand the current node, by randomly selecting one child node
     *
     * @return the selected child node
     */
    public MCTSEChanceNode expand() {
//        StateObserver2048 childSo = (StateObserver2048) so.copy();		// /WK/ can be done w/o using StateObserver2048:
        StateObservation childSo = so.copy();
        childSo.advance(action);
    	
        for (MCTSEChanceNode childrenNode : childrenNodes) {
            if (childrenNode.so.equals(childSo)) {
                //a child node representing this boardstate already exists
                return childrenNode;
            }
        }

        //create a new child node
        MCTSEChanceNode child = new MCTSEChanceNode(childSo, null, this, random, player);
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
        backUpSum(score);
    }

    /**
     * The value of a node is the sum of all it childnodes
     *
     * @param score the value of the new childnode
     */
    private void backUpSum(double score) {
        if (so.getNumPlayers()==2) {
            score =- score;
        }

        visits++;
        value += score;

        if (parentNode != null) {
            parentNode.backUp(score);
        }
    }

    /**
     * The value of a node is the value of the lowest childnode
     *
     * @param score the value of the new childnode
     */
    private void backUpMin(double score) {
        if (so.getNumPlayers()==2) {
            score =- score;
        }

        visits++;
        if(score < value || value == 0) {
            value = score;
        }

        if (parentNode != null) {
            parentNode.backUp(score);
        }
    }
}
