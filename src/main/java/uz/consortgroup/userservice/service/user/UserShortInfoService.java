package uz.consortgroup.userservice.service.user;

import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;

import java.util.List;
import java.util.UUID;

public interface UserShortInfoService {
    UserShortInfoResponseDto getUserShortInfoById(UUID userId);
    List<UserShortInfoResponseDto> getUserShortInfoByIds(List<UUID> userIds);
}
