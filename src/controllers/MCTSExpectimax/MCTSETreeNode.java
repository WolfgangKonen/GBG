package controllers.MCTSExpectimax;

import controllers.MCTSWrapper.utils.Tuple;
import games.StateObsNondeterministic;
import games.StateObservation;
import tools.ScoreTuple;
import tools.Types;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Random;

/**
 * This class represents an MCTS Expectimax Tree Node. (Min/Max Node).
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
	/**
	 * If {@code WITH_PARTIAL==true}, complete a possible partial state when reaching {@link #expand()}.
	 * <p>
	 * If {@code WITH_PARTIAL==false}, no partial state completion takes place in {@link #expand()}
	 * (<b>not</b> recommended, just for comparing with the status prior to 2021-09).
	 */
	public static boolean WITH_PARTIAL=true;

	/**
	 * switch what to return in {@link #getQ()}, see discussion {@link #getQ() there}
	 */
	public static boolean QDIRECT = true;

	public static double epsilon = 1e-6;		            // tiebreaker
    /**
     * the parent node's state
     */
    StateObservation so;
    /**
     * the action which leads from parent node's state to this state
     */
    public Types.ACTIONS action;
    private final MCTSEChanceNode parentNode;
	/**
	 * The set of CHANCE child tuples, where each tuple carries the child node
	 * in element1 and the probability of selecting this child node in element2
	 */
	private final HashSet<Tuple<MCTSEChanceNode,Double>> childrenNodes = new HashSet<>();
    public MCTSEPlayer m_player;
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
	 * The value Q(e) of EXPECTIMAX node e (={@code this}) is formed by calculating the weighted average of (Q_c/N_c) for all
	 * child nodes c, where the weight for node c is the probability of the nondeterministic action that leads from e to c.
	 * Finally, the weighted average is multiplied by N, the number of visits to e, because the PUCT formula calculates
	 * Q(e)/N. This is the result if switch {@link #QDIRECT}{@code ==false} in source code.
	 * <p>
	 * [Earlier we used instead {@code this.value}, which is formed by accumulating the backups during search. In the limit of
	 * large N, both approaches yield very similar values, although not identical. But if node e is <b>not</b> visited very
	 * often (e.g. deeper down the tree), the formula for switch {@link #QDIRECT}{@code ==false} should be more precise, since it puts rare events
	 * directly in the right context.
	 * <p>
	 * However, we found that the earlier version yields better results for KuhnPoker, so we retain the earlier
	 * version in case {@link #QDIRECT}{@code ==true} in source code. - However, with the new switch {@link #WITH_PARTIAL}{@code ==true}
	 * and with {@link games.KuhnPoker.StateObserverKuhnPoker#PLAY_ONE_ROUND_ONLY PLAY_ONE_ROUND_ONLY}{@code ==true}, we
	 * see again that both versions of {@link #QDIRECT} work equally well.]
	 *
	 * @return the value of this Expectimax node
	 */
	double getQ() {
		if (MCTSETreeNode.QDIRECT) {
			return this.value; 		// this was effective before 09/2021
		} else {
			// the following is a bit longer, but more precise in the case of fewer visits to this.
			// However, it turns out to be a bit worse for KuhnPoker.
			double qValue=0.0;
			for (Tuple<MCTSEChanceNode,Double> childTuple : childrenNodes) {
				MCTSEChanceNode childNode = childTuple.element1;
				Double prob = childTuple.element2;
				qValue += prob * (childNode.value/ childNode.visits);
			}
			qValue *= this.visits;

			// only debug & comparison with this.value:
			boolean DBG_GETQ=false;
			if (DBG_GETQ) {
				if (depth==0) {
					StateObsNondeterministic thisSO = (StateObsNondeterministic) this.so.copy();
					thisSO.advanceDeterministic(this.action);
					int nrands = thisSO.getAvailableRandoms().size();
					int nchilds = this.childrenNodes.size();
					DecimalFormat frm = new DecimalFormat("0.0000");
					System.out.println("d="+depth+", value="+frm.format(value)+", qValue="+frm.format(qValue)
							+", v/qV="+frm.format(value/qValue)
							+", nrand="+nrands+","+nchilds+", nVisits="+visits);
				}
			}

			return qValue;
		}
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
     * More precisely: <ul>
	 *     <li> Take the parent node's state {@link #so} and advance it by {@link #action}.
     * This advance contains the deterministic and the non-deterministic part, so it can end up 
     * in various CHANCE child nodes.
	 *     <li> If the child is a partial state (and if {@link #WITH_PARTIAL} is true), then complete it randomly to a
	 *  full state. (This is important for games like Kuhn Poker in order to correctly average <i>early enough</i> over
	 *  the possible completions for the imperfect information.)
	 *     <li> Now see if such a child node already exists in {@link #childrenNodes},
     * if so, return it. If not, create and return a new one and add it to {@link #childrenNodes}.
     * </ul>
     * @return the selected child node
     */
    public MCTSEChanceNode expand() {
		Double dprob;
		boolean stopOnRoundOver = m_player.getParMCTSE().getStopOnRoundOver();
		StateObsNondeterministic childSo = (StateObsNondeterministic) so.copy();
        childSo.advanceDeterministic(action);
        if (childSo.isNextActionDeterministic()) {
        	// the deterministic advance results in a next-action-deterministic state (this happens for example in
			// Poker, if the 1st player bets or checks. Now an action is required from the 2nd player, no card is dealt)
			// --> we model such a case with a tree node that has only one 'random' action 'do nothing' and thus results
			// with probability 1.0 in the CHANCE child {@code childSO}.
        	dprob = 1.0;
		} else {
        	// the normal case: the deterministic advance results in a next-action-NOT-deterministic state
			Types.ACTIONS randAct = childSo.advanceNondeterministic();
			dprob = childSo.getProbability(randAct);	// the probability that environment selects this random action
		}

        if (WITH_PARTIAL && childSo.isPartialState()) {
			Tuple<StateObservation,Double> tup = childSo.completePartialState();
			dprob *= tup.element2;
		}

    	if(childSo.isRoundOver() && !stopOnRoundOver)	// /WK/03/2021 Bug fix '&& !stopOnRoundOver' as suggested by TZ
    		childSo.initRound();

        for (Tuple<MCTSEChanceNode,Double> childTuple : childrenNodes) {
			MCTSEChanceNode childNode = childTuple.element1;
//          if (childNode.so.equals(childSo)) {		// OLD (before 03/2021) AND WRONG !!
			if (childNode.so.stringDescr().equals(childSo.stringDescr())) {
                return childNode;	//a child node representing this state already exists
            }
        }

        // *** this code shows why the old childrenNode.so.equals(childSo) above was WRONG ***
//		String s1 = "aaa";
//		String s2 = "aaa";
//		String s3 = s1;
//		System.out.println("s1 s2 "+ s1.equals(s2));
//		System.out.println("s1 s3 "+ s1.equals(s3));
//		childSo = so.copy();
//		System.out.println("so childSo direct "+ so.equals(childSo));   // returns false
//		System.out.println("so childSo strings "+ so.stringDescr().equals(childSo.stringDescr()));   // returns true

		//create a new child node
        MCTSEChanceNode child = new MCTSEChanceNode(childSo, null, this, random, m_player);
        childrenNodes.add(new Tuple<>(child,dprob));

        // for unclear reasons, this assertion does fire (unwantedly) if WITH_PARTIAL==true --> TODO
//        if (dprob==1.0)
//			assert childrenNodes.size()==1 : "Too many children!";
//			// in the next-action-deterministic-case and without partial completion, this node should have exactly one child

        return child;
    }

    /**
     * Backup the score through all parent nodes, until the root node is reached
     * calls itself recursively in each parent node
     *
	 * @param delta the reward vector returned from {@link MCTSEChanceNode#rollOut()}
     */
    public void backUp(ScoreTuple delta) {
        backUpSum(delta);
    }

    /**
     * The value of a node is the sum of all its child nodes
     *
	 * @param delta the reward vector returned from {@link MCTSEChanceNode#rollOut()}
     */
    private void backUpSum(ScoreTuple delta) {
        visits++;
		if (parentNode != null) {
			// Why do we test on parentNode here? - Because we need parentNode to know the player
			// preceding n's player. But isn't this incomplete because we then never accumulate the root node's
			// value? - No, it is not incomplete, because a node's value is only needed
			// for nodes n being *children* of some other nodes (see bestAction() and uct()). And the root
			// node is not the child of anyone.
			// [Note that uct() needs mroot.visits, that's why we increment visits for all n.]
			int pPlayer = parentNode.so.getPlayer();	// pPlayer: the player of the preceding CHANCE node
			value += delta.scTup[pPlayer];	// backup delta for pPlayer

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
	 * just for diagnostics:
	 * 
	 * @return number of nodes in the MCTSE tree
	 */
	public int numDescendants(int depth) {
		int N = 1; // include this
		for (Tuple<MCTSEChanceNode,Double> childTuple : childrenNodes) {
			MCTSEChanceNode c = childTuple.element1;
			if (c != null)
				N += c.numDescendants(depth + 1);
//			if (depth==1) {
//				System.out.println("   "+N);
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
        double multiplier = 1/(m_player.getRootNode().maxRolloutScore+ MCTSETreeNode.epsilon);
		int cVisits = 0;
		int verbose = m_player.getVerbosity();
		String indention = "";
		for (int n = 0; n < nIndention; n++)
			indention += "  ";

		for (Tuple<MCTSEChanceNode,Double> childTuple : childrenNodes) {
			MCTSEChanceNode c = childTuple.element1;
			if (c != null) {
				cVisits += c.visits;
				if (verbose > 1) { 	// =2: print direct child info
					double uct_exploit = c.value * multiplier / (c.visits + MCTSETreeNode.epsilon);
					double uct_explore = m_player.getK()
							* Math.sqrt(Math.log(this.visits + 1) / (c.visits + MCTSETreeNode.epsilon));
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
