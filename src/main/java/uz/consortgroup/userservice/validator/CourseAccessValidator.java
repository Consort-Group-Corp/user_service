package uz.consortgroup.userservice.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.entity.UserPurchasedCourse;
import uz.consortgroup.userservice.exception.CourseAlreadyPurchasedAndStillActiveException;
import uz.consortgroup.userservice.repository.UserPurchasedCourseRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourseAccessValidator {
    private final UserPurchasedCourseRepository userPurchasedCourseRepository;

    public void validateUserCanPurchaseCourse(UUID userId, UUID courseId) {
        log.info("Validating course purchase eligibility for userId={}, courseId={}", userId, courseId);

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
    }
}
