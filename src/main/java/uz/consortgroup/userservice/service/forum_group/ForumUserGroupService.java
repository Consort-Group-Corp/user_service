package uz.consortgroup.userservice.service.forum_group;

import uz.consortgroup.core.api.v1.dto.user.enumeration.ForumAccessType;
import uz.consortgroup.userservice.entity.ForumUserGroup;

import java.util.UUID;

public interface ForumUserGroupService {
    ForumUserGroup create(UUID courseId, String title, UUID ownerId, ForumAccessType forumAccessType);
}
