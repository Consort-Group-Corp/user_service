package uz.consortgroup.userservice.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.userservice.entity.cacheEntity.UserCacheEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRedisRepository extends CrudRepository<UserCacheEntity, UUID> {
    Optional<UserCacheEntity> findByEmail(String email);
}
