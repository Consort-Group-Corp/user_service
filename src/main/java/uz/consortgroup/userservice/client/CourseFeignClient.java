package uz.consortgroup.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseCreateRequestDto;
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
    @PostMapping("/api/v1/courses")
    CourseResponseDto createCourse(@RequestBody CourseCreateRequestDto dto);

    @GetMapping("/internal/courses/{id}/purchase-validation")
    CoursePurchaseValidationResponseDto validateCourseForPurchase(@PathVariable("id") UUID courseId);

    @DeleteMapping("/api/v1/courses/{courseId}")
    void deleteCourse(@PathVariable("courseId") UUID courseId);

    @GetMapping("/api/v1/courses/{courseId}")
    CourseResponseDto getCourseById(@PathVariable("courseId") UUID courseId);
}
