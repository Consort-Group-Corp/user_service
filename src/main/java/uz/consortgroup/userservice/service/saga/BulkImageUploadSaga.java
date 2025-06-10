package uz.consortgroup.userservice.service.saga;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.KafkaException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.image.BulkImageUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.image.ImageUploadResponseDto;
import uz.consortgroup.userservice.client.ImageFeignClient;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.exception.ImageUploadRollbackException;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;
import uz.consortgroup.userservice.service.impl.UserDetailsImpl;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BulkImageUploadSaga {
    private final ImageFeignClient imageFeignClient;
    private final MentorActionLogger mentorActionLogger;

    public BulkImageUploadResponseDto run(UUID lessonId, String metadataJson, List<MultipartFile> files) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID mentorId = userDetails.getId();

        BulkImageUploadResponseDto response = imageFeignClient.uploadImages(lessonId, metadataJson, files);

        try {
            for (ImageUploadResponseDto image : response.getImages()) {
                mentorActionLogger.logMentorResourceAction(image.getResourceId(), mentorId, MentorActionType.IMAGE_UPLOADED);
            }
        } catch (Exception ex) {
            try {
                for (ImageUploadResponseDto image : response.getImages()) {
                    imageFeignClient.deleteImage(lessonId, image.getResourceId());
                }
            } catch (Exception deleteEx) {
                throw new ImageUploadRollbackException("Ошибка при откате загруженных изображений", deleteEx);
            }
            throw new MentorActionLoggingException("Ошибка логирования. Все загруженные изображения удалены.", ex);
        }

        return response;
    }
}
