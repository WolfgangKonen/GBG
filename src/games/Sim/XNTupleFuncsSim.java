package games.Sim;

import java.util.HashSet;

import games.StateObservation;
import games.XNTupleFuncs;

public class XNTupleFuncsSim implements XNTupleFuncs {

	
    public XNTupleFuncsSim() 
    {
    
    }
	
	@Override
	public int getNumCells() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumPositionValues() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumPlayers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getBoardVector(StateObservation so) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[][] symmetryVectors(int[] boardVector) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] symmetryActions(int actionKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[][] fixedNTuples(int mode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String fixedTooltipString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getAvailFixedNTupleModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashSet adjacencySet(int iCell) {
		// TODO Auto-generated method stub
		return null;
	}

}
