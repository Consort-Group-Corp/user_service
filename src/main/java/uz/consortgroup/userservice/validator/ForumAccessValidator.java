package uz.consortgroup.userservice.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessReason;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.repository.ForumUserGroupMembershipRepository;
import uz.consortgroup.userservice.service.forum_group.CourseForumGroupCreationService;
import uz.consortgroup.userservice.service.purchases.CoursePurchaseService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ForumAccessValidator {
    private final CourseForumGroupCreationService courseForumGroupCreationService;
    private final ForumUserGroupMembershipRepository forumUserGroupMembershipRepository;
    private final CoursePurchaseService coursePurchaseService;

    @AllAspect
    public ForumAccessReason validateAccess(UUID courseId, UUID userId) {
        return courseForumGroupCreationService.findByCourseId(courseId)
                .map(group -> {
                    boolean isMember = forumUserGroupMembershipRepository.existsByUserIdAndGroupId(userId, group.getGroupId());
                    if (!isMember) {
                        return ForumAccessReason.USER_NOT_IN_GROUP;
                    }

                    boolean hasAccess = coursePurchaseService.hasActiveAccess(userId, courseId);
                    return hasAccess ? ForumAccessReason.USER_HAS_ACCESS : ForumAccessReason.ACCESS_EXPIRED;
                }).orElse(ForumAccessReason.FORUM_GROUP_NOT_FOUND);
    }
}
