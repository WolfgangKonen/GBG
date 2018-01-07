package controllers.MCTSExpectimax.MCTSExpectimax1;

import controllers.MCTSExpectimax.MCTSEChanceNode;
import controllers.MCTSExpectimax.MCTSEPlayer;
import controllers.MCTSExpectimax.MCTSETreeNode;
import games.StateObsNondeterministic;
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
 *  This MCTSExpectimax Version implements the {@link StateObsNondeterministic} interface, using this interface we can increase the {@link controllers.MCTSExpectimax.MCTSExpectimaxAgt} speed by about 3-4%.
 *
 *  @author Johannes Kutsch
 */

public class MCTSE1ChanceNode extends MCTSEChanceNode
{
    private StateObsNondeterministic so = null;

    /**
     * This Class represents a MCTS Expectmiax Chance Node.
     * Each Chance Node has multiple {@link MCTSE1TreeNode} children and one {@link MCTSE1TreeNode} parent.
     *
     * @param so            the state of the node
     * @param action	    the action which leads from parentNode's state to this state ({@code null} for root node)
     * @param parentNode    the parentNode node ({@code null} for root node)
     * @param random        a random number generator
     * @param player        a reference to the one MCTS agent where {@code this} is part of (needed to access several parameters of the MCTS agent)
     */
    public MCTSE1ChanceNode(StateObsNondeterministic so, Types.ACTIONS action, MCTSETreeNode parentNode, Random random, MCTSEPlayer player) {
        super(so, action, parentNode, random, player);
        this.so = so;
    }

    /**
     * Expand the current node, by randomly selecting one unexplored child node
     * 
     * @return the selected child node
     */
    public MCTSE1TreeNode expand() {
        Types.ACTIONS action = notExpandedActions.get(random.nextInt(notExpandedActions.size()));
        notExpandedActions.remove(action);

        StateObsNondeterministic childSo = so.copy();
        childSo.advanceDeterministic(action);

        MCTSE1TreeNode child = new MCTSE1TreeNode(childSo, action, this, random, player);
        childrenNodes.add(child);
        return child;
    }
}
