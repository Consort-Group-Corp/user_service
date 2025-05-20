package uz.consortgroup.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.video.BulkVideoUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.video.VideoUploadResponseDto;
import uz.consortgroup.userservice.config.client.FeignClientConfig;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "course-service",
        contextId = "videoClient",
        url = "${course.service.url}",
        configuration = FeignClientConfig.class
)
public interface VideoFeignClient {
    @PostMapping(value = "/api/v1/lessons/{lessonId}/videos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    VideoUploadResponseDto uploadVideo(
            @PathVariable("lessonId") UUID lessonId,
            @RequestPart("metadata") String metadataJson,
            @RequestPart("file") MultipartFile file
    );

    @PostMapping(value = "/api/v1/lessons/{lessonId}/videos/bulk",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    BulkVideoUploadResponseDto uploadVideos(
            @PathVariable("lessonId") UUID lessonId,
            @RequestPart("metadata") String metadataJson,
            @RequestPart("files") List<MultipartFile> files
    );

    @DeleteMapping("/api/v1/lessons/{lessonId}/videos/{resourceId}")
    void deleteVideo(
            @PathVariable("lessonId") UUID lessonId,
            @PathVariable("resourceId") UUID resourceId
    );

    @DeleteMapping(
            value = "/api/v1/lessons/{lessonId}/videos/bulk",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void deleteVideos(
            @PathVariable("lessonId") UUID lessonId,
            @RequestBody List<UUID> resourceIds
    );
}
