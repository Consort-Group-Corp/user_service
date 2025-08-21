package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.forum.ForumAuthorDto;
import uz.consortgroup.core.api.v1.dto.forum.ForumAuthorIdsRequest;
import uz.consortgroup.userservice.service.forum.ForumAuthorDirectoryService;
import uz.consortgroup.userservice.service.forum.ForumMembershipDirectoryService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/forum-directory")
@RequiredArgsConstructor
@Hidden
public class ForumDirectoryController {

    private final ForumAuthorDirectoryService authorService;
    private final ForumMembershipDirectoryService membershipService;

    @PostMapping("/authors/batch")
    public Map<UUID, ForumAuthorDto> getAuthors(@RequestBody ForumAuthorIdsRequest request) {
        return authorService.getAuthors(request.getIds());
    }

    @GetMapping("/users/{userId}/groups")
    public List<UUID> getGroupIdsForUser(@PathVariable UUID userId) {
        return membershipService.getGroupIdsForUser(userId);
    }
}
