package uz.consortgroup.userservice.service.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class BulkVideoUploadSaga {
    private final VideoFeignClient videoFeignClient;
    private final MentorActionLogger mentorActionLogger;

    public BulkVideoUploadResponseDto run(UUID lessonId, String metadataJson, List<MultipartFile> files) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID mentorId = userDetails.getId();

        log.info("Starting bulk video upload for lessonId={}, mentorId={}, filesCount={}", lessonId, mentorId, files.size());

        BulkVideoUploadResponseDto response;
        try {
            response = videoFeignClient.uploadVideos(lessonId, metadataJson, files);
            log.info("Successfully uploaded {} videos for lessonId={}", response.getVideos().size(), lessonId);
        } catch (Exception ex) {
            log.error("Failed to upload videos for lessonId={}", lessonId, ex);
            throw ex;
        }

        try {
            for (VideoUploadResponseDto video : response.getVideos()) {
                log.debug("Logging VIDEO_UPLOADED action for resourceId={}, mentorId={}", video.getResourceId(), mentorId);
                mentorActionLogger.logMentorResourceAction(video.getResourceId(), mentorId, MentorActionType.VIDEO_UPLOADED);
            }
        } catch (Exception ex) {
            log.warn("MentorAction logging failed. Rolling back uploaded videos for lessonId={}", lessonId);
            try {
                for (VideoUploadResponseDto video : response.getVideos()) {
                    log.debug("Deleting video resourceId={} for lessonId={}", video.getResourceId(), lessonId);
                    videoFeignClient.deleteVideo(lessonId, video.getResourceId());
                }
                log.info("Rollback successful: All uploaded videos have been deleted for lessonId={}", lessonId);
            } catch (Exception deleteEx) {
                log.error("Rollback failed while deleting videos for lessonId={}", lessonId, deleteEx);
                throw new VideoUploadRollbackException("Ошибка при откате загруженных видео", deleteEx);
            }

            log.error("Mentor action logging failed. Uploaded videos were rolled back.", ex);
            throw new MentorActionLoggingException("Ошибка логирования событий. Все загруженные видео были удалены.", ex);
        }

        log.info("Bulk video upload and mentor action logging completed for lessonId={}", lessonId);
        return response;
    }
}
