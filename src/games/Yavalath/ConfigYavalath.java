package games.Yavalath;

public class ConfigYavalath {

    private static int BOARD_SIZE = 5; //Edge length of the board
    private static int MAX_ROW_LENGTH = (BOARD_SIZE *2)-1; //Max length one row of the board can have
    static int x = (MAX_ROW_LENGTH - BOARD_SIZE);
    static int INVALID_CELLS = x*x+x; //Number of invalid cells that are only there to simplify the board
    static int CELLS = MAX_ROW_LENGTH * MAX_ROW_LENGTH -INVALID_CELLS;
    private static int PLAYERS = 2;

    //Line length to win the game
    final static int LENGTH_TO_WIN = 4;

    public final static int INVALID_FIELD = -2;
    public final static int EMPTY = -1;
    public final static int PLAYER_ZERO = 0;
    public final static int PLAYER_ONE = 1;
    public final static int PLAYER_TWO = 2;

    final static double NEGATIVE_REWARD = -1.0;
    final static double POSITIVE_REWARD = 1.0;

    //Tile size in px
    final static int TILE_SIZE = 80;

    //Offset from the border of the window
    final static int OFFSET = TILE_SIZE/2;

    public final static int GAME_PIECE_RADIUS = TILE_SIZE/4;

    public static void setBoardSize(int size){
        BOARD_SIZE = size;
        MAX_ROW_LENGTH = (BOARD_SIZE *2)-1;
        x = (MAX_ROW_LENGTH - BOARD_SIZE);
        INVALID_CELLS = x*x+x; //
        CELLS = MAX_ROW_LENGTH * MAX_ROW_LENGTH -INVALID_CELLS;

    }

    public static int getMaxRowLength(){
        return MAX_ROW_LENGTH;
    }

    public static int getBoardSize(){
        return BOARD_SIZE;
    }

    public static void setPlayers(int players){
        PLAYERS = players;
    }

    public static int getPlayers(){
        return PLAYERS;
    }

}
