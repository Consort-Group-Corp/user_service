package uz.consortgroup.userservice.service.user;

import uz.consortgroup.core.api.v1.dto.user.response.UserLanguageInfoDto;

import java.util.List;
import java.util.UUID;

public interface UserInfoService {
    List<UserLanguageInfoDto> getUserLanguages(List<UUID> userIds);
}
