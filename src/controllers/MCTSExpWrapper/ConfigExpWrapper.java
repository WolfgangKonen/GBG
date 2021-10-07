package controllers.MCTSExpWrapper;

import controllers.MCTSExpWrapper.stateApproximation2.PlayAgentApproximator2;

public class ConfigExpWrapper {

    /**
     * A switch for {@link PlayAgentApproximator2}:
     * <ul>
     *  <li> If false (recommended setting for RubiksCube), do not use softmax squashing for move probabilities.
     *  <li> If true (recommended setting for Othello, ConnectFour), use softmax squashing.
     * </ul>
     */
    public static boolean USESOFTMAX=false;

    // --- currently not used in MCTSExpWrapper ---
//    /**
//     * A switch for {@link MctseWrapperAgent}:
//     * <ul>
//     *  <li>  If false (recommended setting for ConnectFour), force tree re-build in every call.
//     *  <li>  If true (recommended setting for Othello), re-use the tree (i.e. as in JS's master code).
//     * </ul>
//     */
//    public static boolean USELASTMCTS = true;

    /**
     * EPS is a parameter for {@link MctseChanceNode#selectChild(double)}, it should be &lt;&lt; 1. It controls the behavior
     * if N(s)==0, where N(s) is the number of visits to {@code this} node:
     * <ul>
     *  <li> Case A)  EPS = +1e-8 (a small positive value): The Nair-case, like in <a href="https://web.stanford.edu/~surag/posts/alphazero.html">
     *           https://web.stanford.edu/~surag/posts/alphazero.html</a>:
     *           If N(s)==0, then select the action with highest getP(a).
     *  <li> Case B)  EPS = 0: The JS-case: If N(s)==0, select the 1st action from the available actions.
     *  <li> Case C)  EPS = -1e-8 (a small negative value): The random case: select a random action from the available actions.
     * </ul>
     * While Case A) is a more exploiting start, Cases B) and C) give the node a more exploring start.
     * Case A) should be from theory the best choice in general.
     * If N(s) &gt; 0, then EPS is negligible (if condition  <code>|EPS| << 1</code> is fulfilled): Thus, all cases are the
     * same, they use the PUCT formula for selection.
     * <p>
     * Recommendation:
     * <ul>
     *  <li> Use EPS = 1e-8 (Case A) throughout.
     * </ul>
     * [EPS = 0.0  (Case B) had in one configuration better results for Othello, but when averaging over several random
     * configurations it was superseeded by EPS = 1e-8 (Case A).]
     */
    public static double EPS = 1e-8;  //1e-8; 0.0; -1e-8
}
