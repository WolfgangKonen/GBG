package ludiiInterface.othello.transform.players;

import ludiiInterface.othello.testDoubles.LudiiContext;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public final class GbgPlayerTest {
    @Test
    public void intValue_is_0_if_GbgPlayer_is_constructed_from_a_black_LudiiPlayer() {
        assertEquals(
            0,
            new GbgPlayer(
                new LudiiPlayer(LudiiContext.forNewGame())
            ).toInt()
        );
    }

    @Test
    public void intValue_is_1_if_GbgPlayer_is_constructed_from_a_white_LudiiPlayer() {
        assertEquals(
            1,
            new GbgPlayer(
                new LudiiPlayer(LudiiContext.fromMoves(List.of(43)))
            ).toInt()
        );
    }
}
