package uz.consortgroup.userservice.service.saga;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.KafkaException;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.client.CourseFeignClient;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.exception.CourseCreationRollbackException;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;

@Service
@RequiredArgsConstructor
public class CourseCreationSaga {
    private final CourseFeignClient courseFeignClient;
    private final MentorActionLogger mentorActionLogger;

    @AllAspect
    public CourseResponseDto run(CourseCreateRequestDto dto) {
        CourseResponseDto course = courseFeignClient.createCourse(dto);

        try {
            mentorActionLogger.logMentorResourceAction(course.getId(), course.getAuthorId(), MentorActionType.COURSE_CREATED);
        } catch (KafkaException kafkaEx) {
            try {
                courseFeignClient.deleteCourse(course.getId());
            } catch (Exception deleteEx) {
                throw new CourseCreationRollbackException("Не удалось удалить курс после ошибки логирования", deleteEx);
            }
            throw new MentorActionLoggingException("Не удалось отправить событие в Kafka, курс отменён", kafkaEx);
        }
        return course;
    }
}
