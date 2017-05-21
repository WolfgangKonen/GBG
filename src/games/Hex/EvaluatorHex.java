package games.Hex;

import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;


public class EvaluatorHex extends Evaluator {
    public EvaluatorHex(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
        super(e_PlayAgent, stopEval);
        //initEvaluator(gb,1);
    }

    public EvaluatorHex(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode) {
        super(e_PlayAgent, stopEval);
        //initEvaluator(gb,mode);
    }

    public EvaluatorHex(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
        //initEvaluator(gb,mode);
    }

    @Override
    protected boolean eval_Agent() {
        return false;
    }

    @Override
    public double getLastResult() {
        return 0;
    }

    @Override
    public boolean isAvailableMode(int mode) {
        return false;
    }

    @Override
    public int[] getAvailableModes() {
        return new int[0];
    }
}
