package games.Yavalath;

import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;
import tools.Types;

import java.io.Serializable;
import java.util.HashSet;

public class XNTupleFuncsYavalath extends XNTupleBase implements XNTupleFuncs, Serializable {

    private int[] actionVector;
    private int[][] actionArray;
    private transient BoardVector[] symmetryVectors;

    public XNTupleFuncsYavalath(){
        actionVector = new int[ConfigYavalath.CELLS];
        for (int i = 0; i < actionVector.length; i++) {
            actionVector[i] = i;
        }
        symmetryVectors = symmetryVectors(new BoardVector(actionVector),0);
        actionArray = new int[actionVector.length][symmetryVectors.length];

        for (int i = 0; i < actionArray.length; i++) {
            for (int j = 0; j < symmetryVectors.length; j++) {
                actionArray[i][j] = indexOfArray(symmetryVectors[j].bvec,i);
            }
        }
    }

    public int indexOfArray(int[] array, int number){
        for (int i = 0; i < array.length; i++) {
            if(array[i] == number) return i;
        }
        throw new RuntimeException("indexOfArray: array doesnt contain " + number);
    }

    @Override
    public int getNumCells() {
        return ConfigYavalath.CELLS;
    }

    @Override
    public int getNumPositionValues() {
        return ConfigYavalath.PLAYERS+1;
    }

    @Override
    public int getNumPlayers() {
        return ConfigYavalath.PLAYERS;
    }


    @Override
    public int getNumSymmetries() {
        return 12;
    }


    /**
     * The board vector is an {@code int[]} vector where each entry corresponds to one
     * cell of the board. In the case of TicTacToe the mapping is
     * <pre>
     *                      00  01  02  03  04
     *                    05  06  07  08  09  10
     *                  11  12  13  14  15  16  17
     *                18  19  20  21  22  23  24  25
     *              26  27  28  29  30  31  32  33  34
     *                35  36  37  38  39  40  41  42
     *                  43  44  45  46  47  48  49
     *                    50  51  52  53  54  55
     *                      56  57  58  59  60
     * </pre>
     * @return a vector of length {@link #getNumCells()}, holding for each board cell its
     * position value with 0=empty, 1="X", 2="O".
     */
    @Override
    public BoardVector getBoardVector(StateObservation so) {
        assert (so instanceof StateObserverYavalath);
        TileYavalath[][] board = ((StateObserverYavalath) so).getGameBoard();

        int[] boardValues = new int[getNumCells()];

        for (int i = 0, counter=0; i < ConfigYavalath.getMaxRowLength(); i++) {
            for (int j = 0; j < ConfigYavalath.getMaxRowLength(); j++) {
                if(board[i][j].getPlayer() != ConfigYavalath.INVALID_FIELD){
                    boardValues[counter] = board[i][j].getPlayer()+1;
                    counter++;
                }

            }
        }
        return new BoardVector(boardValues);
    }

    /**
     * Given a board vector from {@link #getBoardVector(StateObservation)} and given that the
     * game has s symmetries, return an array which holds at most s symmetric board vectors: <ul>
     * <li> the first element {@code symmetryVectors[0]} is the board vector itself
     * <li> the other elements are the board vectors when transforming {@code boardVector}
     * 		according to the s-1 other symmetries (e. g. rotation, reflection, if applicable).
     * </ul>
     * In the case of Yavalath we have s=12 symmetries (6 board rotations * 2 board flips)
     *
     */
     @Override
    public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {

        int syms = getNumSymmetries();
        BoardVector[] symmetryVectors = new BoardVector[syms];

        //Original boardvector
        symmetryVectors[0] = boardVector;
        //5 rotations clockwise
        for (int i = 1; i < 6; i++) {
            symmetryVectors[i] = rotate(symmetryVectors[i-1]);
        }

        //Mirrored original
        symmetryVectors[6] = mirror(boardVector);
        //Another 5 rotations clockwise on the mirrored vector
        for (int i = 7; i < 12; i++) {
            symmetryVectors[i] = rotate(symmetryVectors[i-1]);
        }
        return symmetryVectors;

    }

    @Override
    public int[] symmetryActions(int actionKey) {
        return actionArray[actionKey];
    }

    @Override
    public int[][] fixedNTuples(int mode) {
        return new int[0][];
    }

    //Needed so the arena can run, replace if you know what you are doing later
    @Override
    public String fixedTooltipString() {
        return "<html>"
                +"1"
                +"2"
                +"</html>";
    }

    //Needed so the arena can run, replace if you know what you are doing later
    @Override
    public int[] fixedNTupleModesAvailable() {
        return new int[]{1,2};
    }

    @Override
    public HashSet adjacencySet(int iCell) {

        HashSet adjSet = new HashSet();

        int maxRowLength = ConfigYavalath.getMaxRowLength();

        int tileNumber = ConfigYavalath.getTileValueFromAction(new Types.ACTIONS(iCell));
        int tileY = tileNumber % maxRowLength;
        int tileX = (tileNumber-tileY) / maxRowLength;

        if(isValidNeighbour(tileX-1,tileY-1)){
            int tileValue = ((tileX-1) * maxRowLength) + tileY-1;
            int actionValue = ConfigYavalath.getActionFromTileValue(tileValue).toInt();
            adjSet.add(actionValue);
        }
        if(isValidNeighbour(tileX-1,tileY)){
            int tileValue = ((tileX-1) * maxRowLength) + tileY;
            int actionValue = ConfigYavalath.getActionFromTileValue(tileValue).toInt();
            adjSet.add(actionValue);
        }
        if(isValidNeighbour(tileX,tileY+1)){
            int tileValue = (tileX * maxRowLength) + tileY+1;
            int actionValue = ConfigYavalath.getActionFromTileValue(tileValue).toInt();
            adjSet.add(actionValue);
        }
        if(isValidNeighbour(tileX+1,tileY+1)){
            int tileValue = ((tileX+1) * maxRowLength) + tileY+1;
            int actionValue = ConfigYavalath.getActionFromTileValue(tileValue).toInt();
            adjSet.add(actionValue);
        }
        if(isValidNeighbour(tileX+1,tileY)){
            int tileValue = ((tileX+1) * maxRowLength) + tileY;
            int actionValue = ConfigYavalath.getActionFromTileValue(tileValue).toInt();
            adjSet.add(actionValue);
        }
        if(isValidNeighbour(tileX,tileY-1)){
            int tileValue = (tileX * maxRowLength) + tileY-1;
            int actionValue = ConfigYavalath.getActionFromTileValue(tileValue).toInt();
            adjSet.add(actionValue);
        }

        return adjSet;
    }

    /**
     * Checks if there is a valid tile on the board at position x,y.
     */
    private boolean isValidNeighbour(int x, int y){
        if((x >= 0 && x < ConfigYavalath.getMaxRowLength()) && (y >= 0 && y < ConfigYavalath.getMaxRowLength()))
            if(Math.abs(y-x) < ConfigYavalath.getBoardSize()) return true;
        return false;
    }


    /**
     * Rotate the board by 60 degrees clockwise.
     * Helper function for {@link XNTupleFuncsYavalath#symmetryVectors(BoardVector, int)}.
     * <pre>
     *              Original                             Rotate by 60 degrees
     *
     *          00  01  02  03  04                        26  18  11  05  00
     *        05  06  07  08  09  10                    35  27  19  12  06  01
     *      11  12  13  14  15  16  17                43  36  28  20  13  07  02
     *    18  19  20  21  22  23  24  25            50  44  37  29  21  14  08  03
     *  26  27  28  29  30  31  32  33  34        56  51  45  38  30  22  15  09  04
     *    35  36  37  38  39  40  41  42            57  52  46  39  31  23  16  10
     *      43  44  45  46  47  48  49                58  53  47  40  32  24  17
     *        50  51  52  53  54  55                    59  54  48  41  33  25
     *          56  57  58  59  60                        60  55  49  42  34
     *
     * </pre>
     */
    private BoardVector rotate(BoardVector boardVector){
        int[] bvec = boardVector.bvec;
        int[] rotationIndex = createRotationIndex();int[] result = new int[bvec.length];
        for (int i = 0; i < bvec.length; i++) {
            result[i] = bvec[rotationIndex[i]];
        }

        return new BoardVector(result);
    }

    /**
     * Mirror the board along the vertical central axis.
     * Helper function for {@link XNTupleFuncsYavalath#symmetryVectors(BoardVector, int)}.
     * <pre>
     *              Original                              Mirrored Vertically
     *
     *          00  01  02  03  04                      04  03  02  01  00
     *        05  06  07  08  09  10                  10  09  08  07  06  05
     *      11  12  13  14  15  16  17              17  16  15  14  13  12  11
     *    18  19  20  21  22  23  24  25          25  24  23  22  21  20  19  18
     *  26  27  28  29  30  31  32  33  34      34  33  32  31  30  29  28  27  26
     *    35  36  37  38  39  40  41  42          42  41  40  39  38  37  36  35
     *      43  44  45  46  47  48  49              49  48  47  46  45  44  43
     *        50  51  52  53  54  55                  55  54  53  52  51  50
     *          56  57  58  59  60                      60  59  58  57  56
     *
     * </pre>
     */
    private BoardVector mirror(BoardVector boardVector){
        int[] bvec = boardVector.bvec;
        int[] mirrorIndex = createMirrorIndex();
        int[] result = new int[bvec.length];

        for (int i = 0; i < bvec.length; i++) {
            result[i] = bvec[mirrorIndex[i]];
        }
        return new BoardVector(result);
    }

    /**
     * Helper function for {@link XNTupleFuncsYavalath#mirror(BoardVector)}.
     * Creates an action vector for the mirrored board.
     * Algorithm can probably be a lot better/easier to understand.
     */
    private int[] createMirrorIndex(){
        int maxRowLength = ConfigYavalath.getMaxRowLength();
        int boardSize = ConfigYavalath.getBoardSize();
        int currentRowLength;
        int[] output = new int[ConfigYavalath.CELLS];
        int outputcounter = 0,start=-1;
        for (int i = 0; i < boardSize; i++) {
            currentRowLength = boardSize+i;
            start += currentRowLength;
            for (int j = 0; j < currentRowLength; j++,outputcounter++) {
                output[outputcounter] = start-j;
            }
        }

        for (int i = 1; i < boardSize; i++) {
            currentRowLength = maxRowLength-i;
            start += currentRowLength;
            for (int j = 0; j < currentRowLength; j++,outputcounter++) {
                output[outputcounter] = start-j;
            }
        }
        return output;
    }

    /**
     *  Helper function for {@link XNTupleFuncsYavalath#rotate(BoardVector)}.
     *  Creates an action vector for the rotated board.
     *  Algorithm can probably be a lot better/easier to understand.
     */
    private int[] createRotationIndex(){
        int boardSize = ConfigYavalath.getBoardSize();
        int[][] board = getBoardArray();

        int curRowLength=boardSize-1,curRowStart=-curRowLength,curRowEnd;
        int outputPlacement;
        int[] output = new int[ConfigYavalath.CELLS];

        for (int i = 0; i < boardSize; i++) {
            curRowStart += curRowLength;
            curRowLength++;
            curRowEnd = curRowStart + curRowLength-1;

            outputPlacement = curRowEnd;

            for (int j = 0, count=0, y=1; j < curRowLength; j++,count++) {
                if(count<boardSize){
                    output[outputPlacement] = board[j][i];
                }else{
                    output[outputPlacement] = board[j][i-y];
                    y++;
                }
                outputPlacement--;
            }

        }

        for (int i = boardSize,x = boardSize-1,m=1; i < boardSize*2-1; i++,x--,m++) {
            curRowStart += curRowLength;
            curRowLength--;
            curRowEnd = curRowStart + curRowLength-1;

            outputPlacement = curRowEnd;

            for (int j = 0,count=0,y=1; j < curRowLength; j++,count++) {
                if(count<x){
                    output[outputPlacement] = board[j+m][i];
                }else{
                    output[outputPlacement] = board[j+m][i-y];
                    y++;
                }
                outputPlacement--;
            }
        }

        return output;
    }


    /**
     * Creates an array representing the board that contains the corresponding action value. Helper function for rotating the board.
     */
    private int[][] getBoardArray(){
        int maxRowLength = ConfigYavalath.getMaxRowLength();
        int boardSize = ConfigYavalath.getBoardSize();

        int[][] board = new int[maxRowLength][];
        int counter = 0, currentRowLength=boardSize;
        int adjustment = 1;
        for (int i = 0; i < maxRowLength; i++) {
            board[i] = new int[currentRowLength];
            for (int j = 0; j < currentRowLength; j++) {
                board[i][j] = counter;
                counter++;
            }
            currentRowLength+= adjustment;
            if(currentRowLength == maxRowLength) adjustment=-1;
        }

        return board;
    }
}
