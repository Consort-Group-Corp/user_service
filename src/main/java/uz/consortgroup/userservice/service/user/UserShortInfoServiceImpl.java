package uz.consortgroup.userservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.service.operation.UserOperationsService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserShortInfoServiceImpl implements UserShortInfoService {
    private final UserMapper userMapper;
    private final UserOperationsService userOperationsService;

    @Override
    public UserShortInfoResponseDto getUserShortInfoById(UUID userId) {
        log.info("Fetching short user info by userId = {}", userId);
        User userShortInfo = userOperationsService.getUserFromDbAndCacheById(userId);
        UserShortInfoResponseDto responseDto = userMapper.toUserShortInfoResponseDto(userShortInfo);
        log.info("Successfully retrieved short user info: {}", responseDto);
        return responseDto;
    }
}
