package controllers.MCTSExpectimax;

import games.StateObsNondeterministic;
import tools.Types;

import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

/**
 * This class represents a MCTS Expectimax Tree Node. (Min/Max Node)
 * Each Tree Node has multiple {@link AltChanceNode} children and one {@link AltChanceNode} parent.
 * <p>
 * This <b>alternate</b> version {@link AltTreeNode}  implements the {@link StateObsNondeterministic} interface. 
 * Using this interface we can increase the {@link MCTSExpectimaxAgt}
 * speed by about 3-4%.
 *
 * @author Johannes Kutsch, 03.06.2017.
 * 
 * @see MCTSETreeNode
 */
public class AltTreeNode extends MCTSETreeNode {
    private StateObsNondeterministic so = null;
    
    // --- CAUTION --- /WK/ This shadows the definition of childrenNodes in the superclass (but
    // methods from the superclass may use the superclass' childrenNodes, which may be confusing)
    private HashMap<Types.ACTIONS, AltChanceNode> childrenNodes = new HashMap<>();

    /**
     * 
     * @param so         the unadvanced state of the parentNode
     * @param action	 the action which leads from parentNode's state to this state ({@code null} for root node)
     * @param parentNode the parentNode node
     * @param random     a random number generator
     * @param player     a reference to the one MCTS agent where {@code this} is part of (needed to access several parameters of the MCTS agent)
     */
    public AltTreeNode(StateObsNondeterministic so, Types.ACTIONS action, AltChanceNode parentNode, Random random, MCTSEPlayer player) {
        super(so, action, parentNode, random, player);
        this.so = so;
    }

    /**
     * Expand the current node, by randomly selecting one child node
     *
     * @return the selected child node
     */
    public AltChanceNode expand() {
        StateObsNondeterministic childSo = so.copy();

        AltChanceNode child;
        Types.ACTIONS action = childSo.getNextNondeterministicAction();

        if(!childrenNodes.containsKey(action)) {
            //create a new child node
            childSo.advanceNondeterministic();
            child = new AltChanceNode(childSo, action, this, random, m_player);
            childrenNodes.put(action, child);
        } else {
            //a child node representing this boardstate already exists
            child = childrenNodes.get(action);
        }

        return child;
    }
}
