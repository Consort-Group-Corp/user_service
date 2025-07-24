package uz.consortgroup.userservice.service.proxy.video;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.video.BulkVideoUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.video.VideoUploadResponseDto;
import uz.consortgroup.userservice.service.saga.BulkVideoUploadSaga;
import uz.consortgroup.userservice.service.saga.VideoUploadSaga;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProxyServiceImpl implements VideoProxyService {
    private final VideoUploadSaga videoUploadSaga;
    private final BulkVideoUploadSaga bulkVideoUploadSaga;

    @Override
    @Transactional
    public VideoUploadResponseDto uploadVideo(UUID lessonId, String metadataJson, MultipartFile file) {
        log.info("Uploading single video: lessonId={}, filename={}, metadata={}",
                lessonId, file.getOriginalFilename(), metadataJson);
        try {
            VideoUploadResponseDto response = videoUploadSaga.run(lessonId, metadataJson, file);
            log.debug("Uploaded video: resourceId={}, fileUrl={}, orderPosition={}, translationsCount={}",
                    response.getResourceId(), response.getFileUrl(),
                    response.getOrderPosition(),
                    response.getTranslations() != null ? response.getTranslations().size() : 0);
            return response;
        } catch (Exception e) {
            log.error("Failed to upload video for lessonId={}, filename={}", lessonId, file.getOriginalFilename(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public BulkVideoUploadResponseDto uploadVideos(UUID lessonId, String metadataJson, List<MultipartFile> files) {
        log.info("Uploading {} videos: lessonId={}, metadata={}", files.size(), lessonId, metadataJson);
        try {
            BulkVideoUploadResponseDto response = bulkVideoUploadSaga.run(lessonId, metadataJson, files);
            log.debug("Uploaded {} videos successfully", response.getVideos().size());
            return response;
        } catch (Exception e) {
            log.error("Bulk upload of videos failed for lessonId={}, totalFiles={}", lessonId, files.size(), e);
            throw e;
        }
    }
}
