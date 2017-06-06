package controllers.MCTSExpectimax;

import games.StateObservation;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import javafx.util.Pair;
import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by Johannes on 03.06.2017.
 */
public class MCTSExpectimaxTreeNode {
    public static double epsilon = 1e-6;		            // tiebreaker
    private StateObserver2048 so = null;
    public Types.ACTIONS action = null;		                // the action which leads from parent's state to this state
    private MCTSExpectimaxChanceNode parentNode = null;
    private TreeMap<Double, MCTSExpectimaxChanceNode> childrenNodes = new TreeMap<>();
    private MCTSExpectimaxPlayer player = null;
    public double value = 0;                                   // total value
    public int visits = 0;                                     // total number of visits
    private Random random;
    public int depth;

    public MCTSExpectimaxTreeNode(StateObserver2048 so, Types.ACTIONS action, MCTSExpectimaxChanceNode parentNode, Random random, MCTSExpectimaxPlayer player) {
        this.so = so;
        this.action = action;
        this.parentNode = parentNode;
        this.player = player;
        this.random = random;
        this.depth = parentNode.depth;
    }

    public MCTSExpectimaxChanceNode treePolicy() {
        return expand();
    }

    private MCTSExpectimaxChanceNode expand() {
        StateObserver2048 childSo = so.copy();
        childSo.advance(action);

        MCTSExpectimaxChanceNode child;
        if(!childrenNodes.containsKey(childSo.getBoard())) {
            child = new MCTSExpectimaxChanceNode(childSo, null, this, random, player);
            childrenNodes.put(childSo.getBoard(), child);
        } else {
            child = childrenNodes.get(childSo.getBoard());
        }

        return child;
    }



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

        return rollerState.getGameScore();
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

    public void backUp(double score) {
        if (so.getNumPlayers()==2) {
            score =- score;
        }

        visits++;
        value += score;

        if (parentNode != null) {
            parentNode.backUp(score);
        }
    }
}
