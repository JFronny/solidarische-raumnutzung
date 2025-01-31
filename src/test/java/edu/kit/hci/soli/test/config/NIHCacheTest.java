package edu.kit.hci.soli.test.config;

import edu.kit.hci.soli.config.NIHCache;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NIHCacheTest {
    @Test
    public void testCache() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        // we probably won't spend more than 100ms in interrupts, right?
        NIHCache<Integer> cache = new NIHCache<>(100, counter::incrementAndGet);
        assertEquals(1, cache.getWithException());
        assertEquals(1, cache.getWithException());
        assertEquals(1, cache.getWithException());
        Thread.sleep(100);
        assertEquals(2, cache.getWithException());
        assertEquals(2, cache.getWithException());
    }
}
