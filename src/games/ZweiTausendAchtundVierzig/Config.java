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

    public static final int NUMBEREVALUATIONS = 100;

    //Heuristik Values
    public static final double EMPTYTILEMULTIPLIER = 0.005;
    public static final double HIGHESTTILEINCORENERMULTIPLIER = 1;
    public static final double ROWMULTIPLIER = 1;
    public static final double MERGEMULTIPLIER = 0.2;

    //Bestrafung
    //Value from -1 to 0
    public static final double PENALISATION = 0;
    //Gibt an ob die unveränderte Gamescore zu der Bestrafung hinzuaddiert werden soll. Heuristiken werden bei dieser Socre nich berücksichtigt!
    public static final boolean ADDSCORE = false;

}