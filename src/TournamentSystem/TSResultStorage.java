package TournamentSystem;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class saves all measurements and data of a tournament. It is Serializable to allow the persisting on disk.
 * This class gets instantiated in {@link TSAgentManager}.
 *
 * @author Felix Barsnick, University of Applied Sciences Cologne, 2018
 */
public class TSResultStorage implements Serializable {
    public ArrayList<TSAgent> mAgents;
    public boolean lockedToCompete = false;
    private final String TAG = "[TSResultStorage] ";

    public int gamePlan[][] = null; // [ numGames ],[ [IDAgent1],[IDAgent2] ]
    public int gameResult[][] = null; // [ numGames ],[ [winAgent1],[tie],[winAgent2] ]
    public TSTimeStorage timeStorage[][] = null;
    public int nextGame = 0;
    public int numberOfGames = -1;
    public boolean tournamentDone = false;

    public TSResultStorage() {
        //...
    }

    public byte getSize() {
        return 1;
    } // dummy stub (for size of agent, see LoadSaveTD.saveTDAgent)
}
