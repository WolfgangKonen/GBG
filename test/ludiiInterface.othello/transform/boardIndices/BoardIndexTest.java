package ludiiInterface.othello.transform.boardIndices;

import com.nitorcreations.junit.runners.NestedRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(NestedRunner.class)
public final class BoardIndexTest {
    public static final class Is_valid {
        @Test
        public void if_its_IntValue_is_in_the_range_of_0_to_63_inclusive() {
            for (int i = 0; i <= 63; i++) {
                assertTrue(
                    new BoardIndexStub(i).isValid()
                );
            }
        }
    }

    public static final class Is_not_valid {
        @Test
        public void if_its_IntValue_is_less_than_0() {
            assertFalse(
                new BoardIndexStub(-1).isValid()
            );
        }

        @Test
        public void if_its_IntValue_is_greater_than_63() {
            assertFalse(
                new BoardIndexStub(64).isValid()
            );
        }
    }

    public static final class Is_equal_to {
        @Test
        public void itself() {
            final BoardIndex boardIndex = new BoardIndexStub();
            assertEquals(boardIndex, boardIndex);
        }

        @Test
        public void another_BoardIndex_if_their_classes_and_IntValues_match() {
            assertEquals(new BoardIndexStub(2), new BoardIndexStub(2));
        }
    }

    public static final class Is_never_equal_to {
        @Test
        public void _null() {
            assertNotEquals(null, new BoardIndexStub());
        }

        @Test
        public void another_BoardIndex_of_a_different_Class() {
            assertNotEquals(new BoardIndexStub(2), new BoardIndexWithIntValue2Stub());
        }

        @Test
        public void another_BoardIndex_with_a_different_IntValue() {
            assertNotEquals(new BoardIndexStub(2), new BoardIndexStub(1));
        }
    }

    private static final class BoardIndexStub extends BoardIndex {
        private final int _boardIndex;

        BoardIndexStub() {
            this(0);
        }

        BoardIndexStub(final int boardIndex) {
            _boardIndex = boardIndex;
        }

        @Override
        int toInt() {
            return _boardIndex;
        }
    }

    private static final class BoardIndexWithIntValue2Stub extends BoardIndex {
        @Override
        int toInt() {
            return 2;
        }
    }
}