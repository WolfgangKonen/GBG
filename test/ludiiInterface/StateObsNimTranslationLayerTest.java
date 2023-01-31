package ludiiInterface;

import ludiiInterface.games.Nim.StateObserverNimTranslationLayer;
import ludiiInterface.games.Nim.SystemConversionNim;
import main.collections.FastArrayList;
import org.junit.Test;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.move.Move;
import other.trial.Trial;
import utils.LudiiAI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class StateObsNimTranslationLayerTest {
    static final AI player1 = new LudiiAI();
    static final AI player2 = new LudiiAI();

    static StateObserverNimTranslationLayer sobs;

    /**
     * Plays a game of fixed moves with a known outcome. Then checks if the state observer has been translated
     * correctly by comparing its outcome with the expected one.
     * */
    @Test
    public void playFixedGame(){
        List<String> options = Arrays.asList("Number Piles/5", "End Rules/Last Mover Wins");
        final var ludiiGame = GameLoader.loadGameFromName("Nim.lud", options);
        final Context context = new Context(ludiiGame, new Trial(ludiiGame));

        ludiiGame.start(context);

        context.trial().reset(ludiiGame); // needed because context makes moves on its own after being initialized

        int[] moves = {1,1,1,1,-1,3,3,3,-1,3,-1,2,2,2,2,-1,0,0,-1,4,4,-1,0,-1,2,-1,4}; //version in which Player 1 is the winner
        //int[] moves = {1,1,1,1,-1,3,3,3,-1,3,-1,2,2,2,2,-1,0,0,-1,4,4,4,-1,0,-1,2}; //version in which P2 is the winner

        //iterates over moves list and applies the moves
        for(int move : moves){
            FastArrayList<Move> possibleMoves = context.moves(context).moves();


            for(Move ludiiMove : possibleMoves){
                if(ludiiMove.to() == move){
                    //System.out.println("APPLYING MOVE: " + move);
                    ludiiGame.apply(context,ludiiMove);
                }
            }

        }

        sobs = new StateObserverNimTranslationLayer(context,1, 5);

        assert sobs.isGameOver();
        assert sobs.win();

        // Heaps should be initialized correctly and have no items left after the game
        System.out.println(sobs.stringDescr());
        assert sobs.getHeaps().length == 5;

        assert sobs.getGameScore(0) == 1; // Player 1 (X) has won
        //assert sobs.getGameScore(1) == 1; for version in which P2 is the winner
    }


    /**
     * Checks if the state observer has been initialized correctly.
     * Plays random game of Nim between 2 Ludii agents and compares whether information
     * between the Ludii context and the state observer matches. */
    @Test
    public void playRandomGame(){
        List<String> options = Arrays.asList("Number Piles/5", "End Rules/Last Mover Wins");
        final var ludiiGame = GameLoader.loadGameFromName("Nim.lud", options);
        final Context context = new Context(ludiiGame, new Trial(ludiiGame));
        SystemConversionNim conversion = new SystemConversionNim(5);

        final List<AI> ais = new ArrayList<AI>();
        ais.add(null);
        ais.add(player1);
        ais.add(player2);

        ludiiGame.start(context);

        context.trial().reset(ludiiGame); // needed because context makes moves on its own after being initialized

        for(int i = 1; i < ais.size(); i++){
            ais.get(i).initAI(ludiiGame,i);
        }

        final Model model = context.model();

        while(!context.trial().over()){
            model.startNewStep(context, ais, 1);
        }

        sobs = new StateObserverNimTranslationLayer(context,context.trial().status().winner(),5);

        assert sobs.isGameOver();
        assert sobs.win();

        //System.out.println(sobs.stringDescr());

        // compare if the first player (X) is winner from both GBG and Ludii perspective
        // else compare if the second player (O) is winner for both
        if(sobs.getGameScore(0) == 1){
            assertEquals(context.trial().status().winner(), conversion.getLudiiPlayerFromGBG(0));// Spieler X
            System.out.println("Spieler X hat gewonnen!");
        }
        else {
            assertEquals(context.trial().status().winner(), conversion.getLudiiPlayerFromGBG(1)); // Spieler O
            System.out.println("Spieler O hat gewonnen!");
        }

    }
}
