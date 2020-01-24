package games.TicTacToe;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFrame;

import controllers.PlayAgent;
import games.Arena;
import games.ArenaTrain;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;
import tools.Types;

/**
 * {@link ArenaTrain} for TicTacToe. It borrows all functionality
 * from the general class {@link ArenaTrain} derived from {@link Arena}. It only overrides 
 * the abstract methods <ul>
 * <li> {@link Arena#makeGameBoard()}, 
 * <li> {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int)}, and 
 * <li> {@link Arena#makeFeatureClass(int)}, 
 * <li> {@link Arena#makeXNTupleFuncs()}, 
 * </ul> 
 * such that these factory methods return objects of class {@link GameBoardTTT}, 
 * {@link EvaluatorTTT}, {@link FeatureTTT}, and {@link XNTupleFuncsTTT}, respectively.
 * <p>
 * {@link ArenaTrainTTT} has a short {@link #main(String[])} for launching the trainable 
 * version of GBG. 
 * 
 * @see GameBoardTTT
 * @see EvaluatorTTT
 * 
 * @author Wolfgang Konen, TH Koeln, 2016-2020
 */
public class ArenaTrainTTT extends ArenaTrain   {
	
	public ArenaTrainTTT() {
		super();
	}

	public ArenaTrainTTT(String title) {
		super(title);		
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
	 * @param mode		which evaluator mode. Throws a runtime exception 
	 * 					if {@code mode} is not in the set {@link EvaluatorTTT#getAvailableModes()}.
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

	/**
	 * Start GBG for TicTacToe (trainable version)
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		ArenaTrainTTT t_Frame = new ArenaTrainTTT("General Board Game Playing");

// ---  just for analysis: compute the state space & game tree complexity ---		
//		System.out.println("Rough approximation for nStates = "+(int) Math.pow(3, 9)+ " = (3^9)");
//		TicTDBase.countStates2(false);
//		TicTDBase.countStates2(true);
		
		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[ArenaTrainTTT.main] args="+args+" not allowed. Use TicTacToeBatch.");
		}
	}
	
}
