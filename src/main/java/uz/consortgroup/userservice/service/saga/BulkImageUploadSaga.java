package uz.consortgroup.userservice.service.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class BulkImageUploadSaga {
    private final ImageFeignClient imageFeignClient;
    private final MentorActionLogger mentorActionLogger;

    public BulkImageUploadResponseDto run(UUID lessonId, String metadataJson, List<MultipartFile> files) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID mentorId = userDetails.getId();

        log.info("Starting bulk image upload for lessonId={}, mentorId={}, filesCount={}", lessonId, mentorId, files.size());

        BulkImageUploadResponseDto response;
        try {
            response = imageFeignClient.uploadImages(lessonId, metadataJson, files);
            log.info("Uploaded {} images for lessonId={}", response.getImages().size(), lessonId);
        } catch (Exception ex) {
            log.error("Failed to upload images for lessonId={}", lessonId, ex);
            throw ex;
        }

        try {
            for (ImageUploadResponseDto image : response.getImages()) {
                log.debug("Logging IMAGE_UPLOADED action for resourceId={}, mentorId={}", image.getResourceId(), mentorId);
                mentorActionLogger.logMentorResourceAction(image.getResourceId(), mentorId, MentorActionType.IMAGE_UPLOADED);
            }
        } catch (Exception ex) {
            log.warn("MentorAction logging failed. Rolling back uploaded images...");

            try {
                for (ImageUploadResponseDto image : response.getImages()) {
                    log.debug("Deleting image resourceId={} for lessonId={}", image.getResourceId(), lessonId);
                    imageFeignClient.deleteImage(lessonId, image.getResourceId());
                }
                log.info("Rollback successful: All uploaded images have been deleted.");
            } catch (Exception deleteEx) {
                log.error("Rollback failed while deleting images for lessonId={}", lessonId, deleteEx);
                throw new ImageUploadRollbackException("Ошибка при откате загруженных изображений", deleteEx);
            }

            log.error("Mentor action logging failed. Uploaded images were rolled back.", ex);
            throw new MentorActionLoggingException("Ошибка логирования. Все загруженные изображения удалены.", ex);
        }

        log.info("Bulk image upload and mentor action logging completed for lessonId={}", lessonId);
        return response;
    }
}
