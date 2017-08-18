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
}
