package games.Hex;

import controllers.PlayAgent;
import games.ArenaTrain;
import games.Evaluator;
import games.GameBoard;

import javax.swing.*;

public class ArenaTrainHex extends ArenaTrain {

    public ArenaTrainHex(){ super();}

    public ArenaTrainHex(JFrame frame) {
        super(frame);
    }

    @Override
    public String getGameName() {
        return "Hex";
    }

    @Override
    public GameBoard makeGameBoard() {
        return new GameBoardHex(this);
    }

    @Override
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
        return new EvaluatorHex(pa,gb,stopEval,mode,verbose);
    }
}
