package uz.consortgroup.userservice.validator.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.EligibilityReason;
import uz.consortgroup.core.api.v1.dto.user.response.EligibilityResponse;
import uz.consortgroup.userservice.entity.UserPurchasedCourse;
import uz.consortgroup.userservice.repository.UserPurchasedCourseRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class AlreadyActiveStrategy extends AbstractEligibilityStrategy {

    private final UserPurchasedCourseRepository userPurchasedCourseRepository;

    public AlreadyActiveStrategy(UserPurchasedCourseRepository userPurchasedCourseRepository) {
        super(EligibilityReason.ALREADY_ACTIVE);
        this.userPurchasedCourseRepository = userPurchasedCourseRepository;
    }

    @Override
    public boolean isApplicable(UUID userId, UUID courseId) {
        Optional<UserPurchasedCourse> existingPurchase = userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId);
        boolean isApplicable = existingPurchase.isPresent() &&
                existingPurchase.get().getAccessUntil().isAfter(Instant.now());

        if (isApplicable) {
            log.debug("AlreadyActiveStrategy applicable: user {} has active access to course {} until {}",
                    userId, courseId, existingPurchase.get().getAccessUntil());
        }
        return isApplicable;
    }

    @Override
    public EligibilityResponse check(UUID userId, UUID courseId) {
        Optional<UserPurchasedCourse> existingPurchase = userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId);

        Integer accessUntilTimestamp = existingPurchase
                .map(UserPurchasedCourse::getAccessUntil)
                .map(Instant::getEpochSecond)
                .map(Long::intValue)
                .orElse(null);

        log.warn("User {} cannot purchase course {} - already active until {}",
                userId, courseId, accessUntilTimestamp);

        return EligibilityResponse.builder()
                .eligible(false)
                .reason(EligibilityReason.ALREADY_ACTIVE)
                .accessUntil(accessUntilTimestamp)
                .build();
    }
}