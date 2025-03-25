package uz.consortgroup.userservice.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.userservice.entity.UserCacheEntity;

@Repository
public interface UserRedisRepository extends CrudRepository<UserCacheEntity, Long> {
}
