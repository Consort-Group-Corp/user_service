package uz.consortgroup.user_service.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.user_service.entity.cacheEntity.VerificationCodeCacheEntity;

@Repository
public interface VerificationCodeRedisRepository extends CrudRepository<VerificationCodeCacheEntity, Long> {
}
