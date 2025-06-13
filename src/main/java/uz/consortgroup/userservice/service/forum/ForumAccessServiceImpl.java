package uz.consortgroup.userservice.service.forum;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessReason;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessRequest;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessResponse;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.entity.CourseForumGroup;
import uz.consortgroup.userservice.service.forum_group.CourseForumGroupCreationService;
import uz.consortgroup.userservice.validator.ForumAccessValidator;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForumAccessServiceImpl implements ForumAccessService {
    private final ForumAccessValidator forumAccessValidator;
    private final CourseForumGroupCreationService courseForumGroupCreationService;


    @Override
    @AllAspect
    public ForumAccessResponse checkAccess(ForumAccessRequest request) {
        ForumAccessReason reason = forumAccessValidator.validateAccess(request.getCourseId(), request.getUserId());
        boolean hasAccess = reason == ForumAccessReason.USER_HAS_ACCESS;

        return ForumAccessResponse.builder()
                .hasAccess(hasAccess)
                .reason(reason)
                .build();
    }

    @Override
    @AllAspect
    public UUID getCourseIdByForumId(UUID forumId) {
        return courseForumGroupCreationService.findByGroupId(forumId)
                .map(CourseForumGroup::getCourseId)
                .orElseThrow(() -> new IllegalArgumentException("Forum group not found for id: " + forumId));
    }
}
