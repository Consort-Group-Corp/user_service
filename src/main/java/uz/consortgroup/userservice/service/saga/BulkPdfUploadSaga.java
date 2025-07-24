package uz.consortgroup.userservice.service.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkPdfUploadSaga {
    private final PdfFeignClient pdfFeignClient;
    private final MentorActionLogger mentorActionLogger;

    public BulkPdfFilesUploadResponseDto run(UUID lessonId, String metadataJson, List<MultipartFile> files) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID mentorId = userDetails.getId();

        log.info("Starting bulk PDF upload for lessonId={}, mentorId={}, filesCount={}", lessonId, mentorId, files.size());

        BulkPdfFilesUploadResponseDto response;
        try {
            response = pdfFeignClient.uploadPdfFiles(lessonId, metadataJson, files);
            log.info("Successfully uploaded {} PDF files for lessonId={}", response.getPdfFiles().size(), lessonId);
        } catch (Exception ex) {
            log.error("Failed to upload PDF files for lessonId={}", lessonId, ex);
            throw ex;
        }

        try {
            for (PdfFileUploadResponseDto pdf : response.getPdfFiles()) {
                log.debug("Logging PDF_UPLOADED action for resourceId={}, mentorId={}", pdf.getResourceId(), mentorId);
                mentorActionLogger.logMentorResourceAction(pdf.getResourceId(), mentorId, MentorActionType.PDF_UPLOADED);
            }
        } catch (Exception ex) {
            log.warn("MentorAction logging failed. Rolling back uploaded PDFs for lessonId={}", lessonId);
            try {
                for (PdfFileUploadResponseDto pdf : response.getPdfFiles()) {
                    log.debug("Deleting PDF resourceId={} for lessonId={}", pdf.getResourceId(), lessonId);
                    pdfFeignClient.deletePdf(lessonId, pdf.getResourceId());
                }
                log.info("Rollback successful: All uploaded PDFs have been deleted for lessonId={}", lessonId);
            } catch (Exception deleteEx) {
                log.error("Rollback failed while deleting PDFs for lessonId={}", lessonId, deleteEx);
                throw new PdfUploadRollbackException("Ошибка при откате загруженных PDF", deleteEx);
            }

            log.error("Mentor action logging failed. Uploaded PDFs were rolled back.", ex);
            throw new MentorActionLoggingException("Ошибка логирования действий. Все загруженные PDF удалены.", ex);
        }

        log.info("Bulk PDF upload and mentor action logging completed for lessonId={}", lessonId);
        return response;
    }
}
