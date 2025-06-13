package uz.consortgroup.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.forum.CreateForumGroupByHrRequest;
import uz.consortgroup.core.api.v1.dto.forum.HrForumGroupCreateResponse;
import uz.consortgroup.userservice.service.forum_group.HrForumGroupService;

@RestController
@RequestMapping("/api/v1/hr")
@RequiredArgsConstructor
@Validated
public class HrController {
    private final HrForumGroupService hrForumGroupService;

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public HrForumGroupCreateResponse createForumGroupByHr(@Valid @RequestBody CreateForumGroupByHrRequest request) {
        return hrForumGroupService.createHrForumGroup(request);
    }
}
