package games.Sim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.math3.exception.OutOfRangeException;

import agentIO.LoadSaveGBG;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

public class XNTupleFuncsSim extends XNTupleBase implements XNTupleFuncs, Serializable {

	List<int[]> list = new ArrayList<int[]>();
	transient AllPermutation perm; // /WK/ 'perm' is only needed to build 'list'. 'perm' could be local to setPermutations()
	
	/**
	 * {@link #arrLink}{@code .get(i)} holds all links emerging from node {@code i}.
	 */
	transient ArrayList<Link[]> arrLink;
	
	int [][] actions;
	int [][] symVec;
	int nCells, nPositionValues, nPlayers;
	
    /**
     * Provide a version UID here. Change the version UID for serialization only if a newer version is no 
     * longer compatible with an older one (older .gamelog or .agt.zip containing this object will
     * become unreadable)  
     */
	private static final long serialVersionUID = 5296353414404403319L;

	public XNTupleFuncsSim(int cl, int val, int pl) 
    {
		nCells = cl;
		nPositionValues = val;
		nPlayers = pl;
		
		setPermutations();			// fills 'list' with all permutations of nodes 
		setArrLink();
		setActions();
		
    }
	/**
	 * Special treatment after loading from disk (e. g. instantiation
	 * of transient members, setting of defaults for older .agt.zip). <br>
	 * Here, the default stub does just nothing. 
	 * @return true 
	 * 
	 * @see LoadSaveGBG#transformObjectToPlayAgent
	 */
	@Override
	public boolean instantiateAfterLoading() { 
		setPermutations();			// fills 'list' with all permutations of nodes 
		setArrLink();
		setActions();		
		return true; 
	}
	
	private int [] getNodes()
	{
		int [] nodes = new int [ConfigSim.GRAPH_SIZE];
		for(int i = 0; i < ConfigSim.GRAPH_SIZE; i++)
			nodes[i] = i + 1;
			
		return nodes;
	}
	
	private void setPermutations()
	{
		int [] nodes = getNodes();		// /WK/ specific to K_6 graph. Better use ConfigSim.GRAPH_SIZE
		
		perm = new AllPermutation(nodes);
		
		list.add(perm.GetFirst());
		
		while (perm.HasNext()) 
		{ 
			list.add(perm.GetNext());
		}
	}
	
	/**
	 * Build member {@link #arrLink} which is needed by {@link #findLink(int, int)}.<br>
	 * {@link #arrLink}{@code .get(i)} holds all links emerging from node {@code i}.
	 */
	private void setArrLink() {
		// we build an ArrayList of all links emerging from node no=1,...,ConfigSim.GRAPH_SIZE 
		arrLink = new ArrayList<Link[]>(ConfigSim.GRAPH_SIZE+1);
		arrLink.add(new Link[1]); 	// Insert 0th element as dummy. Only now we may call arrLink.add(1,...)
		StateObserverSim so = ConfigSim.SO;
		for (Node node : so.getNodes()) {
			arrLink.add(node.getNumber(), node.getLinks());
		}		
	}
	
	private int [] initVector()
	{
		int [] vec = new int [ConfigSim.GRAPH_SIZE*(ConfigSim.GRAPH_SIZE-1)/2];
		for(int i = 0; i < vec.length; i++)
			vec[i] = i;
			
		return vec;
	}
	
	private void setActions()
	{
		// /WK/ specific to K_6 graph. Better use ConfigSim.GRAPH_SIZE
		int [] vec = initVector();
		symVec = symmetryVectors(vec);
		actions = new int[symVec.length][];
		
		for (int i = 0; i < actions.length; i++) 
		{
    		actions[i] = new int[vec.length];
    		for (int j = 0; j < vec.length; j++)
    			actions[i][j] = whereHas(symVec[i],j);
    	}
		
	}
	
	 private int whereHas(int[] arr, int j) 
	 {
	    	for (int i = 0; i < arr.length; i++) 
	    		if (arr[i] == j) return i;
	    	throw new RuntimeException("whereHas: arr does not contain j!!");
	}
	 
	@Override
	public int getNumCells() 
	{
		return nCells;
	}

	@Override
	public int getNumPositionValues() 
	{
		return nPositionValues;
	}

	@Override
	public int getNumPlayers() 
	{
		return nPlayers;
	}

	@Override
	public int[] getBoardVector(StateObservation so) 
	{
		StateObserverSim sim = (StateObserverSim) so;
		int [] board = new int[ConfigSim.GRAPH_SIZE*(ConfigSim.GRAPH_SIZE-1)/2]; 
		int k = 0;
		
		for(int i = 0; i < sim.getNodes().length -1 ; i++)
		{
			for(int j = 0; j < sim.getNodes().length - 1 - i; j++)
			{
				board[k] = sim.getNodes()[i].getLinkPlayerPos(j);
				k++;
			}
		}
		
		return board;
	}

	@Override
	public int[][] symmetryVectors(int[] boardVector) {
		int [][] symmetricVectors = new int [list.size()][];
		//symmetricVectors.length
		for(int i = 0; i < symmetricVectors.length; i++)
			symmetricVectors[i] = getSymVector(boardVector,list.get(i));
		return symmetricVectors;
	}
	// crate a symetric vector for given permutation of nodes
	private int [] getSymVector(int [] boardVector,int [] permutation)
	{
		int [][] splittedBoardVec = splitVector(boardVector);
		int [][] splittedSymVec = new int[ConfigSim.GRAPH_SIZE-1][];
		
		// /WK/ specific to K_6 graph. Better use ConfigSim.GRAPH_SIZE
		for(int i = 0; i < ConfigSim.GRAPH_SIZE-1; i++)
				splittedSymVec[i] = getValuesSameNode(i,splittedBoardVec,permutation);
		
		return mergeVector(splittedSymVec);
	}
	
	private int [][] splitVector(int [] boardVector)
	{
		int [][] vec = new int [ConfigSim.GRAPH_SIZE - 1][];
		int index = 0;
		for(int i = 0; i < ConfigSim.GRAPH_SIZE - 1; i++)
		{
			vec [i] = new int[ConfigSim.GRAPH_SIZE - 1 - i];
			for(int j = 0; j < vec.length - i; j++)
			{
				vec[i][j] = boardVector[index];
				index++;
			}
		}
		
		return vec;
	}
	
	private int[] getValuesSameNode(int pos,int [][] boardVec, int [] permutation)
	{
		int[] vec = new int[ConfigSim.GRAPH_SIZE - 1 - pos];
		int index = 0;
		for(int i = pos + 1; i < ConfigSim.GRAPH_SIZE; i++)
		{
			if(permutation[i] < permutation[pos])
			{
				vec[index] = boardVec[permutation[i] - 1][permutation[pos] - (permutation[i] + 1)];
			}
			else
			{
				vec[index] = boardVec[permutation[pos] - 1][permutation[i] - (permutation[pos] + 1)];
			}
			index++;
		}
				
		return vec;
	}
	

	private int [] mergeVector(int [][] splittedVec)
	{
		int[]  vec = new int[ConfigSim.GRAPH_SIZE*(ConfigSim.GRAPH_SIZE-1)/2];
		int index = 0;
		
		for(int i = 0; i < splittedVec.length; i++)
			for(int j = 0; j < splittedVec[i].length; j++)
			{
				vec[index] = splittedVec[i][j];
				index++;
			}
		return vec;
	}
	@Override
	public int[] symmetryActions(int actionKey) 
	{
		int[] equivAction = new int[actions.length];
		for (int i = 0; i < actions.length; i++) 
			equivAction[i] = actions[i][actionKey];

		return equivAction;
	}
	
	/**
	 * @return an array A with the link 3-tuples of all triangles contained in the Sim graph.<p>
	 * 		In more detail: A[k][] is an int[3] vector carrying the 3 link numbers which make up 
	 * 		the k'th triangle. <br>
	 * 		There are Z=combination(K,3) such triangles (K={@link ConfigSim#GRAPH_SIZE}). 
	 * 		Z=20 for K=6.
	 */
	private int[][] allTriangleTuples() {
		int K = ConfigSim.GRAPH_SIZE;
		Link[] link = new Link[3];
		int Z = 1; 
		for (int i=K; i>K-3; i--) Z = Z*i;
		Z /= 6;		// 6 = 3!
		int[][] A = new int[Z][3]; 
		
		// we loop over all nodes that are corners of a triangle
		for (int i=1,m=0; i<K-1; i++) 
			for (int j=i+1; j<K; j++)
				for (int k=j+1; k<=K; k++,m++) {
					// find the links connecting those nodes and add them to A[m][]
					link[0] = findLink(i,j);
					link[1] = findLink(j,k);
					link[2] = findLink(i,k);
					for (int n=0; n<3; n++)
						A[m][n] = link[n].getNum();
				}
		return A;
	}
	
	/**
	 * @return an array A with the link 4-tuples of all quadruples contained in the Sim graph.<p>
	 * 		In more detail: A[k][] is an int[4] vector carrying the 4 link numbers which make up 
	 * 		the k'th quadruple. <br>
	 * 		There are Z=2*combination(K,4) such quadruples (K={@link ConfigSim#GRAPH_SIZE}). 
	 * 		combination(K,4) is the number of 4-nodes-subsets in the graph.
	 * 		Why factor 2? - Because for each 4-nodes-subset there are 2 (!) possible 
	 * 		closed paths: <br><pre>
	 * 			(1-2, 2-3, 3-4, 4-1) and (1-3, 3-4, 4-2, 2-1). </pre>
	 * 		Z=30 for K=6.
	 */
	private int[][] allQuadrupleTuples() {
		int K = ConfigSim.GRAPH_SIZE;
		Link[] link = new Link[4];
		int Z = 1; 
		for (int i=K; i>K-4; i--) Z = Z*i;
		Z /= 24;	// 24 = 4!
		Z *= 2;
		int[][] A = new int[Z][4]; 
		
		// --- now done in constructor ---
//		// first we build an ArrayList of all links emerging from node no=1,...,K 
//		ArrayList<Link[]> arrLink = new ArrayList<Link[]>(K+1);
//		arrLink.add(link); 	// Insert 0th element as dummy. Only now we may call arrLink.add(1,...)
//		StateObserverSim so = ConfigSim.SO;
//		for (Node node : so.getNodes()) {
//			arrLink.add(node.getNumber(), node.getLinks());
//		}
		
		//  we loop over all nodes that are corners of a quadruple
		for (int i=1,m=0; i<K-2; i++) 
			for (int j=i+1; j<K-1; j++)
				for (int k=j+1; k<K; k++) 
					for (int l=k+1; l<=K; l++)
					{
						// find the links connecting those nodes and add them to A[m][]
						// 1st path:
						link[0] = findLink(i,j);
						link[1] = findLink(j,k);
						link[2] = findLink(k,l);
						link[3] = findLink(i,l);
						for (int n=0; n<4; n++)
							A[m][n] = link[n].getNum();
						m++;
						// 2nd path:
						link[0] = findLink(i,k);
						link[1] = findLink(k,l);
						link[2] = findLink(j,l);
						link[3] = findLink(i,j);
						for (int n=0; n<4; n++)
							A[m][n] = link[n].getNum();
						m++;
					}
		return A;
	}
	
	/**
	 * @return an array A with the link 6-tuples of all 4-cliques contained in the Sim graph.<p>
	 * 		In more detail: A[k][] is an int[6] vector carrying the 6 link numbers which make up 
	 * 		the k'th 4-clique. <br> 
	 * 		The difference between quadruple and 4-clique: a quadruple connects the 
	 * 		4 nodes by a closed path of 4 links, but a 4-clique has all the 6 links that 
	 * 		exist between the 4 nodes of the subset. <br>
	 * 		There are Z=combination(K,4) such 4-cliques (K={@link ConfigSim#GRAPH_SIZE}). 
	 * 		Z=15 for K=6.
	 */
	private int[][] all4CliqueTuples() {
		int K = ConfigSim.GRAPH_SIZE;
		Link[] link = new Link[6];
		int Z = 1; 		
		for (int i=K; i>K-4; i--) Z = Z*i;
		Z /= 24;	// 24 = 4!		
		int[][] A = new int[Z][6]; 
		
		// --- now done in constructor ---
//		// first we build an ArrayList of all links emerging from node no=1,...,K 
//		ArrayList<Link[]> arrLink = new ArrayList<Link[]>(K+1);
//		arrLink.add(link); 	// Insert 0th element as dummy. Only now we may call arrLink.add(1,...)
//		StateObserverSim so = ConfigSim.SO;
//		for (Node node : so.getNodes()) {
//			arrLink.add(node.getNumber(), node.getLinks());
//		}
		
		// we loop over all nodes that are elements of a 4-clique
		for (int i=1,m=0; i<K-2; i++) 
			for (int j=i+1; j<K-1; j++)
				for (int k=j+1; k<K; k++) 
					for (int l=k+1; l<=K; l++,m++)
					{
						// find the links connecting those nodes and add them to A[m][]
						link[0] = findLink(i,j);
						link[1] = findLink(i,k);
						link[2] = findLink(i,l);
						link[3] = findLink(j,k);
						link[4] = findLink(j,l);
						link[5] = findLink(k,l);
						for (int n=0; n<6; n++)
							A[m][n] = link[n].getNum();
					}
		return A;
	}
	
	// Helper function for allTriangleTuples, allQuadrupleTuples & all4CliqueTuples:
	// Find the link that connects node i with node j. 
	// Condition: i<j (!)
	private Link findLink(int i, int j) {
		for (Link lnk : arrLink.get(i)) 	// arrLink.get(i) holds the links of node i
			if (lnk.getNode()==j) {
				return lnk;
			}
		throw new RuntimeException("findLink: Link ("+i+","+j+") not found!");
	}

	private int[][] merge(int[][] A, int[][] B) {
		int[][] C = new int[A.length+B.length][];
		for (int i=0; i<A.length; i++) C[i]=A[i];
		int c=A.length;
		for (int i=0; i<B.length; i++) C[i+c]=B[i];
		return C;
	}
	
	@Override
	public int[][] fixedNTuples(int mode) {
		// /WK/ bug: the next line is not general enough (specific to K_6 graph) AND it is 
		// also a too big n-tuple, at least for P=3 players (would raise an out-of-mem error).
//		int nTuple[][] = {{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14}};
		
		// /WK/ just some other choices (case 0 and 1 are not necessarily sensible).
		//		Recommended: mode==3.
		switch(mode)
		{
		case 0: return new int[][] {
			//--- 8 random 2-tuples
			{0, 8}, {0, 9}, {1, 9}, {2, 4}, {2, 6}, {2, 10}, {3, 11}, {4, 11} 
		};
		case 1: return new int[][] {
			//--- 4 random 4-tuples
			{0, 1, 8, 9}, {2, 11, 10, 3}, 
			{11, 5, 4, 8}, {14, 8, 6, 2}
		};
		case 2: 
			//--- all triangle tuples ---
			return allTriangleTuples();
		case 3:
			//--- all triangle tuples + all quadruple tuples
			return merge(allTriangleTuples(),allQuadrupleTuples());
		case 4:
			//--- all triangle tuples + all quadruple tuples
			return merge(allTriangleTuples(),all4CliqueTuples());
		default: 
			throw new OutOfRangeException(mode, 0, fixedModes.length-1);
		}
	}

	@Override
	public String fixedTooltipString() {
		return "<html>"
				+ "0: 8 random 2-tuples<br>"
				+ "1: 4 random 4-tuples<br>"
				+ "2: all triangle 3-tuples<br>"
				+ "3: all triangle + all quadruple tuples<br>"
				+ "4: all triangle + all 4-clique tuples<br>"
				+ "</html>";
	}

	private static int[] fixedModes = {0,1,2,3,4};
	
	@Override
	public int[] getAvailFixedNTupleModes() {
		return fixedModes;
	}

	@Override
	public HashSet adjacencySet(int iCell) {
		// /WK/ this is buggy, because in a symmetric graph every link should have the same number
		// of adjacent links. Also, it should be done programmatically in a way that generalizes 
		// for every ConfigSim.GRAPH_SIZE: For the given link number iCell, use proper logic and 
		// findLink() to find all other links connected to link iCell.
		HashSet adjSet = new HashSet();
		int node1 = -1, node2 = -1, count = 0;
		for(int i = 0; i < ConfigSim.GRAPH_SIZE -1 ; i++)
		{
			for(int j = 0; j < ConfigSim.GRAPH_SIZE - 1 - i; j++)
			{
				if(iCell == count)
				{
					node1 = i;
					node2 = (i+1) + j;
					count++;
				}
				else
					count++;
			}
		}
		count = 0;
		System.out.println(node1 + "    " +  node2);
		for(int i = 0; i < ConfigSim.GRAPH_SIZE -1 ; i++)
		{
			for(int j = 0; j < ConfigSim.GRAPH_SIZE - 1 - i; j++)
			{
				if(((node1 == i || node2 == i) || (node1 == (i+1) + j || node2 == (i+1) + j)) && !(node1 == i && node2 == (i+1) + j))
				{
					adjSet.add(count);
					count++;
				}
				else
					count++;
			}
		}
		return adjSet;
	}

	
}
