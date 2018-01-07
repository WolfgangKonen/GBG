package controllers.MCTSExpectimax.MCTSExpectimax1;

import controllers.MCTSExpectimax.MCTSEChanceNode;
import controllers.MCTSExpectimax.MCTSEPlayer;
import controllers.MCTSExpectimax.MCTSETreeNode;
import games.StateObsNondeterministic;
import tools.Types;

import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by Johannes on 03.06.2017.
 */
public class MCTSE1TreeNode extends MCTSETreeNode {
    private StateObsNondeterministic so = null;
    private HashMap<Types.ACTIONS, MCTSE1ChanceNode> childrenNodes = new HashMap<>();


    /**
     * This Class represents a MCTS Expectmiax Tree Node. (Min/Max Node)
     * Each Tree Node has multiple {@link MCTSE1ChanceNode} children and one {@link MCTSE1ChanceNode} parent.
     *
     *
     * This MCTSExpectimax Version implements the {@link StateObsNondeterministic} interface, using this interface we can increase the {@link controllers.MCTSExpectimax.MCTSExpectimaxAgt} speed by about 3-4%.
     * @param so         the unadvanced state of the parentNode
     * @param action	 the action which leads from parentNode's state to this state ({@code null} for root node)
     * @param parentNode the parentNode node
     * @param random     a random number generator
     * @param player     a reference to the one MCTS agent where {@code this} is part of (needed to access several parameters of the MCTS agent)
     */
    public MCTSE1TreeNode(StateObsNondeterministic so, Types.ACTIONS action, MCTSE1ChanceNode parentNode, Random random, MCTSEPlayer player) {
        super(so, action, parentNode, random, player);
        this.so = so;
    }

    /**
     * Expand the current node, by randomly selecting one child node
     *
     * @return the selected child node
     */
    public MCTSE1ChanceNode expand() {
        StateObsNondeterministic childSo = so.copy();

        MCTSE1ChanceNode child;
        Types.ACTIONS action = childSo.getNextNondeterministicAction();

        if(!childrenNodes.containsKey(action)) {
            //create a new child node
            childSo.advanceNondeterministic();
            child = new MCTSE1ChanceNode(childSo, action, this, random, player);
            childrenNodes.put(action, child);
        } else {
            //a child node representing this boardstate already exists
            child = childrenNodes.get(action);
        }

        return child;
    }
}
