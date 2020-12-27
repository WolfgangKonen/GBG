package games;

import controllers.TD.ntuple2.NTuple2ValueFunc;

/**
 * 	This container class just bundles a {@link BoardVector} with its creating {@link StateObservation} object.
 * 	<p>
 * 	Note: It copies the bundled elements just by reference.
 * 
 *  @see XNTupleFuncs#symmetryVectors(StateObsWithBoardVector, int)
 *  @see NTuple2ValueFunc
 */
public class StateObsWithBoardVector {
	private final StateObservation so;
	private final BoardVector boardV;
	
	public StateObsWithBoardVector(StateObservation so, BoardVector boardV) {
		this.so = so;
		this.boardV = boardV;
	}
	public StateObsWithBoardVector(StateObservation so, XNTupleFuncs xnf) {
		this.so = so;
		this.boardV = xnf.getBoardVector(so);
	}
	
	public StateObservation getStateObservation() {
		return so;
	}
	public BoardVector getBoardVector() {
		return boardV;
	}

	// --- never used ---
//	public boolean hasBoardVector() {
//		return (boardV.bvec!=null);
//	}

}
