package games.Hex;

import games.Feature;
import games.StateObservation;

import javax.swing.plaf.nimbus.State;


public class FeatureHex implements Feature {
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
        return new int[]{0, 1};
    }

    @Override
	public int getInputSize(int featmode) {
        switch (featmode){
        case 0:
            return 2;
        case 1:
            return HexConfig.BOARD_SIZE*HexConfig.BOARD_SIZE;
        default:
            throw new RuntimeException("Unknown featmode: "+featmode);
        }
    	
    }
    
    public double[] createFeatureVector0(int player, HexTile[][] board){
        double[] inputVector = new double[2];
        switch (player){
            case HexConfig.PLAYER_ONE:
                inputVector[0] = HexUtils.getLongestChain(board, HexConfig.PLAYER_ONE);
                inputVector[1] = HexUtils.getLongestChain(board, HexConfig.PLAYER_TWO);
                break;
            case HexConfig.PLAYER_TWO:
                inputVector[0] = HexUtils.getLongestChain(board, HexConfig.PLAYER_TWO);
                inputVector[1] = HexUtils.getLongestChain(board, HexConfig.PLAYER_ONE);
                break;
        }

        //Normalize to range of 0-1
        inputVector[0] /= HexConfig.BOARD_SIZE;
        inputVector[1] /= HexConfig.BOARD_SIZE;

        System.out.println(stringRepr(inputVector));
        return inputVector;
    }

    public double[] createFeatureVector1(int player, HexTile[][] board){
        double[] inputVector = new double[HexConfig.BOARD_SIZE*HexConfig.BOARD_SIZE];

        for (int i = 0; i<HexConfig.BOARD_SIZE; i++ ){
            for (int j = 0; j<HexConfig.BOARD_SIZE; j++ ){
                inputVector[j*HexConfig.BOARD_SIZE+i] = board[i][j].getPlayer();
            }
        }

        System.out.println(stringRepr(inputVector));
        return inputVector;
    }
}
