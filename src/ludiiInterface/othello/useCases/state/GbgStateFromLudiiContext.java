package ludiiInterface.othello.useCases.state;

import games.Othello.BaseOthello;
import games.Othello.ConfigOthello;
import ludiiInterface.othello.transform.players.GbgPlayer;
import ludiiInterface.othello.useCases.moves.GbgMoves;
import ludiiInterface.othello.useCases.moves.LudiiMoves;
import tools.Types;
import util.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

final class GbgStateFromLudiiContext implements GbgState {
    private final Context _ludiiContext;

    public GbgStateFromLudiiContext(final Context ludiiContext) {
        _ludiiContext = ludiiContext;
    }

    @Override
    public void advance(final Types.ACTIONS action) {
        new LudiiMoves(_ludiiContext)
            .availableMoveBy(action)
            .ifPresent(
                move ->
                    _ludiiContext.game()
                        .apply(
                            _ludiiContext,
                            move
                        )
            );
    }

    @Override
    public int[][] toArray2D() {
        final var state = state();
        return new int[][]{
            Arrays.copyOfRange(state, 0, 8),
            Arrays.copyOfRange(state, 8, 16),
            Arrays.copyOfRange(state, 16, 24),
            Arrays.copyOfRange(state, 24, 32),
            Arrays.copyOfRange(state, 32, 40),
            Arrays.copyOfRange(state, 40, 48),
            Arrays.copyOfRange(state, 48, 56),
            Arrays.copyOfRange(state, 56, 64)
        };
    }

    @Override
    public ArrayList<Types.ACTIONS> availableActions() {
        return BaseOthello.possibleActions(toArray2D(), player().toInt());
    }

    @Override
    public GbgState copy() {
        return new GbgStateFromLudiiContext(
            new Context(_ludiiContext)
        );
    }

    @Override
    public GbgPlayer player() {
        return new GbgPlayer(_ludiiContext);
    }

    @Override
    public String stringRepresentation() {
        return Arrays.stream(state())
            .mapToObj(
                i -> i == ConfigOthello.BLACK
                    ? "O"
                    : (i == ConfigOthello.WHITE
                    ? "X"
                    : "-")
            ).collect(Collectors.joining());
    }

    private int[] state() {
        final var state = new int[64];

        Arrays.fill(state, ConfigOthello.EMPTY);

        new GbgMoves(_ludiiContext)
            .played()
            .forEach(
                (index, player) -> state[index.toInt()] = player.toInt()
            );

        return state;
    }
}
