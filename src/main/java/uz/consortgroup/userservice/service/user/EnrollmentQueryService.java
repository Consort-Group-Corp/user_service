package uz.consortgroup.userservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.repository.UserPurchasedCourseRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentQueryService {

    private final UserPurchasedCourseRepository purchasedRepo;

    public List<UUID> filterEnrolled(UUID courseId, List<UUID> userIds) {
        if (courseId == null || userIds == null || userIds.isEmpty()) return List.of();
        List<UUID> result = purchasedRepo.findEnrolledUserIds(courseId, userIds, Instant.now());
        log.debug("filterEnrolled: course={}, in={}, out={}", courseId, userIds.size(), result.size());
        return result;
    }
}
