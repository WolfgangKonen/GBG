package games.Yavalath;

import games.ObserverBase;
import games.StateObservation;
import tools.Types;

import java.io.Serializable;
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
    private boolean swapRuleUsed = false;
    private int numPlayers = PLAYERS;
    private GameInformation information;
    private ArrayList <TileYavalath> moveList;

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
        information = new GameInformation(numPlayers);
        moveList = new ArrayList<>();
    }

    public StateObserverYavalath(StateObserverYavalath stateObserverYavalath) {
        super(stateObserverYavalath);
        this.board = new TileYavalath[BOARD_SIZE][BOARD_SIZE];
        this.board = UtilityFunctionsYavalath.copyBoard(stateObserverYavalath.getGameBoard());
        this.currentPlayer = stateObserverYavalath.currentPlayer;
        this.numPlayers = stateObserverYavalath.numPlayers;
        this.swapRuleUsed = stateObserverYavalath.swapRuleUsed;
        this.information = new GameInformation(stateObserverYavalath.information);
        this.moveList = (ArrayList<TileYavalath>) stateObserverYavalath.moveList.clone();
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
    @Override
    public void setAvailableActions() {

        availableActions = new ArrayList<>();

        //In 3-player-variant need to check if the next player can win on this move and set the action to that winning move
        if(numPlayers == 3 && moveList != null){
            Types.ACTIONS threateningMove = UtilityFunctionsYavalath.nextPlayerCanWin(this);
            if(threateningMove!= null){
                availableActions.add(threateningMove);
                return;
            }
        }

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
        if(getMoveCounter()==1 && numPlayers == 2){
            availableActions.add(new Types.ACTIONS(moveList.get(0).getX()*BOARD_SIZE+moveList.get(0).getY()));
        }

    }

    @Override
    public Types.ACTIONS getAction(int i) {
        return availableActions.get(i);
    }

    /**
     * Advances the current state given 'action' to a new state and updates the game information accordingly.
     */
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
        board[i][j].setThreateningMove(false);
        moveList.add(0, board[i][j]);
        super.addToLastMoves(action);
        super.incrementMoveCounter();

        setAvailableActions();
        Types.WINNER info = UtilityFunctionsYavalath.getWinner(this);
        if(info != null){
            switch(info){
                case TIE -> information.updateGameInformation(-1,-1);
                case PLAYER_WINS -> information.updateGameInformation(currentPlayer,-1);
                case PLAYER_LOSES -> {
                    information.updateGameInformation(-2,currentPlayer);

                    if(availableActions.size() == 0){   //in case the losing move of the player also places the last tile on the board
                        information.updateGameInformation(-1,information.loser);
                    }
                }
            }
        }
        currentPlayer = getNextPlayer();
    }

    private void advance(TileYavalath tile){
        Types.ACTIONS action = new Types.ACTIONS(tile.getX()*BOARD_SIZE+tile.getY());
        advance(action);
    }

    @Override
    public int getPlayer() {
        return currentPlayer;
    }

    @Override
    public int getNumPlayers() {
        return numPlayers;
    }


    /**
     * @return 	the game score, i.e. the sum of rewards for the current state.
     * 			For Yavalath only game-over states have a non-zero game score.
     * 			It is the reward from the perspective of {@code player}.
     */
    @Override
    public double getGameScore(int player) {
        return information.getGameScores()[player];
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
        return (information.winner != -2);
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
     * Returns the last tile that player has played.
     */
    public TileYavalath getLastTileFromPlayer(int player){
        for (TileYavalath x:moveList){
            if(x.getPlayer() == player) return x;
        }
        return null;
    }

    public ArrayList<TileYavalath> getMoveList(){
        return moveList;
    }


    /**
     * Checks if the current state of the game board is legal and achievable by counting
     * the game tiles each player has placed. Considers the swap-rule in 2 player version
     * and prematurely exited players in 3 player version.
     */
    @Override
    public boolean isLegalState() {
        int playerOneTiles = 0;
        int playerTwoTiles = 0;
        int playerThreeTiles = 0;

        for (TileYavalath[] tileRow:board) {
            for(TileYavalath tile:tileRow){
                if(tile.getPlayer() == PLAYER_ONE){
                    playerOneTiles++;
                } else if(tile.getPlayer()==PLAYER_TWO){
                    playerTwoTiles++;
                } else if(tile.getPlayer() == PLAYER_THREE){
                    playerThreeTiles++;
                }
            }
        }

        boolean result = false;

        switch (numPlayers){
            case 2 -> result = legalState2P(playerOneTiles,playerTwoTiles);
            case 3 -> result = legalState3P(playerOneTiles,playerTwoTiles,playerThreeTiles);
        }

        return result;
    }

    private boolean legalState2P(int playerOneTiles, int playerTwoTiles){
        if(currentPlayer == PLAYER_ONE){
            if(swapRuleUsed){
                return (playerTwoTiles-1 == playerOneTiles);
            } else{
                return (playerOneTiles == playerTwoTiles);
            }
        } else {
            if(swapRuleUsed){
                return (playerOneTiles == playerTwoTiles);
            } else{
                return (playerOneTiles-1 == playerTwoTiles);
            }
        }
    }

    private boolean legalState3P(int playerOneTiles, int playerTwoTiles, int playerThreeTiles){
        if(currentPlayer == PLAYER_ONE){
            switch(information.loser){
                case -1 -> {
                    return (playerOneTiles == playerTwoTiles && playerOneTiles == playerThreeTiles);
                }
                case 0 -> {
                    return (playerTwoTiles == playerThreeTiles);
                }
                case 1 -> {
                    return (playerOneTiles == playerThreeTiles);
                }
                case 2 -> {
                    return (playerOneTiles == playerTwoTiles);
                }
            }
        }else if(currentPlayer == PLAYER_TWO){
            switch(information.loser){
                case -1 -> {
                    return (playerOneTiles-1 == playerTwoTiles && playerOneTiles-1 == playerThreeTiles);
                }
                case 0 -> {
                    return (playerTwoTiles == playerThreeTiles);
                }
                case 1 -> {
                    return (playerOneTiles-1 == playerThreeTiles);
                }
                case 2 -> {
                    return (playerOneTiles-1 == playerTwoTiles);
                }
            }
        }else{
            switch(information.loser){
                case -1 -> {
                    return (playerOneTiles == playerTwoTiles && playerOneTiles+1 == playerThreeTiles);
                }
                case 0 -> {
                    return (playerTwoTiles+1 == playerThreeTiles);
                }
                case 1 -> {
                    return (playerOneTiles+1 == playerThreeTiles);
                }
                case 2 -> {
                    return (playerOneTiles+1 == playerTwoTiles);
                }
            }
        }
        return false;
    }

    public boolean isLegalAction(Types.ACTIONS action){
        return availableActions.contains(action);
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

    public TileYavalath[][] getGameBoard(){
        return board;
    }

    public void useSwapRule(){
        advance(moveList.get(0));
    }

    public boolean swapRuleUsed(){
        return swapRuleUsed;
    }

    /**
     * Adjusts the next player. Skips if next player has lost already (Only applicable to 3-player-Yavalath).
     * @return Next player to play
     */
    @Override
    public int getNextPlayer(){
        int nextPlayer = (currentPlayer+1)%numPlayers;
        if(nextPlayer == information.loser){
            nextPlayer = (nextPlayer+1)%numPlayers;
        }
        return nextPlayer;
    }

    public int getNextPlayerFrom(int player){
        int nextPlayer = (player+1)%numPlayers;
        if(nextPlayer == information.loser){
            nextPlayer = (nextPlayer+1)%numPlayers;
        }
        return nextPlayer;
    }

    public int getLoser(){
        return information.loser;
    }

    public int getWinner(){
        return information.winner;
    }

    public boolean isTie(){
        return information.winner == -1;
    }


    /**
     * Only used for adjusting the player numbers for testing purposes.
     * @param players Number of players
     */
    public void setNumPlayers(int players){
        numPlayers = players;
        information = new GameInformation(players);
    }


    /**
     * Holds information about the final game state, including if a player
     * has lost already in 3-player-Yavalath and the game-scores for each player.
     */
    class GameInformation implements Serializable {
        private int winner; //-2 uninitialized, -1 on draw, 0,1,2 on player win
        private int loser; //-1 uninitialized, 0,1,2 if a player has lost
        double[] gameScores;

        GameInformation(int numPlayers){
            winner = -2;
            loser = -1;
            gameScores = new double[numPlayers];
        }

        GameInformation(GameInformation original){
            this.winner = original.winner;
            this.loser = original.loser;
            this.gameScores = original.gameScores.clone();
        }

        /**
         * Updates the game information from the latest winner or loser information.
         * @param winner -1 for ties, 0,1,2 for player win, -2 for everything else
         * @param loser 0,1,2 for player loss, -1 for everything else
         */
        public void updateGameInformation(int winner,int loser){
            this.winner = winner;
            this.loser = loser;
            updateGameScores();
        }

        private void updateGameScores() {
            switch (numPlayers){
                case 2 -> updateGameScores2P();
                case 3 -> updateGameScores3P();
            }
        }

        private void updateGameScores3P() {
            switch (winner){
                case -2 -> {     //No winner yet
                    switch (loser){
                        case 0 -> { //Player 1 has lost
                            gameScores[0] = NEGATIVE_REWARD;
                            // another player has already lost before, need to mark winner
                            if(gameScores[1] == NEGATIVE_REWARD || gameScores[2] == NEGATIVE_REWARD){
                                if(gameScores[1] == NEGATIVE_REWARD){ //Player 2 has also lost, 3 wins
                                    winner = 2;
                                    gameScores[2] = POSITIVE_REWARD;
                                }else {     //Player 3 has also lost, 2 wins
                                    winner = 1;
                                    gameScores[1] = POSITIVE_REWARD;
                                }
                            }
                        }
                        case 1 -> { //Player 2 has lost
                            gameScores[1] = NEGATIVE_REWARD;
                            // another player has already lost before, need to mark winner
                            if(gameScores[0] == NEGATIVE_REWARD || gameScores[2] == NEGATIVE_REWARD){
                                if(gameScores[0] == NEGATIVE_REWARD){ //Player 1 has also lost, 3 wins
                                    winner = 2;
                                    gameScores[2] = POSITIVE_REWARD;
                                }else {     //Player 3 has also lost, 1 wins
                                    winner = 0;
                                    gameScores[0] = POSITIVE_REWARD;
                                }
                            }
                        }
                        case 2 -> { //Player 3 has lost
                            gameScores[2] = NEGATIVE_REWARD;
                            // another player has already lost before, need to mark winner
                            if(gameScores[0] == NEGATIVE_REWARD || gameScores[1] == NEGATIVE_REWARD){
                                if(gameScores[0] == NEGATIVE_REWARD){   //Player 1 has also lost, 2 wins
                                    winner = 1;
                                    gameScores[1] = POSITIVE_REWARD;
                                }else {     //Player 2 has also lost, 1 wins
                                    winner = 0;
                                    gameScores[0] = POSITIVE_REWARD;
                                }
                            }
                        }
                    }
                }
                case -1 -> {  //draw
                    switch (loser){
                        case -1 -> { //draw between everyone
                                for (int i = 0; i <3 ; i++) {
                                gameScores[i] = 0.0;
                            }
                        }
                        case 0 -> { //draw between player 2 and 3
                            gameScores[1] = 0.0;
                            gameScores[2] = 0.0;
                        }
                        case 1 -> { //draw between player 1 and 3
                            gameScores[0] = 0.0;
                            gameScores[2] = 0.0;
                        }
                        case 2 -> { //draw between player 1 and 2
                            gameScores[0] = 0.0;
                            gameScores[1] = 0.0;
                        }

                    }
                    }
                case 0 -> { //Player 1 wins, 2 and 3 lose
                    gameScores[0] = POSITIVE_REWARD;
                    gameScores[1] = NEGATIVE_REWARD;
                    gameScores[2] = NEGATIVE_REWARD;
                }
                case 1 -> { //Player 2 wins, 1 and 3 lose
                    gameScores[0] = NEGATIVE_REWARD;
                    gameScores[1] = POSITIVE_REWARD;
                    gameScores[2] = NEGATIVE_REWARD;
                }
                case 2 -> { //Player 3 wins, 1 and 2 lose
                    gameScores[0] = NEGATIVE_REWARD;
                    gameScores[1] = NEGATIVE_REWARD;
                    gameScores[2] = POSITIVE_REWARD;
                }
            }
        }

        private void updateGameScores2P() {
            switch(winner){
                case -2 -> { //no winner yet
                    switch (loser){
                        case 0 -> { //Player 1 loses, 2 wins
                            gameScores[0] = NEGATIVE_REWARD;
                            gameScores[1] = POSITIVE_REWARD;
                            winner = 1;
                        }
                        case 1 -> { //Player 2 loses, 1 wins
                            gameScores[0] = POSITIVE_REWARD;
                            gameScores[1] = NEGATIVE_REWARD;
                            winner = 0;
                        }
                    }
                }
                case -1 -> { //draw
                    gameScores[0] = 0.0;
                    gameScores[1] = 0.0;
                }
                case 0 -> { //Player 1 wins, 2 loses
                    gameScores[0] = POSITIVE_REWARD;
                    gameScores[1] = NEGATIVE_REWARD;
                }
                case 1 -> { //Player 2 wins, 1 loses
                    gameScores[0] = NEGATIVE_REWARD;
                    gameScores[1] = POSITIVE_REWARD;
                }
            }
        }

        public int getWinner(){
            return winner;
        }

        public int getLoser(){
            return loser;
        }

        public double[] getGameScores(){
            return gameScores;
        }


    }
}
