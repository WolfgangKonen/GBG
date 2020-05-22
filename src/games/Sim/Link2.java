package games.Sim;

import java.io.Serializable;

/**
 *  This class holds all links emanating from a certain node {@code nFrom} in the Sim graph. 
 *  <p>
 *  Note that only links with {@literal nFrom < i} are relevant, where i is an index for {@code lTo}.
 *
 */
public class Link2 implements Serializable 
{

	private int nFrom;		// the number of the 'from'-node (from 0 to K-1 in the case of K nodes)
	private int[] lTo;		// lTo[i]: the player (1,2,3) who owns the link from node nFrom to node i (0 if empty) 
	
	private static final long serialVersionUID = 12L;		//Serial number

	Link2(int nFrom, int numNodes)
	{
		this.nFrom = nFrom;
		this.lTo = new int[numNodes];
	}
	Link2(Link2 other) {
		this.nFrom = other.nFrom;
		this.lTo = other.lTo.clone();
	}

	public boolean hasSpaceLeft() {
		for (int i=nFrom+1; i<lTo.length; i++) {
			if (lTo[i]==0) return true;
		}
		return false;
	}
	
	/**
	 * @return 'from'-node of {@code this} (range 0 to K-1).
	 */
	public int getNode() {
		return nFrom;
	}

	/** 
	 * @param i	'to'-node (range 0 to K-1)
	 * @return the player 1,2,3 who owns the link from {@link #getNode()} to node {@code i}. 0 if link is empty.
	 */
	public int getPlayer(int i) {
		return lTo[i];
	}

	// never needed
//	public void setNode(int node) {
//		this.nFrom = node;
//	}

	/**
	 * Set the link from {@link #getNode()} to node {@code i}.
	 * @param i 'to'-node (range 0 to K-1)
	 * @param player 1, 2, 3 for player P0, P1, P2. 0 for empty link.
	 */
	public void setPlayer(int i, int player) {
		this.lTo[i] = player;
	}
	
	
}
