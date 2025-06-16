package uz.consortgroup.userservice.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.userservice.entity.UserPurchasedCourse;
import uz.consortgroup.userservice.exception.CourseAlreadyPurchasedAndStillActiveException;
import uz.consortgroup.userservice.repository.UserPurchasedCourseRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseAccessValidatorTest {

    @Mock
    private UserPurchasedCourseRepository userPurchasedCourseRepository;

    @InjectMocks
    private CourseAccessValidator courseAccessValidator;

    @Test
    void validateUserCanPurchaseCourse_ShouldPassWhenNoExistingPurchase() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        when(userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId))
            .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> 
            courseAccessValidator.validateUserCanPurchaseCourse(userId, courseId));
    }

    @Test
    void validateUserCanPurchaseCourse_ShouldPassWhenAccessExpired() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UserPurchasedCourse expiredPurchase = new UserPurchasedCourse();
        expiredPurchase.setAccessUntil(Instant.now().minusSeconds(3600));

        when(userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId))
            .thenReturn(Optional.of(expiredPurchase));

        assertDoesNotThrow(() ->
            courseAccessValidator.validateUserCanPurchaseCourse(userId, courseId));
    }

    @Test
    void validateUserCanPurchaseCourse_ShouldThrowExceptionWhenAccessStillActive() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UserPurchasedCourse activePurchase = new UserPurchasedCourse();
        activePurchase.setAccessUntil(Instant.now().plusSeconds(3600));

        when(userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId))
            .thenReturn(Optional.of(activePurchase));

        CourseAlreadyPurchasedAndStillActiveException exception = assertThrows(
            CourseAlreadyPurchasedAndStillActiveException.class,
            () -> courseAccessValidator.validateUserCanPurchaseCourse(userId, courseId)
        );

        assertEquals("Курс уже куплен и доступ к нему ещё не истёк", exception.getMessage());
    }

    @Test
    void validateUserCanPurchaseCourse_ShouldPassWhenAccessExactlyNow() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UserPurchasedCourse purchase = new UserPurchasedCourse();
        purchase.setAccessUntil(Instant.now());

        when(userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId))
            .thenReturn(Optional.of(purchase));

        assertDoesNotThrow(() ->
            courseAccessValidator.validateUserCanPurchaseCourse(userId, courseId));
    }
}