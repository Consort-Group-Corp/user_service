package uz.consortgroup.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.request.UserSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.response.UserSearchResponse;
import uz.consortgroup.userservice.service.user.UserSearchService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserSearchController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserSearchService userSearchService;

    @Test
    @WithMockUser(authorities = "SEARCHED_USER")
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

        mockMvc.perform(post("/api/v1/users/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser
    void searchUsers_shouldReturnBadRequest_whenQueryIsEmpty() throws Exception {
        UserSearchRequest request = new UserSearchRequest("");

        mockMvc.perform(post("/api/v1/users/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "SEARCHED_USER")
    void searchUsers_shouldReturnBadRequest_whenQueryIsTooShort() throws Exception {
        UserSearchRequest request = new UserSearchRequest("short");

        mockMvc.perform(post("/api/v1/users/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}