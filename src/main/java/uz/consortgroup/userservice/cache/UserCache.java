package uz.consortgroup.userservice.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.cacheEntity.UserCacheEntity;
import uz.consortgroup.userservice.mapper.UserCacheMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.service.cache.UserCacheService;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class UserCache extends AbstractCacheWarmup<User, UserCacheEntity> {
    private static final String USER_CACHE = "userCache";

    private final UserCacheService userCacheService;
    private final UserRepository userRepository;
    private final UserCacheMapper userCacheMapper;

    public UserCache(ThreadPoolTaskExecutor taskExecutor, UserCacheService userCacheService,
                     UserRepository userRepository, UserCacheMapper userCacheMapper) {
        super(taskExecutor);
        this.userCacheService = userCacheService;
        this.userRepository = userRepository;
        this.userCacheMapper = userCacheMapper;
    }

    @Override
    protected List<User> fetchBatch(UUID lastId, int batchSize) {
        return userRepository.findUsersByBatch(lastId, batchSize);
    }

    @Override
    protected UUID getLastId(List<User> entities) {
        return entities.get(entities.size() - 1).getId();
    }

    @Override
    protected UserCacheEntity mapToCacheEntity(User entity) {
        return userCacheMapper.toUserCache(entity);
    }

    @Override
    protected void saveCache(List<UserCacheEntity> cacheEntities) {
        userCacheService.cacheUsers(cacheEntities);
    }

    @Override
    protected String getCacheName() {
        return USER_CACHE;
    }
}