package games.EWS;

import games.EWS.StateObserverHelper.Helper;
import games.EWS.StateObserverHelper.Player;
import games.EWS.StateObserverHelper.Token;
import games.EWS.constants.ConfigEWS;
import games.EWS.constants.StartingPositions;
import games.ObserverBase;
import games.StateObsNondeterministic;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;

import java.util.ArrayList;
import java.util.Random;

import static tools.Types.WINNER.*;

public class StateObserverEWS extends ObserverBase implements  StateObsNondeterministic {

    public static final long serialVersionUID = 12L;
    private static final double REWARD_NEGATIVE = -1, REWARD_POSITIVE = 1;
    private Random random;
    private int numPlayers;
    private int player;
    private int size;
    public int turn = 0 ;
    private boolean isNextActionDeterministic;
    private ACTIONS nextNondeterministicAction;
    private Token[][] gameState;
    private ACTIONS rolledDice;
    private ArrayList<Player> players;
    private ArrayList<ACTIONS> availableActions;
    private ArrayList<ACTIONS> availableRandomActions;
    private int playerWin;      // Representing the winning player for faster evaluation
    private int playerLooses;   // Representing the loosing player for faster evaluation

    public StateObserverEWS(){
        this(3,2);
    }

    public StateObserverEWS(int size, int numPlayers){
        super();
        this.player =  0;
        this.rolledDice = null;
        this.numPlayers = numPlayers;
        this.size = size;
        playerWin = -1;
        playerLooses = -1;
        this.nextNondeterministicAction=null;
        this.isNextActionDeterministic=false;
        this.gameState = new Token[size][size];
        this.random = new Random();
        this.players = new ArrayList<>();
        this.availableActions = new ArrayList<>();
        this.availableRandomActions = new ArrayList<>();
        for(int i = 0; i < getRandomActionSize(size,numPlayers); i++){
            availableRandomActions.add( new ACTIONS(i));
        }
        for(int i = 0; i < numPlayers; i++){
            players.add(new Player(i,size));
        }
        int[][][] startingPosition = getStartingsPosition(size,numPlayers);
        for(int i = 0;i < startingPosition.length; i++){
            for(int k = 0; k < startingPosition[i].length; k++){
                int[] startPosEntry = startingPosition[i][k];
                Token token = new Token(i,k,size,startPosEntry[0],startPosEntry[1]);
                gameState[i][k] = token;
                if(startPosEntry[1] >= 0){
                    players.get(startPosEntry[1]).addToken(token);
                }
            }
        }
        // Init empty board:




        advanceNondeterministic(); // setActions when we start a new state
    }


    public StateObserverEWS(StateObserverEWS other){

        this.player = other.getPlayer();
        this.numPlayers = other.getNumPlayers();
        this.size = other.getSize();
        this.random = new Random();
        this.players = new ArrayList<>();
        this.nextNondeterministicAction = other.getNextNondeterministicAction();
        this.isNextActionDeterministic = other.isNextActionDeterministic();
        this.playerLooses = other.getPlayerLooses();
        this.playerWin = other.getPlayerWin();
        this.availableActions = new ArrayList<>();
        this.availableRandomActions = other.getAvailableRandoms();
        this.gameState = new Token[size][size];
        this.turn = other.turn;
        // init players
        for(int i = 0; i < numPlayers; i++){
            this.players.add(new Player(i,size));
        }
        // init gamestate
        Token[][] gs = other.getGameState();
        for(int i = 0;  i <gs.length; i++){
            for(int k = 0; k < gs[i].length; k++){
                Token t = gs[i][k].copy();
                this.gameState[i][k] = t;
                if(t.getPlayer() >= 0){
                    players.get(t.getPlayer()).addToken(t);
                }
            }
        }
        if(this.availableActions == null) throw new RuntimeException("AvailActions cannot be null");
       setAvailableActions();
    }


    /**
     * Splitting the advancing step to deterministic and non-deterministic
     * @param action the action
     */
    @Override
    public void advance(ACTIONS action) {
        if(isNextActionDeterministic) advanceDeterministic(action);
        if(!isNextActionDeterministic) advanceNondeterministic();
    }

    /**
     * Advancing step <br />
     *      the deterministic part
     * We get action p.e. 706
     * <ol>
     *     <li>Convert the action 706 to indices [from,to] => [7,6] {@link Helper}</li>
     *     <li>Split the indices of the array to their from and to coordinates</li>
     *     <li>if to_token is a players token remove it</li>
     *     <li>Changing the gameState</li>
     *     <li>Update Players token</li>
     * </ol>

     * @param action    the action
     */
    public void advanceDeterministic(ACTIONS action){
        if(!isNextActionDeterministic){
            throw new RuntimeException("ACTION IS NOT NONDETERMINISTIC");
        }
        if(this.isGameOver()){
            throw new RuntimeException("Game is already over"); // Cannot advance a finished game.
        }
        int[] convertedAction = Helper.getIntsFromAction(action); // [from, to] int array
        int to_x = convertedAction[1] % size;
        int to_y = (convertedAction[1] - to_x) / size;
        int from_x = convertedAction[0] % size;
        int from_y = (convertedAction[0] - from_x) / size;
        Token from_token = gameState[from_y][from_x];
        Token to_token = gameState[to_y][to_x];
        // check if the token is owned by a player or is empty
        if (to_token.getPlayer() >= 0) {
            players.get(to_token.getPlayer()).removeToken(to_token);
        }
        gameState[to_y][to_x] = from_token; // Setting token to new index;
        players.get(player).updateToken(from_token, to_token);
        gameState[from_y][from_x] = new Token(from_y, from_x, size, -1, -1); // Remove old token
        turn++;
        if(!this.isGameOver()){
            this.player = (this.player + 1) % numPlayers; //next player
            isNextActionDeterministic=false;
            availableActions.clear();
        }
    }

    @Override
    public void advanceNondeterministic(){
        if(isNextActionDeterministic){
            throw new RuntimeException("ACTION IS DETERMINISTIC must be NON");
        }
        advanceNondeterministic(availableRandomActions.get(random.nextInt(availableRandomActions.size())));
    }

    @Override
    public void advanceNondeterministic(ACTIONS action) {
        nextNondeterministicAction = action;
        this.setAvailableActions();


    }




    private int getRandomActionSize(int size, int player){
        if(player == 2) return size == 3 ? 3 : 6;
        else if(player == 3) return 6;
        return size == 6 ? 6:3;
    }

    private int[][][] getStartingsPosition(int size, int player){
        if(player == 2) {
            return size == 3 ? StartingPositions.S3P2 : StartingPositions.S5P2;
        }
        else if(player == 3) return StartingPositions.S6P3;
        else{
             return size == 6 ? StartingPositions.S6P4 : StartingPositions.S4P4;
        }
    }


    /**
     * Used to reset the state to an empty state
     * Due to the multiple modes, which can be chosen from
     * This method will reuse the size and num players to reload the same starting position.
     * @return new StateObserverEWS(size, numPlayers);
     */
    public StateObserverEWS reset(){
        return new StateObserverEWS(this.getSize(), this.getNumPlayers());
    }



    @Override
    public ArrayList<ACTIONS> getAllAvailableActions() {
        ArrayList<ACTIONS> ar = new ArrayList<>();
        for(int i = 0; i <size; i++){
            for(int k = 0; k < size; k++){
                ar.add(Helper.parseAction(i,k, size));
                ar.add(Helper.parseAction(k,i, size));
            }
            ar.add(new ACTIONS(i));
        }
        return ar;
    }

    @Override
    public void setPartialState(boolean p) {
        super.setPartialState(p);
    }

    @Override
    public ArrayList<ACTIONS> getAvailableActions() {
        return this.availableActions;
    }

    @Override
    public int getNumAvailableActions() {
        return this.availableActions.size();
    }

    @Override
    public void setAvailableActions() {
        Player p = players.get(player);
        p.setAvailableActions(nextNondeterministicAction.toInt());
        this.availableActions = p.getAvailableActions();
        if(availableActions.size() == 0){
            System.out.println("Player is " + player + " turn: " + turn+ " TokenSize: " + players.get(player).getTokens().size());
            passToNextPlayer();
            this.player = (player+1) % numPlayers;
            this.isNextActionDeterministic = false;
            advanceNondeterministic();
        }else {
            isNextActionDeterministic = true;
        }
    }

    @Override
    public ACTIONS getAction(int i) {
        return this.availableActions.get(i);
    }



    @Override
    public int getPlayer() {
        return this.player;
    }

    @Override
    public int getNumPlayers() {
        return numPlayers;
    }

    private Types.WINNER getWinner(){

        if(!isGameOver()) throw new RuntimeException("Sorry game is not over!");
        if(numPlayers == 2) {
            if(playerWin >= 0) {
                return playerWin == player ? PLAYER_WINS : PLAYER_LOSES;
            }else if(playerLooses >= 0){
                return playerWin == player ? PLAYER_LOSES : PLAYER_WINS;
            }
        } else if(numPlayers == 3){
            // Can only be the single player
            if(playerLooses >= 0){
                if(playerLooses == 0 || playerLooses == 1){
                    return (player == 0 || player == 1) ? PLAYER_LOSES : PLAYER_WINS;
                }
                return player == 2 ? PLAYER_LOSES : PLAYER_WINS;
            }

            if(player == playerWin) return PLAYER_WINS;
            else return PLAYER_LOSES;

        }
        else {
            // 4 Player
            if(playerLooses == 0 || playerLooses == 2){
                if(playerLooses == 0 || playerLooses == 1){
                    return (player == 0 || player == 1) ? PLAYER_LOSES : PLAYER_WINS;
                }
                return player == 2 ? PLAYER_LOSES : PLAYER_WINS;
            }

            if(player == playerWin) return PLAYER_WINS;
            else return PLAYER_LOSES;
        }

        throw new RuntimeException("Game is no over yet.");
    }

    @Override
    public double getGameScore(StateObservation referringState) {
        if(this.isGameOver()){
            int sign = (referringState.getPlayer() == this.player) ? 1 :(-1);
            Types.WINNER win = getWinner();
            switch (win){
                case PLAYER_LOSES:
                     return sign* REWARD_NEGATIVE;
                case PLAYER_WINS:
                     return sign* REWARD_POSITIVE;
                case TIE:  throw new RuntimeException("invalid outcome of the game [wrong getGameScore]");
            };
        }
        return 0.0;
    }


    @Override
    public double getMinGameScore() {
        return -1;
    }

    @Override
    public double getMaxGameScore() {
        return +1;
    }

    @Override
    public String getName() {
        return "EWS";
    }

    @Override
    public String stringDescr() {
        String str = "\n";
        for(int i = 0; i < gameState.length; i++){
            for(int k = 0; k < gameState[i].length; k++){
                Token t = gameState[i][k];
                int p = t.getPlayer();
                str += p== 0 ? "[X": p== 1 ? "[O":p==2 ? "[*" : p==3 ? "[#": "[ ";
                String val = String.valueOf(t.getValue());
                str += (t.getValue()>-1  ? val +"]" : " ]") + " ";
            }
            str += "\n";
        }
        str += "\n";
        return str;
    }


    @Override
    public ACTIONS getNextNondeterministicAction() {
        return nextNondeterministicAction;
    }

    @Override
    public boolean isNextActionDeterministic(){
        return this.isNextActionDeterministic;
    }

    @Override
    public ArrayList<ACTIONS> getAvailableRandoms() {
        return availableRandomActions;
    }

    @Override
    public int getNumAvailableRandoms() {
        return availableRandomActions.size();
    }

    @Override
    public double getProbability(ACTIONS action) {
        return 1/availableRandomActions.size();
    }

    @Override
    public void setPlayer(int p ){
        this.player = player;
    }

    @Override
    public StateObsNondeterministic copy() {
        return new StateObserverEWS(this);
    }



    /**
     * Game Over conditions
     *
     * 2 Player:
     *  One player does not have any tokens left.
     *  One Token reaches the opposite corner
     *
     * 3 Player:
     *  One player does not have any tokens left
     *  The 2-player team needs to capture all tokens of the single-team player
     *
     *
     * @return
     */
    @Override
    public boolean isGameOver() {
       switch(numPlayers){
           case 2:  return gameOverTwoPlayer();
           case 3: return gameOverThreePlayer();
           case 4: return gameOverFourPlayer();
           default: throw new RuntimeException("numPlayer "+ numPlayers+ " is not not implemented yet");
       }
    }

    private boolean gameOverFourPlayer(){
            int[] winningPositions = {size*size-1, 0,size-1,(size*size)-size};
            //Check for winning position
            for(Player p : players){
                for(Token t: p.getTokens()){
                    if(t.getIndex() == winningPositions[p.getPlayer()]){
                        playerWin = p.getPlayer();
                        return true;
                    }
                }
                if(p.getTokens().size() == 0)
                {
                    playerLooses = p.getPlayer();
                    return true;
                }
            }

            return false;
        }

    private boolean gameOverThreePlayer(){
        for(Player p : players){
            if(p.getTokens().size() == 0) {
                playerLooses = p.getPlayer();
                return true;
            };
        }
        int winPosition = size-1;
        for(Token t : players.get(numPlayers-1).getTokens()){
            if(t.getIndex() == winPosition){
                playerWin = numPlayers-1; // can only be the 3rd player
                return true;
            }
        }
        return false;
    }

    private boolean gameOverTwoPlayer(){
        int[] winningPositions = {size*size-1,0};
        for (Player p : players) {
            for (Token t : p.getTokens()) {
                if (t.getIndex() == winningPositions[t.getPlayer()]) {
                    playerWin = p.getPlayer();
                    return true;
                }
            }
            if (p.getTokens().size() == 0) {
                playerLooses = p.getPlayer(); // setting player for getWinner()
                return true;
            }

        }
        return false;
    }

    @Override
    public boolean isDeterministicGame() {
        return false;
    }

    @Override
    public boolean isFinalRewardGame() {
        return true;
    }

    public boolean isLegalAction(ACTIONS act){
        return availableActions.contains(act);
    }
    @Override
    public boolean isLegalState() {
        return true;
    }

    public int getSize(){
        return size;
    }

    public Token[][] getGameState(){
        return gameState;
    }

    public ArrayList<Player> getPlayers(){
        return players;
    }

    public int getPlayerWin(){
        return playerWin;
    }

    public int getPlayerLooses(){
        return playerLooses;
    }

}
