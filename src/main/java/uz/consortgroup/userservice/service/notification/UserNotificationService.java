package uz.consortgroup.userservice.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.request.NotificationCreateRequestDto;
import uz.consortgroup.userservice.asspect.annotation.AllAspect;
import uz.consortgroup.userservice.client.NotificationTaskClient;

@Service
@RequiredArgsConstructor
public class UserNotificationService {

    private final NotificationTaskClient notificationTaskClient;

    @AllAspect
    public void notifyUser(NotificationCreateRequestDto request) {
        notificationTaskClient.createNotification(request);
    }
}
