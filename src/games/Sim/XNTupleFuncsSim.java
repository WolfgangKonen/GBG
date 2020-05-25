package games.Sim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.exception.OutOfRangeException;

import agentIO.LoadSaveGBG;
import games.BoardVector;
import games.StateObsWithBoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;
import tools.PermutationIterable;
import tools.PermutationIterator;

public class XNTupleFuncsSim extends XNTupleBase implements XNTupleFuncs, Serializable {

	List<int[]> list = new ArrayList<int[]>();
	transient AllPermutation perm; // /WK/ 'perm' is only needed to build 'list'. 'perm' could be local to setPermutations()
	
	transient PermutationIterable <Integer> pi;
//	/**
//	 * {@link #arrLink}{@code .get(i)} holds all links emerging from node {@code i}.
//	 */
//	transient ArrayList<Link[]> arrLink;
	
	int [][] actions;
	BoardVector[] symVec;
	int nCells, nPositionValues, nPlayers;
	
    transient private Random rand = new Random ();
    
//    transient private StateObserverSim configSO = ConfigSim.SO;


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
		
//		if (ConfigSim.NUM_NODES<5) 
			setPermutations();			// prepare permutation iterator
//		setArrLink();
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
		if (this.nCells==0 || this.nPlayers==0 || this.nPositionValues==0) {
			// /WK/ for unclear reasons, re-loading a Sim agent from disk has all these
			// xnf-members equal to 0, so we re-instantiate them
			this.nCells = ConfigSim.NUM_NODES*(ConfigSim.NUM_NODES-1)/2;
			this.nPositionValues = ConfigSim.NUM_PLAYERS+1;
			this.nPlayers = ConfigSim.NUM_PLAYERS;
		}
//		if (ConfigSim.NUM_NODES<5) 
			setPermutations();			// prepare permutation iterator 
//		setArrLink();
		setActions();		
		return true; 
	}
	
	private int [] getNodes()
	{
		int [] nodes = new int [ConfigSim.NUM_NODES];
		for(int i = 0; i < ConfigSim.NUM_NODES; i++)
			nodes[i] = i + 1;
			
		return nodes;
	}
	
	private void setPermutations()
	{
		// this is the new attempt which should also work for large node number, 
		// because it only creates the list of nodes 'il' and on top of this a 
		// PermutationIterable 'pi'
		//
        List <Integer> il = new ArrayList <Integer> ();
        for (int i = 0; i < ConfigSim.NUM_NODES; i++) il.add(Integer.valueOf(i));
        pi = new PermutationIterable <Integer> (il);

        // this is the old version by Percy Wünsch. It explicitly creates all 
        // permutations in perm and therefore crashes (mem exhausted) if the 
        // number of nodes is larger
//		int [] nodes = getNodes();		
//		perm = new AllPermutation(nodes);
//		list.add(perm.GetFirst());
//		while (perm.HasNext()) 
//		{ 
//			list.add(perm.GetNext());
//		}
	}
	
//	/**
//	 * Build member {@link #arrLink} which is needed by {@link #findLink(int, int)}.<br>
//	 * {@link #arrLink}{@code .get(i)} holds all links emerging from node {@code i}.
//	 */
//	private void setArrLink() {
//		// we build an ArrayList of all links emerging from node no=1,...,ConfigSim.GRAPH_SIZE 
//		arrLink = new ArrayList<Link[]>(ConfigSim.NUM_NODES+1);
//		arrLink.add(new Link[1]); 	// Insert 0th element as dummy. Only now we may call arrLink.add(1,...)
//		StateObserverSim so = ConfigSim.SO;
//		for (Node node : so.getNodes()) {
//			arrLink.add(node.getNumber(), node.getLinks());
//		}		
//	}
	
	private int [] initVector()
	{
		int [] vec = new int [ConfigSim.NUM_NODES*(ConfigSim.NUM_NODES-1)/2];
		for(int i = 0; i < vec.length; i++)
			vec[i] = i;
			
		return vec;
	}
	
	private void setActions()
	{
//		int [] vec = initVector();
//		symVec = symmetryVectors(new BoardVectorSim(vec,new StateObserverSim()),0);
//		actions = new int[symVec.length][];
//		
//		for (int i = 0; i < actions.length; i++) 
//		{
//    		actions[i] = new int[vec.length];
//    		for (int j = 0; j < vec.length; j++)
//    			actions[i][j] = whereHas(symVec[i].bvec,j);
//    	}
		
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

	/**
	 * Sim has an astonishing large number of K! symmetries (K={@link ConfigSim#NUM_NODES}).
	 * 
	 * @return the maximum number of symmetries in this game
	 */
	public int getNumSymmetries() {
		int nSym=1;
		for (int i=1; i<= ConfigSim.NUM_NODES; i++) nSym *= i;
		return nSym;
	}
	
	@Override
	public BoardVector getBoardVector(StateObservation so) 
	{
		if (so instanceof StateObserverSim) {
			StateObserverSim sim = (StateObserverSim) so;
			int [] board = new int[ConfigSim.NUM_NODES*(ConfigSim.NUM_NODES-1)/2]; 
			
			for(int i=0, k=0; i < sim.getNumNodes() -1 ; i++) {
				for(int j = i+1; j <  sim.getNumNodes(); j++) {
					board[k++] = sim.getLinkFromTo(i,j);
				}
			}
			
			return new BoardVector(board);			
		} 
		throw new RuntimeException("StateObservation so is not StateObserverSim");
	}

	// should return a symmetry permuted BoardVector, but does not yet work
	//
	private BoardVector getBoardVector(StateObservation so, List<Integer> rli) 
	{
		if (so instanceof StateObserverSim) {
			StateObserverSim sim = (StateObserverSim) so;
			int [] board = new int[ConfigSim.NUM_NODES*(ConfigSim.NUM_NODES-1)/2]; 
			int k = 0;
			
			for(int i = 0; i < sim.getNumNodes() -1 ; i++) {
				for(int j = i+1; j < sim.getNumNodes(); j++) {
					board[k++] = sim.getLinkFromTo(rli.get(i),rli.get(j));
				}
			}
			
			return new BoardVector(board);
		}
		throw new RuntimeException("StateObservation so is not StateObserverSim");
	}

	@Override
	public BoardVector[] symmetryVectors(StateObsWithBoardVector curSOWB, int n) {
		BoardVector boardVector = curSOWB.getBoardVector();
		StateObservation so = curSOWB.getStateObservation();
		assert so instanceof StateObserverSim : "Ooops, so is not of class StateObserverSim";
		StateObserverSim sim = (StateObserverSim) so;
		
		//TODO: This does not yet guarantee that all n symmetric states are different!!
		//      Way out: make a set with exactly n-1 different numbers (if K! is large)
		//		or make a permutation of (0,1,...,K!-1) and pick only the first n elements (if K! is small)
		BoardVector[] symmetricVectors = new BoardVector[n];
		symmetricVectors[0] = boardVector;
        PermutationIterator <Integer> pitor = (PermutationIterator  <Integer>) pi.iterator ();
        for (int i = 1; i < n; ++i)
        {
            int rnd = rand.nextInt ((int) pitor.last); 
            List <Integer> rli = pitor.get (rnd);
            symmetricVectors[i] = getBoardVector(so,rli); 		
//            System.out.println(symmetricVectors[i].toString());
//            int dummy = 1; 
        }
		return symmetricVectors;
	}
	
	@Override
	public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
		
		throw new RuntimeException("symmetryVectors(BoardVector,int) is not implemented for XNTupleFuncsSim!");
		// Problem: this method cannot construct symmetric states if the underlying StateObserverSim is unknown.
		// Way out: Use other method symmetryVectors(StateObsWithBoardVector curSOWB, int n). 
		
//		assert boardVector instanceof BoardVectorSim : "Oops, boardVector is not of class BoardVectorSim!";
//		StateObservation so = ((BoardVectorSim)boardVector).sim;
//		
//		// TODO
//		n = 10; // DBG
//		BoardVector[] symmetricVectors = new BoardVector[n];
//        PermutationIterator <Integer> pitor = (PermutationIterator  <Integer>) pi.iterator ();
//        for (int i = 0; i < n; ++i)
//        {
//            int rnd = rand.nextInt ((int) pitor.last); 
//            List <Integer> rli = pitor.get (rnd);
//            symmetricVectors[i] = getBoardVector(so,rli); 		
//            System.out.println(symmetricVectors[i].toString());
//            int dummy = 1; 
//        }

//		for(int i = 0; i < symmetricVectors.length; i++)
//			symmetricVectors[i] = getSymVector(boardVector,list.get(i));

//		return symmetricVectors;
	}
	
	// --- old version (P. Wünsch), now obsolete
	// crate a symmetric vector for given permutation of nodes
//	private BoardVector getSymVector(BoardVector bv,int [] permutation)
//	{
//		int[] boardVector = bv.bvec;
//		int [][] splittedBoardVec = splitVector(boardVector);
//		int [][] splittedSymVec = new int[ConfigSim.NUM_NODES-1][];
//		
//		for(int i = 0; i < ConfigSim.NUM_NODES-1; i++)
//				splittedSymVec[i] = getValuesSameNode(i,splittedBoardVec,permutation);
//		
//		return new BoardVector(mergeVector(splittedSymVec));
//	}
	
	// --- old version (P. Wünsch), now obsolete
//	private int [][] splitVector(int [] boardVector)
//	{
//		int [][] vec = new int [ConfigSim.NUM_NODES - 1][];
//		int index = 0;
//		for(int i = 0; i < ConfigSim.NUM_NODES - 1; i++)
//		{
//			vec [i] = new int[ConfigSim.NUM_NODES - 1 - i];
//			for(int j = 0; j < vec.length - i; j++)
//			{
//				vec[i][j] = boardVector[index];
//				index++;
//			}
//		}
//		
//		return vec;
//	}
	
	// --- old version (P. Wünsch), now obsolete
//	private int[] getValuesSameNode(int pos,int [][] boardVec, int [] permutation)
//	{
//		int[] vec = new int[ConfigSim.NUM_NODES - 1 - pos];
//		int index = 0;
//		for(int i = pos + 1; i < ConfigSim.NUM_NODES; i++)
//		{
//			if(permutation[i] < permutation[pos])
//			{
//				vec[index] = boardVec[permutation[i] - 1][permutation[pos] - (permutation[i] + 1)];
//			}
//			else
//			{
//				vec[index] = boardVec[permutation[pos] - 1][permutation[i] - (permutation[pos] + 1)];
//			}
//			index++;
//		}
//				
//		return vec;
//	}
	

	// --- old version (P. Wünsch), now obsolete
//	private int [] mergeVector(int [][] splittedVec)
//	{
//		int[]  vec = new int[ConfigSim.NUM_NODES*(ConfigSim.NUM_NODES-1)/2];
//		int index = 0;
//		
//		for(int i = 0; i < splittedVec.length; i++)
//			for(int j = 0; j < splittedVec[i].length; j++)
//			{
//				vec[index] = splittedVec[i][j];
//				index++;
//			}
//		return vec;
//	}
	
	@Override
	public int[] symmetryActions(int actionKey) 
	{
		// TODO!!
		return null;
//		int[] equivAction = new int[actions.length];
//		for (int i = 0; i < actions.length; i++) 
//			equivAction[i] = actions[i][actionKey];
//
//		return equivAction;
	}
	
	/**
	 * @return an array A with the link 3-tuples of all triangles contained in the Sim graph.<p>
	 * 		In more detail: A[k][] is an int[3] vector carrying the 3 link numbers which make up 
	 * 		the k'th triangle. <br>
	 * 		There are Z=combination(K,3) such triangles (K={@link ConfigSim#NUM_NODES}). 
	 * 		Z=20 for K=6.
	 */
	private int[][] allTriangleTuples() {
		int K = ConfigSim.NUM_NODES;
		int[] link = new int[3];
		int Z = 1; 
		for (int i=K; i>K-3; i--) Z = Z*i;
		Z /= 6;		// 6 = 3!
		int[][] A = new int[Z][3]; 
		StateObserverSim configSO = new StateObserverSim();
		
		// We loop over all nodes that are corners of a triangle:
		// i is the lowest-numbered node, j the 2nd-lowest-numbered node and k is the highest numbered node.
		// m is the number of the triangle in 0,...,Z-1.
		for (int i=0,m=0; i<K-2; i++) 
			for (int j=i+1; j<K-1; j++)
				for (int k=j+1; k<K; k++,m++) {
					// find the links connecting those nodes and add them to A[m][]
					link[0] = configSO.inputToActionInt(i,j);
					link[1] = configSO.inputToActionInt(j,k);
					link[2] = configSO.inputToActionInt(i,k);
					for (int n=0; n<3; n++)
						A[m][n] = link[n];
				}
		return A;
	}
	
	/**
	 * @return an array A with the link 4-tuples of all quadruples contained in the Sim graph.<p>
	 * 		In more detail: A[k][] is an int[4] vector carrying the 4 link numbers which make up 
	 * 		the k'th quadruple. <br>
	 * 		There are Z=2*combination(K,4) such quadruples (K={@link ConfigSim#NUM_NODES}). 
	 * 		combination(K,4) is the number of 4-nodes-subsets in the graph.
	 * 		Why factor 2? - Because for each 4-nodes-subset there are 2 (!) possible 
	 * 		closed paths: <br><pre>
	 * 			(1-2, 2-3, 3-4, 4-1) and (1-3, 3-4, 4-2, 2-1). </pre>
	 * 		Z=30 for K=6.
	 */
	private int[][] allQuadrupleTuples() {
		int K = ConfigSim.NUM_NODES;
		int[] link = new int[4];
		int Z = 1; 
		for (int i=K; i>K-4; i--) Z = Z*i;
		Z /= 24;	// 24 = 4!
		Z *= 2;
		int[][] A = new int[Z][4]; 
		StateObserverSim configSO = new StateObserverSim();
		
		// --- now done in constructor ---
//		// first we build an ArrayList of all links emerging from node no=1,...,K 
//		ArrayList<Link[]> arrLink = new ArrayList<Link[]>(K+1);
//		arrLink.add(link); 	// Insert 0th element as dummy. Only now we may call arrLink.add(1,...)
//		StateObserverSim so = ConfigSim.SO;
//		for (Node node : so.getNodes()) {
//			arrLink.add(node.getNumber(), node.getLinks());
//		}
		
		//  we loop over all nodes that are corners of a quadruple
		// i is the lowest-numbered node, j the 2nd-lowest-numbered node ... and l is the highest numbered node.
		// m is the number of the quadruple in 0,...,Z-1.
		for (int i=0,m=0; i<K-3; i++) 
			for (int j=i+1; j<K-2; j++)
				for (int k=j+1; k<K-1; k++) 
					for (int l=k+1; l<K; l++)
					{
						// find the links connecting those nodes and add them to A[m][]
						// 1st path:
						link[0] = configSO.inputToActionInt(i,j);
						link[1] = configSO.inputToActionInt(j,k);
						link[2] = configSO.inputToActionInt(k,l);
						link[3] = configSO.inputToActionInt(i,l);
						for (int n=0; n<4; n++)
							A[m][n] = link[n];
						m++;
						// 2nd path:
						link[0] = configSO.inputToActionInt(i,k);
						link[1] = configSO.inputToActionInt(k,l);
						link[2] = configSO.inputToActionInt(j,l);
						link[3] = configSO.inputToActionInt(i,j);
						for (int n=0; n<4; n++)
							A[m][n] = link[n];
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
	 * 		There are Z=combination(K,4) such 4-cliques (K={@link ConfigSim#NUM_NODES}). 
	 * 		Z=15 for K=6.
	 */
	private int[][] all4CliqueTuples() {
		int K = ConfigSim.NUM_NODES;
		int[] link = new int[6];
		int Z = 1; 		
		for (int i=K; i>K-4; i--) Z = Z*i;
		Z /= 24;	// 24 = 4!		
		int[][] A = new int[Z][6]; 
		StateObserverSim configSO = new StateObserverSim();
		
		// --- now done in constructor ---
//		// first we build an ArrayList of all links emerging from node no=1,...,K 
//		ArrayList<Link[]> arrLink = new ArrayList<Link[]>(K+1);
//		arrLink.add(link); 	// Insert 0th element as dummy. Only now we may call arrLink.add(1,...)
//		StateObserverSim so = ConfigSim.SO;
//		for (Node node : so.getNodes()) {
//			arrLink.add(node.getNumber(), node.getLinks());
//		}
		
		// we loop over all nodes that are elements of a 4-clique
		// i is the lowest-numbered node, j the 2nd-lowest-numbered node ... and l is the highest numbered node.
		// m is the number of the 4-clique in 0,...,Z-1.
		for (int i=0,m=0; i<K-3; i++) 
			for (int j=i+1; j<K-2; j++)
				for (int k=j+1; k<K-1; k++) 
					for (int l=k+1; l<K; l++,m++)
					{
						// find the links connecting those nodes and add them to A[m][]
						link[0] = configSO.inputToActionInt(i,j);
						link[1] = configSO.inputToActionInt(i,k);
						link[2] = configSO.inputToActionInt(i,l);
						link[3] = configSO.inputToActionInt(j,k);
						link[4] = configSO.inputToActionInt(j,l);
						link[5] = configSO.inputToActionInt(k,l);
						for (int n=0; n<6; n++)
							A[m][n] = link[n];
					}
		return A;
	}
	
//	// Helper function for allTriangleTuples, allQuadrupleTuples & all4CliqueTuples:
//	// Find the link that connects node i with node j. 
//	// Condition: i<j (!)
//	private Link findLink(int i, int j) {
//		for (Link lnk : arrLink.get(i)) 	// arrLink.get(i) holds the links of node i
//			if (lnk.getNode()==j) {
//				return lnk;
//			}
//		throw new RuntimeException("findLink: Link ("+i+","+j+") not found!");
//	}

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
	public int[] fixedNTupleModesAvailable() {
		return fixedModes;
	}

	@Override
	public HashSet adjacencySet(int iCell) {
		HashSet adjSet = new HashSet();
		int node1 = -1, node2 = -1, count = 0;
		for(int i = 0; i < ConfigSim.NUM_NODES -1 ; i++)
		{
			for(int j = 0; j < ConfigSim.NUM_NODES - 1 - i; j++)
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
		for(int i = 0; i < ConfigSim.NUM_NODES -1 ; i++)
		{
			for(int j = 0; j < ConfigSim.NUM_NODES - 1 - i; j++)
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
