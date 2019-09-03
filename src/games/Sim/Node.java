package games.Sim;

public class Node 
{
	private Link [] links; 
	private int number;			// the number of the node (from 1 to 6 in case of K_6)
	
	Node(int size, int num)
	{
		number = num;
		links = new Link [size-1];	// each node has size-1 links to all other nodes
		setupLinks(size);
	}

	private void setupLinks(int size)
	{
		int n = number + 1;
		int k = 0;
		for(int nod=1; nod<number; nod++) 
			k += (size-nod);
		// now k carries the link number of the first link starting from node 'number'
		
		for(int i = 0; i < links.length; i++)
		{
			if(n > links.length + 1)
				n = 1;
			links[i] = new Link(k,n,0); // link to node n with pl=0 ("empty", no player owns this link)
			k++;
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
	
	/**
	 * @param node the 'to'-node, i.e. the link is from this to 'to'-node
	 * @return the link's player number (1,2,3), which is GBG player number + 1
	 */
	public int getLinkPlayer(int node)
	{
		for(Link link : links)
			if(node == link.getNode())
				return link.getPlayer();
		//return 0;
		throw new RuntimeException("We should never get here");  // /WK/
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
