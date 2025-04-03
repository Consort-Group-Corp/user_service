package uz.consortgroup.userservice.cache;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractCacheWarmup <T, E> {
    @Value("${cache.redis-batch-size}")
    private int batchSize;

    @Value("${cache.warmup.enabled:true}")
    private boolean warmupEnabled;

    @Value("${cache.warmup.timeout:30000}")
    private int warmupTimeout;

    private final ThreadPoolTaskExecutor taskExecutor;

    protected AbstractCacheWarmup(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @PostConstruct
    public void init() {
        if (warmupEnabled) {
            log.info("Starting {} cache warmup", getCacheName());
            CompletableFuture.runAsync(this::warmUpCache, taskExecutor)
                    .orTimeout(warmupTimeout, TimeUnit.MILLISECONDS)
                    .exceptionally(ex -> {
                        log.error("Cache warmup failed for {}", getCacheName(), ex);
                        return null;
                    });
        } else {
            log.info("Cache warmup for {} is disabled", getCacheName());
        }
    }

    protected void warmUpCache() {
        Long lastId = 0L;
        boolean hasMore;
        int maxAttempts = 3;
        int attempt = 0;

        do {
            try {
                List<T> entities = fetchBatch(lastId, batchSize);
                hasMore = !entities.isEmpty();

                if (hasMore) {
                    saveToCache(entities);
                    lastId = getLastId(entities);
                    log.debug("Cached {} {}, last ID: {}", entities.size(), getCacheName(), lastId);
                    attempt = 0;
                }
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    log.error("Max attempts reached for {} cache warmup, aborting", getCacheName(), e);
                    hasMore = false;
                } else {
                    log.warn("Retry attempt {}/{} for {} cache warmup", attempt, maxAttempts, getCacheName(), e);
                    hasMore = true;
                }
            }
        } while (hasMore);

        log.info("{} cache warmup completed", getCacheName());
    }

    protected void saveToCache(List<T> entities) {
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
