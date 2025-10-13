package uz.consortgroup.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uz.consortgroup.core.api.v1.dto.course.response.course.CoursePurchaseValidationResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;
import uz.consortgroup.userservice.config.client.FeignClientConfig;

import java.util.UUID;

@FeignClient(
        name = "course-service",
        contextId = "courseClient",
        url = "${course.service.url}",
        configuration = FeignClientConfig.class
)
public interface CourseFeignClient {

    @GetMapping("/internal/courses/{id}/purchase-validation")
    CoursePurchaseValidationResponseDto validateCourseForPurchase(@PathVariable("id") UUID courseId);

    @GetMapping("/api/v1/courses/{courseId}")
    CourseResponseDto getCourseById(@PathVariable("courseId") UUID courseId);
}
