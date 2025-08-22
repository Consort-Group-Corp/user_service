package uz.consortgroup.userservice.service.saga;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;
import uz.consortgroup.userservice.client.CourseFeignClient;
import uz.consortgroup.userservice.exception.CourseCreationRollbackException;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
import uz.consortgroup.userservice.service.event.course_group.CourseGroupEventService;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseCreationSagaTest {

    @Mock
    private CourseFeignClient courseFeignClient;

    @Mock
    private MentorActionLogger mentorActionLogger;

    @Mock
    private CourseGroupEventService courseGroupEventService;

    @InjectMocks
    private CourseCreationSaga courseCreationSaga;

    @Test
    void run_ShouldThrowMentorActionLoggingExceptionWhenLoggingFails() {
        CourseCreateRequestDto requestDto = new CourseCreateRequestDto();
        UUID courseId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        
        CourseResponseDto mockResponse = new CourseResponseDto();
        mockResponse.setId(courseId);
        mockResponse.setAuthorId(authorId);
        
        when(courseFeignClient.createCourse(any())).thenReturn(mockResponse);
        doThrow(new RuntimeException("Logging failed")).when(mentorActionLogger)
            .logMentorResourceAction(any(), any(), any());
        doNothing().when(courseFeignClient).deleteCourse(any());

        MentorActionLoggingException exception = assertThrows(MentorActionLoggingException.class,
            () -> courseCreationSaga.run(requestDto));

        assertEquals("Не удалось отправить событие, курс отменён", exception.getMessage());
        verify(courseFeignClient).deleteCourse(courseId);
    }

    @Test
    void run_ShouldThrowCourseCreationRollbackExceptionWhenDeleteFails() {
        CourseCreateRequestDto requestDto = new CourseCreateRequestDto();
        UUID courseId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        
        CourseResponseDto mockResponse = new CourseResponseDto();
        mockResponse.setId(courseId);
        mockResponse.setAuthorId(authorId);
        
        when(courseFeignClient.createCourse(any())).thenReturn(mockResponse);
        doThrow(new RuntimeException("Logging failed")).when(mentorActionLogger)
            .logMentorResourceAction(any(), any(), any());
        doThrow(new RuntimeException("Delete failed")).when(courseFeignClient).deleteCourse(any());

        CourseCreationRollbackException exception = assertThrows(CourseCreationRollbackException.class,
            () -> courseCreationSaga.run(requestDto));

        assertEquals("Не удалось удалить курс после ошибки логирования", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Delete failed", exception.getCause().getMessage());
    }

    @Test
    void run_ShouldThrowExceptionWhenCourseCreationFails() {
        CourseCreateRequestDto requestDto = new CourseCreateRequestDto();
        
        when(courseFeignClient.createCourse(any())).thenThrow(new RuntimeException("Creation failed"));

        assertThrows(RuntimeException.class,
            () -> courseCreationSaga.run(requestDto));

        verify(mentorActionLogger, never()).logMentorResourceAction(any(), any(), any());
        verify(courseFeignClient, never()).deleteCourse(any());
    }
}