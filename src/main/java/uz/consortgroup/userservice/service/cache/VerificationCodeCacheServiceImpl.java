package uz.consortgroup.userservice.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterReturning;
import uz.consortgroup.userservice.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.userservice.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.userservice.entity.cacheEntity.VerificationCodeCacheEntity;
import uz.consortgroup.userservice.repository.VerificationCodeRedisRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class VerificationCodeCacheServiceImpl implements VerificationCodeCacheService {
    private final VerificationCodeRedisRepository verificationCodeRedisRepository;

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterReturning
    @AspectAfterThrowing
    public Optional<VerificationCodeCacheEntity> findCodeById(UUID id) {
        try {
            return verificationCodeRedisRepository.findById(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void saveVerificationCode(VerificationCodeCacheEntity code) {
        if (code != null && code.getId() != null) {
            try {
                verificationCodeRedisRepository.save(code);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Failed to cache verification code: %s", code.getId()), e);
            }
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void saveVerificationCodes(List<VerificationCodeCacheEntity> codes) {
        try {
            verificationCodeRedisRepository.saveAll(codes);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to cache verification codes: %s", codes), e);
        }
    }
}
