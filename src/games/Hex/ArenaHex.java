package games.Hex;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;

import java.io.IOException;

import javax.swing.*;

/**
 * {@link Arena} for TicTacToe. It borrows all functionality
 * from the general class {@link Arena}. It only overrides the abstract
 * methods {@link Arena#makeGameBoard()},
 * {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int)},
 * and {@link Arena#makeFeatureClass(int)}, such that
 * these factory methods return objects of class {@link GameBoardHex},
 * {@link EvaluatorHex}, and {@link FeatureHex}, respectively.
 *
 * @author Kevin Galitzki, Wolfgang Konen, TH Koeln, 2016-2020
 * 
 * @see GameBoardHex
 * @see EvaluatorHex
 */
public class ArenaHex extends Arena {

    public ArenaHex() {
        super();
    }

	public ArenaHex(String title) {
		super(title);		
	}
	
    /**
     * @return a name of the game, suitable as subdirectory name in the
     * {@code agents} directory
     */
    public String getGameName() {
        return "Hex";
    }

    /**
     * @return the Hex board size
     */
    public static int getBoardSize() {
        return HexConfig.BOARD_SIZE;
    }

    /**
     * set the Hex board size
     */
    public static void setBoardSize(int val) {
        HexConfig.BOARD_SIZE = val;
        HexConfig.TILE_COUNT = val*val;
    }

    /**
     * Factory pattern method: make a new GameBoard
     *
     * @return the game board
     */
    public GameBoard makeGameBoard() {
        gb = new GameBoardHex(this);
        return gb;
    }

    /**
     * Factory pattern method: make a new Evaluator
     *
     * @param pa       the agent to evaluate
     * @param gb       the game board
     * @param stopEval the number of successful evaluations needed to reach the
     *                 evaluator goal (may be used during training to stop it
     *                 prematurely)
     * @param mode     which evaluator mode: 0,1,2,9. Throws a runtime exception
     *                 if {@code mode} is not in the set {@link Evaluator#getAvailableModes()}.
     * @param verbose  how verbose or silent the evaluator is
     * @return		   the evaluator
     */
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
//        if (mode == -1) mode = EvaluatorHex.getDefaultEvalMode();
        return new EvaluatorHex(pa, gb, stopEval, mode, verbose);
    }

    public Feature makeFeatureClass(int featmode) {
        return new FeatureHex(featmode);
    }

    public XNTupleFuncs makeXNTupleFuncs() {
        return new XNTupleFuncsHex();
    }

    public void performArenaDerivedTasks() {
    }

	/**
	 * Start GBG for Hex (non-trainable version)
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		ArenaHex t_Frame = new ArenaHex("General Board Game Playing");

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[ArenaHex.main] args="+args+" not allowed.");
		}
	}
	
}
