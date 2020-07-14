package ludiiInterface.othello.useCases.moves;

import ludiiInterface.othello.transform.boardIndices.GbgBoardIndex;
import ludiiInterface.othello.transform.boardIndices.LudiiBoardIndex;
import ludiiInterface.othello.transform.players.LudiiPlayer;
import tools.Types;
import util.Context;
import util.Move;
import util.action.Action;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LudiiMoves {
    private final Context _ludiiContext;

    private final Collector<Action, ?, Map<LudiiBoardIndex, LudiiPlayer>> asIndexPlayerMap = Collectors.toMap(
        LudiiBoardIndex::new,
        LudiiPlayer::new,
        (oldVal, newVal) -> newVal,
        LinkedHashMap::new
    );

    private final Predicate<Action> isValidAction = a ->
        new LudiiBoardIndex(a).isValid()
            && new LudiiPlayer(a).isValid();

    public LudiiMoves(final Context ludiiContext) {
        _ludiiContext = ludiiContext;
    }

    public Map<LudiiBoardIndex, LudiiPlayer> played() {
        return _ludiiContext.trial().moves()
            .stream()
            .flatMap(this::actionsFor)
            .filter(isValidAction)
            .collect(asIndexPlayerMap);
    }

    private Stream<Action> actionsFor(final Move move) {
        return move.actions()
            .stream()
            .flatMap(
                a -> a instanceof Move
                    ? actionsFor((Move) a)
                    : Arrays.stream(new Action[]{a})
            );
    }

    public Optional<Move> availableMoveBy(final Types.ACTIONS gbgAction) {
        return availableMoveBy(new GbgBoardIndex(gbgAction));
    }

    public Optional<Move> availableMoveBy(final GbgBoardIndex gbgIndex) {
        return availableMoveBy(gbgIndex.toLudiiIndex());
    }

    public Optional<Move> availableMoveBy(final LudiiBoardIndex ludiiIndex) {
        return available()
            .filter(m -> moveCorrespondsToLudiiIndex(m, ludiiIndex))
            .findFirst();
    }

    private Stream<Move> available() {
        return Arrays.stream(
            _ludiiContext.game()
                .moves(_ludiiContext).moves()
                .toArray(new Move[]{})
        );
    }

    private boolean moveCorrespondsToLudiiIndex(final Move move, final LudiiBoardIndex ludiiIndex) {
        return actionsFor(move)
            .anyMatch(a ->
                ludiiIndex.equals(
                    new LudiiBoardIndex(a)
                )
            );
    }
}
