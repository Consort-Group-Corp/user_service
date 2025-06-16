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
import uz.consortgroup.core.api.v1.dto.course.response.pdf.BulkPdfFilesUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.PdfFileUploadResponseDto;
import uz.consortgroup.userservice.client.PdfFeignClient;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
import uz.consortgroup.userservice.exception.PdfUploadRollbackException;
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
class BulkPdfUploadSagaTest {

    @Mock
    private PdfFeignClient pdfFeignClient;

    @Mock
    private MentorActionLogger mentorActionLogger;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BulkPdfUploadSaga bulkPdfUploadSaga;

    @Test
    void run_ShouldSuccessfullyUploadAndLogActions() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        List<MultipartFile> files = List.of(
            new MockMultipartFile("file1.pdf", "file1.pdf", "application/pdf", new byte[10])
        );

        UUID mentorId = UUID.randomUUID();
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UUID pdfId = UUID.randomUUID();
        BulkPdfFilesUploadResponseDto mockResponse = new BulkPdfFilesUploadResponseDto();
        PdfFileUploadResponseDto pdfResponse = new PdfFileUploadResponseDto();
        pdfResponse.setResourceId(pdfId);
        mockResponse.setPdfFiles(List.of(pdfResponse));
        when(pdfFeignClient.uploadPdfFiles(any(), any(), any())).thenReturn(mockResponse);

        BulkPdfFilesUploadResponseDto response = bulkPdfUploadSaga.run(lessonId, metadataJson, files);

        assertNotNull(response);
        verify(pdfFeignClient).uploadPdfFiles(lessonId, metadataJson, files);
        verify(mentorActionLogger).logMentorResourceAction(pdfId, mentorId, MentorActionType.PDF_UPLOADED);
    }

    @Test
    void run_ShouldThrowMentorActionLoggingExceptionWhenLoggingFails() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        List<MultipartFile> files = List.of(
            new MockMultipartFile("file1.pdf", "file1.pdf", "application/pdf", new byte[10])
        );

        UUID mentorId = UUID.randomUUID();
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UUID pdfId = UUID.randomUUID();
        BulkPdfFilesUploadResponseDto mockResponse = new BulkPdfFilesUploadResponseDto();
        PdfFileUploadResponseDto pdfResponse = new PdfFileUploadResponseDto();
        pdfResponse.setResourceId(pdfId);
        mockResponse.setPdfFiles(List.of(pdfResponse));
        when(pdfFeignClient.uploadPdfFiles(any(), any(), any())).thenReturn(mockResponse);
        doThrow(new RuntimeException("Logging failed")).when(mentorActionLogger)
            .logMentorResourceAction(any(), any(), any());
        doNothing().when(pdfFeignClient).deletePdf(any(), any());

        MentorActionLoggingException exception = assertThrows(MentorActionLoggingException.class,
            () -> bulkPdfUploadSaga.run(lessonId, metadataJson, files));

        assertEquals("Ошибка логирования действий. Все загруженные PDF удалены.", exception.getMessage());
        verify(pdfFeignClient).deletePdf(lessonId, pdfId);
    }

    @Test
    void run_ShouldThrowPdfUploadRollbackExceptionWhenDeleteFails() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        List<MultipartFile> files = List.of(
            new MockMultipartFile("file1.pdf", "file1.pdf", "application/pdf", new byte[10])
        );

        UUID mentorId = UUID.randomUUID();
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UUID pdfId = UUID.randomUUID();
        BulkPdfFilesUploadResponseDto mockResponse = new BulkPdfFilesUploadResponseDto();
        PdfFileUploadResponseDto pdfResponse = new PdfFileUploadResponseDto();
        pdfResponse.setResourceId(pdfId);
        mockResponse.setPdfFiles(List.of(pdfResponse));
        when(pdfFeignClient.uploadPdfFiles(any(), any(), any())).thenReturn(mockResponse);
        doThrow(new RuntimeException("Logging failed")).when(mentorActionLogger)
            .logMentorResourceAction(any(), any(), any());
        doThrow(new RuntimeException("Delete failed")).when(pdfFeignClient).deletePdf(any(), any());

        PdfUploadRollbackException exception = assertThrows(PdfUploadRollbackException.class,
            () -> bulkPdfUploadSaga.run(lessonId, metadataJson, files));

        assertEquals("Ошибка при откате загруженных PDF", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Delete failed", exception.getCause().getMessage());
    }

    @Test
    void run_ShouldThrowExceptionWhenUploadFails() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{}";
        List<MultipartFile> files = List.of(
            new MockMultipartFile("file1.pdf", "file1.pdf", "application/pdf", new byte[10])
        );

        UUID mentorId = UUID.randomUUID();
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mentorId);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(pdfFeignClient.uploadPdfFiles(any(), any(), any())).thenThrow(new RuntimeException("Upload failed"));

        assertThrows(RuntimeException.class,
            () -> bulkPdfUploadSaga.run(lessonId, metadataJson, files));

        verify(mentorActionLogger, never()).logMentorResourceAction(any(), any(), any());
        verify(pdfFeignClient, never()).deletePdf(any(), any());
    }
}