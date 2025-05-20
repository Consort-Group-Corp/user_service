package uz.consortgroup.userservice.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.user.enumeration.VerificationCodeStatus;
import uz.consortgroup.userservice.repository.VerificationCodeRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeExpirationScheduler {
    private final VerificationCodeRepository verificationCodeRepository;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void expireOldCodes() {
        LocalDateTime now = LocalDateTime.now();
        int expiredCount = verificationCodeRepository.updateExpiredCodes(
                VerificationCodeStatus.EXPIRED,
                now
        );

        if (expiredCount > 0) {
            log.info("Automatically expired {} verification codes", expiredCount);
        }
    }
}
