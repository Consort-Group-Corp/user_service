package uz.consortgroup.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.entity.cacheEntity.UserCacheEntity;
import uz.consortgroup.userservice.entity.cacheEntity.VerificationCodeCacheEntity;
import uz.consortgroup.userservice.entity.enumeration.VerificationCodeStatus;
import uz.consortgroup.userservice.repository.VerificationCodeRedisRepository;
import uz.consortgroup.userservice.repository.VerificationCodeRepository;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class VerificationCodeCacheService {
    private final VerificationCodeRedisRepository verificationCodeRedisRepository;

    public Optional<VerificationCodeCacheEntity> findCodeById(Long id) {
        try {
            return verificationCodeRedisRepository.findById(id);
        } catch (Exception e) {
            log.error("Redis access error: {}", id, e);
            return Optional.empty();
        }
    }

    public void saveVerificationCode(VerificationCodeCacheEntity code) {
        if (code != null && code.getId() != null) {
            try {
                verificationCodeRedisRepository.save(code);
                log.debug("Code cached with TTL: {}", code.getId());
            } catch (Exception e) {
                log.error("Cache save failed: {}", code.getId(), e);
            }
        }
    }

    public void saveVerificationCodes(List<VerificationCodeCacheEntity> codes) {
        try {
            verificationCodeRedisRepository.saveAll(codes);
            log.debug("Codes cached with TTL: {}", codes.size());
        } catch (Exception e) {
            log.error("Cache save failed: {}", codes.size(), e);
        }
    }
}
