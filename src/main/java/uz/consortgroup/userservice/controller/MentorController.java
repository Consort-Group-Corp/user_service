package uz.consortgroup.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.image.BulkImageUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.image.ImageUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.BulkPdfFilesUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.PdfFileUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.video.BulkVideoUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.video.VideoUploadResponseDto;
import uz.consortgroup.userservice.service.proxy.course.CourseProxyService;
import uz.consortgroup.userservice.service.proxy.image.ImageProxyService;
import uz.consortgroup.userservice.service.proxy.pdf.PdfProxyService;
import uz.consortgroup.userservice.service.proxy.video.VideoProxyService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mentor")
public class MentorController {
    private final CourseProxyService courseProxyService;
    private final VideoProxyService videoProxyService;
    private final ImageProxyService imageProxyService;
    private final PdfProxyService pdfProxyService;

    @PostMapping(value = "/courses", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponseDto createCourse(@Valid @RequestBody CourseCreateRequestDto dto) {
        return courseProxyService.createCourse(dto);
    }

    @PostMapping(value = "/lessons/{lessonId}/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public VideoUploadResponseDto uploadVideo(
            @PathVariable UUID lessonId,
            @RequestPart("metadata") String metadataJson,
            @RequestPart("file") MultipartFile file
    ) {
        return videoProxyService.uploadVideo(lessonId, metadataJson, file);
    }

    @PostMapping(value = "/lessons/{lessonId}/videos/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public BulkVideoUploadResponseDto uploadVideos(
            @PathVariable UUID lessonId,
            @RequestPart("metadata") String metadataJson,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return videoProxyService.uploadVideos(lessonId, metadataJson, files);
    }

    @PostMapping(value = "/lessons/{lessonId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImageUploadResponseDto uploadImage(
            @PathVariable UUID lessonId,
            @RequestPart("metadata") String metadataJson,
            @RequestPart("file") MultipartFile file
    ) {
        return imageProxyService.uploadImage(lessonId, metadataJson, file);
    }

    @PostMapping(value = "/lessons/{lessonId}/images/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public BulkImageUploadResponseDto uploadImages(
            @PathVariable UUID lessonId,
            @RequestPart("metadata") String metadataJson,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return imageProxyService.uploadImages(lessonId, metadataJson, files);
    }

    @PostMapping(value = "/lessons/{lessonId}/pdfs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PdfFileUploadResponseDto uploadPdf(
            @PathVariable UUID lessonId,
            @RequestPart("metadata") String metadataJson,
            @RequestPart("file") MultipartFile file
    ) {
        return pdfProxyService.uploadPdfFile(lessonId, metadataJson, file);
    }

    @PostMapping(value = "/lessons/{lessonId}/pdfs/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public BulkPdfFilesUploadResponseDto uploadPdfs(
            @PathVariable UUID lessonId,
            @RequestPart("metadata") String metadataJson,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return pdfProxyService.uploadPdfFiles(lessonId, metadataJson, files);
    }
}