package controllers.SB3;

import agentIO.AgentLoader;
import controllers.PlayAgent;
import games.Arena;
import games.XArenaButtons;
import games.XArenaFuncs;
import tools.Types;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SB3HelperFunctions {
    /**
     * helper function for loadAgents. Checks if path of agent to load is valid.
     * @param path
     * @param arena
     * @return
     */
    private static boolean isValidLoadAgentPath(String path, Arena arena) {
        try {
            path = Types.GUI_DEFAULT_DIR_AGENT+"/"+ arena.getGameName() + "/" + path;
            return Files.exists(Paths.get(path));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Loads or fetches agents from an array with Stings. If the String is one of the following:
     * - Name of an Agent
     * - Dir of a saved agent
     * - "Self Play" adds selfPlayAgent to the returned List
     * @param agentsNameOrDir
     * @param playerNumber
     * @param arena
     * @param xArenaFuncs
     * @param xArenaButtons
     * @param selfPlayAgent
     * @return
     */
    public static List<PlayAgent> loadAgents(String[] agentsNameOrDir, int playerNumber, Arena arena, XArenaFuncs xArenaFuncs, XArenaButtons xArenaButtons, PlayAgent selfPlayAgent, int numberOfPlayers) {
        List<PlayAgent> agents = new ArrayList<PlayAgent>();

        for (String agent: agentsNameOrDir) {
            if (agent.equals("Self Play")) {
                agents.add(selfPlayAgent);
                continue;
            }
            if (isValidLoadAgentPath(agent, arena)) {
                AgentLoader agentLoader = new AgentLoader(arena, agent);
                agents.add(agentLoader.getAgent());
                continue;
            }
            agents.add(xArenaFuncs.fetchAgent(playerNumber, agent, xArenaButtons));
        }
        while (agents.size() < numberOfPlayers) {
            // fill with self play
            agents.add(selfPlayAgent);
        }
        System.out.println("Enemies loaded: ");
        for (PlayAgent enemy: agents) {
            System.out.println(enemy.getName());
        }
        return agents;
    }
}

