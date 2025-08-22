package uz.consortgroup.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.consortgroup.core.api.v1.dto.user.enumeration.Communication;
import uz.consortgroup.core.api.v1.dto.user.enumeration.CreatorRole;
import uz.consortgroup.core.api.v1.dto.user.enumeration.Language;
import uz.consortgroup.core.api.v1.dto.user.request.NotificationCreateRequestDto;
import uz.consortgroup.core.api.v1.dto.user.request.TranslationDto;
import uz.consortgroup.userservice.service.notification.UserNotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserNotificationController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
class UserNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserNotificationService userNotificationService;


    @Test
    void sendNotification_shouldReturnBadRequest_whenCreatedByUserIdIsNull() throws Exception {
        NotificationCreateRequestDto request = createValidRequest();
        request.setCreatedByUserId(null);
        
        mockMvc.perform(post("/api/v1/user-notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendNotification_shouldReturnBadRequest_whenCreatorRoleIsNull() throws Exception {
        NotificationCreateRequestDto request = createValidRequest();
        request.setCreatorRole(null);
        
        mockMvc.perform(post("/api/v1/user-notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendNotification_shouldReturnBadRequest_whenCommunicationIsNull() throws Exception {
        NotificationCreateRequestDto request = createValidRequest();
        request.setCommunication(null);
        
        mockMvc.perform(post("/api/v1/user-notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendNotification_shouldReturnBadRequest_whenRecipientUserIdsIsEmpty() throws Exception {
        NotificationCreateRequestDto request = createValidRequest();
        request.setRecipientUserIds(List.of());
        
        mockMvc.perform(post("/api/v1/user-notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendNotification_shouldReturnBadRequest_whenTranslationsIsEmpty() throws Exception {
        NotificationCreateRequestDto request = createValidRequest();
        request.setTranslations(Map.of());
        
        mockMvc.perform(post("/api/v1/user-notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendNotification_shouldReturnBadRequest_whenTranslationTitleIsBlank() throws Exception {
        NotificationCreateRequestDto request = createValidRequest();
        request.getTranslations().get(Language.ENGLISH).setTitle("");
        
        mockMvc.perform(post("/api/v1/user-notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendNotification_shouldReturnBadRequest_whenTranslationMessageIsBlank() throws Exception {
        NotificationCreateRequestDto request = createValidRequest();
        request.getTranslations().get(Language.ENGLISH).setMessage(null);
        
        mockMvc.perform(post("/api/v1/user-notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private NotificationCreateRequestDto createValidRequest() {
        NotificationCreateRequestDto request = new NotificationCreateRequestDto();
        request.setCreatedByUserId(UUID.randomUUID());
        request.setCreatorRole(CreatorRole.SUPER_ADMIN);
        request.setTranslations(Map.of(Language.ENGLISH, new TranslationDto()));
        request.setCommunication(Communication.EMAIL);
        request.setSendAt(LocalDateTime.now().plusHours(1));
        request.setActive(true);
        request.setRecipientUserIds(List.of(UUID.randomUUID()));
        
        TranslationDto translation = new TranslationDto();
        translation.setTitle("Test Title");
        translation.setMessage("Test Message");
        
        request.setTranslations(Map.of(Language.ENGLISH, translation));
        return request;
    }
}