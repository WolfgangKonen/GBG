package TournamentSystem.tools;

import TournamentSystem.TSAgent;
import TournamentSystem.TSAgentManager;
import TournamentSystem.TSTimeStorage;
import controllers.PlayAgent;
import games.StateObservation;

/**
 * This class stores all the measurement data required for single player tournament.
 * An instance of this class gets passed from {@link TSAgentManager} to {@link games.Arena#PlayGame(TSGameDataTransfer)}
 * to enable the special single player tournament gamemode.
 * <p>
 * This class is called in {@link TSAgentManager} and provides the data for a single player tournament.
 *
 * @author Felix Barsnick, Cologne University of Applied Sciences, 2018
 */
public class TSGameDataTransfer {
    public TSAgent[] nextTeam;
    public TSTimeStorage[] nextTimes;
    public final boolean standardAgentSelected;
    public final int rndmStartMoves;
    public StateObservation startSO;

    private final String TAG = "[TSGameDataTransfer] ";

    /**
     * Create an instance of this class to run a single player tournament game and provide all data needed to the Arena.
     * @param agents array with just one element containing the playing nextTeam
     * @param timeSt array with two elements, first containing the playing agents timestorage
     * @param numberOfRandomStartMoves number of random start moves set in settings
     * @param startSo selected random startstate provided by {@link TSAgentManager#getNextStartState()}
     */
    public TSGameDataTransfer(TSAgent[] agents, TSTimeStorage[] timeSt, int numberOfRandomStartMoves, StateObservation startSo) {
        nextTeam = agents;
        nextTimes = timeSt;
        standardAgentSelected = !nextTeam[0].isHddAgent();
        this.rndmStartMoves = numberOfRandomStartMoves;
        this.startSO = startSo;
        //System.out.println(TAG+"lengths nextTeam:"+nextTeam.length+" timeS:"+nextTimes.length);
    }

    /**
     * get {@link PlayAgent}s from {@link TSAgent}s
     * @return array with {@link PlayAgent}s
     */
    public PlayAgent[] getPlayAgents() {
        PlayAgent[] pa = new PlayAgent[nextTeam.length];
        for (int i = 0; i< nextTeam.length; i++) {
            pa[i] = nextTeam[i].getPlayAgent();
        }
        return pa;
    }
}
