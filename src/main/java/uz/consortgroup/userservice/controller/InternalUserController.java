package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.course.response.course.EnrollmentFilterRequest;
import uz.consortgroup.core.api.v1.dto.user.request.UserBulkSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.request.UserSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.response.UserBulkSearchResponse;
import uz.consortgroup.core.api.v1.dto.user.response.UserLanguageInfoDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserSearchResponse;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.userservice.service.directory.UserDirectoryService;
import uz.consortgroup.userservice.service.user.EnrollmentQueryService;
import uz.consortgroup.userservice.service.user.UserIdentifierResolverService;
import uz.consortgroup.userservice.service.user.UserInfoService;
import uz.consortgroup.userservice.service.user.UserSearchService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/users")
@Hidden
public class InternalUserController {
    private final UserDirectoryService userDirectoryService;
    private final EnrollmentQueryService enrollmentQueryService;
    private final UserIdentifierResolverService userIdentifierResolverService;
    private final UserSearchService userSearchService;
    private final UserInfoService userInfoService;

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

    @PostMapping("/enrollments/filter")
    @ResponseStatus(HttpStatus.OK)
    public List<UUID> filterEnrolled(@RequestBody EnrollmentFilterRequest req) {
        return enrollmentQueryService.filterEnrolled(req.getCourseId(), req.getUserIds());
    }

    @PostMapping("/bulk-search")
    @ResponseStatus(HttpStatus.OK)
    public UserBulkSearchResponse bulkSearch(@RequestBody UserBulkSearchRequest request) {
        return userIdentifierResolverService.resolveBulk(request);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/search")
    public UserSearchResponse searchUsers(@Valid @RequestBody UserSearchRequest request) {
        return userSearchService.findUserByEmailOrPinfl(request);
    }


    @PostMapping("/basic-info")
    @ResponseStatus(HttpStatus.OK)
    public List<UserLanguageInfoDto> getUserBasicInfo(@RequestBody List<UUID> userIds) {
        return userInfoService.getUserLanguages(userIds);
    }
}
