package uz.consortgroup.userservice.service.forum_group;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.entity.ForumUserGroupMembership;
import uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent;
import uz.consortgroup.userservice.repository.ForumUserGroupMembershipRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ForumUserGroupMembershipServiceImpl implements ForumUserGroupMembershipService {
    private final ForumUserGroupMembershipRepository forumUserGroupMembershipRepository;
    private final CourseForumGroupCreationService courseForumGroupCreationService;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    @AllAspect
    public void saveAllPurchasedCourses(List<CoursePurchasedEvent> events) {
        if (events.isEmpty()) {
            return;
        }

        List<MembershipResult> results = events.stream()
                .filter(this::isNewEvent)
                .flatMap(this::prepareMemberships)
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            return;
        }

        saveMemberships(results);
    }

    private boolean isNewEvent(CoursePurchasedEvent event) {
        return !isProcessed(event.getMessageId());
    }

    private Stream<MembershipResult> prepareMemberships(CoursePurchasedEvent event) {
        UUID courseId = event.getCourseId();
        UUID userId = event.getUserId();

        return courseForumGroupCreationService.findByCourseId(courseId)
                .map(courseForumGroup -> {
                    UUID groupId = courseForumGroup.getGroupId();
                    if (forumUserGroupMembershipRepository.existsByUserIdAndGroupId(userId, groupId)) {
                        return null;
                    }
                    return new MembershipResult(
                            event.getMessageId(),
                            ForumUserGroupMembership.builder()
                                    .userId(userId)
                                    .groupId(groupId)
                                    .joinedAt(Instant.now())
                                    .build()
                    );
                }).stream();
    }

    private void saveMemberships(List<MembershipResult> results) {
        List<ForumUserGroupMembership> memberships = results.stream()
                .map(MembershipResult::membership)
                .collect(Collectors.toList());

        forumUserGroupMembershipRepository.saveAll(memberships);
        results.forEach(result -> markAsProcessed(result.messageId()));
    }

    private boolean isProcessed(UUID messageId) {
        String key = redisKey(messageId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private void markAsProcessed(UUID messageId) {
        String key = redisKey(messageId);
        redisTemplate.opsForValue().set(key, "true", Duration.ofHours(1));
    }

    private String redisKey(UUID messageId) {
        return "forum_membership_processed_" + messageId;
    }

    private record MembershipResult(UUID messageId, ForumUserGroupMembership membership) {}
}