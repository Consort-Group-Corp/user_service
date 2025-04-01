package uz.consortgroup.userservice.cache;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractCacheWarmup <T, E> {
    @Value("${cache.redis-batch-size}")
    private int batchSize;

    private final ThreadPoolTaskExecutor taskExecutor;

    protected AbstractCacheWarmup(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @PostConstruct
    public void init() {
        log.info("Starting {} cache warmup", getCacheName());
        CompletableFuture.runAsync(this::warmUpCache, taskExecutor)
                .exceptionally(ex -> {
                    log.error("Cache warmup failed for {}", getCacheName(), ex);
                    return null;
                });
    }

    private void warmUpCache() {
        try {
            Long lastId = 0L;
            boolean hasMore;

            do {
                List<T> entities = fetchBatch(lastId, batchSize);
                hasMore = !entities.isEmpty();

                if (hasMore) {
                    saveToCache(entities);
                    lastId = getLastId(entities);
                    log.debug("Cached {} {}, last ID: {}", entities.size(), getCacheName(), lastId);
                }
            } while (hasMore);

            log.info("{} cache warmup completed", getCacheName());
        } catch (Exception e) {
            log.error("Error during {} cache warmup", getCacheName(), e);
        }
    }

    private void saveToCache(List<T> entities) {
        List<E> cacheEntities = entities.stream()
                .map(this::mapToCacheEntity)
                .collect(Collectors.toList());

        saveCache(cacheEntities);
    }

    protected abstract List<T> fetchBatch(Long lastId, int batchSize);
    protected abstract Long getLastId(List<T> entities);
    protected abstract E mapToCacheEntity(T entity);
    protected abstract void saveCache(List<E> cacheEntities);
    protected abstract String getCacheName();
}
