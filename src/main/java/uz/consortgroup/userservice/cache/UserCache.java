package uz.consortgroup.userservice.cache;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.UserCacheEntity;
import uz.consortgroup.userservice.mapper.UserCacheMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.service.UserCacheService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCache {
    @Value("${user-cache.redis-batch-size}")
    private int redisBatchSize;
    private final UserCacheService userCacheService;
    private final UserRepository userRepository;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final UserCacheMapper userCacheMapper;

    @PostConstruct
    public void init() {
        log.info("UserCache initialized. Cache warming will start asynchronously");

        try {
           CompletableFuture.runAsync(this::warmUpCacheAsync, taskExecutor);

        } catch (Exception e) {
            log.error("Failed to warm up UserCache", e);
        }
    }

    private void warmUpCacheAsync() {
        log.info("Starting warm-up of UserCache");
        try {
            Long lastId = 0L;
            while (true) {
                Pageable pageable = PageRequest.of(0, redisBatchSize);
                Page<User> userPage = userRepository.findUsersByBatch(lastId, pageable);

                List<User> users = userPage.getContent();
                if (users.isEmpty()) {
                    break;
                }

                List<UserCacheEntity> cacheEntities = users.stream()
                        .map(userCacheMapper::toUserEntity)
                        .toList();

                userCacheService.saveUsersToCache(cacheEntities);
                log.info("Saved {} users to Redis during warm-up", cacheEntities.size());

                lastId = users.get(users.size() - 1).getId();
            }
            log.info("UserCache warm-up completed.");
        } catch (Exception e) {
            log.error("Error while warming up cache", e);
        }
    }
}
