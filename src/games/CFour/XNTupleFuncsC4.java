package games.CFour;

import java.io.Serializable;
import java.util.HashSet;

import controllers.TD.ntuple2.NTupleFactory;
import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

public class XNTupleFuncsC4 extends XNTupleBase implements XNTupleFuncs, Serializable {

    /**
     * Provide a version UID here. Change the version ID for serialization only if a newer version 
     * is no longer compatible with an older one (older .gamelog or .agt.zip containing this object
     * will become unreadable or you have to provide a special version transformation)
     */
    private static final long serialVersionUID = 12L;

	/**
	 * @return the number of board cells
	 */
	@Override
	public int getNumCells() {
		return C4Base.COLCOUNT*C4Base.ROWCOUNT;
	}
	
	/**
	 * @return the number P of position values 0, 1, 2,..., P-1 that each board cell 
	 * can have. For ConnectFour: 
	 * <ul>
	 * <li> either P=3, with 0=empty, 1="X", 2="O"
	 * <li> or     P=4, with 0=non-reachable-empty, 1="X", 2="O", 3=reachable-empty 
	 * 		(<b>recommended</b> case)
	 * </ul> 
	 * 
	 * @see #getBoardVector(StateObservation)
	 */
	@Override
	public int getNumPositionValues() {
		// return 3; // (*not* recommended)
		return 4; 
	}
	
	/**
	 * @return the number of players in this game 
	 */
	@Override
	public int getNumPlayers() {
		return 2;
	}
	
	/**
	 * The board vector is an {@code int[]} vector where each entry corresponds to one 
	 * cell of the board. In the case of ConnectFour with 7 columns and 6 rows the mapping is
	 * <pre>
	 *      05  11  17  23  29  35  41
	 *      04  10  16  22  28  34  40
	 *      03  09  15  21  27  33  39
	 *      02  08  14  20  26  32  38
	 *      01  07  13  19  25  31  37
	 *      00  06  12  18  24  30  36
	 * </pre>
	 * @return a vector of length {@link #getNumCells()}, holding for each board cell its 
	 * position value which is with 
	 * <ul> 
	 * <li> 0=empty, 1="X", 2="O" in case {@link #getNumPositionValues()}==3 and
	 * <li> 0=not-reachable-empty, 1="X", 2="O", 3=reachable-empty in case 
	 *      {@link #getNumPositionValues()}==4 (<b>recommended</b> case).
	 * </ul>
	 * An reachable-empty cell is an empty cell which is either in the lowest row or has a
	 * piece directly below. 
	 */
	@Override
	public BoardVector getBoardVector(StateObservation so) {
		assert (so instanceof StateObserverC4);
		int[][] board = ((StateObserverC4) so).getBoard();
		int[] bvec = new int[getNumCells()]; 
		for (int i = 0, n=0; i < C4Base.COLCOUNT; i++) {
			for (int j = 0; j < C4Base.ROWCOUNT; j++, n++) {
            	bvec[n]=board[i][j];  
			}
		}
		if (this.getNumPositionValues()==4) {
			for (int i = 0, n=0; i < C4Base.COLCOUNT; i++) {
				for (int j = 0; j < C4Base.ROWCOUNT; j++, n++) {
	            	if (board[i][j]==0) {
	            		if (j==0 ) bvec[n]=3;
	            		else if (board[i][j-1]==1 || board[i][j-1]==2) bvec[n]=3; 
	            		//
	            		// this was a previous bug: we had board[i][j] where it should be bvec[n] !!!!
//	            		if (j==0 ) board[i][j]=3;
//	            		else if (board[i][j-1]==1 || board[i][j-1]==2) board[i][j]=3; 
	            	}
				}
			}
			
		}

		return new BoardVector(bvec);   
	}
	
	/**
	 * Given a board vector from {@link #getBoardVector(StateObservation)} and given that the 
	 * game has s symmetries, return an array which holds s symmetric board vectors: <ul>
	 * <li> the first row {@code boardArray[0]} is the board vector itself
	 * <li> the other rows are the board vectors when transforming {@code boardVector}
	 * 		according to the s-1 other symmetries (e. g. rotation, reflection, if applicable).
	 * </ul>
	 * In the case of ConnectFour we have s=2 symmetries (the board itself + 1 vertical mirror flip)
	 * 
	 * @param boardVector
	 * @return boardArray
	 */
	@Override
	public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
		BoardVector[] equiv = new BoardVector[2];
		equiv[0] = new BoardVector(boardVector.bvec);
		equiv[1] = flip(boardVector);

		return equiv;
	}
	
	/**
	 * Given a certain board array of symmetric (equivalent) states for state <b>{@code so}</b> 
	 * and a certain action to be taken in <b>{@code so}</b>, 
	 * generate the array of equivalent action keys {@code equivAction} for the symmetric states.
	 * <p>
	 * This method is needed for Q-learning and Sarsa only.
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
		int[] equivAction = new int[2];
		equivAction[0] = actionKey;
		equivAction[1] = C4Base.COLCOUNT-1 - actionKey;
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
		// Standard Tuple-Set for C4 (COLCOUNT:7, ROWCOUNT:6)
		// Use this Board representation:
		// 5 11 17 23 29 35 41
		// 4 10 16 22 28 34 40
		// 3 9 15 21 27 33 39
		// 2 8 14 20 26 32 38
		// 1 7 13 19 25 31 37
		// 0 6 12 18 24 30 36
		int nTuple[][] =  { //
				{ 0, 6, 7, 12, 13, 14, 19, 21 }, //
				{ 13, 18, 19, 20, 21, 26, 27, 33 }, //
				{ 1, 3, 4, 6, 7, 8, 9, 10 }, //
				{ 7, 8, 9, 12, 15, 19, 25, 30 }, //
				{ 4, 5, 9, 10, 11, 15, 16, 17 }, //
				{ 1, 2, 3, 8, 9, 10, 16, 17 }, //
				{ 3, 8, 9, 10, 11, 14, 15, 16 }, //
				{ 0, 1, 2, 6, 8, 12, 13, 18 }, //
				{ 25, 26, 27, 32, 33, 37, 38, 39 }, //
				{ 3, 4, 8, 9, 11, 14, 15, 21 }, //
				{ 2, 3, 4, 8, 9, 14, 15, 20 }, //
				{ 18, 19, 24, 30, 31, 32, 36, 37 }, //
				{ 3, 4, 8, 9, 10, 14, 15, 16 }, //
				{ 5, 10, 11, 16, 17, 21, 22, 27 }, //
				{ 4, 10, 15, 20, 21, 22, 27, 28 }, //
				{ 18, 24, 25, 30, 31, 32, 37, 38 }, //
				{ 11, 17, 21, 23, 27, 28, 33, 39 }, //
				{ 21, 25, 26, 27, 32, 34, 35, 41 }, //
				{ 22, 25, 26, 27, 30, 32, 33, 37 }, //
				{ 4, 10, 11, 16, 20, 21, 22, 23 }, //
				{ 0, 6, 7, 8, 12, 13, 14, 15 }, //
				{ 17, 23, 28, 29, 32, 33, 34, 35 }, //
				{ 0, 6, 7, 12, 18, 25, 32, 38 }, //
				{ 2, 3, 4, 5, 8, 9, 10, 11 }, //
				{ 27, 32, 33, 34, 37, 38, 39, 40 }, //
				{ 4, 10, 16, 21, 26, 32, 33, 38 }, //
				{ 0, 6, 7, 12, 13, 20, 27, 28 }, //
				{ 0, 6, 12, 19, 25, 31, 32, 33 }, //
				{ 1, 2, 6, 7, 13, 14, 15, 20 }, //
				{ 1, 2, 5, 8, 11, 15, 16, 17 }, //
				{ 13, 14, 16, 18, 21, 22, 23, 24 }, //
				{ 2, 3, 9, 10, 11, 16, 17, 22 }, //
				{ 15, 16, 17, 20, 22, 23, 25, 31 }, //
				{ 15, 16, 17, 21, 22, 23, 28, 29 }, //
				{ 24, 26, 30, 31, 32, 33, 36, 37 }, //
				{ 12, 13, 18, 19, 20, 26, 27, 33 }, //
				{ 1, 2, 3, 8, 9, 13, 14, 21 }, //
				{ 13, 14, 18, 20, 24, 25, 31, 37 }, //
				{ 14, 15, 16, 21, 26, 31, 38, 39 }, //
				{ 1, 2, 6, 7, 12, 13, 14, 20 }, //
				{ 4, 5, 10, 11, 17, 22, 23, 29 }, //
				{ 2, 4, 5, 7, 9, 10, 14, 19 }, //
				{ 5, 9, 10, 11, 15, 16, 21, 27 }, //
				{ 1, 2, 3, 7, 8, 13, 14, 20 }, //
				{ 1, 2, 8, 9, 14, 15, 21, 26 }, //
				{ 22, 23, 29, 33, 34, 35, 38, 41 }, //
				{ 13, 18, 19, 24, 25, 26, 31, 32 }, //
				{ 27, 28, 29, 31, 32, 33, 37, 38 }, //
				{ 10, 14, 15, 16, 17, 20, 21, 23 }, //
				{ 4, 5, 9, 10, 15, 20, 21, 22 }, //
				{ 13, 20, 25, 26, 27, 32, 34, 41 }, //
				{ 30, 31, 33, 34, 36, 37, 38, 39 }, //
				{ 11, 16, 23, 28, 34, 35, 40, 41 }, //
				{ 3, 4, 10, 11, 14, 15, 16, 17 }, //
				{ 15, 20, 21, 22, 26, 32, 33, 39 }, //
				{ 18, 19, 25, 26, 31, 32, 34, 39 }, //
				{ 4, 9, 11, 15, 16, 22, 23, 29 }, //
				{ 26, 27, 31, 32, 33, 37, 38, 39 }, //
				{ 20, 27, 28, 33, 34, 35, 40, 41 }, //
				{ 1, 2, 7, 14, 20, 27, 28, 29 }, //
				{ 8, 9, 10, 15, 16, 17, 22, 23 }, //
				{ 9, 14, 15, 20, 21, 22, 27, 32 }, //
				{ 1, 2, 3, 6, 7, 8, 9, 13 }, //
				{ 10, 14, 15, 16, 20, 23, 25, 26 }, //
				{ 0, 1, 2, 6, 7, 8, 13, 14 }, //
				{ 1, 6, 7, 12, 13, 20, 26, 27 }, //
				{ 8, 14, 20, 25, 26, 31, 33, 38 }, //
				{ 20, 21, 26, 27, 28, 33, 35, 40 }, //
				{ 2, 3, 4, 8, 9, 11, 16, 21 }, //
				{ 1, 2, 3, 4, 5, 6, 11, 12 }, //
		}; 
		
		return nTuple;		
	}
	
	@Override
	public String fixedTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>"
				+ "1: 70 specific 8-tuples"
				+ "</html>";
	}


    private static int[] fixedModes = {1};
    
	public int[] fixedNTupleModesAvailable() {
		return fixedModes;
	}


	/**
	 * Return all neighbors of {@code iCell}. See {@link #getBoardVector(StateObservation)} 
	 * for board coding.
	 * 
	 * @param iCell
	 * @return a set of all cells adjacent to {@code iCell} (referring to the coding in 
	 * 		a board vector) 
	 * 
	 */
	public HashSet adjacencySet(int iCell) {
		// used by NTupleFactory#generateRandomWalkNTuples()
		//
		int[] aList = new int[4];			// 4-point neighborhood
		int count=0;
		
		if (iCell>=C4Base.ROWCOUNT) {			// there is a left neighbor
			aList[count++]=iCell-C4Base.ROWCOUNT;  
		}
		if ((iCell+1)%C4Base.ROWCOUNT != 0) {	// there is an upper neighbor
			aList[count++]=iCell+1;
		}
		if (iCell<C4Base.ROWCOUNT*(C4Base.COLCOUNT-1)) {	// there is a right neighbor
			aList[count++]=iCell+C4Base.ROWCOUNT;  
		}
		if (iCell%C4Base.ROWCOUNT != 0) {		// there is a lower neighbor
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
	 * Helper for {@link #symmetryVectors(int[])}: 
	 * Flip the board around the center column. <p>
	 * 
	 * The board is represented as a COLCOUNT*ROWCOUNT-element int vector:
	 * <pre>
	 *      05  11  17  23  29  35  41			41  35  29  23  17  11  05
	 *      04  10  16  22  28  34  40          40  34  28  22  16  10  04
	 *      03  09  15  21  27  33  39   --->   39  33  27  21  15  09  03
	 *      02  08  14  20  26  32  38          38  32  26  20  14  08  02
	 *      01  07  13  19  25  31  37          37  31  25  19  13  07  01
	 *      00  06  12  18  24  30  36          36  30  24  18  12  06  00
	 * </pre>
	 * 
	 * @param board
	 * @return the mirrored board
	 */
	private BoardVector flip(BoardVector boardVector) {
		int[] board = boardVector.bvec;
		//mirror ConnectFour board (vertical flip)
		int[] newBoard = new int[C4Base.ROWCOUNT*C4Base.COLCOUNT];
		for (int i = C4Base.COLCOUNT-1, k=0; i>=0; i--)			// i: col index {0,..,7}, counting down
			for (int j =0; j < C4Base.ROWCOUNT; j++) 			// j: row index {0,..,6}
				newBoard[k++] = board[i*C4Base.ROWCOUNT + j];
		
		return new BoardVector(newBoard);
	}

}
