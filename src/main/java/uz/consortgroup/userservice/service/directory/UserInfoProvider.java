package uz.consortgroup.userservice.service.directory;

import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserInfoProvider {
    boolean supports(String type);
    String getType();
    Optional<UserShortInfoResponseDto> getById(UUID userId);
    Map<UUID, UserShortInfoResponseDto> getByIds(List<UUID> userIds);
}
