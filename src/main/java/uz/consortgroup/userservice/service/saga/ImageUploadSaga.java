package uz.consortgroup.userservice.service.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ImageUploadSaga {
    private final ImageFeignClient imageFeignClient;
    private final MentorActionLogger mentorActionLogger;

    public ImageUploadResponseDto run(UUID lessonId, String metadataJson, MultipartFile file) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID mentorId = userDetails.getId();

        log.info("Starting image upload saga: lessonId={}, mentorId={}, filename={}", lessonId, mentorId, file.getOriginalFilename());

        ImageUploadResponseDto response = imageFeignClient.uploadImage(lessonId, metadataJson, file);
        UUID resourceId = response.getResourceId();

        log.debug("Image uploaded successfully: resourceId={}, lessonId={}", resourceId, lessonId);

        try {
            mentorActionLogger.logMentorResourceAction(resourceId, mentorId, MentorActionType.IMAGE_UPLOADED);
            log.info("Mentor action logged: resourceId={}, mentorId={}, action={}", resourceId, mentorId, MentorActionType.IMAGE_UPLOADED);
        } catch (Exception ex) {
            log.error("Logging mentor action failed, attempting rollback: resourceId={}, lessonId={}", resourceId, lessonId, ex);
            try {
                imageFeignClient.deleteImage(lessonId, resourceId);
                log.info("Rollback successful: deleted image resourceId={}, lessonId={}", resourceId, lessonId);
            } catch (Exception deleteEx) {
                log.error("Rollback failed: unable to delete uploaded image. resourceId={}, lessonId={}", resourceId, lessonId, deleteEx);
                throw new ImageUploadRollbackException("Не удалось удалить изображение после ошибки логирования.", deleteEx);
            }
            throw new MentorActionLoggingException("Ошибка логирования события загрузки изображения. Операция отменена.", ex);
        }

        return response;
    }
}
