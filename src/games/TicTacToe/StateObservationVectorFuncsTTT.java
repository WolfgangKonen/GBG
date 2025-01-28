package games.TicTacToe;

import games.StateObservation;
import games.StateObservationVectorFuncs;

import java.util.Arrays;

public class StateObservationVectorFuncsTTT implements StateObservationVectorFuncs {
    private final int stateObservationVectorSize = 9 + 1; // 9 for table 1 for player
    private final int numPlayers = 2;

    @Override
    public int[] getStateObservationVectorStarts() {
        return new int[stateObservationVectorSize];
    }

    @Override
    public int[] getObservationVectorRanges() {
        int[] ranges = new int[10];
        Arrays.fill(ranges, 3);
        ranges[stateObservationVectorSize - 1] = numPlayers;
        return ranges;
    }

    @Override
    public int[] getStateObservationVector(StateObservation stateObservation) {
        if (stateObservation instanceof StateObserverTTT stateObserverTTT) {
            int[][] table = stateObserverTTT.getTable();
            int[] stateObservationVector = new int[stateObservationVectorSize];
            for (int i = 0, n = 0; i < 3; i++)
                for (int j = 0; j < 3; j++, n++)
                    stateObservationVector[n] = table[i][j] + 1;

            stateObservationVector[stateObservationVectorSize -1] = stateObservation.getPlayer();
            return stateObservationVector;
        }

        throw new RuntimeException("stateObservation not of type StateObserverTTT");
    }

    @Override
    public boolean isObservationDiscrete() {
        return true;
    }
}
