package uz.consortgroup.userservice.validator.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.EligibilityReason;
import uz.consortgroup.core.api.v1.dto.user.response.EligibilityResponse;
import uz.consortgroup.userservice.client.CourseFeignClient;

import java.util.UUID;

@Component
@Slf4j
public class CourseNotFoundStrategy extends AbstractEligibilityStrategy {
    private final CourseFeignClient courseFeignClient;

    public CourseNotFoundStrategy(CourseFeignClient courseFeignClient) {
        super(EligibilityReason.COURSE_NOT_FOUND);
        this.courseFeignClient = courseFeignClient;
    }

    @Override
    public boolean isApplicable(UUID userId, UUID courseId) {
        boolean courseNotFound = courseFeignClient.getCourseById(courseId) == null;
        if (courseNotFound) {
            log.warn("CourseNotFoundStrategy applicable: course {} not found for user {}", courseId, userId);
        }
        return courseNotFound;
    }

    @Override
    public EligibilityResponse check(UUID userId, UUID courseId) {
        log.error("User {} cannot purchase course {} - course not found", userId, courseId);
        return super.check(userId, courseId);
    }
}