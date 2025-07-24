package uz.consortgroup.userservice.service.forum_group;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    public void assignUsersToForumGroups(List<CoursePurchasedEvent> events) {
        log.info("Assigning users to forum groups. Incoming event count: {}", events.size());

        if (events.isEmpty()) {
            log.warn("No events to process.");
            return;
        }

        List<MembershipResult> results = events.stream()
                .filter(this::isNewEvent)
                .flatMap(this::prepareMemberships)
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            log.info("No new memberships to create after filtering.");
            return;
        }

        saveMemberships(results);
    }

    @Override
    @Transactional
    public void assignUsers(UUID groupId, List<UUID> userIds) {
        log.info("Assigning {} user(s) to groupId={}", userIds.size(), groupId);

        List<ForumUserGroupMembership> memberships = userIds.stream()
                .filter(userId -> {
                    boolean exists = forumUserGroupMembershipRepository.existsByUserIdAndGroupId(userId, groupId);
                    log.debug("Membership exists check: userId={}, groupId={}, exists={}", userId, groupId, exists);
                    return !exists;
                })
                .map(userId -> ForumUserGroupMembership.builder()
                        .userId(userId)
                        .groupId(groupId)
                        .joinedAt(Instant.now())
                        .build())
                .toList();

        forumUserGroupMembershipRepository.saveAll(memberships);
        log.info("Saved {} new forum memberships to group {}", memberships.size(), groupId);
    }

    private boolean isNewEvent(CoursePurchasedEvent event) {
        boolean processed = isProcessed(event.getMessageId());
        log.info("Deduplication check — messageId={}, alreadyProcessed={}", event.getMessageId(), processed);
        return !processed;
    }

    private Stream<MembershipResult> prepareMemberships(CoursePurchasedEvent event) {
        UUID courseId = event.getCourseId();
        UUID userId = event.getUserId();
        UUID messageId = event.getMessageId();

        log.info("Preparing membership — messageId={}, userId={}, courseId={}", messageId, userId, courseId);

        Optional<CourseForumGroup> courseForumGroupOpt = courseForumGroupCreationService.findByCourseId(courseId);
        log.debug("Course forum group for courseId={} found={}", courseId, courseForumGroupOpt.isPresent());

        Optional<ForumUserGroupMembership> membershipOpt = courseForumGroupOpt.flatMap(courseForumGroup -> {
            UUID groupId = courseForumGroup.getGroupId();

            boolean exists = forumUserGroupMembershipRepository.existsByUserIdAndGroupId(userId, groupId);
            log.debug("Checking existing membership: userId={}, groupId={}, exists={}", userId, groupId, exists);

            if (exists) {
                log.info("User {} already has membership in forum group {} for course {}", userId, groupId, courseId);
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
            log.warn("Skipping messageId={} — no group found or already exists", messageId);
        }

        return membershipOpt.stream().map(m -> new MembershipResult(messageId, m));
    }

    private void saveMemberships(List<MembershipResult> results) {
        List<ForumUserGroupMembership> memberships = results.stream()
                .map(MembershipResult::membership)
                .toList();

        forumUserGroupMembershipRepository.saveAll(memberships);
        results.forEach(result -> markAsProcessed(result.messageId()));

        log.info("Saved {} forum user group memberships", memberships.size());
    }

    private boolean isProcessed(UUID messageId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey(messageId)));
    }

    private void markAsProcessed(UUID messageId) {
        String key = redisKey(messageId);
        redisTemplate.opsForValue().set(key, "true", Duration.ofHours(1));
        log.debug("Marked messageId={} as processed in Redis", messageId);
    }

    private String redisKey(UUID messageId) {
        return "forum_membership_processed_" + messageId;
    }

    private record MembershipResult(UUID messageId, ForumUserGroupMembership membership) {}
}
