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

import static ludiiInterface.Util.errorDialog;
import static ludiiInterface.Util.loadFileFromDialog;

public final class GbgAsLudiiAgent extends AI {
    private PlayAgent gbgAgent;

    public GbgAsLudiiAgent() {
        friendlyName = getClass().getSimpleName();
        try {
            gbgAgent = new ArenaTrainOthello(
                "GBG vs. Ludii - Othello Arena",
                false
            ).tdAgentIO.loadGBGAgent(
                loadFileFromDialog("GBG Agenten auswählen")
            );
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
        try {
            return new LudiiMoves(context)
                .availableMoveBy(
                    gbgAction(context))
                .get();
        } catch (final RuntimeException e) {
            errorDialog(e);
            throw e;
        }
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
