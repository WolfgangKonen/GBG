package controllers.MCTSExpectimax;

import games.StateObsNondeterministic;
import tools.Types;

import java.util.Random;

/**
 * This Class represents a MCTS Expectmiax Chance Node.
 * Each Chance Node has multiple {@link AltTreeNode} children and one {@link AltTreeNode} parent.
 * <p>
 * This is adapted from Diego Perez MCTS reference implementation
 * 		http://gvgai.net/cont.php
 * (with a bug fix concerning the number of available actions and an 
 * extension for 1- and 2-player games, adjusted for nondeterministic
 * games and the return of VTable information.)
 * <p>
 * This <b>alternate</b> version {@link AltChanceNode} implements the {@link StateObsNondeterministic} interface. 
 * Using this interface we can increase the {@link MCTSExpectimaxAgt} speed by about 3-4%.
 *  
 * @author Johannes Kutsch
 */

public class AltChanceNode extends MCTSEChanceNode
{
    private StateObsNondeterministic so = null;

    /**
     *
     * @param so            the state of the node
     * @param action	    the action which leads from parentNode's state to this state ({@code null} for root node)
     * @param parentNode    the parentNode node ({@code null} for root node)
     * @param random        a random number generator
     * @param player        a reference to the one MCTS agent where {@code this} is part of (needed to access several parameters of the MCTS agent)
     */
    public AltChanceNode(StateObsNondeterministic so, Types.ACTIONS action, MCTSETreeNode parentNode, Random random, MCTSEPlayer player) {
        super(so, action, parentNode, random, player);
        this.so = so;
    }

    /**
     * Expand the current node, by randomly selecting one unexplored child node
     * 
     * @return the selected child node
     */
    public AltTreeNode expand() {
        Types.ACTIONS action = notExpandedActions.get(m_rnd.nextInt(notExpandedActions.size()));
        notExpandedActions.remove(action);

        StateObsNondeterministic childSo = so.copy();
        childSo.advanceDeterministic(action);

        AltTreeNode child = new AltTreeNode(childSo, action, this, m_rnd, m_player);
        childrenNodes.add(child);
        return child;
    }
}
