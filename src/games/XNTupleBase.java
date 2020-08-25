package games;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import agentIO.LoadSaveGBG;
import games.Othello.ConfigOthello;
import games.Othello.StateObserverOthello;
import tools.ValidateAgentTest;

/**
 *  This class just provides a default implementation for methods {@link #instantiateAfterLoading()} and
 *  {@link #makeBoardVectorEachCellDifferent()} from the interface {@link XNTupleFuncs}. All other methods 
 *  are left as abstract methods.
 */
abstract public class XNTupleBase implements Serializable, XNTupleFuncs {

    /**
     * Provide a version UID here. Change the version UID for serialization only if a newer version is no 
     * longer compatible with an older one (older .gamelog or .agt.zip containing this object will
     * become unreadable)  
     */
    private static final long serialVersionUID = 42L;

    public XNTupleBase() {	}

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
		return true; 
	}
	
	/**
	 * @return a board vector where each cell has a different {@code int}. Here we do it 
	 * the generic way: The returned board vector has values 0,1,...,{@code nc}-1, where
	 * {@code nc = }{@link #getNumCells()} 
	 * 
	 * @see ValidateAgent
	 */
	public BoardVector makeBoardVectorEachCellDifferent() {
		int nc = this.getNumCells();
		int[] boardVec = new int[nc];
		for(int i = 0;  i < nc; i++) {
			boardVec[i] = i;
		}
		return new BoardVector(boardVec);		
	}
	
	@Override
	abstract public int getNumCells();

	@Override
	abstract public int getNumPositionValues();

	@Override
	abstract public int getNumPlayers();

	@Override
	abstract public int getNumSymmetries();

	@Override
	abstract public BoardVector getBoardVector(StateObservation so);

	@Override
	public BoardVector[] symmetryVectors(StateObsWithBoardVector curSOWB, int n) {
		return symmetryVectors(curSOWB.getBoardVector(),n);
	}
	
	@Override
	abstract public BoardVector[] symmetryVectors(BoardVector boardVector, int n);

	@Override
	abstract public int[] symmetryActions(int actionKey);

	@Override
	abstract public int[][] fixedNTuples(int mode);

	@Override
	abstract public String fixedTooltipString();

	@Override
	abstract public int[] fixedNTupleModesAvailable();

	@Override
	abstract public HashSet adjacencySet(int iCell);

}
