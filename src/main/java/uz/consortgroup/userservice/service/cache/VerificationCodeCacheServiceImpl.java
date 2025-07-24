package uz.consortgroup.userservice.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.entity.cacheEntity.VerificationCodeCacheEntity;
import uz.consortgroup.userservice.repository.VerificationCodeRedisRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationCodeCacheServiceImpl implements VerificationCodeCacheService {

    private final VerificationCodeRedisRepository verificationCodeRedisRepository;

    public Optional<VerificationCodeCacheEntity> findCodeById(UUID id) {
        log.info("Attempting to find verification code by ID: {}", id);
        try {
            Optional<VerificationCodeCacheEntity> result = verificationCodeRedisRepository.findById(id);
            log.info("Verification code found by ID {}: {}", id, result.isPresent());
            return result;
        } catch (Exception e) {
            log.error("Failed to find verification code by ID: {}", id, e);
            return Optional.empty();
        }
    }

    public void saveVerificationCode(VerificationCodeCacheEntity code) {
        if (code != null && code.getId() != null) {
            UUID id = code.getId();
            log.info("Saving verification code to cache with ID: {}", id);
            try {
                verificationCodeRedisRepository.save(code);
                log.info("Successfully cached verification code with ID: {}", id);
            } catch (Exception e) {
                log.error("Failed to cache verification code with ID: {}", id, e);
                throw new RuntimeException(String.format("Failed to cache verification code: %s", id), e);
            }
        } else {
            log.warn("Skipping saveVerificationCode due to null code or null ID");
        }
    }

    public void saveVerificationCodes(List<VerificationCodeCacheEntity> codes) {
        int size = codes != null ? codes.size() : 0;
        log.info("Saving {} verification codes to cache", size);
        try {
            verificationCodeRedisRepository.saveAll(Objects.requireNonNull(codes));
            log.info("Successfully cached {} verification codes", size);
        } catch (Exception e) {
            log.error("Failed to cache verification codes batch: {}", codes, e);
            throw new RuntimeException(String.format("Failed to cache verification codes: %s", codes), e);
        }
    }
}
