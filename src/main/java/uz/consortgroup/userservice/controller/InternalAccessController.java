package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.user.response.CourseAccessResponse;
import uz.consortgroup.userservice.service.access.CourseAccessService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/access")
@RequiredArgsConstructor
@Hidden
public class InternalAccessController {

    private final CourseAccessService courseAccessService;

    @GetMapping("/{userId}/courses/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    public CourseAccessResponse check(@PathVariable UUID userId, @PathVariable UUID courseId) {
        return courseAccessService.checkAccess(userId, courseId);
    }
}
