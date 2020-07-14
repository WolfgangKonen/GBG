package ludiiInterface.othello.transform.boardIndices;

import java.util.Objects;

abstract class BoardIndex {
    final int[] transformIndices = {
        56, 57, 58, 59, 60, 61, 62, 63,
        48, 49, 50, 51, 52, 53, 54, 55,
        40, 41, 42, 43, 44, 45, 46, 47,
        32, 33, 34, 35, 36, 37, 38, 39,
        24, 25, 26, 27, 28, 29, 30, 31,
        16, 17, 18, 19, 20, 21, 22, 23,
        8, 9, 10, 11, 12, 13, 14, 15,
        0, 1, 2, 3, 4, 5, 6, 7
    };

    abstract int toInt();

    public boolean isValid() {
        return toInt() >= 0 && toInt() <= 63;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BoardIndex boardIndex = (BoardIndex) o;
        return toInt() == boardIndex.toInt();
    }

    @Override
    public int hashCode() {
        return Objects.hash(toInt());
    }
}