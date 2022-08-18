package controllers.MCTSWrapper;

import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import games.StateObservation;

public class ConfigWrapper {

    /**
     * A switch for {@link PlayAgentApproximator}:
     * <ul>
     *  <li> If false (recommended setting for RubiksCube 2x2x2), do not use softmax squashing for move probabilities.
     *  <li> If true (recommended setting for Othello, ConnectFour, RubiksCube 3x3x3), use softmax squashing.
     * </ul>
     */
    public static boolean USESOFTMAX=true;


    /**
     * A switch for {@link MCTSWrapperAgent}:
     * <ul>
     *  <li>  If false (recommended setting for ConnectFour), force tree re-build in every call.
     *  <li>  If true (recommended setting for Othello, RubiksCube), re-use the tree (i.e. as in JS's master code) in
     *        subsequent calls during one episode.
     * </ul>
     */
    public static boolean USELASTMCTS = true;

    /**
     * A switch for {@link MCTSWrapperAgent}: If this wrapper agent is trained, use one of the following exploration modes
     * during training. Exploration will be used if {@link MCTSWrapperAgent#getNextAction2(StateObservation, boolean, boolean) getNextAction2}
     * is called with parameter {@code random = true}.
     * <ul>
     *  <li>  <strong>mode 0</strong>: no explicit exploration (there is only implicit exploration through the iteration-wise
     *        changes in the tree and through the weight changes of the inner agent that modify a node's probability distribution)
     *  <li>  <strong>mode 1</strong>: select actions proportional to their visit counts (also actions with lower visit
     *        count will be selected with lower probability)
     *  <li>  <strong>mode 2</strong>: epsilon-greedy selection (where parameter m_epsilon is currently hard-coded in
     *        {@link MCTSWrapperAgent}
     * </ul>
     */
    public static int EXPLORATION_MODE = 0;

    /**
     * a quick hack to set epsilon for ConfigWrapper.EXPLORATION_MODE=2 (eps-greedy)
     */
    public static double epsilon = 0.15;

    /**
     * EPS is a parameter for {@link MCTSNode#selectChild(double)}, it should be <code>&lt;&lt;</code> 1. It controls the behavior
     * if N(s)==0, where N(s) is the number of visits to {@code this} node:
     * <ul>
     *  <li> Case A)  EPS = +1e-8 (a small positive value): The Nair-case, like in <a href="https://web.stanford.edu/~surag/posts/alphazero.html">
     *           https://web.stanford.edu/~surag/posts/alphazero.html</a>:
     *           If N(s)==0, then select the action with highest getP(a).
     *  <li> Case B)  EPS = 0: The JS-case: If N(s)==0, select the 1st action from the available actions.
     *  <li> Case C)  EPS = -1e-8 (a small negative value): The random case: select a random action from the available actions.
     * </ul>
     * While Case A is a more exploiting start, Cases B and C give the node a more exploring start.
     * Case A should be from theory the best choice in general.
     * If N(s) &gt; 0, then EPS is negligible (if condition  <code>|EPS| &lt;&lt; 1</code> is fulfilled): Thus, all cases are the
     * same, they use the PUCT formula for selection.
     * <p>
     * <p>
     * <strong>Recommendation: Use Case A (EPS = +1e-8) throughout.</strong>
     * <p>
     * [Case B  (EPS = 0.0) had in one configuration better results for Othello, but when averaging over several random
     * configurations it was superseded by Case A (EPS = +1e-8).]
     */
    public static double EPS = 1e-8;  //1e-8; 0.0; -1e-8
}
