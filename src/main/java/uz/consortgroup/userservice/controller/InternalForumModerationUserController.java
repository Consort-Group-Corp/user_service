package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.forum.moderation.response.ModerationUserInfoResponseDto;
import uz.consortgroup.userservice.service.forum.ModerationUserDirectoryService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/forum")
@Hidden
public class InternalForumModerationUserController {

    private final ModerationUserDirectoryService moderationUserDirectoryService;

    @PostMapping("/moderation-users")
    @ResponseStatus(HttpStatus.OK)
    public Map<UUID, ModerationUserInfoResponseDto> getModerationUsers(List<UUID> ids) {
        return moderationUserDirectoryService.getUsers(ids);
    }
}
