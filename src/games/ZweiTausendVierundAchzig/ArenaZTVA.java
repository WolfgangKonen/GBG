package games.ZweiTausendVierundAchzig;

import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;
import games.Arena;

import javax.swing.*;

/**
 * Created by Johannes on 18.11.2016.
 */
public class ArenaZTVA extends Arena {
    public ArenaZTVA() {
        super();
    }

    public ArenaZTVA(JFrame frame) {
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
        gb = new GameBoardZTVA(this);
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
        //if (mode==9) return new Evaluator9(pa,stopEval);
        // --- this is now inside EvaluatorZTVA ---

        return new EvaluatorZTVA(pa,gb,stopEval,mode,verbose);
    }

    public void performArenaDerivedTasks() {  }
}
