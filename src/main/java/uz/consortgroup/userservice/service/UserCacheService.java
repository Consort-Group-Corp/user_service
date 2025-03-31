package uz.consortgroup.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.entity.cacheEntity.UserCacheEntity;
import uz.consortgroup.userservice.repository.UserRedisRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheService {
    private final UserRedisRepository userRedisRepository;

    public Optional<UserCacheEntity> findUserById(Long id) {
        try {
            return userRedisRepository.findById(id);
        } catch (Exception e) {
            log.error("Redis access error for user: {}", id, e);
            return Optional.empty();
        }
    }

    public void cacheUser(UserCacheEntity user) {
        if (user != null && user.getId() != null) {
            try {
                userRedisRepository.save(user);
                log.debug("User cached successfully: {}", user.getId());
            } catch (Exception e) {
                log.error("Failed to cache user: {}", user.getId(), e);
            }
        }
    }

    public void cacheUsers(List<UserCacheEntity> users) {
        for (UserCacheEntity userFromCache : users) {
            if (userFromCache != null && userFromCache.getId() != null) {
                try {
                    userRedisRepository.save(userFromCache);
                } catch (Exception e) {
                    log.error("Failed to cache user: {}", userFromCache.getId(), e);
                }
            }
        }
        log.debug("Users cached successfully: {}", users.size());
    }

    public void removeUserFromCache(Long userId) {
        try {
            userRedisRepository.deleteById(userId);
            log.debug("User evicted from cache: {}", userId);
        } catch (Exception e) {
            log.error("Failed to evict user from cache: {}", userId, e);
        }
    }
}
