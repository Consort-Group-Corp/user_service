package uz.consortgroup.userservice.service.saga;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.KafkaException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.video.BulkVideoUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.video.VideoUploadResponseDto;
import uz.consortgroup.userservice.client.VideoFeignClient;
import uz.consortgroup.userservice.event.mentor.MentorActionType;
import uz.consortgroup.userservice.exception.MentorActionLoggingException;
import uz.consortgroup.userservice.exception.VideoUploadRollbackException;
import uz.consortgroup.userservice.service.event.mentor.MentorActionLogger;
import uz.consortgroup.userservice.service.impl.UserDetailsImpl;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BulkVideoUploadSaga {
    private final VideoFeignClient videoFeignClient;
    private final MentorActionLogger mentorActionLogger;

    public BulkVideoUploadResponseDto run(UUID lessonId, String metadataJson, List<MultipartFile> files) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID mentorId = userDetails.getId();

        BulkVideoUploadResponseDto response = videoFeignClient.uploadVideos(lessonId, metadataJson, files);

        try {
            for (VideoUploadResponseDto video : response.getVideos()) {
                mentorActionLogger.logMentorResourceAction(video.getResourceId(), mentorId, MentorActionType.VIDEO_UPLOADED);
            }
        } catch (KafkaException kafkaEx) {
            try {
                for (VideoUploadResponseDto video : response.getVideos()) {
                    videoFeignClient.deleteVideo(lessonId, video.getResourceId());
                }
            } catch (Exception deleteEx) {
                throw new VideoUploadRollbackException("Ошибка при откате загруженных видео", deleteEx);
            }
            throw new MentorActionLoggingException("Ошибка логирования событий в Kafka. Все загруженные видео были удалены.", kafkaEx);
        }

        return response;
    }
}
