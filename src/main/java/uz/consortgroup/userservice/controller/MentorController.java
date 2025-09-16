package uz.consortgroup.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.enumeration.Language;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.course.request.image.BulkImageUploadRequestDto;
import uz.consortgroup.core.api.v1.dto.course.request.pdf.BulkPdfFilesUploadRequestDto;
import uz.consortgroup.core.api.v1.dto.course.request.video.BulkVideoUploadRequestDto;
import uz.consortgroup.core.api.v1.dto.course.request.video.VideoUploadRequestDto;
import uz.consortgroup.core.api.v1.dto.course.response.course.CoursePreviewResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.course.CourseResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.image.BulkImageUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.image.ImageUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.BulkPdfFilesUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.pdf.PdfFileUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.video.BulkVideoUploadResponseDto;
import uz.consortgroup.core.api.v1.dto.course.response.video.VideoUploadResponseDto;
import uz.consortgroup.userservice.handler.ErrorResponse;
import uz.consortgroup.userservice.service.proxy.course.CourseProxyService;
import uz.consortgroup.userservice.service.proxy.image.ImageProxyService;
import uz.consortgroup.userservice.service.proxy.pdf.PdfProxyService;
import uz.consortgroup.userservice.service.proxy.video.VideoProxyService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mentor")
@Tag(name = "Mentor", description = "Создание курса и загрузка материалов")
@SecurityRequirement(name = "bearerAuth")
public class MentorController {

    private final CourseProxyService courseProxyService;
    private final VideoProxyService videoProxyService;
    private final ImageProxyService imageProxyService;
    private final PdfProxyService pdfProxyService;

    // Док-тексты по форматам/лимитам (из application.yml)
    private static final String SERVER_LIMITS =
            "Серверные лимиты multipart: max-file-size=1000MB, max-request-size=1000MB.";
    private static final String VIDEO_RULES =
            "Видео-файл. Допустимые MIME: video/mp4, video/mpeg. Расширения: mp4, mpeg. " +
                    "Максимальный размер (storage): 1000MB. " + SERVER_LIMITS;
    private static final String IMAGE_RULES =
            "Файл изображения. Допустимые MIME: image/jpeg, image/png. Расширения: jpg, jpeg, png. " +
                    "Максимальный размер (storage): 100MB. " + SERVER_LIMITS;
    private static final String PDF_RULES =
            "PDF-файл. MIME: application/pdf. Расширение: pdf. " +
                    "Максимальный размер (storage): 20MB. " + SERVER_LIMITS;

    // ===== Создать курс (JSON) =====
    @PostMapping(value = "/courses", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            operationId = "createCourse",
            summary = "Создать курс",
            description = "Создаёт курс с переводами, модулями и уроками. Время указывать в ISO-8601 (UTC)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Курс создан",
                    content = @Content(schema = @Schema(implementation = CourseResponseDto.class),
                            examples = @ExampleObject(value = """
                {
                  "id": "9e09e19d-2988-453f-ab69-e8f39a8f723b",
                  "authorId": "e2f00427-fd76-41ad-940e-4624955f9384",
                  "courseType": "BASE",
                  "priceType": "PAID",
                  "priceAmount": 99.99,
                  "discountPercent": 10.0,
                  "startTime": "2025-05-01T10:00:00Z",
                  "endTime": "2025-07-01T10:00:00Z",
                  "accessDurationMin": 1440,
                  "courseStatus": "ACTIVE",
                  "coverImageUrl": "",
                  "createdAt": "2025-08-13T23:02:19.5947094",
                  "updatedAt": null,
                  "translations": [],
                  "modules": []
                }
                """))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CourseResponseDto createCourse(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные курса",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CourseCreateRequestDto.class))
            )
            @RequestBody @Valid CourseCreateRequestDto dto
    ) {
        return courseProxyService.createCourse(dto);
    }

    // ===== Загрузка видео (один файл) =====
    @PostMapping(value = "/lessons/{lessonId}/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            operationId = "uploadVideo",
            summary = "Загрузить видео к уроку",
            description = "Загрузка видео-файла и JSON-метаданных. " + VIDEO_RULES
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Видео загружено",
                    content = @Content(schema = @Schema(implementation = VideoUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации/формата",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "413", description = "Размер файла превышает 1000MB"),
            @ApiResponse(responseCode = "415", description = "Неподдерживаемый тип файла (разрешены: video/mp4, video/mpeg)")
    })
    public VideoUploadResponseDto uploadVideo(
            @Parameter(in = ParameterIn.PATH, name = "lessonId", required = true,
                    description = "ID урока", example = "b6a1dd6c-f6f1-4b42-9b0a-2d7a2d2d3c1f")
            @PathVariable UUID lessonId,

            @Parameter(
                    name = "metadata",
                    description = "JSON-метаданные видео",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = VideoUploadRequestDto.class),
                            examples = @ExampleObject(value = """
                {
                  "duration": 120,
                  "resolution": "1080p",
                  "orderPosition": 1,
                  "translations": [
                    {"language":"RU","title":"Введение","description":"Что будет в курсе"}
                  ]
                }
                """)))
            @RequestPart("metadata") String metadataJson,

            @Parameter(
                    name = "file",
                    description = "Видео-файл",
                    required = true,
                    content = {
                            @Content(mediaType = "video/mp4",
                                    schema = @Schema(type = "string", format = "binary")),
                            @Content(mediaType = "video/mpeg",
                                    schema = @Schema(type = "string", format = "binary"))
                    })
            @RequestPart("file") MultipartFile file
    ) {
        return videoProxyService.uploadVideo(lessonId, metadataJson, file);
    }

    // ===== Загрузка видео (bulk) =====
    @PostMapping(value = "/lessons/{lessonId}/videos/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            operationId = "uploadVideos",
            summary = "Загрузить несколько видео",
            description = "Передай JSON с массивом метаданных и такой же по длине массив файлов. " + VIDEO_RULES
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Видео загружены",
                    content = @Content(schema = @Schema(implementation = BulkVideoUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации/количества элементов",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "413", description = "Размер одного из файлов превышает 1000MB"),
            @ApiResponse(responseCode = "415", description = "Неподдерживаемый тип файла (разрешены: video/mp4, video/mpeg)")
    })
    public BulkVideoUploadResponseDto uploadVideos(
            @Parameter(in = ParameterIn.PATH, name = "lessonId", required = true,
                    description = "ID урока", example = "b6a1dd6c-f6f1-4b42-9b0a-2d7a2d2d3c1f")
            @PathVariable UUID lessonId,

            @Parameter(
                    name = "metadata",
                    description = "JSON с массивом метаданных видео",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BulkVideoUploadRequestDto.class),
                            examples = @ExampleObject(value = """
                {
                  "videos": [
                    {"duration": 120, "resolution": "1080p", "orderPosition": 1, "translations":[]},
                    {"duration": 95,  "resolution": "720p",  "orderPosition": 2, "translations":[]}
                  ]
                }
                """)))
            @RequestPart("metadata") String metadataJson,

            @Parameter(
                    name = "files",
                    description = "Список видео-файлов (длина должна совпадать с размером массива metadata.videos)",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
                    )
            )
            @RequestPart("files") List<MultipartFile> files
    ) {
        return videoProxyService.uploadVideos(lessonId, metadataJson, files);
    }

    // ===== Загрузка изображений =====
    @PostMapping(value = "/lessons/{lessonId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "uploadImage", summary = "Загрузить изображение к уроку",
            description = IMAGE_RULES)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Изображение загружено",
                    content = @Content(schema = @Schema(implementation = ImageUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации/формата",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "413", description = "Размер файла превышает 100MB"),
            @ApiResponse(responseCode = "415", description = "Неподдерживаемый тип файла (разрешены: image/jpeg, image/png)")
    })
    public ImageUploadResponseDto uploadImage(
            @Parameter(in = ParameterIn.PATH, name = "lessonId", required = true) @PathVariable UUID lessonId,

            @Parameter(
                    name = "metadata",
                    description = "JSON-метаданные изображения (ImageUploadRequestDto)",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {"orderPosition":1,"translations":[{"language":"RU","title":"Схема","description":"..."}]}
                """)))
            @RequestPart("metadata") String metadataJson,

            @Parameter(
                    name = "file",
                    description = "Файл изображения",
                    required = true,
                    content = {
                            @Content(mediaType = "image/jpeg",
                                    schema = @Schema(type = "string", format = "binary")),
                            @Content(mediaType = "image/png",
                                    schema = @Schema(type = "string", format = "binary"))
                    })
            @RequestPart("file") MultipartFile file
    ) {
        return imageProxyService.uploadImage(lessonId, metadataJson, file);
    }

    @PostMapping(value = "/lessons/{lessonId}/images/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "uploadImages", summary = "Загрузить несколько изображений",
            description = IMAGE_RULES + " Длина массива файлов должна совпадать с metadata.images.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Изображения загружены",
                    content = @Content(schema = @Schema(implementation = BulkImageUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации/количества элементов",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "413", description = "Размер одного из файлов превышает 100MB"),
            @ApiResponse(responseCode = "415", description = "Неподдерживаемый тип файла (разрешены: image/jpeg, image/png)")
    })
    public BulkImageUploadResponseDto uploadImages(
            @Parameter(in = ParameterIn.PATH, name = "lessonId", required = true) @PathVariable UUID lessonId,

            @Parameter(
                    name = "metadata",
                    description = "JSON с массивом метаданных (BulkImageUploadRequestDto)",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BulkImageUploadRequestDto.class),
                            examples = @ExampleObject(value = """
                {"images":[{"orderPosition":1,"translations":[]},{"orderPosition":2,"translations":[]}]}
                """)))
            @RequestPart("metadata") String metadataJson,

            @Parameter(
                    name = "files",
                    description = "Список изображений (длина должна совпадать с размером массива metadata.images)",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
                    )
            )
            @RequestPart("files") List<MultipartFile> files
    ) {
        return imageProxyService.uploadImages(lessonId, metadataJson, files);
    }

    // ===== Загрузка PDF =====
    @PostMapping(value = "/lessons/{lessonId}/pdfs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "uploadPdf", summary = "Загрузить PDF к уроку",
            description = PDF_RULES)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "PDF загружен",
                    content = @Content(schema = @Schema(implementation = PdfFileUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации/формата",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "413", description = "Размер файла превышает 20MB"),
            @ApiResponse(responseCode = "415", description = "Неподдерживаемый тип файла (разрешён: application/pdf)")
    })
    public PdfFileUploadResponseDto uploadPdf(
            @Parameter(in = ParameterIn.PATH, name = "lessonId", required = true) @PathVariable UUID lessonId,

            @Parameter(
                    name = "metadata",
                    description = "JSON-метаданные PDF (PdfFileUploadRequestDto)",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {"orderPosition":1,"translations":[{"language":"RU","title":"Методичка","description":""}]}
                """)))
            @RequestPart("metadata") String metadataJson,

            @Parameter(
                    name = "file",
                    description = "PDF-файл",
                    required = true,
                    content = @Content(mediaType = "application/pdf",
                            schema = @Schema(type = "string", format = "binary")))
            @RequestPart("file") MultipartFile file
    ) {
        return pdfProxyService.uploadPdfFile(lessonId, metadataJson, file);
    }

    @PostMapping(value = "/lessons/{lessonId}/pdfs/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "uploadPdfs", summary = "Загрузить несколько PDF",
            description = PDF_RULES + " Длина массива файлов должна совпадать с metadata.pdfs.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "PDF-файлы загружены",
                    content = @Content(schema = @Schema(implementation = BulkPdfFilesUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации/количества элементов",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "413", description = "Размер одного из файлов превышает 20MB"),
            @ApiResponse(responseCode = "415", description = "Неподдерживаемый тип файла (разрешён: application/pdf)")
    })
    public BulkPdfFilesUploadResponseDto uploadPdfs(
            @Parameter(in = ParameterIn.PATH, name = "lessonId", required = true) @PathVariable UUID lessonId,

            @Parameter(
                    name = "metadata",
                    description = "JSON с массивом метаданных (BulkPdfFilesUploadRequestDto)",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BulkPdfFilesUploadRequestDto.class),
                            examples = @ExampleObject(value = """
                {"pdfs":[{"orderPosition":1,"translations":[]},{"orderPosition":2,"translations":[]}]}
                """)))
            @RequestPart("metadata") String metadataJson,

            @Parameter(
                    name = "files",
                    description = "Список PDF-файлов (длина должна совпадать с размером массива metadata.pdfs)",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
                    )
            )
            @RequestPart("files") List<MultipartFile> files
    ) {
        return pdfProxyService.uploadPdfFiles(lessonId, metadataJson, files);
    }

    @Operation(
            summary = "Получить превью курса",
            description = "Возвращает краткое превью курса по его UUID и языку."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Превью успешно получено",
                    content = @Content(schema = @Schema(implementation = CoursePreviewResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    @GetMapping("/courses/{courseId}/preview")
    @ResponseStatus(HttpStatus.OK)
    public CoursePreviewResponseDto getCoursePreview(
            @Parameter(description = "UUID курса", example = "9e09e19d-2988-453f-ab69-e8f39a8f723b")
            @PathVariable UUID courseId,
            @Parameter(description = "Код языка", example = "RU",
                    schema = @Schema(allowableValues = {"RU", "EN", "UZ", "UZK", "KAA"}))
            @RequestParam Language language
    ) {
        return courseProxyService.getCoursePreview(courseId, language);
    }

    @Operation(
            summary = "Удалить курс",
            description = "Удаляет курс по его UUID. Если удаление успешно, возвращается статус 204."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Курс удалён"),
            @ApiResponse(responseCode = "401", description = "Неавторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    @DeleteMapping("/courses/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(
            @Parameter(description = "UUID курса", example = "9e09e19d-2988-453f-ab69-e8f39a8f723b")
            @PathVariable UUID courseId
    ) {
        courseProxyService.deleteCourse(courseId);
    }
}
