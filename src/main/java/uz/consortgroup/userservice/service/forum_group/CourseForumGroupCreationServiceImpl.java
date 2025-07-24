package uz.consortgroup.userservice.service.forum_group;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.entity.CourseForumGroup;
import uz.consortgroup.userservice.event.course_group.CourseForumGroupCreatedEvent;
import uz.consortgroup.userservice.repository.CourseForumGroupRepository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseForumGroupCreationServiceImpl implements CourseForumGroupCreationService {

    private final CourseForumGroupRepository courseForumGroupRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public void saveAllForumGroupCreations(List<CourseForumGroupCreatedEvent> courseForumGroupCreatedEvents) {
        log.info("Saving forum group creations, incoming events count: {}", courseForumGroupCreatedEvents.size());

        if (courseForumGroupCreatedEvents.isEmpty()) {
            log.warn("Received empty list of course forum group events, skipping");
            return;
        }

        List<CourseForumGroup> courseForumGroups = courseForumGroupCreatedEvents.stream()
                .filter(event -> {
                    boolean shouldProcess = markIfNotProcessed(event.getMessageId());
                    if (!shouldProcess) {
                        log.info("Duplicate event skipped with messageId: {}", event.getMessageId());
                    }
                    return shouldProcess;
                })
                .map(event -> {
                    log.info("Processing new course forum group event: {}", event.getMessageId());
                    return CourseForumGroup.builder()
                            .courseId(event.getCourseId())
                            .groupId(event.getGroupId())
                            .startTime(event.getStartTime())
                            .endTime(event.getEndTime())
                            .createdAt(event.getCreatedAt())
                            .build();
                })
                .toList();

        if (courseForumGroups.isEmpty()) {
            log.info("No new forum groups to save after deduplication.");
            return;
        }

        try {
            courseForumGroupRepository.saveAll(courseForumGroups);
            log.info("Successfully saved {} course forum groups", courseForumGroups.size());
        } catch (Exception e) {
            log.error("Failed to save course forum group list", e);
            throw new RuntimeException("Failed to save course forum group", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CourseForumGroup> findByCourseId(UUID courseId) {
        log.info("Finding course forum group by courseId: {}", courseId);
        Optional<CourseForumGroup> result = courseForumGroupRepository.findByCourseId(courseId);
        log.info("Course forum group found by courseId={} → present={}", courseId, result.isPresent());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CourseForumGroup> findByGroupId(UUID groupId) {
        log.info("Finding course forum group by groupId: {}", groupId);
        Optional<CourseForumGroup> result = courseForumGroupRepository.findByGroupId(groupId);
        log.info("Course forum group found by groupId={} → present={}", groupId, result.isPresent());
        return result;
    }

    private boolean markIfNotProcessed(UUID messageId) {
        String key = "event_processed_" + messageId;
        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(key, "true", Duration.ofHours(1));
        boolean result = Boolean.TRUE.equals(wasSet);
        log.debug("Deduplication check for messageId {} → shouldProcess={}", messageId, result);
        return result;
    }
}
