package ludiiInterface.general;

import controllers.PlayAgent;
import game.Game;
import games.Hex.HexConfig;
import games.Othello.ArenaOthello;
import ludiiInterface.Util;
import ludiiInterface.games.Hex.StateObserverHexTranslationLayer;
import ludiiInterface.games.Hex.SystemConversionHex;
import ludiiInterface.games.othello.StateObserverOthelloTranslationLayer;
import ludiiInterface.games.othello.SystemConversionOthello;
import ludiiInterface.games.yavalath.StateObserverYavalathTranslationLayer;
import ludiiInterface.games.yavalath.SystemConversionYavalath;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.move.Move;
import tools.Types;

import javax.swing.*;
import java.util.Optional;
import java.util.Random;

import static ludiiInterface.Util.loadFileFromDialog;

public class GBGAsLudiiAI extends AI {

    private PlayAgent gbgAgent;

    /**
     * -1 = uninitialized, needed for game selection in Ludii UI
     * 0 = Othello
     * 1 = Yavalath
     * 2 = Hex
     **/
    private int gameID = -1;
    private int playerID, size, players;
    private String gbgAgentPathYavalath = "C:\\Users\\Ann\\IdeaProjects\\GBG\\agents\\Yavalath\\yavAgent.agt.zip";
    private final String gbgAgentPathOthello = "C:\\Users\\Ann\\IdeaProjects\\GBG\\agents\\Othello\\TCL3-100_7_250k-lam05_P4_nPly2-FAm_A.agt.zip";
    private final String gbgAgentPathHex = "C:\\Users\\Ann\\IdeaProjects\\GBG\\agents\\Hex\\06\\TDNT3-TCLid-25_6-300k-eps02.agt.zip";
    private final String[] games = {"Othello", "Yavalath", "Hex"};
    private String agentPath;

    public GBGAsLudiiAI(){
        friendlyName = getClass().getSimpleName();
    }

    public GBGAsLudiiAI(int gameID){
        this.gameID = gameID;
        friendlyName = getClass().getSimpleName();
    }

    public GBGAsLudiiAI(int gameID, int size, int players){
        this.gameID = gameID;
        this.size = size;
        this.players = players;
        friendlyName = getClass().getSimpleName();
    }

    /**
     * Sets the gameID if it has not been specified in the constructor. Then loads the agent that is supposed to play
     * either via a direct path or by opening a file dialog to choose from.
     * @param game
     * @param playerID
     */
    @Override
    public void initAI(final Game game, final int playerID){
        this.playerID = playerID;
        if(gameID == -1){
            JComboBox comboBox = new JComboBox(games);
            comboBox.setSelectedIndex(0);
            JOptionPane.showMessageDialog(null, comboBox,"Choose a game", JOptionPane.QUESTION_MESSAGE);
            switch ((String) comboBox.getSelectedItem()){
                case "Othello" -> gameID = 0;
                case "Yavalath" -> gameID = 1;
                case "Hex" -> {
                    gameID = 2;
                    size = 6;
                    HexConfig.BOARD_SIZE = 6;
                }
            }
        }

        //Load the agent either via file dialog, or directly via a specified string (useful if you're only using the same agent for a while)
        try{
            if(agentPath == null){
                agentPath = loadFileFromDialog("GBG Agenten auswählen");
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
     * Selects an action based on the game being played. If no game is selected returns a random move from the (possible) moves list.
     * The Ludii limitations maxSeconds, maxIterations and maxDepth are ignored as of now.
     * @return The move the agent wants to make.
     */
    @Override
    public Move selectAction(Game game, Context context, double maxSeconds, int maxIterations, int maxDepth) {
        Optional<Move> move;


        switch(gameID){
            case 0 -> {
                move = selectActionOthello(game,context,maxSeconds,maxIterations,maxDepth,playerID);
            }
            case 1 -> {
                move = selectActionYavalath(game,context,maxSeconds,maxIterations,maxDepth);
            }
            case 2 -> {
                move = selectActionHex(game,context,maxSeconds,maxIterations,maxDepth);
            }
            default -> {
                // Default to random move
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

            Types.ACTIONS gbgAction = gbgAgent.getNextAction2(soo2.partialState(), false, true);
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
        Types.ACTIONS gbgAction = gbgAgent.getNextAction2(new StateObserverYavalathTranslationLayer(context, playerID).partialState(),false, true);

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

        Optional<Move> returnMove = Optional.empty();

        SystemConversionHex conversion = new SystemConversionHex(size);
        StateObserverHexTranslationLayer sohex = new StateObserverHexTranslationLayer(context,playerID,size);

        Types.ACTIONS gbgAction =null;
        try {
            gbgAction = gbgAgent.getNextAction2(sohex,false,true);
        } catch (Exception e){
            Util.errorDialog(e);
        }

        FastArrayList<Move> moves = game.moves(context).moves();
        for(Move move : moves){
            if(move.to() == conversion.getLudiiIndexFromGBG(gbgAction.toInt())) returnMove = Optional.of(move);
        }
        return returnMove;
    }
}
