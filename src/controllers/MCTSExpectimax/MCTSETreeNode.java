package controllers.MCTSExpectimax;

import games.StateObsNondeterministic;
import games.StateObservation;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import tools.TestCompare;
import tools.Types;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * This class represents a MCTS Expectimax Tree Node. (Min/Max Node)
 * Each Tree Node has multiple {@link MCTSEChanceNode} children and one {@link MCTSEChanceNode} parent.
 * <p>
 * This version MCTSETreeNode  implements the {@link StateObservation} interface. 
 * 
 * @author Johannes Kutsch, 03.06.2017.
 * 
 * @see AltTreeNode
 */
public class MCTSETreeNode {
    public static double epsilon = 1e-6;		            // tiebreaker
    StateObservation so = null;
    public Types.ACTIONS action = null;		                // the action which leads from parent's state to this state
    private MCTSEChanceNode parentNode = null;
    private HashSet<MCTSEChanceNode> childrenNodes = new HashSet<>();
    public MCTSEPlayer player = null;
    public double value = 0;                                   // total value
    public int visits = 0;                                     // total number of visits
    public Random random;
    public int depth;


    /**
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

	public void printChildInfo(int nIndention, boolean doAssert) {
		DecimalFormat form = new DecimalFormat("0.0000");
		DecimalFormat for2 = new DecimalFormat("+0.0000;-0.0000");
		DecimalFormat ifor = new DecimalFormat("0000");
        double multiplier = 1/(player.getRootNode().maxRolloutScore+ this.epsilon);
		int cVisits = 0;
		int verbose = player.getVerbosity();
		String indention = "";
		for (int n = 0; n < nIndention; n++)
			indention += "  ";

		for (MCTSEChanceNode c : this.childrenNodes) {
			if (c != null) {
				cVisits += c.visits;
				if (verbose > 1) { 	// =2: print direct child info
					double uct_exploit = c.value * multiplier / (c.visits + this.epsilon);
					double uct_explore = player.getK()
							* Math.sqrt(Math.log(this.visits + 1) / (c.visits + this.epsilon));
					// System.out.println(c.m_state.stringDescr() + ": " +
					// c.nVisits + ", " +
					// form.format(c.totValue*3932156/c.nVisits)); // for 2048
					System.out.println(indention + c.so.stringActionDescr(c.action) + ": " + ifor.format(c.visits) + ", "
							+ for2.format(uct_exploit) + " + " + form.format(uct_explore) + " = "
							+ form.format(uct_exploit + uct_explore));
				}
				if (verbose > 2)	// =3: children+grandchildren, =4: 3 generations, =5: 4 gen, ...
					if (c.depth<=(verbose-2)) c.printChildInfo(nIndention + 1, false);
			}
		}
		if (doAssert)
			assert cVisits == this.visits : "children visits do not match the visits of this!";

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
    
	/**
	 * just for diagnostics:
	 * 
	 * @return number of nodes in the MCTSE tree
	 */
	public int numDescendants(int depth) {
		int N = 1; // include this
		Iterator<MCTSEChanceNode> it = childrenNodes.iterator();
		while (it.hasNext()) {
			MCTSEChanceNode c = it.next();
			if (c != null)
				N += c.numDescendants(depth+1);
//			if (depth==1) {
//				System.out.println("   "+N);
//				int dummy=1;
//			}
		}
		return N;
	}

}
