package uz.consortgroup.userservice.service.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;
import uz.consortgroup.userservice.client.CourseFeignClient;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.exception.CourseCreationRollbackException;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
import uz.consortgroup.userservice.service.event.course_group.CourseGroupEventService;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseCreationSaga {
    private final CourseFeignClient courseFeignClient;
    private final MentorActionLogger mentorActionLogger;
    private final CourseGroupEventService courseGroupEventService;


    public CourseResponseDto run(CourseCreateRequestDto dto) {
        String courseTitle = dto.getTranslations() != null && !dto.getTranslations().isEmpty()
                ? dto.getTranslations().getFirst().getTitle()
                : "N/A";

        log.info("Creating course: title='{}', authorId={}", courseTitle, dto.getAuthorId());

        CourseResponseDto course;
        try {
            course = courseFeignClient.createCourse(dto);
            log.info("Course created: id={}, title='{}'", course.getId(), courseTitle);
        } catch (Exception ex) {
            log.error("Failed to create course '{}'", courseTitle, ex);
            throw ex;
        }

        try {
            mentorActionLogger.logMentorResourceAction(course.getId(), course.getAuthorId(), MentorActionType.COURSE_CREATED);
            courseGroupEventService.sendCourseGroupEvent(course);
            log.info("Mentor action logged and course group event sent: courseId={}, authorId={}", course.getId(), course.getAuthorId());
        } catch (Exception ex) {
            log.warn("Failed to log mentor action or send event. Rolling back course creation: courseId={}", course.getId());
            try {
                courseFeignClient.deleteCourse(course.getId());
                log.info("Course rollback successful: courseId={}", course.getId());
            } catch (Exception deleteEx) {
                log.error("Rollback failed: couldn't delete courseId={}", course.getId(), deleteEx);
                throw new CourseCreationRollbackException("Не удалось удалить курс после ошибки логирования", deleteEx);
            }
            throw new MentorActionLoggingException("Не удалось отправить событие, курс отменён", ex);
        }

        return course;
    }
}
