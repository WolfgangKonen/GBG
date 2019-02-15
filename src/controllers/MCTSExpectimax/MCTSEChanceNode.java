package controllers.MCTSExpectimax;

import games.StateObservation;
import games.ZweiTausendAchtundVierzig.ConfigGame;
import games.ZweiTausendAchtundVierzig.Heuristic.HeuristicSettings2048;
import params.ParMCTSE;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import tools.Types;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import controllers.MCTS.SingleTreeNode;

/**
 * This is adapted from Diego Perez MCTS reference implementation
 * 		http://gvgai.net/cont.php
 * (with a bug fix concerning the number of available actions and an 
 *  extension for 1- and 2-player games, adjusted for nondeterministic
 *  games and the return of VTable information.)
 *
 *  @author Johannes Kutsch
 */

public class MCTSEChanceNode
{
    public static double epsilon = 1e-6;		                                // tiebreaker
    public StateObservation so = null;
    public Types.ACTIONS action = null;		                                // the action which leads from parent's state to this state
    private MCTSETreeNode parentNode = null;
    public List<MCTSETreeNode> childrenNodes = new ArrayList<>();
    public MCTSEPlayer m_player = null;

    public List<Types.ACTIONS> notExpandedActions = new ArrayList<>();         // Actions that are not represented by a node
    public double value = 0;   				// total value
    public int visits = 0; 					// total number of visits
    public static Random m_rnd;
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
        this.so = so;
        this.action = action;
        this.parentNode = parentNode;
        this.m_player = player;
        this.m_rnd = random;

        notExpandedActions = so.getAvailableActions();

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
     *
     * Called by {@link MCTSEPlayer#run(double[])}.
     *
     * Do for {@code player.NUM_ITERS} iterations
     * --- select a {@link MCTSEChanceNode} leaf node via {@link #treePolicy()} (this
     *     includes {@link #expand()} of not fully expanded nodes, as long as the maximum tree
     *     depth is not yet reached)
     * --- make a {@link #rollOut()} starting from this leaf node (a game
     *     with random actions until game is over or until the maximum rollout depth is reached)
     * --- {@link #backUp(double)} the resulting score {@code score} and
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
        while (iterations < m_player.getNUM_ITERS()) {
            //select a child node
            MCTSEChanceNode selected = treePolicy();

            double score;

            if(selected.so instanceof StateObserver2048 && m_player.getParMCTSE().getEnableHeuristics()) {
                // only for game 2048: use special heuristic and add non-normalized score:
                // a) get Heuristic bonus
                score = ((StateObserver2048)selected.so).getHeuristicBonus(m_player.getHeuristicSettings2048());
                if(m_player.getHeuristicSettings2048().enableRollout) {
                    // b) add rollout bonus (non-normalized score)
                    score += selected.rollOut1() * m_player.getHeuristicSettings2048().rolloutWeighting;
                }
            } else {
                //get "normal" MCTSE score
                score = selected.rollOut();
            }

            //set max score in root node
            if(m_player.getRootNode().maxRolloutScore < score) {
                m_player.getRootNode().maxRolloutScore = score;
            }

            //backup the score
            selected.backUp(score);

            iterations++;
        }

        // fill vTable
        for (int i = 0; i < so.getNumAvailableActions(); i++) {
            for (MCTSETreeNode child : childrenNodes) {
                if (child.action.equals(so.getAction(i))) {
                    vTable[i] = child.value / child.visits;
                }
            }
        }
        
		// /WK/ some diagnostic checks (not required for normal operation)
		this.printChildInfo(0, true);

		if (m_player.getVerbosity() > 0)
			System.out.println(
					"--  iter=" + iterations + " -- (tree/chance)-nodes=(" +
					m_player.getRootNode().numberTreeNodes+"/"+
					m_player.getRootNode().numberChanceNodes + ") = "+ 
					this.numDescendants(0));
    }

    /**
     * Select the next {@link MCTSETreeNode} that should be evaluated
     *
     * If the current node is not fully expanded, a random unexpanded {@link MCTSETreeNode} will be chosen
     * If the current node is fully expanded the {@link MCTSETreeNode} will be select with uct()
     *
     * @return the {@link MCTSETreeNode} that should be evaluated
     */
    private MCTSEChanceNode treePolicy() {
        if(so.isGameOver() || depth >= m_player.getTREE_DEPTH()) {
            return this;

        } else if(notExpandedActions.size() != 0) {
            if(m_player.getRootNode().numberTreeNodes < m_player.getMaxNodes()) {
                return expand().treePolicy();
            } else {
                return this;
            }
        } else {
            //recursively go down the tree
            //select a child with uct(), select/create a chance node with treePolicy() and call this method in the new chance node

            return uctNormalised().treePolicy().treePolicy();
        }
    }

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

		for (MCTSETreeNode c : this.childrenNodes) {
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

    /**
     * Select the child node with the highest UCT value
     *
     * @return the selected child node
     */
    private MCTSETreeNode uct() {
        MCTSETreeNode selected = null;
        double selectedValue = -Double.MAX_VALUE;

        for (MCTSETreeNode child : childrenNodes)
        {
            double uctValue = child.value / child.visits + m_player.getK() * Math.sqrt(Math.log(visits + 1) / (child.visits + this.epsilon)) 
            		+ m_rnd.nextDouble() * epsilon; 
            		// small random numbers: break ties in unexpanded node

            if (uctValue > selectedValue) {
                selected = child;
                selectedValue = uctValue;
            }
        }

        return selected;
    }

    private MCTSETreeNode uctNormalised() {
        if(m_player.getRootNode().iterations < 100) { 
            return uct();			// run the first 100 iterations w/o normalization to establish
            						// a rough estimate of maxRolloutScore
        } else {
            double multiplier;

            if(m_player.getRootNode().maxRolloutScore == 0.0d) {
                multiplier = 1;
            } else {
                multiplier = 1/m_player.getRootNode().maxRolloutScore;
            }

            MCTSETreeNode selected = null;
            double selectedValue = -Double.MAX_VALUE;

            for (MCTSETreeNode child : childrenNodes) {
                double uctValue = child.value * multiplier / child.visits + m_player.getK() * Math.sqrt(Math.log(visits + 1) / (child.visits + this.epsilon)) 
                		+ m_rnd.nextDouble() * epsilon; 
                		// small random numbers: break ties in unexpanded node

                if (uctValue > selectedValue) {
                    selected = child;
                    selectedValue = uctValue;
                }
            }

            assert selected != null : "Error: No tree node selected!";
            
            // just debug info for 2048:
            double selVal = selected.value/selected.visits*StateObserver2048.MAXSCORE;
            double selVal2 = selected.value/selected.visits*multiplier;
            double maxVal = m_player.getRootNode().maxRolloutScore*StateObserver2048.MAXSCORE;
            int selAct = selected.action.toInt();
            int dummy=1;
//            if(selected == null) {
//                System.out.println("2: " + childrenNodes.size());
//            }

            return selected;
        }
    }

    /**
     * Epsilon-Greedy, a variant to UCT
     *
     * @return the best child node
     */
    public MCTSETreeNode egreedy() {
        if (m_rnd.nextDouble() < ParMCTSE.EGREEDYEPSILON) {
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
     * Expand the current node, by randomly selecting one unexplored child node
     * 
     * @return the selected child node
     */
    public MCTSETreeNode expand() {
        Types.ACTIONS action = notExpandedActions.get(m_rnd.nextInt(notExpandedActions.size()));
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
     * @return 	the {@link StateObservation#getReward(StateObservation, boolean) reward} 
     * 			after the rollout is finished
     * 
     * @see StateObservation#getReward(StateObservation, boolean)
     */
    public double rollOut() {
        StateObservation rollerState = so.copy();
        int thisDepth = this.depth;

        while (!finishRollout(rollerState, thisDepth)) {
            rollerState.setAvailableActions();
            int action = m_rnd.nextInt(rollerState.getNumAvailableActions());
            rollerState.advance(rollerState.getAction(action));
            thisDepth++;
        }

        if (rollerState.isGameOver()) {
            m_player.nRolloutFinished++;
        }

        return rollerState.getReward(so,m_player.rgs);
    }

    /**
     * Starting from this leaf node a game with random actions will be played until the game 
     * is over or the maximum rollout depth is reached.
     * This rollout variant works only for 2048 and returns the non-normalized raw score.
     *
     * @return the 2048-raw-score after the rollout is finished
     */
    public double rollOut1() {
        assert so instanceof StateObserver2048: "so is not instanceof StateObserver2048, use rollOut() instead off rollOut1()";
        StateObserver2048 rollerState = (StateObserver2048)so.copy();
        int thisDepth = this.depth;

        while (!finishRollout(rollerState, thisDepth)) {
            rollerState.setAvailableActions();
            int action = m_rnd.nextInt(rollerState.getNumAvailableActions());
            rollerState.advance(rollerState.getAction(action));
            thisDepth++;
        }

        if (rollerState.isGameOver()) {
            m_player.nRolloutFinished++;
        }

        return rollerState.score;
    }

    /**
     * checks if a rollout is finished
     *
     * @param rollerState the current gamestate
     * @param depth the current rolloutdepth
     * @return true if the rollout is finished, false if not
     */
    public boolean finishRollout(StateObservation rollerState, int depth) {
        if (depth >= m_player.getROLLOUT_DEPTH()) {
            return true;
        }

        if (rollerState.isGameOver()) {
            return true;
        }

        return false;
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
     * Selects the best Action of an expanded tree
     *
     * @return the {@link Types.ACTIONS} of the root's childrenNodes which maximizes U(i) = childrenNodes[i].value/childrenNodes[i].visits
     */
    public Types.ACTIONS bestAction() {
        Types.ACTIONS selected = null;
        double bestValue = -Double.MAX_VALUE;
        double currentValue;

        for (MCTSETreeNode child: childrenNodes) {
            currentValue = child.value / child.visits + m_rnd.nextDouble() * epsilon;
            if (currentValue > bestValue) {
                bestValue = currentValue;
                selected = child.action;
            }
        }

        return selected;
    }
    
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

}
