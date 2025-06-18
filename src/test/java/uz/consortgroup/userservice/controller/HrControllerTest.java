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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HrControllerTest {

    @Mock
    private HrForumGroupService hrForumGroupService;

    @InjectMocks
    private HrController hrController;

    private MockMvc mockMvc;
    private  ObjectMapper objectMapper ;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createForumGroupByHr_shouldReturnCreatedResponse() throws Exception {
        CreateForumGroupByHrRequest request = new CreateForumGroupByHrRequest();
        request.setCourseId(UUID.randomUUID());
        request.setUserIds(List.of(UUID.randomUUID()));
        request.setStartTime(Instant.now());
        request.setEndTime(Instant.now().plusSeconds(3600));

        HrForumGroupCreateResponse response = new HrForumGroupCreateResponse();
        when(hrForumGroupService.createHrForumGroup(any())).thenReturn(response);

        mockMvc = MockMvcBuilders.standaloneSetup(hrController).build();
        mockMvc.perform(post("/api/v1/hr/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void createForumGroupByHr_shouldReturn400WhenMissingCourseId() throws Exception {
        CreateForumGroupByHrRequest request = new CreateForumGroupByHrRequest();
        request.setUserIds(List.of(UUID.randomUUID()));
        request.setStartTime(Instant.now());
        request.setEndTime(Instant.now().plusSeconds(3600));

        mockMvc = MockMvcBuilders.standaloneSetup(hrController).build();
        mockMvc.perform(post("/api/v1/hr/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createForumGroupByHr_shouldReturn400WhenEmptyUserIds() throws Exception {
        CreateForumGroupByHrRequest request = new CreateForumGroupByHrRequest();
        request.setCourseId(UUID.randomUUID());
        request.setUserIds(List.of());
        request.setStartTime(Instant.now());
        request.setEndTime(Instant.now().plusSeconds(3600));

        mockMvc = MockMvcBuilders.standaloneSetup(hrController).build();
        mockMvc.perform(post("/api/v1/hr/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createForumGroupByHr_shouldReturn400WhenMissingStartTime() throws Exception {
        CreateForumGroupByHrRequest request = new CreateForumGroupByHrRequest();
        request.setCourseId(UUID.randomUUID());
        request.setUserIds(List.of(UUID.randomUUID()));
        request.setEndTime(Instant.now().plusSeconds(3600));

        mockMvc = MockMvcBuilders.standaloneSetup(hrController).build();
        mockMvc.perform(post("/api/v1/hr/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createForumGroupByHr_shouldReturn400WhenMissingEndTime() throws Exception {
        CreateForumGroupByHrRequest request = new CreateForumGroupByHrRequest();
        request.setCourseId(UUID.randomUUID());
        request.setUserIds(List.of(UUID.randomUUID()));
        request.setStartTime(Instant.now());

        mockMvc = MockMvcBuilders.standaloneSetup(hrController).build();
        mockMvc.perform(post("/api/v1/hr/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}