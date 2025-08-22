package uz.consortgroup.userservice.service.purchases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;
import uz.consortgroup.userservice.client.CourseFeignClient;
import uz.consortgroup.userservice.entity.UserPurchasedCourse;
import uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent;
import uz.consortgroup.userservice.repository.UserPurchasedCourseRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoursePurchaseServiceImpl implements CoursePurchaseService {
    private static final Duration FALLBACK_ACCESS = Duration.ofDays(30);

    private final UserPurchasedCourseRepository userPurchasedCourseRepository;
    private final StringRedisTemplate redisTemplate;
    private final CourseFeignClient courseFeignClient;

    @Override
    @Transactional
    public void saveAllPurchasedCourses(List<CoursePurchasedEvent> events) {
        if (events.isEmpty()) {
            log.info("No course purchase events to process");
            return;
        }

        log.info("Processing {} course purchase events", events.size());

        List<UserPurchasedCourse> userPurchasedCourses = events.stream()
                .filter(event -> {
                    boolean process = markIfNotProcessed(event.getMessageId());
                    if (!process) log.debug("Duplicate message ignored: messageId={}", event.getMessageId());
                    return process;
                })
                .map(event -> {
                    CourseResponseDto course = courseFeignClient.getCourseById(event.getCourseId());

                    Instant accessUntil = computeAccessUntil(
                            event.getPurchasedAt(),
                            course.getEndTime(),
                            course.getAccessDurationMin()
                    );

                    log.debug("Create UPC: userId={}, courseId={}, purchasedAt={}, accessUntil={}",
                            event.getUserId(), event.getCourseId(), event.getPurchasedAt(), accessUntil);

                    return UserPurchasedCourse.builder()
                            .userId(event.getUserId())
                            .courseId(event.getCourseId())
                            .purchasedAt(event.getPurchasedAt())
                            .accessUntil(accessUntil)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        userPurchasedCourseRepository.saveAll(userPurchasedCourses);
        log.info("Successfully saved {} UserPurchasedCourse records", userPurchasedCourses.size());
    }

    @Override
    public boolean hasActiveAccess(UUID userId, UUID courseId) {
        boolean hasAccess = userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId)
                .map(p -> p.getAccessUntil().isAfter(Instant.now()))
                .orElse(false);
        log.debug("User access check: userId={}, courseId={}, hasActiveAccess={}", userId, courseId, hasAccess);
        return hasAccess;
    }

    private Instant computeAccessUntil(Instant purchasedAt, Instant courseEnd, Integer accessDurationMin) {

        boolean hasDuration = accessDurationMin != null && accessDurationMin > 0;
        Instant durationUntil = hasDuration ? purchasedAt.plus(Duration.ofMinutes(accessDurationMin)) : null;

        if (courseEnd != null && durationUntil != null) {
            return durationUntil.isBefore(courseEnd) ? durationUntil : courseEnd;
        }

        if (courseEnd != null) {
            return courseEnd;
        }

        if (durationUntil != null) {
            return durationUntil;
        }

        return purchasedAt.plus(FALLBACK_ACCESS);
    }

    private boolean markIfNotProcessed(UUID messageId) {
        String key = "course_purchase_processed_" + messageId;
        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(key, "true", Duration.ofHours(1));
        log.debug("Deduplication result for messageId={}: {}", messageId, wasSet);
        return Boolean.TRUE.equals(wasSet);
    }
}
