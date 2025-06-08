package uz.consortgroup.userservice.service.saga;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.KafkaException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.BulkPdfFilesUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.PdfFileUploadResponseDto;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.client.CourseFeignClient;
import uz.consortgroup.userservice.client.PdfFeignClient;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
import uz.consortgroup.userservice.exception.PdfUploadRollbackException;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;
import uz.consortgroup.userservice.service.impl.UserDetailsImpl;
import uz.consortgroup.userservice.service.operation.UserOperationsService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BulkPdfUploadSaga {
    private final PdfFeignClient pdfFeignClient;
    private final MentorActionLogger mentorActionLogger;

    @AllAspect
    public BulkPdfFilesUploadResponseDto run(UUID lessonId, String metadataJson, List<MultipartFile> files) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID mentorId = userDetails.getId();

        BulkPdfFilesUploadResponseDto response = pdfFeignClient.uploadPdfFiles(lessonId, metadataJson, files);

        try {
            for (PdfFileUploadResponseDto pdf : response.getPdfFiles()) {
                mentorActionLogger.logMentorResourceAction(pdf.getResourceId(), mentorId, MentorActionType.PDF_UPLOADED);
            }
        } catch (Exception ex) {
            try {
                for (PdfFileUploadResponseDto pdf : response.getPdfFiles()) {
                    pdfFeignClient.deletePdf(lessonId, pdf.getResourceId());
                }
            } catch (Exception deleteEx) {
                throw new PdfUploadRollbackException("Ошибка при откате загруженных PDF", deleteEx);
            }
            throw new MentorActionLoggingException("Ошибка логирования действий. Все загруженные PDF удалены.", ex);
        }

        return response;
    }

}
