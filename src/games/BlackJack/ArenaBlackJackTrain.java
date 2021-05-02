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
     * @param stopEval the number of successful evaluations needed to reach the
     *                 evaluator goal (may be used during training to stop it
     *                 prematurely)
     * @param mode     which evaluator mode: 0,1,2,3,4,5. Throws a runtime exception
     *                 if {@code mode} is not in the set {@link Evaluator#getAvailableModes()}.
     * @param verbose  how verbose or silent the evaluator is
     * @return		   the evaluator
     */
    @Override
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
        return new EvaluatorBlackJack(pa, gb, mode, stopEval, verbose);
    }


    /**
     * Factory pattern method: make a new XNTupleFuncs (needed for TD-Agents)
     * @return XNTupleFuncsBlackJack
     */
    @Override
    public XNTupleFuncs makeXNTupleFuncs() {
        return new XNTupleFuncsBlackJackSimple();
    }

    /**
     * Start GBG for BlackJack (trainable version)
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ArenaBlackJackTrain t_Frame = new ArenaBlackJackTrain("General Board Game Playing", true);

        if (args.length == 0) {
            t_Frame.init();
        } else {
            throw new RuntimeException("[ArenaBlackJack] args=" + args + " not allowed.");
        }
    }

}
