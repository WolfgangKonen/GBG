package games.ZweiTausendAchtundVierzig;

/**
 * Created by Johannes on 04.05.2017.
 */
public class ConfigEvaluator {
    //The Evaluator that is used when clicking on Quick Evaluation
    //0 > Evaluator2048, evaluates whole games
    //1 > Evaluator2048_BoardPositions, evaluates single GameStates, used for calculating Certainty
    public static final int DEFAULTEVALUATOR = 0;


    //Evaluator2048 Settings
    public static final int NUMBEREVALUATIONS = 15;
    public static final int MINPOINTS = 10000;
    public static final boolean PLAYSTATS_CSV = true;


    //Evaluator2048_BoardPositions Settings
    public static final int NC = 20; //Number of Evaluations for a single BoardPositions
    public static final int BOARDPOSITIONS = 20; //Number of Boardpositions per availableActions/emptyTiles pair
    public static final boolean EVALUATEMC = false;
    public static final boolean EVALUATEMCTSE = true;

    //Agent Settings for MC and MCTS while using Evaluator2048_BoardPositions
    public static final int ROLLOUTDEPTH = 150;
    public static final int ITERATIONS = 1000;  //Iterations for each availableAction
    public static final int NUMBERAGENTS = 1;   //only MC Agent uses Majority Vote, MCTS Agent gets more Iterations when using multiple Agents
    public static final int TREEDEPTH = 10;

    public static final boolean GENERATENEWGAMESTATES = false; //create a new gameStates.ser
    public static final int GAMESFORNEWGAMESTATES = 20; //number of games when creating a new gameStates.ser
}
