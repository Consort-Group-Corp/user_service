package uz.consortgroup.userservice.service.forum_group;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.entity.CourseForumGroup;
import uz.consortgroup.userservice.event.course_group.CourseForumGroupCreatedEvent;
import uz.consortgroup.userservice.repository.CourseForumGroupRepository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseForumGroupCreationServiceImpl implements CourseForumGroupCreationService {
    private final CourseForumGroupRepository courseForumGroupRepository;
    private final StringRedisTemplate redisTemplate;

    @AllAspect
    @Transactional
    public void saveAllForumGroupCreations(List<CourseForumGroupCreatedEvent> courseForumGroupCreatedEvents) {
        if (courseForumGroupCreatedEvents.isEmpty()) {
            return;
        }

        List<CourseForumGroup> courseForumGroups = courseForumGroupCreatedEvents.stream()
                .filter(event -> markIfNotProcessed(event.getMessageId()))
                .map(event -> CourseForumGroup.builder()
                        .courseId(event.getCourseId())
                        .groupId(event.getGroupId())
                        .startTime(event.getStartTime())
                        .endTime(event.getEndTime())
                        .createdAt(event.getCreatedAt())
                        .build())
                .toList();

        try {
            courseForumGroupRepository.saveAll(courseForumGroups);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save course forum group", e);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CourseForumGroup> findByCourseId(UUID courseId) {
        return courseForumGroupRepository.findByCourseId(courseId);
    }


    private boolean markIfNotProcessed(UUID messageId) {
        String key = "event_processed_" + messageId;
        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(key, "true", Duration.ofHours(1));
        return Boolean.TRUE.equals(wasSet);
    }
}
