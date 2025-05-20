package uz.consortgroup.userservice.service.proxy.video;

import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.video.BulkVideoUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.video.VideoUploadResponseDto;

import java.util.List;
import java.util.UUID;

public interface VideoProxyService {
    VideoUploadResponseDto uploadVideo(UUID lessonId, String metadataJson, MultipartFile file);
    BulkVideoUploadResponseDto uploadVideos(UUID lessonId, String metadataJson, List<MultipartFile> files);
}
