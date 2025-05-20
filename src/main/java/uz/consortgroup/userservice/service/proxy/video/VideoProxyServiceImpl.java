package uz.consortgroup.userservice.service.proxy.video;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.video.BulkVideoUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.video.VideoUploadResponseDto;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.service.saga.BulkVideoUploadSaga;
import uz.consortgroup.userservice.service.saga.VideoUploadSaga;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoProxyServiceImpl implements VideoProxyService {
    private final VideoUploadSaga videoUploadSaga;
    private final BulkVideoUploadSaga bulkVideoUploadSaga;

    @Override
    @AllAspect
    @Transactional
    public VideoUploadResponseDto uploadVideo(UUID lessonId, String metadataJson, MultipartFile file) {
        return videoUploadSaga.run(lessonId, metadataJson, file);
    }

    @Override
    @AllAspect
    @Transactional
    public BulkVideoUploadResponseDto uploadVideos(UUID lessonId, String metadataJson, List<MultipartFile> files) {
        return bulkVideoUploadSaga.run(lessonId, metadataJson, files);
    }
}
