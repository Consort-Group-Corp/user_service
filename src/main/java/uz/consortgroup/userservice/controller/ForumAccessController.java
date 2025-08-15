package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessRequest;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessResponse;
import uz.consortgroup.userservice.service.forum.ForumAccessService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/forum-access")
@RequiredArgsConstructor
@Validated
@Hidden
public class ForumAccessController {
    private final ForumAccessService forumAccessService;

    @PostMapping("/access")
    @ResponseStatus(HttpStatus.OK)
    public ForumAccessResponse checkAccess(@Valid @RequestBody ForumAccessRequest request) {
        return forumAccessService.checkAccess(request);
    }

    @GetMapping("/course-id/{groupId}")
    @ResponseStatus(HttpStatus.OK)
    public UUID getCourseIdByGroupId(@PathVariable UUID groupId) {
        return forumAccessService.getCourseIdByGroupId(groupId);
    }
}
