package games.Sim;

public class Edge_OLD {
	
	private int node1, node2, player;
	
	public Edge_OLD(int n1, int n2)
	{
		node1 = n1;
		node2 = n2;
		player = 0;
	}
	
	public int getNode1() {
		return node1;
	}

	public void setNode1(int node1) {
		this.node1 = node1;
	}

	public int getNode2() {
		return node2;
	}

	public void setNode2(int node2) {
		this.node2 = node2;
	}

	public int getPlayer() {
		return player;
	}

	public void setPlayer(int player) {
		this.player = player;
	}


}
