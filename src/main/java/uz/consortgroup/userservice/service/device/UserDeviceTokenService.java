package uz.consortgroup.userservice.service.device;

import org.springframework.data.domain.Page;
import uz.consortgroup.core.api.v1.dto.user.request.RegisterDeviceTokenRequest;
import uz.consortgroup.core.api.v1.dto.user.response.FcmTokenDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserDeviceTokenService {
    void registerToken(UUID userId, RegisterDeviceTokenRequest request);
    Page<FcmTokenDto> getActiveTokensPaged(int page, int size);
    Page<FcmTokenDto> getActiveTokensByUserId(UUID userId, int page, int size);
    Map<UUID, List<FcmTokenDto>> getTokensByUserIds(List<UUID> userIds);
}
