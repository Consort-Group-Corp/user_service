package uz.consortgroup.userservice.service.operation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterReturning;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.mapper.UserCacheMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.service.cache.UserCacheServiceImpl;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserOperationsServiceServiceImpl implements UserOperationsService {
    private final UserCacheServiceImpl userCacheService;
    private final UserRepository userRepository;
    private final UserCacheMapper userCacheMapper;

    @Transactional
    @Override
    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterReturning
    public User findUserById(UUID userId) {
        return getUserFromDbAndCacheById(userId);
    }

    @Transactional
    @Override
    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterReturning
    public User findUserByEmail(String email) {
        return getUserFromCacheOrDbByEmail(email);
    }


    @Override
    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    public void saveUser(User user) {
        userCacheService.cacheUser(userCacheMapper.toUserCache(user));
        userRepository.save(user);
    }

    @Override
    @Transactional
    @AllAspect
    public User changeUserRoleByEmail(String email, UserRole role) {
        User user = getUserFromCacheOrDbByEmail(email);
        user.setRole(role);
        cacheUser(user);
        return userRepository.save(user);
    }

    @Override
    @AllAspect
    public UUID findUserIdByEmail(String email) {
        return userRepository.findUserIdByEmail(email);
    }


    @Transactional
    @AllAspect
    public User getUserFromDbAndCacheById(UUID userId) {
        return userCacheService.findUserById(userId)
                .map(userCacheMapper::toUserEntity)
                .orElseGet(() -> {
                    try {
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
                        cacheUser(user);
                        return user;
                    } catch (UserNotFoundException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException("Неожиданная ошибка при получении пользователя", e);
                    }
                });
    }

    @Transactional
    @AllAspect
    public User getUserFromCacheOrDbByEmail(String email) {
        return userCacheService.findUserByEmail(email)
                .map(userCacheMapper::toUserEntity)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
                    cacheUser(user);
                    return user;
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
