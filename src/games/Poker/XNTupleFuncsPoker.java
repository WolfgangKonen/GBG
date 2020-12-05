package games.Poker;

import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

import java.io.Serializable;
import java.util.HashSet;

public class XNTupleFuncsPoker extends XNTupleBase implements XNTupleFuncs, Serializable {

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .gamelog or .agt.zip containing this object will
     * become unreadable or you have to provide a special version transformation)
     * <p>
     * [We need this strange number here, because serialVersionUID was not present before, 
     * and the so far stored agents had this automatically created serialVersionUID.]
     */
    private static final long serialVersionUID = 7556763505414386566L;

	public XNTupleFuncsPoker() {

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
	 * @return a vector of length {@link #getNumCells()}, holding for each board cell its 
	 * position value with 0:"O", 1=empty, 2="X".
	 */
	@Override
	public BoardVector getBoardVector(StateObservation so) {
		return null;
	}
	
	/**
		TODO
	 */
	@Override
	public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
		return null;
	}
	
	/**
	 	TODO
	 */
	@Override
	public int[] symmetryActions(int actionKey) {
		return null;
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
		return null;
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


	@SuppressWarnings("rawtypes")
	public HashSet adjacencySet(int iCell) {
		return null;
	}



}
