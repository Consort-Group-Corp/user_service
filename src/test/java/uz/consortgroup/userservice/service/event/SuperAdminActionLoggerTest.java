package uz.consortgroup.userservice.service.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.event.admin.SuperAdminActionType;
import uz.consortgroup.userservice.event.admin.SuperAdminActionEvent;
import uz.consortgroup.userservice.kafka.SuperAdminActionLogProducer;
import uz.consortgroup.userservice.service.event.admin.SuperAdminActionLogger;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuperAdminActionLoggerTest {

    @Mock
    private SuperAdminActionLogProducer superAdminActionLogProducer;

    @InjectMocks
    private SuperAdminActionLogger superAdminActionLogger;

    @Test
    void logSuperAdmin_Actions_ShouldSendEvent() {
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setEmail("newuser@example.com");

        UUID adminId = UUID.randomUUID();
        SuperAdminActionType superAdminActionType = SuperAdminActionType.USER_CREATED;

        superAdminActionLogger.userRoleChangedEvent(user, adminId, superAdminActionType);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SuperAdminActionEvent>> captor = ArgumentCaptor.forClass(List.class);
        verify(superAdminActionLogProducer).sendSuperAdminActionEvents(Collections.singletonList(captor.capture()));

        List<SuperAdminActionEvent> events = captor.getValue();
        assertEquals(1, events.size());

        SuperAdminActionEvent event = events.get(0);
        assertEquals(userId, event.getUserId());
        assertEquals(adminId, event.getAdminId());
        assertEquals("newuser@example.com", event.getEmail());
        assertNull(event.getRole());
        assertEquals(superAdminActionType, event.getSuperAdminActionType());
        assertNotNull(event.getMessageId());
        assertNotNull(event.getCreatedAt());
        assertTrue(event.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void logSuperAdmin_Actions_WhenProducerThrows_ShouldPropagateException() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("erroruser@example.com");
        UUID adminId = UUID.randomUUID();
        SuperAdminActionType superAdminActionType = SuperAdminActionType.USER_CREATED;

        doThrow(new RuntimeException("Kafka error"))
                .when(superAdminActionLogProducer).sendSuperAdminActionEvents(anyList());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                superAdminActionLogger.userRoleChangedEvent(user, adminId, superAdminActionType)
        );
        assertEquals("Kafka error", ex.getMessage());
    }

    @Test
    void logUserCreationByAdmin_WithNullUser_ShouldThrowNullPointerExceptionActions() {
        UUID adminId = UUID.randomUUID();
        SuperAdminActionType superAdminActionType = SuperAdminActionType.USER_CREATED;

        assertThrows(NullPointerException.class, () ->
                superAdminActionLogger.userRoleChangedEvent(null, adminId, superAdminActionType)
        );

        verify(superAdminActionLogProducer, never()).sendSuperAdminActionEvents(anyList());
    }
}
