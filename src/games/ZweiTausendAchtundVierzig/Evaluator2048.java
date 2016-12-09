package games.ZweiTausendAchtundVierzig;

import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;

/**
 * Created by Johannes on 02.12.2016.
 */
public class Evaluator2048 extends Evaluator {

    public Evaluator2048(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
    }

    @Override
    protected boolean eval_Agent() {
        return false;
    }

    @Override
    public double getLastResult() {
        return 0;
    }
}
