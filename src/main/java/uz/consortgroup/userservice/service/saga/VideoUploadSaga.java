package uz.consortgroup.userservice.service.saga;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.KafkaException;
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
public class VideoUploadSaga {
    private final VideoFeignClient videoFeignClient;
    private final MentorActionLogger mentorActionLogger;

    public VideoUploadResponseDto run(UUID lessonId, String metadataJson, MultipartFile file) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID mentorId = userDetails.getId();

        VideoUploadResponseDto response = videoFeignClient.uploadVideo(lessonId, metadataJson, file);

        try {
            mentorActionLogger.logMentorResourceAction(response.getResourceId(), mentorId, MentorActionType.VIDEO_UPLOADED);
        } catch (Exception ex) {
            try {
                videoFeignClient.deleteVideo(lessonId, response.getResourceId());
            } catch (Exception deleteEx) {
                throw new VideoUploadRollbackException("Не удалось удалить видео после ошибки логирования", deleteEx);
            }
            throw new MentorActionLoggingException("Ошибка логирования события загрузки видео. Операция отменена.", ex);
        }

        return response;
    }
}
