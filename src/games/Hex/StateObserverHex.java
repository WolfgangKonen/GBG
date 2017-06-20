package games.Hex;

import games.StateObservation;
import tools.Types;

import java.util.ArrayList;

import static games.Hex.HexConfig.*;


public class StateObserverHex implements StateObservation {
    private int currentPlayer;

    private HexTile[][] board;
    private HexTile lastUpdatedTile;
    private ArrayList<Types.ACTIONS> actions;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

    public StateObserverHex(){
        board = defaultGameBoard();
        currentPlayer = HexConfig.PLAYER_ONE;
        setAvailableActions();
    }

    public StateObserverHex(HexTile[][] table) {
        int pieceCount=0;
        for (int i=0; i<HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                pieceCount += (table[i][j].getPlayer() == HexConfig.PLAYER_ONE ? 1 : -1);
            }
        }
        currentPlayer = (pieceCount%2==0 ? HexConfig.PLAYER_ONE : PLAYER_TWO);
        board = new HexTile[HexConfig.BOARD_SIZE][HexConfig.BOARD_SIZE];
        copyTable(table);
        setAvailableActions();
    }

    public StateObserverHex(HexTile[][] table, int player, HexTile lastUpdatedTile) {
        board = new HexTile[HexConfig.BOARD_SIZE][HexConfig.BOARD_SIZE];
        copyTable(table);
        currentPlayer = player;
        this.lastUpdatedTile = lastUpdatedTile;
        setAvailableActions();
    }

    private void copyTable(HexTile[][] table){
        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                board[i][j] = table[i][j].copy();
            }
        }
    }

    private HexTile[][] defaultGameBoard(){
        HexTile[][] newBoard = new HexTile[HexConfig.BOARD_SIZE][HexConfig.BOARD_SIZE];

        for (int i=0;i<HexConfig.BOARD_SIZE;i++) {
            for (int j=0;j<HexConfig.BOARD_SIZE;j++) {
                newBoard[i][j] = new HexTile(i, j);
                newBoard[i][j].setPoly(HexUtils.createHexPoly(i, j, HexConfig.OFFSET, HexConfig.BOARD_SIZE, HexConfig.HEX_SIZE));
            }
        }

        return newBoard;
    }

    @Override
    public StateObserverHex copy() {
        return new StateObserverHex(board, currentPlayer, lastUpdatedTile);
    }

    @Override
    public boolean isGameOver() {
        return determineWinner() != PLAYER_NONE || getNumAvailableActions() == 0;
    }

    @Override
    public Types.WINNER getGameWinner() {
        return (determineWinner() == currentPlayer ? Types.WINNER.PLAYER_LOSES : Types.WINNER.PLAYER_WINS);
    }

    private int determineWinner(){
        Types.WINNER winner = HexUtils.getWinner(getBoard(), getLastUpdatedTile());
        if (winner == Types.WINNER.PLAYER_WINS){
            //Reverse winners, since current player changes after the winning tile was placed
            return (getCurrentPlayer() == PLAYER_ONE ? PLAYER_ONE : PLAYER_TWO);
        }
        return PLAYER_NONE;
    }

    @Override
    public boolean isLegalState() {
        int playerOneTiles = 0;
        int playerTwoTiles = 0;

        for (int i=0; i<HexConfig.BOARD_SIZE; i++){
            for (int j=0; j<HexConfig.BOARD_SIZE; j++){
                if (board[i][j].getPlayer() == PLAYER_ONE){
                    playerOneTiles++;
                } else if (board[i][j].getPlayer() == PLAYER_TWO){
                    playerTwoTiles++;
                }
            }
        }

        if (currentPlayer == PLAYER_ONE){
            return (playerOneTiles == playerTwoTiles);
        } else {
            return (playerOneTiles-1 == playerTwoTiles);
        }
    }

    @Override
    public String stringDescr() {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<HexConfig.BOARD_SIZE;i++) {
            for (int k = 0; k<i; k++){
                sb.append(' ');
            }
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                switch (board[i][j].getPlayer()){
                    case HexConfig.PLAYER_ONE:
                        sb.append('W');
                        break;
                    case PLAYER_TWO:
                        sb.append('B');
                        break;
                    default:
                        sb.append('-');
                }
            }
            sb.append("\n");

        }
        return sb.toString();
    }

    public String toString() {
        return stringDescr();
    }

    @Override
    public double getGameScore() {
        int winner = determineWinner();
        if (winner == PLAYER_NONE){
            return 0;
        }

        return (winner == currentPlayer ? REWARD_NEGATIVE : REWARD_POSITIVE);
    }

    @Override
    public double getGameValue() {
        return getGameScore();
    }

    @Override
    public double getGameScore(StateObservation referingState) {
        return (this.getPlayer() == referingState.getPlayer() ? getGameScore() : getGameScore()*(-1));
    }

    @Override
    public double getMinGameScore() {
        return REWARD_NEGATIVE;
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
        if (action == null){
            System.out.println("HEX ERROR: null given as action in advance()");
            return;
        }
        int actionInt = action.toInt();
        assert (0<=actionInt && actionInt<=(HexConfig.BOARD_SIZE*HexConfig.BOARD_SIZE)) : "Invalid action: "+actionInt;
        int j = actionInt       % HexConfig.BOARD_SIZE;
        int i = (actionInt - j) / HexConfig.BOARD_SIZE;

        if (board[i][j].getPlayer() != HexConfig.PLAYER_NONE){
            System.out.println("Tile ("+i+", "+j+") has already been claimed by a player.");
            return;
        }
        board[i][j].setPlayer(currentPlayer);

        lastUpdatedTile = board[i][j];
        setAvailableActions(); 			// IMPORTANT: adjust the available actions (have reduced by one)

        // set up player for next advance()
        currentPlayer = (currentPlayer == HexConfig.PLAYER_ONE ? PLAYER_TWO : HexConfig.PLAYER_ONE);
    }

    @Override
    public ArrayList<Types.ACTIONS> getAvailableActions() {
        return actions;
    }

    @Override
    public int getNumAvailableActions() {
        return actions.size();
    }

    @Override
    public void setAvailableActions() {
        actions = new ArrayList<>();
        for (int i=0; i<HexConfig.BOARD_SIZE; i++){
            for (int j=0; j<HexConfig.BOARD_SIZE; j++){
                if (board[i][j].getPlayer() == HexConfig.PLAYER_NONE){
                    int actionInt = i*HexConfig.BOARD_SIZE + j;
                    actions.add(Types.ACTIONS.fromInt(actionInt));
                }
            }
        }
    }

    @Override
    public Types.ACTIONS getAction(int i) {
        return actions.get(i);
    }

    @Override
    public void storeBestActionInfo(Types.ACTIONS bestAction, double[] valueTable) {
        for(int i=0;i<HexConfig.BOARD_SIZE;i++){
            for(int j=0;j<HexConfig.BOARD_SIZE;j++) {
                board[i][j].setValue(Double.NaN);
            }
        }

        for(int k = 0; k < getNumAvailableActions(); ++k) {
            double val = valueTable[k];
            int actionInt = getAction(k).toInt();
            int j = actionInt % HexConfig.BOARD_SIZE;
            int i = (actionInt - j) / HexConfig.BOARD_SIZE;
            board[i][j].setValue(val);
        }
    }

    protected void clearTileValues(){
        for (int i=0;i<HexConfig.BOARD_SIZE;i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                board[i][j].setValue(Double.NaN);
            }
        }
    }

    @Override
    public int getPlayer() {
        return currentPlayer;
    }

    public HexTile[][] getBoard(){
        return board;
    }

    @Override
    public int getNumPlayers() {
        return 2;
    }

    HexTile getLastUpdatedTile() {
        return lastUpdatedTile;
    }

    int getCurrentPlayer(){
        return currentPlayer;
    }
}
