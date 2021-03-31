package games;

import games.Othello.XNTupleFuncsOthello;

/**
 * This class holds an {@code int[]} representation of a {@link StateObservation} object in its public member 
 * {@link #bvec}. It may hold additional information in auxiliary variables to allow the reconstruction of the 
 * generating {@link StateObservation} object from {@code this}.
 * 
 * @see XNTupleFuncs#getBoardVector(StateObservation)
 * @see XNTupleFuncsOthello#getBoardVector(StateObservation)
 */
public class BoardVector {

	public int[] bvec = null;		// the board vector
	public int[] aux = null; 		// auxiliary vector, needed in some cases (RubiksCube) to reconstruct from BoardVector 
									// back the generating StateObservation object
	
	public BoardVector(int[] bvec) {
		if (bvec!=null) this.bvec = bvec.clone();
	}

	public BoardVector(int[] bvec, int[] aux) {
		if (bvec!=null) this.bvec = bvec.clone();
		if ( aux!=null) this.aux = aux.clone();
	}

	public String toString() {
		String result = "("+bvec[0];
		for (int i=1; i<bvec.length; i++) {
			result += ","+bvec[i];
		}
		return result +")";
	}

}
