package ludiiInterface;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import ludiiInterface.games.CFour.StateObserverC4TranslationLayer;
import ludiiInterface.games.CFour.SystemConversionC4;
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
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StateObsC4TranslationLayerTest {
    static final AI player1 = new LudiiAI();
    static final AI player2 = new LudiiAI();

    static StateObserverC4TranslationLayer sobs;

    /**
     * Plays a game of fixed moves with a known outcome. Then checks if the state observer has been translated
     * correctly by comparing its outcome with the expected one.
     * */
    @Test
    public void playFixedGame(){
        final var ludiiGame = GameLoader.loadGameFromName("Connect Four.lud");
        final Context context = new Context(ludiiGame, new Trial(ludiiGame));

        ludiiGame.start(context);

        int[] moves ={3,0,4,0,3,0,2,0};

        // iterates over moves list and applies the moves
        for(int move : moves){
            FastArrayList<Move> possibleMoves = context.moves(context).moves();

            for(Move ludiiMove : possibleMoves)
                if (ludiiMove.to() == move) {
                    ludiiGame.apply(context,ludiiMove);
                }

        }

        sobs = new StateObserverC4TranslationLayer(context,1);

        System.out.println(sobs.stringDescr());
        assert sobs.isGameOver();
        assert sobs.win();
        assert sobs.countPieces() == 8;
        assert sobs.getGameScore(1) == 1; // Player 2 (0) has won
        assert sobs.getGameScore(0) == -1; // Player 1 (X) has lost

    }

    /**
     * Checks if the state observer has been initialized correctly.
     * Plays random game of Connect Four between 2 Ludii agents and compares whether information
     * between the Ludii context and the state observer matches. */
    @Test
    public void playRandomGame(){
        final var ludiiGame = GameLoader.loadGameFromName("Connect Four.lud");
        final Context context = new Context(ludiiGame, new Trial(ludiiGame));
        SystemConversionC4 conversion = new SystemConversionC4();

        final List<AI> ais = new ArrayList<AI>();
        ais.add(null);
        ais.add(player1);
        ais.add(player2);

        ludiiGame.start(context);

        for(int i = 1; i < ais.size(); i++){
            ais.get(i).initAI(ludiiGame,i);
        }

        final Model model = context.model();

        while(!context.trial().over()){
            model.startNewStep(context,ais,1);
        }

        sobs = new StateObserverC4TranslationLayer(context, context.trial().status().winner());

        assert sobs.isGameOver();
        assert sobs.win();

        System.out.println(sobs.stringDescr());

        // compare if the first player (X) is winner from both GBG and Ludii perspective
        // else compare if the second player (O) is winner for both
        if(sobs.getGameScore(0) == 1){
            assertEquals(context.trial().status().winner(), conversion.getLudiiPlayerFromGBG(0)); // Player 1 (X)
        }
        else {
            assertEquals(context.trial().status().winner(), conversion.getLudiiPlayerFromGBG(1)); // Player 2 (O)
        }

    }
}
