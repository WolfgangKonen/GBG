package games.RubiksCube;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import controllers.TD.ntuple4.Sarsa4Agt;
import controllers.TD.ntuple4.TDNTuple4Agt;
import games.BoardVector;
import games.StateObsWithBoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;
import games.RubiksCube.CubeConfig.BoardVecType;

/**
 * Implementation of methods needed for the n-tuple interface {@link XNTupleFuncs}
 * for the game RubiksCube
 */
public class XNTupleFuncsCube extends XNTupleBase implements XNTupleFuncs, Serializable {

	// --- deprecated, we use CubeStateMap.allWholeCubeRots ---
//	private final CubeStateMap hmRots = new CubeStateMap(CsMapType.AllWholeCubeRotTrafos);
//	private final ColorTrafoMap hmCols = new ColorTrafoMap(ColMapType.AllColorTrafos);

	/**
	 * change the version ID for serialization only if a newer version is no longer
	 * compatible with an older one (older .gamelog or .agt.zip containing this object will
	 * become unreadable or you have to provide a special version transformation)
	 */
	@Serial
	private static final long serialVersionUID = 12L;

	//
	// the generic methods (valid for both cases 2x2x2 and 3x3x3)
	//

	/**
	 * @return the number of board cells
	 */
	@Override
	public int getNumCells() {
		return switch (CubeConfig.cubeSize) {
			case POCKET -> this.getNumCells2x2();
			case RUBIKS -> this.getNumCells3x3();
		};
	}

	/**
	 * @return the number P of position values 0, 1, 2,..., P-1 that each board cell
	 * can have. If different cells have different P, return max(P).
	 * <p>
	 * 		Example: If {@link CubeConfig#boardVecType}{@code = CUBESTATE}: <br>
	 * 		P=6 colors, with 0:w, 1:b, 2:o, 3:y, 4:g, 5:r
	 * </p>
	 */
	@Override
	public int getNumPositionValues() {
		return switch (CubeConfig.cubeSize) {
			case POCKET -> this.getNumPositionValues2x2();
			case RUBIKS -> this.getNumPositionValues3x3();
		};
	}

	/**
	 * [This method is only needed for agents {@link TDNTuple4Agt} and {@link Sarsa4Agt}.]
	 *
	 * @return 	a vector of length {@link #getNumCells()} that has in its {@code i}th element the number
	 * 			P of position values 0, 1, 2,..., P-1 that board cell {@code i} can have
	 */
	@Override
	public int[] getPositionValuesVector(){
		return switch (CubeConfig.cubeSize) {
			case POCKET -> this.getPositionValuesVector2x2();
			case RUBIKS -> this.getPositionValuesVector3x3();
		};
	}

	/**
	 * @return the number of players in this game
	 */
	@Override
	public int getNumPlayers() {
		return 1;
	}

	/**
	 * @return the maximum number of symmetries in this game
	 */
	public int getNumSymmetries() {
		return 24;
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
	 * game has s symmetries, return an array which holds n &le; s symmetric board vectors: <ul>
	 * <li> the first row {@code boardArray[0]} is the board vector itself
	 * <li> the other rows are the board vectors when transforming {@code boardVector}
	 * 		according to n-1 other symmetries.
	 * </ul>
	 * In the case of the cube we have at most s=24 color symmetries (6 places 1st color * 4 places 2nd color).
	 * If n &lt; 24, we select id plus (n-1) out of the 23 non-id trafos, randomly selected.
	 *
	 * @param curSOWB a state with its board vector
	 * @param n number of symmetry vectors to return (n=0 meaning 'all')
	 * @return a vector {@code equiv} of board vectors
	 */
	@Override
	public BoardVector[] symmetryVectors(StateObsWithBoardVector curSOWB, int n) {
		StateObserverCube so = (StateObserverCube) curSOWB.getStateObservation();
		CubeState cS1 = so.getCubeState();
		boolean doAssert=false;		// if true, do assertion checks on color trafo (slower!)
		BoardVector[] equiv;

		//
		// select a set of n ColorTrafo objects (or all, if n==0)
		//
		ColorTrafoMap allCT = ColorTrafoMap.allCT;
		ColorTrafoMap myMap = new ColorTrafoMap();
		assert (n>=0);
		if (n>0) {
			myMap.put(0, allCT.get(0));			// put in any case the identity trafo (yielding the state itself)
			if (n>1) {
				int nmax = (n <= allCT.size()) ? n-1 : (allCT.size()-1);
				List<Integer> list = new ArrayList<Integer>();
				for (int k = 1; k < allCT.size(); k++) list.add(k);
				java.util.Collections.shuffle(list);
				for (int k = 0; k < nmax; k++)  myMap.put(list.get(k), allCT.get(list.get(k)));
			}									// the last line picks nmax random color trafos NOT being 'id'
		} else {
			myMap = allCT;						// take all color trafos, if n==0
		}

		CubeStateMap mapColSymm = cS1.applyCT(myMap,doAssert);

// DEPRECATED, use cS1.applyCT	instead
//		//
//		// calculate all color-symmetric states for cS1, collect
//		// in 'set' only the truly different CubeStates
//		CubeStateMap mapColSymm = hmCols.applyColSymm(cS1,hmRots);
//		HashSet<CubeState> set = new HashSet<>();
//		if (doAssert && CubeConfig.cubeSize == CubeConfig.CubeSize.POCKET)
//			assert(mapColSymm.countYgrHomeStates()==mapColSymm.size()) :
//				"not all color-symmetric states have ygr 'home'!";
//		for (Map.Entry<Integer, CubeState> entry : mapColSymm.entrySet()) {
//			set.add(entry.getValue());
//		}
//
//		// once we have the truly different CubeStates in 'set',
//		// create and fill 'equiv' accordingly:
//		equiv = new BoardVector[set.size()];
//		for (CubeState cs : set) {
//			equiv[i++] = cs.getBoardVector();
//		}

		// fill 'equiv'  with all states found in mapColSymm:
		// (we take here all and not only the distinct ones, because empirically there are in many cases 24 distinct
		// states for a cube states being more than 5 twists away from the solved cube. See DistinctColorTrafos in
		// test/games/RubiksCube for the precise numbers
		equiv = new BoardVector[mapColSymm.size()];
		int i=0;
		for (Map.Entry<Integer, CubeState> entry : mapColSymm.entrySet()) {
			CubeState cs = entry.getValue();
			equiv[i++] = cs.getBoardVector();
		}

		return equiv;
	}

	/**
	 * Given a board vector from {@link #getBoardVector(StateObservation)} and given that the
	 * game has s symmetries, return an array which holds n &le; s symmetric board vectors: <ul>
	 * <li> the first row {@code boardArray[0]} is the board vector itself
	 * <li> the other rows are the board vectors when transforming {@code boardVector}
	 * 		according to the s-1 other symmetries.
	 * </ul>
	 * In the case of the cube we have at most s=24 color symmetries (6 places 1st color * 4 places 2nd color)
	 *
	 * @param boardVector the board vector
	 * @param n number of symmetry vectors to return (n=0 meaning 'all')
	 * @return boardArray
	 */
	@Override
	@Deprecated
	public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
		throw new RuntimeException("Not implemented for XNTupleFuncsCube");
// OLD and DEPRECATED:
//		CubeStateFactory csFactory = new CubeStateFactory();
//		int i=0;
//		boolean doAssert=true;
//		BoardVector[] equiv;
//		CubeState cS1 = csFactory.makeCubeState(boardVector);
//		HashSet<CubeState> set = new HashSet<>();
//		//
//		// calculate all color-symmetric states for cS1, collect
//		// in 'set' only the truly different CubeStates
//		CubeStateMap mapColSymm = hmCols.applyColSymm(cS1,hmRots);
//		if (doAssert && CubeConfig.cubeSize == CubeConfig.CubeSize.POCKET)
//			assert(mapColSymm.countYgrHomeStates()==mapColSymm.size()) :
//				"not all color-symmetric states have ygr 'home'!";
//		for (Map.Entry<Integer, CubeState> entry : mapColSymm.entrySet()) {
//			set.add(entry.getValue());
//		}
//
//		// once we have the truly different CubeStates in 'set',
//		// create and fill 'equiv' accordingly:
//		equiv = new BoardVector[set.size()];
//		for (CubeState cs : set) {
//			equiv[i++] = cs.getBoardVector();
//		}
//
//		return equiv;
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
	 * i'th equivalent board vector equiv[i] = {@link #symmetryVectors(StateObsWithBoardVector, int)}[i]
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
		return switch (CubeConfig.cubeSize) {
			case POCKET -> this.fixedNTuples2x2(mode);
			case RUBIKS -> this.fixedNTuples3x3(mode);
		};
	}

	@Override
	public String fixedTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>"
				+ "0: 4 'ring' 8-tuples<br>"
				+ "1: 30 7-tuples<br>"
				+ "2: 60 7-tuples"
				+ "</html>";
	}

	private final static int[] fixedModes = {0,1,2};

	public int[] fixedNTupleModesAvailable() {
		return fixedModes;
	}

	/**
	 * Return all neighbors of cell {@code iCell} in the board vector.
	 */
	public HashSet<Integer> adjacencySet(int iCell) {
		return switch (CubeConfig.cubeSize) {
			case POCKET -> adjacencySet2x2(iCell);
			case RUBIKS -> adjacencySet3x3(iCell);
		};
	}


	//
	// methods specific for 2x2x2 Pocket Cube
	//

	/**
	 * @return the number of board cells
	 */
	private int getNumCells2x2() {
		return switch (CubeConfig.boardVecType) {
			case CUBESTATE -> 24;
			case CUBEPLUSACTION -> 26;
			case STICKER -> 49;
			case STICKER2 -> 14;
		};
	}

	/**
	 * @return the maximum number P of position values 0, 1, 2,..., P-1 that a board cell
	 * can have.
	 */
	private int getNumPositionValues2x2() {
		return switch (CubeConfig.boardVecType) {
			case CUBESTATE, CUBEPLUSACTION -> 6;
			case STICKER -> 4;
			case STICKER2 -> 7;
		};
	}

	/**
	 * [This method is only needed for agents {@link TDNTuple4Agt} and {@link Sarsa4Agt}.]
	 *
	 * @return 	a vector of length {@link #getNumCells()} that has in its {@code i}th element the number
	 * 			P of position values 0, 1, 2,..., P-1 that board cell {@code i} can have
	 */
	public int[] getPositionValuesVector2x2() {
		int[] posValVec = new int[getNumCells()];
		for (int i=0; i<posValVec.length; i++) posValVec[i] = getNumPositionValues();

		if (CubeConfig.boardVecType==BoardVecType.STICKER2) {
			// the max is P=7, but the 2nd half of cells have only P = 3
			for (int i=posValVec.length/2; i<posValVec.length; i++) posValVec[i] = 3;
		}
		return posValVec;
	}

	private int[][] fixedNTuples2x2(int mode) {
		// Examples for some n-tuples in case CubeConfig.cubeSize==POCKET:
		switch (mode) {
			case 0:
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
			case 1:
				// 30 random, but fixed 7-tuples from theNtuple.txt
				return new int[][] {
						{17, 2, 20, 21, 22, 23, 12},
						{0, 3, 4, 5, 6, 22, 7},
						{16, 19, 20, 10, 12, 13, 14},
						{0, 1, 2, 3, 5, 22, 23},
						{0, 1, 17, 18, 3, 8, 9},
						{16, 17, 1, 18, 19, 8, 11},
						{1, 17, 18, 19, 10, 11, 13},
						{0, 21, 6, 7, 8, 9, 15},
						{3, 4, 20, 21, 5, 22, 6},
						{0, 2, 3, 5, 21, 22, 23},
						{0, 3, 4, 6, 7, 8, 9},
						{16, 19, 6, 12, 13, 14, 15},
						{16, 18, 19, 9, 10, 11, 13},
						{4, 6, 7, 8, 9, 14, 15},
						{16, 6, 7, 9, 10, 12, 15},
						{16, 17, 19, 12, 13, 14, 15},
						{19, 7, 10, 12, 13, 14, 15},
						{4, 5, 6, 22, 7, 8, 9},
						{0, 4, 21, 5, 6, 22, 8},
						{16, 17, 2, 18, 19, 3, 23},
						{16, 17, 2, 3, 21, 22, 23},
						{0, 3, 21, 5, 22, 23, 8},
						{4, 21, 6, 22, 7, 9, 15},
						{20, 21, 10, 12, 13, 14, 15},
						{4, 7, 8, 9, 13, 14, 15},
						{18, 19, 9, 10, 11, 13, 14},
						{16, 17, 1, 2, 18, 3, 23},
						{16, 19, 20, 10, 12, 13, 14},
						{2, 20, 5, 22, 6, 23, 12},
						{16, 17, 19, 20, 23, 12, 13}
				};
			case 2:
				// 60 random, but fixed 7-tuples from theNtuple.txt
				return new int[][] {
						{0, 3, 4, 5, 22, 23, 8},
						{0, 4, 5, 7, 8, 9, 11},
						{16, 21, 5, 22, 6, 12, 15},
						{0, 4, 20, 21, 5, 22, 6},
						{16, 1, 18, 19, 9, 10, 11},
						{19, 20, 7, 10, 12, 13, 14},
						{2, 3, 20, 5, 22, 6, 23},
						{21, 5, 6, 7, 12, 14, 15},
						{4, 7, 9, 12, 13, 14, 15},
						{16, 17, 19, 20, 21, 23, 12},
						{16, 17, 1, 2, 18, 23, 12},
						{16, 17, 18, 19, 23, 10, 12},
						{16, 17, 18, 19, 23, 12, 13},
						{3, 4, 21, 5, 6, 22, 15},
						{19, 21, 6, 12, 13, 14, 15},
						{16, 1, 17, 2, 18, 19, 11},
						{0, 1, 17, 2, 18, 4, 8},
						{3, 21, 5, 22, 6, 23, 7},
						{3, 21, 5, 6, 22, 7, 15},
						{4, 20, 21, 6, 7, 9, 15},
						{2, 3, 20, 21, 22, 23, 12},
						{6, 7, 8, 9, 13, 14, 15},
						{16, 20, 21, 22, 23, 12, 15},
						{17, 2, 18, 3, 20, 5, 23},
						{1, 18, 19, 4, 7, 8, 11},
						{1, 17, 2, 20, 21, 22, 23},
						{16, 1, 17, 18, 19, 10, 11},
						{0, 1, 4, 7, 8, 11, 14},
						{17, 1, 2, 20, 23, 11, 12},
						{16, 1, 17, 2, 18, 19, 11},
						{17, 2, 18, 3, 20, 22, 23},
						{1, 18, 2, 8, 9, 10, 11},
						{20, 21, 5, 6, 7, 14, 15},
						{4, 20, 5, 21, 6, 22, 7},
						{1, 19, 9, 10, 11, 14, 15},
						{16, 19, 20, 21, 12, 13, 15},
						{16, 17, 18, 19, 20, 10, 12},
						{2, 20, 21, 6, 22, 23, 15},
						{0, 4, 21, 6, 7, 14, 15},
						{20, 21, 5, 6, 7, 12, 15},
						{0, 18, 7, 8, 9, 11, 14},
						{3, 4, 5, 6, 12, 14, 15},
						{0, 4, 7, 8, 9, 10, 11},
						{0, 7, 8, 9, 10, 14, 15},
						{4, 7, 8, 9, 10, 11, 14},
						{7, 9, 10, 12, 13, 14, 15},
						{16, 17, 18, 20, 23, 12, 15},
						{16, 6, 9, 12, 13, 14, 15},
						{0, 1, 18, 2, 3, 8, 11},
						{0, 3, 4, 5, 6, 22, 23},
						{0, 4, 5, 8, 9, 10, 11},
						{16, 17, 2, 19, 20, 23, 12},
						{4, 5, 6, 22, 7, 14, 15},
						{3, 4, 5, 7, 8, 9, 14},
						{0, 1, 18, 3, 4, 5, 8},
						{16, 17, 2, 18, 20, 22, 23},
						{16, 17, 18, 19, 10, 11, 13},
						{16, 17, 9, 10, 12, 13, 14},
						{16, 1, 17, 2, 18, 23, 11},
						{16, 19, 8, 9, 10, 11, 13}
				};

		}
		throw new RuntimeException("[XNTupleFuncsCube::fixedNTuples] Unsupported value mode="+mode);
	}

	/**
	 * Return all neighbors (adjacent cells) of {@code iCell} for the 2x2x2 pocket cube.
	 * <p>
	 * This is, depending on {@link CubeConfig#boardVecType}:
	 * <ul>
	 *     <li><strong>{@code CUBESTATE, CUBEPLUSACTION}</strong>: See {@link CubeState}
	 * 	for board coding. 4-point-neighborhoods on the cube with wrap-around.
	 *     <li><strong>{@code STICKER}</strong>: Adjacency set on the 7x7 stickers board:
	 * 	 All cells from the next row, except the cell in the same column as {@code iCell}, are in the adjacency set.
	 * 	 See {@link CubeState#getBoardVector()} for the stickers board coding.
	 *     <li><strong>{@code STICKER2}</strong>: All cells different from {@code iCell}.
	 * </ul>
	 *
	 * @param iCell	the board cell for which we want the set of adjacent cells
	 * @return a set of numbers for all cells adjacent to {@code iCell} (referring to the coding in
	 * 		a board vector)
	 */
	private HashSet<Integer> adjacencySet2x2(int iCell) {
		int i,j;
		HashSet<Integer> adjSet = new HashSet<>();
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
			case STICKER:
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
				break;
			case STICKER2:
				for (int k=0; k<getNumCells(); k++) {
					if (k!=iCell) adjSet.add(k);
				}
				break;
		} // switch

		return adjSet;
	}


	//
	// methods specific for 3x3x3 Rubik's Cube
	//

	/**
	 * @return the number of board cells
	 */
	private int getNumCells3x3() {
		// TODO: may be different for 3x3x3 Rubiks Cube
		return switch (CubeConfig.boardVecType) {
			case CUBESTATE -> 54;
			case CUBEPLUSACTION -> 56;
			case STICKER -> 480;
			case STICKER2 -> 40;
		};
	}

	/**
	 * @return the maximum number P of position values 0, 1, 2,..., P-1 that a board cell
	 * can have.
	 */
	private int getNumPositionValues3x3() {
		return switch (CubeConfig.boardVecType) {
			case CUBESTATE, CUBEPLUSACTION -> 6;
			case STICKER -> 2;
			case STICKER2 -> 12;
		};
	}

	/**
	 * [This method is only needed for agents {@link TDNTuple4Agt} and {@link Sarsa4Agt}.]
	 *
	 * @return 	a vector of length {@link #getNumCells()} that has in its {@code i}th element the number
	 * 			P of position values 0, 1, 2,..., P-1 that board cell {@code i} can have
	 */
	public int[] getPositionValuesVector3x3() {
		int[] posValVec = new int[getNumCells()];
		switch (CubeConfig.boardVecType) {
			case CUBESTATE, CUBEPLUSACTION -> {
				Arrays.fill(posValVec, 6);
				return posValVec;
			}
			case STICKER -> {
				Arrays.fill(posValVec, 2);
				return posValVec;
			}
			case STICKER2 -> {
				return new int[]{
						8, 8, 8, 8, 8, 8, 8, 8,
						3, 3, 3, 3, 3, 3, 3, 3,
						12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
						2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
			}
			default -> throw new RuntimeException("We should not arrive here!");
		} // switch
	}

	private int[][] fixedNTuples3x3(int mode) {
		// TODO:
		throw new RuntimeException("[XNTupleFuncsCube::fixedNTuples3x3]: Not yet implemented!");
	}

	/**
	 * Return all neighbors (adjacent cells) of {@code iCell} for the 2x2x2 pocket cube.
	 * <p>
	 * This is, depending on {@link CubeConfig#boardVecType}:
	 * <ul>
	 *     <li><strong>{@code CUBESTATE, CUBEPLUSACTION}</strong>: -- not yet implemented --
	 *     <li><strong>{@code STICKER}</strong>: -- not yet implemented --
	 *     <li><strong>{@code STICKER2}</strong>: For {@code iCell} in {0,...,15} (corner): all other cells in {0,...,15}.
	 *     For {@code iCell} in {16,...,39} (edge): all other cells in {16,...,39}.
	 * </ul>
	 *
	 * @param iCell	the board cell for which we want the set of adjacent cells
	 * @return a set of numbers for all cells adjacent to {@code iCell} (referring to the coding in
	 * 		a board vector)
	 */
	private HashSet<Integer> adjacencySet3x3(int iCell) {
		HashSet<Integer> adjSet = new HashSet<>();
		switch (CubeConfig.boardVecType) {
			case CUBESTATE:
			case CUBEPLUSACTION:
				// TODO:
				throw new RuntimeException("[XNTupleFuncsCube::adjacencySet3x3], case CUBESTATE: Not yet implemented!");
			case STICKER:
				// TODO:
				throw new RuntimeException("[XNTupleFuncsCube::adjacencySet3x3], case STICKER: Not yet implemented!");
			case STICKER2:
				if (iCell<16) {
					for (int k=0; k<16; k++) {
						if (k!=iCell) adjSet.add(k);
					}
				} else {
					for (int k=16; k<getNumCells(); k++) {
						if (k!=iCell) adjSet.add(k);
					}
				}
				break;
		} // switch

		return adjSet;
	}
}
