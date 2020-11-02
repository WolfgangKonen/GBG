package controllers.MCTSWrapper.utils;

import java.util.function.Supplier;

/**
 * Provides support for delayed initialization.
 *
 * @param <T> The lazy evaluated value's type.
 */
public final class Lazy<T> {
    private final Supplier<T> valueSupplier;

    private boolean valueLoaded;
    private T value;

    /**
     * @param valueSupplier A supplier function that evaluates to a value of type T.
     */
    public Lazy(final Supplier<T> valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    public T value() {
        if (!valueLoaded) {
            value = valueSupplier.get();
            valueLoaded = true;
        }
        return value;
    }
}
