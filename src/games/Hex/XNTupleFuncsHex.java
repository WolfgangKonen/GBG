package games.Hex;

import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

import java.io.Serializable;
import java.util.HashSet;

public class XNTupleFuncsHex extends XNTupleBase implements XNTupleFuncs, Serializable {
	
    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .gamelog or .agt.zip containing this object will
     * become unreadable or you have to provide a special version transformation)
     * <p>
     * [We need this strange number here, because serialVersionUID was not present before, 
     * and the so far stored agents had this automatically created serialVersionUID.]
     */
    private static final long serialVersionUID = -2631312361401190002L;
    
    private int[] actionVector;
    private transient BoardVector[] newplace;
    private int[][] actionArray;

    public XNTupleFuncsHex() {
    	actionVector = new int[getNumCells()];
    	for (int i=0; i<actionVector.length; i++) actionVector[i]=i;
    	// calculate actionArray[][]: for a given action with key j, the element
    	// actionArray[i][j] holds the equivalent action when the state is transformed to 
    	// equiv[i] = symmetryVectors(int[] boardVector)[i]
    	newplace = symmetryVectors(new BoardVector(actionVector),0);
    	actionArray = new int[newplace.length][];
    	for (int i=0; i<actionArray.length; i++) {
    		actionArray[i] = new int[this.getNumCells()];
    		for (int j=0; j<this.getNumCells(); j++)
    			actionArray[i][j] = whereHas(newplace[i].bvec,j);
    	}
    }
    
    // helper function for XNTupleFuncsHex(): "Where has array arr the content j?"
    private int whereHas(int[] arr, int j) {
    	for (int i=0; i<arr.length; i++) 
    		if (arr[i]==j) return i;
    	throw new RuntimeException("whereHas: arr does not contain j!!");
    }
    
   @Override
    public int getNumCells() {
        return HexConfig.TILE_COUNT;
    }

    @Override
    public int getNumPositionValues() {
        return 3;
    }

    @Override
    public int getNumPlayers() {
        return 2;
    }

    @Override
    public BoardVector getBoardVector(StateObservation so) {
        int[] bmap = {0, 1, 2};  // bmap is just for debug check (no effect if bmap={0,1,2}
        // and any other permutation should lead after re-training to
        // identical results as well.
        StateObserverHex stateObs = (StateObserverHex) so;
        HexTile[] boardVectorTiles = HexUtils.boardToVector(stateObs.getBoard());
        int[] boardVectorInt = new int[boardVectorTiles.length];

        for (int i = 0; i < boardVectorInt.length; i++) {
            boardVectorInt[i] = bmap[boardVectorTiles[i].getPlayer() + 1];
        }

        return new BoardVector(boardVectorInt);
    }

    @Override
    public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
        BoardVector[] symmetries = new BoardVector[2];
        symmetries[0] = boardVector;
        symmetries[1] = rotateBoard2(boardVector); // a bit faster than rotateBoard()
//        int[] check = rotateBoard(boardVector);
//        for (int i=0; i<boardVector.length; i++)
//        	assert symmetries[1][i]==check[i] : "Oops, rotateBoard() and rotateBoard2() differ!";
        return symmetries;
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
		int i;
		int numEquiv = actionArray.length;
		int[] equivAction = new int[numEquiv];
		for (i = 0; i < numEquiv; i++) {
			equivAction[i] = actionArray[i][actionKey];
		}

		return equivAction;
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
        int[][] tuples = new int[(HexConfig.BOARD_SIZE * 2) + 1][HexConfig.BOARD_SIZE];

        int[][] fixedTuples6x6 = {			// a tuple config which gave good results
    			{4, 5, 9, 10, 11, 15},
    			{18, 6, 7, 8, 12, 13},
    			{19, 20, 8, 24, 13, 14},
    			{16, 17, 21, 22, 23, 11},
    			{16, 4, 5, 9, 10, 11},
    			{18, 24, 12, 13, 30, 31},
    			{32, 33, 21, 22, 26, 27},
    			{19, 20, 9, 13, 14, 15},
    			{32, 16, 33, 21, 22, 27},
    			{18, 19, 24, 25, 26, 30},
    			{18, 19, 7, 8, 12, 13},
    			{16, 20, 21, 25, 10, 27},
    			{0, 1, 2, 3, 6, 7},
    			{2, 7, 8, 12, 14, 15},
    			{1, 2, 3, 6, 7, 8},
    			{32, 33, 25, 26, 27, 31},
    			{34, 35, 23, 27, 28, 29},
    			{0, 1, 2, 3, 4, 7},
    			{0, 1, 2, 3, 6, 12},
    			{23, 26, 27, 28, 29, 31},
    			{32, 20, 25, 26, 27, 31},
    			{18, 19, 20, 21, 25, 30},
    			{1, 2, 6, 7, 8, 13},
    			{32, 34, 35, 27, 28, 29},
    			{32, 33, 25, 26, 27, 31} };
        
        switch (mode) {
        case 1:
            //All diagonal lines
            for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
                for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                    int actionInt = i * HexConfig.BOARD_SIZE + j;
                    tuples[i][j] = actionInt;

                    actionInt = j * HexConfig.BOARD_SIZE + i;
                    tuples[i + HexConfig.BOARD_SIZE][j] = actionInt;
                }
            }

            //The straight line in the center of the board
            //(0, n); (1, n-1); ... (n-1, 1); (n, 0);
            for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
                int j = HexConfig.BOARD_SIZE - i;
                int actionInt = i * HexConfig.BOARD_SIZE + j;
                tuples[HexConfig.BOARD_SIZE * 2][i] = actionInt - 1;
            }
        	break;
        case 2:			// individual settings
        	switch (HexConfig.BOARD_SIZE) {
        	case 6: 
        		tuples = fixedTuples6x6;
        		break;
        	default:
            	throw new RuntimeException("[fixedNTuples] mode=2 for board size="+HexConfig.BOARD_SIZE+" not allowed");
        	}
        	break;
        default:
        	throw new RuntimeException("[fixedNTuples] mode="+mode+" not allowed");
        }

        return tuples;
    }
    
	@Override
	public String fixedTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>"
				+ "1: TODO"
				+ "</html>";
	}

    private static int[] fixedModes = {1,2};
    
	public int[] fixedNTupleModesAvailable() {
		return fixedModes;
	}


    @Override
    public HashSet adjacencySet(int iCell) {
        //Each cell has a max of 6 neighbors
        //Coordinates of those relative to current cell:
        //-1,0; -1,1; 0,-1; 0,1; 1,-1; 1,0 (x,y)
        HashSet<Integer> adjacencySet = new HashSet<>();

        int x = (int) Math.floor(iCell / HexConfig.BOARD_SIZE);
        int y = iCell % HexConfig.BOARD_SIZE;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int neighborX = x + i;
                int neighborY = y + j;
                //Not all of those exist if the cell is next to an edge, so check for validity
                if (i != j && HexUtils.isValidTile(neighborX, neighborY)) {
                    //Transform from 2D cell coordinates back to linear representation and add to HashSet
                    adjacencySet.add(neighborX * HexConfig.BOARD_SIZE + neighborY);
                }
            }
        }

        return adjacencySet;
    }

    /**
     * Mirrors the board along the given axis.
     * Not actually useful - Mirrored boards in Hex are not equivalent.
     *
     * @param boardVector Game board vector
     * @param axis        Axis along which to mirror
     * @return Mirrored board
     */
    @Deprecated
    private int[] mirrorBoard(int[] boardVector, Axis axis) {
        int[] mirroredVector = boardVector.clone();

        if (axis == Axis.VERTICAL) {
            //Subdivide into chunks of BOARD_SIZE elements and reverse each
            //Example for BOARD_SIZE=3:
            //Before: 1,2,3, 4,5,6, 7,8,9
            //After:  3,2,1, 6,5,4, 9,8,7
            for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
                int[] tmp = new int[HexConfig.BOARD_SIZE];
                for (int j = 0; j < ((HexConfig.BOARD_SIZE + 1) / 2); j++) {
                    tmp[j] = mirroredVector[i * HexConfig.BOARD_SIZE + j];
                    mirroredVector[i * HexConfig.BOARD_SIZE + j] = mirroredVector[i * HexConfig.BOARD_SIZE + HexConfig.BOARD_SIZE - j - 1];
                    mirroredVector[i * HexConfig.BOARD_SIZE + HexConfig.BOARD_SIZE - j - 1] = tmp[j];
                }
            }
        } else if (axis == Axis.HORIZONTAL) {
            //Swap the places of chunks of BOARD_SIZE elements from front and end until center is reached
            //Example for BOARD_SIZE=3:
            //Before: 1,2,3, 4,5,6, 7,8,9
            //After:  7,8,9, 4,5,6, 1,2,3
            for (int i = 0; i < ((HexConfig.BOARD_SIZE + 1) / 2); i++) {
                int[] tmp = new int[HexConfig.BOARD_SIZE];
                for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                    tmp[j] = mirroredVector[i * HexConfig.BOARD_SIZE + j];
                    mirroredVector[i * HexConfig.BOARD_SIZE + j] = mirroredVector[(HexConfig.BOARD_SIZE - i - 1) * HexConfig.BOARD_SIZE + j];
                    mirroredVector[(HexConfig.BOARD_SIZE - i - 1) * HexConfig.BOARD_SIZE + j] = tmp[j];
                }
            }
        }

        return mirroredVector;
    }

    /**
     * Rotates the board by 180° degrees by mirroring along both axes.
     * Hex has rotational symmetry if rotated by 180°. Slower than {@link #rotateBoard2(int[])}.
     *
     * @param boardVector Game board vector
     * @return Rotated board
     */
    @Deprecated
    private BoardVector rotateBoard(BoardVector boardVector) {
        int[] rotatedBoard = boardVector.bvec.clone();

        //Rotating by 180 degrees is the same as mirroring by both axes
        //Rotating by 90 or 270 degrees would not be an equivalent board in Hex
        rotatedBoard = mirrorBoard(rotatedBoard, Axis.HORIZONTAL);
        rotatedBoard = mirrorBoard(rotatedBoard, Axis.VERTICAL);

        return new BoardVector(rotatedBoard);
    }

    /**
     * Rotates the board by 180° degrees by mirroring along both axes.
     * Hex has rotational symmetry if rotated by 180°.
     *
     * @param boardVector game board vector
     * @return rotated board vector
     */
    private BoardVector rotateBoard2(BoardVector boardVector) {
    	int[] bvec = boardVector.bvec;
        int[] rotatedBoard = bvec.clone();
        int L = bvec.length;

        //Rotating by 180 degrees is the same as mirroring by both axes
        //Rotating by 90 or 270 degrees would not be an equivalent board in Hex
        for (int i=0,j=L-1; i<L; i++,j--) {
        	rotatedBoard[i]=bvec[j];
        }

        return new BoardVector(rotatedBoard);
    }

    private enum Axis {
        HORIZONTAL, VERTICAL
    }
}
