package games.Sim;

public class Node 
{
	private Link [] links; 
	private int number;
	
	Node(int size, int num)
	{
		number = num;
		links = new Link [size-1];
		setupLinks();
	}

	private void setupLinks()
	{
		int n = number + 1;
		for(int i = 0; i < links.length; i++)
		{
			if(n > links.length + 1)
				n = 1;
			links[i] = new Link(n, 0);
			n++;
		}
	}
	
	public Link [] getLinks() {
		return links;
	}

	public void setLinksCopy(Link [] links) 
	{
		for(int i = 0; i < links.length; i++)
			this.links[i].setPlayer(links[i].getPlayer());
	}

	
	public void setLinks(Link [] links) 
	{
		for(int i = 0; i < links.length; i++)
			this.links = links;
	}
	
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
	
	public int getLinkPlayer(int node)
	{
		for(Link link : links)
			if(node == link.getNode())
				return link.getPlayer();
		return 0;
	}
	
	public int getLinkNodePos(int pos)
	{
		return links[pos].getNode();
	}
	
	public int getLinkPlayerPos(int pos)
	{
		return links[pos].getPlayer();
	}
	
	public boolean hasSpaceLeft()
	{
		for(Link link : links)
			if(link.getPlayer() == 0)
				return true;
		return false;
	}
	
	public void setPlayerPos(int pos, int player)
	{
		links[pos].setPlayer(player);
	}
	
	public void setPlayerNode(int node, int player)
	{
		for(Link link : links)
			if(link.getNode() == node)
				link.setPlayer(player);;
	}
}
