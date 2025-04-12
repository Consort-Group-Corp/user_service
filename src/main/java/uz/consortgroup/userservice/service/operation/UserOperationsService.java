package uz.consortgroup.userservice.service.operation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterReturning;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.mapper.UserCacheMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.service.cache.UserCacheService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserOperationsService implements UserOperations {
    private final UserCacheService userCacheService;
    private final UserRepository userRepository;
    private final UserCacheMapper userCacheMapper;

    @Transactional
    @Override
    public User findUserById(UUID userId) {
        return getUserFromDbAndCache(userId);
    }


    @Transactional
    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    @AspectAfterReturning
    public User getUserFromDbAndCache(UUID userId) {
        return userCacheService.findUserById(userId)
                .map(userCacheMapper::toUserEntity)
                .orElseGet(() -> {
                    try {
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));
                        cacheUser(user);
                        return user;
                    } catch (Exception e) {
                        throw e;
                    }
                });
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    public void cacheUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");

        }
        userCacheService.cacheUser(userCacheMapper.toUserCache(user));
    }
}
