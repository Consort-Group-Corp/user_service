package uz.consortgroup.userservice.service.proxy.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.image.BulkImageUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.image.ImageUploadResponseDto;
import uz.consortgroup.userservice.service.saga.BulkImageUploadSaga;
import uz.consortgroup.userservice.service.saga.ImageUploadSaga;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageProxyServiceImpl implements ImageProxyService {
    private final ImageUploadSaga imageUploadSaga;
    private final BulkImageUploadSaga bulkImageUploadSaga;

    @Override
    @Transactional
    public ImageUploadResponseDto uploadImage(UUID lessonId, String metadataJson, MultipartFile file) {
        log.info("Uploading single image for lessonId: {}", lessonId);
        try {
            ImageUploadResponseDto response = imageUploadSaga.run(lessonId, metadataJson, file);
            log.debug("Successfully uploaded image. Image ID: {}", response.getResourceId());
            return response;
        } catch (Exception e) {
            log.error("Failed to upload image for lessonId: {}", lessonId, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public BulkImageUploadResponseDto uploadImages(UUID lessonId, String metadataJson, List<MultipartFile> files) {
        log.info("Uploading {} images for lessonId: {}", files.size(), lessonId);
        try {
            BulkImageUploadResponseDto response = bulkImageUploadSaga.run(lessonId, metadataJson, files);
            log.debug("Successfully uploaded {} images for lessonId: {}", response.getImages().size(), lessonId);
            return response;
        } catch (Exception e) {
            log.error("Failed to upload multiple images for lessonId: {}", lessonId, e);
            throw e;
        }
    }
}
