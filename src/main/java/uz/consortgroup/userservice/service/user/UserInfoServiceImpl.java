package uz.consortgroup.userservice.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.response.UserLanguageInfoDto;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {

    private final UserRepository userRepository;


    @Override
    @AllAspect
    public List<UserLanguageInfoDto> getUserLanguages(List<UUID> userIds) {
        return userRepository.findByIdIn(userIds).stream()
                .map(user -> new UserLanguageInfoDto(
                        user.getId(),
                        user.getLanguage().name().toLowerCase()
                ))
                .toList();
    }
}
