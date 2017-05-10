package controllers.MC;

/**
 * Created by Johannes on 09.12.2016.
 */
public class MCAgentConfig {
    public static final int ITERATIONS = 1000; //Number of Games played for every available Action
    public static final int ROLLOUTDEPTH = 20; //Number of times advance() is called for every Iteration
    public static final int NUMBERAGENTS = 1; //Number of Agents for Majority Vote
    public static boolean DOCALCCERTAINTY = false; // if true, calculate several certainty measures at the
}
