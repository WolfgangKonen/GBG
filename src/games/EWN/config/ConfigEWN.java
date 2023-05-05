package games.EWN.config;

import games.BoardVector;
import games.EWN.XNTupleFuncsEWN;
import games.StateObservation;

public class ConfigEWN {

    /**
     * Number of players.
     * Allowed values are 2, 3, 4.
     */
    public static int NUM_PLAYERS = 2;

    /**
     * Board size.
     * Allowed values are 3, 4, 5, 6. <br>
     * For {@code BOARD_SIZE} = 3, 4, each player has 3 tokens, for {@code BOARD_SIZE} = 5, 6, each player has 6 tokens.
     */
    public static int BOARD_SIZE = 3;

    /**
     * {@code CELL_CODING} influences only how a {@link BoardVector} is coded in {@link XNTupleFuncsEWN}.
     * Allowed values are: <ul>
     * <li> 0: only the quality {@code player} of a token is coded. Positional values are 0,...,N, where N codes 'empty'
     * <li> 1: codes {@code player} and {@code value} in 3 levels. Positional values are 0,...,3*N, where 3*N codes 'empty'
     * <li> 2: codes {@code player} and {@code value} in 6 levels. Positional values are 0,...,6*N, where 6*N codes 'empty'
     * </ul>
     *
     * @see XNTupleFuncsEWN#getBoardVector(StateObservation)
     */
    public static int CELL_CODING = 0;

    public static String[] CELL_CODE_NAMING = {"[0]", "[0],[1],[2]","[0],[1],[2],[3],[4],[5]"};

    public static String[] CELL_CODE_DIR_NAMING = {"G-0", "G-0-1-2","G-0-1-2-3-4-5"};
    // avoid square brackets "[" and "]" in dir name because Excel cannot save on such dirs

    public static boolean RANDOM_POSITION = false;

}
