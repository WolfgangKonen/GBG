package games.TicTacToe;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;

import org.apache.commons.math3.exception.OutOfRangeException;

import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

public class XNTupleFuncsTTT extends XNTupleBase implements XNTupleFuncs, Serializable {

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .gamelog or .agt.zip containing this object will
     * become unreadable or you have to provide a special version transformation)
     * <p>
     * [We need this strange number here, because serialVersionUID was not present before, 
     * and the so far stored agents had this automatically created serialVersionUID.]
     */
	@Serial
    private static final long serialVersionUID = 7556763505414386566L;
    
    private final int[][] actionArray;

    public XNTupleFuncsTTT() {
    	// calculate actionArray[][]: for a given action with key j, the element
    	// actionArray[i][j] holds the equivalent action when the state is transformed to 
    	// equiv[i] = symmetryVectors(int[] boardVector)[i]
		int[] actionVector = {0,1,2,3,4,5,6,7,8};
		BoardVector[] newplace = symmetryVectors(new BoardVector(actionVector),0);
    	actionArray = new int[8][];
    	for (int i=0; i<actionArray.length; i++) {
    		actionArray[i] = new int[9];
    		for (int j=0; j<9; j++)
    			actionArray[i][j] = whereHas(newplace[i].bvec,j);
    	}

    }
    
    // helper function for XNTupleFuncsTTT(): "Where has array arr the content j?"
    private int whereHas(int[] arr, int j) {
    	for (int i=0; i<arr.length; i++) 
    		if (arr[i]==j) return i;
    	throw new RuntimeException("whereHas: arr does not contain j!!");
    }
    
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
	 * @return the number of players in this game 
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
	 * cell of the board. In the case of TicTacToe the mapping is
	 * <pre>
	 *    0 1 2
	 *    3 4 5
	 *    6 7 8
	 * </pre>
	 * @param so the stateObservation of the current game state
	 * @return a vector of length {@link #getNumCells()}, holding for each board cell its
	 * position value with 0:"O", 1=empty, 2="X".
	 */
	@Override
	public BoardVector getBoardVector(StateObservation so) {
		assert (so instanceof StateObserverTTT) : "Oops, not StateObserverTTT";
		int[][] table = ((StateObserverTTT) so).getTable();
		int[] bvec = new int[getNumCells()]; 
		for (int i=0, n=0;i<3;i++)
			for (int j=0;j<3;j++, n++) 
            	bvec[n]=table[i][j]+1;                					

		return new BoardVector(bvec);   
	}
	
	/**
	 * Given a board vector from {@link #getBoardVector(StateObservation)} and given that the 
	 * game has s symmetries, return an array which holds at most s symmetric board vectors: <ul>
	 * <li> the first element {@code vecOfBvecs[0]} is the board vector itself
	 * <li> the other elements are the board vectors when transforming {@code boardVector}
	 * 		according to the s-1 other symmetries (e. g. rotation, reflection, if applicable).
	 * </ul>
	 * In the case of TicTacToe we have s=8 symmetries (4 board rotations * 2 board flips)
	 * 
	 * @param boardVector a certain board in vector representation
	 * @param n number of symmetry vectors to return (n=0 meaning 'all')
	 * @return vecOfBvecs
	 */
	@Override
	public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
		int i;
		if (n==0) n=8;
		BoardVector[] equiv = new BoardVector[n];
		equiv[0] = boardVector;
		for (i = 1; i < 4; i++) {
			if (i >= n)
				break;
			equiv[i] = rotate(equiv[i - 1]);
		}
		equiv[i] = flip(boardVector);
		for (i=5; i < 8; i++) {
			if (i >= n)
				break;
			equiv[i] = rotate(equiv[i - 1]);
		}
		return equiv;
	}
	
	/**
	 * Given a certain board array of symmetric (equivalent) states for state <b>{@code so}</b> 
	 * and a certain action to be taken in <b>{@code so}</b>, generate the array of equivalent 
	 * action keys {@code equivAction} for the symmetric states.
	 * <p>
	 * This method is needed for Q-learning and Sarsa only.
	 * 
	 * @param actionKey
	 * 				the key of the action to be taken in <b>{@code so}</b> 
	 * @return <b>equivAction</b>
	 * 				array of the equivalent actions' keys. 
	 * <p>
	 * If actionKey is the key of a certain action in board equiv[0], then equivAction[i] is the key of the equivalent
	 * action in the i'th equivalent board vector equiv[i]. <br>
	 * Here, equiv[i] = {@link #symmetryVectors(BoardVector, int)}{@code [i]}.
	 * <p>
	 * Example: If the symmetry transformation 1 is a 90° clockwise rotation which maps fields 0,1,2 to 2,5,8 and the
	 * action is 1 on board equiv[0], then the corresponding equivAction[1] is 5 (because placing a stone on field 1
	 * on the original board corresponds to field 5 on the rotated board).
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
	 * 
	 * @param mode one of the values from {@link #fixedNTupleModesAvailable()}
	 * @return nTuples[numTuples][]
	 */
	@Override
	public int[][] fixedNTuples(int mode) {
		switch(mode)
		{
		//best chosen 40, 4-tuples
		case 1:	
			return new int[][] {
				{6, 7, 4, 0},{4, 5, 8, 7},{4, 3, 0, 1},{4, 5, 2, 1},{6, 3, 0, 1},
				{6, 3, 0, 4},{0, 1, 5, 2},{2, 1, 4, 7},{7, 3, 4, 5},{0, 4, 1, 2},{8, 4, 0, 1},{7, 4, 1, 0},
				{7, 4, 0, 1},{8, 7, 4, 1},{7, 4, 8, 5},{8, 7, 4, 1},{7, 8, 5, 4},{4, 8, 7, 6},{2, 1, 5, 4},
				{3, 0, 1, 4},{8, 7, 3, 4},{8, 4, 3, 0},{4, 1, 2, 5},{6, 3, 0, 4},{1, 2, 5, 8},{1, 4, 3, 7},
				{6, 3, 0, 1},{8, 5, 4, 3},{3, 4, 7, 6},{5, 8, 7, 6},{5, 4, 0, 1},{6, 3, 4, 7},{0, 3, 4, 8},
				{6, 3, 7, 8},{2, 1, 4, 0},{3, 7, 4, 1},{1, 2, 5, 4},{8, 5, 1, 4},{6, 7, 8, 4},{6, 3, 0, 1}
			};
		//all straight 3-tuples
		case 2:	
			return new int[][] {
				{ 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, 	// the horizontals
				{ 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, 	// the verticals
				{ 0, 4, 8 }, { 2, 4, 6 }				// the diagonals
			};
		default: throw new OutOfRangeException(mode, 1, 2);
		}
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
	}

	@Override
	public String fixedTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>"
				+ "1: 40 best chosen 4-tuples"+"<br>" 
				+ "2: all the straight 3-tuples" 
				+ "</html>";
	}

    private static final int[] fixedModes = {1,2};
    
	public int[] fixedNTupleModesAvailable() {
		return fixedModes;
	}


	/**
	 * Return all neighbors of {@code iCell}. See {@link #getBoardVector(StateObservation)} 
	 * for board coding.
	 * 
	 * @param iCell cell index
	 * @return a set of all cells adjacent to {@code iCell} (referring to the coding in 
	 * 		a board vector) 
	 */
	public HashSet<Integer> adjacencySet(int iCell) {
		int[] aList = new int[4];			// 4-point neighborhood
		int count=0;
		
		if (iCell>2) {						// there is an upper neighbor
			aList[count++]=iCell-3;  
		}
		if ((iCell+1)%3 != 0) {				// there is a right neighbor
			aList[count++]=iCell+1;
		}
		if (iCell<6) {						// there is a lower neighbor
			aList[count++]=iCell+3;  
		}
		if (iCell%3 != 0) {					// there is a left neighbor
			aList[count++]=iCell-1;
		}
		
		if (count==0) throw new RuntimeException("No neighbors for cell "+iCell+"!?!?");
		
//		// bList: the real neighbors of iCell (may be 2,3, or 4
//		int[] bList = new int[count];
//		for (int i=0; i<count; i++) bList[i]=aList[i];

		HashSet<Integer> adjSet = new HashSet<>();
		for (int i=0; i<count; i++) adjSet.add(aList[i]);
		
		return adjSet;
	}

	/**
	 * Helper for {@link #symmetryVectors(BoardVector, int)}:
	 * Rotate the given board clockwise by 90 degree. <p>
	 * 
	 * The board is represented as a 9-element int vector:
	 * <pre>
	 *    0 1 2          6 3 0
	 *    3 4 5   --->   7 4 1   
	 *    6 7 8          8 5 2
	 * </pre>
	 *
	 * @param boardVector a certain board
	 * @return the rotated board
	 */
	private BoardVector rotate(BoardVector boardVector) {
		int[] board = boardVector.bvec;
		//rotate TicTacToe board
		int[] newBoard = new int[9];
//		for (int i = 2, k = 0; i >= 0; i--)
//			for (int j = 0; j < 9; j += 3)
//				newBoard[k++] = board[i + j];
		int[] ri = {6,3,0,7,4,1,8,5,2};
		for (int k=0; k<9; k++) newBoard[k] =board[ri[k]];
		return new BoardVector(newBoard);
	}

	/**
	 * Helper for {@link #symmetryVectors(BoardVector, int)}:
	 * Flip the board around the center row. <p>
	 * 
	 * The board is represented as a 9-element int vector:
	 * <pre>
	 *    0 1 2          6 7 8
	 *    3 4 5   --->   3 4 5   
	 *    6 7 8          0 1 2
	 * </pre>
	 *
	 * @param boardVector a certain board
	 * @return the mirrored board
	 */
	private BoardVector flip(BoardVector boardVector) {
		int[] board = boardVector.bvec;
		//mirror TicTacToe board (horizontal flip)
		int[] newBoard = new int[9];
//		for (int i = 2, k = 0; i >= 0; i--)
//			for (int j = 0; j < 3; j++)
//				newBoard[k++] = board[i * 3 + j];
		int[] ri = {6,7,8,3,4,5,0,1,2};
		for (int k=0; k<9; k++) newBoard[k] =board[ri[k]];
		return new BoardVector(newBoard);
	}

}
