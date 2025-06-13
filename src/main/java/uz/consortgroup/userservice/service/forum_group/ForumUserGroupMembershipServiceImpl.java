package uz.consortgroup.userservice.service.forum_group;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.entity.CourseForumGroup;
import uz.consortgroup.userservice.entity.ForumUserGroupMembership;
import uz.consortgroup.userservice.event.coursepurchased.CoursePurchasedEvent;
import uz.consortgroup.userservice.repository.ForumUserGroupMembershipRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForumUserGroupMembershipServiceImpl implements ForumUserGroupMembershipService {
    private final ForumUserGroupMembershipRepository forumUserGroupMembershipRepository;
    private final CourseForumGroupCreationService courseForumGroupCreationService;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    @AllAspect
    public void assignUsersToForumGroups(List<CoursePurchasedEvent> events) {
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

    @Override
    @Transactional
    @AllAspect
    public void assignUsers(UUID groupId, List<UUID> userIds) {
        List<ForumUserGroupMembership> memberships = userIds.stream()
                .filter(userId -> !forumUserGroupMembershipRepository.existsByUserIdAndGroupId(userId, groupId))
                .map(userId -> ForumUserGroupMembership.builder()
                        .userId(userId)
                        .groupId(groupId)
                        .joinedAt(Instant.now())
                        .build())
                .toList();

        forumUserGroupMembershipRepository.saveAll(memberships);
    }

    private boolean isNewEvent(CoursePurchasedEvent event) {
        boolean processed = isProcessed(event.getMessageId());
        log.info("isNewEvent check — messageId={}, processed={}", event.getMessageId(), processed);
        return !processed;
    }

    private Stream<MembershipResult> prepareMemberships(CoursePurchasedEvent event) {
        UUID courseId = event.getCourseId();
        UUID userId = event.getUserId();
        UUID messageId = event.getMessageId();

        log.info("Preparing membership for event: messageId={}, userId={}, courseId={}", messageId, userId, courseId);

        Optional<CourseForumGroup> courseForumGroupOpt = courseForumGroupCreationService.findByCourseId(courseId);
        log.info("Result of findByCourseId({}): present={}", courseId, courseForumGroupOpt.isPresent());

        Optional<ForumUserGroupMembership> membershipOpt = courseForumGroupOpt.flatMap(courseForumGroup -> {
            UUID groupId = courseForumGroup.getGroupId();

            boolean exists = forumUserGroupMembershipRepository.existsByUserIdAndGroupId(userId, groupId);
            log.info("Membership existence check for userId={}, groupId={}: {}", userId, groupId, exists);
            if (exists) {
                log.info("User {} is already a member of forum group {} for course {}", userId, groupId, courseId);
                return Optional.empty();
            }

            ForumUserGroupMembership membership = ForumUserGroupMembership.builder()
                    .userId(userId)
                    .groupId(groupId)
                    .joinedAt(Instant.now())
                    .build();
            return Optional.of(membership);
        });

        if (membershipOpt.isEmpty()) {
            log.warn("No group mapping found for courseId {}. Skipping messageId {}", courseId, messageId);
        }

        return membershipOpt.stream().map(m -> new MembershipResult(messageId, m));
    }


    private void saveMemberships(List<MembershipResult> results) {
        List<ForumUserGroupMembership> memberships = results.stream()
                .map(MembershipResult::membership)
                .collect(Collectors.toList());

        forumUserGroupMembershipRepository.saveAll(memberships);
        results.forEach(result -> markAsProcessed(result.messageId()));
        log.info("Saved {} forum user group memberships.", memberships.size());
    }

    private boolean isProcessed(UUID messageId) {
        String key = redisKey(messageId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private void markAsProcessed(UUID messageId) {
        String key = redisKey(messageId);
        redisTemplate.opsForValue().set(key, "true", Duration.ofHours(1));
        log.debug("Marked message {} as processed in Redis with key {}", messageId, key);
    }

    private String redisKey(UUID messageId) {
        return "forum_membership_processed_" + messageId;
    }

    private record MembershipResult(UUID messageId, ForumUserGroupMembership membership) {}
}
