package games.RubiksCube;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;
import games.RubiksCube.ColorTrafoMap.ColMapType;
import games.RubiksCube.CubeConfig.BoardVecType;
import games.RubiksCube.CubeStateMap.CsMapType;

public class XNTupleFuncsCube extends XNTupleBase implements XNTupleFuncs, Serializable {

	private CubeStateMap hmRots = new CubeStateMap(CsMapType.AllWholeCubeRotTrafos);
	private ColorTrafoMap hmCols = new ColorTrafoMap(ColMapType.AllColorTrafos);
	
    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .gamelog or .agt.zip containing this object will
     * become unreadable or you have to provide a special version transformation)
     */
    private static final long serialVersionUID = 12L;

    //
	// The following five functions are only needed for the n-tuple interface:
	//
	/**
	 * @return the number of board cells
	 */
	@Override
	public int getNumCells() {
		switch(CubeConfig.boardVecType) {
		case CUBESTATE: 
			return 24;
		case CUBEPLUSACTION:
			return 26;
		case STICKERS: 
			return 49;
		default: 
			throw new RuntimeException("Unallowed value in switch boardVecType");
		}
	}
	
	/**
	 * @return the number P of position values 0, 1, 2,..., P-1 that each board cell 
	 * can have. For Rubik's Cube: P=6 colors, with 0:w, 1:b, 2:o, 3:y, 4:g, 5:r) 
	 */
	@Override
	public int getNumPositionValues() {
		switch(CubeConfig.boardVecType) {
		case CUBESTATE: 
		case CUBEPLUSACTION:
			return 6; 
		case STICKERS: 
			return 4;
		default: 
			throw new RuntimeException("Unallowed value in switch boardVecType");
		}
	}
	
	/**
	 * @return the number of players in this game 
	 */
	@Override
	public int getNumPlayers() {
		return 1;
	}
	
	/**
	 * The board vector is an {@code int[]} vector where each entry corresponds to one 
	 * cell of the board. See {@link CubeState} for the concrete mapping
	 * 
	 * @return a vector of length {@link #getNumCells()}, holding for each board cell its 
	 * position value with 0:w, 1:b, 2:o, 3:y, 4:g, 5:r.
	 */
	@Override
	public BoardVector getBoardVector(StateObservation so) {
		assert (so instanceof StateObserverCube);
		return ((StateObserverCube) so).getCubeState().getBoardVector();
	}
	
	/**
	 * Given a board vector from {@link #getBoardVector(StateObservation)} and given that the 
	 * game has s symmetries, return an array which holds s symmetric board vectors: <ul>
	 * <li> the first row {@code boardArray[0]} is the board vector itself
	 * <li> the other rows are the board vectors when transforming {@code boardVector}
	 * 		according to the s-1 other symmetries.
	 * </ul>
	 * In the case of the cube we have s=24 color symmetries (6 places 1st color * 4 places 2nd color)
	 * 
	 * @param boardVector
	 * @return boardArray
	 */
	@Override
	public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
		int i=0;
		boolean doAssert=true;
		BoardVector[] equiv = null;
		CubeState cS1 = new CubeState(boardVector); // problem: not every board vector can be transformed to CubeState (STICKERS)
		HashSet set = new HashSet();
		//
		// calculate all color-symmetric states for cS1, collect
		// in 'set' only the truly different CubeStates  
		CubeStateMap mapColSymm = hmCols.applyColSymm(cS1,hmRots);
		if (doAssert) assert(mapColSymm.countYgrHomeStates()==mapColSymm.size()) : 
			"not all color-symmetric states have ygr 'home'!";
		Iterator it1 = mapColSymm.entrySet().iterator(); 
	    while (it1.hasNext()) {
		    Map.Entry entry = (Map.Entry)it1.next();
		    set.add((CubeState)entry.getValue());	
        } 	
	    
	    // once we have the truly different CubeStates in 'set', 
	    // create and fill 'equiv' accordingly:
		equiv = new BoardVector[set.size()];
		it1 = set.iterator();
	    while (it1.hasNext()) {
		    CubeState cs  = (CubeState)it1.next();
		    equiv[i++] = cs.getBoardVector();	
        } 

		return equiv;
	}
	
	/**
	 * Given a certain board array of symmetric (equivalent) states for state <b>{@code so}</b> 
	 * and a certain action to be taken in <b>{@code so}</b>, 
	 * generate the array of equivalent action keys {@code equivAction} for the symmetric states.
	 * <p>
	 * This method is needed for Q-learning and Sarsa.
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
		/* TODO */
		return null;
	}
	
	/** 
	 * Return a fixed set of {@code numTuples} n-tuples suitable for that game. 
	 * Different n-tuples may have different length. An n-tuple {0,1,4} means a 3-tuple 
	 * containing the cells 0, 1, and 4.
	 * 
	 * @param mode one of the values from {@link #fixedNTupleModesAvailable()}
	 * @return nTuples[numTuples][]
	 */
	@Override
	public int[][] fixedNTuples(int mode) {
		// Examples for some n-tuples for Rubik's PocketCube:
		switch (mode) {
		case 1: 
			switch(CubeConfig.boardVecType) {
			case CUBESTATE: 
				// the 4 "ring" 8-tuples:
				return new int[][]{ {15,14,9,8,0,3,22,21} ,{12,13,10,11,1,2,23,20},
									{5,4,8,11,18,17,23,22},{ 6,7,9,10,19,16,20,21} };
			case CUBEPLUSACTION:
				// the 4 "ring" 8-tuples + the 2 lastAction-cells:
				return new int[][]{ {15,14,9,8,0,3,22,21,24,25} ,{12,13,10,11,1,2,23,20,24,25},
									{5,4,8,11,18,17,23,22,24,25},{ 6,7,9,10,19,16,20,21,24,25} };
			}
		}
		throw new RuntimeException("Unsupported value mode="+mode+" in XNTupleFuncs::fixedNTuples(int)");
	}

	@Override
	public String fixedTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>"
				+ "1: TODO"
				+ "</html>";
	}

    private static int[] fixedModes = {1};
    
	public int[] fixedNTupleModesAvailable() {
		return fixedModes;
	}


	/**
	 * Return all neighbors of cell {@code iCell} in the board vector. 
	 */
	public HashSet adjacencySet(int iCell) {
		switch (CubeConfig.cubeType) {
		case POCKET: return adjacencySet2x2(iCell);
		case RUBIKS: throw new RuntimeException("Not yet done!");
		default: throw new RuntimeException("CubeConfig.cubeType = "+CubeConfig.cubeType+" not known!");
		}
	}
	
	/**
	 * Return all neighbors of {@code iCell} for the 2x2x2 pocket cube. 
	 * <p>
	 * If {@link CubeConfig#boardVecType}{@code  =CUBESTATE, CUBEPLUSACTION}:  See {@link CubeState}  
	 * for board coding. 4-point-neighborhoods on the cube with wrap-around.
	 * <p>
	 * If {@link CubeConfig#boardVecType}{@code  =STICKERS}: Adjacency set on the 7x7 stickers board: 
	 * All cells from the next row, except the cell in the same column as {@code iCell}, are in the adjacency set. 
	 * See {@link CubeState#getBoardVector()} for the stickers board coding.
	 * 
	 * @param iCell	the board cell for which we want the set of adjacent cells
	 * @return a set of all cells adjacent to {@code iCell} (referring to the coding in 
	 * 		a board vector) 
	 */
	public HashSet adjacencySet2x2(int iCell) {
		int i,j;
		HashSet adjSet = new HashSet();
		switch (CubeConfig.boardVecType) {
		case CUBESTATE:
		case CUBEPLUSACTION:
			final int[][] aList = {						// 4-point neighborhoods
					{1,3,4,8},		//0
					{0,2,18,11},		
					{1,3,23,17},
					{0,2,22,5},
					{0,5,7,8},		//4
					{3,4,6,22},
					{5,7,15,21},
					{4,6,9,14},
					{0,4,9,11},		//8
					{7,8,10,14},
					{9,11,13,19},
					{1,8,10,18},
					{13,15,16,20},	//12
					{10,14,12,19},
					{7,9,13,15},
					{6,14,12,21},
					{12,17,19,20},	//16
					{2,18,16,23},
					{1,11,19,17},
					{10,13,16,18},
					{12,16,23,21},	//20
					{6,15,20,22},
					{3,5,21,23},
					{2,17,20,22},
//					{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23},	// OLD, is inferior
//					{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23}		//
					{0,1,2,3,4,5},
					{0,1,2,3,4,5}
					};
			// just a one-time debug test, if we wrote up aList correctly:
//			int[] counter = new int[24];
//			for (i=0; i<24; i++) 
//				for (j=0; j<4; j++) 
//					counter[aList[i][j]]++;
//			for (i=0; i<24; i++) assert(counter[i]==4) : "Oops, counter["+i+"] is not 4";
			
			for (i=0; i<4; i++) adjSet.add(aList[iCell][i]);
			
			// If the board vector includes lastAction, we follow the model that the first 6 fcol-cells have
			// cell 24 + 25 (coding lastAction) as neighbors:
//			if (CubeConfig.boardVecType==BoardVecType.CUBEPLUSACTION) {	// OLD: all cells, is inferior
			if (CubeConfig.boardVecType==BoardVecType.CUBEPLUSACTION && iCell<6) {
				adjSet.add(24);
				adjSet.add(25);
			}
			break;
		case STICKERS:
			j = iCell % 7;	// column index
			i = iCell - j;	// row index
			boolean ROWWISE = true;
			if (ROWWISE) {
				int newi = (i+1)%7;	// next row (cyclically)
				for (int k=0; k<7; k++) {
					if (k!=j) adjSet.add(newi*7+k);	// add a cell from next row being NOT in column j
				}				
			} else {
				int newj = (i+1)%7;	// next column (cyclically)
				for (int k=0; k<7; k++) {
					if (k!=i) adjSet.add(k*7+newj);	// add a cell from next column being NOT in row i
				}
			}
		} // switch
				
		return adjSet;
	}

}
