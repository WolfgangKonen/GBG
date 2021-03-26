package games.BlackJack;

import java.io.IOException;

import controllers.PlayAgent;
import games.ArenaTrain;
import games.Evaluator;
import games.GameBoard;
import games.XNTupleFuncs;

public class ArenaBlackJackTrain extends ArenaTrain {

    public ArenaBlackJackTrain(String title, boolean withUI) {
        super(title, withUI);
    }

    @Override
    public String getGameName() {
        return "BlackJack";
    }

    @Override
    public GameBoard makeGameBoard() {
        return gb = new GameBoardBlackJack(this);
    }

    @Override
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
        return new EvaluatorBlackJack(pa, gb, mode, stopEval, verbose);
    }

    @Override
    public XNTupleFuncs makeXNTupleFuncs() {
        return new XNTupleFuncsBlackJackSimple();
    }

    public static void main(String[] args) throws IOException {
        ArenaBlackJackTrain t_Frame = new ArenaBlackJackTrain("General Board Game Playing", true);

        if (args.length == 0) {
            t_Frame.init();
        } else {
            throw new RuntimeException("[ArenaBlackJack] args=" + args + " not allowed.");
        }
    }

}
