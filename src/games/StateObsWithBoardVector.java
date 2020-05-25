package games;

/**
 * 	This container class just bundles a {@link BoardVector} with its creating {@link StateObservation} object.
 * 
 *  @see XNTupleFuncs#symmetryVectors(StateObsWithBoardVector, int)
 */
public class StateObsWithBoardVector {
	private StateObservation so;
	private BoardVector boardV;
	
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
	
	public boolean hasBoardVector() {
		return (boardV.bvec!=null);
	}

}
