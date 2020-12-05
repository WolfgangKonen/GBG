package controllers.MCTSWrapper;

import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;

public class ConfigWrapper {

    /**
     * A switch for {@link PlayAgentApproximator}:
     *  If false (recommended setting for RubiksCube), do not use softmax squashing for move probabilities.
     *  If true (recommended setting for Othello), use softmax squashing.
     */
    public static boolean USESOFTMAX=true;


    /**
     * A switch for {@link MCTSWrapperAgent}:
     *  If false (recommended setting for ConnectFour, RubiksCube), force tree re-build in every call.
     *  If true (recommended setting for Othello), re-use the tree (i.e. as in JS's master code).
     */
    public static boolean USELASTMCTS = true;

    /**
     * A parameter for {@link MCTSNode#selectChild(double)}:
     * Case A)  0<EPS<<1: The Nair-case, like in https://web.stanford.edu/~surag/posts/alphazero.html: If N(s)==0, then
     *          select the action with highest getP(a). [N(s): number of visits to {@code this} node.]
     * Case B)  EPS==0: The JS-case: If N(s)==0, select the 1st action from the available actions.
     * Case C)  EPS<0: The random case: If N(s)==0, select a random action from the available actions.
     * While case A) is a more exploiting start, cases B) and C) give the node a more exploring start.
     * For N(s)>0, all cases are the same: they use the PUCT formula for selection.
     * Case A) should be from theory the best choice in general.
     *
     * Recommendations:
     *      Use EPS = 1e-8 (Case A) for RubiksCube, much better results
     *      Use EPS = 0.0  (Case B) for Othello, somewhat better results.
     */
    public static double EPS = 1e-8;  //1e-8; 0.0; -1.0
}
