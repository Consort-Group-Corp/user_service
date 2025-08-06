package uz.consortgroup.userservice.service.directory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.userservice.repository.SuperAdminRepository;
import uz.consortgroup.userservice.repository.UserRepository;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDirectoryService {

    private final RedisTemplate<String, String> redisTemplate;
    private final List<UserInfoProvider> providers;

    private final UserRepository userRepository;
    private final SuperAdminRepository superAdminRepository;

    public Optional<UserShortInfoResponseDto> getUserInfo(UUID userId) {
        String redisKey = "user_type_by_id:" + userId;
        String typeValue = redisTemplate.opsForValue().get(redisKey);
        log.debug("Redis lookup for {}: {}", redisKey, typeValue);

        if (typeValue == null) {
            log.debug("Type not found in Redis for userId: {}. Resolving from DB...", userId);
            typeValue = resolveAndCacheUserType(userId, redisKey);
            if (typeValue == null) {
                log.warn("User type not found for ID: {}", userId);
                return Optional.empty();
            }
        }

        final String finalType = typeValue;
        log.debug("Resolved user type for {}: {}", userId, finalType);

        return providers.stream()
                .filter(p -> p.supports(finalType))
                .findFirst()
                .flatMap(p -> p.getById(userId));
    }

    public Map<UUID, UserShortInfoResponseDto> getUserInfoBulk(List<UUID> userIds) {
        log.info("Fetching bulk user info for {} users", userIds.size());

        List<String> redisKeys = userIds.stream()
                .map(id -> "user_type_by_id:" + id)
                .toList();

        List<String> redisTypes = redisTemplate.opsForValue().multiGet(redisKeys);
        log.debug("Fetched {} types from Redis", redisTypes != null ? redisTypes.size() : 0);

        Map<UUID, String> typesById = IntStream.range(0, userIds.size())
                .mapToObj(i -> {
                    UUID id = userIds.get(i);
                    String type = redisTypes.get(i);
                    if (type == null) {
                        log.debug("Type missing in Redis for userId: {}. Resolving from DB...", id);
                        type = resolveAndCacheUserType(id, redisKeys.get(i));
                    }
                    return new AbstractMap.SimpleEntry<>(id, type);
                })
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.debug("Resolved types for {} users", typesById.size());

        Map<String, List<UUID>> groupedByType = typesById.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));

        Map<UUID, UserShortInfoResponseDto> result = providers.stream()
                .filter(provider -> groupedByType.containsKey(provider.getType()))
                .flatMap(provider -> {
                    List<UUID> ids = groupedByType.get(provider.getType());
                    log.debug("Fetching {} users for type '{}'", ids.size(), provider.getType());
                    return provider.getByIds(ids).entrySet().stream();
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.info("Successfully retrieved short info for {} users", result.size());
        return result;
    }

    private String resolveAndCacheUserType(UUID userId, String redisKey) {
        if (userRepository.existsById(userId)) {
            redisTemplate.opsForValue().set(redisKey, "USER");
            log.debug("Cached user type USER for ID: {}", userId);
            return "USER";
        }
        if (superAdminRepository.existsById(userId)) {
            redisTemplate.opsForValue().set(redisKey, "SUPER_ADMIN");
            log.debug("Cached user type SUPER_ADMIN for ID: {}", userId);
            return "SUPER_ADMIN";
        }

        log.warn("User ID not found in USER or SUPER_ADMIN tables: {}", userId);
        return null;
    }
}
