package games.TicTacToe;

import java.io.Serializable;

import games.StateObservation;
import games.XNTupleFuncs;

public class XNTupleFuncsTTT implements XNTupleFuncs, Serializable {

	//
	// The following five functions are only needed for the n-tuple interface:
	//
	/**
	 * @return the number of board cells
	 */
	@Override
	public int getNumCells() {
		return 9;
	}
	
	/**
	 * @return the number P of position values 0, 1, 2,..., P-1 that each board cell 
	 * can have. For TicTacToe: P=3, with 0:"O", 1=empty, 2="X") 
	 */
	@Override
	public int getNumPositionValues() {
		return 3; 
	}
	
	/**
	 * The board vector is an {@code int[]} vector where each entry corresponds to one 
	 * cell of the board. In the case of TicTacToe the mapping is
	 * <pre>
	 *    0 1 2
	 *    3 4 5
	 *    6 7 8
	 * </pre>
	 * @return a vector of length {@link #getNumCells()}, holding for each board cell its 
	 * position value with 0:"O", 1=empty, 2="X".
	 */
	@Override
	public int[] getBoardVector(StateObservation so) {
		assert (so instanceof StateObserverTTT);
		int[][] table = ((StateObserverTTT) so).getTable();
		int[] bvec = new int[getNumCells()]; 
		for (int i=0, n=0;i<3;i++)
			for (int j=0;j<3;j++, n++) 
            	bvec[n]=table[i][j];                					

		return bvec;   
	}
	
	/**
	 * Given a board vector from {@link #getBoardVector(StateObservation)} and given that the 
	 * game has s symmetries, return an array which holds s symmetric board vectors: <ul>
	 * <li> the first row {@code boardArray[0]} is the board vector itself
	 * <li> the other rows are the board vectors when transforming {@code boardVector}
	 * 		according to the s-1 other symmetries (e. g. rotation, reflection, if applicable).
	 * </ul>
	 * @param boardVector
	 * @return boardArray
	 */
	@Override
	public int[][] symmetryVectors(int[] boardVector) {
		int i;
		int[][] equiv = null;
		equiv = new int[8][];
		equiv[0] = boardVector;
		for (i = 1; i < 4; i++)
			equiv[i] = rotate(equiv[i - 1]);
		equiv[i] = flip(boardVector);
		for (i=5; i < 8; i++)
			equiv[i] = rotate(equiv[i - 1]);

		return equiv;
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
		// Examples for some NTuples for TicTacToe:
		//best chosen 40, 4-tuples
		int nTuple[][]={{6, 7, 4, 0},{4, 5, 8, 7},{4, 3, 0, 1},{4, 5, 2, 1},{6, 3, 0, 1},
				{6, 3, 0, 4},{0, 1, 5, 2},{2, 1, 4, 7},{7, 3, 4, 5},{0, 4, 1, 2},{8, 4, 0, 1},{7, 4, 1, 0},
				{7, 4, 0, 1},{8, 7, 4, 1},{7, 4, 8, 5},{8, 7, 4, 1},{7, 8, 5, 4},{4, 8, 7, 6},{2, 1, 5, 4},
				{3, 0, 1, 4},{8, 7, 3, 4},{8, 4, 3, 0},{4, 1, 2, 5},{6, 3, 0, 4},{1, 2, 5, 8},{1, 4, 3, 7},
				{6, 3, 0, 1},{8, 5, 4, 3},{3, 4, 7, 6},{5, 8, 7, 6},{5, 4, 0, 1},{6, 3, 4, 7},{0, 3, 4, 8},
				{6, 3, 7, 8},{2, 1, 4, 0},{3, 7, 4, 1},{1, 2, 5, 4},{8, 5, 1, 4},{6, 7, 8, 4},{6, 3, 0, 1}
		};
		// int nTuple[][] = {{0,1,2,3,4,5,6,7,8}};
		// int nTuple[][] = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 4, 8 } };
		// int nTuple[][] = {{0,1,2,3,4,5,6,7}, {8,7,6,5,4,3,2,1},
		// 		{8,7,6,5,4,3,2,0}, {0,1,2,3,4,5,6,8}};
		// int nTuple[][] = {{0,1,2,5,8,7}, {0,3,6,7,8,5}, {0,3,6,1,4,7},
		// 		{2,5,8,7,4,1}, {0,1,2,3,4,5}, {6,7,8,3,4,5}, {0,4,8,7,6,3}};
		// int nTuple[][] =
		// 		{{0,1,2,3},{4,5,6,7},{8,0,3,6},{1,4,7,2},{5,8,0,4},{6,4,2,0}};
		// int nTuple[][] = {{0,1,2},{3,4,5},{6,7,8},{0,4,8},{0,1,2,5,8,7},
		//	 	{0,3,6,7,8,5}, {0,3,6,1,4,7}, {2,5,8,7,4,1}, {0,1,2,3,4,5},
		//	 	{6,7,8,3,4,5}, {0,4,8,7,6,3}};
		
		return nTuple;		
	}
	
	/**
	 * Helper for {@link #symmetryVectors(int[])}: 
	 * Rotate the given board clockwise by 90 degree. <p>
	 * 
	 * The board is represented as a 9-element int vector:
	 * <pre>
	 *    0 1 2          6 3 0
	 *    3 4 5   --->   7 4 1   
	 *    6 7 8          8 5 2
	 * </pre>
	 * 
	 * @param board
	 * @return the rotated board
	 */
	private int[] rotate(int[] board) {
		//rotate TicTacToe board
		int[] newBoard = new int[9];
//		for (int i = 2, k = 0; i >= 0; i--)
//			for (int j = 0; j < 9; j += 3)
//				newBoard[k++] = board[i + j];
		int[] ri = {6,3,0,7,4,1,8,5,2};
		for (int k=0; k<9; k++) newBoard[k] =board[ri[k]];
		return newBoard;
	}

	/**
	 * Helper for {@link #symmetryVectors(int[])}: 
	 * Flip the board around the center row. <p>
	 * 
	 * The board is represented as a 9-element int vector:
	 * <pre>
	 *    0 1 2          6 7 8
	 *    3 4 5   --->   3 4 5   
	 *    6 7 8          0 1 2
	 * </pre>
	 * 
	 * @param board
	 * @return the mirrored board
	 */
	private int[] flip(int[] board) {
		//mirror TicTacToe board (horizontal flip)
		int[] newBoard = new int[9];
//		for (int i = 2, k = 0; i >= 0; i--)
//			for (int j = 0; j < 3; j++)
//				newBoard[k++] = board[i * 3 + j];
		int[] ri = {6,7,8,3,4,5,0,1,2};
		for (int k=0; k<9; k++) newBoard[k] =board[ri[k]];
		return newBoard;
	}

}
