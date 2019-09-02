package games.Sim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.math3.exception.OutOfRangeException;

import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

public class XNTupleFuncsSim extends XNTupleBase implements XNTupleFuncs, Serializable {

	List<int[]> list = new ArrayList<int[]>();
	transient AllPermutation perm;
	int [][] actions;
	int [][] symVec;
	private static int[] fixedModes = {2};
	int cells, positionValues, numPlayers;
    /**
	 * 
	 */
	private static final long serialVersionUID = 5296353414404403319L;

	/**
	 * 
	 */

	public XNTupleFuncsSim(int cl, int val, int pl) 
    {
		cells = cl;
		positionValues = val;
		numPlayers = pl;
		
		setPermutations();
		setActions();
		
    }
	
	private void setPermutations()
	{
		int [] nodes = {1,2,3,4,5,6};
		
		perm = new AllPermutation(nodes);
		
		list.add(perm.GetFirst());
		
		while (perm.HasNext()) 
	       { 
			 list.add(perm.GetNext());
	            
	       }
	}
	
	private void setActions()
	{
		int [] vec = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14};
		symVec = symmetryVectors(vec);
		actions = new int[symVec.length][];
		
		for (int i = 0; i < actions.length; i++) 
		{
    		actions[i] = new int[15];
    		for (int j = 0; j < 15; j++)
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
		return cells;
	}

	@Override
	public int getNumPositionValues() 
	{
		return positionValues;
	}

	@Override
	public int getNumPlayers() 
	{
		return numPlayers;
	}

	@Override
	public int[] getBoardVector(StateObservation so) 
	{
		StateObserverSim sim = (StateObserverSim) so;
		int [] board = new int[15]; 
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
		int [][] splittedSymVec = new int[5][];
		
		for(int i = 0; i < 5; i++)
				splittedSymVec[i] = getValuesSameNode(i,splittedBoardVec,permutation);
		
		return mergeVector(splittedSymVec);
	}
	
	private int [][] splitVector(int [] boardVector)
	{
		int [][] vec = new int [5][];
		int index = 0;
		for(int i = 0; i < 5; i++)
		{
			vec [i] = new int[5-i];
			for(int j = 0; j < 5 - i; j++)
			{
				vec[i][j] = boardVector[index];
				index++;
			}
		}
		
		return vec;
	}
	
	private int[] getValuesSameNode(int pos,int [][] boardVec, int [] permutation)
	{
		int[] vec = new int[5 - pos];
		int index = 0;
		for(int i = pos + 1; i < 6; i++)
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
		int[]  vec = new int[15];
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

	@Override
	public int[][] fixedNTuples(int mode) {
		// /WK/ bug: the next line is not general enough (specific to K_6 graph) AND it is 
		// also a too big n-tuple, at least for P=3 players (would raise an out-of-mem error).
//		int nTuple[][] = {{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14}};
		
		// /WK/ just some other choice (not necessarily sensible)
		switch(mode)
		{
		//--- 8 2-Tuple
		case 0: return new int[][] {
			{0, 8}, {0, 9}, {1, 9}, {2, 4}, {2, 6}, {2, 10}, {3, 11}, {4, 11} 
		};
		//--- 4 Random 4-Tuple
		case 1: return new int[][] {
			{0, 1, 8, 9}, {2, 11, 10, 3}, 
			{11, 5, 4, 8}, {14, 8, 6, 2}
		};
		default: throw new OutOfRangeException(mode, 0, 1);
		}
	}

	@Override
	public String fixedTooltipString() {
		return "<html>"
				+ "1: TODO"
				+ "</html>";
	}

	@Override
	public int[] getAvailFixedNTupleModes() {
		return fixedModes;
	}

	@Override
	public HashSet adjacencySet(int iCell) {
		HashSet adjSet = new HashSet();
		switch(iCell)
		{
		case 0:
			adjSet.add(1);
			adjSet.add(8);
		case 1:
			adjSet.add(0);
			adjSet.add(2);
			adjSet.add(5);
			adjSet.add(9);
		case 2:
			adjSet.add(1);
			adjSet.add(3);
			adjSet.add(6);
			adjSet.add(12);
		case 3:
			adjSet.add(2);
			adjSet.add(4);
			adjSet.add(7);
			adjSet.add(14);
		case 4:
			adjSet.add(3);
			adjSet.add(8);
		case 5:
			adjSet.add(6);
			adjSet.add(1);
		case 6:
			adjSet.add(5);
			adjSet.add(7);
			adjSet.add(11);
			adjSet.add(2);
		case 7:
			adjSet.add(6);
			adjSet.add(8);
			adjSet.add(3);
			adjSet.add(10);
		case 8:
			adjSet.add(7);
			adjSet.add(0);
			adjSet.add(9);
			adjSet.add(4);
		case 9:
			adjSet.add(1);
			adjSet.add(10);
			adjSet.add(8);
			adjSet.add(12);
		case 10:
			adjSet.add(9);
			adjSet.add(11);
			adjSet.add(7);
			adjSet.add(13);
		case 11:
			adjSet.add(10);
			adjSet.add(6);
		case 12:
			adjSet.add(2);
			adjSet.add(13);
			adjSet.add(9);
			adjSet.add(14);
		case 13:
			adjSet.add(10);
			adjSet.add(12);
		case 14:
			adjSet.add(3);
			adjSet.add(12);
		}
		
		return adjSet;
	}

	
}
