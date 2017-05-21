package games.Hex;

import games.StateObservation;
import games.XNTupleFuncs;

import java.util.HashSet;

public class XNTupleFuncsHex implements XNTupleFuncs {
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
    public int[] getBoardVector(StateObservation so) {
        return new int[0];
    }

    @Override
    public int[][] symmetryVectors(int[] boardVector) {
        return new int[0][];
    }

    @Override
    public int[][] fixedNTuples() {
        return new int[0][];
    }

    @Override
    public HashSet adjacencySet(int iCell) {
        return null;
    }
}
