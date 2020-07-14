package ludiiInterface.othello.useCases.moves;

import ludiiInterface.othello.transform.boardIndices.GbgBoardIndex;
import ludiiInterface.othello.transform.boardIndices.LudiiBoardIndex;
import ludiiInterface.othello.transform.players.GbgPlayer;
import ludiiInterface.othello.transform.players.LudiiPlayer;
import util.Context;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class GbgMoves {
    private final LudiiMoves _ludiiMoves;

    public GbgMoves(final Context ludiiContext) {
        this(new LudiiMoves(ludiiContext));
    }

    public GbgMoves(final LudiiMoves ludiiMoves) {
        _ludiiMoves = ludiiMoves;
    }

    private final Collector<Map.Entry<LudiiBoardIndex, LudiiPlayer>, ?, Map<GbgBoardIndex, GbgPlayer>> asIndexPlayerMap = Collectors.toMap(
        e -> new GbgBoardIndex(e.getKey()),
        e -> new GbgPlayer(e.getValue()),
        (oldVal, newVal) -> newVal,
        LinkedHashMap::new
    );

    public Map<GbgBoardIndex, GbgPlayer> played() {
        return _ludiiMoves.played()
            .entrySet()
            .stream()
            .collect(asIndexPlayerMap);
    }
}
