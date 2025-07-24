package uz.consortgroup.userservice.service.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.video.VideoUploadResponseDto;
import uz.consortgroup.userservice.client.VideoFeignClient;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
import uz.consortgroup.userservice.exception.VideoUploadRollbackException;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;
import uz.consortgroup.userservice.service.impl.UserDetailsImpl;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoUploadSaga {
    private final VideoFeignClient videoFeignClient;
    private final MentorActionLogger mentorActionLogger;

    public VideoUploadResponseDto run(UUID lessonId, String metadataJson, MultipartFile file) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID mentorId = userDetails.getId();

        log.info("Начало саги загрузки видео: lessonId={}, mentorId={}, filename={}", lessonId, mentorId, file.getOriginalFilename());

        VideoUploadResponseDto response = videoFeignClient.uploadVideo(lessonId, metadataJson, file);
        UUID resourceId = response.getResourceId();

        log.debug("Видео успешно загружено: resourceId={}, lessonId={}", resourceId, lessonId);

        try {
            mentorActionLogger.logMentorResourceAction(resourceId, mentorId, MentorActionType.VIDEO_UPLOADED);
            log.info("Лог действия наставника записан: resourceId={}, mentorId={}, action={}", resourceId, mentorId, MentorActionType.VIDEO_UPLOADED);
        } catch (Exception ex) {
            log.error("Ошибка логирования действия, откат: resourceId={}, lessonId={}", resourceId, lessonId, ex);
            try {
                videoFeignClient.deleteVideo(lessonId, resourceId);
                log.info("Откат выполнен: видео удалено. resourceId={}, lessonId={}", resourceId, lessonId);
            } catch (Exception deleteEx) {
                log.error("Ошибка при откате: не удалось удалить видео. resourceId={}, lessonId={}", resourceId, lessonId, deleteEx);
                throw new VideoUploadRollbackException("Не удалось удалить видео после ошибки логирования", deleteEx);
            }
            throw new MentorActionLoggingException("Ошибка логирования события загрузки видео. Операция отменена.", ex);
        }

        return response;
    }
}
