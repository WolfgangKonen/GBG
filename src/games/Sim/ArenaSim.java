package games.Sim;

import java.io.IOException;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;
import games.TicTacToe.ArenaTTT;
import games.TicTacToe.EvaluatorTTT;
import games.TicTacToe.FeatureTTT;
import games.TicTacToe.XNTupleFuncsTTT;


public class ArenaSim extends Arena{

	public ArenaSim() {
		super();
	}

	public ArenaSim(String title) {
		super(title);		
	}
	
	@Override
	public String getGameName() {
		return "Sim";
	}

	@Override
	public GameBoard makeGameBoard() {
		gb = new GameBoardSim(this);	
		return gb;
	}

	@Override
	public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
		return new EvaluatorSim(pa,gb,stopEval,mode,verbose);
	}

	public Feature makeFeatureClass(int featmode) {
		return new FeatureSim(featmode);
	}
	
	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsTTT();
	}

	
	@Override
	public void performArenaDerivedTasks() {
	
	}

	public static void main(String[] args) throws IOException 
	{
		ArenaSim t_Frame = new ArenaSim("General Board Game Playing");

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[Arena.main] args="+args+" not allowed. Use TicTacToeBatch.");
		}
	}
}
