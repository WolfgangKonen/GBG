package games.SimpleGame;

import controllers.PlayAgent;
import games.*;

import java.io.IOException;

/**
 * {@link ArenaTrain} for TicTacToe. It borrows all functionality
 * from the general class {@link ArenaTrain} derived from {@link Arena}. It only overrides 
 * the abstract methods <ul>
 * <li> {@link Arena#makeGameBoard()}, 
 * <li> {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int)}, and 
 * <li> {@link Arena#makeFeatureClass(int)}, 
 * <li> {@link Arena#makeXNTupleFuncs()}, 
 * </ul> 
 * such that these factory methods return objects of class {@link GameBoardSG},
 * {@link EvaluatorSG}, {@link FeatureSG}, and {@link XNTupleFuncsSG}, respectively.
 * 
 * @see GameBoardSG
 * @see EvaluatorSG
 * 
 * @author Wolfgang Konen, TH Koeln, 2016-2020
 */
public class ArenaTrainSG extends ArenaTrain   {
	
	public ArenaTrainSG(String title, boolean withUI) {
		super(title,withUI);		
	}
	
	/**
	 * @return a name of the game, suitable as subdirectory name in the 
	 *         {@code agents} directory
	 */
	public String getGameName() {
		return "SimpleGame";
	}
	
	/**
	 * Factory pattern method: make a new GameBoard 
	 * @return	the game board
	 */
	public GameBoard makeGameBoard() {
		gb = new GameBoardSG(this);
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
	 * 					if {@code mode} is not in the set {@link EvaluatorSG#getAvailableModes()}.
	 * @param verbose	how verbose or silent the evaluator is
	 * @return			the evaluator
	 */
	public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
		return new EvaluatorSG(pa,gb,stopEval,mode,verbose);
	}

	public Feature makeFeatureClass(int featmode) {
		return new FeatureSG(featmode);
	}

	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsSG();
	}

	/**
	 * Start GBG for TicTacToe (trainable version)
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		ArenaTrainSG t_Frame = new ArenaTrainSG("General Board Game Playing",true);

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[ArenaTrainSG.main] args="+args+" not allowed. Use GBGBatch.");
		}
	}
	
}
