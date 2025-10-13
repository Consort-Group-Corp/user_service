package uz.consortgroup.userservice.service.operation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.cacheEntity.UserCacheEntity;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.mapper.UserCacheMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.service.cache.UserCacheService;
import uz.consortgroup.userservice.validator.UserValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserOperationsServiceServiceImpl implements UserOperationsService {
    private static final int BATCH_SIZE = 50;
    private final UserCacheService userCacheService;
    private final UserRepository userRepository;
    private final UserCacheMapper userCacheMapper;
    private final UserValidator userValidator;

    @Transactional
    @Override
    public User findUserById(UUID userId) {
        log.info("Finding user by ID: {}", userId);
        return getUserFromDbAndCacheById(userId);
    }

    @Override
    @Transactional
    public List<User> batchFindUsersById(List<UUID> userIds) {
        log.info("Batch finding users by IDs: {}", userIds.size());
        return userIds.stream()
                .map(this::getUserFromDbAndCacheById)
                .toList();
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
        userValidator.validateUniqueFields(user.getEmail(), user.getPinfl(), user.getPhoneNumber());

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

    @Transactional(readOnly = true)
    public User getUserFromDbAndCacheById(UUID userId) {
        log.debug("Trying to get user from cache by ID: {}", userId);
        return userCacheService.findUserById(userId)
                .map(userCacheMapper::toUserEntity)
                .orElseGet(() -> {
                    log.debug("User not found in cache. Querying DB for ID: {}", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> {
                                log.warn("User not found in DB: {}", userId);
                                return new UserNotFoundException(String.format("User with id %s not found", userId));
                            });
                    cacheUser(user);
                    return user;
                });
    }

    @Transactional(readOnly = true)
    public User getUserFromCacheOrDbByEmail(String email) {
        log.debug("Trying to get user from cache by email: {}", email);
        return userCacheService.findUserByEmail(email)
                .map(userCacheMapper::toUserEntity)
                .orElseGet(() -> {
                    log.debug("User not found in cache. Querying DB for email: {}", email);
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> {
                                log.warn("User not found in DB: {}", email);
                                return new UserNotFoundException(String.format("User with email %s not found", email));
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

    @Transactional(readOnly = true)
    @Override
    public List<User> findUsersBatch(List<String> emails, List<String> pinfls) {
        List<User> results = new ArrayList<>();

        for (int i = 0; i < emails.size(); i += BATCH_SIZE) {
            List<String> batchEmails = emails.subList(i, Math.min(i + BATCH_SIZE, emails.size()));
            results.addAll(findUsersInCacheOrDbByEmails(batchEmails));
        }

        for (int i = 0; i < pinfls.size(); i += BATCH_SIZE) {
            List<String> batchPinfls = pinfls.subList(i, Math.min(i + BATCH_SIZE, pinfls.size()));
            results.addAll(findUsersInCacheOrDbByPinfls(batchPinfls));
        }

        return new ArrayList<>(new LinkedHashSet<>(results));
    }

    @Override
    public boolean isUserBlocked(UUID userId) {
        return userRepository.isUserBlocked(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmailIfExists(String emailRaw) {
        final String email = (emailRaw == null) ? null : emailRaw.trim().toLowerCase();

        if (email == null || email.isBlank()) {
            log.debug("findByEmailIfExists: empty email");
            return Optional.empty();
        }
        if (!isEmail(email)) {
            log.debug("findByEmailIfExists: invalid email format: {}", maskEmail(email));
            return Optional.empty();
        }

        log.debug("Getting user from cache by email: {}", maskEmail(email));
        Optional<User> cached = userCacheService.findUserByEmail(email)
                .map(userCacheMapper::toUserEntity);
        if (cached.isPresent()) {
            return cached;
        }

        log.debug("User not found in cache. Trying to get from DB: {}", maskEmail(email));
        Optional<User> fromDb = userRepository.findByEmail(email);

        fromDb.ifPresent(user -> {
            cacheUser(user);
            log.debug("User cached by email: {}", maskEmail(email));
        });

        return fromDb;
    }

    public List<User> findUsersInCacheOrDbByEmails(List<String> emails) {
        return findUsersInCacheOrDb(
                emails,
                userCacheService::findUsersByEmails,
                userRepository::findByEmailIn,
                User::getEmail,
                userCacheMapper::toUserCache,
                userCacheMapper::toUserEntity
        );
    }

    public List<User> findUsersInCacheOrDbByPinfls(List<String> pinfls) {
        return findUsersInCacheOrDb(
                pinfls,
                userCacheService::findUsersByPinfls,
                userRepository::findByPinflIn,
                User::getPinfl,
                userCacheMapper::toUserCache,
                userCacheMapper::toUserEntity
        );
    }

    private List<User> findUsersInCacheOrDb(List<String> keys,
                                            Function<List<String>, List<UserCacheEntity>> cacheFetcher,
                                            Function<List<String>, List<User>> dbFetcher,
                                            Function<User, String> keyExtractor,
                                            Function<User, UserCacheEntity> toCacheEntity,
                                            Function<UserCacheEntity, User> fromCacheEntity) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserCacheEntity> cachedEntities = cacheFetcher.apply(keys);
        List<User> cachedUsers = cachedEntities.stream()
                .map(fromCacheEntity)
                .toList();

        Set<String> cachedKeys = cachedUsers.stream()
                .map(keyExtractor)
                .collect(Collectors.toSet());

        List<String> keysNotInCache = keys.stream()
                .filter(k -> !cachedKeys.contains(k))
                .toList();

        List<User> dbUsers = keysNotInCache.isEmpty() ? Collections.emptyList() : dbFetcher.apply(keysNotInCache);

        dbUsers.forEach(user -> userCacheService.cacheUser(toCacheEntity.apply(user)));

        List<User> result = new ArrayList<>(cachedUsers.size() + dbUsers.size());
        result.addAll(cachedUsers);
        result.addAll(dbUsers);

        return result;
    }

    private boolean isEmail(String value) {
        Pattern emailPattern = Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
        return emailPattern.matcher(value).matches();
    }

    private String maskEmail(String email) {
        return (email == null) ? "" : email.replaceAll("(^.).*(@.*$)", "$1***$2");
    }
}
