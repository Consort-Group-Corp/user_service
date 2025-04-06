package uz.consortgroup.user_service.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import uz.consortgroup.user_service.entity.User;
import uz.consortgroup.user_service.entity.cacheEntity.UserCacheEntity;
import uz.consortgroup.user_service.mapper.UserCacheMapper;
import uz.consortgroup.user_service.repository.UserRepository;
import uz.consortgroup.user_service.service.UserCacheService;

import java.util.List;

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
    protected List<User> fetchBatch(Long lastId, int batchSize) {
        return userRepository.findUsersByBatch(lastId, batchSize);
    }

    @Override
    protected Long getLastId(List<User> entities) {
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
