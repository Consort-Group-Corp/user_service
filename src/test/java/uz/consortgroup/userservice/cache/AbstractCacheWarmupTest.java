package uz.consortgroup.userservice.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AbstractCacheWarmupTest {

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    private TestCacheWarmup testCacheWarmup;

    static class TestCacheWarmup extends AbstractCacheWarmup<String, Integer> {
        private final List<String> mockEntities;

        TestCacheWarmup(ThreadPoolTaskExecutor taskExecutor, List<String> mockEntities) {
            super(taskExecutor);
            this.mockEntities = mockEntities;
        }

        @Override
        protected List<String> fetchBatch(Long lastId, int batchSize) {
            if (lastId >= mockEntities.size()) {
                return Collections.emptyList();
            }
            int endIndex = Math.min(lastId.intValue() + batchSize, mockEntities.size());
            return mockEntities.subList(lastId.intValue(), endIndex);
        }

        @Override
        protected Long getLastId(List<String> entities) {
            return entities.isEmpty() ? 0L : (long) mockEntities.indexOf(entities.get(entities.size() - 1)) + 1;
        }

        @Override
        protected Integer mapToCacheEntity(String entity) {
            return Integer.parseInt(entity);
        }

        @Override
        protected void saveCache(List<Integer> cacheEntities) {

        }

        @Override
        protected String getCacheName() {
            return "TestCache";
        }

        public void warmUpCache() {
            super.warmUpCache();
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        List<String> mockData = Arrays.asList("1", "2", "3", "4", "5");
        testCacheWarmup = spy(new TestCacheWarmup(taskExecutor, mockData));
        try {
            Field batchSizeField = AbstractCacheWarmup.class.getDeclaredField("batchSize");
            batchSizeField.setAccessible(true);
            batchSizeField.set(testCacheWarmup, 2);

            Field warmupEnabledField = AbstractCacheWarmup.class.getDeclaredField("warmupEnabled");
            warmupEnabledField.setAccessible(true);
            warmupEnabledField.set(testCacheWarmup, true);

            Field warmupTimeoutField = AbstractCacheWarmup.class.getDeclaredField("warmupTimeout");
            warmupTimeoutField.setAccessible(true);
            warmupTimeoutField.set(testCacheWarmup, 5000);
        } catch (Exception e) {
            fail("Failed to set fields via reflection", e);
        }
    }

    @Test
    void testSuccessfulCacheWarmup() throws Exception {
        CompletableFuture<?> future = CompletableFuture.runAsync(testCacheWarmup::warmUpCache, taskExecutor);
        future.get(5, TimeUnit.SECONDS);

        verify(testCacheWarmup, times(4)).fetchBatch(anyLong(), eq(2));
        verify(testCacheWarmup, times(3)).saveCache(anyList());
        assertTrue(future.isDone(), "Cache warmup should complete successfully");
    }

    @Test
    void testEmptyFetchBatch() throws Exception {
        TestCacheWarmup emptyCacheWarmup = spy(new TestCacheWarmup(taskExecutor, Collections.emptyList()));
        setFields(emptyCacheWarmup, 2, true, 5000);

        CompletableFuture<?> future = CompletableFuture.runAsync(emptyCacheWarmup::warmUpCache, taskExecutor);
        future.get(5, TimeUnit.SECONDS);

        verify(emptyCacheWarmup, times(1)).fetchBatch(eq(0L), eq(2));
        verify(emptyCacheWarmup, never()).saveCache(anyList());
        assertTrue(future.isDone(), "Cache warmup should complete with empty data");
    }

    @Test
    void testCacheWarmupWithException() throws Exception {
        doThrow(new RuntimeException("Fetch error")).when(testCacheWarmup).fetchBatch(anyLong(), anyInt());

        testCacheWarmup.warmUpCache();

        verify(testCacheWarmup, times(3)).fetchBatch(eq(0L), eq(2));
        verify(testCacheWarmup, never()).saveCache(anyList());
    }

    @Test
    void testWarmupDisabled() throws Exception {
        setFields(testCacheWarmup, 2, false, 5000);

        testCacheWarmup.init();
        Thread.sleep(1000);

        verify(testCacheWarmup, never()).fetchBatch(anyLong(), anyInt());
        verify(testCacheWarmup, never()).saveCache(anyList());
    }

    @Test
    void testTimeoutExceeded() throws Exception {
        doAnswer(invocation -> {
            Thread.sleep(10000);
            return Collections.emptyList();
        }).when(testCacheWarmup).fetchBatch(anyLong(), anyInt());

        CompletableFuture<?> future = CompletableFuture.runAsync(testCacheWarmup::warmUpCache, taskExecutor);

        assertThrows(java.util.concurrent.TimeoutException.class, () -> future.get(6, TimeUnit.SECONDS));
        verify(testCacheWarmup, times(1)).fetchBatch(eq(0L), eq(2));
    }

    @Test
    void testMappingAndSaving() {
        List<String> entities = Arrays.asList("10", "20");
        testCacheWarmup.saveToCache(entities);

        verify(testCacheWarmup, times(1)).saveCache(Arrays.asList(10, 20));
    }


    private void setFields(TestCacheWarmup instance, int batchSize, boolean warmupEnabled, int warmupTimeout) {
        try {
            Field batchSizeField = AbstractCacheWarmup.class.getDeclaredField("batchSize");
            batchSizeField.setAccessible(true);
            batchSizeField.set(instance, batchSize);

            Field warmupEnabledField = AbstractCacheWarmup.class.getDeclaredField("warmupEnabled");
            warmupEnabledField.setAccessible(true);
            warmupEnabledField.set(instance, warmupEnabled);

            Field warmupTimeoutField = AbstractCacheWarmup.class.getDeclaredField("warmupTimeout");
            warmupTimeoutField.setAccessible(true);
            warmupTimeoutField.set(instance, warmupTimeout);
        } catch (Exception e) {
            fail("Failed to set fields via reflection", e);
        }
    }
}