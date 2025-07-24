package uz.consortgroup.userservice.service.proxy.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.BulkPdfFilesUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.PdfFileUploadResponseDto;
import uz.consortgroup.userservice.service.saga.BulkPdfUploadSaga;
import uz.consortgroup.userservice.service.saga.PdfUploadSaga;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfProxyServiceImpl implements PdfProxyService {
    private final PdfUploadSaga pdfUploadSaga;
    private final BulkPdfUploadSaga bulkPdfUploadSaga;

    @Override
    @Transactional
    public PdfFileUploadResponseDto uploadPdfFile(UUID lessonId, String metadataJson, MultipartFile file) {
        log.info("Uploading single PDF: lessonId={}, filename={}, metadata={}",
                lessonId, file.getOriginalFilename(), metadataJson);
        try {
            PdfFileUploadResponseDto response = pdfUploadSaga.run(lessonId, metadataJson, file);
            log.debug("Uploaded PDF: resourceId={}, fileUrl={}, orderPosition={}, translationsCount={}",
                    response.getResourceId(), response.getFileUrl(),
                    response.getOrderPosition(), response.getTranslations() != null ? response.getTranslations().size() : 0);
            return response;
        } catch (Exception e) {
            log.error("Failed to upload PDF for lessonId={}, filename={}", lessonId, file.getOriginalFilename(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public BulkPdfFilesUploadResponseDto uploadPdfFiles(UUID lessonId, String metadataJson, List<MultipartFile> files) {
        log.info("Uploading {} PDFs: lessonId={}, metadata={}",
                files.size(), lessonId, metadataJson);
        try {
            BulkPdfFilesUploadResponseDto response = bulkPdfUploadSaga.run(lessonId, metadataJson, files);
            log.debug("Uploaded {} PDFs successfully", response.getPdfFiles().size());
            return response;
        } catch (Exception e) {
            log.error("Bulk upload of PDFs failed for lessonId={}, totalFiles={}", lessonId, files.size(), e);
            throw e;
        }
    }
}
