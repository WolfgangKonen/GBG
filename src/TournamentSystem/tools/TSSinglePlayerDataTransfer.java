package TournamentSystem.tools;

import TournamentSystem.TSAgent;
import TournamentSystem.TSAgentManager;
import TournamentSystem.TSTimeStorage;
import controllers.PlayAgent;

/**
 * This class stores all the measurement data required for single player tournament.
 * An instance of this class gets passed from {@link TSAgentManager} to {@link games.Arena#PlayGame(TSSinglePlayerDataTransfer)}
 * to enable the special single player tournament gamemode.
 * <p>
 * This class is called in {@link TSAgentManager} and provides the data for a single player tournament.
 *
 * @author Felix Barsnick, University of Applied Sciences Cologne, 2018
 */
public class TSSinglePlayerDataTransfer {
    public TSAgent[] agent;
    public TSTimeStorage[] timeStorage;
    public final boolean standardAgentSelected;
    public final int numberOfRandomStartMoves;

    private final String TAG = "[TSSinglePlayerDataTransfer] ";

    /**
     * Create an instance of this class to run a single player tournament game and provide all data needed to the Arena.
     * @param agents array with just one element containing the playing agent
     * @param timeSt array with two elements, first containing the playing agents timestorage
     * @param numberOfRandomStartMoves number of random start moves set in settings
     */
    public TSSinglePlayerDataTransfer(TSAgent[] agents, TSTimeStorage[] timeSt, int numberOfRandomStartMoves) {
        agent = agents;
        timeStorage = timeSt;
        standardAgentSelected = !agent[0].isHddAgent();
        this.numberOfRandomStartMoves = numberOfRandomStartMoves;
        //System.out.println(TAG+"lengths agent:"+agent.length+" timeS:"+timeStorage.length);
    }

    /**
     * get {@link PlayAgent}s from {@link TSAgent}s
     * @return array with {@link PlayAgent}s
     */
    public PlayAgent[] getPlayAgents() {
        PlayAgent[] pa = new PlayAgent[agent.length];
        for (int i=0; i<agent.length; i++) {
            pa[i] = agent[i].getPlayAgent();
        }
        return pa;
    }
}
