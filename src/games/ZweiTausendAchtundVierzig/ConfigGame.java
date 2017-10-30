package games.ZweiTausendAchtundVierzig;

/**
 * Created by Johannes on 03.11.2016.
 */
public class ConfigGame {
    //General Game Settings
    public static final int ROWS = 4;
    public static final int COLUMNS = 4;
    public static final int STARTINGFIELDS = 2;
    public static final int WINNINGVALUE = 2048;

    public static final int STARTINGVALUES[] = {2,2,2,2,2,2,2,2,2,4};

    //Heuristic Values
    public static final double EMPTYTILEMULTIPLIER = 0.1;
    public static final double HIGHESTTILEINCORENERMULTIPLIER = 0.7;
    public static final double ROWMULTIPLIER = 0.7;
    public static final double MERGEMULTIPLIER = 0.2;

    //Penalisation: value from -1 to 0
    //(not used anymore, appears only in StateObserver2048Slow.getGameScore1())
    public static final double PENALISATION = -1;
    //ADDSCORE: should the game score be added to the penalisation? 
    //(not used anymore, appears only in StateObserver2048Slow.getGameScore1())
    public static final boolean ADDSCORE = true;
    
    // N-tuples:
    /**
     * =1: along the lines of [Jaskowski16] Fig 3b, 5 4-tuples, smaller LUTs (5*50e3, 2 MB agt.zip file), medium results <br>
     * =2: along the lines of [Jaskowski16] Fig 3c, 4 6-tuples, very big LUTs (4*11e6 weights, 69 MB or 93 MB agt.zip!!), 
     *     very good results
     * @see XNTupleFuncs2048#fixedNTuples()
     */
    public static final int FIXEDNTUPLEMODE = 2;
    
    // this is now parameter rewardIsGameScore in OtherParams and ParOther,
    // so REWARD_IS_EMPTYTILES is not needed anymore:
    /* public static final boolean REWARD_IS_EMPTYTILES = false;     */
}