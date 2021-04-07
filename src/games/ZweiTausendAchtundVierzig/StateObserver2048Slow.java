package games.ZweiTausendAchtundVierzig;

import games.StateObservation;
import games.ObserverBase;
import games.StateObsNondeterministic;
import tools.Types;
import tools.Types.ACTIONS;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class <strong> StateObs2048Slow </strong> holds a 2048 game state.
 * The game state is coded in a matrix of tiles {@code Tile[][] gameBoard}. The code for 
 * merging tiles is better understandable, but also slower than in {@link StateObserver2048}.<p>
 * <p>
 * This is just an alternate class for {@link StateObserver2048} (for testing purposes). It is only used if
 * class {@link StateObserver2048} is renamed to {@code StateObserverNEW} and then
 * class {@link StateObserver2048Slow} is renamed to {@code StateObserver}.
 * 
 * @author Johannes Kutsch, 2016
 */
public class StateObserver2048Slow extends ObserverBase implements StateObsNondeterministic {
    private final Random random = new Random();
    protected List<Tile> emptyTiles = new ArrayList<>();
    protected List<Integer> availableMoves;
    protected List<Integer> availableRandoms = new ArrayList<>();
    private Tile[][] gameBoard;
	protected ACTIONS[] actions;

    // 0 = running, 1 = won, -1 = lost
    private int winState;
    public int score;
    public int highestTileValue = Integer.MIN_VALUE;
//    public boolean highestTileInCorner = false;
//    public int rowLength = 0;
//    public int rowValue = 0;
//    public int mergeValue = 0;
    public int moves = 0;
    private final boolean ASSERTSAME = false; // if true, run through assertSameAdvance
    private boolean isNextActionDeterministic;

    // --- this is now in ObserverBase ---
//  public ACTIONS[] storedActions = null;
//  public ACTIONS storedActBest = null;
//  public double[] storedValues = null;
//  private double storedMaxScore;
  
    private ACTIONS nextNondeterministicAction;

    public final static double MAXSCORE = 3932156;
    private static final double REWARD_NEGATIVE = -1.0;
    private static final double REWARD_POSITIVE =  1.0;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	@Serial
    private static final long serialVersionUID = 12L;

	// never used
//  public StateObserver2048Slow() {
//      newBoard();
//  }

    public StateObserver2048Slow(int[][] values, int score, int winState) {
        gameBoard = new Tile[ConfigGame.ROWS][ConfigGame.COLUMNS];
        for(int row = 0; row < ConfigGame.ROWS; row++) {
            for(int column = 0; column < ConfigGame.COLUMNS; column++) {
                Tile newTile = new Tile(values[row][column], new Position(row,column));
                gameBoard[row][column] = newTile;
                updateHighestTile(newTile);
                if(values[row][column] == 0) {
                    emptyTiles.add(newTile);
                }
            }
        }
        this.score = score;
        this.winState = winState;

        updateAvailableMoves();
    }

    // Note: StateObserver2048Slow.copy() copies the board state, score, winState,
    // but it does NOT copy storedActions, storedActBest, storedValues, storedMaxScore.
    @Override
    public StateObserver2048Slow copy() {
    	StateObserver2048Slow so2 = new StateObserver2048Slow(toArray(), score, winState);
    	so2.m_counter = this.m_counter;
    	return so2;
    }

    /**
     * Only debug check (if ASSERTSAME==true):
     * Assert that StateObserver2048Slow and StateObs2048 (fast, with bit shift) result in
     * <ul>
     * <li> the same state when doing a move with iAction on {@code this} (no random tile added)
     * <li> the same score
     * <li> the same list of available moves
     * <li> the same empty tile positions (note that the lists {@code emptyTiles} contain different
     * 		things in both classes)
     * </ul>
     * @param iAction the action to advance
     * @return {@code true} if all assertions are passed, {@code false} else
     */
    private boolean assertSameAdvance(int iAction) {
    	StateObserver2048Slow sot = this.copy();
    	int[][] iArray = toArray();
    	StateObserver2048 sbs = new StateObserver2048(iArray, score, winState,true);
    	//System.out.println("sot: "+sot.toHexString()+",  score="+sot.getScore());
    	//System.out.println("sbs: "+sbs.stringDescr()+",  score="+sbs.getScore());
    	sot.move(iAction);
    	sot.updateAvailableMoves();
    	sbs.move(iAction);
    	sbs.updateEmptyTiles();
    	sbs.updateAvailableMoves();
    	String s_sot = sot.toHexString();
    	String s_sbs = sbs.stringDescr();
    	System.out.println("sot: "+s_sot+",  score="+sot.getScore());
    	System.out.println("sbs: "+s_sbs+",  score="+sbs.getScore());
    	if (!s_sot.equals(s_sbs)) {
    		return false;
    	}
    	if (sot.getScore()!=sbs.getScore()) {
    		return false;
    	}
    	for (Integer iVal : sot.availableMoves) {
    		if (!sbs.availableMoves.contains(iVal)) {
    			return false;
    		}
    	}
    	for (Tile tile : sot.emptyTiles) {
    		// note that the numbering of tile positions is different:
    		// in StateObserver2048Slow row 0 is the highest row and column 0 is the leftmost column;
    		// in StateObs2048BitShift row 0 is the lowest row and column 0 is the rightmost column.
    		// Therefore we add two times a "(3-...)" for comparison.
    		Position pos = tile.getPosition();
    		Integer iVal = (3-pos.getColumn()) + 4*(3-pos.getRow());
    		if (!sbs.emptyTiles.contains(iVal)) {
    			return false;
    		}
    	}
    	return true;
    }

    @Override
    public boolean isGameOver() {
        return availableMoves.size() == 0;
    }

    @Override
    public boolean isLegalState() {
        //Sollte eigentlich nicht benötigt werden, solange das Spielfeld nur über advance() verändert wird.
        //Ich überprüfe den State vorerst nicht, da die Überprüfung nicht Notwendig sein sollte und das Programm nur verlangsamen würde.
        return true;
    }

//    @Override
//    public Types.WINNER getGameWinner() {
//        assert isGameOver() : "Game is not yet over!";
//        switch (winState) {
//            case 1:
//                return Types.WINNER.PLAYER_WINS;
//            default:
//                return Types.WINNER.PLAYER_LOSES;
//        }
//    }

    // never used
//    public double getGameScore1() {
//        if (isGameOver()) {
//            double penalisation = ConfigGame.PENALISATION;
//
//            if(ConfigGame.ADDSCORE) {
//                penalisation += (score / MAXSCORE);
//            }
//            return penalisation;
//        }
//        else {
//            double realScore = score;
//
//            //Row Heuristik
//            evaluateBoard();
//            realScore += rowValue * ConfigGame.ROWMULTIPLIER;
//
//            //Highest Tile In Corner Heuristik
//            if (highestTileInCorner) {
//                //realScore *= ConfigGame.HIGHESTTILEINCORENERMULTIPLIER+1;
//                realScore += highestTileValue * ConfigGame.HIGHESTTILEINCORENERMULTIPLIER;
//            }
//
//            //Empty Tiles Heuristik
//            //realScore *= Math.pow(ConfigGame.EMPTYTILEMULTIPLIER+1, emptyTiles.size());
//            //realScore += highestTileValue*emptyTiles.size()*(ConfigGame.EMPTYTILEMULTIPLIER);
//            realScore += score * emptyTiles.size() * ConfigGame.EMPTYTILEMULTIPLIER;
//
//            //Merge Heuristik
//            realScore += mergeValue * ConfigGame.MERGEMULTIPLIER;
//
//
//            if (realScore == 0) {
//                return 0;
//            } else {
//                realScore /= MAXSCORE;
//                return realScore;
//            }
//        }
//    }

    // never used
//   private void evaluateBoard() {
//        RowInformationContainer rowInformationContainer;
//        highestTileInCorner = false;
//        rowValue = 0;
//        rowLength = 0;
//        mergeValue = 0;
//
//        if (gameBoard[0][0].getValue() == highestTileValue) {
//            highestTileInCorner = true;
//
//            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 0, 1, 2);
//            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
//                rowValue = rowInformationContainer.rowValue;
//            }
//
//            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 0, 1, 3);
//            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
//                rowValue = rowInformationContainer.rowValue;
//            }
//        }
//
//        if (gameBoard[ConfigGame.ROWS - 1][0].getValue() == highestTileValue) {
//            highestTileInCorner = true;
//
//            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 0, 1, 2);
//            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
//                rowValue = rowInformationContainer.rowValue;
//            }
//
//            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 0, 1, 1);
//            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
//                rowValue = rowInformationContainer.rowValue;
//            }
//        }
//
//        if (gameBoard[0][ConfigGame.COLUMNS - 1].getValue() == highestTileValue) {
//            highestTileInCorner = true;
//
//            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 0, 1, 0);
//            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
//                rowValue = rowInformationContainer.rowValue;
//            }
//
//            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 0, 1, 3);
//            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
//                rowValue = rowInformationContainer.rowValue;
//            }
//        }
//
//        if (gameBoard[ConfigGame.ROWS - 1][ConfigGame.COLUMNS - 1].getValue() == highestTileValue) {
//            highestTileInCorner = true;
//
//            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 0, 1, 0);
//            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
//                rowValue = rowInformationContainer.rowValue;
//            }
//
//            rowInformationContainer = evaluateRow(highestTileValue, 0, 0, 0, 1, 1);
//            if (rowInformationContainer.rowValue > rowValue) {
//                rowLength = rowInformationContainer.rowLength;
//                rowValue = rowInformationContainer.rowValue;
//            }
//        }
//
//        for(int row = 0; row < ConfigGame.ROWS-1; row++) {
//            for (int column = 0; column < ConfigGame.COLUMNS; column++) {
//                int currentValue = gameBoard[row][column].getValue();
//                if(currentValue != 0) {
//                    for (int position = row+1; position < ConfigGame.ROWS; position++) {
//                        int newValue = gameBoard[position][column].getValue();
//                        if(newValue != 0) {
//                            if (currentValue == newValue) {
//                                mergeValue += currentValue;
//                            }
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//
//        for(int row = 0; row < ConfigGame.ROWS; row++) {
//            for (int column = 0; column < ConfigGame.COLUMNS-1; column++) {
//                int currentValue = gameBoard[row][column].getValue();
//                if(currentValue != 0) {
//                    for (int position = column+1; position < ConfigGame.COLUMNS; position++) {
//                        int newValue = gameBoard[row][position].getValue();
//                        if(newValue != 0) {
//                            if (currentValue == newValue) {
//                                mergeValue += currentValue;
//                            }
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//    }

    private RowInformationContainer evaluateRow(int currentTileValue, int currentRowLength, int currentRowValue, int position, int offset, int direction) {
        switch (direction) {
            case 0:
                //left
                for (int i = ConfigGame.COLUMNS-1-offset; i >= 0; i--) {
                    if (gameBoard[position][i].getValue() != 0 && gameBoard[position][i].getValue() < currentTileValue /*gameBoard[position][i].getValue() == currentTileValue/2*/) {
                        currentRowLength++;
                        currentTileValue = gameBoard[position][i].getValue();
                        currentRowValue += currentTileValue;
                    } else {
                        return new RowInformationContainer(currentRowLength, currentRowValue);
                    }
                }

                return evaluateRow(currentTileValue, currentRowLength, currentRowValue, position+1, 0, 2);

            case 1:
                //up
                for (int i = ConfigGame.ROWS-1-offset; i >= 0; i--) {
                    if (gameBoard[i][position].getValue() != 0 && gameBoard[i][position].getValue() < currentTileValue /*gameBoard[i][position].getValue() == currentTileValue/2*/) {
                        currentRowLength++;
                        currentTileValue = gameBoard[i][position].getValue();
                        currentRowValue += currentTileValue;
                    } else {
                        return new RowInformationContainer(currentRowLength, currentRowValue);
                    }
                }

                return evaluateRow(currentTileValue, currentRowLength, currentRowValue, position+1, 0, 3);

            case 2:
                //right
                for (int i = 0+offset; i < ConfigGame.COLUMNS; i++) {
                    if (gameBoard[position][i].getValue() != 0 && gameBoard[position][i].getValue() < currentTileValue /*gameBoard[position][i].getValue() == currentTileValue/2*/) {
                        currentRowLength++;
                        currentTileValue = gameBoard[position][i].getValue();
                        currentRowValue += currentTileValue;
                    } else {
                        return new RowInformationContainer(currentRowLength, currentRowValue);
                    }
                }
                return evaluateRow(currentTileValue, currentRowLength, currentRowValue, position+1, 0, 0);

            case 3:
                //down
                for (int i = 0+offset; i < ConfigGame.ROWS; i++) {
                    if (gameBoard[i][position].getValue() != 0 && gameBoard[i][position].getValue() < currentTileValue /*gameBoard[i][position].getValue() == currentTileValue/2*/) {
                        currentRowLength++;
                        currentTileValue = gameBoard[i][position].getValue();
                        currentRowValue += currentTileValue;
                    } else {
                        return new RowInformationContainer(currentRowLength, currentRowValue);
                    }
                }
                return evaluateRow(currentTileValue, currentRowLength, currentRowValue, position+1, 0, 1);
        }

        return null;
    }

    @Override
    public double getGameScore(StateObservation referingState) {
        return score / MAXSCORE;
        // OLD and wrong (endless recursion?):
//        assert (referingState instanceof StateObserver2048Slow) : "referingState is not of class StateObserver2048Slow";
//        return this.getGameScore(this);
    }

	/**
	 * The cumulative reward relative to referringState. 
	 * @param referringState
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different, 
	 * 		  game-specific reward
	 * @return  the cumulative reward 
	 */
    @Override
	public double getReward(StateObservation referringState, boolean rewardIsGameScore) {
		return getGameScore(referringState);
	}

	/**
	 * Same as getReward(referringState), but with the player of referringState. 
	 * @param player the player of referringState, a number in 0,1,...,N.
	 * @param rewardIsGameScore if true, use game score as reward; if false, use a different, 
	 * 		  game-specific reward
	 * @return  the cumulative reward 
	 */
	public double getReward(int player, boolean rewardIsGameScore) {
        return this.getGameScore(this);
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
    public String getName() { return "ZweiTausendAchtundVierzig"; }

	/**
	 * The board vector is an {@code int[]} vector where each entry corresponds to one
	 * cell of the board. In the case of 2048 the mapping is
	 * <pre>
	 *    00 01 02 03
	 *    04 05 06 07
	 *    08 09 10 11
	 *    12 13 14 15
	 * </pre>
	 * @return a vector of length {@link games.XNTupleFuncs#getNumCells()}, holding for each board cell its
	 * position value 0:empty, 1: tile 2^1, 2: tile 2^2,..., P-1: tile 2^(P-1).
	 */
	public int[] getBoardVector() {
		int[] bvec = new int[ConfigGame.ROWS* ConfigGame.COLUMNS];
		int b2,k;
		for(int row = 0, n = 0; row < ConfigGame.ROWS; row++) {
            for(int column = 0; column < ConfigGame.COLUMNS; column++,n++) {
            	b2 = gameBoard[row][column].getValue();
            	for (k=0; k<16; k++) {
            		// find the exponent k in 2^k by down-shifting:
                    b2 = b2>>1;
            		if (b2==0) break;
            	}
            	bvec[n]=k;
            }
        }
		return bvec;
	}

    @Override
    public void advance(ACTIONS action) {
        int iAction = action.toInt();
        assert (availableMoves.contains(iAction)) : "iAction is not viable.";
        if (ASSERTSAME) {
        	boolean succ = assertSameAdvance(iAction);
        	if (succ==false) throw new RuntimeException("assertSameAdvance failed!");
        }
        move(iAction);
        addRandomTile();
        updateAvailableMoves();
    }

    @Override
	public ArrayList<ACTIONS> getAllAvailableActions() {
        ArrayList allActions = new ArrayList<>();
        for (int j = 0; j < 4; j++) 
        	allActions.add(Types.ACTIONS.fromInt(j));
        
        return allActions;
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
    public int getNumAvailableActions() {
        return availableMoves.size();
    }

	public ArrayList<ACTIONS> getAvailableRandoms() {
        ArrayList<ACTIONS> availRan = new ArrayList<>();
        for(int viableMove : availableRandoms) {
            availRan.add(ACTIONS.fromInt(viableMove));
        }
        return availRan;
		
	}

	public int getNumAvailableRandoms() {
		return availableRandoms.size();
	}

    @Override
    public void setAvailableActions() {
        ArrayList<ACTIONS> acts = this.getAvailableActions();
        actions = new ACTIONS[acts.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = acts.get(i);
        }
    }

    @Override
    public ACTIONS getAction(int i) {
        return actions[i];
    }

    public void advanceDeterministic(ACTIONS action) {
        if(!isNextActionDeterministic) {
            throw new RuntimeException("Next action is nondeterministic but called advanceDeterministic()");
        }

        int iAction = action.toInt();
        assert (availableMoves.contains(iAction)) : "iAction is not viable.";
        move(iAction);
        //updateEmptyTiles();

        isNextActionDeterministic = false;
		super.incrementMoveCounter();
   }

    public void advanceNondeterministic(ACTIONS randAction) {
        if(isNextActionDeterministic) {
            throw new RuntimeException("Next action is deterministic but called advanceNondeterministic()");
        }

        int iAction = randAction.toInt();
        assert (emptyTiles.size() * 2 > iAction) : "iAction is not viable.";

        //System.out.println("Action: " + iAction + " Value: " + ((iAction%2)+1) + " Position: " + (iAction/2));

        addTile(emptyTiles.get(iAction/2), (iAction%2)+1);

        updateAvailableMoves();
        isNextActionDeterministic = true;
        nextNondeterministicAction = null;
		super.incrementMoveCounter();
   	
    }
    
    public void advanceNondeterministic() {
        setNextNondeterministicAction();
        advanceNondeterministic(nextNondeterministicAction);
    }

    /**
     * Selects a Tile and the new value of the tile and saves it in an action
     * 0 = first Tile, value 2
     * 1 = first Tile, value 4
     * 2 = second Tile, value 2
     * 3 = second Tile, value 4
     * ....
     */
    private void setNextNondeterministicAction() {
        if(isNextActionDeterministic) {
            throw new RuntimeException("next Action is Deterministic");
        } else if(nextNondeterministicAction != null) {
            return;
        }


        //select a Tile
        int action = random.nextInt(emptyTiles.size()) * 2;

        //select the new Tile Value
        if(random.nextInt(10) == 9) {
            action += 1;
        }

        nextNondeterministicAction = ACTIONS.fromInt(action);
    }

    public double getProbability(ACTIONS action) {
        int iAction = action.toInt();
        int numEmptyTiles = iAction/2;
        double prob = (iAction%2==0) ? 0.9 : 0.1;
        return prob/numEmptyTiles;
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

    // --- this is now in ObserverBase ---
//    @Override
//    public void storeBestActionInfo(ACTIONS actBest, double[] vtable) {
//        ArrayList<ACTIONS> acts = this.getAvailableActions();
//        storedActions = new ACTIONS[acts.size()];
//        storedValues = new double[acts.size()];
//        for(int i = 0; i < storedActions.length; ++i)
//        {
//            storedActions[i] = acts.get(i);
//            storedValues[i] = vtable[i];
//        }
//        storedActBest = actBest;
//        if (actBest instanceof ACTIONS_VT) {
//        	storedMaxScore = ((ACTIONS_VT) actBest).getVBest();
//        } else {
//            storedMaxScore = vtable[acts.size()];        	
//        }
//    }

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
        String s="";
        int codeA = +'A';		// normally 65
        XNTupleFuncs2048 xnf = new XNTupleFuncs2048();
        int[] bvec = xnf.getBoardVector(this).bvec;
        for(int row = 0, n = 0; row < ConfigGame.ROWS; row++) {
            for(int column = 0; column < ConfigGame.COLUMNS; column++,n++) {
            	if (bvec[n]>=10) {
            		s = s+((char)(65-10+bvec[n]));
            	} else {
                    s = s+bvec[n];
            	}
            }
            if (row<(ConfigGame.ROWS-1)) s=s+",";
        }
        return s;
    }
	// -- possible code to test stringDescr --
	//		int[][] state = {{0,2048,0,0}, {0,0,0,0}, {0,2,4,0}, {0,0,0,0}};
	//		StateObserver2048Slow so = new StateObserver2048Slow(state,0,0);
	//		System.out.println(so.stringDescr());

    public String toHexString() {
        String s="";
        XNTupleFuncs2048 xnf = new XNTupleFuncs2048();
        int[] bvec = xnf.getBoardVector(this).bvec;
        for(int row = 0, n = 0; row < ConfigGame.ROWS; row++) {
            for(int column = 0; column < ConfigGame.COLUMNS; column++,n++) {
            	s = s+Integer.toHexString(bvec[n]);
            }
        }
        return s;
    }

    @Deprecated
    public String toString() {
    	return stringDescr();
    }

    @Override
	public boolean isDeterministicGame() {
		return false;
	}
	
    @Override
	public boolean isFinalRewardGame() {
		return true;
	}

//    @Override
//	public boolean has2OppositeRewards() {
//		return true;
//	}

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

    public Tile getTile(int row, int column) {
        return gameBoard[row][column];
    }

    public int getTileValue(int pos) {
        int row = pos/4;
        int column = pos%4;

        return getTile(row, column).getValue();
    }

    public Tile[][] getGameBoard() {
		return gameBoard;
	}


    public void addTile(Tile tile, int value) {
        emptyTiles.remove(tile);
        tile.setValue(value);
        updateHighestTile(tile);
    }

    public void mergeTiles(Tile tileOne, Tile tileTwo) {
        tileOne.addValue(tileTwo.getValue());
        score += tileOne.getValue();
        if(tileOne.getValue() >= ConfigGame.WINNINGVALUE) {
            setWinState(1);
        }
        updateHighestTile(tileOne);
        tileTwo.setValue(0);
        emptyTiles.add(tileTwo);
    }

    public void updateHighestTile(Tile newTile) {
        if(newTile.getValue() > highestTileValue) {
            highestTileValue = newTile.getValue();
        }
    }

    public void moveTile(Tile oldTile, Tile newTile) {
        emptyTiles.remove(newTile);
        newTile.setValue(oldTile.getValue());

        emptyTiles.add(oldTile);
        oldTile.setValue(0);
    }

    /**
     *
     * @param winState {@code -1: lost, 0: running, 1: won}
     */
    public void setWinState(int winState) {
        //if(this.winState == 0) {
            this.winState = winState;
        //}
    }

    public int[][] toArray() {
        int[][] newBoard = new int[ConfigGame.ROWS][ConfigGame.COLUMNS];
        for(int row = 0; row < ConfigGame.ROWS; row++) {
            for(int column = 0; column < ConfigGame.COLUMNS; column++) {
                newBoard[row][column] = gameBoard[row][column].getValue();
            }
        }
        return newBoard;
    }

    public void updateAvailableMoves() {
        availableMoves = new ArrayList<>();

        loop:
        for(int row = 0; row < ConfigGame.ROWS; row++) {
            for (int column = 1; column < ConfigGame.COLUMNS; column++) {
                if(gameBoard[row][column].getValue() != 0) {
                    if (gameBoard[row][column - 1].getValue() == 0 || gameBoard[row][column].getValue() == gameBoard[row][column - 1].getValue()) {
                        availableMoves.add(0);
                        break loop;
                    }
                }
            }
        }



        loop:
        for(int row = 1; row < ConfigGame.ROWS; row++) {
            for (int column = 0; column < ConfigGame.COLUMNS; column++) {
                if(gameBoard[row][column].getValue() != 0) {
                    if (gameBoard[row - 1][column].getValue() == 0 || gameBoard[row][column].getValue() == gameBoard[row - 1][column].getValue()) {
                        availableMoves.add(1);
                        break loop;
                    }
                }
            }
        }

        loop:
        for(int row = 0; row < ConfigGame.ROWS; row++) {
            for (int column = 0; column < ConfigGame.COLUMNS-1; column++) {
                if(gameBoard[row][column].getValue() != 0) {
                    if (gameBoard[row][column + 1].getValue() == 0 || gameBoard[row][column].getValue() == gameBoard[row][column + 1].getValue()) {
                        availableMoves.add(2);
                        break loop;
                    }
                }
            }
        }

        loop:
        for(int row = 0; row < ConfigGame.ROWS-1; row++) {
            for (int column = 0; column < ConfigGame.COLUMNS; column++) {
                if(gameBoard[row][column].getValue() != 0) {
                    if (gameBoard[row+1][column].getValue() == 0 || gameBoard[row][column].getValue() == gameBoard[row+1][column].getValue()) {
                        availableMoves.add(3);
                        break loop;
                    }
                }
            }
        }

        if(availableMoves.size() <= 0) {
            setWinState(-1);
            if (highestTileValue >= ConfigGame.WINNINGVALUE) setWinState(+1);
        }

        setAvailableActions();
    }

    private void updateAvailableRandoms() {
        availableRandoms.clear();
        for (int i=0; i<emptyTiles.size()*2; i++) availableRandoms.add(i);
    }
    
    public void printBoard() {
        System.out.println("---------------------------------");
        for(Tile[] row: gameBoard)
        {
            System.out.print("|");
            for(Tile tile: row) {
                if(tile.getValue() != 0) {
                    if(tile.getValue() < 10) {
                        System.out.print("   " + tile.getValue() + "   |");
                    }
                    else if(tile.getValue() < 100) {
                        System.out.print("  " + tile.getValue() + "   |");
                    }
                    else if(tile.getValue() < 1000) {
                        System.out.print("  " + tile.getValue() + "  |");
                    }
                    else if(tile.getValue() < 10000) {
                        System.out.print(" " + tile.getValue() + "  |");
                    }
                    else if(tile.getValue() < 100000) {
                        System.out.print(" " + tile.getValue() + " |");
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
            Tile tile = emptyTiles.get(random.nextInt(emptyTiles.size()));
            addTile(tile, ConfigGame.STARTINGVALUES[random.nextInt(ConfigGame.STARTINGVALUES.length)]);
        }
    }

    /**
     *
     * @param move {@literal 0 > left, 1 > up, 2 > right, 3 > down}
     */
    public void move(int move) {
        switch (move) {
            case 0:
                left();
                break;

            case 1:
                up();
                break;

            case 2:
                right();
                break;

            case 3:
                down();
                break;
        }
        moves++;
    }

    private void left() {
        if(availableMoves.contains(0)) {
            for (int row = 0; row < ConfigGame.ROWS; row++) {
                //Feld mit dem gemerged wird oder auf das geschoben wird
                Tile lastTile = getTile(row, 0);

                for (int column = 1; column < ConfigGame.COLUMNS; column++) {
                    Tile currentTile = getTile(row, column);

                    if(currentTile.getValue() != 0) {
                        if (currentTile.getValue() == lastTile.getValue()) {
                            //Es stehen zweimal die selben Zahlen hintereinander
                            mergeTiles(lastTile, currentTile);
                            lastTile = getTile(row, lastTile.getPosition().getColumn()+1);

                        } else if (currentTile.getValue() != 0 && lastTile.getValue() != 0) {
                            //Es stehen zwei verschiedene Zahlen hintereinander
                            lastTile = getTile(row, lastTile.getPosition().getColumn()+1);
                            if(currentTile != lastTile) {
                                moveTile(currentTile, lastTile);
                            }

                        } else if (currentTile.getValue() != 0) {
                            // Eine Zahl steht hinter einem leerem Feld
                            moveTile(currentTile, lastTile);
                        }
                    }
                }
            }
        }
        else {
            System.out.println("Invalid Move 0");
            System.out.println("availableMoves = " + availableMoves);
        }
    }

    private void up() {
        if(availableMoves.contains(1)) {
            for (int column = 0; column < ConfigGame.COLUMNS; column++) {
                Tile lastTile = getTile(0, column);

                for (int row = 1; row < ConfigGame.ROWS; row++) {
                    Tile currentTile = getTile(row, column);

                    if(currentTile.getValue() != 0) {
                        if (currentTile.getValue() == lastTile.getValue()) {
                            //Es stehen zweimal die selben Zahlen hintereinander
                            mergeTiles(lastTile, currentTile);
                            lastTile = getTile(lastTile.getPosition().getRow()+1, column);

                        } else if (currentTile.getValue() != 0 && lastTile.getValue() != 0) {
                            //Es stehen zwei verschiedene Zahlen hintereinander
                            lastTile = getTile(lastTile.getPosition().getRow()+1, column);
                            if(currentTile != lastTile) {
                                moveTile(currentTile, lastTile);
                            }

                        } else if (currentTile.getValue() != 0) {
                            // Eine Zahl steht hinter einem leerem Feld
                            moveTile(currentTile, lastTile);
                        }
                    }
                }
            }
        }
        else {
            System.out.println("Invalid Move 1");
            System.out.println("availableMoves = " + availableMoves);
        }
    }

    private void right() {
        if(availableMoves.contains(2)) {
            for (int row = 0; row < ConfigGame.ROWS; row++) {
                Tile lastTile = getTile(row, ConfigGame.COLUMNS - 1);

                for (int column = ConfigGame.COLUMNS - 2; column >= 0; column--) {
                    Tile currentTile = getTile(row, column);

                    if(currentTile.getValue() != 0) {
                        if (currentTile.getValue() == lastTile.getValue()) {
                            //Es stehen zweimal die selben Zahlen hintereinander
                            mergeTiles(lastTile, currentTile);
                            lastTile = getTile(row, lastTile.getPosition().getColumn()-1);

                        } else if (currentTile.getValue() != 0 && lastTile.getValue() != 0) {
                            //Es stehen zwei verschiedene Zahlen hintereinander
                            lastTile = getTile(row, lastTile.getPosition().getColumn()-1);
                            if(currentTile != lastTile) {
                                moveTile(currentTile, lastTile);
                            }

                        } else if (currentTile.getValue() != 0) {
                            // Eine Zahl steht hinter einem leerem Feld
                            moveTile(currentTile, lastTile);
                        }
                    }
                }
            }
        }
        else {
            System.out.println("Invalid Move 2");
            System.out.println("availableMoves = " + availableMoves);
        }
    }

    private void down() {
        if(availableMoves.contains(3)) {
            for (int column = 0; column < ConfigGame.COLUMNS; column++) {
                Tile lastTile = getTile(ConfigGame.ROWS - 1, column);

                for (int row = ConfigGame.ROWS - 2; row >= 0; row--) {
                    Tile currentTile = getTile(row, column);

                    if(currentTile.getValue() != 0) {
                        if (currentTile.getValue() == lastTile.getValue()) {
                            //Es stehen zweimal die selben Zahlen hintereinander
                            mergeTiles(lastTile, currentTile);
                            lastTile = getTile(lastTile.getPosition().getRow()-1, column);

                        } else if (currentTile.getValue() != 0 && lastTile.getValue() != 0) {
                            //Es stehen zwei verschiedene Zahlen hintereinander
                            lastTile = getTile(lastTile.getPosition().getRow()-1, column);
                            if(currentTile != lastTile) {
                                moveTile(currentTile, lastTile);
                            }

                        } else if (currentTile.getValue() != 0) {
                            // Eine Zahl steht hinter einem leerem Feld
                            moveTile(currentTile, lastTile);
                        }
                    }
                }
            }
        }
        else {
            System.out.println("Invalid Move 3");
            System.out.println("availableMoves = " + availableMoves);
        }
    }

    private void newBoard() {
        gameBoard = new Tile[ConfigGame.ROWS][ConfigGame.COLUMNS];
        for(int row = 0; row < ConfigGame.ROWS; row++) {
            for(int column = 0; column < ConfigGame.COLUMNS; column++) {
                Tile newTile = new Tile(0, new Position(row,column));
                gameBoard[row][column] = newTile;
                emptyTiles.add(newTile);
            }
        }
        score = 0;
        winState = 0;

        for(int i = ConfigGame.STARTINGFIELDS; i > 0; i--) {
            addRandomTile();
        }

        updateAvailableMoves();
    }
    
    // --- take the default implementation from ObserverBase ---
//	/**
//	 * Same as {@link #getGameScore(StateObservation referringState)}, but with the player of referringState. 
//	 * @param player the player of referringState, a number in 0,1,...,N.
//	 * @return  the game score
//	 */
//	public double getGameScore(int player) {
//		return getGameScore();
//	}
	
    // --- take the default implementation from ObserverBase ---
//	/**
//	 * @return	a score tuple which has as {@code i}th value  {@link #getGameScore(int i)}
//	 */
//	public ScoreTuple getGameScoreTuple() {
//		ScoreTuple sc = new ScoreTuple(1);
//		sc.scTup[0] = this.getGameScore();
//		return sc;
//	}

} // class StateObserver2048Slow

