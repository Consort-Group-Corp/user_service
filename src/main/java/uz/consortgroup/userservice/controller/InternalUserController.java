package uz.consortgroup.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.userservice.service.directory.UserDirectoryService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/users")
public class InternalUserController {
    private final UserDirectoryService userDirectoryService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{userId}/short-info")
    public Optional<UserShortInfoResponseDto> getUserShortInfoById(@PathVariable("userId") UUID userId) {
        return userDirectoryService.getUserInfo(userId);
    }

    @PostMapping("/short-info")
    @ResponseStatus(HttpStatus.OK)
    public Map<UUID, UserShortInfoResponseDto> getShortInfoBulk(@RequestBody List<UUID> userIds) {
        return userDirectoryService.getUserInfoBulk(userIds);
    }
}
