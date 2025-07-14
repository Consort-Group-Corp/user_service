package uz.consortgroup.userservice.service.user;

import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;

import java.util.UUID;

public interface UserShortInfoService {
    UserShortInfoResponseDto getUserShortInfoById(UUID userId);
}
