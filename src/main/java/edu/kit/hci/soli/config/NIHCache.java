package edu.kit.hci.soli.config;

import org.springframework.util.function.ThrowingSupplier;

public class NIHCache<T> implements ThrowingSupplier<T> {
    private final long duration;
    private final ThrowingSupplier<T> supplier;

    private long lastWrite = 0;
    private T value;

    public NIHCache(long duration, ThrowingSupplier<T> supplier) {
        this.duration = duration;
        this.supplier = supplier;
    }

    /**
     * Get the element value, may update cache if duration expired.
     *
     * @return The value
     * @throws Exception if cache update failed
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
