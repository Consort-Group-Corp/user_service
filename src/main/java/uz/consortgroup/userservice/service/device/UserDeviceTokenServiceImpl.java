package uz.consortgroup.userservice.service.device;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.user.request.RegisterDeviceTokenRequest;
import uz.consortgroup.core.api.v1.dto.user.response.FcmTokenDto;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.entity.UserDeviceToken;
import uz.consortgroup.userservice.repository.UserDeviceTokenRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDeviceTokenServiceImpl implements UserDeviceTokenService {

    private final UserDeviceTokenRepository userDeviceTokenRepository;

    @Override
    @AllAspect
    @Transactional
    public void registerToken(UUID userId, RegisterDeviceTokenRequest request) {
        LocalDateTime now = LocalDateTime.now();

        List<UserDeviceToken> oldTokens = userDeviceTokenRepository
                .findByFcmTokenAndIsActiveTrue(request.getFcmToken())
                .stream()
                .peek(token -> {
                    token.setIsActive(false);
                    token.setUpdatedAt(now);
                })
                .toList();

        if (!oldTokens.isEmpty()) {
            userDeviceTokenRepository.saveAll(oldTokens);
        }

        UserDeviceToken newToken = UserDeviceToken.builder()
                .userId(userId)
                .fcmToken(request.getFcmToken())
                .deviceType(request.getDeviceType())
                .language(request.getLanguage())
                .appVersion(request.getAppVersion())
                .deviceInfo(request.getDeviceInfo())
                .language(request.getLanguage())
                .isActive(true)
                .lastSeen(now)
                .createdAt(now)
                .build();

        userDeviceTokenRepository.save(newToken);
    }

    @Override
    @AllAspect
    @Transactional(readOnly = true)
    public Page<FcmTokenDto> getActiveTokensPaged(int page, int size) {
        return userDeviceTokenRepository
                .findByIsActiveTrue(PageRequest.of(page, size))
                .map(token -> new FcmTokenDto(
                        token.getUserId(),
                        token.getLanguage(),
                        token.getFcmToken(),
                        token.getDeviceType()
                ));
    }

    @Override
    @AllAspect
    @Transactional(readOnly = true)
    public Page<FcmTokenDto> getActiveTokensByUserId(UUID userId, int page, int size) {
        return userDeviceTokenRepository
                .findByUserIdAndIsActiveTrue(userId, PageRequest.of(page, size))
                .map(token -> new FcmTokenDto(
                        token.getUserId(),
                        token.getLanguage(),
                        token.getFcmToken(),
                        token.getDeviceType()
                ));
    }

    @Override
    @AllAspect
    public Map<UUID, List<FcmTokenDto>> getTokensByUserIds(List<UUID> userIds) {
        List<UserDeviceToken> tokens = userDeviceTokenRepository.findAllByUserIdInAndIsActiveTrue(userIds);

        return tokens.stream()
                .map(token -> FcmTokenDto.builder()
                        .userId(token.getUserId())
                        .fcmToken(token.getFcmToken())
                        .language(token.getLanguage())
                        .deviceType(token.getDeviceType())
                        .build())
                .collect(Collectors.groupingBy(FcmTokenDto::getUserId));
    }
}
