package ludiiInterface.othello.transform.boardIndices;

import com.nitorcreations.junit.runners.NestedRunner;
import game.types.board.SiteType;
import org.junit.Test;
import org.junit.runner.RunWith;
import tools.Types;
import util.action.move.ActionMove;

import static org.junit.Assert.assertEquals;

@RunWith(NestedRunner.class)
public final class LudiiBoardIndexTest {
    public static final class Constructed_from_a_LudiiAction {
        @Test
        public void has_the_same_corresponding_index_like_the_LudiiAction() {
            for (int i = 0; i <= 63; i++) {
                final var ludiiIndex = new LudiiBoardIndex(new ActionMove(SiteType.Cell, -1, -1, SiteType.Cell, i, -1, -1, -1, false));
                assertEquals(ludiiIndex.toInt(), i);
            }
        }
    }

    public static final class Because_LudiiBoardIndices_are_mirrored_vertically_compared_to_GbgBoardIndices {
        public static final class They_have_to_be_transformed_properly_if_a_LudiiBoardIndex_is {
            private final int[] ludiiIndices = {
                56, 57, 58, 59, 60, 61, 62, 63,
                48, 49, 50, 51, 52, 53, 54, 55,
                40, 41, 42, 43, 44, 45, 46, 47,
                32, 33, 34, 35, 36, 37, 38, 39,
                24, 25, 26, 27, 28, 29, 30, 31,
                16, 17, 18, 19, 20, 21, 22, 23,
                8, 9, 10, 11, 12, 13, 14, 15,
                0, 1, 2, 3, 4, 5, 6, 7,
            };

            @Test
            public void constructed_from_a_GbgBoardIndex() {
                for (int i = 0; i <= 63; i++) {
                    final var ludiiIndex = new LudiiBoardIndex(new GbgBoardIndex(new Types.ACTIONS(i)));
                    assertEquals(ludiiIndex.toInt(), ludiiIndices[i]);
                }
            }

            @Test
            public void transformed_to_a_GbgBoardIndex() {
                for (int i = 0; i <= 63; i++) {
                    final var ludiiIndex = new LudiiBoardIndex(new ActionMove(SiteType.Cell, -1, -1, SiteType.Cell, i, -1, -1, -1, false));
                    assertEquals(ludiiIndex.toGbgIndex().toInt(), ludiiIndices[i]);
                }
            }
        }
    }
}
