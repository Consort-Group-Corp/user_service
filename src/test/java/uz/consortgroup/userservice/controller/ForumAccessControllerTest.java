package uz.consortgroup.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessRequest;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessResponse;
import uz.consortgroup.userservice.service.forum.ForumAccessService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ForumAccessControllerTest {

    @Mock
    private ForumAccessService forumAccessService;

    @InjectMocks
    private ForumAccessController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void checkAccess_ByCourse_shouldReturnOkWithValidRequest() throws Exception {
        ForumAccessRequest request = new ForumAccessRequest();
        request.setUserId(UUID.randomUUID());
        request.setCourseId(UUID.randomUUID());

        ForumAccessResponse response = new ForumAccessResponse();
        when(forumAccessService.checkAccessByCourse(any(ForumAccessRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/forum-access/access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void checkAccess_ByCourse_shouldReturn400WhenMissingUserId() throws Exception {
        ForumAccessRequest request = new ForumAccessRequest();
        request.setCourseId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/forum-access/access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkAccess_ByCourse_shouldReturn400WhenMissingCourseId() throws Exception {
        ForumAccessRequest request = new ForumAccessRequest();
        request.setUserId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/forum-access/access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCourseIdByGroupId_shouldReturnOkWithValidGroupId() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        when(forumAccessService.getCourseIdByGroupId(groupId)).thenReturn(courseId);

        mockMvc.perform(get("/api/v1/forum-access/course-id/{groupId}", groupId))
                .andExpect(status().isOk())
                .andExpect(content().json("\"" + courseId + "\""));
    }

    @Test
    void getCourseIdByGroupId_shouldReturn404WhenGroupNotFound() throws Exception {
        UUID groupId = UUID.randomUUID();

        when(forumAccessService.getCourseIdByGroupId(groupId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/forum-access/course-id/{groupId}", groupId))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}