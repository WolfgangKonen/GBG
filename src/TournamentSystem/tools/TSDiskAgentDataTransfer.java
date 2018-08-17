package TournamentSystem.tools;

import TournamentSystem.TSSettingsGUI2;
import agentIO.LoadSaveGBG;
import controllers.PlayAgent;

/**
 * This class is used in {@link LoadSaveGBG#loadMultipleGBGAgent()} to transfer the {@link PlayAgent} objects and
 * filenames as Strings to {@link TSSettingsGUI2}.
 *
 * @author Felix Barsnick, University of Applied Sciences Cologne, 2018
 */
public class TSDiskAgentDataTransfer {
    private String[] filenames;
    private PlayAgent[] agents;
    private int pos;

    private final String TAG = "[TSDiskAgentDataTransfer] ";

    /**
     * Create instance of this class to transfer the agent loaded from disk.
     * For every agent this class transfers the agent itself and its filename.
     * @param size number of agents loaded
     */
    public TSDiskAgentDataTransfer(int size) {
        filenames = new String[size];
        agents = new PlayAgent[size];
        pos = 0;
    }

    /**
     * add a new agent and filename
     * @param fn filename
     * @param pa playagent
     */
    public void addAgent(String fn, PlayAgent pa) {
        if (pos>=agents.length) {
            System.out.println(TAG+"ERROR :: Agent Arrays are full, size was set to: "+agents.length);
            return;
        }

        filenames[pos] = fn;
        agents[pos] = pa;
        pos ++;
    }

    public String[] getAllFileNames() {
        return filenames;
    }

    public PlayAgent[] getAllPlayAgents() {
        return agents;
    }

    public String getFileName(int pos) {
        return filenames[pos];
    }

    public PlayAgent getPlayAgent(int pos) {
        return agents[pos];
    }

    public String getPlayAgentType(int pos) {
        String[] s = agents[pos].getClass().getName().split("\\.");
        //System.out.println("DEBUG :"+s[s.length-1]+"  "+Arrays.toString(s));
        return s[s.length-1];
    }

    public int getSize() {
        return agents.length;
    }
}
