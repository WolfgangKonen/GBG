package TournamentSystem.Scoring.Elo;

import java.io.Serializable;

/**
 * This class holds the FIDE implementation of the k factor determination. For the USCF determination see {@link EloPlayerUSCF}.
 * Use the static {@link EloCalculator} class to calculate the new Elo's after a match.
 * <p>
 * This code uses parts of the following source <a href="https://bitbucket.org/marioosh/java-elo">java-elo</a>
 *
 * @author Felix Barsnick, Cologne University of Applied Sciences, 2018
 */
public class EloPlayerFIDE extends EloPlayer implements Serializable {
    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .tsr.zip will become unreadable or you have
     * to provide a special version transformation)
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor to init this class. The initial Elo is set to 1500 via {@link EloPlayer#startScore}.
     * @param name name of the agent
     */
    public EloPlayerFIDE(String name) {
        super(name, EloPlayer.FIDE);
    }

    /**
     * Constructor to init this class.
     * @param name name of the agent
     * @param startScore initial Elo score
     */
    public EloPlayerFIDE(String name, int startScore) {
        super(name, EloPlayer.FIDE, startScore);
    }

    @Override
    public int getKFactor() {
        if (numGamesPlayed >= 30) {
            if (eloRating >= 2400) {
                // PlayerType.PRO;
                return 10;
            } else {
                // PlayerType.COMMON;
                return 15;
            }
        } else {
            // PlayerType.BEGINNER;
            return 30;
        }
    }

}
