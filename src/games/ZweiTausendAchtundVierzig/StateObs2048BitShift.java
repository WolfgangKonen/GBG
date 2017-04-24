package games.ZweiTausendAchtundVierzig;

import games.StateObservation;
import tools.Types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Class {@link StateObs2048BitShift} holds a 2048 game state.
 * The game state is coded in a compact way in *one* long number
 * <pre> 
 * 		private boardB = 0xfedcba9876543210L
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
 * Method getGameScore2 is currently implemented via StateObserver2048.getGameScore2 
 * (may be slow).
 * 
 * @author Wolfgang Konen, THK
 */
public class StateObs2048BitShift implements StateObservation {
    private Random random = new Random();
    protected List<Integer> emptyTiles = new ArrayList();
    protected List<Integer> availableMoves = new ArrayList();  // 0: left, 1: up, 2: right, 3: down
    private Tile[][] gameBoard;
	protected Types.ACTIONS[] actions;
	
	private long boardB; 
    
    private int winState = 0;		// 0 = running, 1 = won, -1 = lost
    public int score = 0;
    public int highestTileValue = Integer.MIN_VALUE;
    public boolean highestTileInCorner = false;
    public int rowLength = 0;
    public int rowValue = 0;
    public int mergeValue = 0;
    public int moves = 0;

    public Types.ACTIONS[] storedActions = null;
    public Types.ACTIONS storedActBest = null;
    public double[] storedValues = null;
    public double storedMaxScore;

    public final static double MAXSCORE = 3932156;
    public final static double MINSCORE = 0;
    private static final double REWARD_NEGATIVE = -1.0;
    private static final double REWARD_POSITIVE =  1.0;

    public StateObs2048BitShift() {
        newBoard();
    }

    public StateObs2048BitShift(long b) {    	
    	boardB=b;
        updateEmptyTiles();
        updateAvailableMoves();
    }
    public StateObs2048BitShift(long b, int score, int winState) {
    	boardB=b;
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
    public StateObs2048BitShift(int[][] values, int score, int winState) {
        boardB=0;
        updateEmptyTiles();		// add all cells to emptyTiles
        for(int row = 0, position=15; row < Config.ROWS; row++) {
            for(int column = 0; column < Config.COLUMNS; column++,position--) {
                int k,b2 = values[row][column];
            	for (k=0; k<16; k++) {
            		// find the exponent k in 2^k by down-shifting:
                    b2 = b2>>1;
            		if (b2==0) break;
            	}
            	if (k>0)
            		addTile(position,k);	// deletes also 'position' from emptyTiles 
            }
        }
        updateEmptyTiles();
        this.score = score;
        this.winState = winState;

        updateAvailableMoves();
    }

    @Override
    public StateObs2048BitShift copy() {
    	return new StateObs2048BitShift(boardB, score, winState);
    }

    @Override
    public boolean isGameOver() {
        if(availableMoves.size() == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean isLegalState() {
        return true;
    }

    @Override
    public Types.WINNER getGameWinner() {
        assert isGameOver() : "Game is not yet over!";
        switch (winState) {
            case 1:
                return Types.WINNER.PLAYER_WINS;
            default:
                return Types.WINNER.PLAYER_LOSES;
        }
    }

    @Override
    public double getGameScore() {
        if(Config.ENABLEHEURISTICS) {
            return getGameScore2();
        } else {
            return getGameScore1();
        }
    }

    public double getGameScore1() {
        if(score == 0) {
            return 0;
        } else {
            return score / MAXSCORE;
        }
    }

    public double getGameScore2() {
//    	int[][] values = new int[Config.ROWS][Config.COLUMNS];
//        for(int row = Config.ROWS-1, position=0; row >=0 ; row--) {
//            for(int column = Config.COLUMNS-1; column >=0 ; column--,position++) {
//                long b2 = boardB;
//                values[row][column] = (1 << (b2 & 0x0fL));
//                b2 = (b2 >> 4);
//            }
//        }      
        StateObserver2048 so = new StateObserver2048(this.toArray(),score,winState);
        
        double score2 = so.getGameScore2();
        this.highestTileInCorner = false;
        this.rowLength = so.rowLength;
        this.rowValue = so.rowValue;
        this.mergeValue = so.mergeValue;
        return score2;
    }

    @Override
	public double getGameValue() { return getGameScore(); }
	
    @Override
    public double getGameScore(StateObservation referingState) {
        assert (referingState instanceof StateObs2048BitShift) : "referingState is not of class StateObs2048BitShift";
        return this.getGameScore();
    }

    @Override
    public double getMinGameScore() {
        return REWARD_NEGATIVE;
    }

    @Override
    public double getMaxGameScore() {
        return REWARD_POSITIVE;
    }

    @Override
    public void advance(Types.ACTIONS action) {
        int iAction = action.toInt();
        assert (availableMoves.contains(iAction)) : "iAction is not viable.";
        move(iAction);
        updateEmptyTiles();
        addRandomTile();
        updateAvailableMoves();
    }

    @Override
    public ArrayList<Types.ACTIONS> getAvailableActions() {
        ArrayList<Types.ACTIONS> availAct = new ArrayList<>();
        for(int viableMove : availableMoves) {
            availAct.add(Types.ACTIONS.fromInt(viableMove));
        }
        return availAct;
    }

    @Override
    public int getNumAvailableActions() {
        return availableMoves.size();
    }

    @Override
    public void setAvailableActions() {
        ArrayList<Types.ACTIONS> acts = this.getAvailableActions();
        actions = new Types.ACTIONS[acts.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = acts.get(i);
        }
    }

    @Override
    public Types.ACTIONS getAction(int i) {
        return actions[i];
    }

    @Override
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

    @Override
    public int getPlayer() {
        return 0;
    }

//    @Override
//    public int getPlayerPM() {
//        return 1;
//    }

    @Override
    public int getNumPlayers() {
        return 1;
    }

	@Override
    public String stringDescr() {
		return String.format("%016x", boardB);	// format as 16-hex-digit number with  
												// leading 0's (if necessary) 
		//return Long.toHexString(boardB);		// no leading zeros
    }

	@Deprecated
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

    public Tile getTile(int row, int column) {
    	int pos = column + 4*row;
    	long val = (boardB >> 4*pos) & 0x0fL;
    	Tile tile = new Tile((int)val, new Position(row,column));
        return tile;
    }
    
    public long getBoardNum() {
    	return boardB;
    }


    /**
     * Add tile 2^value to the 2048 board, i.e. change {@code boardB} accordingly.<br>
     * Assumes (and asserts) that board is empty at {@code position}.
     * 
     * @param position one out of {0,...,15}, where to add the tile
     * @param value	the exponent (2^value is the tile value)
     */
    public void addTile(int position, int value) {
        boolean succ = emptyTiles.remove(new Integer(position));
        assert (succ==true) : "Something wrong in emptyTiles removal";
        //System.out.println(Long.toHexString(boardB));
        long b = (boardB >> (4*position)) & 0x0fL;
        assert (b==0) : "boardB is not empty at 'position'";
        long v = (long)value << (4*position);
        boardB = boardB + v;
        //System.out.println(Long.toHexString(v));
        updateHighestTile(position,value);
    }

    public void updateHighestTile(int position, int exp) {
    	int tileVal = (1 << exp);
        if(tileVal > highestTileValue) {
            highestTileValue = tileVal;
        }
    }
    
	/**
	 * Given a new board state {@code boardB}, recompute the list {@code emptyTiles} 
	 * (Integer list with positions {0,...,15} which have a 0-tile)
	 */
    public void updateEmptyTiles() {
    	emptyTiles.clear();
    	long b = boardB;
    	for (int j=0; j<16; j++) {
    		if ((b & 0x0fL)==0) emptyTiles.add(new Integer(j)); 
    		b = b >> 4;		// shift down by one hex digit
    	}
    }

    /**
     *
     * @param winState {@literal-1  > lost, 0 > running, 1 > won} 
     */
    public void setWinState(int winState) {
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
        int[][] newBoard = new int[Config.ROWS][Config.COLUMNS];
        for(int row = 0, position=15; row < Config.ROWS; row++) {
            for(int column = 0; column < Config.COLUMNS; column++,position--) {
            	int exp = (int) ((boardB >> 4*position) & 0x0fL);
                newBoard[row][column] = (1 << exp);
            }
        }
        return newBoard;
    }

    public void updateAvailableMoves() {
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

    public void addRandomTile () {
        if(emptyTiles.size() > 0) {
            int position = emptyTiles.get(random.nextInt(emptyTiles.size())).intValue();
            int value = Config.STARTINGVALUES[random.nextInt(Config.STARTINGVALUES.length)] >> 1;
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

        for(int i = Config.STARTINGFIELDS; i > 0; i--) {
            addRandomTile();
        }

        updateEmptyTiles();
        updateAvailableMoves();
    }
    
    public StateObs2048BitShift rightAction() {
    	for (int k=0; k<4; k++) {
    		RowBitShift row = this.getRow(k).rAction();
    		this.putRow(row,k);
    		this.score += row.score;
    	}
    	return this;
    }
    
    public StateObs2048BitShift leftAction() {
    	for (int k=0; k<4; k++) {
    		//System.out.println(String.format("%04x", this.getRow(k).getRow()));
    		RowBitShift row = this.getRow(k).lAction();
    		//System.out.println(String.format("%04x", row.getRow()));
    		this.putRow(row,k);
    		this.score += row.score;
    	}
    	return this;
    }
    
    public StateObs2048BitShift downAction() {
    	for (int k=0; k<4; k++) {
    		RowBitShift row = this.getCol(k).rAction();
    		this.putCol(row,k);
    		this.score += row.score;
    	}
    	return this;
    }
    
    public StateObs2048BitShift upAction() {
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
    public RowBitShift getRow(int k) {
    	long r = (boardB >> (16*k)) & 0x000000000000ffffL;
    	RowBitShift row = new RowBitShift((int) r);
    	return row;
    }
    
    public StateObs2048BitShift putRow(RowBitShift row, int k) {
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
    public RowBitShift getCol(int k) {
    	long btemp = (boardB >> (4*k));  // shift the column to get in row 0 to digit 0
    	long r = 0L;
    	for (int j=3; j>=0; j--) {
    		r = r << 4;
    		r = r + ((btemp >> 16*j) & 0x000000000000000fL);
    	}
    	RowBitShift row = new RowBitShift((int) r);
    	return row;
    }
    
    public StateObs2048BitShift putCol(RowBitShift row, int k) {
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
    
    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
    	
        if (args.length == 0) {
            ;
        } else {
            throw new RuntimeException("[StateObs2048ByteShift.main] args=" + args + " not allowed. Use TicTacToeBatch.");
        }
        
		// some test output for checking bit shift operations
//        long k = 6;
//        System.out.println(Long.toHexString(k << 1));
//        System.out.println(Long.toHexString(k << 5));
//        System.out.println(Long.toHexString((k <<  1) & 0xFFFF));
//        System.out.println(Long.toHexString((k << 14) & 0xFFFF));
//        System.out.println((k << 1));
//        System.out.println((k << 14));
//        System.out.println(((k <<  1) & 0xFFFF));
//        System.out.println(((k << 14) & 0xFFFF));
//        long longpid = 24;
//        String s = String.format("%016x", longpid);  // 16 hex digits with leading zeros
//        System.out.println(s);
		

        // testing RowByteShift.rShift:
        for (int k=0; k<4; k++) {
            RowBitShift row = new RowBitShift(0x231F);
            int d = row.d(k);
            System.out.print("k="+k+": "+Integer.toHexString(d)
            					+", "+Integer.toHexString(row.getRow()));
            row.rShift(k);
            System.out.println(", " + Integer.toHexString(row.getRow()));        	
        }
        
        // testing RowByteShift.rMerge:
        for (int r=0; r<3; r++) {
            RowBitShift row = new RowBitShift(0x3333);
            int d = row.d(r);
            System.out.print("r="+r+": "+Integer.toHexString(d)
            					+", "+Integer.toHexString(row.getRow()));
            row.rMerge(r);
            System.out.println(", " + Integer.toHexString(row.getRow()));        	
        }

        // testing RowByteShift.rAction, lAction:
        RowBitShift row = new RowBitShift(0x22aa);
        System.out.println("rAction: "+Integer.toHexString(row.getRow())
        						 +", "+Integer.toHexString(row.rAction().getRow()));
        row = new RowBitShift(0x2201);
        System.out.println("lAction: "+Integer.toHexString(row.getRow())
        						 +", "+Integer.toHexString(row.lAction().getRow()));

        // testing StateObs2048BitShift.rightAction (leftAction):
        // testing StateObs2048BitShift.toArray and StateObs2048BitShift(int[][],double,double):
        StateObs2048BitShift sob = new StateObs2048BitShift(0x4132011030302211L);
        StateObs2048BitShift so2 = new StateObs2048BitShift(sob.toArray(),sob.score, sob.winState);
        //System.out.println(sob.stringDescr());
        //System.out.println(so2.stringDescr());
        assert sob.boardB == so2.boardB : "Assertion for toArray() failed";
        for (int c=3; c>=0; c--) {
        	System.out.println("col="+c+": "+Integer.toHexString(sob.getCol(c).getRow()));
        }
        System.out.println(Long.toHexString(sob.getBoardNum()));
        //sob.rightAction();
        sob.leftAction();
        System.out.println(Long.toHexString(sob.getBoardNum()));
        
        // testing StateObs2048BitShift.putCol
        sob = new StateObs2048BitShift(0);
        int[] colArr = {0x0123, 0x4567, 0xba98, 0xcdef};
        for (int c=3; c>=0; c--) {
        	RowBitShift col = new RowBitShift(colArr[c]);
        	sob.putCol(col, c);
        	System.out.println("col="+c+": "+String.format("%04x",colArr[c])
        						+", "+String.format("%016x",sob.getBoardNum()));
        }
        
        // time measurement for StateObs2048BitShift.advance vs. StateObserver2048.advance:
		int[][] state = {{0,2048,0,0}, {2,2,2,2}, {0,2,4,0}, {4,4,4,4}};
		StateObserver2048 so = new StateObserver2048(state,0,0);
		System.out.println(so.stringDescr());
		long startTime=System.nanoTime();
		Types.ACTIONS act = Types.ACTIONS.fromInt(0);
		so.advance(act);
		System.out.println("StateObserver2048    time: "+(System.nanoTime()-startTime));
		long lstate = 0x0b00111101202222L;
		StateObs2048BitShift sbs = new StateObs2048BitShift(lstate);
		startTime=System.nanoTime();
		sbs.advance(act);
		System.out.println("StateObs2048BitShift time:  "+(System.nanoTime()-startTime));
		System.out.println(so.toHexString()+",  score="+so.getScore());
		System.out.println(sbs.stringDescr()+",  score="+sbs.getScore());
    }


}

/**
 * RowBitShift represents an row of the 2048 board in the four lowest hex digits of 
 * an int. Digit 3 is the leftmost tile, digit 0 the rightmost tile.  <br>
 * (It represents as well columns of the 2048 board, then digit 3 is the highest tile, 
 * digit 0 is the lowest tile.) <p>
 * 
 * The hex value for each digit is {@code exp} in tile {@code 2^exp}. <p>
 * 
 * RowBitShift has methods {@link RowBitShift#lAction()} and {@link RowBitShift#rAction()} 
 * for left and right move action according to the rules of 2048. On first pass through 
 * this methods, static transposition tables {@code tabLeft} and {@code tabRight} are filled 
 * which contain for each possible row value the resulting row. <p>
 * 
 * This speeds up the calculation in {@link StateObs2048BitShift#advance(Action)} by a 
 * factor of 10 as compared to {@link StateObserver2048#advance(Action)}
 *
 * @author Wolfgang Konen, THK
 */
class RowBitShift {
	int rowB;	// the four lowest hex digits (16 bit) of this 32-bit int are used 
	int score=0;
	static int[] tabRight = null;
	static int[] tabLeft = null; 
	static int[] scoreRight = null;
	static int[] scoreLeft = null; 
	
	public RowBitShift(int row) {
		this.rowB = row;
	}
	
	public RowBitShift(RowBitShift rbs) {
		this.rowB = rbs.rowB;
	}
	
	/**
	 * Extract the k-th hexadecimal digit
	 * @param k	one out of {3,2,1,0}, where 3 is the highest digit
	 * @return an int holding the k-th hexadecimal digit
	 */
	public int d(int k) {
		if (k>3 || k<0) throw new RuntimeException("k"+k+" is not in allowed range {0,1,2,3}");
		return (rowB >> (k*4)) & 0x0F;
	}
	
	/**
	 * Shift digits from 3 to {@code lower} by one hex digit (4 bit) to the right, 
	 * but leave the digits below {@code lower} untouched.
	 * 
	 * @param lower one out of {3,2,1,0}, where 3 is the highest digit
	 * @return the shifted row
	 */
	public RowBitShift rShift(int lower) {
		if (lower>3 || lower<0) throw new RuntimeException("lower"+lower+" is not in allowed range {0,1,2,3}");
		int[] andS = {0xffff, 0xfff0, 0xff00, 0xf000};
		int[] andR = {0x0000, 0x000f, 0x00ff, 0x0fff};
		int shift = (rowB >> 4)  & andS[lower];
		rowB = shift + (rowB & andR[lower]);
		
		return this;
	}
	
	/**
	 * Shift digits from 0 to {@code higher} by one hex digit (4 bit) to the left, 
	 * but leave the digits above {@code higher} untouched.
	 * 
	 * @param higher one out of {3,2,1,0}, where 3 is the highest digit
	 * @return the shifted row
	 */
	public RowBitShift lShift(int higher) {
		if (higher>3 || higher<0) throw new RuntimeException("higher"+higher+" is not in allowed range {0,1,2,3}");
		int[] andS = {0x000f, 0x00ff, 0x0fff, 0xffff};
		int[] andR = {0xfff0, 0xff00, 0xf000, 0x0000};
		int shift = (rowB << 4)  & andS[higher];
		rowB = shift + (rowB & andR[higher]);
		
		return this;
	}
	
	/**
	 * Merge hex digits {@code r+1} and {@code r} on digit {@code r} (right merge),  
	 * assuming that they both contain the same, non-zero value. 
	 * Shift digits above {@code r} accordingly.
	 * Leave digits below {@code r} untouched.
	 * 
	 * @param r one out of {2,1,0}
	 * @return the merged row
	 */
	public RowBitShift rMerge(int r) {
		if (r>2 || r<0) throw new RuntimeException("r="+r+" is not in allowed range {0,1,2}");
		int exp = this.d(r);
		if (exp!=this.d(r+1)) throw new RuntimeException("Digits "+(r+1)+" and "+r+" are not the same"); 
		if (exp==0) throw new RuntimeException("Digit "+r+" must be greater than zero"); 

		// andR is a bit mask which lets all digits pass except the two to-be-merged digits:
		int[] andR = {0xff00, 0xf00f, 0x00ff};   
									
		// since each digit holds the exponent exp of tile 2^exp, merging two tiles (doubling) 
		// is the same as adding 1 to the exponent:
		int newd = exp+1;	
		
		// the score delta is 2^newd:
		this.score += (1 << newd);
 
		// shift the merged result back to digit r and add the 'passed' digits:
		rowB = (newd << (4*r)) + (rowB & andR[r]); 

		this.rShift(r+1);
		return this;
	}
	
	/**
	 * Merge hex digits {@code r} and {@code r-1} on digit {@code r} (left merge),  
	 * assuming that they both contain the same, non-zero value. 
	 * Shift digits below {@code r} accordingly.
	 * Leave digits above {@code r} untouched.
	 * 
	 * @param r one out of {3,2,1}
	 * @return the merged row
	 */
	public RowBitShift lMerge(int r) {
		if (r>3 || r<1) throw new RuntimeException("r="+r+" is not in allowed range {1,2,3}");
		int exp = this.d(r);
		if (exp!=this.d(r-1)) throw new RuntimeException("Digits "+r+" and "+(r-1)+" are not the same"); 
		if (exp==0) throw new RuntimeException("Digit "+r+" must be greater than zero"); 

		// andR is a bit mask which lets all digits pass except the two to-be-merged digits:
		int[] andR = {0x0000, 0xff00, 0xf00f, 0x00ff};   
									
		// since each digit holds the exponent exp of tile 2^exp, merging two tiles (doubling) 
		// is the same as adding 1 to the exponent:
		int newd = exp+1;		
		
		// the score delta is 2^newd:
		this.score += (1 << newd);
 
		// shift the merged result back to digit r and add the 'passed' digits:
		rowB = (newd << (4*r)) + (rowB & andR[r]); 

		this.lShift(r-1);
		return this;
	}
	
	/**
	 * Perform a "right" action. <br>
	 * Use the static transposition table {@code tabRight} to do the job fast. An equivalent
	 * but slow version (w/o {@code tabRight}) is in {@link RowBitShift#rActionSlow()}. 
	 * @return the resulting row object
	 */
	public RowBitShift rAction( ) {
		if (tabRight==null) calcTabRight();
		this.score = scoreRight[rowB];
		this.rowB = tabRight[rowB];
		return this;
	}
	private void calcTabRight() {
		int sz = (1 << 16);
		tabRight = new int[sz];
		scoreRight = new int[sz];
		RowBitShift rbs = new RowBitShift(0);
		for (int i=0; i<sz; i++) {
			rbs.rowB=i;
			rbs.score=0;
			tabRight[i]=rbs.rActionSlow().getRow();
			scoreRight[i]=rbs.score;
		}
	}
	RowBitShift rActionSlow( ) {
		// remove the 'holes' (0-tiles) from left to right:
		for (int k=2; k>=0; k--) 
			if (this.d(k)==0) this.rShift(k);
		
		// merge adjacent same-value tiles from right to left:
		for (int r=0; r<3; r++)
			if (this.d(r+1)==this.d(r) && this.d(r)>0) this.rMerge(r);
		
		return this;
	}
	
	/**
	 * Perform a "left" action. <br>
	 * Use the static transposition table {@code tabLeft} to do the job fast. An equivalent
	 * but slow version (w/o {@code tabLeft}) is in {@link RowBitShift#lActionSlow()}. 
	 * @return the resulting row object
	 */
	public RowBitShift lAction( ) {
		if (tabLeft==null) calcTabLeft();
		this.score = scoreLeft[rowB];
		this.rowB = tabLeft[rowB];
		return this;
	}
	private void calcTabLeft() {
		int sz = (1 << 16);
		tabLeft = new int[sz];
		scoreLeft = new int[sz];
		RowBitShift rbs = new RowBitShift(0);
		for (int i=0; i<sz; i++) {
			rbs.rowB=i;
			rbs.score=0;
			tabLeft[i]=rbs.lActionSlow().getRow();
			scoreLeft[i]=rbs.score;
		}
	}
	RowBitShift lActionSlow( ) {
		// remove the 'holes' (0-tiles) from right to left:
		for (int k=1; k<4; k++) 
			if (this.d(k)==0) this.lShift(k);
		
		// merge adjacent same-value tiles from left to right:
		for (int r=3; r>0; r--)
			if (this.d(r-1)==this.d(r) && this.d(r)>0) this.lMerge(r);
		
		return this;
	}
	
	
	public int getRow() {
		return rowB;
	}
}

