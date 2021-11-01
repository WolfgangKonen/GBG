package games.Yavalath;

public class ConfigYavalath {

    public static int BOARD_LENGTH = 5; //Edge length of the board
    public static int BOARD_SIZE = (BOARD_LENGTH*2)-1; //Max length one row of the board can have
    static int x = (BOARD_SIZE-BOARD_LENGTH);
    static int INVALID_CELLS = x*x+x; //Number of invalid cells that are only there to simplify the board
    static int CELLS = BOARD_SIZE*BOARD_SIZE-INVALID_CELLS;
    static int PLAYERS = 2;

    //Line length to win the game
    final static int LENGTH_TO_WIN = 4;

    public final static int INVALID_FIELD = -2;
    public final static int EMPTY = -1;
    public final static int PLAYER_ONE = 0;
    public final static int PLAYER_TWO = 1;
    public final static int PLAYER_THREE = 2;

    final static double NEGATIVE_REWARD = -1.0;
    final static double POSITIVE_REWARD = 1.0;

    //Tile size in px
    final static int TILE_SIZE = 80;

    //Offset from the border of the window
    final static int OFFSET = TILE_SIZE/2;

    public final static int GAME_PIECE_RADIUS = TILE_SIZE/4;

    public static void setBoardSize(int length){
        BOARD_LENGTH = length;
        BOARD_SIZE = (BOARD_LENGTH*2)-1;
        x = (BOARD_SIZE-BOARD_LENGTH);
        INVALID_CELLS = x*x+x; //
        CELLS = BOARD_SIZE*BOARD_SIZE-INVALID_CELLS;

    }
}
