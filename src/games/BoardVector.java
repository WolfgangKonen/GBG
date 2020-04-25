package games;

public class BoardVector {
	
	public int[] bvec = null;		// the board vector
	public int[] aux = null; 		// auxiliary vector, needed in some cases (RubiksCube) to reconstruct from BoardVector 
									// back the generating StateObservation
	
	public BoardVector(int[] bvec) {
		this.bvec = bvec.clone();
	}

}
