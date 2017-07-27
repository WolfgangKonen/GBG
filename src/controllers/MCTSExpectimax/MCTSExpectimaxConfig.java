package controllers.MCTSExpectimax;

/**
 * Created by Johannes on 10.06.2017.
 */
public class MCTSExpectimaxConfig {
    public static final int DEFAULT_ITERATIONS = 3500;                  //Number of Games played for every available Action
    public static final int DEFAULT_ROLLOUTDEPTH = 150;                 //Number of times advance() is called for every Iteration
    public static final int DEFAULT_TREEDEPTH = 10;
    public static final double DEFAULT_K = 1.4142135623730950488016887242097;
    public static final int DEFAULT_MAXNODES = 500;                     //Max number of nodes expand() can create
    public static final boolean DEFAULT_ALTERNATIVEVERSION = true;      //Use MCTSExpectimax1 instead of MCTSExpectimax
    public static final boolean DEFAULT_ENABLEHEURISTICS = false;
    public static final int DEFAULT_NUMAGENTS = 10;                      //number Agents for majority vote
    /**
     *  egreedyEpsilon = probability that a random action is taken (instead
     *  greedy action). This is *only* relevant, if function egreedy() is
     *  used as variant to uct() (which is currently *not* the case).
     */
    public static final double EGREEDYEPSILON = 0.05;
}
