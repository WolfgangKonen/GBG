package TournamentSystem;

import agentIO.LoadSaveGBG;
import controllers.PlayAgent;

import java.util.Arrays;

/**
 * This class is used in {@link LoadSaveGBG#loadMultipleGBGAgent()} to transfer the {@link PlayAgent} objects and
 * filenames as Strings to {@link TSSettingsGUI2}.
 *
 * @author Felix Barsnick, University of Applied Sciences Cologne, 2018
 */
public class TSDiskAgentDataTransfer {
    private String[] filepaths;
    private PlayAgent[] agents;
    private int pos;

    private final String TAG = "[TSDiskAgentDataTransfer] ";

    public TSDiskAgentDataTransfer(int size) {
        filepaths = new String[size];
        agents = new PlayAgent[size];
        pos = 0;
    }

    public void addAgent(String fp, PlayAgent pa) {
        if (pos>=agents.length) {
            System.out.println(TAG+"ERROR :: Agent Arrays are full, size was set to: "+agents.length);
            return;
        }

        filepaths[pos] = fp;
        agents[pos] = pa;
        pos ++;
    }

    public String[] getAllFilepaths() {
        return filepaths;
    }

    public PlayAgent[] getAllPlayAgents() {
        return agents;
    }

    public String getFilepath(int pos) {
        return filepaths[pos];
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
