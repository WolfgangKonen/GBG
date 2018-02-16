package games.RubiksCube;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import games.StateObservation;
import games.XNTupleFuncs;
import games.RubiksCube.ColorTrafoMap.ColMapType;
import games.RubiksCube.CubeStateMap.CsMapType;

public class XNTupleFuncsCube implements XNTupleFuncs, Serializable {

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
		return 24;
	}
	
	/**
	 * @return the number P of position values 0, 1, 2,..., P-1 that each board cell 
	 * can have. For Rubik's Cube: P=6 colors, with 0:w, 1:b, 2:o, 3:y, 4:g, 5:r) 
	 */
	@Override
	public int getNumPositionValues() {
		return 6; 
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
	public int[] getBoardVector(StateObservation so) {
		assert (so instanceof StateObserverCube);
		int[] bvec = ((StateObserverCube) so).getCubeState().fcol.clone();
		return bvec;   
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
	public int[][] symmetryVectors(int[] boardVector) {
		int i=0;
		boolean doAssert=true;
		int[][] equiv = null;
		CubeState cS1 = new CubeState(boardVector); 
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
		equiv = new int[set.size()][];
		it1 = mapColSymm.entrySet().iterator();
	    while (it1.hasNext()) {
		    Map.Entry entry = (Map.Entry)it1.next();
		    equiv[i++] = ((CubeState)entry.getValue()).fcol.clone();	
        } 

		return equiv;
	}
	
	/** 
	 * Return a fixed set of {@code numTuples} n-tuples suitable for that game. 
	 * Different n-tuples may have different length. An n-tuple {0,1,4} means a 3-tuple 
	 * containing the cells 0, 1, and 4.
	 * 
	 * @param mode one of the values from {@link #getAvailFixedNTupleModes()}
	 * @return nTuples[numTuples][]
	 */
	@Override
	public int[][] fixedNTuples(int mode) {
		// Examples for some NTuples for TicTacToe:
		//best chosen 40, 4-tuples
		int nTuple[][]={{6, 7, 4, 0},{4, 5, 8, 7},{4, 3, 0, 1},{4, 5, 2, 1},{6, 3, 0, 1},
				{6, 3, 0, 4},{0, 1, 5, 2},{2, 1, 4, 7},{7, 3, 4, 5},{0, 4, 1, 2},{8, 4, 0, 1},{7, 4, 1, 0},
				{7, 4, 0, 1},{8, 7, 4, 1},{7, 4, 8, 5},{8, 7, 4, 1},{7, 8, 5, 4},{4, 8, 7, 6},{2, 1, 5, 4},
				{3, 0, 1, 4},{8, 7, 3, 4},{8, 4, 3, 0},{4, 1, 2, 5},{6, 3, 0, 4},{1, 2, 5, 8},{1, 4, 3, 7},
				{6, 3, 0, 1},{8, 5, 4, 3},{3, 4, 7, 6},{5, 8, 7, 6},{5, 4, 0, 1},{6, 3, 4, 7},{0, 3, 4, 8},
				{6, 3, 7, 8},{2, 1, 4, 0},{3, 7, 4, 1},{1, 2, 5, 4},{8, 5, 1, 4},{6, 7, 8, 4},{6, 3, 0, 1}
		};
		// int nTuple[][] = {{0,1,2,3,4,5,6,7,8}};
		// int nTuple[][] = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 4, 8 } };
		// int nTuple[][] = {{0,1,2,3,4,5,6,7}, {8,7,6,5,4,3,2,1},
		// 		{8,7,6,5,4,3,2,0}, {0,1,2,3,4,5,6,8}};
		// int nTuple[][] = {{0,1,2,5,8,7}, {0,3,6,7,8,5}, {0,3,6,1,4,7},
		// 		{2,5,8,7,4,1}, {0,1,2,3,4,5}, {6,7,8,3,4,5}, {0,4,8,7,6,3}};
		// int nTuple[][] =
		// 		{{0,1,2,3},{4,5,6,7},{8,0,3,6},{1,4,7,2},{5,8,0,4},{6,4,2,0}};
		// int nTuple[][] = {{0,1,2},{3,4,5},{6,7,8},{0,4,8},{0,1,2,5,8,7},
		//	 	{0,3,6,7,8,5}, {0,3,6,1,4,7}, {2,5,8,7,4,1}, {0,1,2,3,4,5},
		//	 	{6,7,8,3,4,5}, {0,4,8,7,6,3}};
		
		return nTuple;		
	}

    private static int[] fixedModes = {1};
    
	public int[] getAvailFixedNTupleModes() {
		return fixedModes;
	}


	/**
	 * Return all neighbors of {@code iCell}. See {@link CubeState}  
	 * for board coding. 4-point-neighborhoods on the cube with wrap-around
	 * 
	 * @param iCell
	 * @return a set of all cells adjacent to {@code iCell} (referring to the coding in 
	 * 		a board vector) 
	 */
	public HashSet adjacencySet(int iCell) {
		int[][] aList = {						// 4-point neighborhoods
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
				};
		
		// just a one-time debug test, if we wrote up aList correctly:
//		int[] counter = new int[24];
//		for (int i=0; i<24; i++) 
//			for (int j=0; j<4; j++) 
//				counter[aList[i][j]]++;
//		for (int i=0; i<24; i++) assert(counter[i]==4) : "Oops, counter["+i+"] is not 4";
		
		HashSet adjSet = new HashSet();
		for (int i=0; i<4; i++) adjSet.add(aList[iCell][i]);
		
		return adjSet;
	}

}
