package uz.consortgroup.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.user_service.asspect.annotation.AspectAfterReturning;
import uz.consortgroup.user_service.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.user_service.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.user_service.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.user_service.entity.cacheEntity.VerificationCodeCacheEntity;
import uz.consortgroup.user_service.repository.VerificationCodeRedisRepository;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class VerificationCodeCacheService {
    private final VerificationCodeRedisRepository verificationCodeRedisRepository;

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterReturning
    @AspectAfterThrowing
    public Optional<VerificationCodeCacheEntity> findCodeById(Long id) {
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
