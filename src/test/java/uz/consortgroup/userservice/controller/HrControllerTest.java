package uz.consortgroup.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uz.consortgroup.core.api.v1.dto.forum.CreateForumGroupByHrRequest;
import uz.consortgroup.core.api.v1.dto.forum.HrForumGroupCreateResponse;
import uz.consortgroup.userservice.service.forum_group.HrForumGroupService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HrControllerTest {

    @Mock
    private HrForumGroupService hrForumGroupService;

    @InjectMocks
    private HrController hrController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(hrController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createForumGroupByHr_shouldReturnCreatedResponse() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        Instant startTime = Instant.parse("2025-08-14T09:00:00Z");
        Instant endTime = Instant.parse("2025-09-14T09:00:00Z");

        CreateForumGroupByHrRequest request = new CreateForumGroupByHrRequest();
        request.setCourseId(courseId);
        request.setUserIds(List.of(userId));
        request.setOwnerId(userId);
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        HrForumGroupCreateResponse response = new HrForumGroupCreateResponse(groupId);
        when(hrForumGroupService.createHrForumGroup(any(CreateForumGroupByHrRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/hr/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.groupId").value(groupId.toString()));
    }

    @Test
    void createForumGroupByHr_shouldReturn400WhenMissingCourseId() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant startTime = Instant.parse("2025-08-14T09:00:00Z");
        Instant endTime = Instant.parse("2025-09-14T09:00:00Z");

        CreateForumGroupByHrRequest request = new CreateForumGroupByHrRequest();
        request.setUserIds(List.of(userId));
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        mockMvc.perform(post("/api/v1/hr/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createForumGroupByHr_shouldReturn400WhenEmptyUserIds() throws Exception {
        UUID courseId = UUID.randomUUID();
        Instant startTime = Instant.parse("2025-08-14T09:00:00Z");
        Instant endTime = Instant.parse("2025-09-14T09:00:00Z");

        CreateForumGroupByHrRequest request = new CreateForumGroupByHrRequest();
        request.setCourseId(courseId);
        request.setUserIds(List.of());
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        mockMvc.perform(post("/api/v1/hr/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createForumGroupByHr_shouldReturn400WhenMissingStartTime() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant endTime = Instant.parse("2025-09-14T09:00:00Z");

        CreateForumGroupByHrRequest request = new CreateForumGroupByHrRequest();
        request.setCourseId(courseId);
        request.setUserIds(List.of(userId));
        request.setEndTime(endTime);

        mockMvc.perform(post("/api/v1/hr/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createForumGroupByHr_shouldReturn400WhenMissingEndTime() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant startTime = Instant.parse("2025-08-14T09:00:00Z");

        CreateForumGroupByHrRequest request = new CreateForumGroupByHrRequest();
        request.setCourseId(courseId);
        request.setUserIds(List.of(userId));
        request.setStartTime(startTime);

        mockMvc.perform(post("/api/v1/hr/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createForumGroupByHr_shouldReturn400WhenEndTimeBeforeStartTime() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant startTime = Instant.parse("2025-09-14T09:00:00Z");
        Instant endTime = Instant.parse("2025-08-14T09:00:00Z");

        CreateForumGroupByHrRequest request = new CreateForumGroupByHrRequest();
        request.setCourseId(courseId);
        request.setUserIds(List.of(userId));
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        mockMvc.perform(post("/api/v1/hr/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createForumGroupByHr_shouldReturn400WhenInvalidUUIDFormat() throws Exception {
        String invalidRequest = """
        {
          "courseId": "invalid-uuid",
          "userIds": ["2fbbf276-e14f-4db3-a2b3-db543d51d69c"],
          "startTime": "2025-08-14T09:00:00Z",
          "endTime": "2025-09-14T09:00:00Z"
        }
        """;

        mockMvc.perform(post("/api/v1/hr/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createForumGroupByHr_shouldReturn400WhenInvalidDateTimeFormat() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String invalidRequest = """
        {
          "courseId": "%s",
          "userIds": ["%s"],
          "startTime": "invalid-date-time",
          "endTime": "2025-09-14T09:00:00Z"
        }
        """.formatted(courseId, userId);

        mockMvc.perform(post("/api/v1/hr/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}