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
import uz.consortgroup.core.api.v1.dto.course.response.image.BulkImageUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.image.ImageUploadResponseDto;
import uz.consortgroup.userservice.client.ImageFeignClient;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.exception.ImageUploadRollbackException;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
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
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkImageUploadSagaTest {

    @Mock
    private ImageFeignClient imageFeignClient;

    @Mock
    private MentorActionLogger mentorActionLogger;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BulkImageUploadSaga bulkImageUploadSaga;

    @Test
    void run_ShouldSuccessfullyUploadAndLogActions() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        List<MultipartFile> files = List.of(
            new MockMultipartFile("image1.jpg", "image1.jpg", "image/jpeg", new byte[10]),
            new MockMultipartFile("image2.jpg", "image2.jpg", "image/jpeg", new byte[10])
        );

        UUID mentorId = UUID.randomUUID();
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        BulkImageUploadResponseDto mockResponse = new BulkImageUploadResponseDto();
        mockResponse.setImages(List.of(
            new ImageUploadResponseDto(),
            new ImageUploadResponseDto()
        ));
        when(imageFeignClient.uploadImages(any(), any(), any())).thenReturn(mockResponse);

        BulkImageUploadResponseDto response = bulkImageUploadSaga.run(lessonId, metadataJson, files);

        assertNotNull(response);
        assertEquals(2, response.getImages().size());
        verify(imageFeignClient).uploadImages(lessonId, metadataJson, files);
        verify(mentorActionLogger, times(2)).logMentorResourceAction(any(), eq(mentorId), eq(MentorActionType.IMAGE_UPLOADED));
    }

    @Test
    void run_ShouldRollbackWhenLoggingFails() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        List<MultipartFile> files = List.of(
                new MockMultipartFile("image1.jpg", "image1.jpg", "image/jpeg", new byte[10])
        );

        UUID mentorId = UUID.randomUUID();
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UUID imageId = UUID.randomUUID();
        BulkImageUploadResponseDto mockResponse = new BulkImageUploadResponseDto();
        ImageUploadResponseDto imageResponse = new ImageUploadResponseDto();
        imageResponse.setResourceId(imageId); // Устанавливаем resourceId
        mockResponse.setImages(List.of(imageResponse));

        when(imageFeignClient.uploadImages(any(), any(), any())).thenReturn(mockResponse);
        doThrow(new RuntimeException("Logging failed")).when(mentorActionLogger)
                .logMentorResourceAction(any(), any(), any());
        doNothing().when(imageFeignClient).deleteImage(any(), any());

        assertThrows(MentorActionLoggingException.class, () ->
                bulkImageUploadSaga.run(lessonId, metadataJson, files));

        verify(imageFeignClient).deleteImage(lessonId, imageId);
    }

    @Test
    void run_ShouldThrowRollbackExceptionWhenDeleteFails() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        List<MultipartFile> files = List.of(
                new MockMultipartFile("image1.jpg", "image1.jpg", "image/jpeg", new byte[10])
        );

        UUID mentorId = UUID.randomUUID();
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        BulkImageUploadResponseDto mockResponse = new BulkImageUploadResponseDto();
        mockResponse.setImages(List.of(new ImageUploadResponseDto()));
        when(imageFeignClient.uploadImages(any(), any(), any())).thenReturn(mockResponse);

        doThrow(new RuntimeException("Logging failed")).when(mentorActionLogger)
                .logMentorResourceAction(any(), any(), any());

        doThrow(new RuntimeException("Delete failed")).when(imageFeignClient)
                .deleteImage(any(), any());

        Exception exception = assertThrows(ImageUploadRollbackException.class, () ->
                bulkImageUploadSaga.run(lessonId, metadataJson, files));

        assertEquals("Ошибка при откате загруженных изображений", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Delete failed", exception.getCause().getMessage());
    }

    @Test
    void run_ShouldThrowExceptionWhenUploadFails() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        List<MultipartFile> files = List.of(
            new MockMultipartFile("image1.jpg", "image1.jpg", "image/jpeg", new byte[10])
        );

        UUID mentorId = UUID.randomUUID();
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(imageFeignClient.uploadImages(any(), any(), any())).thenThrow(new RuntimeException("Upload failed"));

        assertThrows(RuntimeException.class, () -> 
            bulkImageUploadSaga.run(lessonId, metadataJson, files));

        verify(mentorActionLogger, never()).logMentorResourceAction(any(), any(), any());
    }
}