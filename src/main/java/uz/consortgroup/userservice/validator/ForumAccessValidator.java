package uz.consortgroup.userservice.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.forum.enumeration.ForumAccessReason;
import uz.consortgroup.userservice.entity.CourseForumGroup;
import uz.consortgroup.userservice.repository.ForumUserGroupMembershipRepository;
import uz.consortgroup.userservice.service.forum_group.CourseForumGroupCreationService;
import uz.consortgroup.userservice.service.purchases.CoursePurchaseService;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ForumAccessValidator {

    private final CourseForumGroupCreationService courseForumGroupCreationService;
    private final ForumUserGroupMembershipRepository forumUserGroupMembershipRepository;
    private final CoursePurchaseService coursePurchaseService;

    public ForumAccessReason validateAccessByCourse(UUID courseId, UUID userId) {
        log.info("Validating forum access for userId={}, courseId={}", userId, courseId);

        return courseForumGroupCreationService.findByCourseId(courseId)
                .map(group -> {
                    boolean isMember = forumUserGroupMembershipRepository.existsByUserIdAndGroupId(userId, group.getGroupId());
                    if (!isMember) {
                        log.warn("User {} is not a member of forum group {}", userId, group.getGroupId());
                        return ForumAccessReason.USER_NOT_IN_GROUP;
                    }

                    boolean hasAccess = coursePurchaseService.hasActiveAccess(userId, courseId);
                    if (hasAccess) {
                        log.info("User {} has active access to course {}", userId, courseId);
                        return ForumAccessReason.USER_HAS_ACCESS;
                    } else {
                        log.warn("User {} had access to course {} but it has expired", userId, courseId);
                        return ForumAccessReason.ACCESS_EXPIRED;
                    }
                })
                .orElseGet(() -> {
                    log.error("Forum group not found for course {}", courseId);
                    return ForumAccessReason.FORUM_GROUP_NOT_FOUND;
                });
    }

    public ForumAccessReason validateAccessByGroup(UUID groupId, UUID userId) {
        log.info("Validating forum access by GROUP: userId={}, groupId={}", userId, groupId);

        Optional<CourseForumGroup> groupOpt = courseForumGroupCreationService.findByGroupId(groupId);
        if (groupOpt.isEmpty()) {
            log.warn("Forum group not found: groupId={}", groupId);
            return ForumAccessReason.FORUM_GROUP_NOT_FOUND;
        }

        CourseForumGroup group = groupOpt.get();
        log.debug("Resolved forum group: groupId={}, courseId={}, startTime={}, endTime={}, createdAt={}",
                group.getGroupId(), group.getCourseId(), group.getStartTime(), group.getEndTime(), group.getCreatedAt());

        boolean member = forumUserGroupMembershipRepository.existsByUserIdAndGroupId(userId, groupId);
        log.debug("Membership check: userId={}, groupId={}, isMember={}", userId, groupId, member);
        if (!member) {
            log.warn("User is NOT a member of group: userId={}, groupId={}", userId, groupId);
            return ForumAccessReason.USER_NOT_IN_GROUP;
        }

        boolean hasAccess = coursePurchaseService.hasActiveAccess(userId, group.getCourseId());
        log.debug("Access-by-purchase check: userId={}, courseId={}, hasActiveAccess={}",
                userId, group.getCourseId(), hasAccess);
        if (!hasAccess) {
            log.warn("User access EXPIRED/ABSENT: userId={}, courseId={}", userId, group.getCourseId());
            return ForumAccessReason.ACCESS_EXPIRED;
        }

        log.info("Access GRANTED: userId={}, groupId={}, courseId={}", userId, groupId, group.getCourseId());
        return ForumAccessReason.USER_HAS_ACCESS;
    }
}
