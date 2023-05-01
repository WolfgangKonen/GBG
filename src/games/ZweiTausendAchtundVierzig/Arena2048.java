package games.ZweiTausendAchtundVierzig;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;
import games.ZweiTausendAchtundVierzig.Heuristic.Evaluator2048_EA;

import java.io.IOException;

public class Arena2048 extends Arena {
	public Arena2048(String title, boolean withUI) {
		super(title,withUI);		
	}

    public Arena2048(String title, boolean withUI, boolean withTrainRights) {
        super(title,withUI,withTrainRights);
    }

    /**
     * @return a name of the game, suitable as subdirectory name in the
     *         {@code agents} directory
     */
    public String getGameName() {
        return "2048";
    }

    /**
     * Factory pattern method: make a new GameBoard
     * @return	the game board
     */
    public GameBoard makeGameBoard() {
        gb = new GameBoard2048(this);
        return gb;
    }
    /**
     * Factory pattern method: make a new Evaluator
     * @param pa        the agent to evaluate
     * @param gb        the game board
     * @param mode        which evaluator mode: 0,1,2. Throws a runtime exception
     * 					if {@code mode} is not in the set {@link Evaluator#getAvailableModes()}.
     * @param verbose    how verbose or silent the evaluator is
     * @return
     */
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int mode, int verbose) {
    	return makeEvaluator(pa, gb, mode, verbose, this);
    }
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int mode, int verbose, Arena ar) {
        switch (mode) {
        	case -1:
                return new Evaluator2048(pa, gb, -1, verbose,ar);
            case 0:
                return new Evaluator2048(pa, gb, 0, verbose,ar);
            case 1:
                return new Evaluator2048_BoardPositions(pa, gb, verbose);
            case 2:
                return new Evaluator2048_EA(pa, gb, verbose);
            default:
                throw new RuntimeException("Mode " + mode + " is not allowed for 2048");
        }
    }

	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncs2048();
	}

    public Feature makeFeatureClass(int featmode) {
        return new Feature2048(featmode);
    }

//    public void performArenaDerivedTasks() {}
    
	/**
	 * Start GBG for 2048 (non-trainable version)
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		Arena2048 t_Frame = new Arena2048("General Board Game Playing",true);

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[Arena2048.main] args="+args+" not allowed. ");
		}
	}
	

}
