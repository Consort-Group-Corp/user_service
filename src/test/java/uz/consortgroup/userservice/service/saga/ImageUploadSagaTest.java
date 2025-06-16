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
import uz.consortgroup.core.api.v1.dto.course.response.image.ImageUploadResponseDto;
import uz.consortgroup.userservice.client.ImageFeignClient;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.exception.ImageUploadRollbackException;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;
import uz.consortgroup.userservice.service.impl.UserDetailsImpl;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageUploadSagaTest {

    @Mock
    private ImageFeignClient imageFeignClient;

    @Mock
    private MentorActionLogger mentorActionLogger;

    @InjectMocks
    private ImageUploadSaga imageUploadSaga;

    @Test
    void run_ShouldSuccessfullyUploadImageAndLogAction() {
        UUID lessonId = UUID.randomUUID();
        UUID mentorId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        String metadataJson = "{}";
        MultipartFile file = mock(MultipartFile.class);
        
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        
        Authentication authentication = new TestingAuthenticationToken(userDetails, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        ImageUploadResponseDto responseDto = new ImageUploadResponseDto();
        responseDto.setResourceId(resourceId);
        
        when(imageFeignClient.uploadImage(lessonId, metadataJson, file)).thenReturn(responseDto);
        
        ImageUploadResponseDto result = imageUploadSaga.run(lessonId, metadataJson, file);
        
        assertEquals(resourceId, result.getResourceId());
        verify(mentorActionLogger).logMentorResourceAction(resourceId, mentorId, MentorActionType.IMAGE_UPLOADED);
    }

    @Test
    void run_ShouldThrowMentorActionLoggingExceptionWhenLoggingFails() {
        UUID lessonId = UUID.randomUUID();
        UUID mentorId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        String metadataJson = "{}";
        MultipartFile file = mock(MultipartFile.class);
        
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        
        Authentication authentication = new TestingAuthenticationToken(userDetails, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        ImageUploadResponseDto responseDto = new ImageUploadResponseDto();
        responseDto.setResourceId(resourceId);
        
        when(imageFeignClient.uploadImage(lessonId, metadataJson, file)).thenReturn(responseDto);
        doThrow(new RuntimeException("Logging failed")).when(mentorActionLogger)
            .logMentorResourceAction(resourceId, mentorId, MentorActionType.IMAGE_UPLOADED);
        
        assertThrows(MentorActionLoggingException.class, 
            () -> imageUploadSaga.run(lessonId, metadataJson, file));
        
        verify(imageFeignClient).deleteImage(lessonId, resourceId);
    }

    @Test
    void run_ShouldThrowImageUploadRollbackExceptionWhenBothLoggingAndRollbackFail() {
        UUID lessonId = UUID.randomUUID();
        UUID mentorId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        String metadataJson = "{}";
        MultipartFile file = mock(MultipartFile.class);
        
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        
        Authentication authentication = new TestingAuthenticationToken(userDetails, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        ImageUploadResponseDto responseDto = new ImageUploadResponseDto();
        responseDto.setResourceId(resourceId);
        
        when(imageFeignClient.uploadImage(lessonId, metadataJson, file)).thenReturn(responseDto);
        doThrow(new RuntimeException("Logging failed")).when(mentorActionLogger)
            .logMentorResourceAction(resourceId, mentorId, MentorActionType.IMAGE_UPLOADED);
        doThrow(new RuntimeException("Rollback failed")).when(imageFeignClient)
            .deleteImage(lessonId, resourceId);
        
        assertThrows(ImageUploadRollbackException.class,
            () -> imageUploadSaga.run(lessonId, metadataJson, file));
    }

    @Test
    void run_ShouldThrowExceptionWhenUploadFails() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        MultipartFile file = mock(MultipartFile.class);
        
        when(imageFeignClient.uploadImage(lessonId, metadataJson, file))
            .thenThrow(new RuntimeException("Upload failed"));
        
        assertThrows(RuntimeException.class,
            () -> imageUploadSaga.run(lessonId, metadataJson, file));
    }
}