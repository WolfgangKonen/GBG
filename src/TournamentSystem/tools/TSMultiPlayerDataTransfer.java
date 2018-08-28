package TournamentSystem.tools;

import TournamentSystem.TSAgent;
import TournamentSystem.TSTimeStorage;
import games.StateObservation;

public class TSMultiPlayerDataTransfer {
    public TSAgent nextTeam[];
    public TSTimeStorage nextTimes[];
    public int rndmStartMoves;
    public StateObservation startSO;

    public TSMultiPlayerDataTransfer(TSAgent[] nextTeam, TSTimeStorage[] nextTimes, int rndmStartMoves, StateObservation startSo) {
        this.nextTeam = nextTeam;
        this.nextTimes = nextTimes;
        this.rndmStartMoves = rndmStartMoves;
        this.startSO = startSo;
    }
}
