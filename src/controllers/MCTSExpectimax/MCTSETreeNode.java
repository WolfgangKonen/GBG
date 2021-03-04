package controllers.MCTSExpectimax;

import games.StateObsNondeterministic;
import games.StateObservation;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import tools.Types;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * This class represents a MCTS Expectimax Tree Node. (Min/Max Node).
 * <p>
 * Each {@code MCTSETreeNode} has multiple {@link MCTSEChanceNode} children and one {@link MCTSEChanceNode} parent.
 *
 * An Expectimax Tree node is reached after a deterministic action. The next action(s) are nondeterministic (environment)
 * actions.
 *
 * @author Johannes Kutsch, 03.06.2017, adapted by Wolfgang Konen 2021
 * 
 * @see AltTreeNode
 */
public class MCTSETreeNode {
    public static double epsilon = 1e-6;		            // tiebreaker
    /**
     * the parent node's state
     */
    StateObservation so = null;
    /**
     * the action which leads from parent node's state to this state
     */
    public Types.ACTIONS action = null;		                
    private MCTSEChanceNode parentNode = null;
    private HashSet<MCTSEChanceNode> childrenNodes = new HashSet<>();
    public MCTSEPlayer m_player = null;
    public double value = 0;                                   // total value
    public int visits = 0;                                     // total number of visits
    public Random random;
    public int depth;
	/**
	 * cumulative probability, needed in {@link MCTSEChanceNode#rouletteWheel()}
	 */
    double cumProb=0;		 		


    /**
     * @param so         the (not advanced) state of the parentNode
     * @param action	 the action which leads from parentNode's state to this state ({@code null} for root node)
     * @param parentNode the parentNode node
     * @param random     a random number generator
     * @param player     a reference to the one {@link MCTSEPlayer} where {@code this} is part of (needed to access several parameters of the MCTS agent)
     */
    public MCTSETreeNode(StateObservation so, Types.ACTIONS action, MCTSEChanceNode parentNode, Random random, MCTSEPlayer player) {
        this.so = so;   //.copy();	// /WK/03/2021/ added .copy()		// removed again
        this.action = action;
        this.parentNode = parentNode;
        this.m_player = player;
        this.random = random;
        this.depth = parentNode.depth;

        player.getRootNode().numberTreeNodes++;
    }

    MCTSEChanceNode getParentNode() {
    	return parentNode;
	}

    /**
     * Select the next {@link MCTSEChanceNode} that should be evaluated.
     * <p>
     * Because the next state is always random, the child node will be chosen randomly using the 
     * action that leads to this {@link MCTSETreeNode}.
     *
     * @return the {@link MCTSEChanceNode} that should be evaluated
     */
    public MCTSEChanceNode treePolicy() {
        return expand();
    }

    /**
     * Expand the current node, by randomly selecting one child node.
     * <p>
     * More precisely: take the parent node's state {@link #so} and advance it by {@link #action}.
     * This advance contains the deterministic and the non-deterministic part, so it can end up 
     * in various child nodes. See if such a child node already exists in {@link #childrenNodes}, 
     * if so, return it. If not, create and return a new one and add it to {@link #childrenNodes}.
     * 
     * @return the selected child node
     */
    public MCTSEChanceNode expand() {
//        StateObserver2048 childSo = (StateObserver2048) so.copy();		// /WK/ can be done w/o using StateObserver2048:
        StateObservation childSo = so.copy();
        childSo.advance(action);
    	if(childSo.isRoundOver())
    		childSo.initRound();

        for (MCTSEChanceNode childrenNode : childrenNodes) {
//          if (childrenNode.so.equals(childSo)) {		// OLD (before 03/2021) AND WRONG !!
			if (childrenNode.so.stringDescr().equals(childSo.stringDescr())) {
                //a child node representing this state already exists
                return childrenNode;
            }
        }

        // *** this code shows why the old childrenNode.so.equals(childSo) above was wrong ***
//		String s1 = "aaa";
//		String s2 = "aaa";
//		String s3 = s1;
//		System.out.println("s1 s2 "+ s1.equals(s2));
//		System.out.println("s1 s3 "+ s1.equals(s3));
//		System.out.println("so childSo direct "+ so.equals(childSo));
//		System.out.println("so childSo strings "+ so.stringDescr().equals(childSo.stringDescr()));


		//create a new child node
        MCTSEChanceNode child = new MCTSEChanceNode(childSo, null, this, random, m_player);
        childrenNodes.add(child);

        return child;
    }

    /**
     * Backup the score through all parent nodes, until the root node is reached
     * calls itself recursively in each parent node
     *
	 * @param delta the reward vector returned from {@link MCTSEChanceNode#rollOut()}
     */
    public void backUp(double[] delta) {
        backUpSum(delta);
    }

    /**
     * The value of a node is the sum of all it child nodes
     *
	 * @param delta the reward vector returned from {@link MCTSEChanceNode#rollOut()}
     */
    private void backUpSum(double[] delta) {
        visits++;
		if (parentNode != null) {
			// Why do we test on parentNode here? - Because we need parentNode to know the player
			// preceding n's player. But isn't this incomplete because we then never accumulate the root node's
			// value? - No, it is not incomplete, because a node's value is only needed
			// for nodes n being *children* of some other nodes (see bestAction() and uct()). And the root
			// node is not the child of anyone.
			// [Note that uct() needs mroot.visits, that's why we increment visits for all n.]
			int pPlayer = parentNode.so.getPlayer();	// pPlayer: the player of the preceding CHANCE node
			value += delta[pPlayer];	// backup delta for pPlayer

			parentNode.backUp(delta);
		}
		// Why pPlayer? - This is for the same reason why we call in backUp2Player() negate *before* the
		// first '+=' to the node's value is made: If the result of a random roll-out for a leaf n is a loss
		// for n, this does not really count. What counts is the result for pPlayer, the player who
		// *created* n. Why? Because pPlayer looks for the best action among its children, and if child
		// n is advantageous for pPlayer, it should have a high totValue. So we have to accumulate in
		// n.value the rewards achievable from the perspective of pPlayer.
	}

    /**
     * The value of a node is the value of the lowest child node
     * (This method is currently not used)
     *
     * @param score the value of the new child node
     */
//    @Deprecated
//    private void backUpMin(double score) {
//    	score = baUpNegateScore(score);
//
//        visits++;
//        if(score < value || value == 0) {
//            value = score;
//        }
//
//        if (parentNode != null) {
//            parentNode.backUp(score);
//        }
//    }

	// --- never used anymore ---
//    /**
//     * Optionally negate the score (in case of 2-player games) before backing it up
//     */
//	private double baUpNegateScore(double score) {
//		switch (so.getNumPlayers()) {
//		case (1):
//			break;
//		case (2):	// negamax variant for 2-player tree
//			// Why do we call 'negate' *before* the first change ('+=') to this.value is made? -
//			// If the score of 'selected' is a loss for the player who has to move on
//			// 'selected', then it is a win for the player who created 'selected'
//			// (negamax principle)
//			score = negate(score);
//			break;
//		default: // i.e. n-player, n>2
//			throw new RuntimeException("MCTSE's backUp is not yet implemented for (n>2)-player games (n>2).");
//		}
//		return score;
//	}

	// --- never used anymore ---
//	private double negate(double delta) {
//		if (m_player.getNormalize()) {
//			// map a normalized delta \in [0,1] again to [0,1], but reverse the order.
//			// /WK/ "1-" is the bug fix 2019-02-09 needed to achieve always child.totValue>=0
//			return 1-delta;
//		} else {
//			// reverse the delta-order for arbitrarily distributed delta;
//			// maps from interval [a,b] to [-b,-a] (this can be problematic for UCT-rule)
//			return -delta;
//		}
//	}
	
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

	/**
	 * Needed by {@link MCTSEChanceNode#printChildInfo(int, boolean)}
	 * 
	 * @param nIndention	indent text by 2*nIndention white spaces
	 * @param doAssert		whether to do assertions
	 */
	public void printChildInfo(int nIndention, boolean doAssert) {
		DecimalFormat form = new DecimalFormat("0.0000");
		DecimalFormat for2 = new DecimalFormat("+0.0000;-0.0000");
		DecimalFormat ifor = new DecimalFormat("0000");
        double multiplier = 1/(m_player.getRootNode().maxRolloutScore+ this.epsilon);
		int cVisits = 0;
		int verbose = m_player.getVerbosity();
		String indention = "";
		for (int n = 0; n < nIndention; n++)
			indention += "  ";

		for (MCTSEChanceNode c : this.childrenNodes) {
			if (c != null) {
				cVisits += c.visits;
				if (verbose > 1) { 	// =2: print direct child info
					double uct_exploit = c.value * multiplier / (c.visits + this.epsilon);
					double uct_explore = m_player.getK()
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

}
