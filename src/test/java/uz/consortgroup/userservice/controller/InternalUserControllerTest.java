package uz.consortgroup.userservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.userservice.service.directory.UserDirectoryService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InternalUserControllerTest {

    @Mock
    private UserDirectoryService userDirectoryService;

    @InjectMocks
    private InternalUserController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getUserShortInfoById_shouldReturnUserInfo() throws Exception {
        UUID userId = UUID.randomUUID();
        UserShortInfoResponseDto responseDto = UserShortInfoResponseDto.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .position("Developer")
                .build();

        when(userDirectoryService.getUserInfo(userId)).thenReturn(Optional.of(responseDto));

        mockMvc.perform(get("/api/v1/internal/users/{userId}/short-info", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.position").value("Developer"));
    }

    @Test
    void getUserShortInfoById_shouldReturn400ForInvalidUUID() throws Exception {
        mockMvc.perform(get("/api/v1/internal/users/{userId}/short-info", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getShortInfoBulk_shouldReturnUsersMap() throws Exception {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        UserShortInfoResponseDto user1 = UserShortInfoResponseDto.builder()
                .id(userId1)
                .firstName("John")
                .lastName("Doe")
                .build();

        UserShortInfoResponseDto user2 = UserShortInfoResponseDto.builder()
                .id(userId2)
                .firstName("Jane")
                .lastName("Smith")
                .build();

        Map<UUID, UserShortInfoResponseDto> expectedResponse = Map.of(
                userId1, user1,
                userId2, user2
        );

        when(userDirectoryService.getUserInfoBulk(anyList())).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/internal/users/short-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"" + userId1 + "\", \"" + userId2 + "\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + userId1 + ".firstName").value("John"))
                .andExpect(jsonPath("$." + userId1 + ".lastName").value("Doe"))
                .andExpect(jsonPath("$." + userId2 + ".firstName").value("Jane"))
                .andExpect(jsonPath("$." + userId2 + ".lastName").value("Smith"));
    }

    @Test
    void getShortInfoBulk_shouldReturnEmptyMapWhenNoUsersFound() throws Exception {
        when(userDirectoryService.getUserInfoBulk(anyList())).thenReturn(Map.of());

        mockMvc.perform(post("/api/v1/internal/users/short-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getShortInfoBulk_shouldReturn400ForInvalidRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/internal/users/short-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid-json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getShortInfoBulk_shouldHandleMixedResults() throws Exception {
        UUID foundUserId = UUID.randomUUID();
        UUID notFoundUserId = UUID.randomUUID();

        UserShortInfoResponseDto foundUser = UserShortInfoResponseDto.builder()
                .id(foundUserId)
                .firstName("John")
                .build();

        Map<UUID, UserShortInfoResponseDto> expectedResponse = Map.of(foundUserId, foundUser);

        when(userDirectoryService.getUserInfoBulk(anyList())).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/internal/users/short-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"" + foundUserId + "\", \"" + notFoundUserId + "\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$." + foundUserId).exists())
                .andExpect(jsonPath("$." + foundUserId + ".firstName").value("John"))
                .andExpect(jsonPath("$." + notFoundUserId).doesNotExist());
    }
}