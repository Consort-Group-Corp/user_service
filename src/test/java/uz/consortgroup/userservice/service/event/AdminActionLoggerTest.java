package uz.consortgroup.userservice.service.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.event.admin.ActionType;
import uz.consortgroup.userservice.event.admin.UserCreatedEvent;
import uz.consortgroup.userservice.kafka.AdminActionLogProducer;
import uz.consortgroup.userservice.service.event.admin.AdminActionLogger;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminActionLoggerTest {

    @Mock
    private AdminActionLogProducer adminActionLogProducer;

    @InjectMocks
    private AdminActionLogger adminActionLogger;

    @Test
    void logUserCreationByAdmin_ShouldSendEvent() {
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setEmail("newuser@example.com");

        UUID adminId = UUID.randomUUID();
        ActionType actionType = ActionType.USER_CREATED;

        adminActionLogger.logUserCreationByAdmin(user, adminId, actionType);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserCreatedEvent>> captor = ArgumentCaptor.forClass(List.class);
        verify(adminActionLogProducer).sendUserCreatedEvents(Collections.singletonList(captor.capture()));

        List<UserCreatedEvent> events = captor.getValue();
        assertEquals(1, events.size());

        UserCreatedEvent event = events.get(0);
        assertEquals(userId, event.getUserId());
        assertEquals(adminId, event.getAdminId());
        assertEquals("newuser@example.com", event.getEmail());
        assertNull(event.getRole());
        assertEquals(actionType, event.getActionType());
        assertNotNull(event.getMessageId());
        assertNotNull(event.getCreatedAt());
        assertTrue(event.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void logUserCreationByAdmin_WhenProducerThrows_ShouldPropagateException() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("erroruser@example.com");
        UUID adminId = UUID.randomUUID();
        ActionType actionType = ActionType.USER_CREATED;

        doThrow(new RuntimeException("Kafka error"))
                .when(adminActionLogProducer).sendUserCreatedEvents(anyList());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                adminActionLogger.logUserCreationByAdmin(user, adminId, actionType)
        );
        assertEquals("Kafka error", ex.getMessage());
    }

    @Test
    void logUserCreationByAdmin_WithNullUser_ShouldThrowNullPointerException() {
        UUID adminId = UUID.randomUUID();
        ActionType actionType = ActionType.USER_CREATED;

        assertThrows(NullPointerException.class, () ->
                adminActionLogger.logUserCreationByAdmin(null, adminId, actionType)
        );

        verify(adminActionLogProducer, never()).sendUserCreatedEvents(anyList());
    }
}
