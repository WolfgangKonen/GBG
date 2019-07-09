package games.CFour;

import java.io.IOException;

import javax.swing.JFrame;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;

/**
 * {@link Arena} for Connect Four. It borrows all functionality
 * from the general class {@link Arena}. It only overrides 
 * the abstract methods <ul>
 * <li> {@link Arena#makeGameBoard()}, 
 * <li> {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int)}, and 
 * <li> {@link Arena#makeFeatureClass(int)}, 
 * <li> {@link Arena#makeXNTupleFuncs()}, 
 * </ul> 
 * such that these factory methods return objects of class {@link GameBoardC4}, 
 * {@link EvaluatorC4}, {@link FeatureC4}, and {@link XNTupleFuncsC4}, respectively.
 * 
 * @see GameBoardC4
 * @see EvaluatorC4
 * 
 * @author Wolfgang Konen, TH Köln, May'18
 */
public class ArenaC4 extends Arena   {
	
	public ArenaC4() {
		super();
	}

	public ArenaC4(String title) {
		super(title);		
	}
	
	/**
	 * @return a name of the game, suitable as subdirectory name in the 
	 *         {@code agents} directory
	 */
	public String getGameName() {
		return "ConnectFour";
	}
	
	/**
	 * Factory pattern method: make a new GameBoard 
	 * @return	the game board
	 */
	public GameBoard makeGameBoard() {
		gb = new GameBoardC4(this);	
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
//		if (mode==-1) mode=EvaluatorC4.getDefaultEvalMode();
		return new EvaluatorC4(pa,gb,stopEval,mode,verbose);
	}
	
	public Feature makeFeatureClass(int featmode) {
		return new FeatureC4(featmode);
	}
	
	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsC4();
	}

	public void performArenaDerivedTasks() {  }

	/**
	 * Start GBG for Connect Four (non-trainable version)
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		ArenaC4 ar = new ArenaC4("General Board Game Playing");

		if (args.length==0) {
			ar.init();
		} else {
			throw new RuntimeException("[ArenaC4.main] args="+args+" not allowed. Use batch facility.");
		}
	}
	
}
