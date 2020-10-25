package controllers.MCTSWrapper.utils;

/**
 * Represents a simple tuple with two values.
 *
 * @param <T1> The first value's type.
 * @param <T2> The second value's type.
 */
public final class Tuple<T1, T2> {
    public final T1 element1;
    public final T2 element2;

    public Tuple(final T1 element1, final T2 element2) {
        this.element1 = element1;
        this.element2 = element2;
    }
}
