package games.Nim;

import java.io.Serializable;
import java.util.HashSet;

import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;
import controllers.TD.ntuple2.NTupleFactory;

public class XNTupleFuncsNim3P extends XNTupleBase implements XNTupleFuncs, Serializable {

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .gamelog or .agt.zip containing this object will
     * become unreadable or you have to provide a special version transformation)
    */
    private static final long serialVersionUID = 12L;
    
    private int[] actionVector;
    private int[][] actionArray;

    public XNTupleFuncsNim3P() {
    	// calculate actionArray[][]: for a given action with key j, the element
    	// actionArray[i][j] holds the equivalent action when the state is transformed to 
    	// equiv[i] = symmetryVectors(int[] boardVector)[i]
       	actionVector = new int[NimConfig.NUMBER_HEAPS*NimConfig.MAX_MINUS];
    	for (int i=0; i<actionVector.length; i++)
    		actionVector[i] = i;
    	actionArray = new int[1][];
    	actionArray[0] = actionVector.clone();
    }
    
	/**
	 * @return the number of board cells = number of heaps
	 */
	@Override
	public int getNumCells() {
		return NimConfig.NUMBER_HEAPS;
	}
	
	/**
	 * @return the number P of position values 0, 1, 2,..., P-1 that each board cell 
	 * can have. For Nim: P=HEAP_SIZE+1, coding 0,1,2,...,HEAP_SIZE items on the heap 
	 */
	@Override
	public int getNumPositionValues() {
		return NimConfig.HEAP_SIZE+1; 
	}
	
	/**
	 * @return the number of players in this game 
	 */
	@Override
	public int getNumPlayers() {
		return 3;
	}
	
	/**
	 * @return the maximum number of symmetries in this game
	 */
	public int getNumSymmetries() {
		return 1;
	}
	
	/**
	 * The board vector is an {@code int[]} vector where each entry corresponds to one 
	 * heap.
	 * 
	 * @return a vector of length {@link #getNumCells()}, holding for each board cell its 
	 * position value (0,1,2,...,HEAP_SIZE items on the heap).
	 */
	@Override
	public BoardVector getBoardVector(StateObservation so) {
		assert (so instanceof StateObserverNim3P);

		return new BoardVector(((StateObserverNim3P) so).getHeaps());   
	}
	
	/**
	 * Given a board vector from {@link #getBoardVector(StateObservation)} and given that the 
	 * game has s symmetries, return an array which holds s symmetric board vectors: <ul>
	 * <li> the first row {@code boardArray[0]} is the board vector itself
	 * <li> the other rows are the board vectors when transforming {@code boardVector}
	 * 		according to the s-1 other symmetries (e. g. rotation, reflection, if applicable).
	 * </ul>
	 * In the case of Nim there are no symmetries.<br>
	 * (There are for N heaps the N-permutation symmetries, but it is too costly to code them as 
	 * equivalent states. It is better to sort always the heaps in decreasing order. That is, 
	 * when a move is made, we check the modified heap, if it is in the right position. If not, 
	 * we exchange it with a heap to the right.)
	 * 
	 * @param boardVector
	 * @return boardArray
	 */
	@Override
	public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
		int i;
		BoardVector[] equiv = null;
		equiv = new BoardVector[1];
		equiv[0] = new BoardVector(boardVector.bvec);
		
		return equiv;
	}
	
	/**
	 * Given a certain board array of symmetric (equivalent) states for state <b>{@code so}</b> 
	 * and a certain action to be taken in <b>{@code so}</b>, generate the array of equivalent 
	 * action keys {@code equivAction} for the symmetric states.
	 * <p>
	 * This method is needed only for Q-learning and Sarsa.
	 * 
	 * @param actionKey
	 * 				the key of the action to be taken in <b>{@code so}</b> 
	 * @return <b>equivAction</b>
	 * 				array of the equivalent actions' keys. 
	 * <p>
	 * equivAction[i] is the key of the action equivalent to actionKey in the
	 * i'th equivalent board vector equiv[i] = {@link #symmetryVectors(int[])}[i]
	 */
	public int[] symmetryActions(int actionKey) {
		int numEquiv = 1; //actionArray.length;   
		int[] equivAction = new int[numEquiv];
		for (int i = 0; i < numEquiv; i++) {
			equivAction[i] = actionArray[i][actionKey];
		}

		return equivAction;
	}
	
	/** 
	 * Return a fixed set of {@code numTuples} n-tuples suitable for that game. 
	 * Different n-tuples may have different length. An n-tuple {0,1,4} means a 3-tuple 
	 * containing the cells 0, 1, and 4.
	 * 
	 * @param mode one of the values from {@link #fixedNTupleModesAvailable()} <br>
	 * 			   1: one n-tuple with length {@link NimConfig#NUMBER_HEAPS}
	 * @return nTuples[numTuples][], where {@code nTuples[i]} describes the {@code i}'th 
	 * n-tuple as an int-array, with {@code i=0,...,numTuples-1}
	 */
	@Override
	public int[][] fixedNTuples(int mode) {
		int nTuple[][]=new int[mode][NimConfig.NUMBER_HEAPS];	
		
		for (int i=0; i<nTuple[0].length; i++) nTuple[0][i] = i;
								// i.e. one n-tuple {0,1,...,NUMBER_HEAPS-1} (covers all heaps)
		if (mode==2)
			for (int i=0; i<nTuple[0].length; i++) nTuple[1][i] = i;
		return nTuple;				
	}

	@Override
	public String fixedTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>"
				+ "1: one n-tuple with length NUMBER_HEAPS"
				+ "2: two n-tuple with length NUMBER_HEAPS"
				+ "</html>";
	}

	/**
	 * 1: one n-tuple with length {@link NimConfig#NUMBER_HEAPS}
	 */
    private static int[] fixedModes = {1,2};		
    
	public int[] fixedNTupleModesAvailable() {
		return fixedModes;
	}


	/**
	 * Return all neighbors of {@code iCell}. See {@link #getBoardVector(StateObservation)} 
	 * for board coding. Needed for random-walk n-tuple generation.
	 * <p>
	 * Here each cell (each heap) is adjacent to all other cells (heaps).
	 * 
	 * @param iCell
	 * @return a set of all cells adjacent to {@code iCell} (referring to the coding in 
	 * 		a board vector) 
	 * 
	 * @see NTupleFactory#generateRandomWalkNTuples(int,int,int,XNTupleFuncs)
	 */
	public HashSet adjacencySet(int iCell) {
		HashSet adjSet = new HashSet();
		for (int i=0; i<NimConfig.NUMBER_HEAPS; i++) 
			if (i!=iCell) adjSet.add(i);
		
		return adjSet;
	}


}
