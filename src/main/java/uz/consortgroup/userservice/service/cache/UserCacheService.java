package uz.consortgroup.userservice.service.cache;

import uz.consortgroup.userservice.entity.cacheEntity.UserCacheEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCacheService {
    Optional<UserCacheEntity> findUserById(UUID id);
    void cacheUser(UserCacheEntity user);
    void cacheUsers(List<UserCacheEntity> users);
    void removeUserFromCache(UUID userId);
    Optional<UserCacheEntity> findUserByEmail(String email);
}
