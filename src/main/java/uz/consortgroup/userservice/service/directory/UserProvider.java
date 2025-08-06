package uz.consortgroup.userservice.service.directory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProvider implements UserInfoProvider {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public boolean supports(String type) {
        return "USER".equalsIgnoreCase(type);
    }

    @Override
    public String getType() {
        return "USER";
    }

    @Override
    public Optional<UserShortInfoResponseDto> getById(UUID userId) {
        log.info("Fetching short user info by userId = {}", userId);
        return userRepository.findById(userId)
                .map(userMapper::toUserShortInfoResponseDto);
    }

    @Override
    public Map<UUID, UserShortInfoResponseDto> getByIds(List<UUID> userIds) {
        log.info("Fetching short user info for userIds: {}", userIds);
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, userMapper::toUserShortInfoResponseDto));
    }
}
