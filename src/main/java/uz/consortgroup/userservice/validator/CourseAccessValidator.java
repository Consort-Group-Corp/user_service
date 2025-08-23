package uz.consortgroup.userservice.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.EligibilityReason;
import uz.consortgroup.core.api.v1.dto.user.response.EligibilityResponse;
import uz.consortgroup.userservice.entity.UserPurchasedCourse;
import uz.consortgroup.userservice.exception.CourseAlreadyPurchasedAndStillActiveException;
import uz.consortgroup.userservice.repository.UserPurchasedCourseRepository;
import uz.consortgroup.userservice.validator.strategy.EligibilityStrategy;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourseAccessValidator {
    private final UserPurchasedCourseRepository userPurchasedCourseRepository;
    private final List<EligibilityStrategy> eligibilityStrategies;

    public EligibilityResponse validateUserCanPurchaseCourse(UUID userId, UUID courseId) {
        log.info("Validating course purchase eligibility for userId={}, courseId={}", userId, courseId);

        for (EligibilityStrategy strategy : eligibilityStrategies) {
            if (strategy.isApplicable(userId, courseId)) {
                log.warn("Validation failed by strategy: {} for userId={}, courseId={}",
                        strategy.getClass().getSimpleName(), userId, courseId);
                throw new CourseAlreadyPurchasedAndStillActiveException(
                        "Purchase not allowed: " + strategy.check(userId, courseId).getReason()
                );
            }
        }

        Optional<UserPurchasedCourse> existingPurchase =
                userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId);

        if (existingPurchase.isPresent()) {
            Instant accessUntil = existingPurchase.get().getAccessUntil();
            if (accessUntil.isAfter(Instant.now())) {
                log.warn("User {} has already purchased course {} with active access until {}", userId, courseId, accessUntil);
                throw new CourseAlreadyPurchasedAndStillActiveException("Course already purchased and access is still valid");
            } else {
                log.info("Previous course access expired for userId={}, courseId={}", userId, courseId);
            }
        } else {
            log.info("No existing purchase found for userId={}, courseId={}", userId, courseId);
        }

        return EligibilityResponse.builder()
                .eligible(true)
                .reason(EligibilityReason.OK)
                .accessUntil(null)
                .build();
    }
}
