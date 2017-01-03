package games.ZweiTausendAchtundVierzig;

import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Johannes on 02.12.2016.
 */
public class Evaluator2048 extends Evaluator {
    private double averageScore;
    private int minScore = Integer.MAX_VALUE;
    private int maxScore = 0;
    private Map<Integer, Integer> tiles = new HashMap<Integer, Integer>();
    private int moves = 0;
    private long startTime;
    private long stopTime;

    public Evaluator2048(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
    }

    @Override
    protected boolean eval_Agent() {

        startTime = System.currentTimeMillis();

        System.out.print("Starting evaluation of " + Config.NUMBEREVALUATIONS + " games, this may take a while...\n");

        for(int i = 0; i < Config.NUMBEREVALUATIONS; i++) {
            long gameSartTime = System.currentTimeMillis();
            StateObserver2048 so = new StateObserver2048();
            while (!so.isGameOver()) {
                so.advance(m_PlayAgent.getNextAction(so, false, new double[so.getNumAvailableActions() + 1], true));
            }

            Integer value = tiles.get(so.highestTileValue);
            if (value == null) {
                tiles.put(so.highestTileValue, 1);
            }
            else {
                tiles.put(so.highestTileValue, value + 1);
            }

            averageScore += so.score;
            if(so.score < minScore) {
                minScore = so.score;
            }
            if(so.score > maxScore) {
                maxScore = so.score;
            }

            moves += so.moves;

            System.out.print("Finished game " + (i+1) + " with score " + so.score + " after " + (System.currentTimeMillis()-gameSartTime) + "ms. Highest tile is " + so.highestTileValue + ".\n");
        }
        averageScore/=Config.NUMBEREVALUATIONS;

        stopTime = System.currentTimeMillis();

        System.out.print("\n");

        return true;
    }

    @Override
    public double getLastResult() {
        return averageScore;
    }

    @Override
    public String getMsg() {
        String tilesString = "";
        for (Map.Entry tile : tiles.entrySet()) {
            tilesString += "\n" + tile.getKey() + ", " + tile.getValue();
        }

        long duration = (stopTime - startTime)/1000;


        return "\n\nSettings:" +
                "\nPenalisation: " + Config.PENALISATION +
                "\nAddscore: " + Config.ADDSCORE +
                "\nEmptitiles multiplier: " + Config.EMPTYTILEVALUE +
                "\nHighesttileincorner multiplier: " + Config.HIGHESTTILEINCORENERVALUE +
                "\nNumber of games: " + Config.NUMBEREVALUATIONS +
                "\n" +
                "\nResults:" +
                "\nAverage score is: " + Math.round(averageScore) +
                "\nLowest score is: " + minScore +
                "\nHighest score is: " + maxScore +
                "\nAverage game duration: " +  Math.round((stopTime - startTime)/Config.NUMBEREVALUATIONS) + "ms" +
                "\nDuration of evaluation: " + duration + "s" +
                "\nMoves per second: " + Math.round(moves/duration) +
                "\nHighest tiles: " +
                tilesString +
                "\n\n";
    }
}
