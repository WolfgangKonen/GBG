package games.Yavalath;

import games.ObserverBase;
import games.StateObservation;
import tools.Types;
import java.util.ArrayList;
import static games.Yavalath.ConfigYavalath.*;
/*
 *                  Board representation, full board with invalid cells
 *                          [0,0][0,1][0,2][0,3][0,4][0,5][0,6][0,7][0,8]
 *                       [1,0][1,1][1,2][1,3][1,4][1,5][1,6][!,7][0,8]
 *                    [2,0][2,1][2,2][2,3][2,4][2,5][2,6][2,7][2,8]
 *                 [3,0][3,1][3,2][3,3][3,4][3,5][3,6][3,7][3,8]
 *              [4,0][4,1][4,2][4,3][4,4][4,5][4,6][4,7][4,8]
 *           [5,0][5,1][5,2][5,3][5,4][5,5][5,6][5,7][5,8]
 *        [6,0][6,1][6,2][6,3][6,4][6,5][6,6][6,7][6,8]
 *     [7,0][7,1][7,2][7,3][7,4][7,5][7,6][7,7][7,8]
 *  [8,0][8,1][8,2][8,3][8,4][8,5][8,6][8,7][8,8]
 *
 *
 *                  Board representation, invalid cells are marked with [x,x]
 *
 *                            [0,0][0,1][0,2][0,3][0,4][x,x][x,x][x,x][x,x]
 *                         [1,0][1,1][1,2][1,3][1,4][1,5][x,x][x,x][x,x]
 *                      [2,0][2,1][2,2][2,3][2,4][2,5][2,6][x,x][x,x]
 *                   [3,0][3,1][3,2][3,3][3,4][3,5][3,6][3,7][x,x]
 *                [4,0][4,1][4,2][4,3][4,4][4,5][4,6][4,7][4,8]
 *             [x,x][5,1][5,2][5,3][5,4][5,5][5,6][5,7][5,8]
 *          [x,x][x,x][6,2][6,3][6,4][6,5][6,6][6,7][6,8]
 *       [x,x][x,x][x,x][7,3][7,4][7,5][7,6][7,7][7,8]
 *    [x,x][x,x][x,x][x,x][8,4][8,5][8,6][8,7][8,8]
 *
 *
 */

public class StateObserverYavalath extends ObserverBase implements StateObservation {

    private ArrayList<Types.ACTIONS> availableActions;
    private TileYavalath[][] board;
    private int currentPlayer;
    private TileYavalath lastPlayedTile;
    private boolean swapRuleUsed = false;

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .gamelog containing this object will become
     * unreadable or you have to provide a special version transformation)
     */
    private static final long serialVersionUID = 12L;

    public StateObserverYavalath(){
        super();
        board = setDefaultGameBoard();
        currentPlayer = PLAYER_ONE;
        setAvailableActions();
    }

    public StateObserverYavalath(StateObserverYavalath stateObserverYavalath) {
        super(stateObserverYavalath);
        this.board = new TileYavalath[BOARD_SIZE][BOARD_SIZE];
        this.board = UtilityFunctionsYavalath.copyBoard(stateObserverYavalath.getGameBoard());
        this.currentPlayer = stateObserverYavalath.currentPlayer;
        this.lastPlayedTile = stateObserverYavalath.lastPlayedTile;
        this.swapRuleUsed = stateObserverYavalath.swapRuleUsed;
        if(stateObserverYavalath.availableActions!=null){
            this.availableActions = (ArrayList<Types.ACTIONS>) stateObserverYavalath.availableActions.clone();
        }
    }

    /**
     * Creates a new game board with empty or invalid tiles depending on how they are used.
     */
    public TileYavalath[][] setDefaultGameBoard() {
        TileYavalath[][] newBoard = new TileYavalath[BOARD_SIZE][BOARD_SIZE];

        for(int i=0;i<BOARD_SIZE;i++){
            for(int j=0;j<BOARD_SIZE;j++){
                if (Math.abs(j-i)<BOARD_LENGTH){
                    newBoard[i][j] = new TileYavalath(i,j, EMPTY);
                }
                else {
                    newBoard[i][j] = new TileYavalath(i,j,INVALID_FIELD);
                }
            }
        }
        return newBoard;
    }

    @Override
    public ArrayList<Types.ACTIONS> getAllAvailableActions() {
        ArrayList<Types.ACTIONS> allActions = new ArrayList<>();
        for(int i=0;i<BOARD_SIZE;i++){
            for(int j=0;j<BOARD_SIZE;j++){
                if(Math.abs(j-i)<BOARD_LENGTH){
                    int actionInt = i*BOARD_SIZE+j;
                    allActions.add(new Types.ACTIONS(actionInt));
                }
            }
        }
        return allActions;
    }


    @Override
    public ArrayList<Types.ACTIONS> getAvailableActions() {
        return availableActions;
    }

    @Override
    public int getNumAvailableActions() {
        return availableActions.size();
    }

    /**
     *
     */
    //TODO Need to consider swap rule + 3 player extension in that case
    @Override
    public void setAvailableActions() {
            availableActions = new ArrayList<>();
            for(int i=0;i<BOARD_SIZE;i++){
                for(int j=0;j<BOARD_SIZE;j++){
                    if(Math.abs(j-i)<BOARD_LENGTH && board[i][j].getPlayer() == EMPTY){
                        int actionInt = i*BOARD_SIZE+j;
                        availableActions.add(new Types.ACTIONS(actionInt));
                    }
                }
            }
            //If only the first piece has been placed, swap rule is available and the just executed action needs to be added to
            //the list of available actions again
            if(getMoveCounter()==1){
                availableActions.add(new Types.ACTIONS(lastPlayedTile.getX()*BOARD_SIZE+lastPlayedTile.getY()));
            }

    }

    @Override
    public Types.ACTIONS getAction(int i) {
        return availableActions.get(i);
    }

    /**
     * Advances the current state given 'action' to a new state
     */
    //TODO 3 player extension
    @Override
    public void advance(Types.ACTIONS action) {
        this.advanceBase(action);
        int actionInt = action.toInt();

        int j = actionInt % BOARD_SIZE;
        int i = (actionInt-j) / BOARD_SIZE;

        if (board[i][j].getPlayer() != EMPTY && board[i][j].getPlayer() != INVALID_FIELD){
            if(super.m_counter == 1){
                board[i][j].setPlayer(currentPlayer);
                swapRuleUsed = true;
            }else return;
        }

        board[i][j].setPlayer(currentPlayer);
        lastPlayedTile = board[i][j];
        setAvailableActions();
        super.addToLastMoves(action);
        super.incrementMoveCounter();

        currentPlayer = (currentPlayer == PLAYER_ONE ) ? PLAYER_TWO : PLAYER_ONE;

    }

    @Override
    public int getPlayer() {
        return currentPlayer;
    }

    @Override
    public int getNumPlayers() {
        return PLAYERS;
    }


    /**
     * @return 	the game score, i.e. the sum of rewards for the current state.
     * 			For Yavalath only game-over states have a non-zero game score.
     * 			It is the reward from the perspective of {@code player}.
     */
    //TODO 3 player extension
    @Override
    public double getGameScore(int player) {
        Types.WINNER winner = UtilityFunctionsYavalath.getWinner(board,lastPlayedTile);

        if(winner == Types.WINNER.PLAYER_WINS) {
            if(player == lastPlayedTile.getPlayer()) return 1.0;
            else return -1.0;
        }
        else if (winner == Types.WINNER.PLAYER_LOSES) {
            if(player == lastPlayedTile.getPlayer()) return -1.0;
            else return 1.0;
        }
        else return 0;
    }

    @Override
    public double getMinGameScore() {
        return NEGATIVE_REWARD;
    }

    @Override
    public double getMaxGameScore() {
        return POSITIVE_REWARD;
    }

    @Override
    public String getName() {
        return "Yavalath";
    }

    @Override
    public StateObserverYavalath copy() {
        return new StateObserverYavalath(this);
    }

    @Override
    public boolean isGameOver() {
        int winner = determineWinner();
        return (getNumAvailableActions() == 0 || winner != EMPTY);
    }

    @Override
    public boolean isDeterministicGame() {
        return true;
    }

    @Override
    public boolean isFinalRewardGame() {
        return true;
    }


    /**
     * Checks if the current state of the game board is legal and achievable by counting
     * the game tiles each player has placed. Considers the swap-rule.
     */
    //TODO 3 player extension
    @Override
    public boolean isLegalState() {
        int playerOneTiles = 0;
        int playerTwoTiles = 0;

        for (TileYavalath[] tileRow:board) {
            for(TileYavalath tile:tileRow){
                if(tile.getPlayer()==PLAYER_ONE){
                    playerOneTiles++;
                } else if(tile.getPlayer()==PLAYER_TWO){
                    playerTwoTiles++;
                }
            }
        }

        if(currentPlayer == PLAYER_ONE){
            if(swapRuleUsed){
                return (playerTwoTiles-1 == playerOneTiles);
            } else {
                return (playerOneTiles == playerTwoTiles);
            }
        } else if(currentPlayer == PLAYER_TWO){
            if(swapRuleUsed){
                return (playerOneTiles == playerTwoTiles);
            } else{
                return (playerOneTiles-1 == playerTwoTiles);
            }
        }

        return true;
    }

    @Override
    public String stringDescr() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (int i = 0; i < BOARD_SIZE; i++) {
            sb.append(" ".repeat(BOARD_SIZE - i));
            for (int j = 0; j < BOARD_SIZE; j++) {
                switch (board[i][j].getPlayer()) {
                    case -2 -> sb.append("  ");
                    case -1 -> sb.append("_ ");
                    case 0 -> sb.append("1 ");
                    case 1 -> sb.append("2 ");
                    case 2 -> sb.append("3 ");
                }

            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public void storeBestActionInfo(Types.ACTIONS_VT bestAction){
        double[] valueTable = bestAction.getVTable();
        clearValues();
        for(int i=0;i<getNumAvailableActions();i++){
            double value = valueTable[i];
            int actionInt = getAction(i).toInt();
            int y = actionInt%BOARD_SIZE, x= (actionInt-y)/BOARD_SIZE;
            board[x][y].setValue(value);
        }
    }

    public void clearValues(){
        for(int i=0;i<BOARD_SIZE;i++){
            for(int j=0;j<BOARD_SIZE;j++){
                board[i][j].setValue(Double.NaN);
            }
        }
    }

    //TODO 3 player extension
    private int determineWinner(){
        Types.WINNER result = UtilityFunctionsYavalath.getWinner(board, lastPlayedTile);
        if(result == Types.WINNER.PLAYER_WINS){
            return (this.getPlayer() == PLAYER_ONE ? PLAYER_TWO : PLAYER_ONE);
        } else if(result == Types.WINNER.PLAYER_LOSES){
            return (this.getPlayer() == PLAYER_ONE ? PLAYER_ONE : PLAYER_TWO);
        }
        return EMPTY;
    }

    public TileYavalath[][] getGameBoard(){
        return board;
    }

    public TileYavalath getLastPlayedTile(){
        return lastPlayedTile;
    }
}
