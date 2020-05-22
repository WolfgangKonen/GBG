package games;

public class StateObsWithBoardVector {
	private StateObservation so;
	private BoardVector bvec;
	
	public StateObsWithBoardVector(StateObservation so, BoardVector bvec) {
		this.so = so;
		this.bvec = bvec;
	}
	
	public StateObservation getStateObservation() {
		return so;
	}
	public BoardVector getBoardVector() {
		return bvec;
	}

}
