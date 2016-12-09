package games.ZweiTausendAchtundVierzig;

import controllers.PlayAgent;
import games.ArenaTrain;
import games.Evaluator;
import games.GameBoard;

import javax.swing.*;

public class ArenaTrain2048 extends ArenaTrain {
    public ArenaTrain2048() {
        super();
    }

    public ArenaTrain2048(JFrame frame) {
        super(frame);
    }

    /**
     * @return a name of the game, suitable as subdirectory name in the
     *         {@code agents} directory
     */
    public String getGameName() {
        return "2048";
    }

    /**
     * Factory pattern method: make a new GameBoard
     * @return	the game board
     */
    public GameBoard makeGameBoard() {
        gb = new GameBoard2048(this);
        return gb;
    }
    /**
     * Factory pattern method: make a new Evaluator
     * @param pa		the agent to evaluate
     * @param gb		the game board
     * @param stopEval	the number of successful evaluations needed to reach the
     * 					evaluator goal (may be used during training to stop it
     * 					prematurely)
     * @param mode		which evaluator mode: 0,1,2,9. Throws a runtime exception
     * 					if {@code mode} is not in the set {@link #getAvailableModes}.
     * @param verbose	how verbose or silent the evaluator is
     * @return
     */
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {

        return new Evaluator2048(pa,gb,stopEval,mode,verbose);
    }
}