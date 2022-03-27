package games.Yavalath;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import tools.Types;
public class ConfigYavalath {

    private static int BOARD_SIZE = 5; //Edge length of the board
    private static int MAX_ROW_LENGTH = (BOARD_SIZE *2)-1; //Max length one row of the board can have
    static int x = (MAX_ROW_LENGTH - BOARD_SIZE);
    static int INVALID_CELLS = x*x+x; //Number of invalid cells that are only there to simplify the board
    static int CELLS = MAX_ROW_LENGTH * MAX_ROW_LENGTH -INVALID_CELLS;
    static int PLAYERS = 2;

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

    /**
     * Maps the values from tiles and actions to each other. While actions are numbered continuously,
     * there are gaps in the tile values produced by the board representation with invalid cells.
     * Example for the size 5 board
     * <pre>
     *               Tile values                            Action values
     *
     *           00  01  02  03  04                     00  01  02  03  04
     *         09  10  11  12  13  14                 05  06  07  08  09  10
     *       18  19  20  21  22  23  24             11  12  13  14  15  16  17
     *    27  28  29  30  31  32  33  34          18  19  20  21  22  23  24  25
     *  36  37  38  39  40  41  42  43  44      26  27  28  29  30  31  32  33  34
     *    46  47  48  49  50  51  52  53          35  36  37  38  39  40  41  42
     *      56  57  58  59  60  61  62              43  44  45  46  47  48  49
     *        66  67  68  69  70  71                  50  51  52  53  54  55
     *          76  77  78  79  80                      56  57  58  59  60
     *
     * </pre>
     */
    public static BiMap<Integer,Integer> actionMapping = HashBiMap.create();

    /**
     * Converts the value of a tile into a corresponding action.
     * @param tileValue
     * @return
     */
    public static Types.ACTIONS getActionFromTileValue(int tileValue){
        if(actionMapping.size() == 0) createActionMap();
        return new Types.ACTIONS(actionMapping.get(tileValue));
    }

    /**
     * Converts an action into the corresponding tile value.
     * @param action
     * @return
     */
    public static int getTileValueFromAction(Types.ACTIONS action){
        if(actionMapping.size() == 0) createActionMap();
        return actionMapping.inverse().get(action.toInt());
    }

    private static void createActionMap(){
        int actionCounter=0;

        for (int i = 0; i < MAX_ROW_LENGTH; i++) {
            for (int j = 0; j < MAX_ROW_LENGTH; j++) {
                if(Math.abs(j-i) < ConfigYavalath.getBoardSize()){
                    actionMapping.put(i*MAX_ROW_LENGTH+j,actionCounter);
                    actionCounter++;
                }
            }
        }
    }

    public static void setBoardSize(int size){
        BOARD_SIZE = size;
        MAX_ROW_LENGTH = (BOARD_SIZE *2)-1;
        x = (MAX_ROW_LENGTH - BOARD_SIZE);
        INVALID_CELLS = x*x+x; //
        CELLS = MAX_ROW_LENGTH * MAX_ROW_LENGTH -INVALID_CELLS;

        //Need to reset the map in case size is changed without a total shutdown
        actionMapping = null;
        actionMapping = HashBiMap.create();

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
