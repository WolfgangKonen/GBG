package games.Nim;

import java.io.IOException;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;

/**
 * {@link Arena} for Nim (2 players). It borrows all functionality
 * from the general class {@link Arena}. It only overrides 
 * the abstract methods <ul>
 * <li> {@link Arena#makeGameBoard()}, 
 * <li> {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int)}, and
 * <li> {@link Arena#makeFeatureClass(int)}, 
 * <li> {@link Arena#makeXNTupleFuncs()}, 
 * </ul> 
 * such that these factory methods return objects of class {@link GameBoardNim2P}, 
 * {@link EvaluatorNim2P}, {@link FeatureNim}, and {@link XNTupleFuncsNim2P}, respectively.
 * <p>
 * {@link ArenaNim2P} has a short {@link #main(String[])} for launching the non-trainable 
 * version of GBG. 
 * 
 * @see GameBoardNim2P
 * @see EvaluatorNim2P
 * 
 * @author Wolfgang Konen, TH Koeln, Dec'18
 */
public class ArenaNim2P extends Arena   {
	
	public ArenaNim2P(String title, boolean withUI) {
		super(title,withUI);		
	}

	public ArenaNim2P(String title, boolean withUI, boolean withTrainRights) {
		super(title,withUI,withTrainRights);
	}

	/**
	 * @return a name of the game, suitable as subdirectory name in the 
	 *         {@code agents} directory
	 */
	public String getGameName() {
		return "Nim";
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
		if(NimConfig.HEAP_SIZE != -1) assert val <= getHeapSize() : "ArenaNim.setMaxMinus: value may not be bigger than heap size!";

		NimConfig.MAX_MINUS = val;
    }

	/**
	 * Factory pattern method: make a new GameBoard 
	 * @return	the game board
	 */
	public GameBoard makeGameBoard() {
		gb = new GameBoardNim2P(this);	
		return gb;
	}
	/**
	 * Factory pattern method: make a new Evaluator
	 * @param pa        the agent to evaluate
	 * @param gb        the game board
	 * @param mode        which evaluator mode: 0,1,2,9. Throws a runtime exception
	 * 					if {@code mode} is not in the set {@link Evaluator#getAvailableModes()}.
	 * @param verbose    how verbose or silent the evaluator is
	 * @return
	 */
	public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int mode, int verbose) {
//		if (mode==-1) mode=EvaluatorNim.getDefaultEvalMode();
		return new EvaluatorNim2P(pa,gb, mode,verbose);
	}
	
	public Feature makeFeatureClass(int featmode) {
		return new FeatureNim(featmode);
	}
	
	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsNim2P();
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
		ArenaNim2P t_Frame = new ArenaNim2P("General Board Game Playing",true);

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[Arena.main] args="+args+" not allowed.");
		}
	}
	

}
