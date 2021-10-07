package games.Yavalath;

import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

import java.io.Serializable;
import java.util.HashSet;

public class XNTupleFuncsYavalath extends XNTupleBase implements XNTupleFuncs, Serializable {

    public XNTupleFuncsYavalath(){

    }

    @Override
    public int getNumCells() {
        return 0;
    }

    @Override
    public int getNumPositionValues() {
        return 0;
    }

    @Override
    public int getNumPlayers() {
        return 0;
    }

    @Override
    public int getNumSymmetries() {
        return 0;
    }

    @Override
    public BoardVector getBoardVector(StateObservation so) {
        return null;
    }

    @Override
    public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
        return new BoardVector[0];
    }

    @Override
    public int[] symmetryActions(int actionKey) {
        return new int[0];
    }

    @Override
    public int[][] fixedNTuples(int mode) {
        return new int[0][];
    }

    //Needed so the arena can run, replace if you know what you are doing later
    @Override
    public String fixedTooltipString() {
        return "<html>"
                +"1"
                +"2"
                +"</html>";
    }

    //Needed so the arena can run, replace if you know what you are doing later
    @Override
    public int[] fixedNTupleModesAvailable() {
        return new int[]{1,2};
    }

    @Override
    public HashSet adjacencySet(int iCell) {
        return null;
    }
}
