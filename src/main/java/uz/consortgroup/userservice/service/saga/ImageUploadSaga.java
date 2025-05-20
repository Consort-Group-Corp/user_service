package uz.consortgroup.userservice.service.saga;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.KafkaException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.image.ImageUploadResponseDto;
import uz.consortgroup.userservice.client.ImageFeignClient;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.exception.ImageUploadRollbackException;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;
import uz.consortgroup.userservice.service.impl.UserDetailsImpl;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageUploadSaga {
    private final ImageFeignClient imageFeignClient;
    private final MentorActionLogger mentorActionLogger;

    public ImageUploadResponseDto run(UUID lessonId, String metadataJson, MultipartFile file) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID mentorId = userDetails.getId();

        ImageUploadResponseDto response = imageFeignClient.uploadImage(lessonId, metadataJson, file);

        try {
            mentorActionLogger.logMentorResourceAction(response.getResourceId(), mentorId, MentorActionType.IMAGE_UPLOADED);
        } catch (KafkaException kafkaEx) {
            try {
                imageFeignClient.deleteImage(lessonId, response.getResourceId());
            } catch (Exception deleteEx) {
                throw new ImageUploadRollbackException("Не удалось удалить изображение после ошибки логирования.", deleteEx);
            }
            throw new MentorActionLoggingException("Ошибка логирования события загрузки изображения. Операция отменена.", kafkaEx);
        }

        return response;
    }
}
