package ludiiInterface.othello.useCases.moves;

import com.nitorcreations.junit.runners.NestedRunner;
import ludiiInterface.othello.testDoubles.LudiiContext;
import ludiiInterface.othello.transform.boardIndices.GbgBoardIndex;
import ludiiInterface.othello.transform.players.GbgPlayer;
import org.junit.Test;
import org.junit.runner.RunWith;
import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(NestedRunner.class)
public final class GbgMovesTest {
    private static boolean MoveIsFromPlayer(
        final Map.Entry<GbgBoardIndex, GbgPlayer> boardIndexPlayerMap,
        final int player
    ) {
        return boardIndexPlayerMap.getValue().toInt() == player;
    }

    private static boolean MoveIsToBoardIndex(
        final Map.Entry<GbgBoardIndex, GbgPlayer> boardIndexPlayerMap,
        final int boardIndex
    ) {
        return boardIndexPlayerMap.getKey().toInt() == boardIndex;
    }

    public static final class A_new_game_is_already_starting_with {
        @Test
        public void four_played_moves() {
            assertEquals(
                4,
                new GbgMoves(LudiiContext.forNewGame())
                    .played().size()
            );
        }

        @Test
        public void player_0_at_position_28() {
            assertTrue(
                new GbgMoves(LudiiContext.forNewGame())
                    .played().entrySet().stream()
                    .anyMatch(
                        m -> MoveIsFromPlayer(m, 0)
                            && MoveIsToBoardIndex(m, 28)
                    )
            );
        }

        @Test
        public void player_0_at_position_35() {
            assertTrue(
                new GbgMoves(LudiiContext.forNewGame())
                    .played().entrySet().stream()
                    .anyMatch(
                        m -> MoveIsFromPlayer(m, 0)
                            && MoveIsToBoardIndex(m, 35)
                    )
            );
        }

        @Test
        public void player_1_at_position_27() {
            assertTrue(
                new GbgMoves(LudiiContext.forNewGame())
                    .played().entrySet().stream()
                    .anyMatch(
                        m -> MoveIsFromPlayer(m, 1)
                            && MoveIsToBoardIndex(m, 27)
                    )
            );
        }

        @Test
        public void player_1_at_position_36() {
            assertTrue(
                new GbgMoves(LudiiContext.forNewGame())
                    .played().entrySet().stream()
                    .anyMatch(
                        m -> MoveIsFromPlayer(m, 1)
                            && MoveIsToBoardIndex(m, 36)
                    )
            );
        }
    }

    public static final class The {
        private int GbgToLudiiIndex(final int gbgIndex) {
            return new GbgBoardIndex(
                new Types.ACTIONS(gbgIndex)
            ).toLudiiIndex().toInt();
        }

        @Test
        public void played_moves_are_sorted_chronologically() {
            final var playedMoves = new ArrayList<>(
                new GbgMoves(LudiiContext.fromMoves(
                    List.of(
                        GbgToLudiiIndex(19),
                        GbgToLudiiIndex(18)
                    )
                )).played().entrySet());

            final var secondToLastMove = playedMoves.get(4);
            assertTrue(
                MoveIsFromPlayer(secondToLastMove, 0) &&
                    MoveIsToBoardIndex(secondToLastMove, 19)
            );

            final var lastMove = playedMoves.get(5);
            assertTrue(
                MoveIsFromPlayer(lastMove, 1) &&
                    MoveIsToBoardIndex(lastMove, 18)
            );
        }
    }
}
