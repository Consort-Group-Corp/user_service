package uz.consortgroup.userservice.service.device;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.user.request.RegisterDeviceTokenRequest;
import uz.consortgroup.core.api.v1.dto.user.response.FcmTokenDto;
import uz.consortgroup.userservice.entity.UserDeviceToken;
import uz.consortgroup.userservice.repository.UserDeviceTokenRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDeviceTokenServiceImpl implements UserDeviceTokenService {

    private final UserDeviceTokenRepository userDeviceTokenRepository;

    @Override
    @Transactional
    public void registerToken(UUID userId, RegisterDeviceTokenRequest request) {
        log.info("Registering FCM token for userId: {}, token: {}", userId, request.getFcmToken());

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
            log.info("Deactivating {} old token(s) for FCM: {}", oldTokens.size(), request.getFcmToken());
            userDeviceTokenRepository.saveAll(oldTokens);
        }

        UserDeviceToken newToken = UserDeviceToken.builder()
                .userId(userId)
                .fcmToken(request.getFcmToken())
                .deviceType(request.getDeviceType())
                .language(request.getLanguage())
                .appVersion(request.getAppVersion())
                .deviceInfo(request.getDeviceInfo())
                .isActive(true)
                .lastSeen(now)
                .createdAt(now)
                .build();

        userDeviceTokenRepository.save(newToken);
        log.info("Saved new FCM token for userId: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FcmTokenDto> getActiveTokensPaged(int page, int size) {
        log.info("Fetching active FCM tokens page={}, size={}", page, size);
        Page<FcmTokenDto> tokens = userDeviceTokenRepository
                .findByIsActiveTrue(PageRequest.of(page, size))
                .map(token -> new FcmTokenDto(
                        token.getUserId(),
                        token.getLanguage(),
                        token.getFcmToken(),
                        token.getDeviceType()
                ));
        log.info("Fetched {} active tokens", tokens.getTotalElements());
        return tokens;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FcmTokenDto> getActiveTokensByUserId(UUID userId, int page, int size) {
        log.info("Fetching active FCM tokens for userId={}, page={}, size={}", userId, page, size);
        Page<FcmTokenDto> tokens = userDeviceTokenRepository
                .findByUserIdAndIsActiveTrue(userId, PageRequest.of(page, size))
                .map(token -> new FcmTokenDto(
                        token.getUserId(),
                        token.getLanguage(),
                        token.getFcmToken(),
                        token.getDeviceType()
                ));
        log.info("Fetched {} active tokens for userId={}", tokens.getTotalElements(), userId);
        return tokens;
    }

    @Override
    public Map<UUID, List<FcmTokenDto>> getTokensByUserIds(List<UUID> userIds) {
        log.info("Fetching active FCM tokens for {} userIds", userIds.size());

        List<UserDeviceToken> tokens = userDeviceTokenRepository.findAllByUserIdInAndIsActiveTrue(userIds);

        Map<UUID, List<FcmTokenDto>> result = tokens.stream()
                .map(token -> FcmTokenDto.builder()
                        .userId(token.getUserId())
                        .fcmToken(token.getFcmToken())
                        .language(token.getLanguage())
                        .deviceType(token.getDeviceType())
                        .build())
                .collect(Collectors.groupingBy(FcmTokenDto::getUserId));

        log.info("Fetched tokens for {} users", result.size());
        return result;
    }
}
