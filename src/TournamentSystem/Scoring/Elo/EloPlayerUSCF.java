package TournamentSystem.Scoring.Elo;

import java.io.Serializable;

/**
 * This class holds the USCF implementation of the k factor determination. For the FIDE determination see {@link EloPlayerFIDE}.
 * Use the static {@link EloCalculator} class to calculate the new Elo's after a match.
 * <p>
 * This code uses parts of the following source <a href="https://bitbucket.org/marioosh/java-elo">java-elo</a>
 *
 * @author Felix Barsnick, University of Applied Sciences Cologne, 2018
 */
public class EloPlayerUSCF extends EloPlayer implements Serializable {
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
    public EloPlayerUSCF(String name) {
        super(name, EloPlayer.USCF);
    }

    /**
     * Constructor to init this class.
     * @param name name of the agent
     * @param startScore initial Elo score
     */
    public EloPlayerUSCF(String name, int startScore) {
        super(name, EloPlayer.USCF, startScore);
    }

    @Override
    public int getKFactor() {
        if (eloRating < 2100) {
            return 32;
        } else if (eloRating > 2400) {
            return 16;
        } else {
            return 24;
        }
    }

}
