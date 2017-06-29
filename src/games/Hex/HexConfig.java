package games.Hex;

class HexConfig {
    final static double REWARD_NEGATIVE = -1.0;
    final static double REWARD_POSITIVE =  1.0;

    final static int PLAYER_NONE = -1;
    final static int PLAYER_ONE = 0;
    final static int PLAYER_TWO = 1;

    final static int BOARD_SIZE = 5;
    final static int HEX_SIZE = 60; //size of hexagons in px (from one side to the opposite one)
    final static int OFFSET = HEX_SIZE/4; //offset in px from top and left borders of the window
    final static int TILE_COUNT = BOARD_SIZE*BOARD_SIZE;
}
