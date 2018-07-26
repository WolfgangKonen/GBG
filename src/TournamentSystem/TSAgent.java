package TournamentSystem;

import controllers.PlayAgent;

import javax.swing.*;

import java.io.Serializable;

import static TournamentSystem.TSAgentManager.faktorLos;
import static TournamentSystem.TSAgentManager.faktorTie;
import static TournamentSystem.TSAgentManager.faktorWin;

/**
 * This class holds the individual {@link PlayAgent}, its information and game statistics.
 * All Agents are stored in {@link TSResultStorage} and manages in {@link TSAgentManager}.
 * <p>
 * This class implements the Serializable interface to be savable to storage, except for the PlayAgent itself.
 *
 * @author Felix Barsnick, University of Applied Sciences Cologne, 2018
 */
public class TSAgent implements Serializable {
    private String name;
    private String agent;
    private boolean isHddAgent;
    private transient PlayAgent mPlayAgent;
    private int won;
    private int lost;
    private int tie;
    public JCheckBox guiCheckBox;

    public TSAgent(String name, String agent, JCheckBox checkbox, boolean hddAgent, PlayAgent playAgent) {
        this.name = name;
        this.agent = agent;
        isHddAgent = hddAgent;
        mPlayAgent = playAgent;
        guiCheckBox = checkbox;
        won = 0;
        tie = 0;
        lost = 0;
    }

    public void addWonGame(){
        won++;
    }

    public void addLostGame(){
        lost++;
    }

    public void addTieGame(){
        tie++;
    }

    public String getName(){
        return name;
    }

    public String getAgentType(){
        return agent;
    }

    public int getCountWonGames(){
        return won;
    }

    public int getCountLostGames(){
        return lost;
    }

    public int getCountTieGames(){
        return tie;
    }

    public int getCountAllGames() {
        return won + tie + lost;
    }

    public float getAgentScore() {
        float agentScore = getCountWonGames()*faktorWin+getCountTieGames()*faktorTie+getCountLostGames()*faktorLos;
        return agentScore;
    }

    public boolean isHddAgent() {
        return isHddAgent;
    }

    public PlayAgent getPlayAgent() {
        return mPlayAgent;
    }

    public String toString() {
        return "n:"+getName()+" t:"+getAgentType();
    }

}
