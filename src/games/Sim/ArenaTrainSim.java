package games.Sim;

import java.io.IOException;

import controllers.PlayAgent;
import games.ArenaTrain;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;
import games.TicTacToe.XNTupleFuncsTTT;

public class ArenaTrainSim extends ArenaTrain {

	public ArenaTrainSim() {
		super();
	}

	public ArenaTrainSim(String title) {
		super(title);		
	}
	
	public ArenaTrainSim(String title, boolean withUI) {
		super(title,withUI);		
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
	
	@Override
	public Feature makeFeatureClass(int featmode)
	{
		return new FeatureSim(featmode);
	}
	
	public XNTupleFuncs makeXNTupleFuncs()
	{
//		return new XNTupleFuncsSim(15,3,2);		// /WK/ Bug: this is special to K_6 + 2 players. Generalize!!
		return new XNTupleFuncsSim(ConfigSim.GRAPH_SIZE*(ConfigSim.GRAPH_SIZE-1)/2,
								   ConfigSim.NUM_PLAYERS+1,
								   ConfigSim.NUM_PLAYERS);		
	}

	public static void main(String[] args) throws IOException 
	{
		ArenaTrainSim t_Frame = new ArenaTrainSim("General Board Game Playing");

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[ArenaTrainSim.main] args="+args+" not allowed. Use TicTacToeBatch.");
		}
	}
}
