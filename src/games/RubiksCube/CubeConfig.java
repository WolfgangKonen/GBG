package games.RubiksCube;

import java.util.Random;
import params.ParOther;

public class CubeConfig {
	
	/**
	 * What type of cube is it?
	 * <ul>
	 * <li> <b>POCKET</b>: 2x2x2 pocket cube
	 * <li> <b>RUBIKS</b>: 3x3x3 Rubik's cube 		
	 * </ul>
	 * @see ArenaTrainCube#setCubeType(String)
	 * 
	 */
	public static enum CubeType {POCKET,RUBIKS}

	/**
	 * @see ArenaTrainCube#setCubeType(String) 
	 */
	public static CubeType cubeType = CubeType.POCKET;
	
	/**
	 * What does a board vector state in {@link StateObserverCube} represent?. The possible options are
	 * <ul>
	 * <li> <b>CUBESTATE</b>: only the cube state (what {@link CubeState#fcol} holds)
	 * <li> <b>CUBEPLUSACTION</b>: the cube state plus the action which led to this state 
	 * <li> <b>STICKER</b>: the sticker representation in one-hot encoding (7*7 cells for 2x2x2)
	 * <li> <b>STICKER2</b>: the sticker representation in compact encoding (7*2 cells for 2x2x2)
	 * </ul>
	 * @see ArenaTrainCube#setBoardVecType(String)
	 */
	public enum BoardVecType {CUBESTATE, CUBEPLUSACTION, STICKER, STICKER2}
	
	/**
	 * {@link BoardVecType} {@code boardVecType} holds the board vector type for all {@link CubeState} objects and 
	 * is used in {@link CubeState#getBoardVector()} and in {@link XNTupleFuncsCube}.
	 * 
	 * @see ArenaTrainCube#setBoardVecType(String) 
	 */
	public static BoardVecType boardVecType = BoardVecType.CUBESTATE;		// CUBESTATE, CUBEPLUSACTION, STICKER

	/**
	 * What type of twists are allowed?
	 * <ul>
	 * <li> <b>ALLTWISTS</b>: all twists, quarter and half twists (i.e. U1, U2, U3)
	 * <li> <b>QUARTERTWISTS</b>: only quarter twists (i.e. U1, U3) 		
	 * </ul>
	 * @see ArenaTrainCube#setTwistType(String)
	 */
	public enum TwistType {ALLTWISTS, QUARTERTWISTS}
	public static TwistType twistType = TwistType.ALLTWISTS;
	
	/**
	 * <b>{@code pMax}</b> serves several purposes:
	 * <ul>
	 * <li> Maximum number of scrambling twists in {@link GameBoardCube#chooseStartState()} (when playing) and 
	 * 		{@link GameBoardCube#chooseStartState(controllers.PlayAgent)} (when training an agent).
	 * <li> Maximum number of scrambling twists in {@link EvaluatorCube}
	 * <li> (Deprecated) Up to which p the distance set arrays D[p] and T[p] in {@link GameBoardCube} are filled.
	 * </ul>
	 * In case that {@link GameBoardCubeGui} is present, the value of  <b>{@code pMax}</b> will be updated from 
	 * {@link ParOther} {@code opar.pMax} each time a train or evaluation process is started. This allows to switch  
	 * <b>{@code pMax}</b> at runtime.
	 */
	public static int pMax = 10;			// 1,2,3,4,5,6,7,8,9,10,11,12,13,14   for 2x2x2 cube

	/**
	 * The cost-to-go for a transition from one state s to the next state s'. Used as part of the reward in
	 * DAVI2Agent, DAVI3Agent, TDNTuple3Agt and StateObserverCube.
	 */
	public static double stepReward = (CubeConfig.cubeType==CubeType.POCKET) ? -0.04 : -0.1;  //-0.01;

	/**
	 * whether a replay buffer with certain capacity and batch size is used or not
	 */
	public static boolean REPLAYBUFFER = true;  // false;true; //

	public static int replayBufferCapacity = 500;
	public static int batchSize = 50;


	/**
	 * This influences the behavior in {@link GameBoardCube#selectByTwists1(int) GameBoardCube.selectByTwists1(p)} 
	 * when forming the twist sequence:
	 * <ul>
	 * <li> <b>true</b>: the selected twists can come in any sequence, e.g. "...U1U2..." is allowed. This requires however 
	 * 		not really two twists because the <b>doublet</b> "U1U2" can be realized as well by one twist "U3". As a 
	 * 		consequence, the twist sequence in this case can be solved often with much less twists than p.
	 * <li> <b>false</b>: doublets are not allowed in the twist sequence. As a consequence, the twist sequence in this 
	 * 		case is likely to require at least p (or p-1) twists to be solved.
	 * </ul>
	 * Detail: In case {@link #twistType}=={@link TwistType}<b>{@code .ALLTWISTS}</b>, the forbidden doublets are 
	 * 		U*U*, L*L*, F*F* with *=1,2,3. <br>
	 * 		In case {@link #twistType}=={@link TwistType}<b>{@code .QUARTERTWISTS}</b>, the forbidden doublets are 
	 * 		U1U3, U3U1, L1L3, L3L1, F1F3, F3F1.
	 */
	final static boolean TWIST_DOUBLETS = false;
	
	/**
	 * EvalNmax: how many states to pick randomly for each p during evaluation
	 */
//		                           0           4                   8
//	public final static int[] EvalNmax = {0,10,50,50, 300, 300, 300, 500, 500,2000,2000,2000};
	public final static int EvalNmax = 200;


	//
	// Elements below are only for now deprecated cases:
	//

	/**
	 * The larger EVAL_EPILENGTH, the larger is the success percentage of {@link EvaluatorCube}, mode=1.<br>
	 * At the same time, a large EVAL_EPILENGTH makes {@link EvaluatorCube} much slower (e. g. during training).
	 */
//	final static int EVAL_EPILENGTH = 12;		// 12 or 50 (should be > pMax)
	// is now replaced with ParOther.stopEval

	/**
	 * Size array for {@link GameBoardCube#generateDistanceSets(Random)}:
	 * Narr[p] is the number of elements to pick from D[p] when generating D[p+1]. So the size
	 * of D[p+1] will be roughly 6*Narr[p].
	 */
	@Deprecated
	final static int[] Narr = {0,0,9,54, 321,1847,5000,5000, 9900,50,50,50}; // for GenerateNext
//	final static int[] Narr = {0,0,9,20, 100, 300, 900,1800, 9900,50,50,50}; // for GenerateNext
//	final static int[] Narr = {0,0,9,50, 150,600,3000,15000, 50,50,50,50};  // for GenerateNextColSymm
//		                       0         4                   8

	/**
	 * Selector array for {@link GameBoardCube#chooseStartState(controllers.PlayAgent)}.<br>
	 * Needed only in the now <b>deprecated</b> ({@link GameBoardCube#SELECT_FROM_D SELECT_FROM_D}{@code ==true})-case.
	 * <p> 
	 * Set X=Xper[{@link CubeConfig#pMax}]. 
	 * If the proportion of training games is in the first X[1] percent, select from D[1], 
	 * if it is between X[1] and X[2] percent, select from D[2], and so on.  
	 */
	@Deprecated
	final static double[][] Xper = 							// 1st index:
			new double[][]{{0.0}, {0.0,1.0}, {0,0.2,1.0}	// [0],[1],[2]
				,{0,0.1,0.2,1.0},{0,0.1,0.2,0.5,1.0}		//,[3],[4]
				,{0,0.05,0.10,0.25,0.5,1.0}					//,[5]
				,{0,0.025,0.05,0.10,0.25,0.5,1.0}			//,[6]
				,{0,0.0125,0.025,0.05,0.125,0.25,0.5,1.0}	//,[7]
			};

	/**
	 * theoCov[p] is the known maximum size of distance set D[p] (theoretical coverage, 2x2x2 cube, see
	 *  <a href="https://en.wikipedia.org/wiki/Pocket_Cube">https://en.wikipedia.org/wiki/Pocket_Cube</a>.
	 */
	final static
	int[] theoCov = {1,9,54,321,  	// the known maximum sizes (ALLTWIST case) for D[0],D[1],D[2],D[3] ...
			1847,9992,50136,227536,	// ... and D[4],D[5],D[6],D[7],
			870072,1887748,623800,	// ... and D[8],D[9],D[10],D[7],
			2644					// ... and D[11]
	};

}
