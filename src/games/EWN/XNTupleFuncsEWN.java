package games.EWN;

import games.*;
import games.EWN.StateObserverHelper.Token;
import games.EWN.config.ConfigEWN;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashSet;


public class XNTupleFuncsEWN extends XNTupleBase implements XNTupleFuncs, Serializable {

    private final static int[] fixedModes ={0}; // fixed modes for tuple selection
    private static final long serialVersionUID = 42L;

    public XNTupleFuncsEWN(){
        super();
    }

    @Override
    public int getNumCells() {
        return ConfigEWN.BOARD_SIZE* ConfigEWN.BOARD_SIZE;
    }

    /**
     *      Player 1..n = { 0,...,n}
     *      Empty = n+1
     *
     * @return The number of the position values
     */
    @Override
    public int getNumPositionValues() {
        switch(ConfigEWN.CEll_CODING){
            case 0: return ConfigEWN.NUM_PLAYERS +1;
            case 1: return ConfigEWN.NUM_PLAYERS * 3 + 1;
            case 2: return ConfigEWN.NUM_PLAYERS * 6 +1;
            default: throw new RuntimeException("Cell_Coding only implements 0 and 1");
        }


    }

    /**
     *
     * @return number of players
     */
    @Override
    public int getNumPlayers() {
        return ConfigEWN.NUM_PLAYERS;
    }


    @Override
    public int getNumSymmetries() {
        return 1;
    }

    /**
     *
     * Since we probably have each player with 6 tokens we can use
     *
     *  size = 3 Possible sizes {3,4,5,6}
     *   <pre>
     * 	 00 01 02
     * 	 03 04 05    ---&gt;    00,01,02,03,04,05,06,07,08
     * 	 06 07 08
     * 	 </pre>
     *
     * 	   returns the board vector
     * 	  <pre>
     * 	   0,1,2,3,4,5,6,7,8
     *
     * 	  </pre>
     *
     * @param so the stateObservation of the current game state
     * @return  a vector of length {@link #getNumCells() , holding for each cell its}
     * position value with  0 = Black, 1 = white, 2 = blue, 3 = red 4 Empty
     *
     *
     *
     */
    @Override
    public BoardVector getBoardVector(StateObservation so) {
        assert (so instanceof StateObserverEWN);
        int size = ((StateObserverEWN) so).getSize();
        Token[][] state = ((StateObserverEWN) so).getGameState();
        int[] vector = new int[size*size];
        //Setting the gamestate players
        for(int i = 0, n=0; i < size; i++) {
            for (int j = 0; j < size; j++, n++) {
                int player = state[i][j].getPlayer();
                int value = state[i][j].getValue();
                switch (ConfigEWN.CEll_CODING) {
                    case 0: {
                        vector[n] = getVectorforNPositionalValues(player);
                        break;
                    }
                    case 1: {
                        vector[n] = getVectorForNTimes3PositionalValues(player,value);
                        break;
                    }
                    case 2: {
                        vector[n] = getVectorPosForNTimes6PositionalValues(player,value);
                        break;
                    }
                }

            }
        }
        return new BoardVector(vector);
    }

    /**
     * Each Token will be seen differently
     * @param player
     * @return number in range of [0,...,N*6] positional values
     */
    private int getVectorPosForNTimes6PositionalValues(int player, int value){
        if (ConfigEWN.BOARD_SIZE > 4) {
         return player < 0 ? ConfigEWN.NUM_PLAYERS * 6:player * 6 + value;
        } else {
            return player < 0 ? ConfigEWN.NUM_PLAYERS * 3:player * 3 + value;
        }
    }

    /**
     * Each token will be grouped with at least one other token
     * @param player
     * @return number in range of [0,...,N*3] positional values
     */
    private int getVectorForNTimes3PositionalValues(int player, int value){
        int returnValue;
        if(ConfigEWN.BOARD_SIZE>4){
            int offset =(value < 2 ? 0 : value < 4 ? 1 : 2);
            return player < 0 ? ConfigEWN.NUM_PLAYERS*3:(player * 3+offset);
        }else{

            return player < 0 ? ConfigEWN.NUM_PLAYERS*3:player*3+value;
        }
    }

    /**
     * Each token will be seen from players perspective only
     * @param player
     * @return number in range of [0,...,N] positional values
     */
    private int getVectorforNPositionalValues(int player){
        return player < 0 ? ConfigEWN.NUM_PLAYERS:player;
    }

    public String debugBoardVector(int[] vector){
        String s = "";
        DecimalFormat frmAct = new DecimalFormat("00");
        for (int z = 0; z < vector.length; z++) {
            s += frmAct.format(vector[z]);
            if((z+1) % ConfigEWN.BOARD_SIZE == 0){
                s+= "\n";
            }else{
                s+= " ";
            }
        }
        return s;
    }


    @Override
    public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
        if(n > 2 || n < 1) throw new RuntimeException("There are maximal 2 symmetry vectors");
        BoardVector[] bvArray = new BoardVector[n];
        bvArray[0] = boardVector;
        if(n == 2) bvArray[1] = getDiagonalSymmetryVector(boardVector);
        return bvArray;
    }

    /**
     * Sarsa  and Q-Learning only.
     * @param actionKey
     * 				the key of the action to be taken in <b>{@code so}</b>
     * @return
     */
    @Override
    public int[] symmetryActions(int actionKey) {
        return new int[0];
    }

    @Override
    public int[][] fixedNTuples(int mode) {
        switch(mode) {
            case 0:
                switch(ConfigEWN.BOARD_SIZE) {
                    case 3:return getNTuple3x3();
                    case 4: return getNTuple4x4();
                    case 5: return getNTuple5x5();
                    case 6: return getNTuple6x6();
                    default:
                        throw new RuntimeException("XNTupleFuncsEWN: The size of the board must be [3,4,5,6].");
                }
            default: throw new RuntimeException("mode = " +mode + " is not supported");
        }

    }
    /**
     *      4 x 4 tuple
     *     Return the NTuples for a board size of 3x3
     */
    private int[][] getNTuple3x3(){
        return new int[][]{
                {0,1,3,4},
                {3,4,6,7},
                {1,2,4,5},
                {4,5,7,8},
        };
    }

    /**
     *      12 x 4 tuple
     *     Return the NTuples for a board size of 4x4
     */
    private int[][] getNTuple4x4(){
        return new int[][]{
                {0,1,4,5},
                {1,2,5,6},
                {2,3,6,7},

                {4,5,8,9},
                {5,6,9,10},
                {6,7,10,11},

                {8,9,12,13},
                {9,10,13,14},
                {10,11,14,15}
        };
    }

        /**
         *      24 x 6 tuple used by Paper: Chu2017AgentEinsteinWN
         *     Return the NTuples for a board size of 5x5
         */
    private int[][] getNTuple5x5(){
        return new int[][]{
                {0, 5, 10, 1, 6, 11},
                {5,10,15,6,11,16},
                {10,15,20,11,16,21},

                {1,6,11,2,7,12},
                {6,11,16,7,12,17},
                {11,16,21,12,17,22},

                {2,7,12,3,8,13},
                {7,12,17,8,13,18},
                {12,17,22,13,18,23},

                {3,8,13,4,9,14},
                {8,13,18,9,14,19},
                {13,18,23,14,19,24},

                {0,1,2,5,6,7},
                {1,2,3,6,7,8},
                {2,3,4,7,8,9},

                {5,6,7,10,11,12},
                {6,7,8,11,12,13},
                {7,8,9,12,13,14},

                {10,11,12,15,16,17},
                {11,12,13,16,17,18},
                {12,13,14,17,18,19},

                {15,16,17,20,21,22},
                {16,17,18,21,22,23},
                {17,18,19,22,23,24}
        };
    }


    /**
     * 40 x 6 tuple
     * Return the NTuples for a board size of 6x6
     */
    private int[][] getNTuple6x6(){
        return new int[][]{
                // first row horizontal
                {0, 6, 12, 1, 7, 13},
                {1, 7, 13, 2, 8, 14},
                {2, 8, 14, 3, 9, 15},
                {3, 9, 15, 4, 10, 16},
                {4, 10, 16, 5, 11, 17},

                // 2nd row horizontal // change index 0 and 3 of each tuple
                {18, 6, 12, 19, 7, 13},
                {19, 7, 13, 20, 8, 14},
                {20, 8, 14, 21, 9, 15},
                {21, 9, 15, 22, 10, 16},
                {22, 10, 16, 23, 11, 17},
                // 3rd row horizontal // change index 1 and 4 of each tuple
                {18, 24, 12, 25, 19, 13},
                {19, 25, 13, 26, 20, 14},
                {20, 26, 14, 27, 21, 15},
                {21, 27, 15, 28, 22, 16},
                {22, 28, 16, 29, 23, 17},
                // 4th row horizontal // change index 2 and 5 of each tuple
                {18, 24, 30, 25,31, 19},
                {19, 25, 31, 26, 32, 20},
                {20, 26, 32, 27,33, 21},
                {21, 27, 33, 28, 34, 22},
                {22, 28, 34, 29, 35, 23},

                // vertical tuple
                {0,1,2,6,7,8},
                {6,7,8,12,13,14},
                {12,13,14,18,19,20},
                {18,19,20,24,25,26},
                {24,25,26,30,31,32},

                {3,1,2,9,7,8},
                {9,7,8,15,13,14},
                {15,13,14,21,19,20},
                {21,19,20,27,25,26},
                {27,25,26,33,31,32},


                {3,4,2,9,10,8},
                {9,10,8,15,16,14},
                {15,16,14,21,22,20},
                {21,22,20,27,28,26},
                {27,28,26,33,34,32},


                {3,4,5,9,10,11},
                {9,10,11,15,16,17},
                {15,16,17,21,22,23},
                {21,22,23,27,28,29},
                {27,28,29,33,34,35},
        };
    }

    /**
     *
     * @param bv Boardvector
     *          [0,1,2,
     *           3,4,5,
     *           6,7,8]
     *
     *           returns a new Boardvector
     *           [
     *           0, 3, 6,
     *           1, 4, 7,
     *           2, 5, 8
     *          ]
     *
     *          Converting the bv to 2d and swap x and y since the board has fixed n*n size
     *          [(0,0) , (0,1), (0,2)
     *           (1,0) , (1,1), (1,2)
     *           (2,0) , (2,1), (2,2)]
     *
     * @return new Boardvector swapped
     *          [(0,0), (1,0), (2,0)
     *           (0,1), (1,1), (2,1)
     *           (0,2), (1,2), (2,2)]*
     */
    private BoardVector getDiagonalSymmetryVector(BoardVector bv){
        int[] mirror = new int[bv.bvec.length];
        int[] indices = new int[bv.bvec.length];
        int size = ConfigEWN.BOARD_SIZE;
        int i = 0;
        for(int pos : bv.bvec){
            // Convert to 2d

            // swap x and y so that
            // (0,0) => (0,0)
            // (0,1) => (1,0)
            // (2,1) => (1,2)
            int x = pos % size;
            int y = (pos-x) / size;
            mirror[i] = ((x*size)+y);
            i++;
        }
        return new BoardVector(mirror);
    }

    @Override
    public String fixedTooltipString() {
        return "<html>"
                + "0: 4 6-Tuples "

                + "</html>";
    }

    @Override
    public int[] fixedNTupleModesAvailable() {
        return fixedModes;
    }

    @Override
    public HashSet adjacencySet(int iCell) {
        HashSet<Integer> neighbours = new HashSet<>();
        int size = ConfigEWN.BOARD_SIZE; // board size

        for(int dir : new int[]{-size-1, -size, -size+1, -1, +1,size-1,size,size+1}){
            int potentialNeighbour = iCell + dir;
            // Bounds for [0,...,size²-1]
            if(potentialNeighbour > size*size-1   || potentialNeighbour < 0) continue;
            if(potentialNeighbour % size == 0 && iCell % size == size-1) continue;
            if(potentialNeighbour % size == size-1 && iCell % size == 0) continue;
            neighbours.add(potentialNeighbour);
        }
        return neighbours;
    }

}

