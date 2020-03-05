package games.ZweiTausendAchtundVierzig;

import controllers.PlayAgent;
import games.ArenaTrain;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;
import games.ZweiTausendAchtundVierzig.Heuristic.Evaluator2048_EA;

import java.io.IOException;

import javax.swing.*;

public class ArenaTrain2048 extends ArenaTrain {
    public ArenaTrain2048() {
        super();
    }

	public ArenaTrain2048(String title) {
		super(title);		
	}
	
	public ArenaTrain2048(String title, boolean withUI) {
		super(title,withUI);		
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
        //gb = new GBInvisible2048(this);
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
    	return makeEvaluator(pa, gb, stopEval, mode, verbose, this);
    }
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose, ArenaTrain ar) {
        switch (mode) {
        	case -1:
                return new Evaluator2048(pa, gb, stopEval, -1, verbose,ar);
            case 0:
                return new Evaluator2048(pa, gb, stopEval, 0, verbose,ar);
            case 1:
                return new Evaluator2048_BoardPositions(pa, gb, stopEval, verbose);
            case 2:
                return new Evaluator2048_EA(pa, gb, stopEval, verbose);
            default:
                throw new RuntimeException("Mode " + mode + " is not allowed for 2048");
        }
    }
    
	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncs2048();
	}

    public Feature makeFeatureClass(int featmode) {
        return new Feature2048(featmode);
    }

	/**
	 * Start GBG for 2048 (trainable version)
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		ArenaTrain2048 t_Frame = new ArenaTrain2048("General Board Game Playing");

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[ArenaTrain2048.main] args="+args+" not allowed. Use batch facility.");
		}
	}
	

}