package games.Sim;

import java.io.Serializable;

public class Link implements Serializable 
{

	private int num,		// the link number (0,...,14 in case of K_6) 
				node,		// the number of the 'to'-node (from 1 to 6 in case of K_6)
				player;		// the player (1,2,3) who owns the node (0 if empty) 
	
	//Serial number
	private static final long serialVersionUID = 12L;

	Link(int k, int no, int pl)
	{
		num = k;
		node = no;
		player = pl;
	}

	public int getNode() {
		return node;
	}

	/**
	 * 
	 * @return the link number. In each Sim graph the links are numbered in a systematic way: 
	 *  	first from node 1 to higher nodes, then from node 2 to higher nodes, and so on. 
	 *  	Example for K_6-graph: <pre>
	 *      start node 1 1 1 1 1 | 2 2 2 2 | 3  3  3 | 4  4 | 5 
	 *        end node 2 3 4 5 6 | 3 4 5 6 | 4  5  6 | 5  6 | 6
	 *        link num 0 1 2 3 4 | 5 6 7 8 | 9 10 11 |12 13 |14 </pre>
	 */
	public int getNum() {
		return num;
	}

	public void setNode(int node) {
		this.node = node;
	}

	public int getPlayer() {
		return player;
	}

	public void setPlayer(int player) {
		this.player = player;
	}
	
	
}
