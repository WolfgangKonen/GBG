package games.EWS;

import games.*;
import games.EWS.StateObserverHelper.Helper;
import games.EWS.StateObserverHelper.Token;
import games.EWS.constants.ConfigEWS;
import tools.Types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public class XNTupleFuncsEWS extends XNTupleBase implements XNTupleFuncs, Serializable {

    private final static int[] fixedModes ={0}; // fixed modes for tuple selection
    private static final long serialVersionUID = 42L;

    public XNTupleFuncsEWS(){
        super();
    }

    @Override
    public int getNumCells() {
        return ConfigEWS.BOARD_SIZE*ConfigEWS.BOARD_SIZE;
    }

    /**
     *      Player 1..n = { 0,...,n}
     *      Empty = n+1
     *
     * @return The number of the position values
     */
    @Override
    public int getNumPositionValues() {
        return ConfigEWS.NUM_PLAYERS +2;
    }

    /**
     *
     * @return
     */
    @Override
    public int getNumPlayers() {
        return ConfigEWS.NUM_PLAYERS;
    }


    @Override
    public int getNumSymmetries() {
        return 0;
    }

    /**
     *
     * Since we probably have each player with 6 tokens we can use
     *
     *  size = 3 Possible sizes {3,4,5,6}
     *   <pre>
     * 	 00 01 02
     * 	 03 04 05
     * 	 06 07 08
     * 	 </pre>
     * @param so the stateObservation of the current game state
     * @return  a vector of length {@link #getNumCells() , holding for each cell its}
     * position value with  0 = Black, 1 = white, 2 = blue, 3 = red 4 Empty
     *
     *
     *
     */
    @Override
    public BoardVector getBoardVector(StateObservation so) {
        assert (so instanceof StateObserverEWS);
        int size = ((StateObserverEWS) so).getSize();
        int numPlayer = so.getNumPlayers();
        Token[][] state = ((StateObserverEWS) so).getGameState();
        int[] vector = new int[size*size];
        //Setting the gamestate players
        for(int i = 0, n=0; i < size; i++){
            for(int j = 0; j < size; j++,n++){
                int player = state[i][j].getPlayer();

                if(player < 0){
                  vector[n] = numPlayer;
                }else {
                    vector[n] = player;
                }
            }
        }
        // Set the possible actions on the vector
        // Which will be the numPlayers + 1
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        for(Types.ACTIONS action:acts){
           int[] actionParsed =  Helper.getIntsFromAction(action); // returns an array of [from,to] indices
            vector[actionParsed[1]] = numPlayer+1;
        }
    return new BoardVector(vector);
    }

    @Override
    public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
        return new BoardVector[]{boardVector};
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
                switch(ConfigEWS.BOARD_SIZE) {
                    case 3:
                        return new int[][]{
                                {0,1,3,4},
                                {3,4,6,7},
                                {1,2,4,5},
                                {4,5,7,8},
                        };
                    case 4: return new int[][]{
                            {0,1,4,5},
                            {1,2,5,6},
                            {2,3,6,7},

                            {4,5,8,9},
                            {5,6,9,10},
                            {6,7,10,11},
                    };
                    // Paper: Chu2017AgentEinsteinWn
                    case 5: {
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
                    case 6:return new int[][]{};
                    default:
                        throw new RuntimeException("XNTupleFuncsEWS: The size of the board must be [3,4,5,6].");
                }
            default: throw new RuntimeException("mode = " +mode + " is not supported");
        }

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
        int size = ConfigEWS.BOARD_SIZE; // board size

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
