package uz.consortgroup.userservice.repository;

import io.micrometer.common.lang.Nullable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.userservice.entity.cacheEntity.UserCacheEntity;

import java.time.LocalDateTime;
import java.util.Map;

@Repository
public interface UserRedisRepository extends CrudRepository<UserCacheEntity, Long> {
}
