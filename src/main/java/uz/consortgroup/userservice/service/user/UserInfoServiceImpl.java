package uz.consortgroup.userservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.response.UserLanguageInfoDto;
import uz.consortgroup.userservice.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoServiceImpl implements UserInfoService {

    private final UserRepository userRepository;

    @Override
    public List<UserLanguageInfoDto> getUserLanguages(List<UUID> userIds) {
        List<UserLanguageInfoDto> result = userRepository.findByIdIn(userIds).stream()
                .map(user -> new UserLanguageInfoDto(
                        user.getId(),
                        user.getLanguage().name().toLowerCase()
                ))
                .toList();

        log.info("Retrieved language settings for users: {}", userIds);
        return result;
    }
}
