package games.Hex;

import controllers.PlayAgent;
import games.Arena;
import games.ArenaTrain;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;
import games.TicTacToe.EvaluatorTTT;
import games.TicTacToe.FeatureTTT;
import games.TicTacToe.GameBoardTTT;
import games.TicTacToe.XNTupleFuncsTTT;

import java.io.IOException;

import javax.swing.*;

/**
 * {@link ArenaTrain} for Hex. It borrows all functionality
 * from the general class {@link ArenaTrain} derived from {@link Arena}. It only overrides 
 * the abstract methods <ul>
 * <li> {@link Arena#makeGameBoard()}, 
 * <li> {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int)}, and 
 * <li> {@link Arena#makeFeatureClass(int)}, 
 * <li> {@link Arena#makeXNTupleFuncs()}, 
 * </ul> 
 * such that these factory methods return objects of class {@link GameBoardHex}, 
 * {@link EvaluatorHex}, {@link FeatureHex}, and {@link XNTupleFuncsHex}, respectively.
 * 
 * @see GameBoardHex
 * @see EvaluatorHex
 * 
 * @author Kevin Galitzki, Wolfgang Konen, TH Koeln, 2016-2020
 */
public class ArenaTrainHex extends ArenaTrain {

	public ArenaTrainHex(String title, boolean withUI) {
		super(title,withUI);		
	}
	
   @Override
    public String getGameName() {
        return "Hex";
    }

    @Override
    public GameBoard makeGameBoard() {
        return new GameBoardHex(this);
    }

    @Override
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
        return new EvaluatorHex(pa,gb,stopEval,mode,verbose);
    }

    public Feature makeFeatureClass(int featmode) {
        return new FeatureHex(featmode);
    }

    public XNTupleFuncs makeXNTupleFuncs() {
        return new XNTupleFuncsHex();
    }
    
	/**
	 * Start GBG for Hex (trainable version)
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		ArenaTrainHex t_Frame = new ArenaTrainHex("General Board Game Playing",true);

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[ArenaTrainHex.main] args="+args+" not allowed. Use batch facility.");
		}
	}
	
}
