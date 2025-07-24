package uz.consortgroup.userservice.service.proxy.video;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.video.BulkVideoUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.video.VideoUploadResponseDto;
import uz.consortgroup.userservice.service.saga.BulkVideoUploadSaga;
import uz.consortgroup.userservice.service.saga.VideoUploadSaga;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoProxyServiceImplTest {

    @Mock
    private VideoUploadSaga videoUploadSaga;

    @Mock
    private BulkVideoUploadSaga bulkVideoUploadSaga;

    @InjectMocks
    private VideoProxyServiceImpl videoProxyService;

    @Test
    void uploadVideo_ShouldCallSagaAndReturnResponse() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{\"title\":\"Sample Video\"}";
        MultipartFile file = new MockMultipartFile("video.mp4", "video.mp4", "video/mp4", "content".getBytes());
        
        VideoUploadResponseDto expectedResponse = new VideoUploadResponseDto();
        when(videoUploadSaga.run(any(), any(), any())).thenReturn(expectedResponse);

        VideoUploadResponseDto actualResponse = videoProxyService.uploadVideo(lessonId, metadataJson, file);

        assertSame(expectedResponse, actualResponse);
        verify(videoUploadSaga).run(lessonId, metadataJson, file);
    }

    @Test
    void uploadVideo_ShouldHaveTransactionalAnnotation() throws NoSuchMethodException {
        var method = VideoProxyServiceImpl.class.getMethod("uploadVideo", UUID.class, String.class, MultipartFile.class);
        assertNotNull(method.getAnnotation(Transactional.class));
    }

    @Test
    void uploadVideos_ShouldHaveTransactionalAnnotation() throws NoSuchMethodException {
        var method = VideoProxyServiceImpl.class.getMethod("uploadVideos", UUID.class, String.class, List.class);
        assertNotNull(method.getAnnotation(Transactional.class));
    }
}