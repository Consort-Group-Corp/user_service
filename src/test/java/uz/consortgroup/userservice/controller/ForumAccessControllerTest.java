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
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessByCourseRequest;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessByGroupRequest;
import uz.consortgroup.core.api.v1.dto.forum.ForumAccessResponse;
import uz.consortgroup.core.api.v1.dto.forum.enumeration.ForumAccessReason;
import uz.consortgroup.userservice.service.forum.ForumAccessService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    void checkAccessByCourse_shouldReturnOkWithValidRequest() throws Exception {
        ForumAccessByCourseRequest request = new ForumAccessByCourseRequest();
        request.setUserId(UUID.randomUUID());
        request.setCourseId(UUID.randomUUID());

        ForumAccessResponse response = new ForumAccessResponse();
        response.setHasAccess(true);
        response.setReason(ForumAccessReason.USER_HAS_ACCESS);

        when(forumAccessService.checkAccessByCourse(any(ForumAccessByCourseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/forum-access/by-course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hasAccess").value(true))
                .andExpect(jsonPath("$.reason").value("USER_HAS_ACCESS"));
    }

    @Test
    void checkAccessByCourse_shouldReturn400WhenMissingUserId() throws Exception {
        ForumAccessByCourseRequest request = new ForumAccessByCourseRequest();
        request.setCourseId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/forum-access/by-course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkAccessByCourse_shouldReturn400WhenMissingCourseId() throws Exception {
        ForumAccessByCourseRequest request = new ForumAccessByCourseRequest();
        request.setUserId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/forum-access/by-course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkAccessByGroup_shouldReturn400WhenMissingUserId() throws Exception {
        ForumAccessByGroupRequest request = new ForumAccessByGroupRequest();
        request.setGroupId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/forum-access/by-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkAccessByGroup_shouldReturn400WhenMissingGroupId() throws Exception {
        ForumAccessByGroupRequest request = new ForumAccessByGroupRequest();
        request.setUserId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/forum-access/by-group")
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
                .andExpect(content().string("\"" + courseId.toString() + "\""));
    }

    @Test
    void getCourseIdByGroupId_shouldReturnOkWhenGroupNotFound() throws Exception {
        UUID groupId = UUID.randomUUID();

        when(forumAccessService.getCourseIdByGroupId(groupId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/forum-access/course-id/{groupId}", groupId))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void getCourseIdByGroupId_shouldReturn400ForInvalidUUID() throws Exception {
        mockMvc.perform(get("/api/v1/forum-access/course-id/{groupId}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }
}