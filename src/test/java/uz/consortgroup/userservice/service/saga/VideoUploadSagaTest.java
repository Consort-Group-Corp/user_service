package uz.consortgroup.userservice.service.saga;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.video.VideoUploadResponseDto;
import uz.consortgroup.userservice.client.VideoFeignClient;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
import uz.consortgroup.userservice.exception.VideoUploadRollbackException;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;
import uz.consortgroup.userservice.service.impl.UserDetailsImpl;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoUploadSagaTest {

    @Mock
    private VideoFeignClient videoFeignClient;

    @Mock
    private MentorActionLogger mentorActionLogger;

    @InjectMocks
    private VideoUploadSaga videoUploadSaga;

    @Test
    void run_ShouldSuccessfullyUploadVideoAndLogAction() {
        UUID lessonId = UUID.randomUUID();
        UUID mentorId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        String metadataJson = "{}";
        MultipartFile file = mock(MultipartFile.class);
        
        setupSecurityContext(mentorId);
        
        VideoUploadResponseDto responseDto = new VideoUploadResponseDto();
        responseDto.setResourceId(resourceId);
        
        when(videoFeignClient.uploadVideo(lessonId, metadataJson, file)).thenReturn(responseDto);
        
        VideoUploadResponseDto result = videoUploadSaga.run(lessonId, metadataJson, file);
        
        assertEquals(resourceId, result.getResourceId());
        verify(mentorActionLogger).logMentorResourceAction(resourceId, mentorId, MentorActionType.VIDEO_UPLOADED);
    }

    @Test
    void run_ShouldThrowMentorActionLoggingExceptionWhenLoggingFails() {
        UUID lessonId = UUID.randomUUID();
        UUID mentorId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        String metadataJson = "{}";
        MultipartFile file = mock(MultipartFile.class);
        
        setupSecurityContext(mentorId);
        
        VideoUploadResponseDto responseDto = new VideoUploadResponseDto();
        responseDto.setResourceId(resourceId);
        
        when(videoFeignClient.uploadVideo(lessonId, metadataJson, file)).thenReturn(responseDto);
        doThrow(new RuntimeException("Logging failed")).when(mentorActionLogger)
            .logMentorResourceAction(resourceId, mentorId, MentorActionType.VIDEO_UPLOADED);
        
        assertThrows(MentorActionLoggingException.class, 
            () -> videoUploadSaga.run(lessonId, metadataJson, file));
        
        verify(videoFeignClient).deleteVideo(lessonId, resourceId);
    }

    @Test
    void run_ShouldThrowVideoUploadRollbackExceptionWhenBothLoggingAndRollbackFail() {
        UUID lessonId = UUID.randomUUID();
        UUID mentorId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        String metadataJson = "{}";
        MultipartFile file = mock(MultipartFile.class);
        
        setupSecurityContext(mentorId);
        
        VideoUploadResponseDto responseDto = new VideoUploadResponseDto();
        responseDto.setResourceId(resourceId);
        
        when(videoFeignClient.uploadVideo(lessonId, metadataJson, file)).thenReturn(responseDto);
        doThrow(new RuntimeException("Logging failed")).when(mentorActionLogger)
            .logMentorResourceAction(resourceId, mentorId, MentorActionType.VIDEO_UPLOADED);
        doThrow(new RuntimeException("Rollback failed")).when(videoFeignClient)
            .deleteVideo(lessonId, resourceId);
        
        VideoUploadRollbackException exception = assertThrows(VideoUploadRollbackException.class,
            () -> videoUploadSaga.run(lessonId, metadataJson, file));
        
        assertEquals("Не удалось удалить видео после ошибки логирования", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void run_ShouldThrowExceptionWhenUploadFails() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        MultipartFile file = mock(MultipartFile.class);
        
        when(videoFeignClient.uploadVideo(lessonId, metadataJson, file))
            .thenThrow(new RuntimeException("Upload failed"));
        
        assertThrows(RuntimeException.class,
            () -> videoUploadSaga.run(lessonId, metadataJson, file));
    }

    private void setupSecurityContext(UUID mentorId) {
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        
        Authentication authentication = new TestingAuthenticationToken(userDetails, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}