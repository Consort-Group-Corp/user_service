package uz.consortgroup.userservice.service.proxy.pdf;

import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.BulkPdfFilesUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.PdfFileUploadResponseDto;

import java.util.List;
import java.util.UUID;

public interface PdfProxyService {
    PdfFileUploadResponseDto uploadPdfFile(UUID lessonId, String metadataJson, MultipartFile file);
    BulkPdfFilesUploadResponseDto uploadPdfFiles(UUID lessonId, String metadataJson, List<MultipartFile> files);
}
