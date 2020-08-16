package ludiiInterface.othello.useCases.moves;

import com.nitorcreations.junit.runners.NestedRunner;
import game.types.board.SiteType;
import ludiiInterface.othello.testDoubles.LudiiContext;
import ludiiInterface.othello.transform.boardIndices.GbgBoardIndex;
import ludiiInterface.othello.transform.boardIndices.LudiiBoardIndex;
import ludiiInterface.othello.transform.players.LudiiPlayer;
import org.junit.Test;
import org.junit.runner.RunWith;
import tools.Types;
import util.action.move.ActionMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(NestedRunner.class)
public final class LudiiMovesTest {
    private static boolean MoveIsFromPlayer(
        final Map.Entry<LudiiBoardIndex, LudiiPlayer> boardIndexPlayerMap,
        final int player
    ) {
        return boardIndexPlayerMap.getValue().toInt() == player;
    }

    private static boolean MoveIsToBoardIndex(
        final Map.Entry<LudiiBoardIndex, LudiiPlayer> boardIndexPlayerMap,
        final int boardIndex
    ) {
        return boardIndexPlayerMap.getKey().toInt() == boardIndex;
    }

    public static final class A_new_game_is_already_starting_with {
        @Test
        public void four_played_moves() {
            assertEquals(
                4,
                new LudiiMoves(LudiiContext.forNewGame())
                    .played().size()
            );
        }

        @Test
        public void player_1_at_position_36() {
            assertTrue(
                new LudiiMoves(LudiiContext.forNewGame())
                    .played().entrySet().stream()
                    .anyMatch(
                        m -> MoveIsFromPlayer(m, 1)
                            && MoveIsToBoardIndex(m, 36)
                    )
            );
        }

        @Test
        public void player_1_at_position_27() {
            assertTrue(
                new LudiiMoves(LudiiContext.forNewGame())
                    .played().entrySet().stream()
                    .anyMatch(
                        m -> MoveIsFromPlayer(m, 1)
                            && MoveIsToBoardIndex(m, 27)
                    )
            );
        }

        @Test
        public void player_2_at_position_35() {
            assertTrue(
                new LudiiMoves(LudiiContext.forNewGame())
                    .played().entrySet().stream()
                    .anyMatch(
                        m -> MoveIsFromPlayer(m, 2)
                            && MoveIsToBoardIndex(m, 35)
                    )
            );
        }

        @Test
        public void player_2_at_position_28() {
            assertTrue(
                new LudiiMoves(LudiiContext.forNewGame())
                    .played().entrySet().stream()
                    .anyMatch(
                        m -> MoveIsFromPlayer(m, 2)
                            && MoveIsToBoardIndex(m, 28)
                    )
            );
        }
    }

    public static final class The {
        @Test
        public void played_moves_are_sorted_chronologically() {
            final var playedMoves = new ArrayList<>(
                new LudiiMoves(LudiiContext.fromMoves(
                    List.of(43, 42)
                )).played().entrySet());

            final var secondToLastMove = playedMoves.get(4);
            assertTrue(
                MoveIsFromPlayer(secondToLastMove, 1) &&
                    MoveIsToBoardIndex(secondToLastMove, 43)
            );

            final var lastMove = playedMoves.get(5);
            assertTrue(
                MoveIsFromPlayer(lastMove, 2) &&
                    MoveIsToBoardIndex(lastMove, 42)
            );
        }

        public static final class Method_availableMoveBy_returns {
            public static final class A_value_if_there_is_an_available_move_in_this_round_that_corresponds_to_the_passed {
                @Test
                public void gbgAction() {
                    assertTrue(
                        new LudiiMoves(LudiiContext.forNewGame())
                            .availableMoveBy(
                                new Types.ACTIONS(19)
                            )
                            .isPresent()
                    );
                }

                @Test
                public void gbgBoardIndex() {
                    assertTrue(
                        new LudiiMoves(LudiiContext.forNewGame())
                            .availableMoveBy(
                                new GbgBoardIndex(new Types.ACTIONS(19))
                            )
                            .isPresent()
                    );
                }

                @Test
                public void ludiiBoardIndex() {
                    assertTrue(
                        new LudiiMoves(LudiiContext.forNewGame())
                            .availableMoveBy(
                                new LudiiBoardIndex(new ActionMove(SiteType.Cell, -1, -1, SiteType.Cell, 43, -1, -1, -1, false))
                            )
                            .isPresent()
                    );
                }
            }

            public static final class No_value_if_there_is_not_an_available_move_in_this_round_that_corresponds_to_the_passed {
                @Test
                public void gbgAction() {
                    assertFalse(
                        new LudiiMoves(LudiiContext.forNewGame())
                            .availableMoveBy(
                                new Types.ACTIONS(11)
                            )
                            .isPresent()
                    );
                }

                @Test
                public void gbgBoardIndex() {
                    assertFalse(
                        new LudiiMoves(LudiiContext.forNewGame())
                            .availableMoveBy(
                                new GbgBoardIndex(new Types.ACTIONS(11))
                            )
                            .isPresent()
                    );
                }

                @Test
                public void ludiiBoardIndex() {
                    assertFalse(
                        new LudiiMoves(LudiiContext.forNewGame())
                            .availableMoveBy(
                                new LudiiBoardIndex(new ActionMove(SiteType.Cell, -1, -1, SiteType.Cell, 51, -1, -1, -1, false))
                            )
                            .isPresent()
                    );
                }
            }
        }
    }
}
