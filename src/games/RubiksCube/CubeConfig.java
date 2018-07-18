package games.RubiksCube;

public class CubeConfig {
	/**
	 * theoCov[p] is the known maximum size of distance set D[p] (theoretical coverage)
	 */
	final static 
	int[] theoCov = {1,9,54,321,  	// the known maximum sizes for D[0],D[1],D[2],D[3] ...
			1847,9992,50136,227536,	// ... and D[4],D[5],D[6],D[7],
			870072,1887748,623800,	// ... and D[8],D[9],D[10],D[7],
			2644					// ... and D[11]
	};
	/**
	 * Size array for {@link GameBoardCube#generateDistanceSets(Random)}:
	 * Narr[p] is the number of elements to pick from D[p] when generating D[p+1]. So the size
	 * of D[p+1] will be roughly 6*Narr[p].
	 */
//	final static int[] Narr = {0,0,9,20, 100, 300, 900,1800, 9900,50,50,50}; // for GenerateNext
	final static int[] Narr = {0,0,9,54, 321,1847,5000,5000, 9900,50,50,50}; // for GenerateNext
//	final static int[] Narr = {0,0,9,50, 150,600,3000,15000, 50,50,50,50};  // for GenerateNextColSymm
//		                       0         4                   8

	/**
	 * EvalNmax: how many states to pick randomly from each distance set T[p]
	 */
	final static int[] EvalNmax = {0,10,50,300, 2000,2000,2000,2000, 2000,2000,2000,2000};
//		                           0            4                    8

	/**
	 * The larger EVAL_EPILENGTH, the larger is the success percentage of {@link EvaluatorCube}, mode=1.<br> 
	 * At the same time, a large EVAL_EPILENGTH makes {@link EvaluatorCube} much slower (e. g. during training).  	
	 */
	final static int EVAL_EPILENGTH = 12;		// 12 or 50 (should be > pMax)
	
	/**
	 * Up to which p the distance set arrays D[p] and T[p] in {@link GameBoardCube} is filled.
	 */
	public final static int pMax = 4;			// 3,4,5,6,7
	
	/**
	 * Selector array for {@link GameBoardCube#chooseStartState(controllers.PlayAgent)}.
	 * <p> 
	 * Set X=Xper[{@link CubeConfig#pMax}]. 
	 * If the proportion of training games is in the first X[1] percent, select from D[1], 
	 * if it is between X[1] and X[2] percent, select from D[2], and so on.  
	 */
	final static double[][] Xper = 							// 1st index:
			new double[][]{{0.0}, {0.0,1.0}, {0,0.2,1.0}	// [0],[1],[2]
				,{0,0.1,0.2,1.0},{0,0.1,0.2,0.5,1.0}		//,[3],[4]
				,{0,0.05,0.10,0.25,0.5,1.0}					//,[5]
				,{0,0.025,0.05,0.10,0.25,0.5,1.0}			//,[6]
				,{0,0.0125,0.025,0.05,0.125,0.25,0.5,1.0}	//,[7]
			};

	/**
	 * What does a state in {@link StateObserverCube} represent? <ul>
	 * <li> <b>CUBESTATE</b>: only the cube state (what {@link CubeState#fcol} holds)
	 * <li> <b>CUBEPLUSACTION</b>: the cube state plus the action which led to this state 		
	 * </ul>
	 */
	public enum StateType {CUBESTATE, CUBEPLUSACTION};
	
	final static StateType stateCube = StateType.CUBESTATE;
//	final static StateType stateCube = StateType.CUBEPLUSACTION;
}
