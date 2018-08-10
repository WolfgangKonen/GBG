package TournamentSystem.Scoring.Elo;

import java.io.Serializable;

/**
 * This abstract class holds most of the logic to save and update the Elo score of an agent.
 * The FIDE and USCF use different methods to determine the players k factor, which are implemented
 * in {@link EloPlayerFIDE} and {@link EloPlayerUSCF}.
 * Use the static {@link EloCalculator} class to calculate the new Elo's after a match.
 * <p>
 * This code uses parts of the following source <a href="https://bitbucket.org/marioosh/java-elo">java-elo</a>
 *
 * @author Felix Barsnick, University of Applied Sciences Cologne, 2018
 */
public abstract class EloPlayer implements Serializable {
    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .tsr.zip will become unreadable or you have
     * to provide a special version transformation)
     */
    private static final long serialVersionUID = 1L;

    public static final int startScore = 1500;
    public static final String FIDE = "FIDE";
    public static final String USCF = "USCF";

    private String namePlayer;
    int eloRating;
    int numGamesPlayed;
    private String calcTyp;

    /**
     * Constructor to init this class. The initial Elo is set to 1500 via {@link EloPlayer#startScore}.
     * @param name name of the agent
     * @param typ type of k factor implementation
     */
    public EloPlayer(String name, String typ) {
        this(name, typ,  startScore);
    }

    /**
     * Constructor to init this class.
     * @param name name of the agent
     * @param typ type of k factor implementation ({@link EloPlayer#FIDE} or {@link EloPlayer#USCF})
     * @param startScore initial Elo score
     */
    public EloPlayer(String name, String typ, int startScore) {
        namePlayer = name;
        calcTyp = typ;
        eloRating = startScore;
        numGamesPlayed = 0;
    }

    public int getEloRating() {
        return eloRating;
    }
    /*
    public void setEloRating(int newElo) {
        eloRating = newElo;
    }
    */

    /**
     * Update the agents Elo rating after a match.
     * @param diff difference between old and new Elo
     */
    public void updateElo(double diff) {
        eloRating += diff;
        numGamesPlayed++;
    }

    /**
     * K factor to calculate the Elo score
     * @return k factor
     */
    public abstract int getKFactor();

    public String toString() {
        return "PlayerName: "+namePlayer+" | EloScore: "+eloRating+" | NumGamesPlayed: "+numGamesPlayed+" | KCalcTyp: "+calcTyp;
    }
}
