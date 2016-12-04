package games.ZweiTausendAchtundVierzig;

import controllers.MinimaxAgent;
import controllers.PlayAgent;
import controllers.RandomAgent;
import games.Evaluator;
import games.GameBoard;
import games.XArenaFuncs;
import tools.Types;

/**
 * Created by Johannes on 02.12.2016.
 */
public class EvaluatorZTAV extends Evaluator {

    public EvaluatorZTAV(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
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
