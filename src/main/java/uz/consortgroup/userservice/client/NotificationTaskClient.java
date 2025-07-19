package uz.consortgroup.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uz.consortgroup.core.api.v1.dto.user.request.NotificationCreateRequestDto;

@FeignClient(name = "notification-service", url = "${notification.service.url}")
public interface NotificationTaskClient {

    @PostMapping("/api/v1/notifications")
    void createNotification(@RequestBody NotificationCreateRequestDto request);
}
