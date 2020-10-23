package games.RubiksCube;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;
import games.RubiksCube.CubeConfig.BoardVecType;
import games.RubiksCube.CubeConfig.CubeType;
import games.RubiksCube.CubeConfig.TwistType;
import games.ArenaTrain;

/**
 * {@link ArenaTrain} for Rubik's Cube. It borrows all functionality
 * from the general class {@link ArenaTrain} derived from {@link Arena}. It only overrides 
 * the abstract methods <ul>
 * <li> {@link Arena#makeGameBoard()}, 
 * <li> {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int)}, and 
 * <li> {@link Arena#makeFeatureClass(int)}, 
 * </ul> such that 
 * these factory methods return objects of class {@link GameBoardCube}, 
 * {@link EvaluatorCube}, and {@link FeatureCube}, respectively.
 * 
 * @see GameBoardCube
 * @see EvaluatorCube
 * 
 * @author Wolfgang Konen, TH Koeln, 2018-2020
 */
public class ArenaTrainCube extends ArenaTrain   {

	public ArenaTrainCube(String title, boolean withUI) {
		super(title,withUI);
		CubeStateFactory.generateInverseTs();
		CubeState.generateForwardTs();
	}
	
	/**
	 * @return a name of the game, suitable as subdirectory name in the 
	 *         {@code agents} directory
	 *         
	 * @see GameBoardCube#getSubDir() 
	 */
	@Override
	public String getGameName() {
		return "RubiksCube";
	}
	
	/**
	 * Factory pattern method
	 */
	@Override
	public GameBoard makeGameBoard() {
		CubeStateFactory.generateInverseTs();	// since makeGameBoard is called via super --> Arena
		CubeState.generateForwardTs();			// prior to finishing ArenaTrainCube(String,boolean)
		gb = new GameBoardCube(this);

		// optional, to print out invU and invL, given invF:
//		CubeStateFactory csfactory = new CubeStateFactory();
//		csfactory.makeCubeState().show_invF_invL_invU();		// once to print out the arrays needed for invL and invU (see CubeState3x3)

		return gb;
	}

	/**
	 * Factory pattern method: make a new Evaluator
	 * @param pa		the agent to evaluate
	 * @param gb		the game board
	 * @param stopEval	maximum episode length {@code epiLength} during evaluation
	 * @param mode		which evaluator mode: -1,0,1. Throws a runtime exception
	 * 					if {@code mode} is not in the set {@link Evaluator#getAvailableModes()}.
	 * @param verbose	how verbose or silent the evaluator is
	 * @return
	 */
	@Override
	public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
//		if (mode==-1) mode=EvaluatorCube.getDefaultEvalMode();
		return new EvaluatorCube(pa,gb,stopEval,mode,verbose);
	}

	@Override
	public Feature makeFeatureClass(int featmode) {
		return new FeatureCube(featmode);
	}

	@Override
	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsCube();
	}

    /**
     * set the cube type (POCKET or RUBIKS) for Rubik's Cube
     */
    public static void setCubeType(String sCube) {
		switch (sCube) {
			case "2x2x2" -> CubeConfig.cubeType = CubeType.POCKET;
			case "3x3x3" -> CubeConfig.cubeType = CubeType.RUBIKS;
			default -> throw new RuntimeException("Cube type " + sCube + " is not known.");
		}
    }

    /**
     * set the board vector type for Rubik's Cube
     */
    public static void setBoardVecType(String bvType) {
		switch (bvType) {
			case "CSTATE" -> CubeConfig.boardVecType = BoardVecType.CUBESTATE;
			case "CPLUS" -> CubeConfig.boardVecType = BoardVecType.CUBEPLUSACTION;
			case "STICKER" -> CubeConfig.boardVecType = BoardVecType.STICKER;
			case "STICKER2" -> CubeConfig.boardVecType = BoardVecType.STICKER2;
			default -> throw new RuntimeException("Board vector type " + bvType + " is not known.");
		}
    }

    /**
     * set the twist type (ALLTWISTS or QUARTERTWISTS) for Rubik's Cube
     */
    public static void setTwistType(String tCube) {
		switch (tCube) {
			case "ALL" -> CubeConfig.twistType = TwistType.ALLTWISTS;
			case "QUARTER" -> CubeConfig.twistType = TwistType.QUARTERTWISTS;
			default -> throw new RuntimeException("Twist type " + tCube + " is not known.");
		}
    }

	/**
	 * Start GBG for Rubik's Cube (trainable version)
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		ArenaTrainCube t_Frame = new ArenaTrainCube("General Board Game Playing",true);

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[ArenaTrainCube.main] args="+args+" not allowed. Use batch facility.");
		}
	}
	
}
