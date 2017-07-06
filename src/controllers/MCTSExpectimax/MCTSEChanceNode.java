package controllers.MCTSExpectimax;

import games.StateObservation;
import games.ZweiTausendAchtundVierzig.ConfigGame;
import games.ZweiTausendAchtundVierzig.Heuristic.HeuristicSettings2048;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
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
 *  @author Johannes Kutsch
 */

public class MCTSEChanceNode
{
    public static double epsilon = 1e-6;		                                // tiebreaker
    public StateObservation so = null;
    private Types.ACTIONS action = null;		                                // the action which leads from parent's state to this state
    private MCTSETreeNode parentNode = null;
    public List<MCTSETreeNode> childrenNodes = new ArrayList<>();
    public MCTSEPlayer player = null;

    public List<Types.ACTIONS> notExpandedActions = new ArrayList<>();         // Actions that are not represented by a Note
    public double value = 0;                                                    // total value
    public int visits = 0;                                                      // total number of visits
    public Random random;
    public int depth;

    public int iterations = 0;
    public int numberTreeNodes = 0;
    public int numberChanceNodes = 1;

    public double maxRolloutScore = 0;                                                 //The max score that was reached during a rollout

    /**
     * This Class represents a MCTS Expectmiax Chance Node.
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
        this.player = player;
        this.random = random;

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
        while (iterations < player.getNUM_ITERS()) {
            //select an childNode
            MCTSEChanceNode selected = treePolicy();

            double score;

            if(selected.so instanceof StateObserver2048 && player.getMCTSParams().getEnableHeuristics()) {
                //get Heuristic bonus
                score = ((StateObserver2048)selected.so).getHeuristicBonus(player.getHeuristicSettings2048());
                if(player.getHeuristicSettings2048().enableRollout) {
                    //add rollout bonus
                    score += selected.rollOut1() * player.getHeuristicSettings2048().rolloutWeighting;
                }
            } else {
                //get "normal" mctse score
                score = selected.rollOut();
            }

            //set max Score in root Node
            if(player.getRootNode().maxRolloutScore < score) {
                player.getRootNode().maxRolloutScore = score;
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
    }

    /**
     * Select the next {@link MCTSETreeNode} that should be evaluated
     *
     * If the current Node is not fully expanded a random unexpanded {@link MCTSETreeNode} will be chosen
     * If the current Node is fully expanded the {@link MCTSETreeNode} will be select with uct()
     *
     * @return the {@link MCTSETreeNode} that should be evaluated
     */
    private MCTSEChanceNode treePolicy() {
        if(so.isGameOver() || depth >= player.getTREE_DEPTH()) {
            return this;

        } else if(notExpandedActions.size() != 0) {
            if(player.getRootNode().numberTreeNodes < player.getMaxNodes()) {
                return expand().treePolicy();
            } else {
                return this;
            }
        } else {
            //recursively go down the three
            //select a child with uct(), select/create a chance node with treePolicy() and call this method in the new chance node

            return uctNormalised().treePolicy().treePolicy();
        }
    }

    /**
     * Select the child node with the highest uct value
     *
     * @return the selected child node
     */
    private MCTSETreeNode uct() {
        MCTSETreeNode selected = null;
        double selectedValue = -Double.MAX_VALUE;

        for (MCTSETreeNode child : childrenNodes)
        {
            double uctValue = child.value / child.visits + player.getK() * Math.sqrt(Math.log(visits + 1) / (child.visits)) + random.nextDouble() * epsilon; // small sampleRandom numbers: break ties in unexpanded node

            if (uctValue > selectedValue) {
                selected = child;
                selectedValue = uctValue;
            }
        }

        return selected;
    }

    private MCTSETreeNode uctNormalised() {
        if(player.getRootNode().iterations < 100) {
            return uct();
        } else {
            double multiplier;

            if(player.getRootNode().maxRolloutScore == 0.0d) {
                multiplier = 1;
            } else {
                multiplier = 1/player.getRootNode().maxRolloutScore;
            }

            MCTSETreeNode selected = null;
            double selectedValue = -Double.MAX_VALUE;

            for (MCTSETreeNode child : childrenNodes) {
                double uctValue = child.value * multiplier / child.visits + player.getK() * Math.sqrt(Math.log(visits + 1) / (child.visits)) + random.nextDouble() * epsilon; // small sampleRandom numbers: break ties in unexpanded node

                if (uctValue > selectedValue) {
                    selected = child;
                    selectedValue = uctValue;
                }
            }

            if(selected == null) {
                System.out.println("2: " + childrenNodes.size());
            }

            return selected;
        }
    }

    /**
     * Epsilon-Greedy, a variant to UCT
     *
     * @return the best child node
     */
    public MCTSETreeNode egreedy() {
        if (random.nextDouble() < MCTSExpectimaxConfig.EGREEDYEPSILON) {
            return childrenNodes.get(random.nextInt(childrenNodes.size()));
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
        Types.ACTIONS action = notExpandedActions.get(random.nextInt(notExpandedActions.size()));
        notExpandedActions.remove(action);

//      StateObserver2048 childSo = (StateObserver2048) so.copy();		// /WK/ can be done w/o using StateObserver2048:
        StateObservation childSo = so.copy();

        MCTSETreeNode child = new MCTSETreeNode(childSo, action, this, random,player);
        childrenNodes.add(child);
        return child;
    }

    /**
     * starting from this leaf node a game with random actions will be played until the game is over or the maximum rollout depth is reached
     *
     * @return the {@link StateObservation#getGameScore()} after the rollout is finished
     */
    public double rollOut() {
        StateObservation rollerState = so.copy();
        int thisDepth = this.depth;

        while (!finishRollout(rollerState, thisDepth)) {
            rollerState.setAvailableActions();
            int action = random.nextInt(rollerState.getNumAvailableActions());
            rollerState.advance(rollerState.getAction(action));
            thisDepth++;
        }

        if (rollerState.isGameOver()) {
            player.nRolloutFinished++;
        }

        return rollerState.getGameScore(so);
    }

    /**
     * starting from this leaf node a game with random actions will be played until the game is over or the maximum rollout depth is reached
     * this rollout variant only works for 2048 and returns the not normalised gamescore
     *
     * @return the {@link StateObservation#getGameScore()} after the rollout is finished
     */
    public double rollOut1() {
        assert so instanceof StateObserver2048: "so is not instanceof StateObserver2048, use rollOut() instead off rollOut1()";
        StateObserver2048 rollerState = (StateObserver2048)so.copy();
        int thisDepth = this.depth;

        while (!finishRollout(rollerState, thisDepth)) {
            rollerState.setAvailableActions();
            int action = random.nextInt(rollerState.getNumAvailableActions());
            rollerState.advance(rollerState.getAction(action));
            thisDepth++;
        }

        if (rollerState.isGameOver()) {
            player.nRolloutFinished++;
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
        if (depth >= player.getROLLOUT_DEPTH()) {
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
            currentValue = child.value / child.visits + random.nextDouble() * epsilon;
            if (currentValue > bestValue) {
                bestValue = currentValue;
                selected = child.action;
            }
        }

        return selected;
    }
}
