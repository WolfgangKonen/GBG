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

    public ArenaBlackJack(String title, boolean withUI, boolean withTrainRights) {
        super(title,withUI,withTrainRights);
    }

    /**
     * @return a name of the game, suitable as subdirectory name in the
     * {@code agents} directory
     */
    @Override
    public String getGameName() {
        return "BlackJack";
    }

    /**
     * Factory pattern method: make a new GameBoard
     * @return the game board
     */
    @Override
    public GameBoard makeGameBoard() {
        return gb = new GameBoardBlackJack(this);
    }

    /**
     * Factory pattern method: make a new Evaluator
     *
     * @param pa       the agent to evaluate
     * @param gb       the game board
     * @param mode     which evaluator mode: 0,1,2,3,4,5. Throws a runtime exception
     *                 if {@code mode} is not in the set {@link Evaluator#getAvailableModes()}.
     * @param verbose  how verbose or silent the evaluator is
     * @return		   the evaluator
     */
    @Override
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int mode, int verbose) {
        return new EvaluatorBlackJack(pa, gb, mode, verbose);
    }

    /**
     * Factory pattern method: make a new XNTupleFuncs (needed for TD-Agents)
     * @return XNTupleFuncsBlackJack
     */
    public XNTupleFuncs makeXNTupleFuncs() {
        return new XNTupleFuncsBlackJack();
    }

    /**
     * Static infinite deck used in every StateObserverBlackJack
     */
    public static Deck deck = new Deck();

    /**
     * Start GBG for BlackJack (non-trainable version)
     *
     * @param args  CLI arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ArenaBlackJack t_Frame = new ArenaBlackJack("General Board Game Playing", true);

        if (args.length == 0) {
            t_Frame.init();
        } else {
            throw new RuntimeException("[ArenaBlackJack] args=" + args + " not allowed.");
        }
    }

}
