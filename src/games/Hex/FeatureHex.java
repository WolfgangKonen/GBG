package games.Hex;

import games.Feature;
import games.StateObservation;

import java.io.Serializable;


public class FeatureHex implements Feature, Serializable {
    int featMode = 0;

    FeatureHex(int featMode) {
        this.featMode = featMode;
    }

    @Override
    public double[] prepareFeatVector(StateObservation so) {
        StateObserverHex stateObs = (StateObserverHex) so;

        switch (featMode) {
            case 0:
                return createFeatureVector0(stateObs.getPlayer(), stateObs.getBoard());
            case 1:
                return createFeatureVector1(stateObs.getPlayer(), stateObs.getBoard());
            case 2:
                return createFeatureVector2(stateObs.getPlayer(), stateObs.getBoard());
            case 3:
                return createFeatureVector3(stateObs.getPlayer(), stateObs.getBoard());
            case 99:
                return createFeatureVector99(stateObs.getPlayer(), stateObs.getBoard());
            default:
                System.out.println("Unknown feature mode, defaulting to feature mode 0");
                return createFeatureVector0(stateObs.getPlayer(), stateObs.getBoard());
        }

    }

    @Override
    public String stringRepr(double[] featVec) {
        StringBuilder sb = new StringBuilder();
        for (double aFeatVec : featVec) {
            sb.append(aFeatVec);
            sb.append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        return sb.toString();
    }

    @Override
    public int getFeatmode() {
        return featMode;
    }

    @Override
    public int[] getAvailFeatmode() {
        return new int[]{0, 1, 2, 3, 99};
    }

    @Override
    public int getInputSize(int featmode) {
        switch (featmode) {
            case 0:
                return 7;
            case 1:
                return HexConfig.TILE_COUNT + 1;
            case 2:
                return HexConfig.BOARD_SIZE * HexConfig.BOARD_SIZE + 2;
            case 3:
                return 12;
            case 99:
                return (int) Math.pow(3, (float) HexConfig.TILE_COUNT); // only for BOARD_SIZE=2
            default:
                throw new RuntimeException("Unknown featmode: " + featmode);
        }

    }

    /**
     * Returns longest chain, number of unused neighboring tiles and number of virtual connections
     * for each player, as well as the number of tiles the player has on the board. All values on a scale of 0-1.
     *
     * @param player The player who has the next move
     * @param board  Current board array
     * @return A vector containing all the features described above
     */
    public double[] createFeatureVector0(int player, HexTile[][] board) {
        double[] featureCurrentPlayer = HexUtils.getFeature0ForPlayer(board, player);
        double[] featureOpponentPlayer = HexUtils.getFeature0ForPlayer(board, HexUtils.getOpponent(player));

        //+1 because of tile count
        double[] inputVector = new double[featureCurrentPlayer.length + featureOpponentPlayer.length + 1];

        int i = 0;

        for (double feature : featureCurrentPlayer) {
            inputVector[i] = feature;
            i++;
        }

        for (double feature : featureOpponentPlayer) {
            inputVector[i] = feature;
            i++;
        }

        //Max tiles per player: Half of tile count (round up in case of odd tile count)
        inputVector[i] = (double) HexUtils.getTileCountForPlayer(board, player) /
                (double) (Math.ceil(HexConfig.TILE_COUNT / 2));

        return inputVector;
    }

    /**
     * Returns longest chain, number of unused neighboring tiles, number of virtual connections, number of direct
     * connections and number of weak connections for each player, as well as the number of tiles the player has
     * on the board. Values range from 0 to n^2 with n being the length of one side of the game board.
     *
     * @param player The player who has the next move
     * @param board  Current board array
     * @return A vector containing all the features described above
     */
    public double[] createFeatureVector3(int player, HexTile[][] board) {
        double[] featureCurrentPlayer = HexUtils.getFeature3ForPlayer(board, HexConfig.PLAYER_ONE);
        double[] featureOpponentPlayer = HexUtils.getFeature3ForPlayer(board, HexConfig.PLAYER_TWO);

        //double[] featureMode1 = createFeatureVector1(player, board);

        //+1 because of tile count
        double[] inputVector = new double[featureCurrentPlayer.length + featureOpponentPlayer.length + 2];

        int i = 0;

        for (double feature : featureCurrentPlayer) {
            inputVector[i] = feature;
            i++;
        }

        for (double feature : featureOpponentPlayer) {
            inputVector[i] = feature;
            i++;
        }

        //Max tiles per player: Half of tile count (round up in case of odd tile count)
        inputVector[i++] = (double) HexUtils.getTileCountForPlayer(board, HexConfig.PLAYER_ONE) /
                (double) (Math.ceil(HexConfig.TILE_COUNT / 2));

        inputVector[i] = player;

        /*for (double feature: featureMode1) {
            inputVector[i] = feature;
            i++;
        }*/

        return inputVector;
    }

    /**
     * Converts the raw board data to a vector and adds one feature indicating which player's turn it is.
     *
     * @param player The player who has the next move
     * @param board  Current board array
     * @return A vector containing all the features described above
     */
    public double[] createFeatureVector1(int player, HexTile[][] board) {
        double[] inputVector = new double[HexConfig.TILE_COUNT + 1];

        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                /*double v = 0.5f; //Default value for unused tiles
                if (board[i][j].getPlayer() == player){
                    v = 1f;
                } else if (board[i][j].getPlayer() == HexUtils.getOpponent(player)){
                    v = 0f;
                }*/
                double v = board[i][j].getPlayer();

                inputVector[j * HexConfig.BOARD_SIZE + i] = v;
            }
        }

        inputVector[HexConfig.TILE_COUNT] = player;

        return inputVector;
    }

    public double[] createFeatureVector2(int player, HexTile[][] board) {
        double[] inputVector = new double[HexConfig.BOARD_SIZE * HexConfig.BOARD_SIZE + 2];

        int nEmpty = 0;
        for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                inputVector[j * HexConfig.BOARD_SIZE + i] = board[i][j].getPlayer();
                if (board[i][j].getPlayer() == HexConfig.PLAYER_NONE) {
                    inputVector[j * HexConfig.BOARD_SIZE + i] = 0.5;
                    nEmpty++;
                }
            }
        }
        inputVector[HexConfig.BOARD_SIZE * HexConfig.BOARD_SIZE] = (nEmpty > 2) ? 1 : 0;
        inputVector[HexConfig.BOARD_SIZE * HexConfig.BOARD_SIZE + 1] = player;

        return inputVector;
    }

    /**
     * Emulates an n-tuple system for testing purposes. Best used on very small boards.
     *
     * @param player The player who has the next move
     * @param board  Current board array
     * @return A vector containing all the features described above
     */
    public double[] createFeatureVector99(int player, HexTile[][] board) {

        double[] inputVector = new double[(int) Math.pow(3, (float) HexConfig.TILE_COUNT)];

        int index = 0; // index into LUT = inputVector
        for (int i = 0, k = 1; i < HexConfig.BOARD_SIZE; i++) {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                int posValue = board[i][j].getPlayer() + 1;
                index += k * posValue;
                k *= 3;
            }
        }
        inputVector[index] = 1;

        return inputVector;
    }

}
