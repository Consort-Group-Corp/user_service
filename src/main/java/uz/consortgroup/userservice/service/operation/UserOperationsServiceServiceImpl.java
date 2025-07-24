package uz.consortgroup.userservice.service.operation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.mapper.UserCacheMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.service.cache.UserCacheServiceImpl;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserOperationsServiceServiceImpl implements UserOperationsService {
    private final UserCacheServiceImpl userCacheService;
    private final UserRepository userRepository;
    private final UserCacheMapper userCacheMapper;

    @Transactional
    @Override
    public User findUserById(UUID userId) {
        log.info("Finding user by ID: {}", userId);
        return getUserFromDbAndCacheById(userId);
    }

    @Transactional(readOnly = true)
    @Override
    public User findUserByEmail(String email) {
        log.info("Finding user by email: {}", email);
        return getUserFromCacheOrDbByEmail(email);
    }

    @Transactional(readOnly = true)
    @Override
    public User findUserByEmailOrPinfl(String query) {
        String cleanedQuery = query.trim().replaceAll("\\s+", "").toLowerCase();
        log.info("Finding user by email or PINFL: {}", cleanedQuery);

        if (isEmail(cleanedQuery)) {
            return getUserFromCacheOrDbByEmail(cleanedQuery);
        } else {
            return getUserFromCacheOrDbByPinfl(cleanedQuery);
        }
    }

    @Override
    public void saveUser(User user) {
        log.info("Saving user with email: {}", user.getEmail());
        userCacheService.cacheUser(userCacheMapper.toUserCache(user));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User changeUserRoleByEmail(String email, UserRole role) {
        log.info("Changing role for user {} to {}", email, role);
        User user = getUserFromCacheOrDbByEmail(email);
        user.setRole(role);
        cacheUser(user);
        return userRepository.save(user);
    }

    @Override
    public UUID findUserIdByEmail(String email) {
        log.info("Finding userId by email: {}", email);
        return userRepository.findUserIdByEmail(email);
    }

    @Transactional
    public User getUserFromDbAndCacheById(UUID userId) {
        log.debug("Trying to get user from cache by ID: {}", userId);
        return userCacheService.findUserById(userId)
                .map(userCacheMapper::toUserEntity)
                .orElseGet(() -> {
                    log.debug("User not found in cache. Querying DB for ID: {}", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> {
                                log.warn("User not found in DB: {}", userId);
                                return new UserNotFoundException("Пользователь не найден");
                            });
                    cacheUser(user);
                    return user;
                });
    }

    @Transactional
    public User getUserFromCacheOrDbByEmail(String email) {
        log.debug("Trying to get user from cache by email: {}", email);
        return userCacheService.findUserByEmail(email)
                .map(userCacheMapper::toUserEntity)
                .orElseGet(() -> {
                    log.debug("User not found in cache. Querying DB for email: {}", email);
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> {
                                log.warn("User not found in DB: {}", email);
                                return new UserNotFoundException("User not found: " + email);
                            });
                    cacheUser(user);
                    return user;
                });
    }

    public void cacheUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        log.debug("Caching user with ID: {}", user.getId());
        userCacheService.cacheUser(userCacheMapper.toUserCache(user));
    }

    private User getUserFromCacheOrDbByPinfl(String pinfl) {
        log.debug("Trying to get user from cache by PINFL: {}", pinfl);
        return userCacheService.findUserByPinfl(pinfl)
                .map(userCacheMapper::toUserEntity)
                .orElseGet(() -> {
                    log.debug("User not found in cache. Querying DB for PINFL: {}", pinfl);
                    User user = userRepository.findUserByPinfl(pinfl)
                            .orElseThrow(() -> {
                                log.warn("User not found in DB with PINFL: {}", pinfl);
                                return new UserNotFoundException(String.format("User with pinfl %s not found", pinfl));
                            });
                    cacheUser(user);
                    return user;
                });
    }

    private boolean isEmail(String value) {
        Pattern emailPattern = Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
        return emailPattern.matcher(value).matches();
    }
}
