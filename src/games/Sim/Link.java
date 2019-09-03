package games.Sim;

public class Link 
{

	private int num,		// the link number (0,...,14 in case of K_6) 
				node,		// the number of the 'to'-node (from 1 to 6 in case of K_6)
				player;		// the player (1,2,3) who owns the node (0 if empty) 
	
	Link(int k, int no, int pl)
	{
		num = k;
		node = no;
		player = pl;
	}

	public int getNode() {
		return node;
	}

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
