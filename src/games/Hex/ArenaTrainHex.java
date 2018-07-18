package games.Hex;

import controllers.PlayAgent;
import games.ArenaTrain;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;

import java.io.IOException;

import javax.swing.*;

public class ArenaTrainHex extends ArenaTrain {

    public ArenaTrainHex(){ super();}

//    public ArenaTrainHex(JFrame frame) {
//        super(frame);
//    }

	public ArenaTrainHex(String title) {
		super(title);		
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
		ArenaTrainHex t_Frame = new ArenaTrainHex("General Board Game Playing");

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[ArenaTrainHex.main] args="+args+" not allowed. Use batch facility.");
		}
	}
	
}
