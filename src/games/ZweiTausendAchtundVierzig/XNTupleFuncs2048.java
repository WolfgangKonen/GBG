package games.ZweiTausendAchtundVierzig;

import java.io.Serializable;
import java.util.HashSet;

import games.StateObservation;
import games.XNTupleFuncs;

public class XNTupleFuncs2048 implements XNTupleFuncs, Serializable {

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
	public int[] getBoardVector(StateObservation so) {
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
	public int[][] symmetryVectors(int[] boardVector) {
		int[][] symmetries = new int[8][];
		int[] mirroredBoardVector = mirrorBoardVector(boardVector);

		for(int i = 0; i < 8; i+=2) {
			symmetries[i] = boardVector;
			symmetries[i+1] = mirroredBoardVector;

			mirroredBoardVector = rotateBoardVector(mirroredBoardVector);
			boardVector = rotateBoardVector(boardVector);
		}
		return symmetries;
	}

	/** 
	 * Return a fixed set of {@code numTuples} n-tuples suitable for that game. 
	 * Different n-tuples may have different length. An n-tuple {0,1,4} means a 3-tuple 
	 * containing the cells 0, 1, and 4.
	 * 
	 * @return nTuples[numTuples][]
	 */
	@Override
	public int[][] fixedNTuples() {
		switch (ConfigGame.FIXEDNTUPLEMODE) {
		case 1: 
			// -- new setting by WK, along the lines of [Jaskowski16], Fig 3b
			// -- Medium resulting score ~30.000, but small in memory (5*50e3 weights, 2 MB)
			return new int[][]{
				{0,4,1,5},{1,5,2,6},{5,9,6,10},
				{2,6,10,14},{3,7,11,15}};
		case 2: 
			// -- former setting by JK, along the lines of [Jaskowski16], Fig 3c.
			// -- Very good resulting score ~50.000, but also very big LUTs (4*11e6 weights, 44 MB agt.zip!!)
			return new int[][]{
					{0,4,8,1,5,9},{1,5,9,2,6,10},
					{2,6,9,10,13,14},{3,7,10,11,14,15}};
		}
		throw new RuntimeException("Unsupported value for ConfigGame.FIXEDNTUPLEMODE");
	}
	
	/**
	 * Return all neighbors of {@code iCell}. See {@link #getBoardVector(StateObservation)} 
	 * for board coding.
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

	private int[] rotateBoardVector(int[] array) {
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
		return rotatedArray;
	}

	private int[] mirrorBoardVector(int[] array) {
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
		return mirroredArray;
	}
}
