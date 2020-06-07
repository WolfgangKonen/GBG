package games.Nim;

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
 * {@link ArenaTrain} for Nim3P (3 players). It borrows all functionality
 * from the general class {@link ArenaTrain} derived from {@link Arena}. It only overrides 
 * the abstract methods <ul>
 * <li> {@link Arena#makeGameBoard()}, 
 * <li> {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int)}, and 
 * <li> {@link Arena#makeFeatureClass(int)}, 
 * <li> {@link Arena#makeXNTupleFuncs()}, 
 * </ul> 
 * such that these factory methods return objects of class {@link GameBoardNim3P}, 
 * {@link EvaluatorNim3P}, {@link FeatureNim}, and {@link XNTupleFuncsNim3P}, respectively.
 * <p>
 * {@link ArenaTrainNim3P} has a short {@link #main(String[])} for launching the trainable 
 * version of GBG. 
 * 
 * @see GameBoardNim3P
 * @see EvaluatorNim3P
 * 
 * @author Wolfgang Konen, TH Koeln, 2020
 */
public class ArenaTrainNim3P extends ArenaTrain   {
	
	public ArenaTrainNim3P(String title, boolean withUI) {
		super(title,withUI);		
	}
	
	/**
	 * @return a name of the game, suitable as subdirectory name in the 
	 *         {@code agents} directory
	 */
	public String getGameName() {
		return "Nim3P";
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

	/**
	 * Start GBG for Nim (trainable version)
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		ArenaTrainNim3P t_Frame = new ArenaTrainNim3P("General Board Game Playing",true);

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[ArenaTrainNim.main] args="+args+" not allowed.");
		}
	}
	
}
