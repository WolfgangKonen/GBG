package games.ZweiTausendAchtundVierzig;

import java.io.Serializable;
import java.util.HashSet;

import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

public class XNTupleFuncs2048 extends XNTupleBase implements XNTupleFuncs, Serializable {

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .gamelog or .agt.zip containing this object will
     * become unreadable or you have to provide a special version transformation)
     * <p>
     * [We need this strange number here, because serialVersionUID was not present before, 
     * and the so far stored agents had this automatically created serialVersionUID.]
     */
    private static final long serialVersionUID = -4486168568704181430L;

    private int[] actionVector = {0,1,2,3};
    private int[][] actionArray;

	/**
	 * Constructor, generates actionArray as needed for {@link #symmetryActions(int)}
	 * <p>
	 * See {@link StateObserver2048#updateAvailableMoves()} for the numbering of actions:<br>
	 * 0: left, 1: up, 2: right, 3: down.
	 * 
	 */
    public XNTupleFuncs2048() {
    	// calculate actionArray[][]: for a given action with key j, the element
    	// actionArray[i][j] holds the equivalent action when the state is transformed to 
    	// equiv[i] = symmetryVectors(int[] boardVector)[i]
    	actionArray = new int[8][];
    	int[] rotatedActionVector = actionVector.clone();
		int[] mirroredActionVector = mirrorActionVector(actionVector);

		for(int i = 0; i < 8; i+=2) {
			actionArray[i] = rotatedActionVector;
			actionArray[i+1] = mirroredActionVector;

			mirroredActionVector = rotateActionVector(mirroredActionVector);
			rotatedActionVector = rotateActionVector(rotatedActionVector);
		}
    }
    
//    // helper function for XNTupleFuncsTTT(): "Where has array arr the content j?"
//    private int whereHas(int[] arr, int j) {
//    	for (int i=0; i<arr.length; i++) 
//    		if (arr[i]==j) return i;
//    	throw new RuntimeException("whereHas: arr does not contain j!!");
//    }
    
	//
	// The following five functions are only needed for the n-tuple interface:
	//
	/**
	 * @return the number of board cells
	 */
	@Override
	public int getNumCells() {
		return 16;
	}
	
	/**
	 * @return the number P of position values 0, 1, 2,..., P-1 that each board cell 
	 * can have. <br>
	 * 0: empty tile, 1: tile 2^1, ..., 14: tile 2^14, assuming that 2^14 = 16384  
	 * is the highest tile we expect to encounter. Theoretically, 2^17 is the highest tile. 
	 */
	@Override
	public int getNumPositionValues() {
		return 15; 
	}
	
	/**
	 * @return the number of players in this game 
	 */
	@Override
	public int getNumPlayers() {
		return 1;
	}
	
	/**
	 * @return the maximum number of symmetries in this game
	 */
	public int getNumSymmetries() {
		return 8;
	}
	
	/**
	 * The board vector is an {@code int[]} vector where each entry corresponds to one 
	 * cell of the board. In the case of 2048 the mapping is
	 * <pre>
	 *    00 01 02 03
	 *    04 05 06 07
	 *    08 09 10 11
	 *    12 13 14 15
	 * </pre>
	 * @return a vector of length {@link #getNumCells()}, holding for each board cell its 
	 * position value 0:empty, 1: tile 2^1, 2: tile 2^2,..., P-1: tile 2^(P-1).
	 */
	@Override
	public BoardVector getBoardVector(StateObservation so) {
		if (so instanceof StateObserver2048) {
			return ((StateObserver2048) so).getBoardVector();
		} else {
			throw new RuntimeException("StateObservation 'so' is not an instance of StateObserver2048");
		}
	}
	
	/**
	 * Given a board vector from {@link #getBoardVector(StateObservation)} and given that the 
	 * game has s symmetries, return an array which holds s symmetric board vectors: <ul>
	 * <li> the first row {@code boardArray[0]} is the board vector itself
	 * <li> the other rows are the board vectors when transforming {@code boardVector}
	 * 		according to the s-1 other symmetries (e. g. rotation, reflection, if applicable).
	 * </ul>
	 * In the case of 2048 we have s=8 symmetries (4 board rotations * 2 board flips)
	 * @param boardVector
	 * @return boardArray
	 */
	@Override
	public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
		BoardVector[] symmetries = new BoardVector[8];
		BoardVector mirroredBoardVector = mirrorBoardVector(boardVector.bvec);

		for(int i = 0; i < 8; i+=2) {
			symmetries[i] = boardVector;
			symmetries[i+1] = mirroredBoardVector;

			if (i==6) break; // a little speedup
			
			mirroredBoardVector = rotateBoardVector(mirroredBoardVector.bvec);
			boardVector = rotateBoardVector(boardVector.bvec);
		}
		return symmetries;
	}

	/**
	 * Given a certain board array of symmetric (equivalent) states for state <b>{@code so}</b> 
	 * and a certain action to be taken in <b>{@code so}</b>, 
	 * generate the array of equivalent action keys {@code equivAction} for the symmetric states.
	 * <p>
	 * This method is needed only for Q-learning and Sarsa.
	 * 
	 * @param actionKey
	 * 				the key of the action to be taken in <b>{@code so}</b> 
	 * @return <b>equivAction</b>
	 * 				array of the equivalent actions' keys. 
	 * <p>
	 * equivAction[i] is the key of the action equivalent to actionKey in the
	 * i'th equivalent board vector equiv[i] = {@link #symmetryVectors(int[])}[i]
	 */
	public int[] symmetryActions(int actionKey) {
		int numEquiv = actionArray.length;
		int[] equivAction = new int[numEquiv];
		for (int i = 0; i < numEquiv; i++) {
			equivAction[i] = actionArray[i][actionKey];
		}

		return equivAction;
	}
	
	/** 
	 * Return a fixed set of {@code numTuples} n-tuples suitable for that game. 
	 * Different n-tuples may have different length. An n-tuple {0,1,4} means a 3-tuple 
	 * containing the cells 0, 1, and 4.
	 * <ul>
	 * <li> <b>mode=1</b>: 5 4-tuples as in [Jaskowski16, Fig. 3b]: two straight lines, 3 squares.
	 * <li> <b>mode=2</b>: 4 6-tuples, similar to [Jaskowski16, Fig. 3c]: two 32-rectangles and
	 * 		only <b>two</b> d-shaped 42-tuples (note that [Jaskowski16] has 3 d-shaped tuples). 
	 * </ul>
	 * <p>
	 * See {@link #getBoardVector(StateObservation)} for the numbering of cells in boardVector.
	 * 
	 * @param mode one of the values from {@link #fixedNTupleModesAvailable()}
	 * @return nTuples[numTuples][]
	 */
	@Override
	public int[][] fixedNTuples(int mode) {
		// TODO: (when mode is stored in the saved agents)
		switch (mode) {
		//switch (ConfigGame.FIXEDNTUPLEMODE) {
		case 1: 
			// -- new setting by WK, along the lines of [Jaskowski16], Fig 3b
			// -- Medium resulting score ~30.000, but small in memory (5*50e3 weights, ~1 MB agt.zip)
			return new int[][]{
				{0,4,1,5},{1,5,2,6},{5,9,6,10},
				{2,6,10,14},{3,7,11,15}};
		case 2: 
			// -- former setting by JK, along the lines of [Jaskowski16], Fig 3c.
			// -- Very good resulting score ~80.000, but also very big LUTs (4*11e6 weights, 200k, 93 MB agt.zip!!)
			return new int[][]{
					{0,4,8,1,5,9},{1,5,9,2,6,10},
					{2,6,9,10,13,14},{3,7,10,11,14,15}};
		}
		throw new RuntimeException("Unsupported value mode="+mode+" in XNTupleFuncs::fixedNTuples(int)");
	}
	
	@Override
	public String fixedTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>"
				+ "1: TODO"
				+ "</html>";
	}

    private static int[] fixedModes = {1,2};
    
    /**
     * This function returns {1,2} as the available fixed-n-tuple modes, with meaning: 
     * <ul>
     * <li> =1: along the lines of [Jaskowski16] Fig 3b, 5 4-tuples, smaller LUTs (5*50e3, 2 MB agt.zip file), medium results <br>
     * <li> =2: along the lines of [Jaskowski16] Fig 3c, 4 6-tuples, very big LUTs (4*11e6 weights, 69 MB or 93 MB agt.zip!!), 
     *     very good results
     * </ul>
     * @see XNTupleFuncs2048#fixedNTuples(int)
     */
    public int[] fixedNTupleModesAvailable() {
		return fixedModes;
	}

	/**
	 * Return all neighbors of {@code iCell}. 
	 * <p>
	 * See {@link #getBoardVector(StateObservation)} for the numbering of cells in boardVector.
	 * 
	 * @param iCell
	 * @return a set of all cells adjacent to {@code iCell} (referring to the coding in 
	 * 		a board vector) 
	 */
	public HashSet adjacencySet(int iCell) {
		int[] aList = new int[4];			// 4-point neighborhood
		int count=0;
		
		if (iCell>3) {						// there is an upper neighbor
			aList[count++]=iCell-4;  
		}
		if ((iCell+1)%4 != 0) {				// there is a right neighbor
			aList[count++]=iCell+1;
		}
		if (iCell<12) {						// there is a lower neighbor
			aList[count++]=iCell+4;  
		}
		if (iCell%4 != 0) {					// there is a left neighbor
			aList[count++]=iCell-1;
		}
		
		if (count==0) throw new RuntimeException("No neighbors for cell "+iCell+"!?!?");
		
//		// bList: the real neighbors of iCell (may be 2,3, or 4
//		int[] bList = new int[count];
//		for (int i=0; i<count; i++) bList[i]=aList[i];
		
		HashSet adjSet = new HashSet();
		for (int i=0; i<count; i++) adjSet.add(aList[i]);
		
		
		return adjSet;
	}

	/**
	 * Rotate 90 degree clockwise.
	 * <p>
	 * See {@link #getBoardVector(StateObservation)} for the numbering of cells in boardVector.
	 * 
	 * @param array
	 * @return
	 */
	private BoardVector rotateBoardVector(int[] array) {
		int[] rotatedArray = new int[16];
		rotatedArray[0] = array[12];
		rotatedArray[1] = array[8];
		rotatedArray[2] = array[4];
		rotatedArray[3] = array[0];
		rotatedArray[4] = array[13];
		rotatedArray[5] = array[9];
		rotatedArray[6] = array[5];
		rotatedArray[7] = array[1];
		rotatedArray[8] = array[14];
		rotatedArray[9] = array[10];
		rotatedArray[10] = array[6];
		rotatedArray[11] = array[2];
		rotatedArray[12] = array[15];
		rotatedArray[13] = array[11];
		rotatedArray[14] = array[7];
		rotatedArray[15] = array[3];
		return new BoardVector(rotatedArray);
	}

	/**
	 * Make a mirror copy along the horizontal mid line.
	 * <p>
	 * See {@link #getBoardVector(StateObservation)} for the numbering of cells in boardVector.
	 * 
	 * @param array
	 * @return
	 */
	private BoardVector mirrorBoardVector(int[] array) {
		int[] mirroredArray = new int[16];
		mirroredArray[0] = array[12];
		mirroredArray[1] = array[13];
		mirroredArray[2] = array[14];
		mirroredArray[3] = array[15];
		mirroredArray[4] = array[8];
		mirroredArray[5] = array[9];
		mirroredArray[6] = array[10];
		mirroredArray[7] = array[11];
		mirroredArray[8] = array[4];
		mirroredArray[9] = array[5];
		mirroredArray[10] = array[6];
		mirroredArray[11] = array[7];
		mirroredArray[12] = array[0];
		mirroredArray[13] = array[1];
		mirroredArray[14] = array[2];
		mirroredArray[15] = array[3];
		return new BoardVector(mirroredArray);
	}

	/**
	 * Rotate an action vector 90 degree clockwise.
	 * <p>
	 * See {@link StateObserver2048#updateAvailableMoves()} for the numbering of actions:<br>
	 * 0: left, 1: up, 2: right, 3: down.
	 * 
	 * @param array
	 * @return
	 */
	private int[] rotateActionVector(int[] array) {
		int[] rotatedArray = new int[4];
		rotatedArray[0] = array[1];
		rotatedArray[1] = array[2];
		rotatedArray[2] = array[3];
		rotatedArray[3] = array[0];
		return rotatedArray;
	}

	/**
	 * Mirror an action vector upside-down.
	 * <p>
	 * See {@link StateObserver2048#updateAvailableMoves()} for the numbering of actions:<br>
	 * 0: left, 1: up, 2: right, 3: down.
	 * 
	 * @param array
	 * @return
	 */
	private int[] mirrorActionVector(int[] array) {
		int[] mirroredArray = new int[4];
		mirroredArray[0] = array[0];
		mirroredArray[1] = array[3];
		mirroredArray[2] = array[2];
		mirroredArray[3] = array[1];
		return mirroredArray;
	}

}

