package uz.consortgroup.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uz.consortgroup.core.api.v1.dto.course.enumeration.CourseStatus;
import uz.consortgroup.core.api.v1.dto.course.enumeration.CourseType;
import uz.consortgroup.core.api.v1.dto.course.enumeration.PriceType;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.course.request.course.CourseTranslationRequestDto;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MentorControllerTest {

    @Mock
    private CourseProxyService courseProxyService;
    @Mock
    private VideoProxyService videoProxyService;
    @Mock
    private ImageProxyService imageProxyService;
    @Mock
    private PdfProxyService pdfProxyService;

    @InjectMocks
    private MentorController mentorController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createCourse_shouldReturnCreatedResponseWithMinimalValidRequest() throws Exception {
        CourseCreateRequestDto request = new CourseCreateRequestDto();
        request.setAuthorId(UUID.randomUUID());
        request.setCourseType(CourseType.PREMIUM);
        request.setPriceType(PriceType.FREE);
        request.setCourseStatus(CourseStatus.ACTIVE);
        request.setTranslations(List.of(new CourseTranslationRequestDto()));

        when(courseProxyService.createCourse(any())).thenReturn(new CourseResponseDto());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(mentorController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();

        mockMvc.perform(post("/api/v1/mentor/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createCourse_shouldReturn400WhenMissingRequiredFields() throws Exception {
        CourseCreateRequestDto request = new CourseCreateRequestDto();

        mockMvc = MockMvcBuilders.standaloneSetup(mentorController).build();
        mockMvc.perform(post("/api/v1/mentor/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadVideo_shouldReturnCreatedResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());
        MockMultipartFile metadata = new MockMultipartFile("metadata", "", "application/json", "{}".getBytes());

        when(videoProxyService.uploadVideo(any(), any(), any())).thenReturn(new VideoUploadResponseDto());

        mockMvc = MockMvcBuilders.standaloneSetup(mentorController).build();
        mockMvc.perform(multipart("/api/v1/mentor/lessons/{lessonId}/videos", UUID.randomUUID())
                .file(file)
                .file(metadata))
                .andExpect(status().isCreated());
    }

    @Test
    void uploadVideo_shouldReturn400WhenNoFileProvided() throws Exception {
        MockMultipartFile metadata = new MockMultipartFile("metadata", "", "application/json", "{}".getBytes());

        mockMvc = MockMvcBuilders.standaloneSetup(mentorController).build();
        mockMvc.perform(multipart("/api/v1/mentor/lessons/{lessonId}/videos", UUID.randomUUID())
                .file(metadata))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadVideos_shouldReturnCreatedResponse() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("files", "video1.mp4", "video/mp4", "content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "video2.mp4", "video/mp4", "content".getBytes());
        MockMultipartFile metadata = new MockMultipartFile("metadata", "", "application/json", "{}".getBytes());

        when(videoProxyService.uploadVideos(any(), any(), any())).thenReturn(new BulkVideoUploadResponseDto());

        mockMvc = MockMvcBuilders.standaloneSetup(mentorController).build();
        mockMvc.perform(multipart("/api/v1/mentor/lessons/{lessonId}/videos/bulk", UUID.randomUUID())
                .file(file1)
                .file(file2)
                .file(metadata))
                .andExpect(status().isCreated());
    }

    @Test
    void uploadImage_shouldReturnCreatedResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "content".getBytes());
        MockMultipartFile metadata = new MockMultipartFile("metadata", "", "application/json", "{}".getBytes());

        when(imageProxyService.uploadImage(any(), any(), any())).thenReturn(new ImageUploadResponseDto());

        mockMvc = MockMvcBuilders.standaloneSetup(mentorController).build();
        mockMvc.perform(multipart("/api/v1/mentor/lessons/{lessonId}/images", UUID.randomUUID())
                .file(file)
                .file(metadata))
                .andExpect(status().isCreated());
    }

    @Test
    void uploadImages_shouldReturnCreatedResponse() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("files", "image1.jpg", "image/jpeg", "content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "image2.jpg", "image/jpeg", "content".getBytes());
        MockMultipartFile metadata = new MockMultipartFile("metadata", "", "application/json", "{}".getBytes());

        when(imageProxyService.uploadImages(any(), any(), any())).thenReturn(new BulkImageUploadResponseDto());

        mockMvc = MockMvcBuilders.standaloneSetup(mentorController).build();
        mockMvc.perform(multipart("/api/v1/mentor/lessons/{lessonId}/images/bulk", UUID.randomUUID())
                .file(file1)
                .file(file2)
                .file(metadata))
                .andExpect(status().isCreated());
    }

    @Test
    void uploadPdf_shouldReturnCreatedResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", "content".getBytes());
        MockMultipartFile metadata = new MockMultipartFile("metadata", "", "application/json", "{}".getBytes());

        when(pdfProxyService.uploadPdfFile(any(), any(), any())).thenReturn(new PdfFileUploadResponseDto());

        mockMvc = MockMvcBuilders.standaloneSetup(mentorController).build();
        mockMvc.perform(multipart("/api/v1/mentor/lessons/{lessonId}/pdfs", UUID.randomUUID())
                .file(file)
                .file(metadata))
                .andExpect(status().isCreated());
    }

    @Test
    void uploadPdfs_shouldReturnCreatedResponse() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("files", "doc1.pdf", "application/pdf", "content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "doc2.pdf", "application/pdf", "content".getBytes());
        MockMultipartFile metadata = new MockMultipartFile("metadata", "", "application/json", "{}".getBytes());

        when(pdfProxyService.uploadPdfFiles(any(), any(), any())).thenReturn(new BulkPdfFilesUploadResponseDto());

        mockMvc = MockMvcBuilders.standaloneSetup(mentorController).build();
        mockMvc.perform(multipart("/api/v1/mentor/lessons/{lessonId}/pdfs/bulk", UUID.randomUUID())
                .file(file1)
                .file(file2)
                .file(metadata))
                .andExpect(status().isCreated());
    }
}