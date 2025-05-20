package uz.consortgroup.userservice.service.proxy.pdf;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.request.pdf.PdfFileUploadRequestDto;
import uz.consortgroup.core.api.v1.dto.course.request.pdf.BulkPdfFilesUploadRequestDto;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.PdfFileUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.BulkPdfFilesUploadResponseDto;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.client.PdfFeignClient;
import uz.consortgroup.userservice.service.saga.BulkPdfUploadSaga;
import uz.consortgroup.userservice.service.saga.PdfUploadSaga;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PdfProxyServiceImpl implements PdfProxyService {
    private final PdfUploadSaga pdfUploadSaga;
    private final BulkPdfUploadSaga bulkPdfUploadSaga;

    @Override
    @Transactional
    @AllAspect
    public PdfFileUploadResponseDto uploadPdfFile(UUID lessonId, String metadataJson, MultipartFile file) {
        return pdfUploadSaga.run(lessonId, metadataJson, file);
    }

    @Override
    @Transactional
    @AllAspect
    public BulkPdfFilesUploadResponseDto uploadPdfFiles(UUID lessonId, String metadataJson, List<MultipartFile> files) {
        return bulkPdfUploadSaga.run(lessonId, metadataJson, files);
    }
}
