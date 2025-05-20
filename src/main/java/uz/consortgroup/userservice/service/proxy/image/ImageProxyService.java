package uz.consortgroup.userservice.service.proxy.image;

import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.image.BulkImageUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.image.ImageUploadResponseDto;

import java.util.List;
import java.util.UUID;

public interface ImageProxyService {
   ImageUploadResponseDto uploadImage(UUID lessonId, String metadataJson, MultipartFile file);
   BulkImageUploadResponseDto uploadImages(UUID lessonId, String metadataJson, List<MultipartFile> files);
}
