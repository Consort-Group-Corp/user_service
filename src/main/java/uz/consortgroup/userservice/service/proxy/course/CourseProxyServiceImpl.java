package uz.consortgroup.userservice.service.proxy.course;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.course.enumeration.Language;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.course.response.course.CoursePreviewResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseTranslationRequestDto;
import uz.consortgroup.userservice.client.CourseFeignClient;
import uz.consortgroup.userservice.service.saga.CourseCreationSaga;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseProxyServiceImpl implements CourseProxyService {
    private final CourseCreationSaga courseCreationSaga;
    private final CourseFeignClient courseFeignClient;

    @Override
    public CourseResponseDto createCourse(CourseCreateRequestDto dto) {
        String title = extractTitle(dto);
        log.info("Starting course creation saga for course title: '{}', authorId: {}", title, dto.getAuthorId());
        try {
            CourseResponseDto response = courseCreationSaga.run(dto);
            log.debug("Course created successfully. ID: {}, title: '{}'", response.getId(), title);
            return response;
        } catch (Exception e) {
            log.error("Failed to create course. Title: '{}', authorId: {}", title, dto.getAuthorId(), e);
            throw e;
        }
    }

    @Override
    public CoursePreviewResponseDto getCoursePreview(UUID courseId, Language language) {
        log.info("Fetching course preview: courseId={}, lang={}", courseId, language);
        return courseFeignClient.getCoursePreview(courseId, language);
    }

    @Override
    public void deleteCourse(UUID courseId) {
        log.info("Deleting course: {}", courseId);
        courseFeignClient.deleteCourse(courseId);
    }

    private String extractTitle(CourseCreateRequestDto dto) {
        if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
            CourseTranslationRequestDto translation = dto.getTranslations().getFirst();
            return translation.getTitle() != null ? translation.getTitle() : "[no title]";
        }
        return "[no translations]";
    }
}
