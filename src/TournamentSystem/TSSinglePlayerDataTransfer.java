package TournamentSystem;

import controllers.PlayAgent;

public class TSSinglePlayerDataTransfer {
    public TSAgent[] agent;
    public TSTimeStorage[] timeStorage;
    public boolean standardAgentSelected = false;

    public TSSinglePlayerDataTransfer(TSAgent[] agents, TSTimeStorage[] timeSt, boolean stndtASel) {
        agent = agents;
        timeStorage = timeSt;
        standardAgentSelected = stndtASel;
    }

    public PlayAgent[] getPlayAgents() {
        PlayAgent[] pa = new PlayAgent[agent.length];
        for (int i=0; i<agent.length; i++) {
            pa[i] = agent[i].getPlayAgent();
        }
        return pa;
    }
}
