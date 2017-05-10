package games.ZweiTausendAchtundVierzig;

/**
 * Created by Johannes on 03.11.2016.
 */
public class Config {
    public static final int ROWS = 4;
    public static final int COLUMNS = 4;
    public static final int STARTINGFIELDS = 2;
    public static final int WINNINGVALUE = 2048;

    public static final int STARTINGVALUES[] = {2,2,2,2,2,2,2,2,2,4};

    public static final int NUMBEREVALUATIONS = 100; // NC for Certainty Calculation

    //Heuristic Values
    public static final boolean ENABLEHEURISTICS = false;
    public static final double EMPTYTILEMULTIPLIER = 0.1;
    public static final double HIGHESTTILEINCORENERMULTIPLIER = 0.7;
    public static final double ROWMULTIPLIER = 0.7;
    public static final double MERGEMULTIPLIER = 0.2;

    //Bestrafung
    //Value from -1 to 0
    public static final double PENALISATION = -1;
    //Gibt an ob die unveränderte Gamescore zu der Bestrafung hinzuaddiert werden soll. Heuristiken werden bei dieser Score nich berücksichtigt!
    public static final boolean ADDSCORE = true;
}