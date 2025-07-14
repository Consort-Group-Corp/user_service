package uz.consortgroup.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.userservice.service.user.UserService;
import uz.consortgroup.userservice.service.user.UserShortInfoService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/users")
public class InternalUserController {
    private final UserShortInfoService userShortInfoService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{userId}/short-info")
    public UserShortInfoResponseDto getUserShortInfoById(@PathVariable("userId") UUID userId) {
        return userShortInfoService.getUserShortInfoById(userId);
    }
}
