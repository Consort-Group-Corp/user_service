package uz.consortgroup.userservice.service.access;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.response.CourseAccessResponse;
import uz.consortgroup.userservice.entity.UserPurchasedCourse;
import uz.consortgroup.userservice.repository.UserPurchasedCourseRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseAccessServiceImpl implements CourseAccessService {

    private final UserPurchasedCourseRepository userPurchasedCourseRepository;

    public CourseAccessResponse checkAccess(UUID userId, UUID courseId) {
       log.info("Checking access for userId={}, courseId={}", userId, courseId);

        Optional<UserPurchasedCourse> purchase =
                userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId);

        if (purchase.isEmpty()) {
            return CourseAccessResponse.builder()
                    .hasAccess(false)
                    .accessUntil(null)
                    .build();
        }

        Instant until = purchase.get().getAccessUntil();
        boolean active = until == null || until.isAfter(Instant.now());

        return CourseAccessResponse.builder()
                .hasAccess(active)
                .accessUntil(until)
                .build();
    }
}
