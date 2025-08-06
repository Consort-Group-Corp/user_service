package uz.consortgroup.userservice.service.directory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.userservice.entity.SuperAdmin;
import uz.consortgroup.userservice.mapper.SuperAdminMapper;
import uz.consortgroup.userservice.repository.SuperAdminRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminProvider implements UserInfoProvider {

    private final SuperAdminRepository superAdminRepository;
    private final SuperAdminMapper superAdminMapper;

    @Override
    public boolean supports(String type) {
        return "SUPER_ADMIN".equalsIgnoreCase(type);
    }

    @Override
    public String getType() {
        return "SUPER_ADMIN";
    }

    @Override
    public Optional<UserShortInfoResponseDto> getById(UUID userId) {
        log.info("Fetching short user info by userId = {}", userId);
        return superAdminRepository.findById(userId)
                .map(superAdminMapper::toShortInfoDto);
    }

    @Override
    public Map<UUID, UserShortInfoResponseDto> getByIds(List<UUID> userIds) {
        log.info("Fetching short user info for userIds: {}", userIds);
        return superAdminRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(SuperAdmin::getId, superAdminMapper::toShortInfoDto));
    }
}
