package uz.consortgroup.userservice.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.consortgroup.core.api.v1.dto.user.request.UpdatePasswordRequestDto;
import uz.consortgroup.userservice.security.AuthContext;
import uz.consortgroup.userservice.service.password.PasswordServiceImpl;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PasswordController.class)
@AutoConfigureMockMvc(addFilters = false)
class PasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AuthContext authContext;

    @MockitoBean
    private PasswordServiceImpl passwordServiceImpl;

    @Test
    @WithMockUser
    void resetPassword_ShouldReturnSuccessMessage() throws Exception {
        UUID userId = authContext.getCurrentUserId();
        doNothing().when(passwordServiceImpl).requestPasswordResetForCurrentUser();

        mockMvc.perform(post("/api/v1/password/recovery", userId))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Password reset request sent"));
    }

    @Test
    @WithMockUser
    void resetPassword_ShouldHandleServiceException() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new RuntimeException("Error")).when(passwordServiceImpl).requestPasswordResetForCurrentUser();

        mockMvc.perform(post("/api/v1/password/recovery", userId))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser
    void updatePassword_ShouldReturnSuccessMessage() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = "valid-token";

        doNothing().when(passwordServiceImpl).updatePassword(any(UpdatePasswordRequestDto.class), eq(token));

        mockMvc.perform(put("/api/v1/password/new-password", userId)
                        .param("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"newPassword123\",\"confirmPassword\":\"newPassword123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully"));
    }

    @Test
    @WithMockUser
    void updatePassword_ShouldHandleInvalidRequest() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = "valid-token";

        mockMvc.perform(put("/api/v1/password/new-password", userId)
                .param("token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updatePassword_ShouldHandleServiceException() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = "invalid-token";
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto();
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("newPassword123");

        doThrow(new RuntimeException("Invalid token"))
                .when(passwordServiceImpl).updatePassword(request, token);

        mockMvc.perform(put("/api/v1/password/new-password", userId)
                        .param("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"newPassword123\",\"confirmPassword\":\"newPassword123\"}"))
                .andExpect(status().is5xxServerError());
    }
}