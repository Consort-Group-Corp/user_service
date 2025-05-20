package uz.consortgroup.userservice.service.proxy.image;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.image.BulkImageUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.image.ImageUploadResponseDto;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.service.saga.BulkImageUploadSaga;
import uz.consortgroup.userservice.service.saga.ImageUploadSaga;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageProxyServiceImpl implements ImageProxyService{
    private final ImageUploadSaga imageUploadSaga;
    private final BulkImageUploadSaga bulkImageUploadSaga;

    @Override
    @Transactional
    @AllAspect
    public ImageUploadResponseDto uploadImage(UUID lessonId, String metadataJson, MultipartFile file) {
        return imageUploadSaga.run(lessonId, metadataJson, file);
    }

    @Override
    @Transactional
    @AllAspect
    public BulkImageUploadResponseDto uploadImages(UUID lessonId, String metadataJson, List<MultipartFile> files) {
        return bulkImageUploadSaga.run(lessonId, metadataJson, files);
    }
}
