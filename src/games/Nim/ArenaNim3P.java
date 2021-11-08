package games.Nim;

import java.io.IOException;

import javax.swing.JFrame;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;
import games.Hex.HexConfig;
import games.Sim.ConfigSim;
import games.ArenaTrain;

/**
 * {@link Arena} for Nim3P (3 players). It borrows all functionality
 * from the general class {@link Arena}. It only overrides 
 * the abstract methods <ul>
 * <li> {@link Arena#makeGameBoard()}, 
 * <li> {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int)}, and 
 * <li> {@link Arena#makeFeatureClass(int)}, 
 * <li> {@link Arena#makeXNTupleFuncs()}, 
 * </ul> 
 * such that these factory methods return objects of class {@link GameBoardNim3P}, 
 * {@link EvaluatorNim3P}, {@link FeatureNim}, and {@link XNTupleFuncsNim3P}, respectively.
 * <p>
 * {@link ArenaNim3P} has a short {@link #main(String[])} for launching the non-trainable 
 * version of GBG. 
 * 
 * @see GameBoardNim3P
 * @see EvaluatorNim3P
 * 
 * @author Wolfgang Konen, TH Koeln, Dec'18
 */
public class ArenaNim3P extends Arena   {
	
	public ArenaNim3P(String title, boolean withUI) {
		super(title,withUI);		
	}

	public ArenaNim3P(String title, boolean withUI, boolean withTrainRights) {
		super(title,withUI,withTrainRights);
	}

	/**
	 * @return a name of the game, suitable as subdirectory name in the 
	 *         {@code agents} directory
	 */
	public String getGameName() {
		return "Nim3P";
	}
	
    /**
     * @return the number of heaps in Nim
     */
    public static int getNumberHeaps() {
        return NimConfig.NUMBER_HEAPS;
    }

    /**
     * @return the initial heap size (initial number of items in each heap)
     */
    public static int getHeapSize() {
        return NimConfig.HEAP_SIZE;
    }

    /**
     * @return the maximum number of items to subtract ('minus') from a heap in one move.
     * If == {@link #getHeapSize()}, then each heap can be cleared in one move.
     */
    public static int getMaxMinus() {
        return NimConfig.MAX_MINUS;
    }

    /**
     * set the number of heaps in Nim
     */
    public static void setNumHeaps(int val) {
    	NimConfig.NUMBER_HEAPS = val;
    }

    /**
     * set the initial heap size (initial number of items in each heap)
     */
    public static void setHeapSize(int val) {
    	NimConfig.HEAP_SIZE = val;
    }

    /**
     * set the maximum number of items to subtract ('minus') from a heap in one move.
     * May not be bigger than {@link #getHeapSize()}. 
     */
    public static void setMaxMinus(int val) {
    	assert val <= getHeapSize() : "ArenaNim.setMaxMinus: value may not be bigger than heap size!"; 
    	NimConfig.MAX_MINUS = val;
    }

    /**
     * set the maximum number of items to subtract ('minus') from a heap in one move.
     * May not be bigger than {@link #getHeapSize()}. 
     */
    public static void setExtraRule(boolean val) {
    	NimConfig.EXTRA_RULE = val;
    }

	/**
	 * Factory pattern method: make a new GameBoard 
	 * @return	the game board
	 */
	public GameBoard makeGameBoard() {
		gb = new GameBoardNim3P(this);	
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
//		if (mode==-1) mode=EvaluatorNim.getDefaultEvalMode();
		return new EvaluatorNim3P(pa,gb,stopEval,mode,verbose);
	}
	
	public Feature makeFeatureClass(int featmode) {
		return new FeatureNim(featmode);
	}
	
	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsNim3P();
	}

//    public void performArenaDerivedTasks() {}

	
	/**
	 * Start GBG for  (non-trainable version)
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		ArenaNim3P t_Frame = new ArenaNim3P("General Board Game Playing",true);

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[Arena.main] args="+args+" not allowed.");
		}
	}
	

}
