package uz.consortgroup.userservice.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uz.consortgroup.core.api.v1.dto.user.response.UserFullInfoResponseDto;

import java.util.UUID;

public interface UserQueryService {
    Page<UserFullInfoResponseDto> getAllUsersFullInfo(Pageable pageable);
    UserFullInfoResponseDto getUserFullInfoById(UUID userId);
    UserFullInfoResponseDto getUserFullInfoByToken(String token);
}
