package uz.consortgroup.userservice.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AbstractCacheWarmupTest {

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    private TestCacheWarmup cacheWarmup;
    private final List<TestEntity> testData = List.of(
            new TestEntity(UUID.randomUUID()),
            new TestEntity(UUID.randomUUID()),
            new TestEntity(UUID.randomUUID())
    );

    @BeforeEach
    void setUp() {
        cacheWarmup = spy(new TestCacheWarmup(taskExecutor, testData));
        ReflectionTestUtils.setField(cacheWarmup, "batchSize", 2);
        ReflectionTestUtils.setField(cacheWarmup, "warmupEnabled", true);
        ReflectionTestUtils.setField(cacheWarmup, "warmupTimeout", 5000);
    }


    @Test
    void warmUpCache_EmptyData_DoesNothing() throws Exception {
        TestCacheWarmup emptyWarmup = spy(new TestCacheWarmup(taskExecutor, List.of()));
        ReflectionTestUtils.setField(emptyWarmup, "batchSize", 2);

        CompletableFuture.runAsync(emptyWarmup::warmUpCache, taskExecutor)
                .get(1, TimeUnit.SECONDS);

        verify(emptyWarmup, times(1)).fetchBatch(any(UUID.class), eq(2));
        verify(emptyWarmup, never()).saveCache(anyList());
    }

    @Test
    void warmUpCache_FetchFails_RetriesThreeTimes() {
        doThrow(new RuntimeException()).when(cacheWarmup).fetchBatch(any(UUID.class), anyInt());

        cacheWarmup.warmUpCache();

        verify(cacheWarmup, times(3)).fetchBatch(any(UUID.class), eq(2));
    }

    @Test
    void init_WarmupDisabled_SkipsExecution() {
        ReflectionTestUtils.setField(cacheWarmup, "warmupEnabled", false);

        cacheWarmup.init();

        verify(cacheWarmup, never()).warmUpCache();
    }

    @Test
    void saveToCache_MapsEntitiesCorrectly() {
        List<TestEntity> entities = List.of(
                new TestEntity(UUID.randomUUID()),
                new TestEntity(UUID.randomUUID())
        );

        cacheWarmup.saveToCache(entities);

        verify(cacheWarmup).saveCache(argThat(list -> list.size() == 2));
    }

    static class TestEntity {
        private final UUID id;

        TestEntity(UUID id) {
            this.id = id;
        }

        UUID getId() {
            return id;
        }
    }

    static class TestCacheWarmup extends AbstractCacheWarmup<TestEntity, UUID> {
        private final List<TestEntity> mockData;

        TestCacheWarmup(ThreadPoolTaskExecutor executor, List<TestEntity> mockData) {
            super(executor);
            this.mockData = mockData;
        }

        @Override
        protected List<TestEntity> fetchBatch(UUID lastId, int batchSize) {
            int startIndex = lastId.equals(new UUID(0, 0)) ? 0
                    : (int) mockData.stream()
                    .takeWhile(e -> !e.getId().equals(lastId))
                    .count();
            return mockData.stream()
                    .skip(startIndex)
                    .limit(batchSize)
                    .collect(Collectors.toList());
        }

        @Override
        protected UUID getLastId(List<TestEntity> entities) {
            return entities.isEmpty() ? new UUID(0, 0)
                    : entities.get(entities.size() - 1).getId();
        }

        @Override
        protected UUID mapToCacheEntity(TestEntity entity) {
            return entity.getId();
        }

        @Override
        protected void saveCache(List<UUID> cacheEntities) {}

        @Override
        protected String getCacheName() {
            return "TestCache";
        }
    }
}
