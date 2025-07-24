package uz.consortgroup.userservice.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.request.NotificationCreateRequestDto;
import uz.consortgroup.userservice.client.NotificationTaskClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserNotificationService {

    private final NotificationTaskClient notificationTaskClient;

    public void notifyUser(NotificationCreateRequestDto request) {
        log.info("Sending notification: communication={}, recipients={}, sendAt={}",
                request.getCommunication(), request.getRecipientUserIds(), request.getSendAt());

        notificationTaskClient.createNotification(request);

        log.info("Notification sent to {} users.", request.getRecipientUserIds().size());
    }
}
