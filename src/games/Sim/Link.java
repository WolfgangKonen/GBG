package games.Sim;

public class Link 
{

	private int node,player;
	
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
