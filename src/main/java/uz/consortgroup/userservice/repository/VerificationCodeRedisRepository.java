package uz.consortgroup.userservice.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.userservice.entity.cacheEntity.VerificationCodeCacheEntity;

@Repository
public interface VerificationCodeRedisRepository extends CrudRepository<VerificationCodeCacheEntity, Long> {
}
