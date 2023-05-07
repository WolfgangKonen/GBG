package games.EWN;

import controllers.TD.ntuple4.NTuple4ValueFunc;
import controllers.TD.ntuple4.QLearn4Agt;
import controllers.TD.ntuple4.Sarsa4Agt;
import games.*;
import games.EWN.StateObserverHelper.Helper;
import games.EWN.StateObserverHelper.Token;
import games.EWN.config.ConfigEWN;
import tools.Types;

import java.io.Serial;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashSet;


public class XNTupleFuncsEWN extends XNTupleBase implements XNTupleFuncs, Serializable {

    private final static int[] fixedModes ={0}; // fixed modes for tuple selection

    @Serial
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
        return switch (ConfigEWN.CELL_CODING) {
            case 0 -> ConfigEWN.NUM_PLAYERS + 1;
            case 1 -> ConfigEWN.NUM_PLAYERS * 3 + 1;
            case 2 -> ConfigEWN.NUM_PLAYERS * 6 + 1;
            default -> throw new RuntimeException("Cell_Coding only implements 0, 1 and 2");
        };


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
     * The board vector is an {@code int[]} vector where each entry corresponds to one
     * cell of the board. In the case of 3x3 EWN the mapping is
     * <pre>
     *    0 1 2
     *    3 4 5
     *    6 7 8
     * </pre>
     *
     * @param so the stateObservation of the current game state
     * @return a vector of length {@link #getNumCells()}, holding for each board cell its
     * positional value. For example in the case of {@link ConfigEWN#CELL_CODING}==0, this is the player, i.e.
     * 0 = Black, 1 = White (optional: 2 = Blue, 3 = Red, depending on N) and N = Empty, where
     * N = {@link ConfigEWN#NUM_PLAYERS}.
     *
     * @see #getVectorforNPositionalValues(int)
     * @see #getVectorForNTimes3PositionalValues(int, int)
     * @see #getVectorPosForNTimes6PositionalValues(int, int)
     */
    @Override
    public BoardVector getBoardVector(StateObservation so) {
        assert (so instanceof StateObserverEWN);
        int size = ((StateObserverEWN) so).getSize();
        Token[][] state = ((StateObserverEWN) so).getGameState();
        int[] vector = new int[size*size];
        //Setting the game state's players and values
        for(int i = 0, n=0; i < size; i++) {
            for (int j = 0; j < size; j++, n++) {
                int player = state[i][j].getPlayer();
                int value = state[i][j].getValue();
                switch (ConfigEWN.CELL_CODING) {
                    case 0 -> vector[n] = getVectorforNPositionalValues(player);
                    case 1 -> vector[n] = getVectorForNTimes3PositionalValues(player, value);
                    case 2 -> vector[n] = getVectorPosForNTimes6PositionalValues(player, value);
                }

            }
        }
        return new BoardVector(vector);
    }

    /**
     * Return the {@link BoardVector} entry for a token that belongs to {@code player}.
     * Each token transmits to a {@link BoardVector} only its player (color), not its value.
     *
     * @param player a number in {0,...,N-1} with N = {@link ConfigEWN#NUM_PLAYERS}
     * @return number in range {0,...,N} positional values, where N codes "empty field"
     */
    private int getVectorforNPositionalValues(int player){
        // an empty field has player=-1
        return player < 0 ? ConfigEWN.NUM_PLAYERS : player;
    }

    /**
     * Return the {@link BoardVector} entry for a token that belongs to {@code player} and has value {@code value}.
     * If there are 6 such values ({@link ConfigEWN#BOARD_SIZE}=4 or 5), these are grouped (0,1) &rArr 0, (2,3) &rArr 1
     * and (4,5) &rArr 2. If there are 3 such values, they are taken directly.
     *
     * @param player a number in {0,...,N-1} with N = {@link ConfigEWN#NUM_PLAYERS}
     * @return positional value b in range {0,...,3*N}, where b=3*{@code player}+val and b=3*N codes "empty field"
     */
    private int getVectorForNTimes3PositionalValues(int player, int value){
        if (ConfigEWN.BOARD_SIZE>4) {
            int offset =(value < 2 ? 0 : value < 4 ? 1 : 2);
            return player < 0 ? ConfigEWN.NUM_PLAYERS*3 : (player*3+offset);
        } else {
            return player < 0 ? ConfigEWN.NUM_PLAYERS*3 : (player*3+value);
        }
    }

    /**
     * Return the {@link BoardVector} entry b for a token that belongs to {@code player} and has value {@code value}.<br>
     * If there are 6 such values ({@link ConfigEWN#BOARD_SIZE}=4 or 5), we have b=6*{@code player}+{@code value} and
     * b=6*N for "empty field". <br>
     * If there are 3 such values, we have b=3*{@code player}+{@code value} and b=3*N for "empty field".
     *
     * @param player a number in {0,...,N-1} with N = {@link ConfigEWN#NUM_PLAYERS}
     * @return positional value b in range {0,...,3*N} or {0,...,6*N}
     */
    private int getVectorPosForNTimes6PositionalValues(int player, int value){
        if (ConfigEWN.BOARD_SIZE > 4) {
            return player < 0 ? ConfigEWN.NUM_PLAYERS * 6 : (player*6+value);
        } else {
            return player < 0 ? ConfigEWN.NUM_PLAYERS * 3 : (player*3+value);
        }
    }

    public String debugBoardVector(int[] vector){
        StringBuilder s = new StringBuilder();
        DecimalFormat frmAct = new DecimalFormat("00");
        for (int z = 0; z < vector.length; z++) {
            s.append(frmAct.format(vector[z]));
            if((z+1) % ConfigEWN.BOARD_SIZE == 0){
                s.append("\n");
            }else{
                s.append(" ");
            }
        }
        return s.toString();
    }


    /**
     * Given a board vector from {@link #getBoardVector(StateObservation)} and given that the
     * game has s symmetries, return an array which holds at most s symmetric board vectors: <ul>
     * <li> the first element {@code vecOfBvecs[0]} is the board vector itself
     * <li> the other elements are the board vectors when transforming {@code boardVector}
     * 		according to the s-1 other symmetries (e. g. rotation, reflection, if applicable).
     * </ul>
     * In the case of EWN we have s=2 symmetries (the state itself and the mirror reflection along the main diagonal)
     *
     * @param boardVector a certain board in vector representation
     * @param n number of symmetry vectors to return (n=0 meaning 'all')
     * @return vecOfBvecs
     */
    @Override
    public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
        if (n==0) n=2;
        assert (n==1 || n==2) : "Only n=1 or n=2 symmetry vectors allowed!";
        //if(n > 2 || n < 1) throw new RuntimeException("There are maximal 2 symmetry vectors");
        BoardVector[] bvArray = new BoardVector[n];
        bvArray[0] = boardVector;
        if(n == 2) bvArray[1] = getDiagonalSymmetryVector(boardVector);
        return bvArray;
    }

    /**
     * Given a certain board array of symmetric (equivalent) states for state <b>{@code so}</b>
     * and a certain action to be taken in <b>{@code so}</b>, generate the array of equivalent
     * action keys {@code equivAction} for the symmetric states.
     * <p>
     * This method is needed for Q-learning and Sarsa only.
     *
     * @param actionKey
     * 				the key of the action to be taken in <b>{@code so}</b>
     * @return <b>equivAction</b>
     * 				array of the equivalent actions' keys.
     * <p>
     * If actionKey is the key of a certain action in board equiv[0], then equivAction[i] is the key of the equivalent
     * action in the i'th equivalent board vector equiv[i]. <br>
     * Here, equiv[i] = {@link #symmetryVectors(BoardVector, int)}{@code [i]}.
     * <p>
     * Example: In EWN, symmetry transformation 1 (the only allowed one) is a mirror reflection along the main diagonal.
     * If the action is "104" (meaning: move token from field 1 to field 4), then the mirror-reflected action is "304"
     * (because field 1 is mirror-mapped to field 3 and field 4 to field 4).
     */
    @Override
    public int[] symmetryActions(int actionKey) {
        int size = ConfigEWN.BOARD_SIZE;
        int[] equivAction = new int[2];
        equivAction[0] = actionKey;

        int[] from_to = Helper.getIntsFromAction(Types.ACTIONS.fromInt(actionKey)); // [from, to] int array
        int to_x = from_to[1] % size;
        int to_y = (from_to[1] - to_x) / size;
        int from_x = from_to[0] % size;
        int from_y = (from_to[0] - from_x) / size;
        int new_to = to_x*size + to_y;
        int new_from = from_x*size + from_y;
        equivAction[1] = new_from*100 + new_to;

        return equivAction;
    }

    /**
     *
     * @return {@code true}, if {@code actionMap} in {@link NTuple4ValueFunc} shall be used. Otherwise, if {@code false},
     * no action mapping occurs and the int values of {@link Types.ACTIONS} are used for indexing.
     * Only relevant for {@link QLearn4Agt} and {@link Sarsa4Agt}.
     *
     * @see NTuple4ValueFunc
     */
    @Override
    public boolean useActionMap() { return true; }

    /**
     * Helper for {@link #symmetryVectors(BoardVector, int)}. Given {@link BoardVector} bv, return the symmetric
     * {@link BoardVector} when mirror-reflecting along the main diagonal. Example for 3x3 EWN:<pre>
     *           0 2 0
     *     bv =  0 1 1
     *           2 2 1
     * </pre>
     * with 0 = player 0, 1 = player 1, 2 = empty, is transformed into <pre>
     *           0 0 2
     *           2 1 2
     *           0 1 1
     * </pre>
     * @param bv the {@link BoardVector} to be transformed
     * @return   the mirror-reflected {@link BoardVector}
     */
    private BoardVector getDiagonalSymmetryVector(BoardVector bv){
        int[] mirror = new int[bv.bvec.length];
        int size = ConfigEWN.BOARD_SIZE;
        // WRONG version (before 05/2023):
//        int[] indices = new int[bv.bvec.length];
//        int i = 0;
//        for(int pos : bv.bvec){
//            int x = pos % size;
//            int y = (pos-x) / size;
//            mirror[i] = ((x*size)+y);
//            i++;
//        }
        // CORRECT version:
        for (int pos=0; pos<bv.bvec.length; pos++) {
            // Convert pos to 2d (x,y) where  pos = (y*size) + x
            int x = pos % size;
            int y = (pos-x) / size;

            // swap x and y so that
            // (0,0) => (0,0)
            // (0,1) => (1,0)
            // (2,1) => (1,2) and so on
            int newpos =  (x*size)+y;
            mirror[newpos] = bv.bvec[pos];
        }
        return new BoardVector(mirror);
    }

    @Override
    public int[][] fixedNTuples(int mode) {
        switch(mode) {
            case 0:
                return switch (ConfigEWN.BOARD_SIZE) {
                    case 3 -> getNTuple3x3();
                    case 4 -> getNTuple4x4();
                    case 5 -> getNTuple5x5();
                    case 6 -> getNTuple6x6();
                    default -> throw new RuntimeException("XNTupleFuncsEWN: The size of the board must be [3,4,5,6].");
                };
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
    public HashSet<Integer> adjacencySet(int iCell) {
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

