package games.Othello;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;

import games.*;

import controllers.TD.ntuple2.NTupleFactory;
import tools.Types.ACTIONS;


public class XNTupleFuncsOthello extends XNTupleBase implements XNTupleFuncs, Serializable {

	private final static int[] fixedModes = {0, 1, 2, 3, 4, 5, 6};

//	private int numPositionValues = 3;  // either P=3, with 0="X" (BLACK), 1="O" (WHITE), 2=empty
	private int numPositionValues = 4;  // or     P=4, with 0="X" (BLACK), 1="O" (WHITE), 2=non-reachable-empty, 3=reachable-empty
	// [Having numPositionValues as a member allows it that we can restore both versions, those
	//  with P=3 and those with P=4, from disk.]
	
    /**
     * Provide a version UID here. Change the version UID for serialization only if a newer version is no 
     * longer compatible with an older one (older .gamelog or .agt.zip containing this object will
     * become unreadable)  
     */
    private static final long serialVersionUID = 42L;
    
	private final int[] actionVector = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8 , 9, 10, 11, 12, 13, 14, 15 , 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30 , 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 , 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63};
	private transient BoardVector[] bv_symmetryActions; //Gives a 2D representation of all SymmetryVectors
	private final int[][] actionPositions; //Given an action key, it gives all symmetric actions

	public XNTupleFuncsOthello() {
		bv_symmetryActions = symmetryVectors(new BoardVector(actionVector),0);
		actionPositions = new int[actionVector.length][bv_symmetryActions.length];
		for (int i = 0; i < actionPositions.length; i++) 
		{
			for(int j = 0; j < bv_symmetryActions.length; j++)
			{
				actionPositions[i][j] = indexOf(bv_symmetryActions[j].bvec, i);
			}
		}
	}

	private int indexOf(int[] arr, int j) {
		for (int i = 0; i < arr.length; i++)
		{
			if (arr[i] == j) return i;
		}
		throw new RuntimeException("indexOf: Arr does not contain " + j);
	}

	@Override
	public boolean instantiateAfterLoading() { 
		bv_symmetryActions = symmetryVectors(new BoardVector(actionVector),0);
		if (numPositionValues==0) { 	// older stored agents did not have this member
			numPositionValues=3;		// --> 	the older agents had the (P=3)-version, so we 
		}								// 		restore it this way 
		return true; 
	}

	/**
	 * @return integer of total board cells
	 */
	@Override
	public int getNumCells() {
		return ConfigOthello.BOARD_SIZE * ConfigOthello.BOARD_SIZE;
	}

	/**
	 * @return the number P of position values 0, 1, 2,..., P-1 that each board cell 
	 * can have. For Othello: 
	 * <ul>
	 * <li> either P=3, with 0="X" (BLACK), 1="O" (WHITE), 2=empty
	 * <li> or     P=4, with 0="X" (BLACK), 1="O" (WHITE), 2=non-reachable-empty, 3=reachable-empty 
	 * 		(<b>recommended</b> case)
	 * </ul> 
	 * 
	 * @see #getBoardVector(StateObservation)
	 */
	@Override
	public int getNumPositionValues() {
		return numPositionValues;
	}

	/**
	 * @return number of Players
	 */
	@Override
	public int getNumPlayers() {
		return 2;
	}
	
	/**
	 * @return the maximum number of symmetries in this game
	 */
	public int getNumSymmetries() {
		return 8;
	}
	
/**
	 * The board vector is an {@code int[]} vector where each entry corresponds to one 
	 * cell of the board. In the case of Othello the mapping is
	 * <pre>
	 * 00 01 02 03 04 05 06 07			
	 * 08 09 10 11 12 13 14 15			
	 * 16 17 18 19 20 21 22 23			
	 * 24 25 26 27 28 29 30 31 
	 * 32 33 34 35 36 37 38 39	
	 * 40 41 42 43 44 45 46 47	
	 * 48 49 50 51 52 53 54 55
	 * 56 57 58 59 60 61 62 63
	 * </pre>
	 * @param so the StateObservation of the current game state
	 * @return a vector of length {@link #getNumCells()}, holding for each board cell its 
	 * position value with 0 = BLACK , 1 = WHITE, 2 = EMPTY.
	 */
	@Override
	public BoardVector getBoardVector(StateObservation so) {
		assert ( so instanceof StateObserverOthello);
		int[][] gameState = ((StateObserverOthello) so).getCurrentGameState();
		int[] retVal = new int[getNumCells()];
		for(int i = 0, n = 0; i < ConfigOthello.BOARD_SIZE; i++){
			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++, n++)
				retVal[n] = gameState[i][j];
		}
		if (this.getNumPositionValues()==4) {
			ArrayList<ACTIONS> availActs = so.getAvailableActions();
            for(int i = 0; i < availActs.size(); ++i) {
            	retVal[availActs.get(i).toInt()] = 3;
            }
		}
		return new BoardVector(retVal);
	}
	
	/**
	 * Given a board vector from {@link #getBoardVector(StateObservation)} and given that the 
	 * game has s symmetries, return an array which holds s symmetric board vectors: <ul>
	 * <li> the first row {@code boardArray[0]} is the board vector itself
	 * <li> the other rows are the board vectors when transforming {@code boardVector}
	 * 		according to the s-1 other symmetries (e. g. rotation, reflection, if applicable).
	 * </ul>
	 * In the case of Othello we have s=8 symmetries (4 board rotations * 2 board vertical Mirrors)
	 * 
	 * @param boardVector
	 * @return boardArray
	 */
	@Override
	public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
		//int s = 16; // Read the comment above!
		int s = 8;
		BoardVector[] symmetryVectors = new BoardVector[s];
		symmetryVectors[0] = boardVector;

		for(int i = 1; i < 4; i++) {
			symmetryVectors[i] = rotate(symmetryVectors[i-1]);
		}
		symmetryVectors[4] = mirrorHorizontally(symmetryVectors[0]);
		for(int j = 5; j < 8; j++)
		{
			symmetryVectors[j] = rotate(symmetryVectors[j-1]);
		}
		return symmetryVectors;
	}

	/**
	 * Helper function for  {@link #symmetryVectors(BoardVector, int)} :
	 * Rotates the given boardVector 90 degrees clockwise
	 * 
	 * <pre>
	 * 
	 * 00 01 02 03 04 05 06 07			56 48 40 32 24 16 08 00
	 * 08 09 10 11 12 13 14 15			57 49 41 33 25 17 09 01
	 * 16 17 18 19 20 21 22 23			58 50 42 34 26 18 10 02
	 * 24 25 26 27 28 29 30 31  ---> 	59 51 43 35 27 19 11 03
	 * 32 33 34 35 36 37 38 39			60 52 44 36 28 20 12 04
	 * 40 41 42 43 44 45 46 47			61 53 45 37 29 21 13 05
	 * 48 49 50 51 52 53 54 55			62 54 46 38 30 22 14 06
	 * 56 57 58 59 60 61 62 63			63 55 47 39 31 23 15 07
	 * 
	 * </pre>
	 * @param boardVector
	 * @return rotatedBoard
	 */
	private BoardVector rotate(BoardVector boardVector)
	{	    
		int[] bvec = boardVector.bvec;
		//		int[] rotationIndex = new int[] { 56, 48, 40, 32, 24, 16, 8, 0, 57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35, 27, 19, 11, 03, 
		//			60, 52, 44, 36, 28, 20, 12, 4, 61, 53, 45, 37, 29, 21, 13, 5, 62, 54, 46, 38, 30, 22, 14, 6, 63, 55, 47, 39, 31, 23, 15, 7 };

		int[] result = new int[bvec.length];
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++)
		{
			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++)
			{
				int oldPosition = i * ConfigOthello.BOARD_SIZE + j;
				int newPosition = (ConfigOthello.BOARD_SIZE * ConfigOthello.BOARD_SIZE - ConfigOthello.BOARD_SIZE) + i - (j * ConfigOthello.BOARD_SIZE);

				result[newPosition] = bvec[oldPosition];
			}	
		}
		for(int i = 0; i < result.length / 2; i++)
		{
			int temp = result[i];
			result[i] = result[result.length - 1 - i];
			result[result.length - 1 - i] = temp;
		}

		return new BoardVector(result);

	}

	// --- mirrorDiagonally: never used ---
//	/**
//	 * Helper function for  {@link #symmetryVectors(int[])}: 
//	 * Mirrors the board along its diagonal from top left to bottom right
//	 * 
//	 * <h4>Unused</h4>
//	 * 
//	 * <pre>
//	 * 
//	 * 00 01 02 03 04 05 06 07			00 08 16 24 32 40 48 56
//	 * 08 09 10 11 12 13 14 15			01 09 17 25 32 41 49 57
//	 * 16 17 18 19 20 21 22 23			02 10 18 26 33 42 50 58
//	 * 24 25 26 27 28 29 30 31  ---> 	03 11 19 27 34 43 51 59
//	 * 32 33 34 35 36 37 38 39			04 12 20 28 35 44 52 60
//	 * 40 41 42 43 44 45 46 47			05 13 21 29 36 45 53 61
//	 * 48 49 50 51 52 53 54 55			06 14 22 30 37 46 54 62
//	 * 56 57 58 59 60 61 62 63			07 15 23 31 38 47 55 63
//	 * 
//	 * </pre>
//	 * @param boardVector
//	 * @return
//	 */
//	private int[] mirrorDiagonally(int[] boardVector)
//	{
//		int[] result = new int[boardVector.length];
//		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++)
//		{
//			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++)
//			{
//				int oldPosition = i * ConfigOthello.BOARD_SIZE + j;
//				int newPosition = j * ConfigOthello.BOARD_SIZE + i;
//
//				result[newPosition] = boardVector[oldPosition];
//			}	
//		}
//		return result;
//	}
	
	/**
	 * Helper function for  {@link #symmetryVectors(BoardVector, int)} :
	 * Mirrors the board along its horizontal central axis
	 * 
	 * <pre>
	 * 
	 * 00 01 02 03 04 05 06 07	        56 57 58 59 60 61 62 63																												
	 * 08 09 10 11 12 13 14 15	        48 49 50 51 52 53 54 55
	 * 16 17 18 19 20 21 22 23	        40 41 42 43 44 45 46 47
	 * 24 25 26 27 28 29 30 31  --to-- 	32 33 34 35 36 37 38 39
	 * 32 33 34 35 36 37 38 39	        24 25 26 27 28 29 30 31
	 * 40 41 42 43 44 45 46 47	        16 17 18 19 20 21 22 23
	 * 48 49 50 51 52 53 54 55	        08 09 10 11 12 13 14 15
	 * 56 57 58 59 60 61 62 63	        00 01 02 03 04 05 06 07
	 * 
	 * </pre>
	 * @param boardVector
	 * @return
	 */
	public BoardVector mirrorHorizontally(BoardVector boardVector)		// WK this is the new, correct version
	{
		int BS = ConfigOthello.BOARD_SIZE;
		int[] result = boardVector.bvec.clone();
		int offset = BS*BS;
		for(int i = 0; i < result.length / 2; i++)
		{
			if ((i%8)==0) offset -= BS;
			int newI = offset+(i%8);
			int temp = result[i];
			result[i] = result[newI];
			result[newI] = temp;
		}
		return new BoardVector(result);
	}

//	public int[] mirrorHorizontallyOLD(int[] boardVector)		// WK this is the old, buggy version
//	{
//		int[] result = new int[boardVector.length];
//		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++)
//		{
//			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++)
//			{
//				int oldPosition = (ConfigOthello.BOARD_SIZE - 1 - i) * ConfigOthello.BOARD_SIZE + (ConfigOthello.BOARD_SIZE - 1 - j);
//				result[i * ConfigOthello.BOARD_SIZE + j] = boardVector[oldPosition];
//			}
//		}
//		return result;
//	}

	/**
	 * Given a certain board array of symmetric (equivalent) states for state <b>{@code so}</b> 
	 * and a certain action to be taken in <b>{@code so}</b>, generate the array of equivalent 
	 * action keys {@code equivAction} for the symmetric states.
	 * <p>
	 * This method is needed only for Q-learning and Sarsa.
	 * 
	 * @param actionKey the key of the action to be taken in <b>{@code so}</b> 
	 * @return <b>equivAction</b> array of the equivalent actions' keys. 
	 * <p>
	 * equivAction[i] is the key of the action equivalent to actionKey in the
	 * i'th equivalent board vector equiv[i] = {@link #symmetryVectors(BoardVector, int)}[i]
	 */
	@Override
	public int[] symmetryActions(int actionKey) 
	{
		return actionPositions[actionKey];
	}

	/** 
	 * Return a fixed set of {@code numTuples} n-tuples suitable for that game. 
	 * Different n-tuples may have different lengths. An n-tuple {0,1,4} means a 3-tuple 
	 * containing the cells 0, 1, and 4.<p>
	 * 
	 * Other options than fixed n-tuples (that is, the generation of random n-tuples) are 
	 * provided by {@link NTupleFactory#makeNTupleSet(params.ParNT, XNTupleFuncs)}
	 * 
	 * The first N-Tuple Architecture is the Network proposed by Wojciech Jaskowski in his paper "Systematic N-Tuple Networks for Othello position evaluation" (2014)
	 * 
	 * @param mode one of the values from {@link #fixedNTupleModesAvailable()}
	 * @return nTuples[numTuples][]
	 * 
	 * @see NTupleFactory#makeNTupleSet(params.ParNT, XNTupleFuncs)
	 */
	@Override
	public int[][] fixedNTuples(int mode) 
	{
		switch(mode)
		{
		//--- 10 1-Tuples
		case 0:	return new int[][] {
			{0}, 
			{8}, {9},
			{16}, {17}, {18},
			{24}, {25}, {26}, {27}
		};
		//--- 32 Straight 2-Tuple
		case 1: return new int[][] {
			{0, 8}, {0, 9}, {1, 9}, {2, 10}, {3, 11},
			{8, 16}, {8, 17}, {9, 17}, {9, 18}, {10, 18}, {11, 19},
			{16, 24}, {16, 25}, {17, 25}, {17, 26}, {18, 26}, {18, 27}, {19, 27},
			{24, 32}, {24, 33}, {25, 33}, {25, 34}, {26, 34}, {26, 35}, {27, 35}, {27, 36},
			{32, 41}, {33, 42}, {34, 43},
			{40, 49}, {41, 50},
			{48, 57}
		};
		//--- 24 Straight 3-Tuple
		case 2: return new int[][] {
			{0, 8, 16}, {0, 9, 18}, {1, 9, 17}, {2, 10, 18}, {3, 11, 19},
			{8, 16, 24}, {8, 17, 26}, {9, 17, 25}, {9, 18, 27}, {10, 18, 26}, {11, 19, 27},
			{16, 24, 32}, {16, 25, 34}, {17, 25, 33}, {17, 26, 35}, {18, 27, 36}, {18, 28, 38}, {19, 28, 37},
			{24, 33, 42}, {25, 34, 43}, {26, 35, 44},
			{32, 41, 50}, {33, 42, 51},
			{40, 49, 58}
		};
		//--- 8 Random snakey bakey 4-Tuple
		case 3: return new int[][] {
			{0, 1, 8, 9}, {2, 11, 10, 3}, 
			{11, 18, 17, 24}, {15, 14, 21, 20},
			{18, 19, 26, 35}, 
			{33, 32, 40, 48},
			{44, 36, 43, 51}, {45, 53, 54, 62}
		};
		//--- Mixed NTuple1
		case 4: return new int[][] {
			
			{0,1,8,9},
			{1,2,3,4},
			{1,2,9,10},
			{0,8,16},
			{17,18,19,20,25,26},
			{3,11,19,27},
			{9,18,19,26,27},
			{2,9},
			{8,16},
			{2,10,16,17,18},
			{9,17,25,18,26,27}
			
			
		};
		//--- Mixed NTuple2
		case 5:return new int[][] {
			{0,1,8,9},
			{9,17,18,25,26},
			{10,11,19,20},
			{10,17,18,19,26},
			{18,19,20,21,27,28},
			{0,1,2,8,9,16,17}
		};
		//--- 50 randomly chosen, but fixed 6-tuples
		case 6:return new int[][] {
			  {4, 20, 5, 6, 12, 13},
			  {49, 50, 51, 40, 41, 42},
			  {33, 49, 34, 40, 41, 43},
			  {48, 33, 49, 40, 25, 42},
			  {5, 6, 7, 13, 14, 15},
			  {32, 17, 33, 18, 25, 26},
			  {1, 2, 3, 4, 9, 11},
			  {4, 5, 6, 23, 13, 14},
			  {34, 50, 35, 41, 42, 43},
			  {6, 22, 7, 23, 14, 15},
			  {49, 34, 35, 52, 42, 43},
			  {18, 2, 4, 9, 10, 11},
			  {33, 34, 18, 25, 9, 26},
			  {16, 17, 18, 24, 9, 10},
			  {32, 16, 17, 33, 24, 25},
			  {52, 58, 59, 43, 60, 61},
			  {35, 36, 37, 38, 55, 46},
			  {54, 55, 61, 62, 47, 63},
			  {17, 34, 35, 26, 42, 43},
			  {1, 2, 3, 9, 11, 12},
			  {49, 33, 34, 56, 41, 42},
			  {0, 16, 1, 8, 9, 10},
			  {19, 20, 21, 6, 12, 13},
			  {5, 6, 7, 13, 14, 15},
			  {35, 51, 36, 42, 43, 44},
			  {33, 34, 35, 26, 27, 43},
			  {51, 52, 59, 43, 60, 61},
			  {37, 38, 54, 39, 46, 31},
			  {37, 22, 7, 23, 30, 15},
			  {53, 54, 55, 62, 46, 63},
			  {34, 35, 36, 21, 41, 28},
			  {19, 26, 10, 11, 12, 28},
			  {52, 54, 55, 60, 61, 46},
			  {16, 1, 17, 8, 9, 10},
			  {6, 7, 23, 14, 15, 31},
			  {4, 21, 6, 7, 13, 14},
			  {0, 16, 1, 17, 8, 9},
			  {0, 1, 2, 8, 9, 10},
			  {33, 49, 50, 41, 42, 58},
			  {21, 37, 38, 29, 30, 46},
			  {35, 19, 53, 27, 44, 28},
			  {32, 33, 17, 18, 24, 25},
			  {16, 17, 34, 24, 8, 25},
			  {20, 36, 21, 28, 13, 29},
			  {6, 22, 23, 14, 30, 15},
			  {21, 38, 39, 12, 30, 31},
			  {1, 2, 3, 8, 10, 11},
			  {16, 17, 18, 25, 10, 11},
			  {20, 39, 27, 28, 29, 30},
			  {21, 22, 39, 30, 46, 31}
		};
		
		default: 
			//throw new OutOfRangeException(mode, 0, 6);
			throw new RuntimeException("mode = "+mode+" is not supported");
		}
	}

	@Override
	public String fixedTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>"	+ "0: 10 1-Tuples "
				+ "<br>1: 32 straight 2-tuples"	
				+ "<br>2: 24 straight 3-tuples"	
				+ "<br>3: 8 random snakey bakey 4-tuples"
				+ "<br>4: Mixed n-tuple 1"	
				+ "<br>5: Mixed n-tuple 2"	
				+ "<br>6: 50 6-tuples"	
				+ "</html>";
	}

	@Override
	public int[] fixedNTupleModesAvailable() {
		return fixedModes;
	}

	@Override
	public HashSet<Integer> adjacencySet(int iCell)
	{
		HashSet<Integer> neighbours = new HashSet<Integer>();
		int cellX = iCell / ConfigOthello.BOARD_SIZE;
		int cellY = iCell - cellX * ConfigOthello.BOARD_SIZE;
		for(int i = -1; i < 2; i++)
		{
			for(int j = -1; j < 2; j++) 
			{
				if(i == 0 && j == 0)
					continue; 

				int x = cellX + i;
				int y = cellY + j;
				if(x >= ConfigOthello.BOARD_SIZE || y >= ConfigOthello.BOARD_SIZE)
					continue;

				if(x < 0 || x > ConfigOthello.BOARD_SIZE || y < 0 || y > ConfigOthello.BOARD_SIZE) {

				}
				else {
					neighbours.add(x * ConfigOthello.BOARD_SIZE + y);
				}
			}
		}
		return neighbours;
	}

	// just some debug code to test the symmetries
	//
	public static void main(String[] args) {
		XNTupleFuncsOthello xnf = new XNTupleFuncsOthello();
		BoardVector bv2 = xnf.makeBoardVectorEachCellDifferent();
		BoardVector[] sv2 = xnf.symmetryVectors(bv2,0);
		for (int i = 0;  i < ConfigOthello.BOARD_SIZE; i++) {
			System.out.println("* i="+i+" *");
			prettyPrintBoardVector(sv2[i].bvec);
		}
		int dummy =1;
		System.out.println("\nCheck mirrorHorizontally\n  * Original *");
		prettyPrintBoardVector(bv2.bvec);
		System.out.println("  * Mirrored *");
		prettyPrintBoardVector(xnf.rotate(bv2).bvec);
	
	
	}
	
	public static void prettyPrintBoardVector(int[] bv) {
		int BS = ConfigOthello.BOARD_SIZE;
		DecimalFormat dform=new DecimalFormat("00");
		for(int i = 0, n=0;  i < BS; i++) {
			for(int j = 0; j < BS; j++,n++) {
				System.out.print(" "+dform.format(bv[n]));
			}	
			System.out.println();
		}	
	}

}
