package ludiiInterface.gateway;

import controllers.PlayAgent;
import game.Game;
import games.Othello.ArenaTrainOthello;
import ludiiInterface.othello.useCases.moves.LudiiMoves;
import ludiiInterface.othello.useCases.state.AsStateObserverOthello;
import ludiiInterface.othello.useCases.state.GbgStateFromLudiiContext;
import tools.Types;
import util.AI;
import util.Context;
import util.Move;

public final class GbgAsLudiiAgent extends AI {
    private final String gbgAgentPath = "D:\\GitHub Repositories\\GBG\\agents\\Othello\\TCL3-fixed6_250k-lam05_P4_H001-diff2-FAm.agt.zip";
//    private final String gbgAgentPath = "C:\\Users\\wolfgang\\Documents\\GitHub\\GBG\\agents\\Othello\\TCL3-fixed6_250k-lam05_P4_H001-diff2-FAm.agt.zip";
    private PlayAgent gbgAgent;

    public GbgAsLudiiAgent() {
        friendlyName = getClass().getSimpleName();
    }

    @Override
    public void initAI(final Game game, final int playerID) {
        try {
            gbgAgent = new ArenaTrainOthello(
                "GBG vs. Ludii - Othello Arena",
                false
            ).tdAgentIO.loadGBGAgent(gbgAgentPath);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Move selectAction(
        final Game game,
        final Context context,
        final double maxSeconds,
        final int maxIterations,
        final int maxDepth
    ) {
        return new LudiiMoves(context)
            .availableMoveBy(
                gbgAction(context))
            .get();
    }

    private Types.ACTIONS gbgAction(final Context ludiiContext) {
        return gbgAgent
            .getNextAction2(
                new AsStateObserverOthello(
                    new GbgStateFromLudiiContext(ludiiContext)
                ),
                false,
                false
            );
    }
}
