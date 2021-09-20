package games.Yavalath;

import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;

public class EvaluatorYavalath extends Evaluator {


    public EvaluatorYavalath(PlayAgent e_PlayAgent, GameBoard gb, int mode, int stopEval){
        super(e_PlayAgent,gb,mode,stopEval);
    }
    @Override
    protected boolean evalAgent(PlayAgent playAgent) {
        return false;
    }

    @Override
    public int[] getAvailableModes() {
        return new int[0];
    }

    @Override
    public int getQuickEvalMode() {
        return 0;
    }

    @Override
    public int getTrainEvalMode() {
        return 0;
    }

    @Override
    public String getPrintString() {
        return null;
    }

    @Override
    public String getTooltipString() {
        return null;
    }

    @Override
    public String getPlotTitle() {
        return null;
    }
}
