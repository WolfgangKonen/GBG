package ludiiInterface.othello.transform.players;

import com.nitorcreations.junit.runners.NestedRunner;
import game.types.board.SiteType;
import ludiiInterface.othello.testDoubles.LudiiContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import util.action.state.ActionSetState;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(NestedRunner.class)
public final class LudiiPlayerTest {
    public static final class IntValue_is {
        @Test
        public void _1_if_LudiiPlayer_is_constructed_from_a_black_players_LudiiAction() {
            assertEquals(
                1,
                new LudiiPlayer(
                    new ActionSetState(SiteType.Cell, 0, 1)
                ).toInt()
            );
        }

        @Test
        public void _2_if_LudiiPlayer_is_constructed_from_a_white_players_LudiiAction() {
            assertEquals(
                2,
                new LudiiPlayer(
                    new ActionSetState(SiteType.Cell, 0, 2)
                ).toInt()
            );
        }

        @Test
        public void _1_if_LudiiPlayer_is_constructed_from_a_black_players_LudiiContext() {
            assertEquals(
                1,
                new LudiiPlayer(LudiiContext.forNewGame())
                    .toInt()
            );
        }

        @Test
        public void _2_if_LudiiPlayer_is_constructed_from_a_white_players_LudiiContext() {
            assertEquals(
                2,
                new LudiiPlayer(LudiiContext.fromMoves(List.of(43)))
                    .toInt()
            );
        }
    }

    public static final class Is {
        @Test
        public void black_player_if_player_index_is_1() {
            final var ludiiPlayer = new LudiiPlayer(LudiiContext.forNewGame());

            assertTrue(
                ludiiPlayer.toInt() == 1 && ludiiPlayer.IsBlack()
            );
        }

        @Test
        public void white_player_if_player_index_is_2() {
            final var ludiiPlayer = new LudiiPlayer(LudiiContext.fromMoves(List.of(43)));

            assertTrue(
                ludiiPlayer.toInt() == 2 && ludiiPlayer.IsWhite()
            );
        }
    }
}