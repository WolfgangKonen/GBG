package games.ZweiTausendAchtundVierzig;

import controllers.PlayAgent;
import games.*;

import javax.swing.*;

public class Arena2048 extends Arena {
    public Arena2048() {
        super();
    }

    public Arena2048(JFrame frame) {
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
     * 					if {@code mode} is not in the set {@link Evaluator#getAvailableModes()}.
     * @param verbose	how verbose or silent the evaluator is
     * @return
     */
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
        if(ConfigEvaluator.Evaluator == 0) {
            return new Evaluator2048(pa, gb, stopEval, mode, verbose);
        } else if(ConfigEvaluator.Evaluator == 1) {
            return new Evaluator2048_BoardPositions(pa, gb, stopEval, mode, verbose);
        } else {
            System.out.println("No valid Evaluator in Arena2048.java");
            return null;
        }
    }

	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncs2048();
	}

    public Feature makeFeatureClass(int featmode) {
        return new Feature2048(featmode);
    }

    public void performArenaDerivedTasks() {  }
}
