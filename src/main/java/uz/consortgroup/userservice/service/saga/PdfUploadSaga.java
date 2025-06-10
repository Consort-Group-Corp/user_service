package uz.consortgroup.userservice.service.saga;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.KafkaException;
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
public class PdfUploadSaga {
    private final PdfFeignClient pdfFeignClient;
    private final MentorActionLogger mentorActionLogger;

    public PdfFileUploadResponseDto run(UUID lessonId, String metadataJson, MultipartFile file) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID mentorId = userDetails.getId();

        PdfFileUploadResponseDto response = pdfFeignClient.uploadPdfFile(lessonId, metadataJson, file);

        try {
            mentorActionLogger.logMentorResourceAction(response.getResourceId(),mentorId,MentorActionType.PDF_UPLOADED);
        } catch (Exception ex) {
            try {
                pdfFeignClient.deletePdf(lessonId, response.getResourceId());
            } catch (Exception deleteEx) {
                throw new PdfUploadRollbackException("Не удалось удалить PDF после ошибки логирования", deleteEx);
            }
            throw new MentorActionLoggingException("Ошибка логирования события загрузки PDF. Операция отменена.", ex);
        }

        return response;
    }
}
