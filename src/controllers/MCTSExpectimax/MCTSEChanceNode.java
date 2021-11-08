package controllers.MCTSExpectimax;

import games.StateObservation;
import params.ParMCTSE;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import tools.ScoreTuple;
import tools.Types;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class represents a MCTS Expectimax (MCTSE) Chance node.
 * Each Chance node has multiple {@link MCTSETreeNode} children and one {@link MCTSETreeNode} parent.
 * The MCTSE tree starts with a Chance node and has Chance nodes at all leaves.
 *
 * A Chance node is reached after nondeterministic (environment) action(s). The next action is a deterministic
 * one calculated by the agent.
 *
 * @author Johannes Kutsch, 2017, adapted by Wolfgang Konen, 2021
 */
public class MCTSEChanceNode
{
    private static final double epsilon = 1e-6;		                                // tiebreaker
    /**
     * the state observation associated with this node
     */
    public StateObservation so;
    /**
     * member {@code #action} is only used for printout in {@link #printChildInfo(int, boolean)}
     */
    public Types.ACTIONS action;
    private final MCTSETreeNode parentNode;
    public List<MCTSETreeNode> childrenNodes = new ArrayList<>();
    public MCTSEPlayer m_player;

    public ArrayList<Types.ACTIONS> notExpandedActions;         // Actions that are not represented by a node
    public double value = 0;   				// total value
    public int visits = 0; 					// total number of visits
    public Random m_rnd;    // do we need it separately in each node or could it be once in MCTSEPlayer ??
    public int depth;

    public int iterations = 0;
    public int numberTreeNodes = 0;
    public int numberChanceNodes = 1;

    public double maxRolloutScore = 0;  	//The max score that was reached during a rollout

    /**
     * This Class represents a MCTS Expectimax Chance Node.
     * Each Chance Node has multiple {@link MCTSETreeNode} children and one {@link MCTSETreeNode} parent.
     *
     * @param so            the state of the node
     * @param action	    the action which leads from parentNode's state to this state ({@code null} for root node)
     * @param parentNode    the parentNode node ({@code null} for root node)
     * @param random        a random number generator
     * @param player        a reference to the one MCTS agent where {@code this} is part of (needed to access several parameters of the MCTS agent)
     */
    public MCTSEChanceNode(StateObservation so, Types.ACTIONS action, MCTSETreeNode parentNode, Random random, MCTSEPlayer player) {
        this.so = so;  //.copy();        // /WK/03/2021/ added .copy() // removed again
        this.action = action;
        this.parentNode = parentNode;
        this.m_player = player;
        this.m_rnd = random;

        notExpandedActions = (ArrayList<Types.ACTIONS>) so.getAvailableActions().clone();

        if(player.getRootNode() != null) {
            player.getRootNode().numberChanceNodes++;
        }

        if (parentNode != null) {
            depth = parentNode.depth + 1;
        } else {
            depth = 0;
        }
    }

    /**
     * Perform an MCTS Expectimax search, i.e. a selection of the best next action given the state
     * in the root node of the tree. The tree consists of alternating layers of {@link MCTSETreeNode}
     * and {@link MCTSEChanceNode}.
     * <p>
     * Called by {@link MCTSEPlayer#run(double[])}.
     * <p>
     * Do for {@code player.NUM_ITERS} iterations:
     * <ul>
     * <li> select a {@link MCTSEChanceNode} leaf node via {@link #treePolicy()} (this
     *      includes {@link #expand()} of not fully expanded nodes, as long as the maximum tree
     *      depth is not yet reached)
     * <li> make a {@link #rollOut()} starting from this leaf node (a game
     *      with random actions until game is over or until the maximum rollout depth is reached)
     * <li> {@link #backUp(ScoreTuple)} the resulting score {@code score} and
     *      the number of visits for all nodes on {@code value} and {@code visits}.
     * <li> Do this for all nodes on the path from the leaf up to the root.
     * </ul>
     * 
     * Once this method is completed, method {@link #bestAction()} will return the
     * action {@link Types.ACTIONS} of the root's childrenNodes which maximizes
     * <pre>
     *        U(i) = childrenNodes[i].value / childrenNodes[i].visits
     * </pre>
     * @param vTable on input an array of length K+1, where K is the number of available
     * 		  moves for the root state. Contains on output {@code U(i)} in the first 
     * 		  K entries and the maximum of all {@code U(i)} in {@code vTable[K]}
     */
    public void mctseSearch(double[] vTable) {
    	while (iterations < m_player.getNUM_ITERS()) {
            //select a child node
            MCTSEChanceNode selected = treePolicy();

            //double[] score;
            ScoreTuple scTuple;

            if(selected.so instanceof StateObserver2048 && m_player.getParMCTSE().getEnableHeuristics()) {
                // only for game 2048: use special heuristic and add non-normalized score:
                // a) get Heuristic bonus
                scTuple = new ScoreTuple(1);
                //score = new double[1];
                scTuple.scTup[0] = ((StateObserver2048)selected.so).getHeuristicBonus(m_player.getHeuristicSettings2048());
                if(m_player.getHeuristicSettings2048().enableRollout) {
                    // b) add rollout bonus (non-normalized score)
                    scTuple.scTup[0] += selected.rollOut1() * m_player.getHeuristicSettings2048().rolloutWeighting;
                }
            } else {
                //get "normal" MCTSE score
                scTuple = selected.rollOut();
            }

            //
            // TODO: this is to be tested: Do we get better performance if we calculate the delta reward, i.e. if we
            // subtract root node's score tuple?
            //
            boolean DELTA_REWARD=true;
            if (DELTA_REWARD)
                scTuple.combine(
                    m_player.getRootNode().so.getRewardTuple(m_player.rgs),
                    ScoreTuple.CombineOP.DIFF,
                    0,0.0
                );


            // --- this is now done in valueFnc(): directly ---
//            //set maxRolloutScore in root node (needed in valueFnc())
//            double s = m_player.getRootNode().scoreNonNormalized;
//            if(m_player.getRootNode().maxRolloutScore < s) {
//                m_player.getRootNode().maxRolloutScore = s;
//            }

            //backup the score
            selected.backUp(scTuple);

            iterations++;
        }

        // fill vTable
        for (int i = 0; i < so.getNumAvailableActions(); i++) {
            for (MCTSETreeNode child : childrenNodes) {
                if (child.action.equals(so.getAction(i))) {
                    vTable[i] = child.value / child.visits;
                }
            }
            if (m_player.getNormalize()) {
                double maxScore = (so instanceof StateObserver2048) ? m_player.getRootNode().maxRolloutScore : so.getMaxGameScore();
                vTable[i] = vTable[i]*maxScore;
            }
        }
        
		// /WK/ some diagnostic checks (not required for normal operation)
		if (m_player.getVerbosity() > 1) 
			this.printChildInfo(0, true);
		if (m_player.getVerbosity() > 0)
			System.out.println(
					"--  iter=" + iterations + " -- (tree/chance)-nodes=(" +
					m_player.getRootNode().numberTreeNodes+"/"+
					m_player.getRootNode().numberChanceNodes + ") = "+ 
					this.numDescendants(0));
    }

    /**
     * Selects the best Action of an expanded tree
     *
     * @return the action from the children of {@code this} which maximizes
     * <pre>
     * 		 U(i) = child[i].getQ()/child[i].visits
     * </pre>
     */
    public Types.ACTIONS bestAction() {
        Types.ACTIONS selected = null;
        double bestValue = -Double.MAX_VALUE;
        double currentValue;

        for (MCTSETreeNode child: childrenNodes) {
            //currentValue = child.value / child.visits + m_rnd.nextDouble() * epsilon; // before 09/2021
            currentValue = child.getQ() / child.visits + m_rnd.nextDouble() * epsilon;
            if (currentValue > bestValue) {
                bestValue = currentValue;
                selected = child.action;
            }
        }

        return selected;
    }

    /**
     * Select the next {@link MCTSEChanceNode} that should be evaluated.
     * <p><ul>
     *      <li> If the current node is not fully expanded, a random unexpanded {@link MCTSETreeNode} will be added and
     * one of its child {@link MCTSEChanceNode}s will be returned via treePolicy().
     *      <li> If the current node is fully expanded, one child {@link MCTSETreeNode} will be selected with
     * {@link #uct()}, {@link #egreedy()} or {@link #rouletteWheel()}, resp., and one of its child {@link MCTSEChanceNode}s
     * will be returned via treePolicy().
     * </ul>
     * @return the {@link MCTSEChanceNode} that should be evaluated
     */
    private MCTSEChanceNode treePolicy() {
        boolean stopOnRoundOver = m_player.getParMCTSE().getStopOnRoundOver();
        if(so.isGameOver() || (stopOnRoundOver && so.isRoundOver()) || depth >= m_player.getTREE_DEPTH()) {
            return this;

        } else if(notExpandedActions.size() != 0) {
            if(m_player.getRootNode().numberTreeNodes < m_player.getMaxNodes()) {
                return expand().treePolicy();
            } else {
                return this;
            }
        } else {
            //recursively go down the tree:
            //select a child (MCTSETreeNode) with UCT or eps-greedy, 
        	//select/create a chance node with MCTSETreeNode::treePolicy() 
        	//and call MCTSEChanceNode::treePolicy() again to ensure recursion

            return switch (m_player.getParMCTSE().getSelectMode()) {
                case 0 ->
//	                    return uctNormalised().treePolicy().treePolicy();
                        // uctNormalised() is obsolete now. We do the optional normalization via valueFnc(),
                        // which is called by rollOut(). (The initial estimate for root's maxRolloutScore
                        // is now calculated in MCTSEPlayer::init().) Given this optional normalization,
                        // we can always use normal uct() here:
                        uct().treePolicy().treePolicy();
                        // Why 2-times treePolicy()? - Because uct() returns a MCTSETreeNode
                        // and uct().treePolicy returns a MCTSEChanceNode. If we stopped after the first treePolicy(),
                        // we would have no recursion, we would just select a chance node 2 layers down. With
                        // the second .treePolicy() we ensure that a complete recursion is done (until a
                        // terminal state or the prescribed tree depth is reached).
                case 1 -> egreedy().treePolicy().treePolicy();
                case 2 -> rouletteWheel().treePolicy().treePolicy();
                default -> throw new RuntimeException("this selectMode is not yet implemented");
            };

        }
    }

    /**
     * Select the child node with the highest UCT value
     *
     * @return the selected child node
     * 
     * @see #rouletteWheel()
     * @see #egreedy()
     */
    private MCTSETreeNode uct() {
        MCTSETreeNode selected = null;
        double selectedValue = -Double.MAX_VALUE;

        for (MCTSETreeNode child : childrenNodes)
        {
            double uctValue = child.getQ() / child.visits
            		+ m_player.getK() * Math.sqrt(Math.log(visits + 1) / (child.visits + epsilon))
            		+ m_rnd.nextDouble() * epsilon; 
            		// small random numbers: break ties in unexpanded node

            if (uctValue > selectedValue) {
                selected = child;
                selectedValue = uctValue;
            }
        }

        return selected;
    }

    // --- never used ---
//    @Deprecated
//    private MCTSETreeNode uctNormalised() {
//    	// we do this now differently with an extra short mctseSearch in MCTSEPlayer::init()
//    	// and with the proper normalization done in this.valueFnc():
//        if(m_player.getRootNode().iterations < 100) {
//            return uct();			// run the first 100 iterations w/o normalization to establish
//            						// a rough estimate of maxRolloutScore
//        } else {
//            double multiplier = m_player.getRootNode().maxRolloutScore;
//            assert multiplier != 0 : "Error: maxRolloutScore is 0.0";
//            multiplier = 1/multiplier;
//
//            MCTSETreeNode selected = null;
//            double selectedValue = -Double.MAX_VALUE;
//
//            for (MCTSETreeNode child : childrenNodes) {
//                double uctValue = child.value * multiplier / child.visits
//                		+ m_player.getK() * Math.sqrt(Math.log(visits + 1) / (child.visits + MCTSEChanceNode.epsilon))
//                		+ m_rnd.nextDouble() * epsilon;
//                		// small random numbers: break ties in unexpanded node
//
//                if (uctValue > selectedValue) {
//                    selected = child;
//                    selectedValue = uctValue;
//                }
//            }
//
//            assert selected != null : "Error: No tree node selected!";
//
//            return selected;
//        }
//    }

    /**
     * Epsilon-Greedy, a variant to UCT
     *
     * @return the selected child node
     * 
     * @see #uct()
     * @see #rouletteWheel()
     */
    public MCTSETreeNode egreedy() {
        if (m_rnd.nextDouble() < ParMCTSE.DEFAULT_EPSILONGREEDY) {
            return childrenNodes.get(m_rnd.nextInt(childrenNodes.size()));
        } else {
            MCTSETreeNode selected = null;
            double selectedValue = -Double.MAX_VALUE;

            for (MCTSETreeNode childrenNode : childrenNodes)
            {
                if (childrenNode.value > selectedValue) {
                    selected = childrenNode;
                    selectedValue = childrenNode.value;
                }
            }

            return selected;
        }
    }

    /**
     * Roulette-wheel selection, a variant to UCT. See Sec. 2.5.1 in [Swiechowski15],
     * <a href="http://dx.doi.org/10.1155/2015/986262">http://dx.doi.org/10.1155/2015/986262</a>: <br>
     * Swiechowski, M., et al.: <i>Recent Advances in General Game Playing</i>, The Scientific World Journal, Volume 2015, Article ID 986262.
     * <p>
     * Each child gets a sector on the roulette wheel (0,1] with its sector size being  
     * proportional to the child's utility
     * <pre>
     * 		 U(child) = child.value/child.visits. </pre> 
     * Now a random number from (0,1] is chosen and the child whose sector 
     * contains this random number is selected.
     * <p>
     * @return the selected child node
     * 
     * @see #uct()
     * @see #egreedy()
     */
    public MCTSETreeNode rouletteWheel() {
    	// TODO: implement one-move wins and one-move losses acc. to [Swiechowski15]
        double rnd = m_rnd.nextDouble();
        double vTotal = 0.0;
        double vMin;
        double cumProb = 0.0;		// cumulative probability of all children up to current child

        // We assign each child a probability which is the softmax of its utility U(child):
        // 		p(i)  =  U(child) / vTotal  =  U(child) / sum_j U(child_j)
        // Example: 3 children with probabilities p(0)=0.2, p(1)=0.5, p(2)=0.3. This defines a 
        // segmentation of the roulette wheel in three sectors:
        //		child_0: (0,0.2], child_1: (0.2,0.7], child_3: (0.7,1.0].
        // The LHS of the intervals are the cumulative probabilities child.cumProb. We select 
        // that child which is the first with its LHS >= rnd.
		vMin = (m_player.getNormalize()) ? 0.0 : so.getMinGameScore();
        for (MCTSETreeNode child : childrenNodes) {
        	vTotal += child.value / child.visits - vMin;
        }
        for (MCTSETreeNode child : childrenNodes) {
        	cumProb = cumProb + ((child.value/child.visits)-vMin)/vTotal;
        	child.cumProb = cumProb;
        	// We do not really need child.cumProb, we could just work with the local variable
        	// cumProb. We have child.cumProb only for debugging purposes in order to have in 
        	// this.children all cumulative probabilities and see if they converge to 1.
        	if (cumProb>=rnd) {
        		return child;		// the normal return
        	}
        }

        throw new RuntimeException("rouletteWheel: We should not arrive here!");
    }

    /**
     * Expand the current node, by randomly selecting one of the yet unexplored child nodes
     * 
     * @return the selected child node
     */
    public MCTSETreeNode expand() {
        Types.ACTIONS action = notExpandedActions.get(ThreadLocalRandom.current().nextInt(notExpandedActions.size()));
        notExpandedActions.remove(action);

//      StateObserver2048 childSo = (StateObserver2048) so.copy();		// /WK/ can be done w/o using StateObserver2048:
        StateObservation childSo = so.copy();

        MCTSETreeNode child = new MCTSETreeNode(childSo, action, this, m_rnd,m_player);
        childrenNodes.add(child);
        return child;
    }

    /**
     * Starting from this leaf node a game with random actions will be played until the game 
     * is over or the maximum rollout depth is reached.
     *
     * @return 	a vector of length N, where the {@code i}th element holds q(reward[{@code i}]), the reward
     * 			for the {@code i}th player in state {@code so} after the rollout is finished.<br>
     * 			q() normalizes the rewards to [0,1] if flag 'Normalize' is checked. Otherwise it is the identity function.
     */
    public ScoreTuple rollOut() {
        boolean stopConditionMet;
        StateObservation rollerState = so.copy();
        int maxDepth = this.m_player.getROLLOUT_DEPTH();

        //for(int i = this.depth; i < maxDepth; i++) {  // this alternative was implemented before 03/2021, but we think
                                                        // it might lead to wrong results for small maxDepth
        for(int i = 0; i < maxDepth; i++) {
            stopConditionMet = isRolloutFinished(rollerState);
            if (!stopConditionMet) {
                if (rollerState.isRoundOver())
                    rollerState.initRound();         // NEW/03/2021: for round-based games
                //rollerState.setAvailableActions();	// /WK/ commented out since every advance() includes setAvailableActions()
                                                        // and the initial 'so' has also its available actions set.
                                                        // Calling setAvailableActions without need slows down,
                                                        // especially for Othello (factor 3-4).
                if (rollerState.getNumAvailableActions() > 0) {
                    int action = ThreadLocalRandom.current().nextInt(rollerState.getNumAvailableActions());
                    rollerState.advance(rollerState.getAction(action));
                }
                else {
                    // If the current player has no available action: we have a pass situation
                    // (like in Othello): We should pass over to the next player and just continue (!)
                    rollerState.passToNextPlayer();
                }
                // --- only for debug ---
                //System.err.println("[MCTSE] " + rollerState.stringDescr());

            } else {
                // /WK/ **BUG1** fix: check every sob (including the first!) whether the stop condition is
                // already met. If this is the case, return sob without advance
                break; // out of for
            }
        }

        if (rollerState.isGameOver()) {
            m_player.nRolloutFinished++;
        }

//        return rollerState.getReward(so,m_player.rgs);
        return valueFnc(rollerState,so);  			// NEW version: N-player tuple
    }

    /**
     * This rollout variant works only for 2048 and returns the non-normalized raw score.
     * It gets only called if EnableHeuristics is activated.
     *
     * Starting from this leaf node a game with random actions will be played until the game
     * is over or the maximum rollout depth is reached.
     *
     * @return the 2048-raw-score after the rollout is finished
     */
    public double rollOut1() {
        boolean stopConditionMet;
        assert so instanceof StateObserver2048: "so is not instanceof StateObserver2048, use rollOut() instead off rollOut1()";
        StateObserver2048 rollerState = (StateObserver2048)so.copy();
        int maxDepth = this.m_player.getROLLOUT_DEPTH();

        //for(int i = this.depth; i < maxDepth; i++) {  // this alternative was implemented before 03/2021, but we think
                                                        // it might lead to wrong results for small maxDepth
        for(int i = 0; i < maxDepth; i++) {
            stopConditionMet = isRolloutFinished(rollerState);
            if (!stopConditionMet) {
                if (rollerState.isRoundOver()) rollerState.initRound();         // NEW/03/2021: for round-based games
                //rollerState.setAvailableActions();	// /WK/ commented out since every advance() includes setAvailableActions()
                                                        // and the initial 'so' has also its available actions set.
                                                        // Calling setAvailableActions without need slows down,
                                                        // especially for Othello (factor 3-4).
                if (rollerState.getNumAvailableActions() > 0) {
                    int action = ThreadLocalRandom.current().nextInt(rollerState.getNumAvailableActions());
                    rollerState.advance(rollerState.getAction(action));
                }
                else {
                    // If the current player has no available action: we have a pass situation
                    // (like in Othello): We should pass over to the next player and just continue (!)
                    rollerState.passToNextPlayer();
                }

            } else {
                break; // out of for
            }
        }

        if (rollerState.isGameOver()) {
            m_player.nRolloutFinished++;
        }

        return rollerState.score;
    }

	/**
	 * Assign the final rollerState a value (reward).
	 * <p>
	 * If 'Normalize' is checked, the reward is passed through a normalizing function q()
	 * which maps to [0,1]. Otherwise q() is the identity function.
	 * 
	 * @param so
	 *            the final state
	 * @param referingState
	 *            the state where the rollout (playout) started [** now perhaps obsolete **]
	 * 
     * @return 	a vector of length N, where the {@code i}th element holds q(reward[{@code i}]), the reward
     * 			for the {@code i}th player in state {@code so}.<br>
     * 			q() normalizes the rewards to [0,1] if flag 'Normalize' is checked. Otherwise it is the identity function.
	 */
	public ScoreTuple valueFnc(StateObservation so, StateObservation referingState) {
		ScoreTuple tup = so.getRewardTuple(m_player.rgs);
        double[] v = tup.scTup.clone();
		if (m_player.getNormalize()) {
            double maxScore;
            double minScore=so.getMinGameScore();
			if (so instanceof StateObserver2048) {
				// A special normalization for 2048: maxRolloutScore is an estimate of the maximum
				// expected rollout score from the current root node (estimated initially in 
				// MCTSEPlayer::init() by a quick 100-iterations mctseScearch() and then updated 
				// whenever a larger rollout score arrives).
				// [Why this rollout score and not so.getMaxGameScore()? - Because the maximum 
				// game score in 2048 is MAXSCORE=3932156 is only a theoretical limit 
				// when tile 2^16 is reached, which does not happen in a 'normal' rollout.
				// Using this MAXSCORE would result in too small child values (no exploitation).]
	            maxScore = m_player.getRootNode().maxRolloutScore;
	            if (v[0]>maxScore)
	            	maxScore = m_player.getRootNode().maxRolloutScore = v[0];
	            //assert maxScore != 0 : "Error: maxRolloutScore is 0.0";
			} else {
				maxScore = so.getMaxGameScore();
			}
			// /WK/ map v to [0,1] (this is q(reward) in notes_MCTS.docx)
            for(int i = 0; i < v.length; i++) {
                v[i] = (v[i] - minScore) / (maxScore - minScore);
                //assert ((v[i] >= 0) && (v[i] <= 1)) : "Error: value v is not in range [0,1]";
                //--- there might be games where maxScore is only approximate and thus the assertion may be
                //--- occasionally violated
            }
		}
		return new ScoreTuple(v);
	}

    /**
     * checks if a rollout is finished
     *
     * @param rollerState the current game state
     * @return true if the rollout is finished, false if not
     */
    public boolean isRolloutFinished(StateObservation rollerState) {
        boolean stopOnRoundOver = m_player.getParMCTSE().getStopOnRoundOver();

        if (rollerState.isGameOver()) {
            return true;
        }

        return (rollerState.isRoundOver() && stopOnRoundOver);        // /WK/ NEW/03/2021
    }

    /**
     * Backup the score through all parent nodes, until the root node is reached
     * calls itself recursively in each parent node
     *
     * @param delta the reward vector returned from {@link #rollOut()}
     */
    public void backUp(ScoreTuple delta) {
        backUpSum(delta);
    }

    /**
     * The value of a node is the sum of all it child nodes
     *
     * @param delta the reward vector returned from {@link #rollOut()}
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
            int pPlayer = parentNode.getParentNode().so.getPlayer();	// pPlayer: the player of the preceding CHANCE node
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

//    /**
//     * The value of a node is the value of the lowest child node<br>
//     * (This method is currently not used)
//     *
//     * @param score the value of the new child node
//     */
//    @Deprecated
//    private void backUpMin(double score) {
//    	// note that the score-negation necessary for 2-player games is done in
//    	// MCTSETreeNode::backUp...()
//        visits++;
//        if(score < value || value == 0) {
//            value = score;
//        }
//
//        if (parentNode != null) {
//            parentNode.backUp(score);
//        }
//    }

	/**
	 * just for diagnostics:
	 * 
	 * @return number of nodes in the MCTSE tree
	 */
	public int numDescendants(int depth) {
		int N = 1; // include this
		for (MCTSETreeNode c : this.childrenNodes) {			
			if (c != null)
				N += c.numDescendants(depth+1);
//			if (depth==0) {
//				System.out.println(N);
//				int dummy=1;
//			}
		}			
		return N;
	}

	/**
	 * More diagnostic info, printed in case {@link #m_player}{@code .getVerbosity} &gt; 2.
	 * 
	 * @param nIndention	indent text by 2*nIndention white spaces
	 * @param doAssert		whether to do assertions
	 */
	public void printChildInfo(int nIndention, boolean doAssert) {
		DecimalFormat form = new DecimalFormat("0.0000");
		DecimalFormat for2 = new DecimalFormat("+0.0000;-0.0000");
		DecimalFormat ifor = new DecimalFormat("0000");
        double multiplier = 1/(m_player.getRootNode().maxRolloutScore+ MCTSEChanceNode.epsilon);
		int cVisits = 0;
		int verbose = m_player.getVerbosity();
		String indention = "";
		for (int n = 0; n < nIndention; n++)
			indention += "  ";

		for (MCTSETreeNode c : this.childrenNodes) {
			if (c != null) {
				cVisits += c.visits;
				if (verbose > 1) { 	// =2: print direct child info
					double uct_exploit = c.value * multiplier / (c.visits + MCTSEChanceNode.epsilon);
					double uct_explore = m_player.getK()
							* Math.sqrt(Math.log(this.visits + 1) / (c.visits + MCTSEChanceNode.epsilon));
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
