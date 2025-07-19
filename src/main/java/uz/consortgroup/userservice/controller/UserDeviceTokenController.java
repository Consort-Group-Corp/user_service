package uz.consortgroup.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uz.consortgroup.core.api.v1.dto.user.request.RegisterDeviceTokenRequest;
import uz.consortgroup.core.api.v1.dto.user.response.FcmTokenDto;
import uz.consortgroup.userservice.service.device.UserDeviceTokenService;
import uz.consortgroup.userservice.service.impl.UserDetailsImpl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/device-tokens")
@RequiredArgsConstructor
public class UserDeviceTokenController {

    private final UserDeviceTokenService userDeviceTokenService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void registerToken(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody RegisterDeviceTokenRequest request) {
        UUID userId = userDetails.getId();
        userDeviceTokenService.registerToken(userId, request);
    }

    @GetMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    public Page<FcmTokenDto> getUserTokens(
            @AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId = userDetails.getId();
        return userDeviceTokenService.getActiveTokensByUserId(userId, page, size);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public Page<FcmTokenDto> getAllActiveTokens(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return userDeviceTokenService.getActiveTokensPaged(page, size);
    }

    @PostMapping("/by-user-ids")
    @ResponseStatus(HttpStatus.OK)
    public Map<UUID, List<FcmTokenDto>> getTokensByUserIds(@RequestBody List<UUID> userIds) {
        return userDeviceTokenService.getTokensByUserIds(userIds);
    }

}
