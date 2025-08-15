package uz.consortgroup.userservice.service.proxy.course;

import uz.consortgroup.core.api.v1.dto.course.enumeration.Language;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.course.response.course.CoursePreviewResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;

import java.util.UUID;

public interface CourseProxyService {
    CourseResponseDto createCourse(CourseCreateRequestDto dto);
    CoursePreviewResponseDto getCoursePreview(UUID courseId, Language language);
    void deleteCourse(UUID courseId);
}
