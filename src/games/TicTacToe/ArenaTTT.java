package games.TicTacToe;

import java.io.IOException;

import javax.swing.JFrame;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;
import games.ArenaTrain;

/**
 * {@link Arena} for TicTacToe. It borrows all functionality
 * from the general class {@link Arena}. It only overrides 
 * the abstract methods <ul>
 * <li> {@link Arena#makeGameBoard()}, 
 * <li> {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int)}, and 
 * <li> {@link Arena#makeFeatureClass(int)}, 
 * <li> {@link Arena#makeXNTupleFuncs()}, 
 * </ul> 
 * such that these factory methods return objects of class {@link GameBoardTTT}, 
 * {@link EvaluatorTTT}, {@link FeatureTTT}, and {@link XNTupleFuncsTTT}, respectively.
 * <p>
 * {@link ArenaTTT} has a short {@link #main(String[])} for launching the non-trainable 
 * version of GBG. 
 * 
 * @see GameBoardTTT
 * @see EvaluatorTTT
 * 
 * @author Wolfgang Konen, TH Koeln, Nov'16
 */
public class ArenaTTT extends Arena   {
	
	public ArenaTTT(String title, boolean withUI) {
		super(title,withUI);		
	}
	
	/**
	 * @return a name of the game, suitable as subdirectory name in the 
	 *         {@code agents} directory
	 */
	public String getGameName() {
		return "TicTacToe";
	}
	
	/**
	 * Factory pattern method: make a new GameBoard 
	 * @return	the game board
	 */
	public GameBoard makeGameBoard() {
		gb = new GameBoardTTT(this);	
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
		return new EvaluatorTTT(pa,gb,stopEval,mode,verbose);
	}
	
	public Feature makeFeatureClass(int featmode) {
		return new FeatureTTT(featmode);
	}
	
	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsTTT();
	}

	public void performArenaDerivedTasks() {  }

	
	/**
	 * Start GBG for TicTacToe (non-trainable version)
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		ArenaTTT t_Frame = new ArenaTTT("General Board Game Playing",true);

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[Arena.main] args="+args+" not allowed. Use TicTacToeBatch.");
		}
	}
	
}
