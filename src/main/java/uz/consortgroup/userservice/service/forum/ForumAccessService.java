package uz.consortgroup.userservice.service.forum;

import uz.consortgroup.core.api.v1.dto.forum.ForumAccessRequest;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessResponse;

import java.util.UUID;

public interface ForumAccessService {
    ForumAccessResponse checkAccess(ForumAccessRequest request);
    UUID getCourseIdByForumId(UUID groupId);
}
