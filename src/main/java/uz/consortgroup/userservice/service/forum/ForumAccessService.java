package uz.consortgroup.userservice.service.forum;

import uz.consortgroup.core.api.v1.dto.forum.ForumAccessByCourseRequest;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessByGroupRequest;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessResponse;

import java.util.UUID;

public interface ForumAccessService {
    ForumAccessResponse checkAccessByCourse(ForumAccessByCourseRequest request);
    ForumAccessResponse checkAccessByGroup(ForumAccessByGroupRequest request);
    UUID getCourseIdByGroupId(UUID groupId);
}
