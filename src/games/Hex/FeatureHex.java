package games.Hex;

import games.Feature;
import games.StateObservation;

import java.io.Serializable;
import java.util.Arrays;


public class FeatureHex implements Feature, Serializable {
    int featMode = 0;

    FeatureHex(int featMode){
        this.featMode = featMode;
    }

    @Override
    public double[] prepareFeatVector(StateObservation so) {
        StateObserverHex stateObs = (StateObserverHex) so;

        switch (featMode){
            case 0:
                return createFeatureVector0(stateObs.getPlayer(), stateObs.getBoard());
            case 1:
                return createFeatureVector1(stateObs.getPlayer(), stateObs.getBoard());
            case 2:
                return createFeatureVector2(stateObs.getPlayer(), stateObs.getBoard());
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

        sb.delete(sb.length()-2, sb.length());

        return sb.toString();
    }

    @Override
    public int getFeatmode() {
        return featMode;
    }

    @Override
    public int[] getAvailFeatmode() {
        return new int[]{0, 1, 2, 99};
    }

    @Override
	public int getInputSize(int featmode) {
        switch (featmode){
        case 0:
            return 7;
        case 1:
        	return HexConfig.TILE_COUNT;
        case 2:
            return HexConfig.BOARD_SIZE*HexConfig.BOARD_SIZE + 2;
        case 99:
        	return 81; // only for BOARD_SIZE=2
        default:
            throw new RuntimeException("Unknown featmode: "+featmode);
        }
    	
    }
    
    public double[] createFeatureVector0(int player, HexTile[][] board){
        double[] featureCurrentPlayer = HexUtils.getFeature0ForPlayer(board, player);
        double[] featureOpponentPlayer = HexUtils.getFeature0ForPlayer(board, HexUtils.getOpponent(player));

        //+1 because of tile count
        double[] inputVector = new double[featureCurrentPlayer.length+featureOpponentPlayer.length+1];

        int i = 0;

        for (double feature: featureCurrentPlayer) {
            inputVector[i] = feature;
            if (feature > 1){
                System.out.println("Hex FeatureVector0 warning: feature "+i+" was greater than 1 ("+feature+")");
            }
            i++;
        }

        for (double feature: featureOpponentPlayer) {
            inputVector[i] = feature;
            if (feature > 1){
                System.out.println("Hex FeatureVector0 warning: feature "+i+" was greater than 1 ("+feature+")");
            }
            i++;
        }

        //Max tiles per player: Half of tile count (round up in case of odd tile count)
        inputVector[i] = (double) HexUtils.getTileCountForPlayer(board, player)/(double) (Math.ceil(HexConfig.TILE_COUNT/2));

        //System.out.println(Arrays.toString(inputVector));
        //double[] boardVector = createFeatureVector1(player, board);

        //ArrayUtils.addAll();
        return inputVector;
    }

    public double[] createFeatureVector1(int player, HexTile[][] board){
        double[] inputVector = new double[HexConfig.TILE_COUNT];

        for (int i = 0; i<HexConfig.BOARD_SIZE; i++ ){
            for (int j = 0; j<HexConfig.BOARD_SIZE; j++ ){
                double v = 0f; //Default value for unused tiles
                if (board[i][j].getPlayer() == player){
                    v = 1f;
                } else if (board[i][j].getPlayer() == HexUtils.getOpponent(player)){
                    v = -1f;
                }

                //Tile values:
                //Opponent: -1
                //Free tile: 0
                //Own tile: 1
                inputVector[i*HexConfig.BOARD_SIZE+j] = v;
            }
        }

        return inputVector;
    }

    public double[] createFeatureVector2(int player, HexTile[][] board){
        double[] inputVector = new double[HexConfig.BOARD_SIZE*HexConfig.BOARD_SIZE + 2];

        int nEmpty=0; 
        for (int i = 0; i<HexConfig.BOARD_SIZE; i++ ){
            for (int j = 0; j<HexConfig.BOARD_SIZE; j++ ){
                inputVector[j*HexConfig.BOARD_SIZE+i] = board[i][j].getPlayer();
                if (board[i][j].getPlayer()==HexConfig.PLAYER_NONE) {
                	inputVector[j*HexConfig.BOARD_SIZE+i] = 0.5;
                	nEmpty++;
                }
            }
        }
        inputVector[HexConfig.BOARD_SIZE*HexConfig.BOARD_SIZE] = (nEmpty>2)?1:0;
        inputVector[HexConfig.BOARD_SIZE*HexConfig.BOARD_SIZE+1] = (nEmpty>2)?1:0;

        return inputVector;
    }

    public double[] createFeatureVector99(int player, HexTile[][] board){
    	if (board[0].length>2)
    		throw new RuntimeException("Feature mode 99 only available for 2x2 board");
    	
        double[] inputVector = new double[81];  // = 3^4

        int index=0; // index into LUT = inputVector 
        for (int i = 0, k=1; i<HexConfig.BOARD_SIZE; i++ ){
            for (int j = 0; j<HexConfig.BOARD_SIZE; j++){
                int posValue = board[i][j].getPlayer()+1;
                index += k*posValue;
                k *= 3;
            }
        }
        inputVector[index] = 1;

        return inputVector;
    }

}
