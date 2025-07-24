package uz.consortgroup.userservice.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.entity.cacheEntity.UserCacheEntity;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.repository.UserRedisRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheServiceImpl implements UserCacheService {
    private final UserRedisRepository userRedisRepository;

    public Optional<UserCacheEntity> findUserById(UUID id) {
        log.info("Finding user in cache by ID: {}", id);
        try {
            Optional<UserCacheEntity> result = userRedisRepository.findById(id);
            log.info("User found by ID {}: {}", id, result.isPresent());
            return result;
        } catch (UserNotFoundException e) {
            log.warn("User not found in cache by ID: {}", id);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error while finding user by ID: {}", id, e);
            throw e;
        }
    }

    public void cacheUser(UserCacheEntity user) {
        if (user != null && user.getId() != null) {
            UUID id = user.getId();
            log.info("Caching single user: {}", id);
            try {
                userRedisRepository.save(user);
                log.info("Successfully cached user: {}", id);
            } catch (Exception e) {
                log.error("Failed to cache user: {}", id, e);
                throw new RuntimeException(String.format("Failed to cache user: %s", id), e);
            }
        } else {
            log.warn("Skipping cacheUser due to null user or null ID.");
        }
    }

    public void cacheUsers(List<UserCacheEntity> users) {
        log.info("Caching {} users", users != null ? users.size() : 0);
        for (UserCacheEntity userFromCache : Objects.requireNonNull(users)) {
            if (userFromCache != null && userFromCache.getId() != null) {
                UUID id = userFromCache.getId();
                try {
                    userRedisRepository.save(userFromCache);
                    log.info("Cached user: {}", id);
                } catch (Exception e) {
                    log.error("Failed to cache user in batch: {}", id, e);
                    throw new RuntimeException(String.format("Failed to cache users: %s", id), e);
                }
            } else {
                log.warn("Skipping null or invalid user in cacheUsers");
            }
        }
    }

    public void removeUserFromCache(UUID userId) {
        log.info("Removing user from cache: {}", userId);
        try {
            userRedisRepository.deleteById(userId);
            log.info("Successfully removed user from cache: {}", userId);
        } catch (Exception e) {
            log.error("Failed to remove user from cache: {}", userId, e);
            throw new RuntimeException(String.format("Failed to remove user from cache: %s", userId), e);
        }
    }

    public Optional<UserCacheEntity> findUserByEmail(String email) {
        log.info("Finding user in cache by email: {}", email);
        try {
            Optional<UserCacheEntity> result = userRedisRepository.findByEmail(email);
            log.info("User found by email {}: {}", email, result.isPresent());
            return result;
        } catch (UserNotFoundException e) {
            log.warn("User not found in cache by email: {}", email);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error while finding user by email: {}", email, e);
            throw e;
        }
    }

    public Optional<UserCacheEntity> findUserByPinfl(String pinfl) {
        log.info("Finding user in cache by PINFL: {}", pinfl);
        try {
            Optional<UserCacheEntity> result = userRedisRepository.findUserCacheEntityByPinfl(pinfl);
            log.info("User found by PINFL {}: {}", pinfl, result.isPresent());
            return result;
        } catch (UserNotFoundException e) {
            log.warn("User not found in cache by PINFL: {}", pinfl);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error while finding user by PINFL: {}", pinfl, e);
            throw e;
        }
    }
}
