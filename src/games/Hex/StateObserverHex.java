package games.Hex;

import games.ObserverBase;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;

import java.util.ArrayList;

import static games.Hex.HexConfig.*;

/**
 * Class StateObserverHex represents states in Hex.
 * <p>
 * The board cells are numbered starting from the west corner going down to the south corner, which constitutes
 * the first row. The next row is the row above. I. e. an 4x4 Hex board has its tiles numbered 
 * as follows:
 * <pre>
 *                   12
 *                08    13
 *             04    09    14
 *          00    05    10    15
 *             01    06    11
 *                02    07
 *                   03
 * </pre>
 * The HexTile[][] array has the following numbering:
 * <pre>
 *                  [3,0]
 *               [2,0] [3,1]
 *            [1,0] [2,1] [3,2]
 *         [0,0] [1,1] [2,2] [3,3]
 *            [0,1] [1,2] [2,3]
 *               [0,2] [1,3]
 *                  [0,3]
 * </pre>
 * Player 0 (Black) has the first move. The white borders are below the first and above the 
 * last row. The black borders are along the columns.
 * 
 */
public class StateObserverHex extends ObserverBase implements StateObservation {
    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .gamelog or .agt.zip containing this object will
     * become unreadable or you have to provide a special version transformation)
     */
    private static final long serialVersionUID = 12L;
    private int currentPlayer;
    private HexTile[][] board;
    private HexTile lastUpdatedTile;
    private ArrayList<Types.ACTIONS> availableActions;

    public StateObserverHex() {
        board = defaultGameBoard();
        currentPlayer = HexConfig.PLAYER_ONE;
        setAvailableActions();
    }

    // never used
//  public StateObserverHex(HexTile[][] table) {
//        int pieceCount = 0;
//        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
//            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
//                pieceCount += (table[i][j].getPlayer() == HexConfig.PLAYER_ONE ? 1 : -1);
//            }
//        }
//        currentPlayer = (pieceCount % 2 == 0 ? HexConfig.PLAYER_ONE : PLAYER_TWO);
//        board = new HexTile[HexConfig.BOARD_SIZE][HexConfig.BOARD_SIZE];
//        copyTable(table);
//        setAvailableActions();
//  }

//  public StateObserverHex(HexTile[][] table, int player, HexTile lastUpdatedTile) {
//        board = new HexTile[HexConfig.BOARD_SIZE][HexConfig.BOARD_SIZE];
//        copyTable(table);
//        currentPlayer = player;
//        this.lastUpdatedTile = lastUpdatedTile;
//        setAvailableActions();
//  }
    
    public StateObserverHex(StateObserverHex other) {	
    	super(other);
        board = new HexTile[HexConfig.BOARD_SIZE][HexConfig.BOARD_SIZE];
        copyTable(other.board);
        this.currentPlayer =other.currentPlayer;
        this.lastUpdatedTile = other.lastUpdatedTile;
		if (other.availableActions!=null)	// this check is needed when loading older logs
			this.availableActions = (ArrayList<ACTIONS>) other.availableActions.clone();
					// note that clone does only clone the ArrayList, but not the contained ACTIONS, they are 
					// just copied by reference. However, as far as we see, the ACTIONS are never altered, so 
					// it should be o.k.
//		this.setAvailableActions();		// this as replacement for clone() would be slower
    }

    @Override
    public StateObserverHex copy() {
//    	StateObserverHex soh = new StateObserverHex(board, currentPlayer, lastUpdatedTile);
//    	soh.m_counter = this.m_counter;
//      return soh;
    	return new StateObserverHex(this);
    }

    /**
     * Replaces the current game board array by a copy of the array that is passed as the parameter.
     *
     * @param table The game board array that is to be copied
     */
    private void copyTable(HexTile[][] table) {
        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                board[i][j] = table[i][j].copy();
            }
        }
    }

    /**
     * @return An empty board array
     */
    private HexTile[][] defaultGameBoard() {
        HexTile[][] newBoard = new HexTile[HexConfig.BOARD_SIZE][HexConfig.BOARD_SIZE];

        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                newBoard[i][j] = new HexTile(i, j);
                newBoard[i][j].setPoly(HexUtils.createHexPoly(i, j, HexConfig.OFFSET, HexConfig.BOARD_SIZE, HexConfig.HEX_SIZE));
            }
        }

        return newBoard;
    }

    @Override
    public boolean isGameOver() {
        return determineWinner() != PLAYER_NONE || getNumAvailableActions() == 0;
    }

//    @Override
//    public Types.WINNER getGameWinner() {
//        return (determineWinner() == currentPlayer ? Types.WINNER.PLAYER_LOSES : Types.WINNER.PLAYER_WINS);
//    }

    /**
     * Uses information about the tile on which the last stone has been placed to determine if the chain containing
     * that stone touches both game board edges that have to be connected. Actual calculation is done in HexUtils.
     *
     * @return ID of the player who won the game. ID of HexConfig.PLAYER_NONE if game is not over.
     */
    private int determineWinner() {
        Types.WINNER winner = HexUtils.getWinner(getBoard(), getLastUpdatedTile());
        if (winner == Types.WINNER.PLAYER_WINS) {
            //Reverse winners, since current player changes after the winning tile was placed
            return (this.getPlayer() == PLAYER_ONE ? PLAYER_ONE : PLAYER_TWO);
        }
        return PLAYER_NONE;
    }

    @Override
	public boolean isDeterministicGame() {
		return true;
	}

    @Override
	public boolean isFinalRewardGame() {
		return true;
	}

    @Override
    public boolean isLegalState() {
        int playerOneTiles = 0;
        int playerTwoTiles = 0;

        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                if (board[i][j].getPlayer() == PLAYER_ONE) {
                    playerOneTiles++;
                } else if (board[i][j].getPlayer() == PLAYER_TWO) {
                    playerTwoTiles++;
                }
            }
        }

        if (currentPlayer == PLAYER_ONE) {
            return (playerOneTiles == playerTwoTiles);
        } else {
            return (playerOneTiles - 1 == playerTwoTiles);
        }
    }

    @Override
    public String stringDescr() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
//            for (int k = 0; k < i; k++) {
//                sb.append(' ');
//            }
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                switch (board[i][j].getPlayer()) {
                    case HexConfig.PLAYER_ONE:
                        sb.append('B');
                        break;
                    case PLAYER_TWO:
                        sb.append('W');
                        break;
                    default:
                        sb.append('-');
                }
            }
//            sb.append("\n");
            sb.append("/");

        }
        return sb.toString();
    }

    @Override
	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For Hex only game-over states have a non-zero game score. 
	 * 			It is the reward from the perspective of {@code refer}.
	 */
    public double getGameScore(StateObservation refer) {
		int sign = (refer.getPlayer()==this.getPlayer()) ? 1 : (-1);
        int winner = determineWinner();
        if (winner == PLAYER_NONE) {
            return 0;
        }

        return sign*(winner == currentPlayer ? REWARD_NEGATIVE : REWARD_POSITIVE);
    }

    @Override
    public double getMinGameScore() {
        return HexConfig.REWARD_NEGATIVE;
    }

    @Override
    public double getMaxGameScore() {
        return HexConfig.REWARD_POSITIVE;
    }

    @Override
    public String getName() {
        return "Hex";
    }

    @Override
    public void advance(Types.ACTIONS action) {
        if (action == null) {
            System.out.println("HEX ERROR: null given as action in advance()");
            return;
        }
        int actionInt = action.toInt();
        assert (0 <= actionInt && actionInt <= (HexConfig.TILE_COUNT)) : "Invalid action: " + actionInt;
        int j = actionInt % HexConfig.BOARD_SIZE;
        int i = (actionInt - j) / HexConfig.BOARD_SIZE;

        if (board[i][j].getPlayer() != HexConfig.PLAYER_NONE) {
            System.out.println("Tile (" + i + ", " + j + ") has already been claimed by a player.");
            return;
        }
        board[i][j].setPlayer(currentPlayer);

        lastUpdatedTile = board[i][j];
        setAvailableActions();            // IMPORTANT: adjust the available actions (have reduced by one)
		super.incrementMoveCounter();

        // set up player for next advance()
        currentPlayer = (currentPlayer == HexConfig.PLAYER_ONE ? PLAYER_TWO : HexConfig.PLAYER_ONE);
    }

    @Override
    public ArrayList<Types.ACTIONS> getAllAvailableActions() {
        ArrayList allActions = new ArrayList<>();
        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                    int actionInt = i * HexConfig.BOARD_SIZE + j;
                    allActions.add(Types.ACTIONS.fromInt(actionInt));
            }
        }
        return allActions;
    }
    
    @Override
    public ArrayList<Types.ACTIONS> getAvailableActions() {
        return availableActions;
    }
    
    /**
     * Return the afterstate preceding {@code this}. 
     */
    @Override
    public StateObservation getPrecedingAfterstate() {
    	// for deterministic games this state and the preceding afterstate are the same
    	return this;
    }

    @Override
    public int getNumAvailableActions() {
        return availableActions.size();
    }

    @Override
    public void setAvailableActions() {
        availableActions = new ArrayList<>();
        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                if (board[i][j].getPlayer() == HexConfig.PLAYER_NONE) {
                    int actionInt = i * HexConfig.BOARD_SIZE + j;
                    availableActions.add(Types.ACTIONS.fromInt(actionInt));
                }
            }
        }
    }

    @Override
    public Types.ACTIONS getAction(int i) {
        return availableActions.get(i);
    }

    @Override
    public void storeBestActionInfo(Types.ACTIONS bestAction, double[] valueTable) {
        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                board[i][j].setValue(Double.NaN);
            }
        }

        for (int k = 0; k < getNumAvailableActions(); ++k) {
            double val = valueTable[k];
            int actionInt = getAction(k).toInt();
            int j = actionInt % HexConfig.BOARD_SIZE;
            int i = (actionInt - j) / HexConfig.BOARD_SIZE;
            board[i][j].setValue(val);
        }
    }

    /**
     * Set all tile values to the default (Double.NaN)
     */
    protected void clearTileValues() {
        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                board[i][j].setValue(Double.NaN);
            }
        }
    }

    @Override
    public int getNumPlayers() {
        return 2;
    }

    @Override
    public int getPlayer() {
        return currentPlayer;
    }

    public HexTile[][] getBoard() {
        return board;
    }


    /**
     * @return The tile on which the last stone was placed
     */
    HexTile getLastUpdatedTile() {
        return lastUpdatedTile;
    }

}
