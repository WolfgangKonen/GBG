package ludiiInterface.othello.testDoubles;

import game.rules.play.moves.Moves;
import util.Context;
import player.utils.loading.GameLoader;
import util.Move;
import util.Trial;
import util.action.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class LudiiContext {
    private static final String _gameName = "Reversi.lud";

    public static Context fromMoves(final List<Integer> ludiiIndices) {
        final var game = GameLoader.loadGameFromName(_gameName);
        final var context = new Context(game, new Trial(game));
        game.start(context);

        ludiiIndices.stream().forEach(i -> game.apply(context, moveToIndex(game.moves(context), i)));

        return context;
    }

    public static Context forNewGame() {
        return fromMoves(new ArrayList<>());
    }

    private static Move moveToIndex(final Moves moves, final int toIndex) {
        return Arrays
            .stream(
                moves.moves().toArray(new Move[0])
            ).filter(m -> actionsFor(m).filter(a -> a.locnB().get(0) == toIndex).count() > 0).findFirst().get();
    }

    private static Stream<Action> actionsFor(final Move move) {
        return move.actions()
            .stream()
            .flatMap(
                a -> a instanceof Move
                    ? actionsFor((Move) a)
                    : Arrays.stream(new Action[]{a})
            );
    }
}