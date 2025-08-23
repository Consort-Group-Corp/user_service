package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.user.response.EligibilityResponse;
import uz.consortgroup.userservice.validator.CourseAccessValidator;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/purchases")
@RequiredArgsConstructor
@Hidden
public class InternalPurchaseController {
    private final CourseAccessValidator courseAccessValidator;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{userId}/courses/{courseId}/eligibility")
    public EligibilityResponse checkEligibility(@PathVariable UUID userId, @PathVariable UUID courseId) {
        return courseAccessValidator.validateUserCanPurchaseCourse(userId, courseId);
    }
}
