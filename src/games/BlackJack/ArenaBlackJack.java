package games.BlackJack;

import java.io.IOException;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.GameBoard;
import games.XNTupleFuncs;

public class ArenaBlackJack extends Arena {


    public ArenaBlackJack(String title, boolean withUI) {
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
        return null;
    }

    @Override
    public void performArenaDerivedTasks() {
        // TODO Auto-generated method stub
    }

    public XNTupleFuncs makeXNTupleFuncs() {
        return new XNTupleFuncsBlackJack();
    }

    public static void main(String[] args) throws IOException {
        ArenaBlackJack t_Frame = new ArenaBlackJack("General Board Game Playing", true);

        if (args.length == 0) {
            t_Frame.init();
        } else {
            throw new RuntimeException("[ArenaBlackJack] args=" + args + " not allowed.");
        }
    }

}
