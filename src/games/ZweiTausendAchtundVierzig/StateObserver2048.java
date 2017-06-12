package games.ZweiTausendAchtundVierzig;

import games.StateObservation;
import games.StateObservationNondeterministic;
import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class {@link StateObserver2048} holds a 2048 game state.
 * The game state is coded in a compact way in *one* long number
 * <pre>
 *                 private boardB = 0xfedcba9876543210L
 *
 *                                 row
 *                      f e d c    3
 *                      b a 9 8    2
 *                      7 6 5 4    1
 *                      3 2 1 0    0
 *
 *                 col= 3 2 1 0            </pre>
 * that is, the 4 lowest hex digits represent the lowest row of the 2048-board, with the
 * digit 3 representing the leftmost cell in this row and so on. <br>
 * Each hex digit represents the exponent {@code exp} of a tile value {@code 2^exp}.
 * Example: Hex digit {@code a} codes tile value {@code 2^a} = 1024.  <p>
 *
 * Method getGameScore2 is currently implemented via StateObserver2048Slow.getGameScore2
 * (may be slow).
 *
 * @author Wolfgang Konen, THK
 * @author Johannes Kutsch
 */
public class StateObserver2048 implements StateObservationNondeterministic {
    private Random random = new Random();
    protected List<Integer> emptyTiles = new ArrayList();
    protected List<Integer> availableMoves = new ArrayList();   // 0: left, 1: up, 2: right, 3: down
    protected Types.ACTIONS[] actions;

    private long boardB;

    private int winState = 0;                                   // 0 = running, 1 = won, -1 = lost
    public int score = 0;
    public int highestTileValue = Integer.MIN_VALUE;
    public boolean highestTileInCorner = false;
    public int rowLength = 0;
    public int rowValue = 0;
    public int mergeValue = 0;
    public int moves = 0;
    private boolean isNextActionDeterministic;

    public Types.ACTIONS[] storedActions = null;
    public Types.ACTIONS storedActBest = null;
    public double[] storedValues = null;
    private double storedMaxScore;
    private Types.ACTIONS nextNondeterminisitcAction;

    public final static double MAXSCORE = 3932156;
    public final static double MINSCORE = 0;
    private static final double REWARD_NEGATIVE = -1.0;
    private static final double REWARD_POSITIVE =  1.0;

    public StateObserver2048() {
        newBoard();
    }

    public StateObserver2048(long board) {
        boardB=board;
        updateEmptyTiles();
        updateAvailableMoves();
        isNextActionDeterministic = true;
    }

    public StateObserver2048(long board, int score, int winState, boolean isNextActionDeterministic) {
        this.isNextActionDeterministic = isNextActionDeterministic;
        boardB=board;
        updateEmptyTiles();
        this.score = score;
        this.winState = winState;
        updateAvailableMoves();
    }

    /**
     * Construct an 2048 game state from {@code int[r][c]} array, where row r=0 is the
     * highest row and column c=0 is the left column
     * @param values the tile values {@code 2^exp}
     * @param score
     * @param winState
     */
    @Deprecated
    public StateObserver2048(int[][] values, int score, int winState, boolean isNextActionDeterministic) {
        this.isNextActionDeterministic = isNextActionDeterministic;
        boardB=0;
        updateEmptyTiles();                // add all cells to emptyTiles
        for(int row = 0, position = 15; row < ConfigGame.ROWS; row++) {
            for(int column = 0; column < ConfigGame.COLUMNS; column++,position--) {
                int k,b2 = values[row][column];
                for (k=0; k<16; k++) {
                    // find the exponent k in 2^k by down-shifting:
                    b2 = b2>>1;
                    if (b2==0) break;
                }
                if (k>0)
                    addTile(position,k);        // deletes also 'position' from emptyTiles
            }
        }
        updateEmptyTiles();
        this.score = score;
        this.winState = winState;

        updateAvailableMoves();
    }

    // Note: StateObs2048 copy() copies the board state, score, winState,
    // but it does NOT copy storedActions, storedActBest, storedValues, storedMaxScore.
    public StateObserver2048 copy() {
        return new StateObserver2048(boardB, score, winState, isNextActionDeterministic);
    }

    public boolean isGameOver() {
        if(availableMoves.size() == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isLegalState() {
        return true;
    }

    public Types.WINNER getGameWinner() {
        assert isGameOver() : "Game is not yet over!";
        switch (winState) {
            case 1:
                return Types.WINNER.PLAYER_WINS;
            default:
                return Types.WINNER.PLAYER_LOSES;
        }
    }

    public double getGameScore() {
        if(ConfigGame.ENABLEHEURISTICS) {
            return getGameScore2();
        } else {
            return getGameScore1();
        }
    }

    private double getGameScore1() {
        if(score == 0) {
            return 0;
        } else {
            return score / MAXSCORE;
        }
    }

    private double getGameScore2() {
//            int[][] values = new int[MCAgentConfig.ROWS][MCAgentConfig.COLUMNS];
//        for(int row = MCAgentConfig.ROWS-1, position=0; row >=0 ; row--) {
//            for(int column = MCAgentConfig.COLUMNS-1; column >=0 ; column--,position++) {
//                long b2 = boardB;
//                values[row][column] = (1 << (b2 & 0x0fL));
//                b2 = (b2 >> 4);
//            }
//        }
        StateObserver2048Slow so = new StateObserver2048Slow(this.toArray(),score,winState);

        double score2 = so.getGameScore2();
        this.highestTileInCorner = false;
        this.rowLength = so.rowLength;
        this.rowValue = so.rowValue;
        this.mergeValue = so.mergeValue;
        return score2;
    }

    public double getGameValue() { return getGameScore(); }

    public double getGameScore(StateObservation referingState) {
        assert (referingState instanceof StateObserver2048) : "referingState is not of class StateObserver2048";
        return this.getGameScore();
    }

    public double getMinGameScore() {
        return REWARD_NEGATIVE;
    }

    public double getMaxGameScore() {
        return REWARD_POSITIVE;
    }

    public String getName() {
        return "2048";
    }

    public double getBoard() {
        return boardB;
    }

    /**
     * The board vector is an {@code int[]} vector where each entry corresponds to one
     * cell of the board. In the case of 2048 the mapping is
     * <pre>
     *    00 01 02 03
     *    04 05 06 07
     *    08 09 10 11
     *    12 13 14 15
     * </pre>
     * @return a vector of length 16, holding for each board cell its
     * position value 0:empty, 1: tile 2^1, 2: tile 2^2,..., P-1: tile 2^(P-1).
     */
    public int[] getBoardVector() {
        int[] bvec = new int[16];
        long b2 = boardB;
        for (int n=15; n>=0; n--) {
            bvec[n] = (int)(b2 & 0x000000000000000fL);
            b2 = b2 >> 4;
        }
        return bvec;
    }

    public void advance(Types.ACTIONS action) {
        int iAction = action.toInt();
        assert (availableMoves.contains(iAction)) : "iAction is not viable.";
        move(iAction);
        updateEmptyTiles();
        addRandomTile();
        updateAvailableMoves();
        isNextActionDeterministic = true;
        nextNondeterminisitcAction = null;
    }

    public ArrayList<Types.ACTIONS> getAvailableActions() {
        ArrayList<Types.ACTIONS> availAct = new ArrayList<>();
        for(int viableMove : availableMoves) {
            availAct.add(Types.ACTIONS.fromInt(viableMove));
        }
        return availAct;
    }

    public int getNumAvailableActions() {
        return availableMoves.size();
    }

    public void setAvailableActions() {
        ArrayList<Types.ACTIONS> acts = this.getAvailableActions();
        actions = new Types.ACTIONS[acts.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = acts.get(i);
        }
    }

    public Types.ACTIONS getAction(int i) {
        return actions[i];
    }

    public void advanceDeterministic(Types.ACTIONS action) {
        if(!isNextActionDeterministic) {
            throw new RuntimeException("Next action is nondeterministic but called advanceDeterministic()");
        }

        int iAction = action.toInt();
        assert (availableMoves.contains(iAction)) : "iAction is not viable.";
        move(iAction);
        updateEmptyTiles();

        isNextActionDeterministic = false;
    }

    public void advanceNondeterministic() {
        setNextNondeterminisitcAction();

        if(isNextActionDeterministic) {
            throw new RuntimeException("Next action is deterministic but called advanceNondeterministic()");
        }

        int iAction = nextNondeterminisitcAction.toInt();
        assert (emptyTiles.size() * 2 > iAction) : "iAction is not viable.";

        //System.out.println("Action: " + iAction + " Value: " + ((iAction%2)+1) + " Position: " + (iAction/2));

        addTile(emptyTiles.get(iAction/2), (iAction%2)+1);

        updateAvailableMoves();
        isNextActionDeterministic = true;
        nextNondeterminisitcAction = null;
    }

    /**
     * Selects a Tile and the new value of the tile and saves it in an action
     * 0 = first Tile, value 2
     * 1 = first Tile, value 4
     * 2 = second Tile, value 2
     * 3 = second Tile, value 4
     * ....
     */
    private void setNextNondeterminisitcAction() {
        if(isNextActionDeterministic) {
            throw new RuntimeException("next Action is Deterministic");
        } else if(nextNondeterminisitcAction != null) {
            return;
        }


        //select a Tile
        int action = random.nextInt(emptyTiles.size()) * 2;

        //select the new Tile Value
        if(random.nextInt(10) == 9) {
            action += 1;
        }

        nextNondeterminisitcAction = Types.ACTIONS.fromInt(action);
    }

    public boolean isNextActionDeterminisitc() {
        return isNextActionDeterministic;
    }

    public Types.ACTIONS getNextNondeterministicAction() {
        setNextNondeterminisitcAction();

        return nextNondeterminisitcAction;
    }

    public void storeBestActionInfo(Types.ACTIONS actBest, double[] vtable) {
        ArrayList<Types.ACTIONS> acts = this.getAvailableActions();
        storedActions = new Types.ACTIONS[acts.size()];
        storedValues = new double[acts.size()];
        for(int i = 0; i < storedActions.length; ++i)
        {
            storedActions[i] = acts.get(i);
            storedValues[i] = vtable[i];
        }
        storedActBest = actBest;
        storedMaxScore = vtable[acts.size()];
    }

    public int getPlayer() {
        return 0;
    }

    public int getNumPlayers() {
        return 1;
    }

    public String stringDescr() {
        return String.format("%016x", boardB);        // format as 16-hex-digit number with
        // leading 0's (if necessary)
        //return Long.toHexString(boardB);                // no leading zeros
    }

    public String toString() {
        return stringDescr();
    }

    public boolean isLegalAction(Types.ACTIONS action) {
        return availableMoves.contains(action.toInt());
    }

    public int getScore() {
        return score;
    }

    public int getWinState() {
        return winState;
    }

    public int getNumEmptyTiles() {
        return emptyTiles.size();
    }

    public int getTileValue(int pos) {
        long val = (boardB >> (15-pos)*4 & 0x0fL);
        return (int)Math.pow(2, val);
    }

    public long getBoardNum() {
        return boardB;
    }

    /**
     * Add tile 2^value to the 2048 board, i.e. change {@code boardB} accordingly.<br>
     * Assumes (and asserts) that board is empty at {@code position}.
     *
     * @param position one out of {0,...,15}, where to add the tile
     * @param value        the exponent (2^value is the tile value)
     */
    private void addTile(int position, int value) {
        boolean succ = emptyTiles.remove(new Integer(position));
        assert (succ==true) : "Something wrong in emptyTiles removal";
        //System.out.println(Long.toHexString(boardB));
        long b = (boardB >> (4*position)) & 0x0fL;
        assert (b==0) : "boardB is not empty at 'position'";
        long v = (long)value << (4*position);
        boardB = boardB + v;
        //System.out.println(Long.toHexString(v));
        updateHighestTile(value);
    }

    //ToDo: this seems to be buggy when using Evaluator 2048
    private void updateHighestTile(int exp) {
        int tileVal = (1 << exp);
        if(tileVal > highestTileValue) {
            highestTileValue = tileVal;
        }
    }

    /**
     * Given a new board state {@code boardB}, recompute the list {@code emptyTiles}
     * (Integer list with positions {0,...,15} which have a 0-tile)
     */
    private void updateEmptyTiles() {
        emptyTiles.clear();
        long b = boardB;
        for (int j=0; j<16; j++) {
            if ((b & 0x0fL)==0) emptyTiles.add(new Integer(j));
            b = b >> 4;                // shift down by one hex digit
        }
    }

    /**
     *
     * @param winState {@literal-1  > lost, 0 > running, 1 > won}
     */
    private void setWinState(int winState) {
        if(this.winState == 0) {
            this.winState = winState;
        }
    }

    /**
     * transform the board state to an {@code int[][]} array in the same way as
     * StateObservation.toArray() does: row 0 is highest row, column 0 is left column
     * @return each value in the {@code int[][]} array carries the tile value {@code 2^exp}
     */
    public int[][] toArray() {
        int[][] newBoard = new int[ConfigGame.ROWS][ConfigGame.COLUMNS];
        for(int row = 0, position = 15; row < ConfigGame.ROWS; row++) {
            for(int column = 0; column < ConfigGame.COLUMNS; column++,position--) {
                int exp = (int) ((boardB >> 4*position) & 0x0fL);
                newBoard[row][column] = (1 << exp);
            }
        }
        return newBoard;
    }

    private void updateAvailableMoves() {
        availableMoves.clear();
        int oldScore = score;
        long oldBoardB = boardB;
        if (leftAction().boardB!=oldBoardB)
            availableMoves.add(0);
        boardB=oldBoardB;
        if (upAction().boardB!=oldBoardB)
            availableMoves.add(1);
        boardB=oldBoardB;
        if (rightAction().boardB!=oldBoardB)
            availableMoves.add(2);
        boardB=oldBoardB;
        if (downAction().boardB!=oldBoardB)
            availableMoves.add(3);
        boardB=oldBoardB;
        score=oldScore;

        if(availableMoves.size() <= 0) {
            setWinState(-1);
        }

        setAvailableActions();
    }

    public void printBoard() {
        System.out.println("---------------------------------");
        for(int r=3; r>=0; r--)
        {
            System.out.print("|");
            RowBitShift row = this.getRow(r);
            for(int c=3; c>=0; c--) {
                int exp=row.d(c);
                if(exp != 0) {
                    int val = (1 << exp);
                    if(val < 10) {
                        System.out.print("   " + val + "   |");
                    }
                    else if(val < 100) {
                        System.out.print("  " + val + "   |");
                    }
                    else if(val < 1000) {
                        System.out.print("  " + val + "  |");
                    }
                    else if(val < 10000) {
                        System.out.print(" " + val + "  |");
                    }
                    else if(val < 100000) {
                        System.out.print("" + val + " |");
                    }
                }
                else {
                    System.out.print("   0   |");
                }
            }
            System.out.println();
            System.out.println("---------------------------------");
        }
        System.out.println("score = " + score);
        System.out.println();
    }

    private void addRandomTile () {
        if(emptyTiles.size() > 0) {
            int position = emptyTiles.get(random.nextInt(emptyTiles.size())).intValue();
            int value = ConfigGame.STARTINGVALUES[random.nextInt(ConfigGame.STARTINGVALUES.length)] >> 1;
            addTile(position, value);
        }
    }

    /**
     *
     * @param move {@literal 0 > left, 1 > up, 2 > right, 3 > down}
     */
    public void move(int move) {
        switch (move) {
            case 0:
                leftAction();
                break;

            case 1:
                upAction();
                break;

            case 2:
                rightAction();
                break;

            case 3:
                downAction();
                break;
        }
        moves++;
    }

    private void newBoard() {
        boardB = 0x0L;
        score = 0;
        winState = 0;
        isNextActionDeterministic = true;

        updateEmptyTiles(); // fixed Empty Tiles JK

        for(int i = ConfigGame.STARTINGFIELDS; i > 0; i--) {
            addRandomTile();
        }

        updateEmptyTiles();
        updateAvailableMoves();
    }

    private StateObserver2048 rightAction() {
        for (int k=0; k<4; k++) {
            RowBitShift row = this.getRow(k).rAction();
            this.putRow(row,k);
            this.score += row.score;
        }
        return this;
    }

    private StateObserver2048 leftAction() {
        for (int k=0; k<4; k++) {
            //System.out.println(String.format("Old: %04x", this.getRow(k).getRow()));
            RowBitShift row = this.getRow(k).lAction();
            //System.out.println(String.format("New: %04x", row.getRow()));
            this.putRow(row,k);
            this.score += row.score;
        }
        return this;
    }

    private StateObserver2048 downAction() {
        for (int k=0; k<4; k++) {
            RowBitShift row = this.getCol(k).rAction();
            this.putCol(row,k);
            this.score += row.score;
        }
        return this;
    }

    private StateObserver2048 upAction() {
        for (int k=0; k<4; k++) {
            RowBitShift row = this.getCol(k).lAction();
            this.putCol(row,k);
            this.score += row.score;
        }
        return this;
    }

    /**
     * Returns the {@code k}th row of the board state
     * @param k one out of {3,2,1,0} where 0 is the lowest row
     * @return
     */
    private RowBitShift getRow(int k) {
        long r = (boardB >> (16*k)) & 0x000000000000ffffL;
        RowBitShift row = new RowBitShift((int) r);
        return row;
    }

    private StateObserver2048 putRow(RowBitShift row, int k) {
        long[] andB = {0xffffffffffff0000L,
                0xffffffff0000ffffL,
                0xffff0000ffffffffL,
                0x0000ffffffffffffL};
        long r = ((long)row.getRow() << (16*k)) ;
        boardB = (boardB & andB[k]) + r;
        return this;
    }

    /**
     * Returns the {@code k}th column of the board state. The column returned is a
     * 4-hex-digit number with the highest digit being the tile in the highest row.
     * @param k one out of {3,2,1,0} where 0 is the rightmost column
     * @return
     */
    private RowBitShift getCol(int k) {
        long btemp = (boardB >> (4*k));  // shift the column to get in row 0 to digit 0
        long r = 0L;
        for (int j=3; j>=0; j--) {
            r = r << 4;
            r = r + ((btemp >> 16*j) & 0x000000000000000fL);
        }
        RowBitShift row = new RowBitShift((int) r);
        return row;
    }

    private StateObserver2048 putCol(RowBitShift row, int k) {
        long[] andB = {0xfff0fff0fff0fff0L,
                0xff0fff0fff0fff0fL,
                0xf0fff0fff0fff0ffL,
                0x0fff0fff0fff0fffL};
        long col = 0L;
        for (int j=3; j>=0; j--) {
            // shift one word (4 hex-digits) up and add the jth digit from row as lowest digit:
            col = (col << 16) + row.d(j);
        }
        // shift the digits from row to the locations (the '0's in the relevant andB[k]):
        col = col << 4*k;

        // add the boardB digits on all other places:
        boardB = (boardB & andB[k]) + col;
        return this;
    }

    @Override
    public boolean equals(Object arg0) {
        StateObserver2048 aItem = null;
        if (arg0 instanceof StateObserver2048) {
            aItem = (StateObserver2048) arg0;
        } else {
        	throw new RuntimeException("so is not a StateObserver2048 object!");
        }
        boolean res = (this.boardB == aItem.boardB);
        return (this.boardB == aItem.boardB);
    }
}
