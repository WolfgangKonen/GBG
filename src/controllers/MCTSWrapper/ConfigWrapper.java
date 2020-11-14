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

}
