package uz.consortgroup.userservice.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.service.operation.UserOperationsService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserShortInfoServiceImpl implements UserShortInfoService {
    private final UserMapper userMapper;
    private final UserOperationsService userOperationsService;

    @Override
    @AllAspect
    public UserShortInfoResponseDto getUserShortInfoById(UUID userId) {
        User userShortInfo = userOperationsService.getUserFromDbAndCacheById(userId);
        return userMapper.toUserShortInfoResponseDto(userShortInfo);
    }
}
