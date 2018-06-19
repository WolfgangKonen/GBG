package TournamentSystem;

import javax.swing.*;
import java.util.ArrayList;

public class TSAgentManager {
    public ArrayList<TSAgent> mAgents;
    private boolean lockedToCompete = false;
    private final String TAG = "[TSAgentManager] ";

    private int gamePlan[][] = null; // [ numGames ],[ [IDAgent1],[IDAgent2] ]
    private int gameResult[][] = null; // [ numGames ],[ [winAgent1],[tie],[winAgent2] ]
    public TSTimeStorage timeStorage[][] = null;
    private int nextGame = 0;
    private int numberOfGames = -1;

    public TSAgentManager() {
        mAgents = new ArrayList<>();
    }

    public void setNumberOfGames(int num) {
        numberOfGames = num;
    }

    public void addAgent(String name, String agent, JCheckBox checkbox) {
        if (!lockedToCompete)
            mAgents.add(new TSAgent(name, agent, checkbox));
        else
            System.out.println(TAG+"ERROR :: manager is locked to compete, can not add new agent");
    }

    public int getNumAgents() {
        return mAgents.size();
    }

    public int getNumAgentsSelected() {
        int num = 0;
        for (TSAgent agent : mAgents)
        {
            if(agent.guiCheckBox.isSelected())
            {
                num++;
            }
        }
        return num;
    }

    public String[] getNamesAgentsSelected() {
        String selectedAGents[] = new String[getNumAgentsSelected()]; // just selected agents
        int tmp = 0;
        for (TSAgent agent : mAgents) {
            if (agent.guiCheckBox.isSelected()) {
                selectedAGents[tmp++] = agent.getAgentType();
            }
        }
        return selectedAGents;
    }

    public String[][] getGamePlan() {
        int internalGamePlan[][] = generateGamePlanInternal();
        String gamePlan[][] = new String[internalGamePlan.length][2]; // games to be played

        for (int i=0; i<internalGamePlan.length; i++) {
            gamePlan[i][0] = mAgents.get(internalGamePlan[i][0]).getName();
            gamePlan[i][1] = mAgents.get(internalGamePlan[i][1]).getName();
        }
        return gamePlan;
    }

    private int[] getIDAgentsSelected() {
        int selectedAgents[] = new int[getNumAgentsSelected()]; // just selected agents
        int tmp = 0;
        for (int i=0; i<mAgents.size(); i++) {
            if (mAgents.get(i).guiCheckBox.isSelected()) {
                selectedAgents[tmp++] = i;
            }
        }
        return selectedAgents;
    }

    private int[][] generateGamePlanInternal() {
        int selectedAgents[] = getIDAgentsSelected();
        int gamePlan[][] = new int[getNumAgentsSelected()*(getNumAgentsSelected()-1)][2]; // games to be played
        int tmpGame = 0;
        for (int i=0; i<getNumAgentsSelected(); i++) {
            for (int j=0; j<getNumAgentsSelected(); j++) {
                if (i!=j) { // avoid agent to play against itself
                    gamePlan[tmpGame][0] = selectedAgents[i];
                    gamePlan[tmpGame++][1] = selectedAgents[j];
                }
            }
        }
        return gamePlan;
    }

    public void printGamePlan() {
        String gamePlan[][] = getGamePlan();
        System.out.println(TAG+"+ GamePlan Info: +");
        System.out.println(TAG+"Games to play: "+gamePlan.length);
        System.out.println(TAG+"each Game is run "+numberOfGames+" time(s)");
        for (String round[] : gamePlan)
            System.out.println(TAG+"["+round[0]+"] vs ["+round[1]+"]");
        System.out.println(TAG+"+ End Info +");
    }

    public TSAgent getAgent(String name) {
        for (TSAgent agnt : mAgents)
            if (agnt.getName().equals(name))
                return agnt;
        return null;
    }

    public boolean isLockedToCompete() {
        return lockedToCompete;
    }

    public void lockToCompete() {
        if (numberOfGames == -1) {
            System.out.println(TAG+"ERROR :: number of games was not set! using 1");
            numberOfGames = 1;
        }
        lockedToCompete = true;
        gamePlan = generateGamePlanInternal();
        gameResult = new int[gamePlan.length][3]; // is initialized with all zeros by JDK
        timeStorage = new TSTimeStorage[gamePlan.length][2];
        for (TSTimeStorage t[] : timeStorage) { // initialize all positions
            t[0] = new TSTimeStorage();
            t[1] = new TSTimeStorage();
        }
        nextGame = 0;
    }

    public TSAgent[] getNextCompetitionTeam() {
        TSAgent out[] = {mAgents.get(gamePlan[nextGame][0]), mAgents.get(gamePlan[nextGame][1])};
        return out;
    }

    public TSTimeStorage[] getNextCompetitionTimeStorage() {
        return timeStorage[nextGame];
    }

    public void enterGameResultWinner(int type) {
        if (!lockedToCompete) {
            System.out.println(TAG+"ERROR :: manager ist not locked, cannot enter result. run lockToCompete() first");
            return;
        }
        if (type<0 || type>2) {
            System.out.println(TAG + "ERROR :: enterGameResultWinner(int type) wrong value for type [0;2] = " + type);
            return;
        }
        else {
            gameResult[nextGame][type] = gameResult[nextGame][type] + 1;

            TSAgent teamPlayed[] = getNextCompetitionTeam(); // save individual win or loss to the tsagent objects in magents list
            if (type == 0){
                teamPlayed[0].addWonGame();
                teamPlayed[1].addLostGame();
            }
            if (type == 2){
                teamPlayed[0].addLostGame();
                teamPlayed[1].addWonGame();
            }
        }

        if (gameResult[nextGame][0]+gameResult[nextGame][1]+gameResult[nextGame][2] == numberOfGames)
            nextGame++;
    }

    public boolean hastNextGame() {
        if (nextGame==gamePlan.length)
            return false;
        else
            return true;
    }

    public void printGameResults() {
        if (gamePlan.length != gameResult.length) {
            System.out.println(TAG+"printGameResults() failed - gamePlan.length != gameResult.length");
            return;
        }
        System.out.println(TAG+"Info on individual games:");
        for (int i=0; i<gamePlan.length; i++) {
            System.out.print(TAG);
            System.out.print("Team: ");
            //System.out.print("["+gamePlan[i][0]+"] vs ["+gamePlan[i][1]+"] || ");
            System.out.print("["+mAgents.get(gamePlan[i][0]).getName()+"] vs ["+mAgents.get(gamePlan[i][1]).getName()+"] || ");
            System.out.print("Res.: Win1: "+gameResult[i][0]+" Tie: "+gameResult[i][1]+" Win2: "+gameResult[i][2]+" || ");
            System.out.print("Agt.1 average Time MS: "+timeStorage[i][0].getAverageTimeForGameMS()+" ");
            System.out.print("Agt.2 average Time MS: "+timeStorage[i][1].getAverageTimeForGameMS()+" ");
            System.out.print("");
            System.out.println();
        }
        System.out.println(TAG+"Info on individual Agents:");
        int[] selectedAgents = getIDAgentsSelected();
        for (int id : selectedAgents) {
            TSAgent a = mAgents.get(id);
            System.out.print(TAG);
            System.out.print("AgentName: "+a.getName()+" ");
            System.out.print("GamesWon: "+a.getCountWonGames()+" GamesLost: "+a.getCountLostGames());
            System.out.println();
        }
    }

    public void unlockAndClear() {
        lockedToCompete = false;
        gamePlan = null;
        gameResult = null;
        timeStorage = null;
        nextGame = 0;
    }

}
