package uz.consortgroup.userservice.service.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.PdfFileUploadResponseDto;
import uz.consortgroup.userservice.client.PdfFeignClient;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
import uz.consortgroup.userservice.exception.PdfUploadRollbackException;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;
import uz.consortgroup.userservice.service.impl.UserDetailsImpl;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfUploadSaga {
    private final PdfFeignClient pdfFeignClient;
    private final MentorActionLogger mentorActionLogger;

    public PdfFileUploadResponseDto run(UUID lessonId, String metadataJson, MultipartFile file) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID mentorId = userDetails.getId();

        log.info("Starting PDF upload saga: lessonId={}, mentorId={}, filename={}", lessonId, mentorId, file.getOriginalFilename());

        PdfFileUploadResponseDto response = pdfFeignClient.uploadPdfFile(lessonId, metadataJson, file);
        UUID resourceId = response.getResourceId();

        log.debug("PDF uploaded successfully: resourceId={}, lessonId={}", resourceId, lessonId);

        try {
            mentorActionLogger.logMentorResourceAction(resourceId, mentorId, MentorActionType.PDF_UPLOADED);
            log.info("Mentor action logged: resourceId={}, mentorId={}, action={}", resourceId, mentorId, MentorActionType.PDF_UPLOADED);
        } catch (Exception ex) {
            log.error("Logging mentor action failed, attempting rollback: resourceId={}, lessonId={}", resourceId, lessonId, ex);
            try {
                pdfFeignClient.deletePdf(lessonId, resourceId);
                log.info("Rollback successful: deleted PDF resourceId={}, lessonId={}", resourceId, lessonId);
            } catch (Exception deleteEx) {
                log.error("Rollback failed: unable to delete uploaded PDF. resourceId={}, lessonId={}", resourceId, lessonId, deleteEx);
                throw new PdfUploadRollbackException("Не удалось удалить PDF после ошибки логирования", deleteEx);
            }
            throw new MentorActionLoggingException("Ошибка логирования события загрузки PDF. Операция отменена.", ex);
        }

        return response;
    }
}
