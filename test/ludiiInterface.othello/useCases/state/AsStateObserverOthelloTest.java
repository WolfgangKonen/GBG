package ludiiInterface.othello.useCases.state;

import com.nitorcreations.junit.runners.NestedRunner;
import games.Othello.StateObserverOthello;
import ludiiInterface.othello.testDoubles.LudiiContext;
import ludiiInterface.othello.transform.boardIndices.GbgBoardIndex;
import org.junit.Test;
import org.junit.runner.RunWith;
import tools.Types;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(NestedRunner.class)
public final class AsStateObserverOthelloTest {
    private static final List<Integer> moveIndicesToOccupyAllFields = List.of(19, 20, 21, 12, 13, 14, 6, 5, 7, 15, 23, 29, 22, 31, 30, 39, 38, 37, 47, 34, 46, 45, 43, 55, 52, 53, 63, 61, 54, 44, 62, 51, 42, 26, 3, 4, 11, 10, 1, 2, 17, 18, 9, 16, 0, 25, 59, 60, 41, 8, 33, 32, 24, 50, 40, 58, 49, 48, 57, 56);

    private static StateObserverOthello initializedStateObs() {
        return initializedStateObs(Collections.emptyList());
    }

    private static StateObserverOthello initializedStateObs(final List<Integer> initialGbgMoves) {
        final var so = new StateObserverOthello();
        initialGbgMoves
            .forEach(i -> so.advance(new Types.ACTIONS(i)));
        return so;
    }

    private static AsStateObserverOthello initializedAsStateObs() {
        return initializedAsStateObs(Collections.emptyList());
    }

    private static AsStateObserverOthello initializedAsStateObs(final List<Integer> initialGbgMoves) {
        return new AsStateObserverOthello(
            LudiiContext.fromMoves(
                initialGbgMoves
                    .stream()
                    .map(m ->
                        new GbgBoardIndex(
                            new Types.ACTIONS(m)
                        ).toLudiiIndex().toInt()
                    )
                    .collect(Collectors.toList())
            )
        );
    }

    private static StateObserverOthello advanceOneTurn(final StateObserverOthello so) {
        so.advance(
            new Types.ACTIONS(
                moveIndicesToOccupyAllFields.get(0)
            )
        );
        return so;
    }

    private static StateObserverOthello advanceSeveralTurns(final StateObserverOthello so) {
        moveIndicesToOccupyAllFields
            .stream()
            .limit(moveIndicesToOccupyAllFields.size() / 2)
            .forEach(i ->
                so.advance(
                    new Types.ACTIONS(i)
                )
            );
        return so;
    }

    private static StateObserverOthello advanceToWhenAllFieldsAreOccupied(final StateObserverOthello so) {
        moveIndicesToOccupyAllFields
            .forEach(i ->
                so.advance(
                    new Types.ACTIONS(i)
                )
            );
        return so;
    }

    public static final class The_aggregation_of_all_BoardIndices_of_the_moves_available_in_a_game {
        @Test
        public void corresponds_to_the_sequence_from_0_to_63_with_the_exception_of_27_28_35_and_36() {
            assertArrayEquals(
                List.of(
                    0, 1, 2, 3, 4, 5,
                    6, 7, 8, 9, 10, 11,
                    12, 13, 14, 15, 16, 17,
                    18, 19, 20, 21, 22, 23,
                    24, 25, 26, 29, 30, 31,
                    32, 33, 34, 37, 38, 39,
                    40, 41, 42, 43, 44, 45,
                    46, 47, 48, 49, 50, 51,
                    52, 53, 54, 55, 56, 57,
                    58, 59, 60, 61, 62, 63
                )
                    .stream()
                    .map(Types.ACTIONS::new)
                    .toArray(),
                new AsStateObserverOthello(LudiiContext.forNewGame())
                    .getAllAvailableActions()
                    .stream()
                    .sorted()
                    .toArray()
            );
        }
    }

    public static final class The_playable_turns_of_a_game_round_correspond_to_those_of_a_StateObserverOthello_instance_after {
        public static final class Been_initialized {
            @Test
            public void empty() {
                assertEquals(
                    initializedAsStateObs()
                        .getAvailableActions(),
                    initializedStateObs()
                        .getAvailableActions()
                );
            }

            @Test
            public void with_one_turn_played() {
                assertEquals(
                    initializedAsStateObs(List.of(19))
                        .getAvailableActions(),
                    initializedStateObs(List.of(19))
                        .getAvailableActions()
                );
            }

            @Test
            public void with_several_turns_played() {
                assertEquals(
                    initializedAsStateObs(moveIndicesToOccupyAllFields.subList(0, moveIndicesToOccupyAllFields.size() / 2))
                        .getAvailableActions(),
                    initializedStateObs(moveIndicesToOccupyAllFields.subList(0, moveIndicesToOccupyAllFields.size() / 2))
                        .getAvailableActions()
                );
            }

            @Test
            public void as_a_finished_game() {
                assertEquals(
                    initializedAsStateObs(moveIndicesToOccupyAllFields)
                        .getAvailableActions(),
                    initializedStateObs(moveIndicesToOccupyAllFields)
                        .getAvailableActions()
                );
            }
        }

        public static final class Advancing {
            @Test
            public void one_turn() {
                assertEquals(
                    advanceOneTurn(
                        initializedAsStateObs()
                    ).getAvailableActions(),
                    advanceOneTurn(
                        initializedStateObs()
                    ).getAvailableActions()
                );
            }

            @Test
            public void several_turns() {
                assertEquals(
                    advanceSeveralTurns(
                        initializedAsStateObs()
                    ).getAvailableActions(),
                    advanceSeveralTurns(
                        initializedStateObs()
                    ).getAvailableActions()
                );
            }

            @Test
            public void to_the_end_of_a_game() {
                assertEquals(
                    advanceToWhenAllFieldsAreOccupied(
                        initializedAsStateObs()
                    ).getAvailableActions(),
                    advanceToWhenAllFieldsAreOccupied(
                        initializedStateObs()
                    ).getAvailableActions()
                );
            }
        }
    }

    public static final class The_player_whose_turn_it_is_in_a_game_round_corresponds_to_that_of_a_StateObserverOthello_instance_after {
        public static final class Been_initialized {
            @Test
            public void empty() {
                assertEquals(
                    initializedAsStateObs()
                        .getPlayer(),
                    initializedStateObs()
                        .getPlayer()
                );
            }

            @Test
            public void with_one_turn_played() {
                assertEquals(
                    initializedAsStateObs(List.of(19))
                        .getPlayer(),
                    initializedStateObs(List.of(19))
                        .getPlayer()
                );
            }

            @Test
            public void with_several_turns_played() {
                assertEquals(
                    initializedAsStateObs(moveIndicesToOccupyAllFields.subList(0, moveIndicesToOccupyAllFields.size() / 2))
                        .getPlayer(),
                    initializedStateObs(moveIndicesToOccupyAllFields.subList(0, moveIndicesToOccupyAllFields.size() / 2))
                        .getPlayer()
                );
            }

            @Test
            public void as_a_finished_game() {
                assertEquals(
                    initializedAsStateObs(moveIndicesToOccupyAllFields)
                        .getPlayer(),
                    initializedStateObs(moveIndicesToOccupyAllFields)
                        .getPlayer()
                );
            }
        }

        public static final class Advancing {
            @Test
            public void one_turn() {
                assertEquals(
                    advanceOneTurn(
                        initializedAsStateObs()
                    ).getPlayer(),
                    advanceOneTurn(
                        initializedStateObs()
                    ).getPlayer()
                );
            }

            @Test
            public void several_turns() {
                assertEquals(
                    advanceSeveralTurns(
                        initializedAsStateObs()
                    ).getPlayer(),
                    advanceSeveralTurns(
                        initializedStateObs()
                    ).getPlayer()
                );
            }

            @Test
            public void to_the_end_of_a_game() {
                assertEquals(
                    advanceToWhenAllFieldsAreOccupied(
                        initializedAsStateObs()
                    ).getPlayer(),
                    advanceToWhenAllFieldsAreOccupied(
                        initializedStateObs()
                    ).getPlayer()
                );
            }
        }
    }

    public static final class The_current_game_state_of_a_game_round_corresponds_to_that_of_a_StateObserverOthello_instance_after {
        public static final class Been_initialized {
            @Test
            public void empty() {
                assertArrayEquals(
                    initializedAsStateObs()
                        .getCurrentGameState(),
                    initializedStateObs()
                        .getCurrentGameState()
                );
            }

            @Test
            public void with_one_turn_played() {
                assertArrayEquals(
                    initializedAsStateObs(List.of(19))
                        .getCurrentGameState(),
                    initializedStateObs(List.of(19))
                        .getCurrentGameState()
                );
            }

            @Test
            public void with_several_turns_played() {
                assertArrayEquals(
                    initializedAsStateObs(moveIndicesToOccupyAllFields.subList(0, moveIndicesToOccupyAllFields.size() / 2))
                        .getCurrentGameState(),
                    initializedStateObs(moveIndicesToOccupyAllFields.subList(0, moveIndicesToOccupyAllFields.size() / 2))
                        .getCurrentGameState()
                );
            }

            @Test
            public void as_a_finished_game() {
                assertArrayEquals(
                    initializedAsStateObs(moveIndicesToOccupyAllFields)
                        .getCurrentGameState(),
                    initializedStateObs(moveIndicesToOccupyAllFields)
                        .getCurrentGameState()
                );
            }
        }

        public static final class Advancing {
            @Test
            public void one_turn() {
                assertArrayEquals(
                    advanceOneTurn(
                        initializedAsStateObs()
                    ).getCurrentGameState(),
                    advanceOneTurn(
                        initializedStateObs()
                    ).getCurrentGameState()
                );
            }

            @Test
            public void several_turns() {
                assertArrayEquals(
                    advanceSeveralTurns(
                        initializedAsStateObs()
                    ).getCurrentGameState(),
                    advanceSeveralTurns(
                        initializedStateObs()
                    ).getCurrentGameState()
                );
            }

            @Test
            public void to_the_end_of_a_game() {
                assertArrayEquals(
                    advanceToWhenAllFieldsAreOccupied(
                        initializedAsStateObs()
                    ).getCurrentGameState(),
                    advanceToWhenAllFieldsAreOccupied(
                        initializedStateObs()
                    ).getCurrentGameState()
                );
            }
        }
    }

    public static final class The_string_representation_of_a_game_rounds_state_corresponds_to_that_of_a_StateObserverOthello_instance_after {
        public static final class Been_initialized {
            @Test
            public void empty() {
                assertEquals(
                    initializedAsStateObs()
                        .stringDescr(),
                    initializedStateObs()
                        .stringDescr()
                );
            }

            @Test
            public void with_one_turn_played() {
                assertEquals(
                    initializedAsStateObs(List.of(19))
                        .stringDescr(),
                    initializedStateObs(List.of(19))
                        .stringDescr()
                );
            }

            @Test
            public void with_several_turns_played() {
                assertEquals(
                    initializedAsStateObs(moveIndicesToOccupyAllFields.subList(0, moveIndicesToOccupyAllFields.size() / 2))
                        .stringDescr(),
                    initializedStateObs(moveIndicesToOccupyAllFields.subList(0, moveIndicesToOccupyAllFields.size() / 2))
                        .stringDescr()
                );
            }

            @Test
            public void as_a_finished_game() {
                assertEquals(
                    initializedAsStateObs(moveIndicesToOccupyAllFields)
                        .stringDescr(),
                    initializedStateObs(moveIndicesToOccupyAllFields)
                        .stringDescr()
                );
            }
        }

        public static final class Advancing {
            @Test
            public void one_turn() {
                assertEquals(
                    advanceOneTurn(
                        initializedAsStateObs()
                    ).stringDescr(),
                    advanceOneTurn(
                        initializedStateObs()
                    ).stringDescr()
                );
            }

            @Test
            public void several_turns() {
                assertEquals(
                    advanceSeveralTurns(
                        initializedAsStateObs()
                    ).stringDescr(),
                    advanceSeveralTurns(
                        initializedStateObs()
                    ).stringDescr()
                );
            }

            @Test
            public void to_the_end_of_a_game() {
                assertEquals(
                    advanceToWhenAllFieldsAreOccupied(
                        initializedAsStateObs()
                    ).stringDescr(),
                    advanceToWhenAllFieldsAreOccupied(
                        initializedStateObs()
                    ).stringDescr()
                );
            }
        }
    }

    public static final class A_game {
        public static final class Is_not_over {
            @Test
            public void immediately_after_it_started() {
                assertFalse(
                    initializedAsStateObs().isGameOver()
                );
            }

            @Test
            public void immediately_after_the_first_move_was_played() {
                assertFalse(
                    advanceOneTurn(
                        initializedAsStateObs()
                    ).isGameOver()
                );
            }

            @Test
            public void as_long_as_the_player_whose_turn_it_is_still_has_possible_moves() {
                assertFalse(
                    advanceSeveralTurns(
                        initializedAsStateObs()
                    ).isGameOver()
                );
            }
        }

        public static final class Is_over {
            @Test
            public void immediately_if_it_was_initialized_as_a_finished_game() {
                assertTrue(
                    initializedAsStateObs(moveIndicesToOccupyAllFields)
                        .isGameOver()
                );
            }

            @Test
            public void after_all_playing_fields_are_occupied() {
                assertTrue(
                    advanceToWhenAllFieldsAreOccupied(
                        initializedAsStateObs()
                    ).isGameOver()
                );
            }

            @Test
            public void after_a_player_runs_out_of_pawns() {
                final var so = initializedAsStateObs();

                List
                    .of(37, 43, 42, 29, 21, 34, 44, 45, 26, 20, 19, 18, 53, 25, 16, 11, 3, 52, 60, 12, 17, 30, 39)
                    .forEach(i ->
                        so.advance(new Types.ACTIONS(i))
                    );

                assertTrue(so.isGameOver());
            }
        }
    }
}
