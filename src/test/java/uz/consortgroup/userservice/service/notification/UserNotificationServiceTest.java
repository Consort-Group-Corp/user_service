package uz.consortgroup.userservice.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.user.request.NotificationCreateRequestDto;
import uz.consortgroup.userservice.client.NotificationTaskClient;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserNotificationServiceTest {

    @Mock
    private NotificationTaskClient notificationTaskClient;

    @InjectMocks
    private UserNotificationService userNotificationService;

    @Test
    void notifyUser_shouldCallNotificationTaskClient() {
        NotificationCreateRequestDto request = new NotificationCreateRequestDto();
        request.setRecipientUserIds(List.of(UUID.randomUUID()));

        userNotificationService.notifyUser(request);

        verify(notificationTaskClient, times(1)).createNotification(request);
    }


    @Test
    void notifyUser_shouldPassRequestWithoutModification() {
        NotificationCreateRequestDto request = new NotificationCreateRequestDto();
        request.setActive(true);
        request.setRecipientUserIds(List.of(UUID.randomUUID()));

        userNotificationService.notifyUser(request);
        
        verify(notificationTaskClient).createNotification(argThat(argument -> 
            argument.getActive() == request.getActive()
        ));
    }


    @Test
    void notifyUser_shouldPropagateClientException() {
        NotificationCreateRequestDto request = new NotificationCreateRequestDto();
        doThrow(new RuntimeException("Test exception")).when(notificationTaskClient).createNotification(request);

        Exception exception = assertThrows(RuntimeException.class,
                () -> userNotificationService.notifyUser(request));

        assertEquals("Test exception", exception.getMessage());
        verify(notificationTaskClient, times(1)).createNotification(request);
    }
}