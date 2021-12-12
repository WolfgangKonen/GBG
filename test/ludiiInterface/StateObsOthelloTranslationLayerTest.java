package ludiiInterface;

import game.Game;
import ludiiInterface.games.othello.StateObserverOthelloTranslationLayer;
import ludiiInterface.games.othello.SystemConversionOthello;
import main.collections.FastArrayList;
import org.junit.Test;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.move.Move;
import other.trial.Trial;
import tools.Types;
import utils.LudiiAI;

import java.util.ArrayList;
import java.util.List;

public class StateObsOthelloTranslationLayerTest {

    static final AI player1 = new LudiiAI();
    static final AI player2 = new LudiiAI();

    static StateObserverOthelloTranslationLayer sobs;


    /**
     * Plays a game of fixed moves with a known outcome. Then checks if the state observer has been translated correctly
     * by comparing the outcome with what it should be.
     */
    @Test
    public void playFixedGame(){
        final var ludiiGame = GameLoader.loadGameFromName("Reversi.lud");
        final Context context = new Context(ludiiGame, new Trial(ludiiGame));

        ludiiGame.start(context);

        //We need to add a -1 at the end of the moves list because of the pass at the end of the game
        int[] moves = {43, 26, 21, 29, 19, 44, 22, 37, 42, 18, 33, 25, 53, 34, 16, 20, 11, 30, 46, 45, 23, 38, 17,
                47, 54, 39, 31, 15, 14, 52, 60, 63, 55, 62, 7, 61, 59, 51, 12, 3, 4, 13, 2, 10, 1, 9, 0, 8, 58, 50, 57, 49, 56, 48, 40, 41, 32, 24, 5, 6,-1,
        };

        //Iterates over the moves list and applies them
        for(int move : moves){
            FastArrayList<Move> possibleMoves = context.moves(context).moves();

            //If the only possible move is a pass move, we need to pass
            if(possibleMoves.get(0).isPass()){
                ludiiGame.apply(context, Game.createPassMove(context,true));
                possibleMoves = context.moves(context).moves();
            }
            for(Move ludiiMove : possibleMoves)
                if(ludiiMove.to() == move){
                    //Apply the matching move directly to the game object
                    ludiiGame.apply(context,ludiiMove);
                }
        }

        sobs = new StateObserverOthelloTranslationLayer(context,1);

        assert sobs.isGameOver();
        assert sobs.winStatus() == Types.WINNER.PLAYER_WINS;
        assert sobs.getCountWhite() == 30;
        assert sobs.getCountBlack() == 34;
    }


    /**
     * Plays a random game of Othello between 2 Ludii agents, then checks if the state observer has been initialized correctly
     * by checking if information between it and the Ludii context matches.
     */
    @Test
    public void playRandomGame(){

        final var ludiiGame = GameLoader.loadGameFromName("Reversi.lud");
        final Context context = new Context(ludiiGame, new Trial(ludiiGame));

        final List<AI> ais = new ArrayList<AI>();
        ais.add(null);
        ais.add(player1);
        ais.add(player2);

        ludiiGame.start(context);

        for (int i = 1; i < ais.size(); i++) {
            ais.get(i).initAI(ludiiGame,i);
        }

        final Model model = context.model();

        while (!context.trial().over()){
            model.startNewStep(context,ais,1);
        }
        sobs = new StateObserverOthelloTranslationLayer(context,context.trial().status().winner());
        //Set current player to the winner
        sobs.setPlayer(new SystemConversionOthello().getGBGPlayerFromLudii(context.trial().status().winner()));

        //Game should be over
        assert sobs.isGameOver();

        int scoreBlack = context.score(1);
        int scoreWhite = context.score(2);

        //Scores between Ludii and GBG SO should match
        assert sobs.getCountBlack() == scoreBlack;
        assert sobs.getCountWhite() == scoreWhite;

        //We set the winner as the current player in the state observer, so win status should be player_wins
        assert sobs.winStatus() == Types.WINNER.PLAYER_WINS;
    }
}
