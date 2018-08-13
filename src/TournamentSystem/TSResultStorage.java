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
    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .tsr.zip will become unreadable or you have
     * to provide a special version transformation)
     */
    private static final long serialVersionUID = 1L;

    public ArrayList<TSAgent> mAgents;
    public boolean lockedToCompete = false;
    private final String TAG = "[TSResultStorage] ";

    public int gamePlan[][] = null; // [ numGames ],[ [IDAgent1],[IDAgent2] ]
    public int gameResult[][] = null; // [ numGames ],[ [winAgent1],[tie],[winAgent2] ]
    public TSTimeStorage timeStorage[][] = null;
    public int nextGame = 0;
    public int numberOfGames = -1;
    public boolean tournamentDone = false;
    public String startDate = "Tournament Start Date: xx.xx.xxxx xx:xx:xx";

    public TSResultStorage() {
        //...
    }

    public byte getSize() {
        return 1;
    } // dummy stub (for size of agent, see LoadSaveTD.saveTDAgent)
}
