package games;

import java.util.Arrays;

public class StateObservationVectorFuncs {
    private final XNTupleFuncs xnTupleFuncs;
    private final int stateObservationVectorSize;

    public StateObservationVectorFuncs(XNTupleFuncs xnTupleFuncs) {
        this.xnTupleFuncs = xnTupleFuncs;
        stateObservationVectorSize = xnTupleFuncs.getNumCells() + 1; // x cells +1 for player
    }

    public int getNumPlayers() {
        return xnTupleFuncs.getNumPlayers();
    }
    /**
     *
     * @return A flattened vector with discrete values representing the current stateObservation for SB3 Agent normally just the game table if no other features are added.
     * the contained values have to be of a distinct set of Integers.
     */
    public int[] getStateObservationVector(StateObservation stateObservation) {
        int[] bvec = xnTupleFuncs.getBoardVector(stateObservation).bvec;
        int[] stateObservationVector = Arrays.copyOf(bvec, stateObservationVectorSize);
        stateObservationVector[stateObservationVectorSize - 1] = stateObservation.getPlayer();
        return stateObservationVector;
    }

    public int[] getObservationVectorRanges() {
        int[] ranges = new int[stateObservationVectorSize];
        Arrays.fill(ranges, xnTupleFuncs.getNumPositionValues());
        ranges[stateObservationVectorSize - 1] = xnTupleFuncs.getNumPlayers();
        return ranges;
    }

    public int[] getStateObservationVectorStarts() {
        return new int[stateObservationVectorSize];
    }

    public boolean isObservationDiscrete() {
        return true;
    }

    public int getStateObservationVectorSize() {
        return stateObservationVectorSize;
    }
}
