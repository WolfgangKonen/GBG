package games.EWN;

import TournamentSystem.TSTimeStorage;
import controllers.PlayAgtVector;
import games.EWN.StateObserverHelper.Helper;
import games.EWN.StateObserverHelper.Player;
import games.EWN.StateObserverHelper.Token;
import games.EWN.config.ConfigEWN;
import games.EWN.config.StartingPositions;
import games.ObsNondetBase;
import games.StateObsNondeterministic;
import games.StateObservation;
import games.XArenaFuncs;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import controllers.ExpectimaxNAgent;

import java.io.Serial;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static tools.Types.WINNER.*;

/**
 * Class StateObserverEWN observes the current state of the game.<p>
 * It has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}),
 * <li> advancing the state of the game with a specific action ({@link games.StateObservation#advance(ACTIONS, Random)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 */
public class StateObserverEWN extends ObsNondetBase implements  StateObsNondeterministic {

    private static final double REWARD_NEGATIVE = -1, REWARD_POSITIVE = 1;
    private int numPlayers; // /WK/ really needed? will be always =ConfigEWN.NUM_PLAYERS
    private int player;
    private int size;                         // gets ConfigEWN.BOARD_SIZE
    private boolean isNextActionDeterministic;
    private ACTIONS nextNondeterministicAction;     // the dice value minus 1
    private Token[][] gameState;
    //private ACTIONS rolledDice;                   // /WK/ seems never used
    //private int turn = 0 ;                        // /WK/ seems never used
    //private int count;                            // /WK/ seems never used
    private ArrayList<Player> players;
    private ArrayList<ACTIONS> availableActions;
    private ArrayList<ACTIONS> availableRandomActions;
    private int playerWin;      // Representing the winning player for faster evaluation
    private int playerLoses;    // Representing the loosing player for faster evaluation
    private ScoreTuple m_scoreTuple;

    @Serial
    private static final long  serialVersionUID = 13L;

    public StateObserverEWN() {
        this((Random) null);
    }

    public StateObserverEWN(Random cmpRand) {
        super();
        init(cmpRand);
    }

    private void init(Random cmpRand) {
        this.player =  0;
        this.numPlayers = ConfigEWN.NUM_PLAYERS;
        this.size = ConfigEWN.BOARD_SIZE;
        m_scoreTuple = new ScoreTuple(numPlayers);
        playerWin = -1;             // -1 signals: not yet determined
        playerLoses = -1;           //
        //count = 0;
        this.nextNondeterministicAction=null;
        this.isNextActionDeterministic=false;
        this.gameState = new Token[size][size];
        this.players = new ArrayList<>();
        this.availableActions = new ArrayList<>();
        this.availableRandomActions = new ArrayList<>();
        for(int i = 0; i < getRandomActionSize(size,numPlayers); i++){
            availableRandomActions.add( new ACTIONS(i));
        }
        for(int i = 0; i < numPlayers; i++){
            players.add(new Player(i,size));
        }
        if(ConfigEWN.RANDOM_POSITION){
            getRandomStartingPosition(cmpRand);
        }else {
            getFixedStartingPosition();
        }
        // Init empty board:
        advanceNondeterministic(cmpRand); // roll the dice and setAvailableActions when we start a new state
    }



    public StateObserverEWN(StateObserverEWN other){
        super(other);		// copy members m_counter, lastMoves and stored*
                            // /WK/ ! was missing before 2021-09-10
        this.player = other.getPlayer();
        this.numPlayers = other.getNumPlayers();
        this.size = other.getSize();
        this.players = new ArrayList<>();
        this.nextNondeterministicAction = other.getNextNondeterministicAction();
        this.isNextActionDeterministic = other.isNextActionDeterministic();
        this.playerLoses = other.getPlayerLoses();
        this.playerWin = other.getPlayerWin();
        this.m_scoreTuple = new ScoreTuple(other.m_scoreTuple);
        this.availableActions = new ArrayList<>();
        this.availableRandomActions = other.getAvailableRandoms();
        this.gameState = new Token[size][size];
        //this.count = other.count;
        //this.turn = other.turn;
        //this.rolledDice = other.rolledDice;

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
        if(this.availableActions == null) throw new RuntimeException("availableActions cannot be null");
        setAvailableActions();
    }

    private void getFixedStartingPosition(){
        int[][][] startingPosition = getStartingPositions();
        for(int i = 0;i < startingPosition.length; i++){
            for(int k = 0; k < startingPosition[i].length; k++){
                int[] startPosEntry = startingPosition[i][k];
                Token token = new Token(i,k,startPosEntry[0],startPosEntry[1]);
                gameState[i][k] = token;
                if(startPosEntry[1] >= 0){
                    players.get(startPosEntry[1]).addToken(token);
                }
            }
        }
    }

    public void getRandomStartingPosition(Random cmpRand){
        // Init empty field
        for(int x = 0; x < ConfigEWN.BOARD_SIZE; x++)
            for(int y = 0; y < ConfigEWN.BOARD_SIZE; y++)
                gameState[x][y] = new Token(x,y,-1,-1);

        //init player tokens and mirror for the opponent
        int[] values = getValues();
        int[] randomPositionIndex = getRandomPositionindices(cmpRand);
        for(int player = 0; player < ConfigEWN.NUM_PLAYERS; player++){
            for(int index = 0; index < values.length;index++){
                int position = rotate(randomPositionIndex[index],player);
                int j = position % ConfigEWN.BOARD_SIZE;
                int i = (position-j)/ConfigEWN.BOARD_SIZE;
                Token t = new Token(i,j,values[index],player);
                players.get(player).addToken(t);
                gameState[i][j]=t;
            }
        }
    }

    /**
     *           2nd player
     *  0 1 2    8 7 6
     *  3 4 5 => 5 4 3
     *  6 7 8    2 1 0
     *
     * @param index
     * @param player
     * @return
     */
    private int rotate(int index, int player){
        int n = ConfigEWN.BOARD_SIZE;
        int j = index % n;
        int i = (index-j) / n ;
        return switch (player) {
            case 0 -> index;
            case 1 -> (n * n - 1) - ((i * n) + j);
            case 2 -> (n - 1 - j) * n + i; // 0 => 30, 1 => 31
            case 3 -> j * n + (n - 1 - i);
            default -> throw new RuntimeException("Swap not implemented");
        };
    }


    private int[] getRandomPositionindices(Random cmpRand){
        int[] randomIndices = getNormalPositionIndices();
        return shuffle(randomIndices, cmpRand);
    }

    private int[] shuffle(int[] array, Random cmpRand){
        int index;
        for(int i = array.length-1; i > 0; i--){
            if (cmpRand==null) {
                index = ThreadLocalRandom.current().nextInt(i+1);
            } else {
                index = cmpRand.nextInt(i+1);
            }
            if(index != i){
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
        return array;
    }

    /**
     * @return the indices - in ascending order - that player 0's pieces occupy in the starting position
     */
    private int[] getNormalPositionIndices(){
        return switch (ConfigEWN.BOARD_SIZE) {
            case 3 -> new int[]{0, 1, 3};
            case 4 -> new int[]{0, 1, 4};
            case 5 -> new int[]{0, 1, 2, 5, 6, 10};
            case 6 -> new int[]{0, 1, 2, 6, 7, 12};
            default -> throw new RuntimeException("Illegal board size");
        };
    }

    private int[] getValues(){
       if(ConfigEWN.BOARD_SIZE > 4) return StartingPositions.TOKENS_SIX;
       else return StartingPositions.TOKENS_THREE;
    }

    /**
     * Do a deterministic and a non-deterministic advance step
     * @param action    the (deterministic) action
     * @param cmpRand   if non-null, use this (reproducible) RNG instead of StateObservation's RNG
     */
    @Override
    public void advance(ACTIONS action, Random cmpRand) {
        super.advanceBase(action);		//		includes addToLastMoves(action)
        if(isNextActionDeterministic) {
            if(action != null) {
                //count = 0;
                advanceDeterministic(action);
            }
            else isNextActionDeterministic = false;
        }
        if(!isNextActionDeterministic) advanceNondeterministic(cmpRand);
        super.incrementMoveCounter();   // increment m_counter
    }

    /**
     * Advancing step, the deterministic part
     * <p>
     * For example, if we get action 706:
     * <ol>
     *     <li>Convert the action to fields (e.g. 706 to fields [from,to] &rArr; [7,6], see {@link Helper#getIntsFromAction(ACTIONS)})
     *     <li>Split the indices of the array to their from and to coordinates
     *     <li>If to_token is a players token remove it
     *     <li>Change the {@code gameState}
     *     <li>Update {@code player}'s tokens
     * </ol>

     * @param action    the action
     */
    @Override
    public void advanceDeterministic(ACTIONS action){
        if(!(action.toInt() == -1)){
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
            gameState[from_y][from_x] = new Token(from_y, from_x, -1, -1); // Remove old token
        }
        //turn++;
        boolean isOver = this.isGameOver();
        if(!isOver){
            this.player = (this.player + 1) % numPlayers; //next player
            availableActions.clear();
        }
        isNextActionDeterministic = false;      // /WK/ this was missing, I think
    }

    @Override
    public ACTIONS advanceNondeterministic(Random cmpRand){
        if(isNextActionDeterministic){
            throw new RuntimeException("ACTION IS DETERMINISTIC must be NON");
        }
        int actIndex;
        if (cmpRand==null) {
            actIndex = ThreadLocalRandom.current().nextInt(availableRandomActions.size());
        } else {
            actIndex = cmpRand.nextInt(availableRandomActions.size());
        }
        advanceNondetSpecific(availableRandomActions.get(actIndex));  // sets isNextActionDeterministic, nextNondeterministicAction
        if(this.availableActions.size() == 0){
            this.availableActions.add(new ACTIONS(-1)); // Add empty action
        }
        return nextNondeterministicAction;
    }



    @Override
    public ACTIONS advanceNondetSpecific(ACTIONS action) {
        nextNondeterministicAction = action;
        this.setAvailableActions();
        this.isNextActionDeterministic = true;

        return action;
    }

    private int getRandomActionSize(int size, int numPlayers){
        return switch (numPlayers) {
            case 2 -> size == 5 ? 6 : 3;
            case 3 -> 6;
            default -> size == 6 ? 6 : 3;
        };
    }

    private int[][][] getStartingPositions(){
        return switch (numPlayers) {
            case 2 -> switch (ConfigEWN.BOARD_SIZE) {
                case 3 -> StartingPositions.S3P2;
                case 4 -> StartingPositions.S4P2;
                case 5 -> StartingPositions.S5P2;
                default -> throw new RuntimeException("ConfigEWN.BOARD_SIZE=" + ConfigEWN.BOARD_SIZE +
                        " not allowed for numPlayers=" + numPlayers);
            };
            //return ConfigEWN.BOARD_SIZE == 3 ? StartingPositions.S3P2 : ConfigEWN.BOARD_SIZE == 4 ? StartingPositions.S4P2 : StartingPositions.S5P2;
            case 3 -> StartingPositions.S6P3;
            default ->    // numPlayers == 4
                    switch (ConfigEWN.BOARD_SIZE) {
                        case 4 -> StartingPositions.S4P4;
                        case 6 -> StartingPositions.S6P4;
                        default -> throw new RuntimeException("ConfigEWN.BOARD_SIZE=" + ConfigEWN.BOARD_SIZE +
                                " not allowed for numPlayers=" + numPlayers);
                    };
            // return ConfigEWN.BOARD_SIZE == 6 ? StartingPositions.S6P4 : StartingPositions.S4P4;
        };
    }


    /**
     * Reset {@link StateObserverEWN} object {@code this}
     * @param cmpRand       if non-null, use this (reproducible) RNG instead of StateObservation's RNG
     * @return {@code this} (with new random dice value and potential new token config)
     */
    public StateObserverEWN reset(Random cmpRand){
        init(cmpRand);
        return this;
    }

    @Override
    public boolean needsRandomization() { return true; }

    /**
     * Randomize the start state (roll the dice). Used in
     * {@link XArenaFuncs#competeNPlayer(PlayAgtVector, int, StateObservation, int, int, TSTimeStorage[], ArrayList, Random, boolean)
     * competeNPlayer(..)} if {@link #needsRandomization()} returns true
     *
     * @param cmpRand	if non-null, use this (reproducible) RNG instead of StateObservation's RNG
     */
    @Override
    public void randomizeStartState(Random cmpRand){
        reset(cmpRand); // roll the dice and setAvailableActions when we start a new episode
    }



    /**
     * Returns all available actions, in sorted order. Actions are coded as {@code from*100 + to} integers, where
     * {@code from} and {@code to} are two fields on the board that are <ul>
     *     <li>not equal and</li>
     *     <li>adjacent</li>
     * </ul>
     * @return the {@link ArrayList} of all actions
     */
    @Override
    public ArrayList<ACTIONS> getAllAvailableActions() {
        ArrayList<ACTIONS> ar = new ArrayList<>();
        int fx,fy,tx,ty;
        //for(int from = 0; from <size*size; from++){
        for(int from = size*size-1; from >= 0; from--){
            for(int to = 0; to < size*size; to++){
                if (from != to) {
                    tx = to % size;
                    ty = (to - tx) / size;
                    fx = from % size;
                    fy = (from - fx) / size;
                    if (Math.abs(tx-fx)<=1 && Math.abs(ty-fy)<=1) {
                        ar.add(Helper.parseAction(from,to));
                    }
                }
            }
        }
        Collections.sort(ar);
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

    /**
     * Helper for test routines
     * @param ranAct
     */
    public void setNextActionDeterministic(ACTIONS ranAct) {
        nextNondeterministicAction = ranAct;
        setAvailableActions();
    }

    @Override
    public void setAvailableActions() {
        Player p = players.get(player);
        p.setAvailableActions(nextNondeterministicAction.toInt());
        this.availableActions = (ArrayList<ACTIONS>) p.getAvailableActions().clone();
        // /WK/05/2023/: added .clone() for safety (this.availableActions should not point to another object)
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

    private Types.WINNER getWinner2(int player) {
        if(playerWin >= 0) return playerWin == player ? PLAYER_WINS : PLAYER_LOSES;
        return playerLoses == player ? PLAYER_LOSES : PLAYER_WINS;

    }

    private Types.WINNER getWinner3(int player) {
            // Can only be player 2
            if(playerWin >= 0) {
                return player == 2 ? PLAYER_WINS : PLAYER_LOSES;
            }
            if(playerLoses >= 0){
                // if playerLooses == 2
                if(playerLoses == 2){
                    return player == 2 ? PLAYER_LOSES : PLAYER_WINS;
                }
                return player == 2 ? PLAYER_WINS : PLAYER_LOSES;
            }
            throw new RuntimeException("There must be a winner");
    }


    private Types.WINNER getWinner4(int player) {
        if(playerWin >= 0){
            // Check if the player is the winner
            if((playerWin == 0 || playerWin == 2) && (player == 0 || player == 2)){
                return PLAYER_WINS;
            }
            if((playerWin == 1 || playerWin == 3) && (player == 1 || player == 3))
            {
                return PLAYER_WINS;
            }
            return PLAYER_LOSES;
        }
        else if(playerLoses >= 0){

            if((playerLoses == 0 || playerLoses == 2) && (player == 0 || player == 2)){
                return PLAYER_LOSES;
            }
            if((playerLoses == 1 || playerLoses == 3) && (player == 1 || player == 3))
            {
                return PLAYER_LOSES;
            }
            return PLAYER_WINS;
        }
        throw new RuntimeException("There must be a winner");
    }

    /**
     * Returns the winning condition of {@code player}, either PLAYER_WINS (+1) or PLAYER_LOSES (-1).
     * Should be only called, if game is over.
     */
    private Types.WINNER getWinner(int player){
        return switch (ConfigEWN.NUM_PLAYERS) {
            case 2 -> getWinner2(player);
            case 3 -> getWinner3(player);
            case 4 -> getWinner4(player);
            default -> throw new RuntimeException("Number of players: " + player + " no allowed");
        };
    }


    @Override
    public ScoreTuple getGameScoreTuple() {
        for(int i = 0; i < ConfigEWN.NUM_PLAYERS; i++){
            m_scoreTuple.scTup[i] = getGameScore(i);
        }
        return m_scoreTuple;
    }

    @Override
    public double getGameScore(int player) {
        if(isGameOver()) {
            Types.WINNER winState = getWinner(player);
            switch (winState){
                case PLAYER_LOSES:
                    return REWARD_NEGATIVE;
                case PLAYER_WINS:
                    return REWARD_POSITIVE;
                case TIE:  throw new RuntimeException("invalid outcome of the game [EWN cannot end in a tie]");
            }
        }
        return 0.0;
    }


    @Override
    public double getReward(int player, boolean rewardIsGameScore) {
        // Check for the player
        return getGameScore(player);
    }

    @Override
    public double getMinGameScore() {
        return -1;
    }

    @Override
    public double getMaxGameScore() {
        return +1;
    }

    // --- obsolete, use Arena().getGameName() instead
//    public String getName() {return "EWN";}

    @Override
    public String stringDescr() {
        StringBuilder str = new StringBuilder("\n");
        for(int i = 0; i < gameState.length; i++){
            for(int k = 0; k < gameState[i].length; k++){
                Token t = gameState[i][k];
                int p = t.getPlayer();
                str.append(p == 0 ? "[X" : p == 1 ? "[O" : p == 2 ? "[*" : p == 3 ? "[#" : "[ ");
                // note that the t.getValue() of non-empty fields is one smaller than the piece value displayed in GameBoard
                String val = String.valueOf(t.getValue());
                str.append(t.getValue() > -1 ? val + "]" : " ]").append(" ");
            }
            if (i==0) {
                // note that diceVal is one smaller than the "Dice: " displayed in GameBoard
                DecimalFormat frmAct = new DecimalFormat("0000");
                str.append("    (diceVal:").append(this.getNextNondeterministicAction().toInt()).append(",   ");
                str.append("availActions:  ");
                for (ACTIONS act : this.getAvailableActions())
                    str.append(frmAct.format(act.toInt())).append(" ");
                str.append(")");
            }
            str.append("\n");
        }
        str.append("\n");
        return str.toString();
    }

    /**
     * Method {@link #stringDescr()} is not unique since it omits the player to move. AND the 'availActions' string can
     * lead to different strings for the same state (depending on whether {@link #getAvailableActions()} returns
     * {@code null} or not). With {@code uniqueStringDescr()} we return a unique string for each state.
     *
     * @return a unique string description of the state
     *
     * @see ExpectimaxNAgent
     */
    @Override
    public String uniqueStringDescr() {
        StringBuilder str = new StringBuilder("\n");
        for(int i = 0; i < gameState.length; i++){
            for(int k = 0; k < gameState[i].length; k++){
                Token t = gameState[i][k];
                int p = t.getPlayer();
                str.append(p == 0 ? "[X" : p == 1 ? "[O" : p == 2 ? "[*" : p == 3 ? "[#" : "[ ");
                // note that the t.getValue() of non-empty fields is one smaller than the piece value displayed in GameBoard
                String val = String.valueOf(t.getValue());
                str.append(t.getValue() > -1 ? val + "]" : " ]").append(" ");
            }
            if (i==0) {
                // note that diceVal is one smaller than the "Dice: " displayed in GameBoard
                str.append("    (diceVal:").append(this.getNextNondeterministicAction().toInt()).append(",   ");
                str.append("player:  ").append(this.getPlayer());
                str.append(")");
            }
            str.append("\n");
        }
        str.append("\n");
        return str.toString();
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
        return 1.0/availableRandomActions.size();
    }

    @Override
    public void setPlayer(int p ){
        this.player = p;
    }

    @Override
    public StateObsNondeterministic copy() {
        return new StateObserverEWN(this);
    }



    /**
     * Game Over conditions
     *
     * 2 or 4 Player:
     *  One player does not have any tokens left.
     *  One Token reaches the opposite corner
     *
     * 3 Player:
     *  One player does not have any tokens left
     *  The 2-player team needs to capture all tokens of the single-team player
     *
     *
     * @return true if game is over, false otherwise
     */
    @Override
    public boolean isGameOver() {
        return switch (numPlayers) {
            case 2 -> gameOverTwoPlayer();
            case 3 -> gameOverThreePlayer();
            case 4 -> gameOverFourPlayer();
            default -> throw new RuntimeException("numPlayer " + numPlayers + " is not not implemented yet");
        };
    }



    private boolean gameOverFourPlayer(){
            int[] winningPositions = {size*size-1, 0,size-1,(size*size)-size};
            //Check for winning position
            for(Player p : players){
                int winPosition = winningPositions[p.getPlayer()]; // get the corresponding item of the array
                for(Token t: p.getTokens()){
                    if(t.getIndex() == winPosition){
                        playerWin = p.getPlayer();
                        return true;
                    }
                }
                if(p.getTokens().size() == 0)
                {
                    playerLoses = p.getPlayer();
                    return true;
                }
            }

            return false;
        }

    private boolean gameOverThreePlayer(){
        int winPosition = ConfigEWN.BOARD_SIZE-1;
        for (Token t : players.get(2).getTokens()) {
            if (t.getIndex() == winPosition) {
                playerWin = 2;
                return true;
            }
        }
        for(Player p : players) {
            if (p.getTokens().size() == 0) {
                playerLoses = p.getPlayer();
                return true;
            }
        }
        return false;
    }

    private boolean gameOverTwoPlayer(){
        int[] winningPositions = {size*size-1,0};
        for (Player p : players) {
                            // game is over, if any token of player p reaches p's winning position (p has won):
            for (Token t : p.getTokens()) {
                if (t.getIndex() == winningPositions[t.getPlayer()]) {
                    playerWin = p.getPlayer();
                    return true;
                }
            }
                                // game is over, if player p has no more tokens (p has lost):
            if (p.getTokens().size() == 0) {
                playerLoses = p.getPlayer(); // setting player for getWinner()
                return true;
            }

        }
                                // game is not yet over:
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

    public int getPlayerLoses(){
        return playerLoses;
    }

    public ScoreTuple getM_scoreTuple(){
        return this.m_scoreTuple;
    }
}

