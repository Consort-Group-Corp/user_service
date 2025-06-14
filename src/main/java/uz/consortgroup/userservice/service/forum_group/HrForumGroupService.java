package uz.consortgroup.userservice.service.forum_group;

import uz.consortgroup.core.api.v1.dto.forum.CreateForumGroupByHrRequest;
import uz.consortgroup.core.api.v1.dto.forum.HrForumGroupCreateResponse;

public interface HrForumGroupService {
    HrForumGroupCreateResponse createHrForumGroup(CreateForumGroupByHrRequest request);
}
