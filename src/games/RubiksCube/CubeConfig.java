package games.RubiksCube;

import params.ParOther;

public class CubeConfig {
	
	/**
	 * What type of cube is it?
	 * <ul>
	 * <li> <b>POCKET</b>: 2x2x2 pocket cube
	 * <li> <b>RUBIKS</b>: 3x3x3 Rubik's cube 		
	 * </ul>
	 * @see ArenaCube#setCubeType(String)
	 * 
	 */
	public enum CubeSize {POCKET,RUBIKS}

	/**
	 * @see ArenaCube#setCubeType(String)
	 */
	public static CubeSize cubeSize = CubeSize.POCKET;
	
	/**
	 * What does a board vector state in {@link StateObserverCube} represent?. The possible options are
	 * <ul>
	 * <li> <b>CUBESTATE</b>: only the cube state (what {@link CubeState#fcol} holds)
	 * <li> <b>CUBEPLUSACTION</b>: the cube state plus the action which led to this state 
	 * <li> <b>STICKER</b>: the sticker representation in one-hot encoding (7*7 cells for 2x2x2) (deprecated)
	 * <li> <b>STICKER2</b>: the sticker representation in compact encoding (7*2 cells for 2x2x2)
	 * </ul>
	 * @see ArenaCube#setBoardVecType(String)
	 */
	public enum BoardVecType {CUBESTATE, CUBEPLUSACTION, STICKER, STICKER2}
	
	/**
	 * {@link BoardVecType} {@code boardVecType} holds the board vector type for all {@link CubeState} objects and 
	 * is used in {@link CubeState#getBoardVector()} and in {@link XNTupleFuncsCube}.
	 * 
	 * @see ArenaCube#setBoardVecType(String)
	 */
	public static BoardVecType boardVecType = BoardVecType.CUBESTATE;

	/**
	 * What type of twists are allowed?
	 * <ul>
	 * <li> <b>HTM</b>: half-turn metric, quarter and half twists (i.e. U1, U2, U3)
	 * <li> <b>QTM</b>: quarter-turn metric, only quarter twists (i.e. U1, U3)
	 * </ul>
	 * @see ArenaCube#setTwistType(String)
	 */
	public enum TwistType {HTM, QTM}
	public static TwistType twistType = TwistType.HTM;
	
	/**
	 * <b>{@code pMax}</b> serves several purposes:
	 * <ul>
	 * <li> Maximum number of scrambling twists in {@link GameBoardCube#chooseStartState()} (when playing) and 
	 * 		{@link GameBoardCube#chooseStartState(controllers.PlayAgent)} (when training an agent).
	 * <li> Maximum number of scrambling twists in {@link EvaluatorCube}
	 * <li> (Deprecated) Up to which p the distance set arrays D[p] and T[p] in {@link GameBoardCube} are filled.
	 * </ul>
	 * In case that {@link GameBoardCubeGui} is present, the value of  <b>{@code pMax}</b> will be updated from 
	 * {@link ParOther}'s {@code pMax} each time a train, play or evaluation process is started. This allows to
	 * switch <b>{@code pMax}</b> at runtime.
	 */
	public static int pMax = 10;			// 1,2,3,4,5,6,7,8,9,10,11,12,13,14   for 2x2x2 cube

	/**
	 * Minimum number of scrambling twists in {@link EvaluatorCube} and in
	 * {@link GameBoardCube#chooseStartState()} (when playing a game).
	 * <p>
	 * In case that {@link GameBoardCubeGui} is present, the value of  <b>{@code pMin}</b> will be updated from
	 * {@link ParOther}'s {@code pMin} each time a play or evaluation process is started. This allows to
	 * switch <b>{@code pMin}</b> at runtime.
	 */
	public static int pMin = 7;

	/**
	 * The cost-to-go for a transition from one state s to the next state s'. Used as part of the reward in
	 * DAVI2Agent, DAVI3Agent, DAVI4Agent, TDNTuple3Agt, TDNTuple4Agt (through {@link StateObserverCube#getStepRewardTuple()}).
	 */
	public static double stepReward = (CubeConfig.cubeSize == CubeSize.POCKET) ? -0.04 : -0.1;  //-0.01;

	/**
	 * whether a replay buffer with certain capacity and batch size is used or not
	 */
	public static boolean REPLAYBUFFER = false;  // false;true; //

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
	 * Detail: In case {@link #twistType}=={@link TwistType}<b>{@code .HTM}</b>, the forbidden doublets are
	 * 		U*U*, L*L*, F*F* with *=1,2,3. <br>
	 * 		In case {@link #twistType}=={@link TwistType}<b>{@code .QTM}</b>, the forbidden doublets are
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

	// --- is now replaced with ParOther.stopEval ---
//	/**
//	 * The larger EVAL_EPILENGTH, the larger is the success percentage of {@link EvaluatorCube}, mode=1.<br>
//	 * At the same time, a large EVAL_EPILENGTH makes {@link EvaluatorCube} much slower (e.g. during training).
//	 */
//	final static int EVAL_EPILENGTH = 12;		// 12 or 50 (should be > pMax)

	// --- is only needed in test/PocketCubeTest, where we define it locally ---
//	/**
//	 * theoCov[p] is the known maximum size of distance set D[p] (theoretical coverage, 2x2x2 cube, see
//	 *  <a href="https://en.wikipedia.org/wiki/Pocket_Cube">https://en.wikipedia.org/wiki/Pocket_Cube</a>).
//	 */
//	final static
//	int[] theoCov = {1,9,54,321,  	// the known maximum sizes (ALLTWIST case) for D[0],D[1],D[2],D[3] ...
//			1847,9992,50136,227536,	// ... and D[4],D[5],D[6],D[7],
//			870072,1887748,623800,	// ... and D[8],D[9],D[10],D[7],
//			2644					// ... and D[11]
//	};

}
