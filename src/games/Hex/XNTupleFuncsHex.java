package games.Hex;

import games.StateObservation;
import games.XNTupleFuncs;

import javax.swing.plaf.nimbus.State;
import java.io.Serializable;
import java.util.HashSet;

public class XNTupleFuncsHex implements XNTupleFuncs, Serializable {

    private enum Axis{
        HORIZONTAL, VERTICAL
    }

    @Override
    public int getNumCells() {
        return HexConfig.BOARD_SIZE*HexConfig.BOARD_SIZE;
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
    public int[] getBoardVector(StateObservation so) {
        StateObserverHex stateObs = (StateObserverHex) so;
        HexTile[] boardVectorTiles = HexUtils.boardToVector(stateObs.getBoard());
        int[] boardVectorInt = new int[boardVectorTiles.length];

        for (int i=0; i<boardVectorInt.length; i++){
            boardVectorInt[i] = boardVectorTiles[i].getPlayer()+1;
        }

        return boardVectorInt;
    }

    @Override
    public int[][] symmetryVectors(int[] boardVector) {
        int[][] symmetries = new int[4][];
        symmetries[0] = boardVector;
        symmetries[1] = mirrorBoard(boardVector, Axis.HORIZONTAL);
        symmetries[2] = mirrorBoard(boardVector, Axis.VERTICAL);
        symmetries[3] = rotateBoard(boardVector);
        return symmetries;
    }

    @Override
    public int[][] fixedNTuples() {
        //TODO
        return new int[0][];
    }

    @Override
    public HashSet adjacencySet(int iCell) {
        //Each cell has a max of 6 neighbors
        //Coordinates of those relative to current cell:
        //-1,0; -1,1; 0,-1; 0,1; 1,-1; 1,0 (x,y)
        HashSet<Integer> adjacencySet = new HashSet<>();

        int x = (int) Math.floor(iCell/HexConfig.BOARD_SIZE);
        int y = iCell % HexConfig.BOARD_SIZE;

        for (int i = -1; i<=1; i++){
            for (int j = -1; j<=1; j++){
                int neighborX = x+i;
                int neighborY = y+j;
                //Not all of those exist if the cell is next to an edge, so check for validity
                if (i != j && HexUtils.isValidTile(neighborX, neighborY)){
                    //Transform from 2D cell coordinates back to linear representation and add to HashSet
                    adjacencySet.add(neighborX*HexConfig.BOARD_SIZE+neighborY);
                }
            }
        }

        return adjacencySet;
    }

    private int[] mirrorBoard(int[] boardVector, Axis axis){
        int[] mirroredVector = boardVector.clone();

        if (axis == Axis.VERTICAL){
            //Subdivide into chunks of BOARD_SIZE elements and reverse each
            //Example for BOARD_SIZE=3:
            //Before: 1,2,3, 4,5,6, 7,8,9
            //After:  3,2,1, 6,5,4, 9,8,7
            for (int i=0; i< HexConfig.BOARD_SIZE; i++){
                int[] tmp = new int[HexConfig.BOARD_SIZE];
                for (int j=0; j < ((HexConfig.BOARD_SIZE+1)/2); j++){
                    tmp[j] = mirroredVector[i*HexConfig.BOARD_SIZE+j];
                    mirroredVector[i*HexConfig.BOARD_SIZE+j] = mirroredVector[i*HexConfig.BOARD_SIZE+HexConfig.BOARD_SIZE-j-1];
                    mirroredVector[i*HexConfig.BOARD_SIZE+HexConfig.BOARD_SIZE-j-1] = tmp[j];
                }
            }
        } else if (axis == Axis.HORIZONTAL){
            //Swap the places of chunks of BOARD_SIZE elements from front and end until center is reached
            //Example for BOARD_SIZE=3:
            //Before: 1,2,3, 4,5,6, 7,8,9
            //After:  7,8,9, 4,5,6, 1,2,3
            for (int i=0; i < ((HexConfig.BOARD_SIZE+1)/2); i++){
                int[] tmp = new int[HexConfig.BOARD_SIZE];
                for (int j=0; j<HexConfig.BOARD_SIZE; j++){
                    tmp[j] = mirroredVector[i*HexConfig.BOARD_SIZE+j];
                    mirroredVector[i*HexConfig.BOARD_SIZE+j] = mirroredVector[(HexConfig.BOARD_SIZE-i-1)*HexConfig.BOARD_SIZE+j];
                    mirroredVector[(HexConfig.BOARD_SIZE-i-1)*HexConfig.BOARD_SIZE+j] = tmp[j];
                }
            }
        }

        return mirroredVector;
    }

    private int[] rotateBoard(int[] boardVector){
        int[] rotatedBoard = boardVector.clone();

        //Rotating by 180 degrees is the same as mirroring by both axes
        //Rotating by 90 or 270 degrees would not be an equivalent board in Hex
        rotatedBoard = mirrorBoard(rotatedBoard, Axis.HORIZONTAL);
        rotatedBoard = mirrorBoard(rotatedBoard, Axis.HORIZONTAL);

        return rotatedBoard;
    }
}
