package uz.consortgroup.userservice.service.proxy.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.image.BulkImageUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.image.ImageUploadResponseDto;
import uz.consortgroup.userservice.service.saga.BulkImageUploadSaga;
import uz.consortgroup.userservice.service.saga.ImageUploadSaga;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageProxyServiceImplTest {

    @Mock
    private ImageUploadSaga imageUploadSaga;

    @Mock
    private BulkImageUploadSaga bulkImageUploadSaga;

    @Mock
    private MultipartFile file;

    @Mock
    private List<MultipartFile> files;

    @InjectMocks
    private ImageProxyServiceImpl imageProxyService;

    @Test
    void uploadImage_Success() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{\"key\":\"value\"}";
        ImageUploadResponseDto expectedResponse = new ImageUploadResponseDto();

        when(imageUploadSaga.run(lessonId, metadataJson, file)).thenReturn(expectedResponse);

        ImageUploadResponseDto actualResponse = imageProxyService.uploadImage(lessonId, metadataJson, file);

        assertEquals(expectedResponse, actualResponse);
        verify(imageUploadSaga).run(lessonId, metadataJson, file);
    }

    @Test
    void uploadImages_SagaThrowsException() {
        UUID lessonId = UUID.randomUUID();
        String metadataJson = "{\"key\":\"value\"}";

        when(bulkImageUploadSaga.run(lessonId, metadataJson, files))
            .thenThrow(new RuntimeException("Upload error"));

        assertThrows(RuntimeException.class, () -> 
            imageProxyService.uploadImages(lessonId, metadataJson, files));
    }
}