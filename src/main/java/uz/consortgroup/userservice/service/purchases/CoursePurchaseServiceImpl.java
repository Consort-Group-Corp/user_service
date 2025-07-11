package uz.consortgroup.userservice.service.purchases;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
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
public class CoursePurchaseServiceImpl implements CoursePurchaseService {
    private final UserPurchasedCourseRepository userPurchasedCourseRepository;
    private final StringRedisTemplate redisTemplate;

    @AllAspect
    @Transactional
    public void saveAllPurchasedCourses(List<CoursePurchasedEvent> coursePurchasedEvents) {
        if (coursePurchasedEvents.isEmpty()) {
            return;
        }

        List<UserPurchasedCourse> userPurchasedCourses = coursePurchasedEvents.stream()
                .filter(event -> markIfNotProcessed(event.getMessageId()))
                .map(event -> UserPurchasedCourse.builder()
                        .userId(event.getUserId())
                        .courseId(event.getCourseId())
                        .purchasedAt(event.getPurchasedAt())
                        .accessUntil(event.getAccessUntil())
                        .build())
                .filter(Objects::nonNull)
                .toList();

        try {
            userPurchasedCourseRepository.saveAll(userPurchasedCourses);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user purchased course", e);
        }
    }

    @Override
    public boolean hasActiveAccess(UUID userId, UUID courseId) {
        return userPurchasedCourseRepository.findByUserIdAndCourseId(userId, courseId)
                .map(p -> p.getAccessUntil().isAfter(Instant.now()))
                .orElse(false);
    }

    private boolean markIfNotProcessed(UUID messageId) {
        String key = "course_purchase_processed_" + messageId;
        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(key, "true", Duration.ofHours(1));
        return Boolean.TRUE.equals(wasSet);
    }
}
