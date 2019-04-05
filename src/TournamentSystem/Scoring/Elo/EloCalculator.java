package TournamentSystem.Scoring.Elo;

/**
 * This static class holds the calculation logic to determin the players new Elo's after a match.
 * Instantiate {@link EloPlayerFIDE} or {@link EloPlayerUSCF} to use this class.
 * <p>
 * This code uses parts of the following source <a href="https://bitbucket.org/marioosh/java-elo">java-elo</a>
 *
 * @author Felix Barsnick, Cologne University of Applied Sciences, 2018
 */
public class EloCalculator {
    public static final float WIN  = 1.0f;
    public static final float DRAW = 0.5f;
    public static final float LOSS = 0.0f;

    /**
     * Calculate and set the new Elo's after a match.
     * @param playerA first player
     * @param gameResult game result<p>
     *                   +1 = Player A wins<p>
     *                    0 = Tie<p>
     *                   -1 = Player B wins
     * @param playerB second player
     */
    public static void setNewElos(EloPlayer playerA, int gameResult, EloPlayer playerB) {
        double changeA, changeB;
        int gameResB;

        switch(gameResult){
            case +1: gameResB = -1; break;
            case  0: gameResB = 0; break;
            case -1: gameResB = +1; break;
            default: gameResB = 0;
        }

        changeA = computeChange(playerA, gameResult, playerB);
        changeB = computeChange(playerB, gameResB, playerA);

        // player updaten
        playerA.updateElo(changeA);
        playerB.updateElo(changeB);
    }

    private static double computeChange(EloPlayer playerA, int gameResult, EloPlayer playerB) {
        double expected = computeExpected(playerA.getEloRating(), playerB.getEloRating());
        float scoreTyp;
        switch(gameResult){
            case +1: scoreTyp = WIN; break;
            case  0: scoreTyp = DRAW; break;
            case -1: scoreTyp = LOSS; break;
            default: return 0;
        }
        return playerA.getKFactor() * (scoreTyp - expected);
    }

    private static double computeExpected(double ratingA, double ratingB) {
        return 1.0 / (1.0 + Math.pow(10, (ratingB - ratingA) / 400));
    }
}
