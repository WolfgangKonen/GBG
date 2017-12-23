package games.Hex;

class HexConfig {
    final static double REWARD_NEGATIVE = -1.0;
    final static double REWARD_POSITIVE = 1.0;

    final static int PLAYER_NONE = -1;
    final static int PLAYER_ONE = 0;
    final static int PLAYER_TWO = 1;

    /**
     * Length of one side of the game board in tiles
     */
    final static int BOARD_SIZE = 5;

    /**
     * Size of hexagons in px (from one side to the opposite one)
     */
    final static int HEX_SIZE = 60;

    /**
     * Width of the outer border of the game board in px
     */
    final static int OUTLINE_WIDTH = HEX_SIZE / 6;

    /**
     * Offset in px from top and left borders of the window
     */
    final static int OFFSET = HEX_SIZE / 4;

    /**
     * Number of tiles on the game board
     */
    final static int TILE_COUNT = BOARD_SIZE * BOARD_SIZE;
 
    final static int EVAL_NUMEPISODES = 3;

    /**
     * the int's in EVAL_START_ACTIONS[N][] code the start boards for evaluation mode 10
     * and for the case of an NxN Hex board:
     * <ul>
     * <li> -1: empty board (a winning board for 1st player Black), 
     * <li> 0/1/...: Black's 1st move was tile 00/01/... (1-ply move, it should be a losing 
     * move. Then such a board is a winning board for 2nd player White)
     * </ul>
     * See {@link StateObserverHex} for the numbering of the tiles. <br>
     * {@link EvaluatorHex}, mode=10, will use all start boards for evaluation and 
     * return the average success.
     * <p>
     * Due to the 180-degree rotation symmetry it is sufficient to number the losing moves
     * on the left half of the board, including the vertical mid-line.
     * <p>
     * How to find out which boards are winning boards? - It is proven that the empty board is 
     * a winning board for all board sizes. The situation is more tricky for the 1-ply moves.
     * For small board sizes N<5, the exact value of each move can be calculated with Minimax. 
     * For larger board sizes, it is possible to use Hexy (a strong Hex playing program): Make
     * a starting move and see whether Hexy can win as 2nd player. However, this has to be done
     * manually for each starting move and each new board size.
     * 
     * @see EvaluatorHex
     */
    final static int[][] EVAL_START_ACTIONS = {
    		{-1},		// dummy for N=0
    		{-1},		// dummy for N=1	
    		{-1,0},		// N=2
    		{-1,0,3},	// N=3
    		{-1,0,1,2},	// N=4 (the complete set is {-1,0,1,2,4,5,8})
    		{-1,0,2,5,10,15},	// N=5 (the complete set is {-1,0,2,5,10,15})
    					// N=6 (the complete set is {-1,0,2,3,6,12,14,18,24}):
    		{-1,0,2,12,14,18,24}	
    };

}
