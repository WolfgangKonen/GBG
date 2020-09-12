package controllers;

/**
 * A vector of {@link PlayAgent} objects. It has the additional property to allow cyclic shifting
 * of all players (method {@link #shift(int)}).
 */
public class PlayAgtVector {

	public PlayAgent[] pavec;
	private final int nPlayer;
	
	public PlayAgtVector(int N) {
		pavec = new PlayAgent[N];
		nPlayer = N;
	}
	
	public PlayAgtVector(PlayAgent p0, PlayAgent p1) {
		pavec = new PlayAgent[] {p0,p1};
		nPlayer = 2;
	}

	public PlayAgtVector(PlayAgent p0, PlayAgent p1, PlayAgent p2) {
		pavec = new PlayAgent[] {p0,p1,p2};
		nPlayer = 3;
	}

	public PlayAgtVector(PlayAgent[] paVector) {
		pavec = paVector.clone();
		nPlayer = paVector.length;
	}

	public PlayAgtVector(PlayAgtVector other) {
		pavec = other.pavec.clone();
		nPlayer = other.pavec.length;
	}

	public PlayAgtVector shift(int k) {
		PlayAgtVector paVector = new PlayAgtVector(nPlayer);
		for (int i=0; i<nPlayer; i++) paVector.pavec[(i+k)%nPlayer] = pavec[i];
		return paVector;
	}
	
	public int getNumPlayers() {
		return nPlayer;
	}
	
}
