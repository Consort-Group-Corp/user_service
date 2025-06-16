package uz.consortgroup.userservice.service.saga;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.video.BulkVideoUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.video.VideoUploadResponseDto;
import uz.consortgroup.userservice.client.VideoFeignClient;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
import uz.consortgroup.userservice.exception.VideoUploadRollbackException;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;
import uz.consortgroup.userservice.service.impl.UserDetailsImpl;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkVideoUploadSagaTest {

    @Mock
    private VideoFeignClient videoFeignClient;

    @Mock
    private MentorActionLogger mentorActionLogger;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BulkVideoUploadSaga bulkVideoUploadSaga;

    @Test
    void run_ShouldSuccessfullyUploadAndLogActions() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        List<MultipartFile> files = List.of(
            new MockMultipartFile("video1.mp4", "video1.mp4", "video/mp4", new byte[10])
        );

        UUID mentorId = UUID.randomUUID();
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UUID videoId = UUID.randomUUID();
        BulkVideoUploadResponseDto mockResponse = new BulkVideoUploadResponseDto();
        VideoUploadResponseDto videoResponse = new VideoUploadResponseDto();
        videoResponse.setResourceId(videoId);
        mockResponse.setVideos(List.of(videoResponse));
        when(videoFeignClient.uploadVideos(any(), any(), any())).thenReturn(mockResponse);

        BulkVideoUploadResponseDto response = bulkVideoUploadSaga.run(lessonId, metadataJson, files);

        assertNotNull(response);
        verify(videoFeignClient).uploadVideos(lessonId, metadataJson, files);
        verify(mentorActionLogger).logMentorResourceAction(videoId, mentorId, MentorActionType.VIDEO_UPLOADED);
    }

    @Test
    void run_ShouldThrowMentorActionLoggingExceptionWhenLoggingFails() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        List<MultipartFile> files = List.of(
            new MockMultipartFile("video1.mp4", "video1.mp4", "video/mp4", new byte[10])
        );

        UUID mentorId = UUID.randomUUID();
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UUID videoId = UUID.randomUUID();
        BulkVideoUploadResponseDto mockResponse = new BulkVideoUploadResponseDto();
        VideoUploadResponseDto videoResponse = new VideoUploadResponseDto();
        videoResponse.setResourceId(videoId);
        mockResponse.setVideos(List.of(videoResponse));
        when(videoFeignClient.uploadVideos(any(), any(), any())).thenReturn(mockResponse);
        doThrow(new RuntimeException("Logging failed")).when(mentorActionLogger)
            .logMentorResourceAction(any(), any(), any());
        doNothing().when(videoFeignClient).deleteVideo(any(), any());

        MentorActionLoggingException exception = assertThrows(MentorActionLoggingException.class,
            () -> bulkVideoUploadSaga.run(lessonId, metadataJson, files));

        assertEquals("Ошибка логирования событий. Все загруженные видео были удалены.", exception.getMessage());
        verify(videoFeignClient).deleteVideo(lessonId, videoId);
    }

    @Test
    void run_ShouldThrowVideoUploadRollbackExceptionWhenDeleteFails() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        List<MultipartFile> files = List.of(
            new MockMultipartFile("video1.mp4", "video1.mp4", "video/mp4", new byte[10])
        );

        UUID mentorId = UUID.randomUUID();
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UUID videoId = UUID.randomUUID();
        BulkVideoUploadResponseDto mockResponse = new BulkVideoUploadResponseDto();
        VideoUploadResponseDto videoResponse = new VideoUploadResponseDto();
        videoResponse.setResourceId(videoId);
        mockResponse.setVideos(List.of(videoResponse));
        when(videoFeignClient.uploadVideos(any(), any(), any())).thenReturn(mockResponse);
        doThrow(new RuntimeException("Logging failed")).when(mentorActionLogger)
            .logMentorResourceAction(any(), any(), any());
        doThrow(new RuntimeException("Delete failed")).when(videoFeignClient).deleteVideo(any(), any());

        VideoUploadRollbackException exception = assertThrows(VideoUploadRollbackException.class,
            () -> bulkVideoUploadSaga.run(lessonId, metadataJson, files));

        assertEquals("Ошибка при откате загруженных видео", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Delete failed", exception.getCause().getMessage());
    }

    @Test
    void run_ShouldThrowExceptionWhenUploadFails() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        List<MultipartFile> files = List.of(
            new MockMultipartFile("video1.mp4", "video1.mp4", "video/mp4", new byte[10])
        );

        UUID mentorId = UUID.randomUUID();
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(videoFeignClient.uploadVideos(any(), any(), any())).thenThrow(new RuntimeException("Upload failed"));

        assertThrows(RuntimeException.class,
            () -> bulkVideoUploadSaga.run(lessonId, metadataJson, files));

        verify(mentorActionLogger, never()).logMentorResourceAction(any(), any(), any());
        verify(videoFeignClient, never()).deleteVideo(any(), any());
    }
}