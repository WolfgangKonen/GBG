package games.ZweiTausendAchtundVierzig;

import controllers.TD.ntuple2.NTupleBase;
import games.StateObservation;
import games.BoardVector;
import games.ObserverBase;
import games.StateObsNondeterministic;
import games.ZweiTausendAchtundVierzig.Heuristic.HeuristicSettings2048;
import tools.Types;
import tools.Types.ACTIONS;

import java.util.ArrayList;
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
 * digit 3 representing the leftmost tile in this row and so on. <br>
 * Each hex digit represents the exponent {@code exp} of a tile value {@code 2^exp}. The 
 * value {@code exp = 0} codes an empty tile. <br>
 * Example: Hex digit {@code a} codes tile value {@code 2^a} = 1024.  
 * <p>
 * See {@link #getBoardVector()} for the numbering of the cells in a {@code int[] boardVector}.
 * <p>
 * See {@link #updateAvailableMoves()} for the numbering of actions:<br>
 * 0: left, 1: up, 2: right, 3: down.
 *
 * @author Wolfgang Konen, TH Koeln
 * @author Johannes Kutsch
 */
public class StateObserver2048 extends ObserverBase implements StateObsNondeterministic {
	public static final String[] ACTIONSTRING = {" left", "   up", "right", " down"};
    private Random random = new Random();
    protected ArrayList<Integer> emptyTiles = new ArrayList<>();
    protected ArrayList<Integer> availableMoves = new ArrayList<>();   // 0: left, 1: up, 2: right, 3: down
    protected ACTIONS[] actions;

    private long boardB;

    private int winState = 0;           // 0 = running, 1 = won, -1 = lost
    public int score = 0;				// up-,down-,left-,rightAction add to this score
    private int highestTileValue = Integer.MIN_VALUE;
    private boolean highestTileInCorner = false;
    private int rowValue = 0;
    private int mergeValue = 0;
    private long cumulEmptyTiles = 0;
    private boolean isNextActionDeterministic;

    private ACTIONS nextNondeterministicAction;

    public final static double MAXSCORE = 3932156.0;
    public final static double MINSCORE = 0;				// never used
//    private static final double REWARD_NEGATIVE = -1.0;		// questionable
    private static final double REWARD_NEGATIVE =  0.0;		
    private static final double REWARD_POSITIVE =  1.0;		// questionable

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

    public StateObserver2048() {
        if (NTupleBase.DBG2_FIXEDSEQUENCE) {
            long seed=42L;					// DEBUG only
            random = new Random(seed);		//
        }
        newBoard();
    }

    public StateObserver2048(long board) {
        boardB=board;
        updateEmptyTiles();
        updateAvailableMoves();
        isNextActionDeterministic = true;
    }

    /**
     * 
     * @param board     the game board
     * @param score     the score
     * @param winState
     * @param cumulEmptyTiles
     * @param isNextActionDeterministic
     */
    public StateObserver2048(long board, int score, int winState, long cumulEmptyTiles, boolean isNextActionDeterministic) {
        this.isNextActionDeterministic = isNextActionDeterministic;
        boardB=board;
        updateEmptyTiles();
        this.score = score;
        this.cumulEmptyTiles = cumulEmptyTiles; 
        this.winState = winState;
        updateAvailableMoves();
    }

    /**
     * Construct an 2048 game state from {@code int[r][c]} array, where row r=0 is the
     * highest row and column c=0 is the left column.
     * @param values the tile values {@code 2^exp}
     * @param score
     * @param winState
     * @param isNextActionDeterministic
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

    public StateObserver2048(StateObserver2048 other) {
    	super(other);
        this.isNextActionDeterministic = other.isNextActionDeterministic;
        this.boardB=other.boardB;
        this.score = other.score;
        this.cumulEmptyTiles = other.cumulEmptyTiles; 
        this.winState = other.winState;
        this.highestTileValue = other.highestTileValue;
		this.actions = other.actions.clone();
		if (other.availableMoves!=null)	// this check is needed when loading older logs
			this.availableMoves = (ArrayList<Integer>) other.availableMoves.clone();
		if (other.emptyTiles!=null)	// this check is needed when loading older logs
			this.emptyTiles = (ArrayList<Integer>) other.emptyTiles.clone();
				// Note that clone does only clone the ArrayList, but not the contained objects, they are 
				// just copied by reference. However, these objects are never altered, so it is o.k.
//		updateEmptyTiles();			// this as replacement for clone() 
//		updateAvailableMoves();		// would be 20% slower
    }

    @Override
    public StateObserver2048 copy() {
//    	return copyOLD();
    	return copyNEW();
    }
    
    // Note: StateObserver2048::copyOLD() copies the board state, score, winState, cumulEmptyTiles, 
    // but it does NOT copy storedActions, storedActBest, storedValues, storedMaxScore.
    public StateObserver2048 copyOLD() {
    	StateObserver2048 so2 =  new StateObserver2048(boardB, score, winState, cumulEmptyTiles, isNextActionDeterministic);
    	so2.m_counter = this.m_counter;
    	so2.winState = this.winState;
    	so2.highestTileValue = this.highestTileValue;
//    	so2.moves = this.moves;
    	return so2;
    }

    // Note: StateObserver2048::copyNEW() copies everything, but is faster since it avoids updateEmptyTiles and
    // updateAvailableMoves
    public StateObserver2048 copyNEW() {
    	StateObserver2048 so2 =  new StateObserver2048(this);
    	return so2;
    }
    
    public boolean isGameOver() {
        return (availableMoves.size() == 0);
    }

    public boolean isLegalState() {
        return true;
    }

//    public Types.WINNER getGameWinner() {
//        assert isGameOver() : "Game is not yet over!";
//        switch (winState) {
//            case 1:
//                return Types.WINNER.PLAYER_WINS;
//            default:
//                return Types.WINNER.PLAYER_LOSES;
//        }
//    }


    public double getHeuristicBonus(HeuristicSettings2048 settings) {
        double bonus = 0;

        //find new Values
        evaluateBoard(settings.rowMethod);

        //Empty Tile Heuristic
        if(settings.enableEmptyTiles) {
            switch (settings.emptyTilesMethod) {
                case 0:
                    bonus += Math.pow(settings.emptyTilesWeighting0 + 1, emptyTiles.size());
                    break;
                case 1:
                    bonus += highestTileValue * emptyTiles.size() * settings.emptyTilesWeighting1;
                    break;
                case 2:
                    bonus += score * emptyTiles.size() * settings.emptyTilesWeighting2;
                    break;
                default:
                    throw new RuntimeException(settings.emptyTilesMethod + " is not a valid Empty Tiles Method");
            }
        }

        //Row Heuristic
        if(settings.enableRow) {
            switch (settings.rowMethod) {
                case 0:
                    bonus += rowValue * settings.rowWeighting0;
                    break;
                case 1:
                    bonus += rowValue * settings.rowWeighting1;
                    break;
                default:
                    throw new RuntimeException(settings.emptyTilesMethod + " is not a valid Row Method");
            }
        }

        //Merge Heuristic
        if(settings.enableMerge) {
            bonus += mergeValue * settings.mergeWeighting;
        }

        //Highest Tile In Corner Heuristic
        if (highestTileInCorner && settings.enableHighestTileInCorner) {
            bonus += highestTileValue * settings.highestTileIncornerWeighting;
        }

        return bonus;
    }

    private void evaluateBoard(int rowEvaluationMethod) {
//        int rowLength = 0;        // is assigned, but not used
        //reset old Values
        highestTileInCorner = false;
        rowValue = 0;
        mergeValue = 0;

        RowInformationContainer rowInformationContainer;

        boolean foundHighestTileInCorner = false;           //this is to prevent situations where multiple tiles with the
                                                            //same highest value in different corners trigger the heuristic

        //evaluate topleft corner
        if(getTileValue(0) == highestTileValue) {
            //check if highest tile is in this corner
            highestTileInCorner = true;
            foundHighestTileInCorner = true;

            //evaluate right row
            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 0, 1, 2, rowEvaluationMethod, 1);
            assert rowInformationContainer!=null : "Ooops, rowInformationContainer is null!";
            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
                rowValue = rowInformationContainer.rowValue;
            }

            //evaluate bottom row
            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 0, 1, 3, rowEvaluationMethod, 1);
            assert rowInformationContainer!=null : "Ooops, rowInformationContainer is null!";
            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
                rowValue = rowInformationContainer.rowValue;
            }
        }

        //evaluate topright corner
        if(getTileValue(3) == highestTileValue) {
            if(!foundHighestTileInCorner) {
                highestTileInCorner = true;
                foundHighestTileInCorner = true;
            } else {
                //there are multiple tiles with the same value in different corners
                highestTileInCorner = false;
            }

            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 0, 1, 0, rowEvaluationMethod, 1);
            assert rowInformationContainer!=null : "Ooops, rowInformationContainer is null!";
            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
                rowValue = rowInformationContainer.rowValue;
            }

            rowInformationContainer = evaluateRow(highestTileValue, 0, 3, 3, 1, 3, rowEvaluationMethod, -1);
            assert rowInformationContainer!=null : "Ooops, rowInformationContainer is null!";
            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
                rowValue = rowInformationContainer.rowValue;
            }
        }

        //evaluate bottomleft corner
        if(getTileValue(12) == highestTileValue) {
            if(!foundHighestTileInCorner) {
                highestTileInCorner = true;
                foundHighestTileInCorner = true;
            } else {
                //there are multiple tiles with the same value in different corners
                highestTileInCorner = false;
            }

            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 0, 1, 1, rowEvaluationMethod, 1);
            assert rowInformationContainer!=null : "Ooops, rowInformationContainer is null!";
            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
                rowValue = rowInformationContainer.rowValue;
            }

            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 3, 1, 2, rowEvaluationMethod, -1);
            assert rowInformationContainer!=null : "Ooops, rowInformationContainer is null!";
            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
                rowValue = rowInformationContainer.rowValue;
            }
        }

        //evaluate bottomright corner
        if(getTileValue(15) == highestTileValue) {
            if(!foundHighestTileInCorner) {
                highestTileInCorner = true;
            } else {
                //there are multiple tiles with the same value in different corners
                highestTileInCorner = false;
            }

            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 3, 1, 0, rowEvaluationMethod, -1);
            assert rowInformationContainer!=null : "Ooops, rowInformationContainer is null!";
            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
                rowValue = rowInformationContainer.rowValue;
            }

            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 3, 1, 1, rowEvaluationMethod, -1);
            assert rowInformationContainer!=null : "Ooops, rowInformationContainer is null!";
            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
                rowValue = rowInformationContainer.rowValue;
            }
        }

        //check for merges
        //vertical
        int mergeValue0 = 0;
        for(int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                int currentValue = getTileValue(row * 4 + column);
                if (currentValue != 0) {
                    for (int position = row + 1; position < ConfigGame.ROWS; position++) {
                        int newValue = getTileValue(position * 4 + column);
                        if (newValue != 0) {
                            if (currentValue == newValue) {
                                mergeValue0 += currentValue;
                                row = position + 1; //else a row with e.g. 4 | 4 | 4 | 0 would count 2 merges
                            }
                            break;
                        }
                    }
                }
            }
        }

        //horizontal
        int mergeValue1 = 0;
        for(int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                int currentValue = getTileValue(row * 4 + column);
                if(currentValue != 0) {
                    for (int position = column+1; position < ConfigGame.COLUMNS; position++) {
                        int newValue = getTileValue(row * 4 + position);
                        if(newValue != 0) {
                            if (currentValue == newValue) {
                                mergeValue1 += currentValue;
                                column = position + 1;
                            }
                            break;
                        }
                    }
                }
            }
        }

        mergeValue = Math.max(mergeValue0, mergeValue1);
    }

    /**
     * Evaluates a row of tiles and checks if the next tile in this row is valid for the row heuristic.
     * If the end of one row is reached, this method calls itself and continues with the next tile e.g.:
     * 0 => 1 => 2 => 3 =>
     * 7 => 6 => 5 => 4 =>
     * 8 => 9...
     *
     * There are two Methods to check if the next tile is a valid part of the row or not.
     * Method 0: The next tile is lower then the current tile
     * Method 1: The next tile has exactly half the value of the current tile
     *
     * @param currentTileValue the Value of the Tile that was last evaluated in the row or the value of the first Tile in a row if it is a new row
     * @param currentRowLength the currentRowLength
     * @param currentRowValue the currentRowValue
     * @param position the position of the row
     * @param offset a offset if we want to skip tiles in the row, set 1 when calling the method initially because the first tile is always part of a row
     * @param direction the direction, 0 => left, 1 => up, 2 => right, 3 => down
     * @param method the Method used to determine if the next tile is part of the row
     * @return a RowInformationContainer containing the currentRowLength and the currentRowValue
     */
    private RowInformationContainer evaluateRow(int currentTileValue, int currentRowLength, int currentRowValue, int position, int offset, int direction, int method, int newRowDirection) {
        switch (direction) {
            case 0:
                //left
                for (int i = 3 - offset; i >= 0; i--) {
                    switch (method) {
                        case 0:
                            if (getTileValue(position * 4 + i) != 0 && getTileValue(position * 4 + i) < currentTileValue) {
                                currentRowLength++;
                                currentTileValue = getTileValue(position * 4 + i);
                                currentRowValue += currentTileValue;
                            } else {
                                return new RowInformationContainer(currentRowLength, currentRowValue);
                            }
                            break;
                        case 1:
                            if (getTileValue(position * 4 + i) != 0 && getTileValue(position * 4 + i) == currentTileValue / 2) {
                                currentRowLength++;
                                currentTileValue = getTileValue(position * 4 + i);
                                currentRowValue += currentTileValue;
                            } else {
                                return new RowInformationContainer(currentRowLength, currentRowValue);
                            }
                            break;
                    }
                }

                return evaluateRow(currentTileValue, currentRowLength, currentRowValue, position + newRowDirection, 0, 2, method, newRowDirection);

            case 1:
                //up
                for (int i = 3 - offset; i >= 0; i--) {
                    switch (method) {
                        case 0:
                            if (getTileValue(i * 4 + position) != 0 && getTileValue(i * 4 + position) < currentTileValue) {
                                currentRowLength++;
                                currentTileValue = getTileValue(i * 4 + position);
                                currentRowValue += currentTileValue;
                            } else {
                                return new RowInformationContainer(currentRowLength, currentRowValue);
                            }
                            break;
                        case 1:
                            if (getTileValue(i * 4 + position) != 0 && getTileValue(i * 4 + position) == currentTileValue / 2) {
                                currentRowLength++;
                                currentTileValue = getTileValue(i * 4 + position);
                                currentRowValue += currentTileValue;
                            } else {
                                return new RowInformationContainer(currentRowLength, currentRowValue);
                            }
                            break;
                    }
                }

                return evaluateRow(currentTileValue, currentRowLength, currentRowValue, position + newRowDirection, 0, 3, method, newRowDirection);

            case 2:
                //right
                for (int i = offset; i < ConfigGame.COLUMNS; i++) {
                    switch (method) {
                        case 0:
                            if (getTileValue(position * 4 + i) != 0 && getTileValue(position * 4 + i) < currentTileValue) {
                                currentRowLength++;
                                currentTileValue = getTileValue(position * 4 + i);
                                currentRowValue += currentTileValue;
                            } else {
                                return new RowInformationContainer(currentRowLength, currentRowValue);
                            }
                            break;
                        case 1:
                            if (getTileValue(position * 4 + i) != 0 && getTileValue(position * 4 + i) == currentTileValue / 2) {
                                currentRowLength++;
                                currentTileValue = getTileValue(position * 4 + i);
                                currentRowValue += currentTileValue;
                            } else {
                                return new RowInformationContainer(currentRowLength, currentRowValue);
                            }
                            break;
                    }
                }
                return evaluateRow(currentTileValue, currentRowLength, currentRowValue, position + newRowDirection, 0, 0, method, newRowDirection);

            case 3:
                //down
                for (int i = offset; i < ConfigGame.ROWS; i++) {
                    switch (method) {
                        case 0:
                            if (getTileValue(i * 4 + position) != 0 && getTileValue(i * 4 + position) < currentTileValue) {
                                currentRowLength++;
                                currentTileValue = getTileValue(i * 4 + position);
                                currentRowValue += currentTileValue;
                            } else {
                                return new RowInformationContainer(currentRowLength, currentRowValue);
                            }
                            break;
                        case 1:
                            if (getTileValue(i * 4 + position) != 0 && getTileValue(i * 4 + position) == currentTileValue / 2) {
                                currentRowLength++;
                                currentTileValue = getTileValue(i * 4 + position);
                                currentRowValue += currentTileValue;
                            } else {
                                return new RowInformationContainer(currentRowLength, currentRowValue);
                            }
                            break;
                    }
                }
                return evaluateRow(currentTileValue, currentRowLength, currentRowValue, position + newRowDirection, 0, 1, method, newRowDirection);
        }

        return null;
    }

	/**
	 * @param refer only needed for the interface, not relevant in 1-person game 2048
	 * @return 	the game score
	 *  
	 */
    public double getGameScore(StateObservation refer) {
		return score / MAXSCORE;
    }

	/**
	 * Same as {@link #getGameScore(StateObservation referringState)}, but with the player of referringState. 
	 * @param player the player of referringState, a number in 0,1,...,N.
	 * @return  the game score
	 */
	public double getGameScore(int player) {
		return this.getGameScore(this);		// there is only one player in 2048
	}
	
	/**
	 * The cumulative reward, seen from the perspective of {@code referringState}'s player. This
	 * player dependence is only relevant for games with more than one player.
	 * @param referringState
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different, 
	 * 		  game-specific reward (currently {@link #getCumulEmptyTiles()})
	 * @return  the cumulative reward 
	 */
    @Override
	public double getReward(StateObservation referringState,boolean rewardIsGameScore) {
    	if (rewardIsGameScore) {
    		return getGameScore(referringState);    		
    	} else {
    		return this.getCumulEmptyTiles();
    	}
	}
    
	/**
	 * Same as getReward(referringState,rgs), but with the player of referringState. 
	 * @param player the player of referringState, a number in 0,1,...,N.
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different, 
	 * 		  game-specific reward (currently {@link #getCumulEmptyTiles()})
	 * @return  the cumulative reward 
	 */
	public double getReward(int player, boolean rewardIsGameScore) {
    	if (rewardIsGameScore) {
    		return this.getGameScore(this);    		
    	} else {
    		return this.getCumulEmptyTiles();
    	}
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
    public BoardVector getBoardVector() {
        int[] bvec = new int[16];
        long b2 = boardB;
        for (int n=15; n>=0; n--) {
            bvec[n] = (int)(b2 & 0x000000000000000fL);
            b2 = b2 >> 4;
        }
        return new BoardVector(bvec);
    }

    @Override
    public ArrayList<ACTIONS> getAvailableActions() {
        ArrayList<ACTIONS> availAct = new ArrayList<>();
        for(int viableMove : availableMoves) {
            availAct.add(ACTIONS.fromInt(viableMove));
        }
        return availAct;
    }

    @Override
	public ArrayList<ACTIONS> getAllAvailableActions() {
        ArrayList<ACTIONS> allActions = new ArrayList<>();
        for (int j = 0; j < 4; j++) 
        	allActions.add(Types.ACTIONS.fromInt(j));
        
        return allActions;
	}
	
   public int getNumAvailableActions() {
        return availableMoves.size();
    }
    
	public ArrayList<ACTIONS> getAvailableRandoms() {
        ArrayList<ACTIONS> availRan = new ArrayList<>();
        for (int i=0; i<emptyTiles.size()*2; i++) 
            availRan.add(ACTIONS.fromInt(i));
        return availRan;	
	}

	public int getNumAvailableRandoms() {
		return emptyTiles.size()*2;
	}

    public void setAvailableActions() {
        ArrayList<ACTIONS> acts = this.getAvailableActions();
        actions = new ACTIONS[acts.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = acts.get(i);
        }
    }

    public ACTIONS getAction(int i) {
        return actions[i];
    }

    public void advance(ACTIONS action) {
        super.advanceBase(action);		//		includes addToLastMoves(action)
        int iAction = action.toInt();
        assert (availableMoves.contains(iAction)) : "iAction is not viable.";
        move(iAction);				// deterministic part, contains super.incrementMoveCounter()
        updateEmptyTiles();
        addRandomTile();			// non-deterministic part
        updateAvailableMoves();
        isNextActionDeterministic = true;
        nextNondeterministicAction = null;
    }

    public void advanceDeterministic(ACTIONS action) {
        super.advanceBase(action);		//		includes addToLastMoves(action)
        if(!isNextActionDeterministic) {
            throw new RuntimeException("Next action is nondeterministic but called advanceDeterministic()");
        }

        int iAction = action.toInt();
        assert (availableMoves.contains(iAction)) : "iAction is not viable.";
        move(iAction);					// deterministic part, contains super.incrementMoveCounter()
        updateEmptyTiles();

        isNextActionDeterministic = false;
    }

    public void advanceNondeterministic(ACTIONS randAction) {
        if(isNextActionDeterministic) {
            throw new RuntimeException("Next action is deterministic but called advanceNondeterministic()");
        }

        int iAction = randAction.toInt();
        assert (emptyTiles.size() * 2 > iAction) : "iAction is not viable.";

        //System.out.println("Action: " + iAction + " Value: " + ((iAction%2)+1) + " Position: " + (iAction/2));

        //here we add a specific tile (according to 'action', which was selected before randomly, 
        //see setNextNondeterministicAction())
        addTile(emptyTiles.get(iAction/2), (iAction%2)+1);

        updateEmptyTiles();
        updateAvailableMoves();
        isNextActionDeterministic = true;
        nextNondeterministicAction = null;
    }
    
    public void advanceNondeterministic() {
        setNextNondeterministicAction();
        advanceNondeterministic(nextNondeterministicAction);
    }

    /**
     * Select a random next nondeterministic action: Select an empty tile and the new value of the tile
     * and translate this into an action:<br>
     * 0 = first tile, value 2 <br>
     * 1 = first tile, value 4 <br>
     * 2 = second tile, value 2 <br>
     * 3 = second tile, value 4
     * ....
     */
    private void setNextNondeterministicAction() {
        if(isNextActionDeterministic) {
            throw new RuntimeException("next Action is Deterministic");
        } else if(nextNondeterministicAction != null) {
            return;
        }

        //select a tile
        int action = random.nextInt(emptyTiles.size()) * 2;

        //select the new tile value:
        // 90% no change in action --> tile value 2,
        // 10% (if nextInt(10)==9) incrementing action by 1 --> tile value 4
        if(random.nextInt(10) == 9) {
            action += 1;
        }
        
        nextNondeterministicAction = ACTIONS.fromInt(action);
    }

    public double getProbability(ACTIONS action) {
        int iAction = action.toInt();
        double prob = (iAction%2==0) ? 0.9 : 0.1;
        return prob/emptyTiles.size();
    }
    
    public boolean isNextActionDeterministic() {
        return isNextActionDeterministic;
    }

    public ACTIONS getNextNondeterministicAction() {
        setNextNondeterministicAction();

        return nextNondeterministicAction;
    }

    @Override
    public StateObservation precedingAfterstate() {
    	// for 2048, the preceding afterstate is not known
    	return null;
    }
    
    public int getPlayer() {
        return 0;
    }

    public int getNumPlayers() {
        return 1;
    }

    public String stringDescr() {
        return String.format("%016x", boardB);  // format as 16-hex-digit number with leading 0's (if necessary)
        //return Long.toHexString(boardB);            // no leading zeros
    }

	/**
	 * 
	 * @return a string representation of actions {@code act}
	 */
	public String stringActionDescr(ACTIONS act) {
		if (act==null) return "";
		return ACTIONSTRING[act.toInt()];
	}

	public String toString() {
        return stringDescr();
    }

	public boolean isDeterministicGame() {
		return false;
	}
	
    @Override
	public boolean isFinalRewardGame() {
		return false;
	}

    public boolean isLegalAction(ACTIONS action) {
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

    /**
     * @return the cumulated number of empty tiles
     */
    public long getCumulEmptyTiles() {
        return cumulEmptyTiles;
    }

    public int getTileValue(int pos) {
        long val = (boardB >> (15-pos)*4 & 0x0fL);
        int value = (int)Math.pow(2, val);
        if(value == 1) {
            return 0;
        } else {
            return value;
        }
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
        boolean succ = emptyTiles.remove(Integer.valueOf(position));
        assert succ : "Something wrong in emptyTiles removal";
        //System.out.println(Long.toHexString(boardB));
        long b = (boardB >> (4*position)) & 0x0fL;
        assert (b==0) : "boardB is not empty at 'position'";
        long v = (long)value << (4*position);
        boardB = boardB + v;
        //System.out.println(Long.toHexString(v));
        updateHighestTile(value);
    }

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
    // updateEmptyTiles is package-visible (for StateObserver2048Slow::assertSameAdvance)
    void updateEmptyTiles() {
        emptyTiles.clear();
        long b = boardB;
        for (int j=0; j<16; j++) {
            if ((b & 0x0fL)==0) emptyTiles.add(j);
            b = b >> 4;                // shift down by one hex digit
        }
        cumulEmptyTiles += emptyTiles.size() - 1;  
        // "-1" because a random tile will be added from the environment
    }

    /**
     *
     * @param winState -1: lost, 0: running, 1: won
     */
    private void setWinState(int winState) {
        //if(this.winState == 0) {
            this.winState = winState;
        //}
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

    // updateAvailableMoves is package-visible (for StateObserver2048Slow::assertSameAdvance)
    /**
	 * Update class member {@code List<Integer> availableMoves}:<br>
	 * The possible actions are: 0: left, 1: up, 2: right, 3: down.
     */
    void updateAvailableMoves() {
        availableMoves.clear();
        int oldScore = score;
        long oldCumulEmptyTiles = cumulEmptyTiles;
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
        cumulEmptyTiles = oldCumulEmptyTiles;
        score=oldScore;

        if(availableMoves.size() <= 0) {
            setWinState(-1);
            if (highestTileValue >= ConfigGame.WINNINGVALUE) setWinState(+1);
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
            int position = emptyTiles.get(random.nextInt(emptyTiles.size()));
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
//      moves++;
        super.incrementMoveCounter();

        for(int i = 0; i < 16; i++) {
            updateHighestTile((int)(boardB >> (15-i)*4 & 0x0fL));
        }
    }

    private void newBoard() {
        boardB = 0x0L;
        score = 0;
        winState = 0;
        isNextActionDeterministic = true;

        updateEmptyTiles(); // fixed Empty Tiles JK
        cumulEmptyTiles = 0L;

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

    /**
     * equals currently only tests if the gameboards are equal
     */
    @Override
    public boolean equals(Object arg0) {
        StateObserver2048 aItem;
        if (arg0 instanceof StateObserver2048) {
            aItem = (StateObserver2048) arg0;
        } else {
        	throw new RuntimeException("so is not a StateObserver2048 object!");
        }
        return (this.boardB == aItem.boardB);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(boardB);
    }
    
	public int getHighestTileValue() {
		return this.highestTileValue;
	}
	
} // class StateObserver2048



