package games.Sim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;
import tools.Types;

public class XNTupleFuncsSim extends XNTupleBase implements XNTupleFuncs, Serializable {

	List<int[]> list = new ArrayList<int[]>();
	AllPermutation perm;
	private static int[] fixedModes = {1};
    /**
	 * 
	 */
	private static final long serialVersionUID = 5296353414404403319L;

	/**
	 * 
	 */

	public XNTupleFuncsSim() 
    {
		int [] nodes = {1,2,3,4,5,6};
		
		perm = new AllPermutation(nodes);
		
		list.add(perm.GetFirst());
		
		while (perm.HasNext()) 
	       { 
			 list.add(perm.GetNext());
	            
	       } 
		
    }
	
	@Override
	public int getNumCells() 
	{
		return 15;
	}

	@Override
	public int getNumPositionValues() 
	{
		return 4;
	}

	@Override
	public int getNumPlayers() 
	{
		return 3;
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

	//not tested yet
	@Override
	public int[][] symmetryVectors(int[] boardVector) {
		int [][] symmetricVectors = new int [list.size()][];
		
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
	public int[] symmetryActions(int actionKey) {
		// TODO 
		 throw new RuntimeException("symmetricActions in XNTuple.java noch nicht implementiert");
	}

	@Override
	public int[][] fixedNTuples(int mode) {
		int nTuple[][] = {{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14}};
		return nTuple;
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
		// TODO Auto-generated method stub
		return null;
	}

	
}
