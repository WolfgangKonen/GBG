package controllers.MC;

/**
 * Configuration constants for {@link MCAgent}
 */
public class MCAgentConfig {
    public static final int DEFAULT_ITERATIONS = 1000; //Number of Games played for every available Action
    public static final int DEFAULT_ROLLOUTDEPTH = 20; //Number of times advance() is called for every Iteration
    public static final int DEFAULT_NUMBERAGENTS = 1; //Number of Agents for Majority Vote
    public static boolean DOCALCCERTAINTY = false; // if true, calculate several certainty measures at the
}
