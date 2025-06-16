package uz.consortgroup.userservice.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.consortgroup.userservice.entity.UserPurchasedCourse;
import uz.consortgroup.userservice.exception.CourseAlreadyPurchasedAndStillActiveException;
import uz.consortgroup.userservice.repository.UserPurchasedCourseRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CourseAccessValidator {
    private final UserPurchasedCourseRepository userPurchasedCourseRepository;

    public void validateUserCanPurchaseCourse(UUID userId, UUID courseId) {
        Optional<UserPurchasedCourse> existingPurchase =
                userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId);

        if (existingPurchase.isPresent()) {
            Instant accessUntil = existingPurchase.get().getAccessUntil();
            if (accessUntil.isAfter(Instant.now())) {
                throw new CourseAlreadyPurchasedAndStillActiveException("Курс уже куплен и доступ к нему ещё не истёк");
            }
        }
    }
}
