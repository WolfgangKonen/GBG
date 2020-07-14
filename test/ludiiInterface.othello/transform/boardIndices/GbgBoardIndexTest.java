package ludiiInterface.othello.transform.boardIndices;

import com.nitorcreations.junit.runners.NestedRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import tools.Types;
import util.action.move.ActionMove;

import static org.junit.Assert.assertEquals;

@RunWith(NestedRunner.class)
public final class GbgBoardIndexTest {
    public static final class Constructed_from_a_GbgAction {
        @Test
        public void has_the_same_corresponding_index_like_the_GbgAction() {
            for (int i = 0; i <= 63; i++) {
                final var gbgIndex = new GbgBoardIndex(new Types.ACTIONS(i));
                assertEquals(gbgIndex.toInt(), i);
            }
        }
    }

    public static final class Because_GbgBoardIndices_are_mirrored_vertically_compared_to_LudiiBoardIndices {
        public static final class They_have_to_be_transformed_properly_if_a_GbgBoardIndex_is {
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
            public void constructed_from_a_LudiiBoardIndex() {
                for (int i = 0; i <= 63; i++) {
                    final var gbgIndex = new GbgBoardIndex(new LudiiBoardIndex(new ActionMove(i, i)));
                    assertEquals(gbgIndex.toInt(), ludiiIndices[i]);
                }
            }

            @Test
            public void transformed_to_a_LudiiBoardIndex() {
                for (int i = 0; i <= 63; i++) {
                    final var gbgIndex = new GbgBoardIndex(new Types.ACTIONS(i));
                    assertEquals(gbgIndex.toLudiiIndex().toInt(), ludiiIndices[i]);
                }
            }
        }
    }
}
