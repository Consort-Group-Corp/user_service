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
import uz.consortgroup.core.api.v1.dto.course.response.pdf.PdfFileUploadResponseDto;
import uz.consortgroup.userservice.client.PdfFeignClient;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
import uz.consortgroup.userservice.exception.PdfUploadRollbackException;
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
class PdfUploadSagaTest {

    @Mock
    private PdfFeignClient pdfFeignClient;

    @Mock
    private MentorActionLogger mentorActionLogger;

    @InjectMocks
    private PdfUploadSaga pdfUploadSaga;

    @Test
    void run_ShouldSuccessfullyUploadPdfAndLogAction() {
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
        
        PdfFileUploadResponseDto responseDto = new PdfFileUploadResponseDto();
        responseDto.setResourceId(resourceId);
        
        when(pdfFeignClient.uploadPdfFile(lessonId, metadataJson, file)).thenReturn(responseDto);
        
        PdfFileUploadResponseDto result = pdfUploadSaga.run(lessonId, metadataJson, file);
        
        assertEquals(resourceId, result.getResourceId());
        verify(mentorActionLogger).logMentorResourceAction(resourceId, mentorId, MentorActionType.PDF_UPLOADED);
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
        
        PdfFileUploadResponseDto responseDto = new PdfFileUploadResponseDto();
        responseDto.setResourceId(resourceId);
        
        when(pdfFeignClient.uploadPdfFile(lessonId, metadataJson, file)).thenReturn(responseDto);
        doThrow(new RuntimeException("Logging failed")).when(mentorActionLogger)
            .logMentorResourceAction(resourceId, mentorId, MentorActionType.PDF_UPLOADED);
        
        assertThrows(MentorActionLoggingException.class, 
            () -> pdfUploadSaga.run(lessonId, metadataJson, file));
        
        verify(pdfFeignClient).deletePdf(lessonId, resourceId);
    }

    @Test
    void run_ShouldThrowPdfUploadRollbackExceptionWhenBothLoggingAndRollbackFail() {
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
        
        PdfFileUploadResponseDto responseDto = new PdfFileUploadResponseDto();
        responseDto.setResourceId(resourceId);
        
        when(pdfFeignClient.uploadPdfFile(lessonId, metadataJson, file)).thenReturn(responseDto);
        doThrow(new RuntimeException("Logging failed")).when(mentorActionLogger)
            .logMentorResourceAction(resourceId, mentorId, MentorActionType.PDF_UPLOADED);
        doThrow(new RuntimeException("Rollback failed")).when(pdfFeignClient)
            .deletePdf(lessonId, resourceId);
        
        Exception exception = assertThrows(PdfUploadRollbackException.class,
            () -> pdfUploadSaga.run(lessonId, metadataJson, file));
        
        assertEquals("Не удалось удалить PDF после ошибки логирования", exception.getMessage());
    }

    @Test
    void run_ShouldThrowExceptionWhenUploadFails() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        MultipartFile file = mock(MultipartFile.class);
        
        when(pdfFeignClient.uploadPdfFile(lessonId, metadataJson, file))
            .thenThrow(new RuntimeException("Upload failed"));
        
        assertThrows(RuntimeException.class,
            () -> pdfUploadSaga.run(lessonId, metadataJson, file));
    }
}