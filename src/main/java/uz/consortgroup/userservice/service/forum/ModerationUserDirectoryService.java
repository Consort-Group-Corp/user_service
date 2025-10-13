package uz.consortgroup.userservice.service.forum;


import uz.consortgroup.core.api.v1.dto.forum.moderation.response.ModerationUserInfoResponseDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ModerationUserDirectoryService {
    Map<UUID, ModerationUserInfoResponseDto> getUsers(List<UUID> ids);
}
