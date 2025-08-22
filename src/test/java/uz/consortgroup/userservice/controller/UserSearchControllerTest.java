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
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.request.UserBulkSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.request.UserSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.response.UserSearchResponse;
import uz.consortgroup.userservice.service.user.UserSearchService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserSearchControllerTest {

    @Mock
    private UserSearchService userSearchService;

    @InjectMocks
    private UserSearchController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void searchUsers_shouldReturnUser_whenValidRequest() throws Exception {
        UserSearchResponse response = UserSearchResponse.builder()
                .userId(UUID.randomUUID())
                .lastName("Doe")
                .firstName("John")
                .email("john.doe@example.com")
                .pinfl("12345678901234")
                .role(UserRole.STUDENT)
                .build();

        when(userSearchService.findUserByEmailOrPinfl(any(UserSearchRequest.class))).thenReturn(response);

        UserSearchRequest request = new UserSearchRequest("john.doe@example.com");

        mockMvc.perform(post("/api/v1/internal/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.pinfl").value("12345678901234"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void searchUsers_shouldReturnBadRequest_whenQueryIsEmpty() throws Exception {
        UserSearchRequest request = new UserSearchRequest("");

        mockMvc.perform(post("/api/v1/internal/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUsers_shouldReturnBadRequest_whenQueryIsTooShort() throws Exception {
        UserSearchRequest request = new UserSearchRequest("ab");

        mockMvc.perform(post("/api/v1/internal/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUsers_shouldReturnBadRequest_whenQueryIsNull() throws Exception {
        String request = "{\"query\": null}";

        mockMvc.perform(post("/api/v1/internal/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUsersBulk_shouldReturnBadRequest_whenQueriesIsEmpty() throws Exception {
        UserBulkSearchRequest request = new UserBulkSearchRequest();
        request.setQueries(List.of());

        mockMvc.perform(post("/api/v1/internal/users/bulk-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUsersBulk_shouldReturnBadRequest_whenQueriesIsNull() throws Exception {
        String request = "{\"queries\": null}";

        mockMvc.perform(post("/api/v1/internal/users/bulk-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}