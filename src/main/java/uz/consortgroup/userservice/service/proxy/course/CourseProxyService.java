package uz.consortgroup.userservice.service.proxy.course;

import uz.consortgroup.core.api.v1.dto.course.request.course.CourseCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;

public interface CourseProxyService {
    CourseResponseDto createCourse(CourseCreateRequestDto dto);
}
