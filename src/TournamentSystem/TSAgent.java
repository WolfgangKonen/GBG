package TournamentSystem;

import javax.swing.*;

public class TSAgent {
    private String name;
    private String agent;
    private int won;
    private int lost;
    private int tie;
    public JCheckBox guiCheckBox;

    public TSAgent(String name, String agent, JCheckBox checkbox) {
        this.name = name;
        this.agent = agent;
        guiCheckBox = checkbox;
        won = 0;
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

}
