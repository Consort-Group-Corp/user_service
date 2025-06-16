package uz.consortgroup.userservice.service.proxy.course;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;
import uz.consortgroup.userservice.service.saga.CourseCreationSaga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseProxyServiceImplTest {

    @Mock
    private CourseCreationSaga courseCreationSaga;

    @InjectMocks
    private CourseProxyServiceImpl courseProxyService;

    @Test
    void createCourse_Success() {
        CourseCreateRequestDto requestDto = new CourseCreateRequestDto();
        CourseResponseDto expectedResponse = new CourseResponseDto();
        
        when(courseCreationSaga.run(requestDto)).thenReturn(expectedResponse);
        
        CourseResponseDto actualResponse = courseProxyService.createCourse(requestDto);
        
        assertEquals(expectedResponse, actualResponse);
        verify(courseCreationSaga).run(requestDto);
    }

    @Test
    void createCourse_NullRequest() {
        CourseResponseDto expectedResponse = new CourseResponseDto();
        
        when(courseCreationSaga.run(null)).thenReturn(expectedResponse);
        
        CourseResponseDto actualResponse = courseProxyService.createCourse(null);
        
        assertEquals(expectedResponse, actualResponse);
        verify(courseCreationSaga).run(null);
    }

    @Test
    void createCourse_SagaThrowsException() {
        CourseCreateRequestDto requestDto = new CourseCreateRequestDto();
        
        when(courseCreationSaga.run(requestDto))
            .thenThrow(new RuntimeException("Saga error"));
        
        assertThrows(RuntimeException.class, () -> 
            courseProxyService.createCourse(requestDto));
    }
}