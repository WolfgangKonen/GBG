package games.Sim;

public class Link 
{

	private int node,		// the number of the 'to'-node (from 1 to 6 in case of K_6)
				player;
	
	Link(int no, int pl)
	{
		node = no;
		player = pl;
	}

	public int getNode() {
		return node;
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
