package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.user.request.UserBulkSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.request.UserSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.response.UserBulkSearchResponse;
import uz.consortgroup.core.api.v1.dto.user.response.UserSearchResponse;
import uz.consortgroup.userservice.service.user.UserSearchService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/users")
@Validated
@Hidden
public class UserSearchController {
    private final UserSearchService userSearchService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/search")
    public UserSearchResponse searchUsers(@Valid @RequestBody UserSearchRequest request) {
        return userSearchService.findUserByEmailOrPinfl(request);
    }

    @PostMapping("/bulk-search")
    @ResponseStatus(HttpStatus.OK)
    public UserBulkSearchResponse searchUsersBulk(@Valid @RequestBody UserBulkSearchRequest request) {
        return userSearchService.bulkUserSearch(request);
    }
}
