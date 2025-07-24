package uz.consortgroup.userservice.service.forum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessReason;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessRequest;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessResponse;
import uz.consortgroup.userservice.entity.CourseForumGroup;
import uz.consortgroup.userservice.service.forum_group.CourseForumGroupCreationService;
import uz.consortgroup.userservice.validator.ForumAccessValidator;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForumAccessServiceImpl implements ForumAccessService {

    private final ForumAccessValidator forumAccessValidator;
    private final CourseForumGroupCreationService courseForumGroupCreationService;

    @Override
    public ForumAccessResponse checkAccess(ForumAccessRequest request) {
        UUID courseId = request.getCourseId();
        UUID userId = request.getUserId();

        log.info("Checking forum access: userId={}, courseId={}", userId, courseId);

        ForumAccessReason reason = forumAccessValidator.validateAccess(courseId, userId);
        boolean hasAccess = reason == ForumAccessReason.USER_HAS_ACCESS;

        ForumAccessResponse response = ForumAccessResponse.builder()
                .hasAccess(hasAccess)
                .reason(reason)
                .build();

        log.info("Forum access check result: userId={}, courseId={}, hasAccess={}, reason={}",
                userId, courseId, hasAccess, reason);

        return response;
    }

    @Override
    public UUID getCourseIdByGroupId(UUID groupId) {
        log.info("Retrieving courseId by forum groupId: {}", groupId);
        return courseForumGroupCreationService.findByGroupId(groupId)
                .map(CourseForumGroup::getCourseId)
                .orElseThrow(() -> {
                    log.error("Forum group not found for groupId: {}", groupId);
                    return new IllegalArgumentException("Forum group not found for id: " + groupId);
                });
    }
}
