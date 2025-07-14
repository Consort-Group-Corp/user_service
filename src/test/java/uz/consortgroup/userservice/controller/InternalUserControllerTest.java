package uz.consortgroup.userservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uz.consortgroup.core.api.v1.dto.user.response.UserShortInfoResponseDto;
import uz.consortgroup.userservice.service.user.UserShortInfoService;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InternalUserControllerTest {

    @Mock
    private UserShortInfoService userShortInfoService;

    @InjectMocks
    private InternalUserController controller;

    private MockMvc mockMvc;

    @Test
    void getUserShortInfoById_shouldReturnUserInfo() throws Exception {
        UUID userId = UUID.randomUUID();
        UserShortInfoResponseDto responseDto = UserShortInfoResponseDto.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .position("Developer")
                .build();

        when(userShortInfoService.getUserShortInfoById(userId)).thenReturn(responseDto);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/internal/users/{userId}/short-info", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.position").value("Developer"));
    }

    @Test
    void getUserShortInfoById_shouldReturn404WhenUserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userShortInfoService.getUserShortInfoById(userId)).thenReturn(null);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/internal/users/{userId}/short-info", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void getUserShortInfoById_shouldReturnEmptyFieldsWhenDataMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        UserShortInfoResponseDto responseDto = UserShortInfoResponseDto.builder()
                .id(userId)
                .build();

        when(userShortInfoService.getUserShortInfoById(userId)).thenReturn(responseDto);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/internal/users/{userId}/short-info", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").doesNotExist())
                .andExpect(jsonPath("$.lastName").doesNotExist())
                .andExpect(jsonPath("$.position").doesNotExist());
    }

    @Test
    void getUserShortInfoById_shouldReturn400ForInvalidUUID() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/internal/users/{userId}/short-info", "invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserShortInfoById_shouldReturnCorrectContentType() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userShortInfoService.getUserShortInfoById(userId)).thenReturn(new UserShortInfoResponseDto());

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/internal/users/{userId}/short-info", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}