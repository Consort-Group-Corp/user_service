package uz.consortgroup.userservice.service.proxy.pdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.BulkPdfFilesUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.PdfFileUploadResponseDto;
import uz.consortgroup.userservice.service.saga.BulkPdfUploadSaga;
import uz.consortgroup.userservice.service.saga.PdfUploadSaga;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfProxyServiceImplTest {

    @Mock
    private PdfUploadSaga pdfUploadSaga;

    @Mock
    private BulkPdfUploadSaga bulkPdfUploadSaga;

    @InjectMocks
    private PdfProxyServiceImpl pdfProxyService;

    @Test
    void uploadPdfFile_WithValidData_CallsSagaAndReturnsResponse() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{\"title\":\"Sample PDF\"}";
        MultipartFile file = new MockMultipartFile("file.pdf", "file.pdf", "application/pdf", "content".getBytes());

        PdfFileUploadResponseDto expectedResponse = new PdfFileUploadResponseDto();
        when(pdfUploadSaga.run(any(), any(), any())).thenReturn(expectedResponse);

        PdfFileUploadResponseDto actualResponse = pdfProxyService.uploadPdfFile(lessonId, metadataJson, file);

        assertSame(expectedResponse, actualResponse);
        verify(pdfUploadSaga).run(lessonId, metadataJson, file);
    }

    @Test
    void uploadPdfFile_VerifyTransactionalAnnotation() throws NoSuchMethodException {
        var method = PdfProxyServiceImpl.class.getMethod("uploadPdfFile", UUID.class, String.class, MultipartFile.class);
        assertNotNull(method.getAnnotation(Transactional.class));
    }

    @Test
    void uploadPdfFiles_VerifyTransactionalAnnotation() throws NoSuchMethodException {
        var method = PdfProxyServiceImpl.class.getMethod("uploadPdfFiles", UUID.class, String.class, List.class);
        assertNotNull(method.getAnnotation(Transactional.class));
    }
}