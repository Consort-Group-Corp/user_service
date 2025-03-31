package uz.consortgroup.userservice.cache;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.entity.VerificationCode;
import uz.consortgroup.userservice.entity.cacheEntity.VerificationCodeCacheEntity;
import uz.consortgroup.userservice.mapper.VerificationCodeCacheMapper;
import uz.consortgroup.userservice.repository.VerificationCodeRepository;
import uz.consortgroup.userservice.service.VerificationCodeCacheService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class VerificationCodesCache {
    @Value("${verification.cache.batch-size:1000}")
    private int batchSize;

    private final VerificationCodeCacheService verificationCodeCacheService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final VerificationCodeCacheMapper verificationCodeCacheMapper;

    @PostConstruct
    public void init() {
        log.info("Starting verification codes cache warmup");
        CompletableFuture.runAsync(this::warmUpCache, taskExecutor)
                .exceptionally(ex -> {
                    log.error("Cache warmup failed", ex);
                    return null;
                });
    }

    private void warmUpCache() {
        try {
            Long lastId = 0L;
            boolean hasMore;

            do {
                List<VerificationCode> codes = verificationCodeRepository.findCodesBatch(lastId, batchSize);
                hasMore = !codes.isEmpty();

                if (hasMore) {
                    saveToCache(codes);
                    lastId = codes.get(codes.size() - 1).getId();
                    log.debug("Cached {} codes, last ID: {}", codes.size(), lastId);
                }
            } while (hasMore);

            log.info("Verification codes cache warmup completed");
        } catch (Exception e) {
            log.error("Error during cache warmup", e);
        }
    }

    private void saveToCache(List<VerificationCode> codes) {
        List<VerificationCodeCacheEntity> cacheEntities = codes.stream()
                .map(verificationCodeCacheMapper::toVerificationCodeCacheEntity)
                .collect(Collectors.toList());

        verificationCodeCacheService.saveVerificationCodes(cacheEntities);
    }
}
