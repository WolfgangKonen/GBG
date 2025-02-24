package ludiiInterface.general;

import controllers.PlayAgent;
import game.Game;
import games.Hex.HexConfig;
import games.Nim.NimConfig;
import games.Othello.ArenaOthello;
import ludiiInterface.Util;
import ludiiInterface.games.CFour.StateObserverC4TranslationLayer;
import ludiiInterface.games.CFour.SystemConversionC4;
import ludiiInterface.games.Hex.StateObserverHexTranslationLayer;
import ludiiInterface.games.Hex.SystemConversionHex;
import ludiiInterface.games.Nim.StateObserverNimTranslationLayer;
import ludiiInterface.games.Nim.SystemConversionNim;
import ludiiInterface.games.othello.StateObserverOthelloTranslationLayer;
import ludiiInterface.games.othello.SystemConversionOthello;
import ludiiInterface.games.yavalath.StateObserverYavalathTranslationLayer;
import ludiiInterface.games.yavalath.SystemConversionYavalath;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.move.Move;
import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static ludiiInterface.Util.loadFileFromDialog;

public class GBGAsLudiiAI extends AI {

    private PlayAgent gbgAgent;
    private int playerID, size, players;
    private String gbgAgentPathYavalath = "C:\\Users\\Ann\\IdeaProjects\\GBG\\agents\\Yavalath\\yavAgent.agt.zip";
    private final String gbgAgentPathOthello = "C:\\Users\\Ann\\IdeaProjects\\GBG\\agents\\Othello\\TCL3-100_7_250k-lam05_P4_nPly2-FAm_A.agt.zip";
    private final String gbgAgentPathHex = "C:\\Users\\Ann\\IdeaProjects\\GBG\\agents\\Hex\\06\\TDNT3-TCLid-25_6-300k-eps02.agt.zip";
    private final String[] games = {"Othello", "Yavalath", "Hex", "Connect Four", "Nim"};
    private String agentPath;
    private List<Move> movesNim;

    public GBGAsLudiiAI(){
        friendlyName = getClass().getSimpleName();
    }

    public GBGAsLudiiAI(int size, int players){
        this.size = size;
        this.players = players;
        friendlyName = getClass().getSimpleName();
        this.movesNim = new ArrayList<>();
    }

    /**
     * Loads the agent that is supposed to play either via a direct path or by opening a file dialog to choose from.
     * @param game
     * @param playerID
     */
    @Override
    public void initAI(final Game game, final int playerID){
        this.playerID = playerID;

        //Load the agent either via file dialog, or directly via a specified string (useful if you're only using the same agent for a while)
        try{
            if(agentPath == null){
                agentPath = loadFileFromDialog("GBG Agenten auswählen");
                System.out.println(agentPath);
            }
            gbgAgent = new ArenaOthello(
                    "GBG vs. Ludii",
                    false
            ).tdAgentIO.loadGBGAgent(
                    agentPath
                    //gbgAgentPathOthello
            );
        } catch( final Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Loads the agent that is supposed to play from param {@code pa}
     * @param game
     * @param playerID
     * @param pa
     */
    public void initAI(final Game game, final int playerID, PlayAgent pa){
        this.playerID = playerID;
        this.gbgAgent = pa;
    }

    /**
     * Selects an action based on the game being played. If the game is not part of the switch cases, it issues a warning
     * and returns a random move from the (possible) moves list.
     * The Ludii limitations maxSeconds, maxIterations and maxDepth are ignored as of now.
     * @return The move the agent wants to make.
     */
    @Override
    public Move selectAction(Game game, Context context, double maxSeconds, int maxIterations, int maxDepth) {
        Optional<Move> move;


        switch(game.name()){
            case "Reversi" -> {
                move = selectActionOthello(game,context,maxSeconds,maxIterations,maxDepth,playerID);
            }
            case "Yavalath" -> {
                move = selectActionYavalath(game,context,maxSeconds,maxIterations,maxDepth);
            }
            case "Hex" -> {
                move = selectActionHex(game,context,maxSeconds,maxIterations,maxDepth);
            }
            case "Connect Four" -> {
                move = selectActionC4(game,context,maxSeconds,maxIterations,maxDepth);
            }
            case "Nim" -> {
                if(movesNim.isEmpty()) selectActionNim(game, context, maxSeconds, maxIterations, maxDepth);

                move = Optional.ofNullable(movesNim.get(0));
                movesNim.remove(0);
            }

            default -> {
                // Default to random move
                System.err.println("[GBGAsLudiiAI.selectAction] Returning a random move, since game is not part of interface!");
                int numMoves = game.moves(context).moves().size();
                Random r = new Random();
                return game.moves(context).moves().get(r.nextInt(numMoves-1));
            }
        }
        assert move.isPresent(): "No move selected";
        return move.get();
    }

    /**
     * First checks if the agent can make any moves or has to pass. Otherwise, creates as StateObserverOthello from the
     * Ludii context, so the agent can select an action. Then translates that action back into a Ludii move.
     */
    private Optional<Move> selectActionOthello(Game game, Context context, double maxSeconds, int maxIterations, int maxDepth, int playerID){
        Optional<Move> returnMove = Optional.empty();
        SystemConversionOthello index = new SystemConversionOthello();
        FastArrayList<Move> moves = game.moves(context).moves();

        if(moves.size() == 0 ||
                (game.moves(context).moves().size() == 1 && game.moves(context).moves().get(0).actions().get(0).to() == -1)){
            returnMove= Optional.of(Game.createPassMove(context, true));
        } else {

            StateObserverOthelloTranslationLayer soo2 = new StateObserverOthelloTranslationLayer(context, playerID);

            Types.ACTIONS gbgAction = gbgAgent.getNextAction2(soo2.partialState(), false, false, true);
            for (Move move : moves) {
                if (move.to() == index.getLudiiIndexFromGBG(gbgAction.toInt())) returnMove = Optional.of(move);
            }
        }
        return returnMove;

    }

    /**
     * Translates the current Ludii context into a StateObserverYavalath that the agent then uses to choose its next action.
     * Then translates that action back into a Ludii move.
     */
    private Optional<Move> selectActionYavalath(Game game, Context context, double maxSeconds, int maxIterations, int maxDepth){
        Optional<Move> returnMove = Optional.empty();

        SystemConversionYavalath index = new SystemConversionYavalath();
        Types.ACTIONS gbgAction = gbgAgent.getNextAction2(new StateObserverYavalathTranslationLayer(context, playerID).partialState(),false, false, true);

        FastArrayList<Move> moves = game.moves(context).moves();
        for(Move move : moves){
            if(move.to() == index.getLudiiIndexFromGBG(gbgAction.toInt())) returnMove = Optional.of(move);
        }

        return returnMove;

    }

    /**
     * Not fully functional yet.
     */
    private Optional<Move> selectActionHex(Game game,Context context,double maxSeconds, int maxIterations, int maxDepth){

        //Detects and sets board size
        for(String s : game.getOptions()){
            if(s.contains("Board Size")){
                int boardSize = Character.getNumericValue(s.charAt(11));
                HexConfig.BOARD_SIZE = boardSize;
                size = boardSize;
            }
        }

        Optional<Move> returnMove = Optional.empty();

        SystemConversionHex conversion = new SystemConversionHex(size);
        StateObserverHexTranslationLayer sohex = new StateObserverHexTranslationLayer(context,playerID,size);

        Types.ACTIONS gbgAction =null;
        try {
            gbgAction = gbgAgent.getNextAction2(sohex,false, false, true);
        } catch (Exception e){
            Util.errorDialog(e);
        }

        FastArrayList<Move> moves = game.moves(context).moves();
        for(Move move : moves){
            if(move.to() == conversion.getLudiiIndexFromGBG(gbgAction.toInt())) returnMove = Optional.of(move);
        }
        return returnMove;
    }

    /**
     * Translates the current Ludii context into a StateObserverC4 that the agent then uses to choose its next action.
     * Then translates that action back into a list of Ludii moves.
     */
    private Optional<Move> selectActionC4(Game game, Context context, double maxSeconds, int maxIterations, int maxDepth){
        Optional<Move> returnMove = Optional.empty();
        SystemConversionC4 conversion = new SystemConversionC4();

        FastArrayList<Move> moves = game.moves(context).moves();
        List<Move> moveList = context.trial().generateCompleteMovesList();
        int gbgAction = 0;

      /*  try{
            gbgAction = gbgAgent.getNextAction2(new StateObserverC4TranslationLayer(context, playerID).partialState(), false, true).toInt();
        } catch (Exception e){
            if(moveList.get(moveList.size()-1).isPass()){
                gbgAction = -1;
            } else{
                StateObserverC4TranslationLayer obs = new StateObserverC4TranslationLayer(context,playerID);
                if(obs.isGameOver()){
                    gbgAction = -1;
                }
            }
        }*/

        // checks if last made move was a pass
        // needed because of ties/draws; when a draw happens, Ludii requires both players to make a pass move before it ends the game)
        if(!moveList.isEmpty() && moveList.get(moveList.size()-1).isPass()){
            gbgAction = -1; // GBG doesn't have pass moves for C4, so just assign it without consulting gbgAgent
        } else {
            // gbgAgent might realize there's no available action in case of a draw and throw an exception
            try{
                gbgAction = gbgAgent.getNextAction2(new StateObserverC4TranslationLayer(context, playerID).partialState(), false, false, true).toInt();
            } catch( Exception e){
                // check whether exception was caused by a draw and assign a pass move to gbgAction
                StateObserverC4TranslationLayer obs = new StateObserverC4TranslationLayer(context,playerID);
                if(obs.isGameOver()){
                    gbgAction = -1;
                }
            }
        }
        for(Move move : moves){
            if(move.to() == conversion.getLudiiIndexFromGBG(gbgAction)) returnMove = Optional.of(move);
        }

        return returnMove;
    }

    /**
     * Translates the current Ludii context into a StateObserverNim that the agent then uses to choose its next action.
     * Then translates that action back into a list of Ludii moves.
     */
    private void selectActionNim(Game game, Context context, double maxSeconds, int maxIterations, int maxDepth) {

        int numberHeaps = 0;

        //Detects and sets number of heaps
        for (String s : game.getOptions()) {
            if (s.contains("Number Piles")) {

                numberHeaps = Character.getNumericValue(s.charAt(13));

                // checking if number of heaps has two digits and the second digit was cut off by charAt()
                if (numberHeaps == 1)
                    numberHeaps = Integer.parseInt(s.substring(13, 15)); // if so, replace numberHeaps with correct two digit integer

                NimConfig.NUMBER_HEAPS = numberHeaps;
                NimConfig.HEAP_SIZE = -1;
                NimConfig.MAX_MINUS = numberHeaps;
            }
        }

        SystemConversionNim conversion = new SystemConversionNim(numberHeaps);
        StateObserverNimTranslationLayer sobTNim = new StateObserverNimTranslationLayer(context, playerID, numberHeaps);
        FastArrayList<Move> moves = game.moves(context).moves();

        Types.ACTIONS gbgAction = gbgAgent.getNextAction2(sobTNim.partialState(), false, false, true);
        int moveRep = (gbgAction.toInt() % NimConfig.MAX_MINUS) + 1;  // calculates how many objects are taken off heap

        for (Move move : moves) {
            if (move.to() == ((gbgAction.toInt() - (gbgAction.toInt() % numberHeaps)) / numberHeaps)) {
                try {
                    if (move.to() == conversion.getLudiiIndexFromGBG(gbgAction.toInt())) { // check if a mapping is available for the GBG move, i.e. if GBG player wants to take only one item off heap
                        movesNim.add(move);
                    }
                } catch (Exception e) { // if no mapping available, then add move as many times as necessary to take wanted amount off heap
                    for (int i = 0; i < moveRep; i++) {
                        movesNim.add(move);
                    }
                }
            }
        }

        int[] testHeap = sobTNim.getHeaps();

        int j = gbgAction.toInt()%NimConfig.MAX_MINUS;
        int heap = (gbgAction.toInt()-j)/NimConfig.MAX_MINUS;
        int subtractor = j+1;

        testHeap[heap] -= subtractor;

        if(testHeap[heap] != 0) // checks if heap will be empty after action is performed, if it's empty, Ludii ends GBG players turn on its own
            movesNim.add(Game.createPassMove(context, true)); // if not, add pass move at end of the list to signal end of the GBG player's turn after move is complete

        assert !movesNim.isEmpty() : "List is Empty!!";
    }

}
