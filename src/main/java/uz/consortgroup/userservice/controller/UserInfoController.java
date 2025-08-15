package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.user.response.UserLanguageInfoDto;
import uz.consortgroup.userservice.service.user.UserInfoService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
@Hidden
public class UserInfoController {
    private final UserInfoService userInfoService;

    @PostMapping("/basic-info")
    @ResponseStatus(HttpStatus.OK)
    public List<UserLanguageInfoDto> getUserBasicInfo(@RequestBody List<UUID> userIds) {
        return userInfoService.getUserLanguages(userIds);
    }
}
