package ludiiInterface.othello.testDoubles;

import ludiiInterface.othello.transform.boardIndices.LudiiBoardIndex;
import ludiiInterface.othello.useCases.moves.LudiiMoves;
import player.utils.loading.GameLoader;
import util.Context;
import util.Move;
import util.Trial;
import util.action.move.ActionMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LudiiContext {
    private static final String _gameName = "Reversi.lud";

    public static Context fromMoves(final List<Integer> ludiiIndices) {
        final var context = newGame();
        context.game().start(context);

        ludiiIndices.forEach(i -> tryApplyMoveTo(context, i));

        return context;
    }

    private static Context newGame() {
        final var game = GameLoader.loadGameFromName(_gameName);
        return new Context(game, new Trial(game));
    }

    public static Context forNewGame() {
        return fromMoves(new ArrayList<>());
    }

    private static void tryApplyMoveTo(final Context context, final int toIndex) {
        moveToIndex(context, toIndex)
            .ifPresent(
                move -> context.game().apply(context, move)
            );
    }

    private static Optional<Move> moveToIndex(final Context context, final int toIndex) {
        return new LudiiMoves(context)
            .availableMoveBy(
                new LudiiBoardIndex(new ActionMove(toIndex, toIndex))
            );
    }
}