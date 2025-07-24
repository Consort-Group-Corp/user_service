package uz.consortgroup.userservice.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessReason;
import uz.consortgroup.userservice.repository.ForumUserGroupMembershipRepository;
import uz.consortgroup.userservice.service.forum_group.CourseForumGroupCreationService;
import uz.consortgroup.userservice.service.purchases.CoursePurchaseService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ForumAccessValidator {
    private final CourseForumGroupCreationService courseForumGroupCreationService;
    private final ForumUserGroupMembershipRepository forumUserGroupMembershipRepository;
    private final CoursePurchaseService coursePurchaseService;

    public ForumAccessReason validateAccess(UUID courseId, UUID userId) {
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
}
