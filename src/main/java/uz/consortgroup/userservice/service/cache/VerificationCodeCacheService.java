package uz.consortgroup.userservice.service.cache;

import uz.consortgroup.userservice.entity.cacheEntity.VerificationCodeCacheEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationCodeCacheService {
    Optional<VerificationCodeCacheEntity> findCodeById(UUID id);
    void saveVerificationCode(VerificationCodeCacheEntity code);
    void saveVerificationCodes(List<VerificationCodeCacheEntity> codes);
}
