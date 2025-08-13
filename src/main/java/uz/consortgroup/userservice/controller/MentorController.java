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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.course.request.image.BulkImageUploadRequestDto;
import uz.consortgroup.core.api.v1.dto.course.request.pdf.BulkPdfFilesUploadRequestDto;
import uz.consortgroup.core.api.v1.dto.course.request.video.BulkVideoUploadRequestDto;
import uz.consortgroup.core.api.v1.dto.course.request.video.VideoUploadRequestDto;
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
                  "translations": [
                    {
                      "id": "36a9400d-8821-4da8-88a4-b0eedfabbfb4",
                      "language": "RU",
                      "title": "Курс по Java",
                      "description": "Углублённый курс по разработке на Java",
                      "slug": "Курс по java для начинающих"
                    },
                    {
                      "id": "adc5625c-dcda-4b07-8869-b0698a1848dd",
                      "language": "EN",
                      "title": "Java Course",
                      "description": "Advanced Java development course",
                      "slug": "Java junior course"
                    }
                  ],
                  "modules": [
                    {
                      "id": "8dee3104-5753-45fa-acd2-d60585304619",
                      "courseId": "9e09e19d-2988-453f-ab69-e8f39a8f723b",
                      "moduleName": "Основы Java",
                      "orderPosition": 1,
                      "isActive": true,
                      "createdAt": "2025-08-13T23:02:19.6200271",
                      "updatedAt": null,
                      "translations": [
                        {
                          "id": "f1d2435a-26a8-4a14-beab-2c57bbcc2db1",
                          "language": "RU",
                          "title": "Введение",
                          "description": "Первый модуль — знакомство с курсом"
                        },
                        {
                          "id": "16bd1233-c497-4e99-af4a-7a898d06ed37",
                          "language": "EN",
                          "title": "Introduction",
                          "description": "First module — course overview"
                        }
                      ],
                      "lessons": [
                        {
                          "id": "4716b6d1-a634-4b50-8a46-c249c248ca22",
                          "moduleId": "8dee3104-5753-45fa-acd2-d60585304619",
                          "orderPosition": 1,
                          "lessonType": "VIDEO",
                          "contentUrl": "url-to-content",
                          "durationMinutes": 30,
                          "isPreview": true,
                          "createdAt": "2025-08-13T23:02:19.6220275",
                          "updatedAt": "2025-08-13T23:02:19.6270273",
                          "translations": [
                            {
                              "id": "799dcfd9-db6f-47e1-8680-dc8d4a8d9a67",
                              "language": "RU",
                              "title": "Урок 1",
                              "description": "Первый урок"
                            },
                            {
                              "id": "47473dcb-0501-4139-b9f5-df8b1906bcac",
                              "language": "EN",
                              "title": "Lesson 1",
                              "description": "First lesson"
                            }
                          ]
                        }
                      ]
                    }
                  ]
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
                    content = @Content(schema = @Schema(implementation = CourseCreateRequestDto.class),
                            examples = @ExampleObject(value = """
                {
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
                  "translations": [
                    {
                      "language": "RU",
                      "title": "Курс по Java",
                      "description": "Углублённый курс по разработке на Java",
                      "slug": "Курс по java для начинающих"
                    },
                    {
                      "language": "EN",
                      "title": "Java Course",
                      "description": "Advanced Java development course",
                      "slug": "Java junior course"
                    }
                  ],
                  "modules": [
                    {
                      "moduleName": "Основы Java",
                      "orderPosition": 1,
                      "translations": [
                        {
                          "language": "RU",
                          "title": "Введение",
                          "description": "Первый модуль — знакомство с курсом",
                          "slug": "vvedenie"
                        },
                        {
                          "language": "EN",
                          "title": "Introduction",
                          "description": "First module — course overview",
                          "slug": "introduction to module"
                        }
                      ],
                      "lessons": [
                        {
                          "orderPosition": 1,
                          "contentUrl": "url-to-content",
                          "lessonType": "VIDEO",
                          "isPreview": true,
                          "durationMinutes": 30,
                          "translations": [
                            {
                              "language": "RU",
                              "title": "Урок 1",
                              "description": "Первый урок",
                              "slug": "urok-1"
                            },
                            {
                              "language": "EN",
                              "title": "Lesson 1",
                              "description": "First lesson",
                              "slug": "lesson-1"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
                """))
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
            description = "Загрузка видео-файла и JSON-метаданных (см. пример)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Видео загружено",
                    content = @Content(schema = @Schema(implementation = VideoUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации/формата",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary")))
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
            description = "Передай JSON с массивом объектов метаданных и такой же по длине массив файлов."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Видео загружены",
                    content = @Content(schema = @Schema(implementation = BulkVideoUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации/количества элементов",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизовано",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
                            array = @ArraySchema(
                                    schema = @Schema(type = "string", format = "binary")
                            )
                    )
            )
            @RequestPart("files") List<MultipartFile> files
    ) {
        return videoProxyService.uploadVideos(lessonId, metadataJson, files);
    }

    // ===== Загрузка изображений =====
    @PostMapping(value = "/lessons/{lessonId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "uploadImage", summary = "Загрузить изображение к уроку")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Изображение загружено",
                    content = @Content(schema = @Schema(implementation = ImageUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации/формата",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary")))
            @RequestPart("file") MultipartFile file
    ) {
        return imageProxyService.uploadImage(lessonId, metadataJson, file);
    }

    @PostMapping(value = "/lessons/{lessonId}/images/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "uploadImages", summary = "Загрузить несколько изображений")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Изображения загружены",
                    content = @Content(schema = @Schema(implementation = BulkImageUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации/количества элементов",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
                            array = @ArraySchema(
                                    schema = @Schema(type = "string", format = "binary")
                            )
                    )
            )
            @RequestPart("files") List<MultipartFile> files
    ) {
        return imageProxyService.uploadImages(lessonId, metadataJson, files);
    }

    // ===== Загрузка PDF =====
    @PostMapping(value = "/lessons/{lessonId}/pdfs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "uploadPdf", summary = "Загрузить PDF к уроку")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "PDF загружен",
                    content = @Content(schema = @Schema(implementation = PdfFileUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации/формата",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary")))
            @RequestPart("file") MultipartFile file
    ) {
        return pdfProxyService.uploadPdfFile(lessonId, metadataJson, file);
    }

    @PostMapping(value = "/lessons/{lessonId}/pdfs/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "uploadPdfs", summary = "Загрузить несколько PDF")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "PDF-файлы загружены",
                    content = @Content(schema = @Schema(implementation = BulkPdfFilesUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации/количества элементов",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
                            array = @ArraySchema(
                                    schema = @Schema(type = "string", format = "binary")
                            )
                    )
            )
            @RequestPart("files") List<MultipartFile> files
    ) {
        return pdfProxyService.uploadPdfFiles(lessonId, metadataJson, files);
    }
}
