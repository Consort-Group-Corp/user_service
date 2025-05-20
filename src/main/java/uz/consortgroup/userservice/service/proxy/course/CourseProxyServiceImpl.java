package uz.consortgroup.userservice.service.proxy.course;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.service.saga.CourseCreationSaga;

@Service
@RequiredArgsConstructor
public class CourseProxyServiceImpl implements CourseProxyService {
    private final CourseCreationSaga courseCreationSaga;

    @Override
    @AllAspect
    public CourseResponseDto createCourse(CourseCreateRequestDto dto) {
        return courseCreationSaga.run(dto);
    }
}
