package games.TicTacToe;

import javax.swing.JFrame;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;
import games.ArenaTrain;
//import params.TDParams;

/**
 * {@link Arena} for TicTacToe. It borrows all functionality
 * from the general class {@link Arena}. It only overrides the abstract
 * methods {@link Arena#makeGameBoard()}, 
 * {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int)},
 * and {@link Arena#makeFeatureClass(int)}, such that 
 * these factory methods return objects of class {@link GameBoardTTT}, 
 * {@link EvaluatorTTT}, and {@link FeatureTTT}, respectively.
 * 
 * @see GameBoardTTT
 * @see EvaluatorTTT
 * 
 * @author Wolfgang Konen, TH Köln, Nov'16
 */
public class ArenaTTT extends Arena   {
	
	public ArenaTTT() {
		super();
	}

	public ArenaTTT(JFrame frame) {
		super(frame);
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
	 * 					If mode==-1, set it from {@link Evaluator#getDefaultEvalMode()}.
	 * @param verbose	how verbose or silent the evaluator is
	 * @return
	 */
	public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
		if (mode==-1) mode=EvaluatorTTT.getDefaultEvalMode();
		return new EvaluatorTTT(pa,gb,stopEval,mode,verbose);
	}
	
	public Feature makeFeatureClass(int featmode) {
		return new FeatureTTT(featmode);
	}
	
	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsTTT();
	}

//	public PlayAgent makeTDSAgent(String sAgent, TDParams tdPar, int maxGameNum){
//		return new TDPlayerTTT(sAgent,tdPar,maxGameNum);
//	}
	

	public void performArenaDerivedTasks() {  }

}
