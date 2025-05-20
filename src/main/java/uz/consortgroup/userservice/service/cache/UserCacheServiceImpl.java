package uz.consortgroup.userservice.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.userservice.entity.cacheEntity.UserCacheEntity;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.repository.UserRedisRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheServiceImpl implements UserCacheService {
    private final UserRedisRepository userRedisRepository;

    @LoggingAspectBeforeMethod
    @AspectAfterThrowing
    public Optional<UserCacheEntity> findUserById(UUID id) {
        try {
            return userRedisRepository.findById(id);
        } catch (UserNotFoundException e) {
            return Optional.empty();
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void cacheUser(UserCacheEntity user) {
        if (user != null && user.getId() != null) {
            try {
                userRedisRepository.save(user);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Failed to cache user: %s", user.getId()), e);
            }
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void cacheUsers(List<UserCacheEntity> users) {
        for (UserCacheEntity userFromCache : users) {
            if (userFromCache != null && userFromCache.getId() != null) {
                try {
                    userRedisRepository.save(userFromCache);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Failed to cache users: %s", userFromCache.getId()), e);
                }
            }
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void removeUserFromCache(UUID userId) {
        try {
            userRedisRepository.deleteById(userId);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to remove user from cache: %s", userId), e);
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    public Optional<UserCacheEntity> findUserByEmail(String email) {
       try {
          return userRedisRepository.findByEmail(email);
       } catch (UserNotFoundException e) {
           return Optional.empty();
       }
    }
}
