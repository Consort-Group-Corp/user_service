package uz.consortgroup.userservice.service.purchases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final UserPurchasedCourseRepository userPurchasedCourseRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public void saveAllPurchasedCourses(List<CoursePurchasedEvent> coursePurchasedEvents) {
        if (coursePurchasedEvents.isEmpty()) {
            log.info("No course purchase events to process");
            return;
        }

        log.info("Processing {} course purchase events", coursePurchasedEvents.size());

        List<UserPurchasedCourse> userPurchasedCourses = coursePurchasedEvents.stream()
                .filter(event -> {
                    boolean process = markIfNotProcessed(event.getMessageId());
                    if (!process) {
                        log.debug("Duplicate message ignored: messageId={}", event.getMessageId());
                    }
                    return process;
                })
                .map(event -> {
                    log.debug("Creating UserPurchasedCourse: userId={}, courseId={}, purchasedAt={}, accessUntil={}",
                            event.getUserId(), event.getCourseId(), event.getPurchasedAt(), event.getAccessUntil());
                    return UserPurchasedCourse.builder()
                            .userId(event.getUserId())
                            .courseId(event.getCourseId())
                            .purchasedAt(event.getPurchasedAt())
                            .accessUntil(event.getAccessUntil())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        try {
            userPurchasedCourseRepository.saveAll(userPurchasedCourses);
            log.info("Successfully saved {} UserPurchasedCourse records", userPurchasedCourses.size());
        } catch (Exception e) {
            log.error("Failed to save user purchased courses", e);
            throw new RuntimeException("Failed to save user purchased course", e);
        }
    }

    @Override
    public boolean hasActiveAccess(UUID userId, UUID courseId) {
        boolean hasAccess = userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId)
                .map(p -> p.getAccessUntil().isAfter(Instant.now()))
                .orElse(false);
        log.debug("User access check: userId={}, courseId={}, hasActiveAccess={}", userId, courseId, hasAccess);
        return hasAccess;
    }

    private boolean markIfNotProcessed(UUID messageId) {
        String key = "course_purchase_processed_" + messageId;
        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(key, "true", Duration.ofHours(1));
        log.debug("Deduplication result for messageId={}: {}", messageId, wasSet);
        return Boolean.TRUE.equals(wasSet);
    }
}
