package uz.consortgroup.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.entity.VerificationCode;
import uz.consortgroup.userservice.entity.cacheEntity.VerificationCodeCacheEntity;
import uz.consortgroup.userservice.entity.enumeration.VerificationCodeStatus;
import uz.consortgroup.userservice.exception.InvalidVerificationCodeException;
import uz.consortgroup.userservice.exception.VerificationCodeExpiredException;
import uz.consortgroup.userservice.mapper.VerificationCodeCacheMapper;
import uz.consortgroup.userservice.repository.VerificationCodeRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerificationService {
    private static final int CODE_EXPIRATION_MINUTES = 5;
    private final VerificationCodeRepository codeRepository;
    private final VerificationCodeCacheService verificationCodeCacheService;
    private final VerificationCodeCacheMapper verificationCodeCacheMapper;

    public String generateAndSaveCode(User user) {
        int previousAttempts = codeRepository.findLastActiveCodeByUserId(user.getId())
                .map(VerificationCode::getAttempts)
                .orElse(0);

        VerificationCode newCode = createNewVerificationCode(user, previousAttempts + 1);
        codeRepository.save(newCode);
        saveCodeToCache(newCode);

        log.info("Generated new verification code for user {}: {}", user.getId(), newCode.getCodeHash());
        return newCode.getCodeHash();
    }

    @Transactional
    public void verifyCode(User user, String inputCode) {
        VerificationCode code = getActiveCode(user);
        validateCode(code, inputCode);
        markCodeAsUsed(code);

        log.info("User {} successfully verified with code {}", user.getId(), inputCode);
    }

    private VerificationCode getActiveCode(User user) {
        Optional<VerificationCodeCacheEntity> cachedCode =
                verificationCodeCacheService.findCodeById(user.getId());

        if (cachedCode.isPresent()) {
            log.debug("Using cached verification code for user {}", user.getId());
            return verificationCodeCacheMapper.toVerificationCode(cachedCode.get());
        }

        log.debug("Fetching verification code from DB for user {}", user.getId());
        return codeRepository.findLastActiveCodeByUserId(user.getId())
                .orElseThrow(() -> new InvalidVerificationCodeException(
                        "No active verification code found for user " + user.getId()));
    }

    private VerificationCode createNewVerificationCode(User user, int attempts) {
        String rawCode = generate4DigitCode();
        LocalDateTime now = LocalDateTime.now();

        return VerificationCode.builder()
                .user(user)
                .codeHash(rawCode)
                .status(VerificationCodeStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .expiresAt(now.plusMinutes(CODE_EXPIRATION_MINUTES))
                .attempts(attempts) // Количество попыток сохраняется
                .build();
    }

    private void validateCode(VerificationCode code, String inputCode) {
        checkCodeExpiration(code);

        if (!inputCode.equals(code.getCodeHash())) {
            code.setAttempts(code.getAttempts() + 1);
            code.setUpdatedAt(LocalDateTime.now());
            codeRepository.save(code);
            updateCodeInCache(code);

            throw new InvalidVerificationCodeException("Invalid verification code");
        }
    }


    private void checkCodeExpiration(VerificationCode code) {
        if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
            code.setStatus(VerificationCodeStatus.EXPIRED);
            code.setUpdatedAt(LocalDateTime.now());
            codeRepository.save(code);
            updateCodeInCache(code);

            throw new VerificationCodeExpiredException("Verification code has expired");
        }
    }

    private void markCodeAsUsed(VerificationCode code) {
        LocalDateTime now = LocalDateTime.now();
        code.setStatus(VerificationCodeStatus.USED);
        code.setUsedAt(now);
        code.setUpdatedAt(now);
        codeRepository.save(code);
        updateCodeInCache(code);

        log.info("Marked verification code {} as USED", code.getId());
    }

    private void saveCodeToCache(VerificationCode code) {
        try {
            VerificationCodeCacheEntity cacheEntity =
                    verificationCodeCacheMapper.toVerificationCodeCacheEntity(code);
            verificationCodeCacheService.saveVerificationCode(cacheEntity);

            log.debug("Saved verification code to cache for user {}", code.getUser().getId());
        } catch (Exception e) {
            log.error("Failed to cache verification code for user {}", code.getUser().getId(), e);
        }
    }


    private void updateCodeInCache(VerificationCode code) {
        try {
            VerificationCodeCacheEntity cacheEntity =
                    verificationCodeCacheMapper.toVerificationCodeCacheEntity(code);
            verificationCodeCacheService.saveVerificationCode(cacheEntity);
        } catch (Exception e) {
            log.error("Failed to update verification code in cache for code {}", code.getId(), e);
        }
    }


    private String generate4DigitCode() {
        return String.format("%04d", new SecureRandom().nextInt(10000));
    }
}
