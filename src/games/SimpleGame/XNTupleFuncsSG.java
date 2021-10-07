package games.SimpleGame;

import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;
import org.apache.commons.math3.exception.OutOfRangeException;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;

public class XNTupleFuncsSG extends XNTupleBase implements XNTupleFuncs, Serializable {

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .gamelog or .agt.zip containing this object will
     * become unreadable or you have to provide a special version transformation)
     */
    @Serial
	private static final long serialVersionUID = 12L;
    
    public XNTupleFuncsSG() {  }
    
	/**
	 * @return the number of board cells
	 */
	@Override
	public int getNumCells() {
		return 2;
	}
	
	/**
	 * @return the maximum number P of position values 0, 1, 2,..., P-1
	 * 		0: (m_sum &gt; UPPER), 1,2,...,UPPER: m_sum
	 */
	@Override
	public int getNumPositionValues() {
		return 1+StateObserverSG.UPPER;
	}

	/**
	 * @return the number P of position values 0, 1, 2,..., P-1 for each cell
	 * 		cell 0: UPPER+1
	 * 	    cell 1: 2 (open: 0, final: 1)
	 */
	@Override
	public int[] getPositionValuesVector() {
		return new int[]{
				1+StateObserverSG.UPPER, 2
		};
	}

	/**
	 * @return the number of players in this game 
	 */
	@Override
	public int getNumPlayers() {
		return 1;
	}
	
	/**
	 * @return the maximum number of symmetries in this game (including identity)
	 */
	public int getNumSymmetries() {
		return 1;
	}
	
	/**
	 * The board vector is an {@code int[]} vector
	 * @return a vector of length {@link #getNumCells()}, holding for each board cell its 
	 * 		for cell 0 the position value 0: (m_sum &gt; UPPER), 1,2,...,UPPER: m_sum.
	 * 		for cell 1 the position value 0: open, 1: final
	 */
	@Override
	public BoardVector getBoardVector(StateObservation so) {
		assert (so instanceof StateObserverSG) : "Oops, not StateObserverSG";
		StateObserverSG sg = (StateObserverSG) so;
		int[] bvec = new int[2];
		if (sg.get_sum()<=StateObserverSG.UPPER) bvec[0] = sg.get_sum();
		if (sg.isGameOver()) bvec[1] = 1;	// it's a final (game-over) state

		return new BoardVector(bvec);   
	}
	
	@Override
	public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
		BoardVector[] equiv = new BoardVector[1];
		equiv[0] = boardVector;
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
	 * equivAction[i] is the key of the action in the i'th equivalent board vector equiv[i], 
	 * which is equivalent to actionKey in equiv[0]. <br>
	 */
	public int[] symmetryActions(int actionKey) {
		int[] equivAction = new int[1];
		equivAction[0] = actionKey;
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
		case 1:
			return new int[][] {
				{0,1}
			};
		//all straight 3-tuples
		default: throw new OutOfRangeException(mode, 1, 1);
		}
	}

	@Override
	public String fixedTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>"
				+ "1: the one-and-only n-tuple"
				+ "</html>";
	}

    private final static int[] fixedModes = {1};
    
	public int[] fixedNTupleModesAvailable() {
		return fixedModes;
	}


	/**
	 * Return all neighbors of {@code iCell}. See {@link #getBoardVector(StateObservation)} 
	 * for board coding.
	 * 
	 * @param iCell cell 0 or 1
	 * @return a set of all cells adjacent to {@code iCell} (referring to the coding in 
	 * 		a board vector) 
	 */
	public HashSet<Integer> adjacencySet(int iCell) {
		HashSet<Integer> adjSet = new HashSet<>();
		adjSet.add(1-iCell);
		return adjSet;
	}
}
