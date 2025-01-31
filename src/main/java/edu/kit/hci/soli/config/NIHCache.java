package edu.kit.hci.soli.config;

import org.springframework.util.function.ThrowingSupplier;

/**
 * A cache implementation that updates its value after a specified duration.
 *
 * @param <T> the type of the cached value
 */
public class NIHCache<T> implements ThrowingSupplier<T> {
    private final long duration;
    private final ThrowingSupplier<T> supplier;

    private long lastWrite = 0;
    private T value;

    /**
     * Constructs a new NIHCache with the specified duration and supplier.
     *
     * @param duration the duration after which the cache should be updated
     * @param supplier the supplier to fetch the new value
     */
    public NIHCache(long duration, ThrowingSupplier<T> supplier) {
        this.duration = duration;
        this.supplier = supplier;
    }

    /**
     * Gets the cached value, updating the cache if the duration has expired.
     *
     * @return the cached value
     * @throws Exception if the cache update fails
     */
    @Override
    public T getWithException() throws Exception {
        if (System.currentTimeMillis() - lastWrite > duration) {
            synchronized (this) {
                if (System.currentTimeMillis() - lastWrite > duration) {
                    value = supplier.getWithException();
                    lastWrite = System.currentTimeMillis();
                }
            }
        }
        return value;
    }
}
