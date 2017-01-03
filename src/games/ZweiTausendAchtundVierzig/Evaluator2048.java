package games.ZweiTausendAchtundVierzig;

import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;

/**
 * Created by Johannes on 02.12.2016.
 */
public class Evaluator2048 extends Evaluator {
    private double averageScore;
    private int minScore = Integer.MAX_VALUE;
    private int maxScore = 0;

    public Evaluator2048(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
    }

    @Override
    protected boolean eval_Agent() {

        System.out.println("Starting evaluation of " + Config.NUMBEREVALUATIONS + " games, this may take a while...");
        for(int i = 0; i < Config.NUMBEREVALUATIONS; i++) {
            StateObserver2048 so = new StateObserver2048();
            while (!so.isGameOver()) {
                so.advance(m_PlayAgent.getNextAction(so, false, new double[so.getNumAvailableActions() + 1], true));
            }
            System.out.println("Finished game " + (i+1) + " with Score: " + so.score);
            averageScore += so.score;
            if(so.score < minScore) {
                minScore = so.score;
            }
            if(so.score > maxScore) {
                maxScore = so.score;
            }
        }
        averageScore/=Config.NUMBEREVALUATIONS;

        return true;
    }

    @Override
    public double getLastResult() {
        return averageScore;
    }

    @Override
    public String getMsg() {
        return "\nAverage Score is: " + averageScore +
                "\nLowest Score is: " + minScore +
                "\nHighest Score is: " + maxScore +
                "\n\n";
    }
}
