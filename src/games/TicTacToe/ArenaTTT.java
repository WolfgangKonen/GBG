package games.TicTacToe;

import javax.swing.JFrame;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.GameBoard;
import games.ArenaTrain;

public class ArenaTTT extends Arena   {
	
	public ArenaTTT() {
		super();
	}

	public ArenaTTT(JFrame frame) {
		super(frame);
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
	 * 					if {@code mode} is not in the set {@link #getAvailableModes}.
	 * @param verbose	how verbose or silent the evaluator is
	 * @return
	 */
	public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
		//if (mode==9) return new Evaluator9(pa,stopEval);
		return new EvaluatorTTT(pa,gb,stopEval,mode,verbose);
	}

	public void performArenaDerivedTasks() {  }

}
